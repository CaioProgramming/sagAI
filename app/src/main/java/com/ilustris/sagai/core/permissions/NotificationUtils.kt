package com.ilustris.sagai.core.permissions

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.os.Build

object NotificationUtils {
    const val CHAT_CHANNEL_ID = "SAGAI_CHAT_CHANNEL_ID"
    private const val CHAT_CHANNEL_NAME = "SagaAI Chat" // User-visible name
    private const val CHAT_CHANNEL_DESCRIPTION = "Notifications for new messages in your sagas." // User-visible description
    const val CHAT_NOTIFICATION_ID = 1


    fun createChatNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel =
                NotificationChannel(CHAT_CHANNEL_ID, CHAT_CHANNEL_NAME, importance).apply {
                    description = CHAT_CHANNEL_DESCRIPTION
                }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun displayChatMessageNotification(
        context: Context,
        title: String,
        message: String,
        smallIconResId: Int, // e.g., R.drawable.ic_spark or your monochrome app icon
        largeIcon: Bitmap?,
        pendingIntent: PendingIntent?,
        // soundUri: Uri? // We will add custom sound in a later step
    ) {
        // ... (existing implementation)
    }
}
