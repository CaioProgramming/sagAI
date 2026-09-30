package com.ilustris.sagai.features.voicepicker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.VoiceCatalog
import com.ilustris.sagai.core.ai.model.Voice
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.voicepicker.data.VoiceSamplePlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VoicePickerState(
    val voices: List<Voice> = emptyList(),
    /** The page the picker opens on: the voice suggested for the saga's genre, else the first. */
    val initialPage: Int = 0,
    val loaded: Boolean = false,
    val playingId: String? = null,
)

@HiltViewModel
class VoicePickerViewModel
    @Inject
    constructor(
        private val voiceCatalog: VoiceCatalog,
        private val samplePlayer: VoiceSamplePlayer,
    ) : ViewModel() {
        private val _state = MutableStateFlow(VoicePickerState())
        val state: StateFlow<VoicePickerState> = _state.asStateFlow()

        /** Loudness of the clip playing now, read in the orb's draw phase. */
        val level: StateFlow<Float> = samplePlayer.level

        private var playJob: Job? = null

        fun load(genre: Genre?) {
            viewModelScope.launch {
                val voices = voiceCatalog.voices().filter { it.isPickable }
                _state.update { VoicePickerState(voices, initialPage(voices, genre), loaded = true) }
            }
        }

        fun play(voice: Voice) {
            playJob?.cancel()
            samplePlayer.stop()
            playJob =
                viewModelScope.launch {
                    _state.update { it.copy(playingId = voice.id) }
                    samplePlayer.play(voice)
                    _state.update { if (it.playingId == voice.id) it.copy(playingId = null) else it }
                }
        }

        fun toggle(voice: Voice) {
            if (_state.value.playingId == voice.id) stop() else play(voice)
        }

        fun stop() {
            playJob?.cancel()
            samplePlayer.stop()
            _state.update { it.copy(playingId = null) }
        }

        override fun onCleared() {
            samplePlayer.stop()
            super.onCleared()
        }

        companion object {
            internal fun initialPage(
                voices: List<Voice>,
                genre: Genre?,
            ): Int = genre?.let { g -> voices.indexOfFirst { it.isSuggestedFor(g.name) } }?.takeIf { it >= 0 } ?: 0
        }
    }
