package com.ilustris.sagai.core.audio

import android.media.MediaRecorder
import android.os.Build
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
 * - Records audio to a dedicated cache folder (audio_recordings)
 * - Exposes recording state via Flow
 * - Start/Stop/Pause recording
 * - Returns audio file after recording stops
 * - Automatic cleanup on app exit
 */
class AudioService(
    private val cacheDir: File,
) {
    private val audioDir =
        File(cacheDir, "audio_recordings").apply {
            if (!exists()) mkdirs()
        }

    private var mediaRecorder: MediaRecorder? = null
    private var currentAudioFile: File? = null

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

            // Create audio file with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            currentAudioFile = File(audioDir, "audio_$timestamp.m4a")

            // Initialize MediaRecorder
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
     * Check if recording is currently active
     */
    fun isRecording(): Boolean = _recordingState.value == RecordingState.RECORDING

    /**
     * Get current audio file being recorded (if any)
     */
    fun getCurrentAudioFile(): File? = currentAudioFile

    /**
     * Clean up all recorded audio files in the cache directory
     */
    fun clearCache() {
        try {
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
     * Release resources
     */
    fun release() {
        cancelRecording()
        clearCache()
    }
}
