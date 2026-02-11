package com.ilustris.sagai.core.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.ChatPrompts
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.core.datastore.DataStorePreferences
import com.ilustris.sagai.core.utils.DateFormatOption
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.formatDate
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
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
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            return try {
                withTimeout(20.seconds) {
                    Timber.d("Generating notification...")
                    val sagaId = inputData.getInt(KEY_SAGA_ID, -1)
                    if (sagaId == -1) {
                        Timber.e("Invalid saga ID provided")
                        return@withTimeout Result.failure()
                    }

                    Timber.d("Starting notification generation for saga: $sagaId")

                    val sagaContent = sagaRepository.sagaDao().getSagaContent(sagaId).first()
                    if (sagaContent == null) {
                        Timber.e("Saga not found: $sagaId")
                        return@withTimeout Result.failure()
                    }

                    if (sagaContent.data.isEnded) {
                        Timber.w("Saga has ended, skipping notification: $sagaId")
                        return@withTimeout Result.success()
                    }

                    // Verificar se tem personagens
                    if (sagaContent.characters.isEmpty()) {
                        Timber.e("No characters found for saga: $sagaId")
                        return@withTimeout Result.failure()
                    }

                    if (sagaContent.flatMessages().isEmpty()) {
                        Timber.e("No messages found for saga: $sagaId")
                        return@withTimeout Result.failure()
                    }

                    // Gerar resumo da cena
                    val sceneSummary =
                        executeRequest {
                            gemmaClient.generate<SceneSummary>(
                                ChatPrompts.sceneSummarizationPrompt(
                                    saga = sagaContent,
                                ),
                            )
                        }.getSuccess()

                    // Gerar mensagem smart
                    val (character, generatedMessage) =
                        sceneSummary?.let {
                            val selectedCharacter =
                                it.charactersPresent
                                    .mapNotNull { characterName ->
                                        sagaContent.characters.find { char ->
                                            char.data.name == characterName
                                        }
                                    }.randomOrNull() ?: sagaContent.characters.first()

                            val prompt =
                                ChatPrompts.scheduledNotificationPrompt(
                                    saga = sagaContent,
                                    selectedCharacter = selectedCharacter,
                                    sceneSummary = sceneSummary,
                                )

                            delay(3.seconds)

                            val message =
                                gemmaClient.generate<String>(prompt, useCore = true) ?: emptyString()

                            selectedCharacter to message
                        } ?: run {
                            // Fallback: usar primeiro personagem
                            sagaContent.characters.first() to emptyString()
                        }

                    // Criar notificação agendada
                    val currentTime = System.currentTimeMillis()
                    val scheduledTime = currentTime + getNotificationDelay()

                    val notification =
                        ScheduledNotification(
                            sagaId = sagaContent.data.id.toString(),
                            sagaTitle = sagaContent.data.title,
                            characterId = character.data.id.toString(),
                            characterName = character.data.name,
                            characterAvatarPath = character.data.image,
                            generatedMessage =
                                generatedMessage.ifEmpty {
                                    applicationContext.getString(R.string.smart_notification_fallback)
                                },
                            exitTimestamp = currentTime,
                            scheduledTimestamp = scheduledTime,
                            generationTimestamp = currentTime,
                        )

                    // Salvar no DataStore
                    dataStore.setString(
                        ScheduledNotificationServiceImpl.SCHEDULED_NOTIFICATION_JSON_KEY,
                        notification.toJsonFormat(),
                    )

                    // Agendar via AlarmManager
                    val alarmManager =
                        applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val intent =
                        Intent(applicationContext, ScheduledNotificationReceiver::class.java).apply {
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
                        scheduledTime,
                        pendingIntent,
                    )

                    Timber.d(
                        "Notification scheduled at: ${
                            notification.scheduledTimestamp.formatDate(
                                DateFormatOption.HOUR_MINUTE_DAY_OF_MONTH_YEAR,
                            )
                        }",
                    )
                    Timber.i("Generated notification: $notification")

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
            const val KEY_SAGA_ID = "saga_id"
            const val WORK_TAG_PREFIX = "notification_"

            private const val NOTIFICATION_DELAY_PRODUCTION_HOURS = 2
            private const val NOTIFICATION_DELAY_DEBUG_MINUTES = 30
        }
    }
