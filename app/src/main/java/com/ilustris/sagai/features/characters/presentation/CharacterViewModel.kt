package com.ilustris.sagai.features.characters.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.domain.CharacterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterViewModel
    @Inject
    constructor(
        private val characterUseCase: CharacterUseCase,
    ) : ViewModel() {
        private val _characters = MutableStateFlow<List<Character>>(emptyList())
        val characters: StateFlow<List<Character>> = _characters.asStateFlow()

        init {
            loadCharacters()
        }

        fun loadCharacters() {
            viewModelScope.launch {
                characterUseCase.getAllCharacters().collectLatest {
                    _characters.value = it
                }
            }
        }

        fun addCharacter(character: Character) {
            viewModelScope.launch {
                characterUseCase.insertCharacter(character)
                loadCharacters()
            }
        }

        fun updateCharacter(character: Character) {
            viewModelScope.launch {
                characterUseCase.updateCharacter(character)
                loadCharacters()
            }
        }

        fun deleteCharacter(characterId: Int) {
            viewModelScope.launch {
                characterUseCase.deleteCharacter(characterId)
                loadCharacters()
            }
        }
    }
