package com.ilustris.sagai.core.media

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.ilustris.sagai.features.settings.domain.SettingsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SagaPlaybackService : Service() {
    @Inject
    lateinit var mediaPlayerManager: MediaPlayerManager

    @Inject
    lateinit var settingsUseCase: SettingsUseCase

    private val binder = LocalBinder()
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var musicObserverJob: Job? = null
    private var currentMusicPath: String? = null
    private var musicEnabledBySettings = true

    private val TAG = "SagaPlaybackService"

    private val ringerModeReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(
                context: Context?,
                intent: Intent?,
            ) {
                if (intent?.action == AudioManager.RINGER_MODE_CHANGED_ACTION) {
                    Log.d(TAG, "Ringer mode changed, updating playback")
                    updatePlayback()
                }
            }
        }

    inner class LocalBinder : Binder() {
        fun getService(): SagaPlaybackService = this@SagaPlaybackService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "SagaPlaybackService created")
        registerReceiver(ringerModeReceiver, IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION))
        observeMusicSettings()
    }

    private fun observeMusicSettings() {
        musicObserverJob?.cancel()
        musicObserverJob =
            serviceScope.launch {
                settingsUseCase.getMusicEnabled().collectLatest { enabled ->
                    Log.d(TAG, "Music setting changed: enabled=$enabled")
                    musicEnabledBySettings = enabled
                    updatePlayback()
                }
            }
    }

    private fun updatePlayback() {
        val path = currentMusicPath ?: return
        val isSilent = isSilentMode()
        val shouldPlay = musicEnabledBySettings && !isSilent

        Log.d(
            TAG,
            "updatePlayback: shouldPlay=$shouldPlay (settings=$musicEnabledBySettings, silent=$isSilent)",
        )

        if (shouldPlay) {
            if (!mediaPlayerManager.isPlaying.value) {
                Log.i(TAG, "Starting/Resuming music: $path")
                mediaPlayerManager.prepareDataSource(
                    path,
                    looping = true,
                    onPrepared = {
                        mediaPlayerManager.play()
                    },
                )
            }
        } else {
            if (mediaPlayerManager.isPlaying.value) {
                Log.i(TAG, "Stopping music due to silence/settings")
                mediaPlayerManager.stop()
            }
        }
    }

    fun startMusic(path: String) {
        if (currentMusicPath == path && mediaPlayerManager.isPlaying.value) return
        currentMusicPath = path
        updatePlayback()
    }

    fun stopMusic() {
        Log.i(TAG, "Stopping music playback")
        mediaPlayerManager.stop()
        currentMusicPath = null
    }

    fun pauseMusic() {
        Log.i(TAG, "Pausing music playback")
        mediaPlayerManager.pause()
    }

    fun resumeMusic() {
        Log.i(TAG, "Resume requested")
        updatePlayback()
    }

    private fun isSilentMode(): Boolean {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val ringerMode = audioManager.ringerMode
        val notificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)

        val isMuted = ringerMode != AudioManager.RINGER_MODE_NORMAL || notificationVolume == 0
        Log.d(
            TAG,
            "isSilentMode: ringerMode=$ringerMode, notificationVolume=$notificationVolume -> $isMuted",
        )
        return isMuted
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        val action = intent?.action
        val path = intent?.getStringExtra(EXTRA_MUSIC_PATH)

        Log.d(TAG, "onStartCommand: action=$action")

        when (action) {
            ACTION_START -> path?.let { startMusic(it) }
            ACTION_STOP -> stopMusic()
            ACTION_PAUSE -> pauseMusic()
            ACTION_RESUME -> resumeMusic()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(ringerModeReceiver)
        stopMusic()
        serviceJob.cancel()
        Log.i(TAG, "SagaPlaybackService destroyed")
    }

    companion object {
        const val ACTION_START = "com.ilustris.sagai.ACTION_START_MUSIC"
        const val ACTION_STOP = "com.ilustris.sagai.ACTION_STOP_MUSIC"
        const val ACTION_PAUSE = "com.ilustris.sagai.ACTION_PAUSE_MUSIC"
        const val ACTION_RESUME = "com.ilustris.sagai.ACTION_RESUME_MUSIC"
        const val EXTRA_MUSIC_PATH = "EXTRA_MUSIC_PATH"
    }
}
