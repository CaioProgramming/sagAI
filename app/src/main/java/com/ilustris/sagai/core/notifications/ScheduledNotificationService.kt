package com.ilustris.sagai.core.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.ChatPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.datastore.DataStorePreferences
import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.core.utils.DateFormatOption
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.formatDate
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds

interface ScheduledNotificationService {
    suspend fun scheduleNotification(sagaContent: SagaContent)

    fun cancelScheduledNotifications()
}

class ScheduledNotificationServiceImpl(
    private val context: Context,
    private val alarmManager: AlarmManager,
    private val gemmaClient: GemmaClient,
    private val dataStore: DataStorePreferences,
) : ScheduledNotificationService {
    override suspend fun scheduleNotification(sagaContent: SagaContent) {
        withContext(Dispatchers.IO) {
            if (sagaContent.data.isEnded) {
                Log.e(
                    javaClass.simpleName,
                    "scheduleNotification: Ended sagas should not send notifications.",
                )
            }
            val generatedMessage =
                createSmartMessage(
                    sagaContent,
                ).getSuccess()!!

            val exitTime = System.currentTimeMillis()
            val scheduledTime = exitTime + getNotificationDelay()

            val notification =
                ScheduledNotification(
                    sagaId = sagaContent.data.id.toString(),
                    sagaTitle = sagaContent.data.title,
                    characterId =
                        generatedMessage.first.data.id
                            .toString(),
                    characterName = generatedMessage.first.data.name,
                    characterAvatarPath = generatedMessage.first.data.image,
                    generatedMessage =
                        generatedMessage.second.ifEmpty {
                            context.getString(R.string.smart_notification_fallback)
                        },
                    exitTimestamp = exitTime,
                    scheduledTimestamp = scheduledTime,
                    generationTimestamp = System.currentTimeMillis(),
                )

            dataStore.setString(
                SCHEDULED_NOTIFICATION_JSON_KEY,
                notification.toJsonFormat(),
            )

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                scheduledTime,
                getNotificationIntent(),
            )

            Log.d(
                javaClass.simpleName,
                "scheduleNotification: message wil be sent at: ${
                    notification.scheduledTimestamp.formatDate(
                        DateFormatOption.HOUR_MINUTE_DAY_OF_MONTH_YEAR,
                    )
                }",
            )
            Log.i(javaClass.simpleName, "Scheduled message -> $notification")
        }
    }

    override fun cancelScheduledNotifications() {
        alarmManager.cancel(getNotificationIntent())
        runBlocking {
            dataStore.removeKey(SCHEDULED_NOTIFICATION_JSON_KEY)
        }
        Log.i(javaClass.simpleName, "Canceled scheduled notifications")
    }

    private fun getNotificationIntent(): PendingIntent {
        val intent =
            Intent(context, ScheduledNotificationReceiver::class.java).apply {
                action = "com.ilustris.sagai.SCHEDULED_NOTIFICATION"
            }
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private suspend fun getSceneContext(saga: SagaContent): RequestResult<SceneSummary?> =
        executeRequest {
            gemmaClient.generate<SceneSummary>(
                ChatPrompts.sceneSummarizationPrompt(
                    saga = saga,
                    recentMessages =
                        saga
                            .flatMessages()
                            .map { it.message }
                            .takeLast(UpdateRules.LORE_UPDATE_LIMIT),
                ),
            )
        }

    private suspend fun createSmartMessage(sagaContent: SagaContent) =
        executeRequest {
            val sceneSummary = getSceneContext(sagaContent).getSuccess()

            val smartMessage =
                sceneSummary?.let {
                    val character =
                        it.charactersPresent
                            .map {
                                sagaContent.findCharacter(it)
                            }.random()!!

                    val prompt =
                        ChatPrompts.scheduledNotificationPrompt(
                            sagaContent,
                            character,
                            sceneSummary,
                        )
                    delay(2.seconds)
                    val message =
                        gemmaClient.generate<String>(prompt, useCore = true) ?: emptyString()
                    character to message
                } ?: run {
                    sagaContent.mainCharacter!! to emptyString()
                }

            smartMessage
        }

    companion object {
        private const val NOTIFICATION_DELAY_PRODUCTION_HOURS = 2
        private const val NOTIFICATION_DELAY_DEBUG_MINUTES = 1
        private const val NOTIFICATION_DELAY_TESTING_MINUTES = 3
        const val SCHEDULED_NOTIFICATION_JSON_KEY = "scheduled_notification_json"

        private fun getNotificationDelay(): Long =
            when {
                BuildConfig.DEBUG -> NOTIFICATION_DELAY_DEBUG_MINUTES * 60 * 1000L
                else -> NOTIFICATION_DELAY_PRODUCTION_HOURS * 60 * 60 * 1000L
            }
    }
}
