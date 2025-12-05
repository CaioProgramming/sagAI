package com.ilustris.sagai.core.media.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.ilustris.sagai.MainActivity
import com.ilustris.sagai.R
import com.ilustris.sagai.core.media.SagaMediaService
import com.ilustris.sagai.core.media.model.PlaybackMetadata
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.core.permissions.NotificationUtils
import com.ilustris.sagai.core.file.scaleBitmapForNotification // Import the new utility function
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.ui.navigation.Routes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaNotificationManagerImpl
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val fileHelper: FileHelper,
    ) : MediaNotificationManager {
        companion object {
            const val MEDIA_NOTIFICATION_ID = 12346
            private const val REQUEST_CODE_PLAY = 101
            private const val REQUEST_CODE_PAUSE = 102
            private const val TARGET_LARGE_ICON_SIZE_PX = 128
        }

        override fun showPlaybackNotification(
            playbackMetadata: PlaybackMetadata,
            isPlaying: Boolean,
            sessionToken: MediaSessionCompat.Token?,
        ): Notification? {
            val openAppIntent =
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    val chatRoute = Routes.CHAT
                    val deepLinkUriString =
                        chatRoute.deepLink
                            ?.replace("{sagaId}", playbackMetadata.sagaId.toString())
                            ?.replace("{isDebug}", "false")

                    putExtra("deepLink", deepLinkUriString)
                }
            val openAppPendingIntent: PendingIntent =
                PendingIntent.getActivity(
                    context,
                    0,
                    openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

            val scaledLargeIconBitmap =
                scaleBitmapForNotification(
                    fileHelper.readFile(playbackMetadata.sagaIcon),
                    TARGET_LARGE_ICON_SIZE_PX,
                )

            // Build notification based on Android version
            return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                // Android 16+ Live Updates with ProgressStyle
                buildLiveUpdatesNotification(
                    playbackMetadata,
                    isPlaying,
                    openAppPendingIntent,
                    scaledLargeIconBitmap
                )
            } else {
                // Fallback for older versions
                buildFallbackNotification(
                    playbackMetadata,
                    isPlaying,
                    openAppPendingIntent,
                    scaledLargeIconBitmap
                )
            }
        }

        @androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM)
        private fun buildLiveUpdatesNotification(
            playbackMetadata: PlaybackMetadata,
            isPlaying: Boolean,
            contentIntent: PendingIntent,
            largeIcon: android.graphics.Bitmap?
        ): Notification {
            val pauseIntent =
                Intent(context, SagaMediaService::class.java).apply {
                    action = SagaMediaService.ACTION_PAUSE
                }
            val pausePendingIntent: PendingIntent =
                PendingIntent.getService(
                    context,
                    REQUEST_CODE_PAUSE,
                    pauseIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

            val playIntent =
                Intent(context, SagaMediaService::class.java).apply {
                    action = SagaMediaService.ACTION_PLAY
                    putExtra(
                        SagaMediaService.EXTRA_SAGA_CONTENT_JSON,
                        playbackMetadata.toJsonFormat(),
                    )
                }
            val playPendingIntent: PendingIntent =
                PendingIntent.getService(
                    context,
                    REQUEST_CODE_PLAY,
                    playIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

            // Calculate progress (example: based on act number)
            val currentProgress = playbackMetadata.currentActNumber
            val maxProgress = playbackMetadata.totalActs.coerceAtLeast(1)

            val progressStyle = Notification.ProgressStyle()
                .setProgress(maxProgress, currentProgress, false)

            return Notification.Builder(context, NotificationUtils.MEDIA_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_spark)
                .setContentTitle(playbackMetadata.sagaTitle)
                .setContentText(playbackMetadata.timelineObjective)
                .setContentIntent(contentIntent)
                .setOngoing(isPlaying)
                .setColorized(true)
                .setColor(playbackMetadata.color)
                .setLargeIcon(largeIcon)
                .setStyle(progressStyle)
                .addAction(
                    if (isPlaying) R.drawable.round_pause_24 else R.drawable.round_play_arrow_24,
                    if (isPlaying) context.getString(R.string.notification_action_pause) 
                    else context.getString(R.string.notification_action_play),
                    if (isPlaying) pausePendingIntent else playPendingIntent
                )
                .build()
        }

        private fun buildFallbackNotification(
            playbackMetadata: PlaybackMetadata,
            isPlaying: Boolean,
            contentIntent: PendingIntent,
            largeIcon: android.graphics.Bitmap?
        ): Notification {
            val notificationBuilder =
                NotificationCompat
                    .Builder(context, NotificationUtils.MEDIA_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_spark)
                    .setContentTitle(playbackMetadata.sagaTitle)
                    .setContentText(playbackMetadata.timelineObjective)
                    .setContentIntent(contentIntent)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setOngoing(isPlaying)
                    .setColorized(true)
                    .setColor(playbackMetadata.color)
                    .setLargeIcon(largeIcon)
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(playbackMetadata.timelineObjective)
                    )

            if (isPlaying) {
                val pauseIntent =
                    Intent(context, SagaMediaService::class.java).apply {
                        action = SagaMediaService.ACTION_PAUSE
                    }
                val pausePendingIntent: PendingIntent =
                    PendingIntent.getService(
                        context,
                        REQUEST_CODE_PAUSE,
                        pauseIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    )
                notificationBuilder.addAction(
                    R.drawable.round_pause_24,
                    context.getString(R.string.notification_action_pause),
                    pausePendingIntent,
                )
            } else {
                val playIntent =
                    Intent(context, SagaMediaService::class.java).apply {
                        action = SagaMediaService.ACTION_PLAY
                        putExtra(
                            SagaMediaService.EXTRA_SAGA_CONTENT_JSON,
                            playbackMetadata.toJsonFormat(),
                        )
                    }
                val playPendingIntent: PendingIntent =
                    PendingIntent.getService(
                        context,
                        REQUEST_CODE_PLAY,
                        playIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    )
                notificationBuilder.addAction(
                    R.drawable.round_play_arrow_24,
                    context.getString(R.string.notification_action_play),
                    playPendingIntent,
                )
            }

            return notificationBuilder.build()
        }

        override fun cancelPlaybackNotification() {
            NotificationManagerCompat.from(context).cancel(MEDIA_NOTIFICATION_ID)
        }
    }
