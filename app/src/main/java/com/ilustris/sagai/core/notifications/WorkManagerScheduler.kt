package com.ilustris.sagai.core.notifications

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.ilustris.sagai.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

interface WorkManagerScheduler {
    fun scheduleNotificationWork(sagaId: Int)

    fun cancelNotificationWork(sagaId: Int)

    fun cancelAllNotificationWork()
}

@Singleton
class WorkManagerSchedulerImpl
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : WorkManagerScheduler {
        private val workManager = WorkManager.getInstance(context)

        override fun scheduleNotificationWork(sagaId: Int) {
            val delay = getNotificationDelay()
            val workTag = "${NotificationGenerationWorker.WORK_TAG_PREFIX}$sagaId"

            val constraints =
                Constraints
                    .Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()

            val workRequest =
                OneTimeWorkRequestBuilder<NotificationGenerationWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setConstraints(constraints)
                    .setInputData(
                        workDataOf(
                            NotificationGenerationWorker.KEY_SAGA_ID to sagaId,
                        ),
                    ).setBackoffCriteria(
                        BackoffPolicy.LINEAR,
                        15,
                        TimeUnit.SECONDS,
                    ).addTag(workTag)
                    .build()

            // Usa ExistingWorkPolicy.REPLACE para evitar duplicatas
            workManager.enqueueUniqueWork(
                workTag,
                ExistingWorkPolicy.REPLACE,
                workRequest,
            )

            Log.d(TAG, "Scheduled notification work for saga: $sagaId with delay: ${delay}ms")
        }

        override fun cancelNotificationWork(sagaId: Int) {
            val workTag = "${NotificationGenerationWorker.WORK_TAG_PREFIX}$sagaId"
            workManager.cancelAllWorkByTag(workTag)
            Log.d(TAG, "Cancelled notification work for saga: $sagaId")
        }

        override fun cancelAllNotificationWork() {
            workManager.cancelAllWorkByTag(NotificationGenerationWorker.WORK_TAG_PREFIX)
            Log.d(TAG, "Cancelled all notification work")
        }

        private fun getNotificationDelay(): Long =
            when {
                BuildConfig.DEBUG -> NOTIFICATION_DELAY_DEBUG_MINUTES * 60 * 1000L
                else -> NOTIFICATION_DELAY_PRODUCTION_HOURS * 60 * 60 * 1000L
            }

        companion object {
            private const val TAG = "WorkManagerScheduler"
            private const val NOTIFICATION_DELAY_PRODUCTION_HOURS = 2
            private const val NOTIFICATION_DELAY_DEBUG_MINUTES = 1
        }
    }
