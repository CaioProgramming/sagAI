package com.ilustris.sagai.core.audio

import android.media.MediaRecorder
import android.os.Build
import com.ilustris.sagai.core.file.FileCacheService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Enum representing the state of audio recording
 */
enum class RecordingState {
    IDLE,
    RECORDING,
    PAUSED,
    STOPPED,
    ERROR,
}

/**
 * AudioService handles audio recording with solid API.
 *
 * Features:
 * - Records audio to app cache directory via FileCacheService
 * - Exposes recording state via Flow for reactive updates
 * - Start/Stop/Pause recording operations
 * - Returns audio file after recording stops
 * - Automatic cleanup via FileCacheService
 *
 * Usage:
 * ```kotlin
 * @Inject lateinit var audioService: AudioService
 *
 * // Observe recording state
 * audioService.recordingState.collect { state ->
 *   when (state) {
 *     RecordingState.RECORDING -> // Show recording UI
 *     RecordingState.STOPPED -> // File ready for use
 *     else -> {}
 *   }
 * }
 *
 * // Start recording
 * audioService.startRecording()
 *
 * // Stop and get file for use in message, etc
 * val audioFile = audioService.stopRecording()
 * ```
 */
class AudioService(
    private val fileCacheService: FileCacheService,
) {
    companion object {
        const val MAX_RECORDING_DURATION_MS = 60_000L // 60 seconds
    }

    private var mediaRecorder: MediaRecorder? = null
    private var currentAudioFile: File? = null
    private var recordingStartTime: Long = 0L

    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.IDLE)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val _recordingDuration = MutableStateFlow<Long>(0)
    val recordingDuration: StateFlow<Long> = _recordingDuration.asStateFlow()

    /**
     * Start recording audio
     * Returns true if recording started successfully, false otherwise
     */
    fun startRecording(): Boolean {
        return try {
            if (_recordingState.value == RecordingState.RECORDING) {
                return false // Already recording
            }

            // Create audio file in cache via FileCacheService
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val audioDir = fileCacheService.getFileCacheDir("audio_recordings")
            currentAudioFile = File(audioDir, "audio_$timestamp.m4a")

            // Track recording start time for max duration limit
            recordingStartTime = System.currentTimeMillis()

            // ...existing code...
            mediaRecorder =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(android.app.Application())
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioEncodingBitRate(128000) // 128 kbps
                    setAudioSamplingRate(44100) // 44.1 kHz
                    setOutputFile(currentAudioFile?.absolutePath)
                    prepare()
                    start()
                }

            _recordingState.value = RecordingState.RECORDING
            true
        } catch (e: Exception) {
            _recordingState.value = RecordingState.ERROR
            mediaRecorder?.release()
            mediaRecorder = null
            currentAudioFile?.delete()
            currentAudioFile = null
            false
        }
    }

    /**
     * Pause recording (maintains MediaRecorder state)
     * Only available on API 24+
     */
    fun pauseRecording(): Boolean {
        return try {
            if (_recordingState.value != RecordingState.RECORDING) {
                return false
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.pause()
                _recordingState.value = RecordingState.PAUSED
                true
            } else {
                false // Pause not supported on older versions
            }
        } catch (e: Exception) {
            _recordingState.value = RecordingState.ERROR
            false
        }
    }

    /**
     * Resume recording from pause (only if paused)
     * Only available on API 24+
     */
    fun resumeRecording(): Boolean {
        return try {
            if (_recordingState.value != RecordingState.PAUSED) {
                return false
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.resume()
                _recordingState.value = RecordingState.RECORDING
                true
            } else {
                false
            }
        } catch (e: Exception) {
            _recordingState.value = RecordingState.ERROR
            false
        }
    }

    /**
     * Stop recording and return the audio file
     * Returns the File if successful, null otherwise
     */
    fun stopRecording(): File? {
        return try {
            if (_recordingState.value !in listOf(RecordingState.RECORDING, RecordingState.PAUSED)) {
                return null // Not recording
            }

            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null

            _recordingState.value = RecordingState.STOPPED

            currentAudioFile?.let { file ->
                if (file.exists() && file.length() > 0) {
                    return file
                } else {
                    file.delete()
                    null
                }
            } ?: run {
                null
            }
        } catch (e: Exception) {
            _recordingState.value = RecordingState.ERROR
            mediaRecorder?.release()
            mediaRecorder = null
            currentAudioFile?.delete()
            null
        }
    }

    /**
     * Cancel recording without saving
     */
    fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            currentAudioFile?.delete()
            currentAudioFile = null
            _recordingState.value = RecordingState.IDLE
        } catch (e: Exception) {
            _recordingState.value = RecordingState.ERROR
        }
    }

    /**
     * Check if recording duration has reached the maximum limit (60 seconds)
     * Returns true if limit is reached and recording should stop
     */
    fun hasReachedMaxDuration(): Boolean {
        if (_recordingState.value != RecordingState.RECORDING) {
            return false
        }

        val elapsedTime = System.currentTimeMillis() - recordingStartTime
        return elapsedTime >= MAX_RECORDING_DURATION_MS
    }

    /**
     * Get remaining recording time in milliseconds
     */
    fun getRemainingTime(): Long {
        if (_recordingState.value != RecordingState.RECORDING) {
            return MAX_RECORDING_DURATION_MS
        }

        val elapsedTime = System.currentTimeMillis() - recordingStartTime
        val remaining = MAX_RECORDING_DURATION_MS - elapsedTime
        return if (remaining > 0) remaining else 0
    }

    /**
     * Check if recording is currently active
     */
    fun isRecording(): Boolean = _recordingState.value == RecordingState.RECORDING

    /**
     * Get current audio file being recorded (if any)
     */
    fun getCurrentAudioFile(): File? = currentAudioFile

    /**
     * Clean up all recorded audio files via FileCacheService
     */
    fun clearCache() {
        try {
            val audioDir = fileCacheService.getFileCacheDir("audio_recordings")
            audioDir.listFiles()?.forEach { file ->
                if (file.isFile && file.extension == "m4a") {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            // Silently handle cleanup errors
        }
    }

    /**
     * Release resources and cleanup
     */
    fun release() {
        cancelRecording()
        clearCache()
    }
}
