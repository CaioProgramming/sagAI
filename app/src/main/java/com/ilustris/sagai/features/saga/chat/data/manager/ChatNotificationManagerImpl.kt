package com.ilustris.sagai.features.saga.chat.domain.manager

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ilustris.sagai.MainActivity
import com.ilustris.sagai.R
import com.ilustris.sagai.core.lifecycle.AppLifecycleManager
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.core.permissions.NotificationUtils.CHAT_CHANNEL_ID
import com.ilustris.sagai.core.permissions.NotificationUtils.CHAT_NOTIFICATION_ID
import com.ilustris.sagai.core.file.cropBitmapToCircle
import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.model.joinMessage
import com.ilustris.sagai.ui.navigation.Routes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ChatNotificationManagerImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val fileHelper: FileHelper,
        private val appLifecycleManager: AppLifecycleManager,
    ) : ChatNotificationManager {
        companion object {
            const val MEDIA_NOTIFICATION_ID = 12346 // Unique ID for media notification
            const val ACTION_PLAY_MEDIA = "com.ilustris.sagai.ACTION_PLAY_MEDIA"
            const val ACTION_PAUSE_MEDIA = "com.ilustris.sagai.ACTION_PAUSE_MEDIA"
        }

        override fun sendMessageNotification(
            saga: SagaContent,
            message: MessageContent,
        ) {
            if (appLifecycleManager.isAppInForeground.value) {
                Log.i(
                    javaClass.simpleName,
                    "App is in foreground. Skipping notification for message: ${message.message.text}",
                )
                return
            }

            Log.i(
                javaClass.simpleName,
                "App is in background. Proceeding with notification for message: ${message.message.text}",
            )

            val characterIcon =
                cropBitmapToCircle(
                    fileHelper.readFile(message.character?.image),
                )

            val chatRoute = Routes.CHAT
            val formatChatDeepLink =
                chatRoute.deepLink
                    ?.replace("{sagaId}", saga.data.id.toString())
                    ?.replace("isDebug", "false")
            sendToNotificationChannel(
                title = context.getString(R.string.notification_title, saga.data.title),
                content = message.joinMessage().formatToString(),
                largeIcon = characterIcon,
                pendingIntent = createPendingIntent(formatChatDeepLink),
                genreColor = saga.data.genre.color,
                smallIconResId = R.drawable.ic_spark,
            )
        }

        private fun createPendingIntent(deepLink: String?): PendingIntent {
            val intent = Intent(context, MainActivity::class.java)
            deepLink?.let {
                intent.putExtra("deepLink", deepLink)
            }

            return PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        private fun sendToNotificationChannel(
            title: String,
            content: String,
            largeIcon: Bitmap?,
            pendingIntent: PendingIntent?,
            genreColor: Color,
            smallIconResId: Int,
        ) {
            val builder =
                NotificationCompat
                    .Builder(context, CHAT_CHANNEL_ID)
                    .setSmallIcon(smallIconResId)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setLargeIcon(largeIcon)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setColor(genreColor.toArgb())
                    .setColorized(true)
                    .setAutoCancel(true)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                NotificationManagerCompat
                    .from(context)
                    .notify(CHAT_NOTIFICATION_ID, builder.build())
            }
        }
    }
