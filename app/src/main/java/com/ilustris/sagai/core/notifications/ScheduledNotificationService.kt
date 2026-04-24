package com.ilustris.sagai.core.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.ilustris.sagai.core.datastore.DataStorePreferences
import kotlinx.coroutines.Dispatchers
import timber.log.Timber
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

interface ScheduledNotificationService {
    suspend fun scheduleNotification(sagaId: Int)

    fun cancelScheduledNotifications()
}

class ScheduledNotificationServiceImpl(
    private val context: Context,
    private val alarmManager: AlarmManager,
    private val workManagerScheduler: WorkManagerScheduler,
    private val dataStore: DataStorePreferences,
) : ScheduledNotificationService {
    override suspend fun scheduleNotification(sagaId: Int) {
        withContext(Dispatchers.IO) {
            Timber.d("Scheduling notification for saga: $sagaId")

            workManagerScheduler.scheduleNotificationWork(sagaId)
        }
    }

    override fun cancelScheduledNotifications() {
        workManagerScheduler.cancelAllNotificationWork()
        alarmManager.cancel(getNotificationIntent())
        runBlocking {
            dataStore.removeKey(SCHEDULED_NOTIFICATION_JSON_KEY)
        }
        Timber.i("Canceled scheduled notifications")
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

    companion object {
        const val SCHEDULED_NOTIFICATION_JSON_KEY = "scheduled_notification_json"
    }
}
