package com.ilustris.sagai.core.media

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.google.gson.Gson
import com.ilustris.sagai.core.file.FileHelper
import com.ilustris.sagai.core.media.model.PlaybackMetadata
import com.ilustris.sagai.core.media.notification.MediaNotificationManager
import com.ilustris.sagai.core.media.notification.MediaNotificationManagerImpl
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class SagaMediaService : Service() {
    @Inject
    lateinit var mediaPlayerManager: MediaPlayerManager

    @Inject
    lateinit var notificationManager: MediaNotificationManager

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var fileHelper: FileHelper

    private lateinit var mediaSession: MediaSessionCompat
    private var currentPlaybackMetadata: PlaybackMetadata? = null
    private var isPausedByApp: Boolean = false

    private val TAG = SagaMediaService::class.java.simpleName

    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSessionCompat(this, "MediaPlayerService")
        mediaSession.setCallback(
            object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    super.onPlay()
                    Log.i(TAG, "MediaSession.Callback: onPlay called")
                    currentPlaybackMetadata?.let { metadata ->
                        startPlayback(metadata)
                    } ?: run {
                        Log.w(TAG, "MediaSession.Callback: onPlay called but currentPlaybackMetadata is null. Cannot start playback.")
                    }
                }

                @SuppressLint("MissingPermission")
                override fun onPause() {
                    super.onPause()
                    Log.i(TAG, "MediaSession.Callback: onPause called")
                    mediaPlayerManager.pause()
                    isPausedByApp = false
                    currentPlaybackMetadata?.let { metadata ->
                        val updatedNotification: Notification? =
                            notificationManager.showPlaybackNotification(
                                playbackMetadata = metadata,
                                isPlaying = false,
                                sessionToken = mediaSession.sessionToken,
                            )
                        if (updatedNotification != null) {
                            NotificationManagerCompat
                                .from(this@SagaMediaService)
                                .notify(MediaNotificationManagerImpl.MEDIA_NOTIFICATION_ID, updatedNotification)
                            Log.d(TAG, "Notification updated for PAUSE from MediaSession callback.")
                        }
                    }
                    val pausedState =
                        PlaybackStateCompat
                            .Builder()
                            .setActions(
                                PlaybackStateCompat.ACTION_PLAY or
                                    PlaybackStateCompat.ACTION_PLAY_PAUSE,
                            ).setState(PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                            .build()
                    mediaSession.setPlaybackState(pausedState)
                    Log.d(TAG, "MediaSession state updated to PAUSED from MediaSession callback.")
                }

                override fun onStop() {
                    super.onStop()
                    Log.i(TAG, "MediaSession.Callback: onStop called")
                    mediaPlayerManager.stop()
                    val stoppedState =
                        PlaybackStateCompat
                            .Builder()
                            .setState(PlaybackStateCompat.STATE_STOPPED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                            .build()
                    mediaSession.setPlaybackState(stoppedState)
                    Log.d(TAG, "MediaSession state updated to STOPPED from MediaSession callback.")
                    stopForeground(STOP_FOREGROUND_REMOVE)
                }
            },
        )
        mediaSession.isActive = true

        val initialState =
            PlaybackStateCompat
                .Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE,
                ).setState(PlaybackStateCompat.STATE_NONE, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                .build()
        mediaSession.setPlaybackState(initialState)
    }

    private fun startPlayback(playbackMetadata: PlaybackMetadata) {
        Log.d(TAG, "Attempting to play media file: ${playbackMetadata.mediaFilePath} with metadata: $playbackMetadata")
        this.currentPlaybackMetadata = playbackMetadata

        val mediaFile = File(playbackMetadata.mediaFilePath)
        if (!mediaFile.exists()) {
            Log.e(TAG, "Media file does not exist: ${playbackMetadata.mediaFilePath}")
            val errorState =
                PlaybackStateCompat
                    .Builder()
                    .setState(PlaybackStateCompat.STATE_ERROR, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                    .build()
            mediaSession.setPlaybackState(errorState)
            return
        }

        mediaPlayerManager.prepareDataSource(
            playbackMetadata.mediaFilePath,
            looping = true,
            onPrepared = {
                Log.i(TAG, "MediaPlayer prepared, starting playback for: ${playbackMetadata.mediaFilePath}")
                mediaPlayerManager.play()

                val notification: Notification? =
                    notificationManager.showPlaybackNotification(
                        playbackMetadata = playbackMetadata,
                        isPlaying = true,
                        sessionToken = mediaSession.sessionToken,
                    )

                if (notification != null) {
                    startForeground(MediaNotificationManagerImpl.MEDIA_NOTIFICATION_ID, notification)
                    Log.d(TAG, "Service started in foreground.")
                } else {
                    Log.e(TAG, "Failed to create notification for startForeground. Check permissions.")
                }

                val playingState =
                    PlaybackStateCompat
                        .Builder()
                        .setActions(
                            PlaybackStateCompat.ACTION_PAUSE or
                                PlaybackStateCompat.ACTION_PLAY_PAUSE,
                        ).setState(PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                        .build()
                mediaSession.setPlaybackState(playingState)
                Log.d(TAG, "MediaSession state updated to PLAYING.")

                val metadataBuilder =
                    MediaMetadataCompat
                        .Builder()
                        .putBitmap(MediaMetadataCompat.METADATA_KEY_ART, fileHelper.readFile(playbackMetadata.sagaIcon))
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, playbackMetadata.sagaTitle)
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, playbackMetadata.timelineObjective)
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, playbackMetadata.timelineObjective)
                        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, playbackMetadata.sagaTitle)
                        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, playbackMetadata.timelineObjective)
                mediaSession.setMetadata(metadataBuilder.build())
                Log.d(TAG, "MediaSession metadata updated for: ${playbackMetadata.sagaTitle}")
            },
            onError = { exception ->
                Log.e(TAG, "Error preparing MediaPlayer for: ${playbackMetadata.mediaFilePath}", exception)
                val errorState =
                    PlaybackStateCompat
                        .Builder()
                        .setState(PlaybackStateCompat.STATE_ERROR, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                        .build()
                mediaSession.setPlaybackState(errorState)
                Log.e(TAG, "MediaSession state updated to ERROR.")
            },
        )
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        val action = intent?.action
        Log.d(TAG, "onStartCommand received action: $action")

        when (action) {
            ACTION_PLAY -> {
                Log.i(TAG, "ACTION_PLAY received")
                val sagaContentJson = intent.getStringExtra(EXTRA_SAGA_CONTENT_JSON)
                if (sagaContentJson == null) {
                    Log.e(TAG, "ACTION_PLAY: sagaContentJson is null. Cannot get PlaybackMetadata.")
                    return START_NOT_STICKY
                }
                var playbackMetadataLocal: PlaybackMetadata? = null
                try {
                    playbackMetadataLocal = gson.fromJson(sagaContentJson, PlaybackMetadata::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "Error deserializing PlaybackMetadata from JSON", e)
                }

                if (playbackMetadataLocal == null) {
                    Log.e(TAG, "ACTION_PLAY: PlaybackMetadata is null. Cannot play.")
                    return START_NOT_STICKY
                }
                startPlayback(playbackMetadataLocal)
            }

            ACTION_PAUSE -> {
                Log.i(TAG, "ACTION_PAUSE (from Intent) received, delegating to MediaSession")
                mediaSession.controller.transportControls.pause()
            }

            ACTION_STOP -> {
                Log.i(TAG, "ACTION_STOP (from Intent) received, delegating to MediaSession")
                mediaSession.controller.transportControls.stop()
            }

            ACTION_PAUSE_MUSIC -> {
                if (mediaPlayerManager.isPlaying.value) {
                    mediaPlayerManager.pause()
                    isPausedByApp = true
                }
            }

            ACTION_RESUME_MUSIC -> {
                if (isPausedByApp && !mediaPlayerManager.isPlaying.value) {
                    mediaPlayerManager.resume()
                    isPausedByApp = false
                }
            }

            else -> {
                Log.w(TAG, "Unknown or null action received: $action")
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy called")
        val stoppedState =
            PlaybackStateCompat
                .Builder()
                .setState(PlaybackStateCompat.STATE_STOPPED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                .build()
        mediaSession.setPlaybackState(stoppedState)
        mediaPlayerManager.release()
        stopForeground(STOP_FOREGROUND_REMOVE)
        mediaSession.release()
        currentPlaybackMetadata = null
    }

    companion object {
        const val ACTION_PLAY = "com.ilustris.sagai.ACTION_PLAY"
        const val ACTION_PAUSE = "com.ilustris.sagai.ACTION_PAUSE"
        const val ACTION_STOP = "com.ilustris.sagai.ACTION_STOP"
        const val EXTRA_SAGA_CONTENT_JSON = "com.ilustris.sagai.EXTRA_SAGA_CONTENT_JSON"
        const val ACTION_PAUSE_MUSIC = "com.ilustris.sagai.ACTION_PAUSE_MUSIC"
        const val ACTION_RESUME_MUSIC = "com.ilustris.sagai.ACTION_RESUME_MUSIC"
    }
}
