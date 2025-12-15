package com.ilustris.sagai.core.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * AudioPermissionManager handles permission checks for audio recording.
 * Follows Android runtime permission best practices.
 */
class AudioPermissionManager(
    private val context: Context,
) {
    /**
     * Get list of permissions required for audio recording
     */
    fun getRequiredPermissions(): List<String> {
        val permissions =
            mutableListOf(
                Manifest.permission.RECORD_AUDIO,
            )

        // Storage permissions depend on API level
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            // Pre-Android 11 requires explicit storage permissions
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        // Android 11+ uses scoped storage (permissions handled differently)

        return permissions
    }

    /**
     * Check if all required permissions are granted
     */
    fun hasAudioPermissions(): Boolean =
        getRequiredPermissions().all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }

    /**
     * Check if microphone permission is granted
     */
    fun hasMicrophonePermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED

    /**
     * Check if storage permissions are granted (only for pre-Android 11)
     */
    fun hasStoragePermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return true // Scoped storage, no explicit permission needed
        }

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ) == PackageManager.PERMISSION_GRANTED
    }
}
