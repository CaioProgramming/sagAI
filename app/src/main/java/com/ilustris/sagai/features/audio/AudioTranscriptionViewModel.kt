package com.ilustris.sagai.features.audio

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.audio.AudioService
import com.ilustris.sagai.core.audio.RecordingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * Example ViewModel demonstrating audio recording and transcription flow.
 *
 * Usage pattern:
 * 1. User clicks "record" → startAudioRecording()
 * 2. User clicks "stop" → stopAudioRecording() (file saved in cache)
 * 3. User clicks "preview" → playAudio() (reads from cache)
 * 4. User clicks "send" → transcribeAudio() (reads file as ByteArray for Gemma)
 *
 * File stays in cache until:
 * - User clicks "cancel" (delete)
 * - Screen closes (cleanup)
 * - App cache cleared by FileCacheService
 */
@HiltViewModel
class AudioTranscriptionViewModel
    @Inject
    constructor(
        private val audioService: AudioService,
        private val gemmaClient: GemmaClient,
    ) : ViewModel() {
        val recordingState: StateFlow<RecordingState> = audioService.recordingState
        val recordingDuration: StateFlow<Long> = audioService.recordingDuration

        private var currentAudioFile: File? = null

        /**
         * Start recording audio
         */
        fun startAudioRecording() {
            viewModelScope.launch {
                val success = audioService.startRecording()
                if (success) {
                    Log.d("AudioTranscription", "Recording started")
                } else {
                    Log.e("AudioTranscription", "Failed to start recording")
                }
            }
        }

        /**
         * Stop recording and save to cache
         * File is ready for preview or sending
         */
        fun stopAudioRecording() {
            viewModelScope.launch {
                currentAudioFile = audioService.stopRecording()
                currentAudioFile?.let { file ->
                    Log.d("AudioTranscription", "Audio saved: ${file.name} (${file.length()} bytes)")
                }
            }
        }

        /**
         * Cancel recording without saving
         */
        fun cancelAudioRecording() {
            viewModelScope.launch {
                audioService.cancelRecording()
                currentAudioFile = null
            }
        }

        /**
         * Play audio from cache (before sending)
         * This is optional - user can preview audio before sending
         */
        fun playAudio() {
            currentAudioFile?.let { file ->
                // TODO: Integrate with MediaPlayer or ExoPlayer
                // mediaPlayer.setDataSource(file.absolutePath)
                // mediaPlayer.prepare()
                // mediaPlayer.start()
                Log.d("AudioTranscription", "Playing audio: ${file.name}")
            }
        }

        /**
         * Transcribe audio using Gemma
         *
         * Flow:
         * 1. Read audio file from cache as ByteArray (on-demand)
         * 2. Send to Gemma with prompt
         * 3. Gemma transcribes and processes audio
         * 4. Return transcription result
         */
        fun transcribeAudio() {
            currentAudioFile?.let { audioFile ->
                viewModelScope.launch {
                    try {
                        Log.d("AudioTranscription", "Transcribing audio: ${audioFile.name}")

                        // Call Gemma with audio file
                        // File is read as ByteArray here (on-demand), not before
                        val result =
                            gemmaClient.generate<String>(
                                prompt = "Please transcribe this audio message and return only the transcription.",
                                audioFile = audioFile,
                                requireTranslation = true,
                            )

                        result?.let { transcription ->
                            Log.d("AudioTranscription", "Transcription result: $transcription")
                            // Handle transcription result (send as message, etc)
                        } ?: Log.e("AudioTranscription", "Failed to transcribe audio")
                    } catch (e: Exception) {
                        Log.e("AudioTranscription", "Error transcribing: ${e.message}", e)
                    }
                }
            }
        }

        /**
         * Delete audio file from cache
         */
        fun deleteAudio() {
            currentAudioFile?.delete()
            currentAudioFile = null
        }

        override fun onCleared() {
            super.onCleared()
            // Cleanup when ViewModel is destroyed
            audioService.release()
        }
    }
