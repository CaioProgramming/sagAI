package com.ilustris.sagai.features.voicepicker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.model.Voice
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/** Persists the narrator a player picks for a freshly created saga. */
@HiltViewModel
class NarratorVoiceViewModel
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
    ) : ViewModel() {
        private val _genre = MutableStateFlow<Genre?>(null)
        val genre: StateFlow<Genre?> = _genre.asStateFlow()

        fun load(sagaId: Int) {
            viewModelScope.launch {
                _genre.value = sagaRepository.getSaga(sagaId).first()?.genre
            }
        }

        /** [onSaved] runs even when saving fails: the saga then falls back to an automatic narrator on first use. */
        fun confirm(
            sagaId: Int,
            voice: Voice,
            onSaved: () -> Unit,
        ) {
            viewModelScope.launch {
                runCatching {
                    sagaRepository.getSaga(sagaId).first()?.let { sagaRepository.updateSaga(it.copy(narratorVoice = voice.id)) }
                }.onFailure { Timber.w(it, "Couldn't save narrator voice ${voice.id} for saga $sagaId") }
                onSaved()
            }
        }
    }
