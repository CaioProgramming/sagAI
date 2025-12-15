package com.ilustris.sagai.features.audio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.audio.AudioPermissionManager
import com.ilustris.sagai.core.audio.AudioService
import com.ilustris.sagai.core.audio.RecordingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * ViewModel for managing audio recording functionality in the UI.
 *
 * Responsibilities:
 * - Manage recording state
 * - Handle start/stop/pause/resume operations
 * - Expose recording state to UI via StateFlow
 * - Track recording duration
 * - Return recorded audio file
 */
@HiltViewModel
class AudioRecorderViewModel
    @Inject
    constructor(
        private val audioService: AudioService,
        private val permissionManager: AudioPermissionManager,
    ) : ViewModel() {
        val recordingState: StateFlow<RecordingState> = audioService.recordingState
        val recordingDuration: StateFlow<Long> = audioService.recordingDuration

        /**
         * Check if audio permissions are granted
         */
        fun hasAudioPermissions(): Boolean = permissionManager.hasAudioPermissions()

        /**
         * Start recording audio
         */
        fun startRecording() {
            viewModelScope.launch {
                audioService.startRecording()
            }
        }

        /**
         * Pause current recording
         */
        fun pauseRecording() {
            viewModelScope.launch {
                audioService.pauseRecording()
            }
        }

        /**
         * Resume paused recording
         */
        fun resumeRecording() {
            viewModelScope.launch {
                audioService.resumeRecording()
            }
        }

        /**
         * Stop recording and get the audio file
         */
        fun stopRecording(): File? = audioService.stopRecording()

        /**
         * Cancel recording without saving
         */
        fun cancelRecording() {
            viewModelScope.launch {
                audioService.cancelRecording()
            }
        }

        /**
         * Check if currently recording
         */
        fun isRecording(): Boolean = audioService.isRecording()

        override fun onCleared() {
            super.onCleared()
            // Cleanup when ViewModel is destroyed
            audioService.release()
        }
    }
