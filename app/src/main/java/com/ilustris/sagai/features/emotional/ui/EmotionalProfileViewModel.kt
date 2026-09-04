package com.ilustris.sagai.features.emotional.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.services.EmotionalToneVisualService
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.features.saga.chat.domain.model.rankEmotionalTone
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmotionalProfileViewModel
    @Inject
    constructor(
        private val emotionalToneVisualService: EmotionalToneVisualService,
        private val sagaRepository: SagaRepository,
    ) : ViewModel() {
        private val _emotionalIconUrl = MutableStateFlow<String?>(null)
        val emotionalIconUrl = _emotionalIconUrl.asStateFlow()

        private val _saga = MutableStateFlow<Saga?>(null)
        val saga = _saga.asStateFlow()

        fun loadEmotionalIcon(sagaId: Int) {
            viewModelScope.launch {
                sagaRepository.getSagaById(sagaId).collect { sagaContent ->
                    if (sagaContent == null) return@collect
                    _saga.value = sagaContent.data
                    val dominantTone =
                        sagaContent.data.emotionalProfile?.dominantTone
                            ?: sagaContent
                                .flatMessages()
                                .rankEmotionalTone()
                                .firstOrNull()
                                ?.first
                            ?: EmotionalTone.NEUTRAL

                    // tone_visuals only: the mascot emote tables it used to fall back to are gone.
                    _emotionalIconUrl.value = emotionalToneVisualService.getVisualUrl(dominantTone)
                }
            }
        }
    }
