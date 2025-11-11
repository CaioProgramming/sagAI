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
import com.ilustris.sagai.core.media.MediaPlayerService
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
            val notificationBuilder =
                NotificationCompat
                    .Builder(context, NotificationUtils.MEDIA_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_spark)
                    .setContentTitle(playbackMetadata.sagaTitle)
                    .setContentText(
                        context.getString(
                            R.string.notification_playing_act,
                            playbackMetadata.currentActNumber.toString(),
                        ),
                    ).setContentIntent(openAppPendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setOngoing(isPlaying)
                    .setColorized(true)
                    .setColor(playbackMetadata.color)
            // .setLargeIcon(scaledLargeIconBitmap)

            if (isPlaying) {
                val pauseIntent =
                    Intent(context, MediaPlayerService::class.java).apply {
                        action = MediaPlayerService.ACTION_PAUSE
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
                    Intent(context, MediaPlayerService::class.java).apply {
                        action = MediaPlayerService.ACTION_PLAY
                        putExtra(
                            MediaPlayerService.EXTRA_SAGA_CONTENT_JSON,
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

            val mediaStyle =
                MediaStyle()
                    .setMediaSession(sessionToken)
                    .setShowActionsInCompactView(0) // Only Play/Pause action in compact view

            notificationBuilder.setStyle(mediaStyle)

            return notificationBuilder.build()
        }

        override fun cancelPlaybackNotification() {
            NotificationManagerCompat.from(context).cancel(MEDIA_NOTIFICATION_ID)
        }
    }
