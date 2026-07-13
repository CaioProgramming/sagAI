package com.ilustris.sagai.core.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.core.database.SagaDatabase
import com.ilustris.sagai.core.datastore.DataStorePreferences
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.home.data.model.getCurrentTimeLine
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * The `sceneSummary.notificationHook` used here is generated inline by the reply model on every
 * chat turn (see `ChatPrompts.REPLY_GENERATION_BLUEPRINT`), so this worker only needs to read the
 * latest timeline and schedule the alarm — no network call, so it can't fail from Doze/background
 * network restrictions the way the old LLM-backed version could.
 */
@HiltWorker
class NotificationGenerationWorker
    @AssistedInject
    constructor(
        @Assisted context: Context,
        @Assisted params: WorkerParameters,
        private val sagaRepository: SagaDatabase,
        private val dataStore: DataStorePreferences,
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            return try {
                val sagaId = inputData.getInt(KEY_SAGA_ID, -1)
                if (sagaId == -1) {
                    Timber.e("Invalid saga ID provided")
                    return Result.failure()
                }

                if (androidx.lifecycle.ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(
                        androidx.lifecycle.Lifecycle.State.STARTED,
                    )
                ) {
                    Timber.w("App is in foreground, aborting notification scheduling")
                    return Result.success()
                }

                Timber.d("Scheduling notification for saga: $sagaId")

                val sagaContent = sagaRepository.sagaDao().getSagaContent(sagaId).first()
                if (sagaContent == null) {
                    Timber.e("Saga not found: $sagaId")
                    return Result.failure()
                }

                val timelineData = sagaContent.getCurrentTimeLine()?.data
                val sceneSummary = timelineData?.sceneSummary
                val message = sceneSummary?.notificationHook

                if (message.isNullOrBlank()) {
                    Timber.w("No notificationHook available for saga: $sagaId, skipping schedule")
                    return Result.success()
                }

                val selectedCharacter =
                    sagaContent.findCharacter(sceneSummary.notificationCharacterName)
                        ?: sagaContent.mainCharacter

                if (selectedCharacter == null) {
                    Timber.e("No character available to attribute notification for saga: $sagaId")
                    return Result.success()
                }

                val currentTime = System.currentTimeMillis()
                val scheduledTime = currentTime + getNotificationDelay()

                val notification =
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
                    scheduledTime,
                    pendingIntent,
                )

                Timber.i("Notification scheduled: ${notification.toJsonFormat()}")

                Result.success()
            } catch (e: Exception) {
                Timber.e(e, "Failed to schedule notification")
                Result.failure()
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
