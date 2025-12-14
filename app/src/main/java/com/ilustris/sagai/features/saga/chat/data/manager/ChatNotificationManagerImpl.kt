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
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import com.ilustris.sagai.MainActivity
import com.ilustris.sagai.R
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.core.file.cropBitmapToCircle
import com.ilustris.sagai.core.lifecycle.AppLifecycleManager
import com.ilustris.sagai.core.permissions.NotificationUtils.CHAT_CHANNEL_ID
import com.ilustris.sagai.core.permissions.NotificationUtils.CHAT_NOTIFICATION_ID
import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.manager.ChatNotificationManager
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.model.joinMessage
import com.ilustris.sagai.ui.components.NotificationStyle
import com.ilustris.sagai.ui.components.SnackBarState
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
                smallIconResId = saga.data.genre.background,
            )
        }

        override fun sendNotification(
            saga: Saga,
            title: String,
            body: String,
            smallIcon: Bitmap?,
            largeIcon: Bitmap?,
        ) {
            if (appLifecycleManager.isAppInForeground.value) {
                Log.i(
                    javaClass.simpleName,
                    "App is in foreground. Skipping notification for: $title",
                )
                return
            }

            Log.i(
                javaClass.simpleName,
                "App is in background. Proceeding with notification: $title",
            )

            val chatRoute = Routes.CHAT
            val formatChatDeepLink =
                chatRoute.deepLink
                    ?.replace("{sagaId}", saga.id.toString())
                    ?.replace("isDebug", "false")

            val finalLargeIcon =
                largeIcon
                    ?: try {
                        android.graphics.BitmapFactory.decodeResource(
                            context.resources,
                            saga.genre.background,
                        )
                    } catch (e: Exception) {
                        null
                    }

            // Crop icon to circle for better appearance
            val croppedIcon = cropBitmapToCircle(finalLargeIcon)

            // Create Person for messaging style
            val person =
                Person
                    .Builder()
                    .setName(title)
                    .setIcon(croppedIcon?.let { IconCompat.createWithBitmap(it) })
                    .build()

            // Create MessagingStyle for chat-like appearance
            val messagingStyle =
                NotificationCompat
                    .MessagingStyle(person)
                    .setConversationTitle(saga.title)
                    .addMessage(
                        body,
                        System.currentTimeMillis(),
                        person,
                    )

            val pendingIntent = createPendingIntent(formatChatDeepLink)

            // Use app icon for small icon
            val finalSmallIconResId =
                try {
                    context.resources.getDrawable(saga.genre.background, null)
                    saga.genre.background
                } catch (e: Exception) {
                    R.drawable.ic_spark
                }

            val builder =
                NotificationCompat
                    .Builder(context, CHAT_CHANNEL_ID)
                    .setSmallIcon(finalSmallIconResId)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setColor(saga.genre.color.toArgb())
                    .setColorized(true)
                    .setAutoCancel(true)
                    .setStyle(messagingStyle)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)

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

        override fun clearNotifications() {
            NotificationManagerCompat.from(context).cancel(CHAT_NOTIFICATION_ID)
        }

        private fun chatDeepLink(sagaId: String): String {
            val chatRoute = Routes.CHAT
            return chatRoute.deepLink
                ?.replace("{sagaId}", sagaId)
                ?.replace("isDebug", "false") ?: ""
        }

        fun sendSnackBarNotification(
            saga: SagaContent,
            snackBarState: SnackBarState,
        ) {
            if (appLifecycleManager.isAppInForeground.value || snackBarState.showInUi) {
                Log.i(
                    javaClass.simpleName,
                    "App is in foreground or notification is UI-only. Skipping notification.",
                )
                return
            }

            Log.i(
                javaClass.simpleName,
                "App is in background. Sending notification with style: ${snackBarState.notificationStyle}",
            )

            val chatRoute = Routes.CHAT
            val formatChatDeepLink =
                chatRoute.deepLink
                    ?.replace("{sagaId}", saga.data.id.toString())
                    ?.replace("isDebug", "false")

            when (snackBarState.notificationStyle) {
                NotificationStyle.CHAT -> {
                    /*sendChatNotification(
                        saga = saga,
                        title = saga.data.title,

                        message = snackBarState.message,
                        largeIcon = snackBarState.largeIcon,
                        pendingIntent = createPendingIntent(formatChatDeepLink),
                    )*/
                }

                NotificationStyle.DEFAULT -> {
                    sendToNotificationChannel(
                        title = saga.data.title,
                        content = snackBarState.message,
                        largeIcon = snackBarState.largeIcon,
                        pendingIntent = createPendingIntent(formatChatDeepLink),
                        genreColor = saga.data.genre.color,
                        smallIconResId = R.drawable.ic_spark,
                        priority = NotificationCompat.PRIORITY_DEFAULT,
                    )
                }

                NotificationStyle.MINIMAL -> {
                    sendMinimalNotification(
                        saga = saga,
                        message = snackBarState.message,
                        pendingIntent = createPendingIntent(formatChatDeepLink),
                    )
                }
            }
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

        override fun sendChatNotification(
            saga: Saga,
            title: String,
            character: Character?,
            message: String,
            largeIcon: Bitmap?,
        ) {
            val pendingIntent = createPendingIntent(chatDeepLink(saga.id.toString()))
            // Extract character name from message if available, or use saga title
            val characterName = character?.name ?: saga.title
            val finalIcon = cropBitmapToCircle(largeIcon)
            // Create Person for the sender
            val person =
                Person
                    .Builder()
                    .setName(characterName)
                    .setIcon(finalIcon?.let { IconCompat.createWithBitmap(it) })
                    .build()

            // Create MessagingStyle for chat-like appearance
            val messagingStyle =
                NotificationCompat
                    .MessagingStyle(person)
                    .setConversationTitle(title)
                    .addMessage(
                        message,
                        System.currentTimeMillis(),
                        person,
                    )

            val builder =
                NotificationCompat
                    .Builder(context, CHAT_CHANNEL_ID)
                    .setSmallIcon(saga.genre.background)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setColor(saga.genre.color.toArgb())
                    .setColorized(true)
                    .setAutoCancel(true)
                    .setStyle(messagingStyle)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)

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

        private fun sendMinimalNotification(
            saga: SagaContent,
            message: String,
            pendingIntent: PendingIntent?,
        ) {
            val builder =
                NotificationCompat
                    .Builder(context, CHAT_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_spark)
                    .setContentTitle(saga.data.title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setContentIntent(pendingIntent)
                    .setColor(
                        saga.data.genre.color
                            .toArgb(),
                    ).setAutoCancel(true)

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

        private fun sendToNotificationChannel(
            title: String,
            content: String,
            largeIcon: Bitmap?,
            pendingIntent: PendingIntent?,
            genreColor: Color,
            smallIconResId: Int,
            priority: Int = NotificationCompat.PRIORITY_HIGH,
        ) {
            // Use app icon for small icon if the provided resource is not suitable
            val finalSmallIconResId =
                try {
                    // Validate if the resource exists and is drawable
                    context.resources.getDrawable(smallIconResId, null)
                    smallIconResId
                } catch (e: Exception) {
                    // Fallback to app icon if the genre background is not suitable for small icon
                    R.drawable.ic_spark
                }

            val builder =
                NotificationCompat
                    .Builder(context, CHAT_CHANNEL_ID)
                    .setSmallIcon(finalSmallIconResId)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setLargeIcon(largeIcon)
                    .setPriority(priority)
                    .setContentIntent(pendingIntent)
                    .setColor(genreColor.toArgb())
                    .setColorized(true)
                    .setAutoCancel(true)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                    .setCategory(NotificationCompat.CATEGORY_SOCIAL)

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
