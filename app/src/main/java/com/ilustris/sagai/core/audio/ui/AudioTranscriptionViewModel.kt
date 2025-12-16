package com.ilustris.sagai.core.audio.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.R
import com.ilustris.sagai.core.audio.AudioService
import com.ilustris.sagai.core.audio.TranscriptionState
import com.ilustris.sagai.core.permissions.PermissionService
import com.ilustris.sagai.core.permissions.PermissionStatus
import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.core.utils.doNothing
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

sealed class AudioState {
    object PermissionRequired : AudioState()

    data class Loading(
        val message: String? = null,
    ) : AudioState()

    data class Error(
        val message: String,
    ) : AudioState()

    data class TranscriptionEnded(
        val message: String,
    ) : AudioState()
}

@HiltViewModel
class AudioTranscriptionViewModel
    @Inject
    constructor(
        private val audioService: AudioService,
        private val permissionService: PermissionService,
        private val stringResourceHelper: StringResourceHelper,
    ) : ViewModel() {
        val state = MutableStateFlow<AudioState?>(null)

        fun initTranscription() {
            reset()
            val permissionState =
                permissionService.getPermissionStatus(android.Manifest.permission.RECORD_AUDIO)
            if (permissionState != PermissionStatus.GRANTED) {
                state.value = AudioState.PermissionRequired
                return
            }
            audioService.transcribeAudio { serviceState ->
                when (serviceState) {
                    is TranscriptionState.Error -> {
                        showError()
                    }

                    TranscriptionState.Listening -> {
                        generateListeningMessage()
                    }

                    is TranscriptionState.Success -> {
                        endTranscription(serviceState.text)
                    }

                    else -> {
                        doNothing()
                    }
                }
            }
        }

        private fun endTranscription(text: String) {
            state.value = AudioState.TranscriptionEnded(text)
        }

        private fun generateListeningMessage() {
            viewModelScope.launch {
                state.value = AudioState.Loading()
                state.value = AudioState.Loading(audioService.generateListeningMessage().getSuccess())
            }
        }

        private fun showError() {
            viewModelScope.launch(Dispatchers.IO) {
                state.value =
                    AudioState
                        .Loading(stringResourceHelper.getString(R.string.unexpected_error))
                delay(5.seconds)
                state.value = null
            }
        }

        fun reset() {
            state.value = null
        }
    }
