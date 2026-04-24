package com.ilustris.sagai.ui.theme.components.mascot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.services.MascotEmotionService
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MascotViewModel
    @Inject
    constructor(
        val mascotEmotionService: MascotEmotionService,
    ) : ViewModel() {
        val mascotEmotionUrl = MutableStateFlow<String?>(null)

        fun getMascotEmotion(
            genre: Genre,
            emotionalTone: EmotionalTone,
        ) {
            viewModelScope.launch {
                mascotEmotionUrl.value = mascotEmotionService.getEmotionUrl(genre, emotionalTone)
            }
        }
    }
