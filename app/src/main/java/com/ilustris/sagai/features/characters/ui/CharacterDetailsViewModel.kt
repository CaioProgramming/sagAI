package com.ilustris.sagai.features.characters.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import com.ilustris.sagai.features.saga.chat.domain.model.filterCharacterMessages
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
        private val characterUseCase: CharacterUseCase,
        private val billingService: BillingService,
    ) : ViewModel() {
        val saga = MutableStateFlow<SagaContent?>(null)
        val character = MutableStateFlow<CharacterContent?>(null)
        val messageCount = MutableStateFlow(0)
        val isGenerating = MutableStateFlow(false)
        val loadingMessage = MutableStateFlow<String?>(null)

        val characterResume = MutableStateFlow<String?>(null)
        val isSummarizing = MutableStateFlow(false)

        fun loadSagaAndCharacter(
            sagaId: String?,
            characterId: String?,
        ) {
            if (sagaId == null) return
            if (characterId == null) return
            viewModelScope.launch(Dispatchers.IO) {
                sagaHistoryUseCase.getSagaById(sagaId.toInt()).collect { sagaContent ->
                    saga.value = sagaContent
                    val foundCharacter =
                        sagaContent?.characters?.find { char -> char.data.id == characterId.toInt() }
                    character.value = foundCharacter
                    messageCount.value =
                        sagaContent
                            ?.flatMessages()
                            ?.filterCharacterMessages(foundCharacter?.data)
                            ?.size ?: 0

                    if (sagaContent != null && foundCharacter != null && characterResume.value == null) {
                        generateResume(sagaContent, foundCharacter)
                    }
                }
            }
        }

        private fun generateResume(
            sagaContent: SagaContent,
            characterContent: CharacterContent,
        ) {
            if (characterResume.value != null) return
            viewModelScope.launch(Dispatchers.IO) {
                isSummarizing.value = true
                characterUseCase
                    .generateCharacterResume(characterContent, sagaContent)
                    .onSuccessAsync {
                        characterResume.emit(it)
                        isSummarizing.value = false
                    }.onFailure {
                        isSummarizing.value = false
                        characterResume.value = characterContent.data.backstory
                        Log.e(javaClass.simpleName, "Error generating character resume: ${it.message}")
                    }
            }
        }

        fun regenerate(
            sagaContent: SagaContent,
            selectedCharacter: Character,
        ) {
            isGenerating.value = true
            loadingMessage.value = "Gerando ${selectedCharacter.name}..."
            viewModelScope.launch(Dispatchers.IO) {
                characterUseCase.generateCharacterImage(
                    selectedCharacter,
                    sagaContent.data,
                )
                isGenerating.value = false
                loadingMessage.emit(null)
            }
        }

        fun init(
            characterContent: CharacterContent?,
            sagaContent: SagaContent,
        ) {
            characterResume.value = null
            viewModelScope.launch(Dispatchers.IO) {
                characterContent?.let { generateResume(sagaContent, it) }
            }
        }
    }
