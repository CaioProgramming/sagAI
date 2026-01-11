package com.ilustris.sagai.core.utils

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RawRes

fun Context.vibrate(pattern: LongArray) {
    val vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(pattern, -1)
    }
}

/**
 * Plays a milestone achievement sound effect.
 * @param soundRes Raw resource ID of the sound file (e.g., R.raw.milestone_sound)
 * @param volume Volume level from 0.0 to 1.0 (default 0.7)
 */
fun Context.playMilestoneSound(
    @RawRes soundRes: Int,
    volume: Float = 0.7f,
) {
    try {
        MediaPlayer.create(this, soundRes)?.apply {
            setVolume(volume, volume)
            setOnCompletionListener { mp ->
                mp.release()
            }
            start()
        }
    } catch (e: Exception) {
        // Silently fail if sound can't play
        e.printStackTrace()
    }
}
