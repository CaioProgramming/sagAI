package com.ilustris.sagai.core.audio

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.AudioPrompts
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.permissions.PermissionService
import com.ilustris.sagai.core.permissions.PermissionStatus
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

sealed class TranscriptionState {
    object Idle : TranscriptionState()

    object Listening : TranscriptionState()

    data class Success(
        val text: String,
    ) : TranscriptionState()

    data class PartialResults(
        val text: String,
    ) : TranscriptionState()

    data class Error(
        val exception: Exception,
    ) : TranscriptionState()
}

@Singleton
class AudioService
    @Inject
    constructor(
        private val context: Context,
        private val permissionService: PermissionService,
        private val gemmaClient: GemmaClient,
    ) {
        companion object {
            private const val TAG = "AudioService"
        }

        fun transcribeAudio(
            prompt: String?,
            onResult: (TranscriptionState?) -> Unit,
        ) {
            try {
                if (!checkPermission()) {
                    onResult(
                        TranscriptionState.Error(
                            IllegalAccessException(
                                "Permission not granted",
                            ),
                        ),
                    )
                }

                // Check availability
                if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                    onResult(
                        TranscriptionState.Error(
                            Exception("Not available"),
                        ),
                    )
                }

                val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                val locale = Locale.getDefault()
                val languageTag = locale.toLanguageTag()
                val textPrompt = prompt ?: "I'm hearing you, start talking..."
                val intent =
                    Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                        )
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
                        putExtra(RecognizerIntent.EXTRA_PROMPT, textPrompt)
                        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                        putExtra(
                            RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                            5.seconds.toInt(
                                DurationUnit.MILLISECONDS,
                            ),
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            putExtra(RecognizerIntent.EXTRA_ENABLE_FORMATTING, true)
                        }
                        Log.d(TAG, "Speech recognition language set to: $languageTag")
                    }

                onResult(TranscriptionState.Listening)
                Log.d(TAG, "Listening started")

                speechRecognizer.setRecognitionListener(
                    object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {}

                        override fun onBeginningOfSpeech() {}

                        override fun onRmsChanged(rmsdB: Float) {}

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {}

                        override fun onError(error: Int) {
                            Log.e(TAG, "Error: $error")
                            onResult(TranscriptionState.Error(Exception("Failed")))
                            speechRecognizer.destroy()
                        }

                        override fun onResults(results: Bundle?) {
                            val matches =
                                results?.getStringArrayList(
                                    SpeechRecognizer.RESULTS_RECOGNITION,
                                )
                            Log.d(TAG, "onResults: Audio recognition results - $matches")
                            val text = matches?.firstOrNull()

                            if (!text.isNullOrBlank()) {
                                Log.d(TAG, "Success: $text")
                                onResult(TranscriptionState.Success(text))
                            } else {
                                Log.w(TAG, "Empty transcription")
                                onResult(TranscriptionState.Error(Exception("Failed")))
                            }
                            speechRecognizer.destroy()
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches =
                                partialResults?.getStringArrayList(
                                    SpeechRecognizer.RESULTS_RECOGNITION,
                                )
                            val text = matches?.firstOrNull()
                            if (!text.isNullOrBlank()) {
                                onResult(TranscriptionState.PartialResults(text))
                            }
                        }

                        override fun onEvent(
                            eventType: Int,
                            params: Bundle?,
                        ) {
                        }
                    },
                )
                speechRecognizer.startListening(intent)
            } catch (e: Exception) {
                onResult(TranscriptionState.Error(e))
            }
        }

        private fun checkPermission(): Boolean {
            val status = permissionService.getPermissionStatus(Manifest.permission.RECORD_AUDIO)
            return status == PermissionStatus.GRANTED
        }

        suspend fun generateListeningMessage() =
            executeRequest {
                val prompt = AudioPrompts.transcribeInstruction()
                gemmaClient.generate<String>(prompt, temperatureRandomness = .1f)!!
            }
    }
