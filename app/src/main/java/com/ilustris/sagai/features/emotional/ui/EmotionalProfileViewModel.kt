package com.ilustris.sagai.features.emotional.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.services.EmotionalToneVisualService
import com.ilustris.sagai.core.services.MascotEmotionService
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.features.saga.chat.domain.model.rankEmotionalTone
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmotionalProfileViewModel
    @Inject
    constructor(
        private val mascotEmotionService: MascotEmotionService,
        private val emotionalToneVisualService: EmotionalToneVisualService,
    ) : ViewModel() {
        private val _emotionalIconUrl = MutableStateFlow<String?>(null)
        val emotionalIconUrl = _emotionalIconUrl.asStateFlow()

        fun loadEmotionalIcon(sagaContent: SagaContent) {
            viewModelScope.launch {
                val dominantTone =
                    sagaContent.data.emotionalProfile?.dominantTone
                        ?: sagaContent
                            .flatMessages()
                            .rankEmotionalTone()
                            .firstOrNull()
                            ?.first
                        ?: EmotionalTone.NEUTRAL

                // Priority: 1. Abstract Visual (tone_visuals) | 2. Genre Mascot | 3. Default Mascot
                _emotionalIconUrl.value = emotionalToneVisualService.getVisualUrl(dominantTone)
                    ?: mascotEmotionService.getEmotionUrl(sagaContent.data.genre, dominantTone)
            }
        }
    }
