package com.ilustris.sagai.features.characters.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterAvatarViewModel
    @Inject
    constructor(
        private val characterUseCase: CharacterUseCase,
    ) : ViewModel() {
        fun checkAndGenerateZoom(character: Character) {
            viewModelScope.launch {
                characterUseCase.checkAndGenerateZoom(character)
            }
        }
    }
