package com.ilustris.sagai.core.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.ChatPrompts
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.core.datastore.DataStorePreferences
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.home.data.model.getCurrentTimeLine
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import kotlin.time.Duration.Companion.seconds

@HiltWorker
class NotificationGenerationWorker
    @AssistedInject
    constructor(
        @Assisted context: Context,
        @Assisted params: WorkerParameters,
        private val gemmaClient: GemmaClient,
        private val sagaRepository: SagaDatabase,
        private val dataStore: DataStorePreferences,
        private val genreConfigService: GenreConfigService,
        private val promptService: com.ilustris.sagai.core.ai.services.PromptService,
        private val remoteConfigService: com.ilustris.sagai.core.services.RemoteConfigService,
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            return try {
                withTimeout(60.seconds) {
                    Timber.d("Generating notification...")
                    val sagaId = inputData.getInt(KEY_SAGA_ID, -1)
                    if (sagaId == -1) {
                        Timber.e("Invalid saga ID provided")
                        return@withTimeout Result.failure()
                    }

                    if (androidx.lifecycle.ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(
                            androidx.lifecycle.Lifecycle.State.STARTED,
                        )
                    ) {
                        Timber.w("App is in foreground, aborting notification generation")
                        return@withTimeout Result.success()
                    }

                    Timber.d("Starting notification generation for saga: $sagaId")

                    val sagaContent = sagaRepository.sagaDao().getSagaContent(sagaId).first()
                    if (sagaContent == null) {
                        Timber.e("Saga not found: $sagaId")
                        return@withTimeout Result.failure()
                    }

                    val notification =
                        sagaContent.getCurrentTimeLine()?.data?.sceneSummary?.let {
                            val selectedCharacter =
                                sagaContent.findCharacter(it.charactersPresent.randomOrNull())
                                    ?: sagaContent.mainCharacter
                            val conversationDirective =
                                genreConfigService.conversationBlueprint(sagaContent.data.genre)

                            val prompt =
                                ChatPrompts.scheduledNotificationPrompt(
                                    promptService = promptService,
                                    saga = sagaContent,
                                    selectedCharacter = selectedCharacter!!,
                                    sceneSummary = it,
                                    conversationDirective = conversationDirective,
                                )

                            val currentTime = System.currentTimeMillis()
                            val scheduledTime = currentTime + getNotificationDelay()

                            val message = gemmaClient.generate<String>(prompt, useCore = true)!!

                            selectedCharacter to message

                            ScheduledNotification(
                                sagaId = sagaContent.data.id.toString(),
                                sagaTitle = sagaContent.data.title,
                                characterId = selectedCharacter.data.id.toString(),
                                characterName = selectedCharacter.data.name,
                                characterAvatarPath = selectedCharacter.data.image,
                                generatedMessage = message,
                                exitTimestamp = currentTime,
                                scheduledTimestamp = scheduledTime,
                                generationTimestamp = currentTime,
                            )
                        }

                    notification?.let {
                        dataStore.setString(
                            ScheduledNotificationServiceImpl.SCHEDULED_NOTIFICATION_JSON_KEY,
                            notification.toJsonFormat(),
                        )

                        val alarmManager =
                            applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        val intent =
                            Intent(
                                applicationContext,
                                ScheduledNotificationReceiver::class.java,
                            ).apply {
                                action = "com.ilustris.sagai.SCHEDULED_NOTIFICATION"
                            }
                        val pendingIntent =
                            PendingIntent.getBroadcast(
                                applicationContext,
                                0,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                            )

                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            it.scheduledTimestamp,
                            pendingIntent,
                        )

                        Timber.i("Notification scheduled: ${notification.toJsonFormat()}")
                    }

                    Result.success()
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to generate notification")
                Result.retry()
            }
        }

        private fun getNotificationDelay(): Long =
            when {
                BuildConfig.DEBUG -> NOTIFICATION_DELAY_DEBUG_MINUTES * 60 * 1000L
                else -> NOTIFICATION_DELAY_PRODUCTION_HOURS * 60 * 60 * 1000L
            }

        companion object {
            const val TAG = "NotificationWorker"
            const val KEY_SAGA_ID = "saga_id"
            const val WORK_TAG_PREFIX = "notification_"

            private const val NOTIFICATION_DELAY_PRODUCTION_HOURS = 2
            private const val NOTIFICATION_DELAY_DEBUG_MINUTES = 30
        }
    }
