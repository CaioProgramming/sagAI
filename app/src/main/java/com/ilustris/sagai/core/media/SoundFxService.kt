package com.ilustris.sagai.core.media

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import timber.log.Timber
import java.io.File

class SoundFxService(
    private val context: Context,
) {
    private val tag = "SoundFxService"

    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val vibrator: Vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager)
                .defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

    private val soundPool: SoundPool =
        SoundPool
            .Builder()
            .setMaxStreams(2)
            .setAudioAttributes(
                AudioAttributes
                    .Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            ).build()

    private var loadedSoundId: Int = 0

    private fun isRingerNormal(): Boolean = audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL

    fun prepare(file: File) {
        if (loadedSoundId != 0) soundPool.unload(loadedSoundId)
        loadedSoundId =
            soundPool.load(file.absolutePath, 1).also { id ->
                soundPool.setOnLoadCompleteListener { _, sampleId, status ->
                    if (sampleId == id && status == 0) {
                        Timber.tag(tag).d("SFX loaded: ${file.name}")
                    } else {
                        Timber.tag(tag).w("SFX load failed: ${file.name} (status=$status)")
                    }
                }
            }
    }

    fun play() {
        if (loadedSoundId == 0) {
            Timber.tag(tag).d("play() skipped — not loaded")
            return
        }
        if (!isRingerNormal()) {
            Timber.tag(tag).d("play() skipped — ringer silent/vibrate")
            return
        }
        val streamId = soundPool.play(loadedSoundId, 1f, 1f, 1, 0, 1f)
        Timber.tag(tag).d(if (streamId == 0) "SFX not ready yet" else "SFX playing")
    }

    fun vibrate(pattern: LongArray) {
        if (audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT) return
        if (!vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    fun playWithHaptics(pattern: LongArray?) {
        play()
        pattern?.let { vibrate(it) }
    }

    fun release() {
        soundPool.release()
        loadedSoundId = 0
    }
}
