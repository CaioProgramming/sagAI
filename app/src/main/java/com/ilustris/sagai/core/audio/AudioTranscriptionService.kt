package com.ilustris.sagai.core.audio

import android.util.Log
import com.ilustris.sagai.core.ai.GemmaClient
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AudioTranscriptionService handles transcription of audio files using Gemma.
 *
 * Workflow:
 * 1. Receives audio File from cache
 * 2. Sends to Gemma with transcription prompt
 * 3. Gemma transcribes and corrects audio
 * 4. Returns transcription text for further processing
 *
 * Usage:
 * ```kotlin
 * val audioFile = audioService.stopRecording()
 * val transcription = audioTranscriptionService.transcribeAudio(audioFile)
 * // Use transcription as user input for next request
 * ```
 */
@Singleton
class AudioTranscriptionService
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
    ) {
        /**
         * Transcribe audio file using Gemma
         *
         * @param audioFile Audio file from cache (MPEG-4 AAC format)
         * @return Transcribed and corrected text, or null if transcription fails
         */
        suspend fun transcribeAudio(audioFile: File?): String? {
            if (audioFile == null) {
                Log.w("AudioTranscription", "Audio file is null, skipping transcription")
                return null
            }

            if (!audioFile.exists() || audioFile.length() == 0L) {
                Log.w("AudioTranscription", "Audio file does not exist or is empty: ${audioFile.name}")
                return null
            }

            return try {
                Log.d("AudioTranscription", "Starting transcription for: ${audioFile.name}")

                val transcriptionPrompt =
                    buildString {
                        appendLine("Transcribe the following audio message accurately.")
                        appendLine("Correct any pronunciation errors and maintain coherent text.")
                        appendLine("Return ONLY the transcribed text, nothing else.")
                        appendLine("If the audio is unclear or inaudible, indicate [unclear] or [inaudible] for those parts.")
                    }

                val transcription =
                    gemmaClient.generate<String>(
                        prompt = transcriptionPrompt,
                        audioFile = audioFile,
                        requireTranslation = false, // Audio is in user's language
                        describeOutput = false, // Expecting plain text, not JSON
                    )

                transcription?.let { text ->
                    if (text.isNotBlank()) {
                        Log.d("AudioTranscription", "Transcription successful: ${text.take(100)}...")
                        text.trim()
                    } else {
                        Log.w("AudioTranscription", "Transcription returned empty text")
                        null
                    }
                } ?: run {
                    Log.e("AudioTranscription", "Gemma returned null for transcription")
                    null
                }
            } catch (e: Exception) {
                Log.e("AudioTranscription", "Error transcribing audio: ${e.message}", e)
                null
            }
        }

        /**
         * Transcribe audio and return corrected text
         *
         * Wrapper that provides the same functionality with clearer naming
         * for cases where you just need the text without intermediate logging
         */
        suspend fun transcribeAndCorrect(audioFile: File?): String? = transcribeAudio(audioFile)

        /**
         * Check if audio file is valid before transcription
         */
        fun isValidAudioFile(audioFile: File?): Boolean = audioFile != null && audioFile.exists() && audioFile.length() > 0
    }
