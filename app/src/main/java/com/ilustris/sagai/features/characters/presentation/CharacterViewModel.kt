package com.ilustris.sagai.features.characters.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.data.State
import com.ilustris.sagai.core.utils.sortCharactersByMessageCount
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.domain.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterViewModel
    @Inject
    constructor(
        private val characterUseCase: CharacterUseCase,
        private val sagaHistoryUseCase: SagaHistoryUseCase,
    ) : ViewModel() {
        private val _saga = MutableStateFlow<SagaContent?>(null)
        val saga: StateFlow<SagaContent?> = _saga.asStateFlow()

        private val _characters = MutableStateFlow<List<Character>>(emptyList())
        val characters: StateFlow<List<Character>> = _characters.asStateFlow()
        val state = MutableStateFlow<State>(State.Loading)

        fun loadCharacters(sagaId: Int?) {
            if (sagaId == null) {
                state.value = State.Error("Saga não encontrada")
                return
            }
            viewModelScope.launch {
                state.value = State.Loading
                sagaHistoryUseCase.getSagaById(sagaId).collect { currentSaga ->
                    _saga.value = currentSaga
                    currentSaga?.let {
                        val sortedCharacters = sortCharactersByMessageCount(it.getCharacters(), it.flatMessages())
                        _characters.value = sortedCharacters
                    }

                    state.value = State.Success(Unit)
                }
            }
        }

        fun addCharacter(character: Character) {
            viewModelScope.launch {
                characterUseCase.insertCharacter(character)
            }
        }

        fun updateCharacter(character: Character) {
            viewModelScope.launch {
                characterUseCase.updateCharacter(character)
            }
        }

        fun deleteCharacter(characterId: Int) {
            viewModelScope.launch {
                characterUseCase.deleteCharacter(characterId)
            }
        }
    }
