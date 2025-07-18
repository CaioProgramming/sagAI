package com.ilustris.sagai.features.characters.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterDetailsViewModel
    @Inject
    constructor(
        private val sagaHistoryUseCase: SagaHistoryUseCase,
    ) : ViewModel() {
        val saga = MutableStateFlow<SagaContent?>(null)
        val character = MutableStateFlow<Character?>(null)
        val messageCount = MutableStateFlow(0)

        fun loadSagaAndCharacter(
            sagaId: String?,
            characterId: String?,
        ) {
            if (sagaId == null) return
            if (characterId == null) return
            viewModelScope.launch(Dispatchers.IO) {
                sagaHistoryUseCase.getSagaById(sagaId.toInt()).collect {
                    saga.value = it
                    character.value = it?.characters?.find { char -> char.id == characterId.toInt() }
                    messageCount.value =
                        it?.messages?.filter { message ->
                            message.character?.id == characterId.toInt() ||
                                message.character?.name.equals(character.value?.name, true)
                        }?.size ?: 0
                }
            }
        }
    }
