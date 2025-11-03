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

        fun loadSagaAndCharacter(
            sagaId: String?,
            characterId: String?,
        ) {
            if (sagaId == null) return
            if (characterId == null) return
            viewModelScope.launch(Dispatchers.IO) {
                sagaHistoryUseCase.getSagaById(sagaId.toInt()).collect {
                    saga.value = it
                    character.value = it?.characters?.find { char -> char.data.id == characterId.toInt() }
                    messageCount.value =
                        it?.flatMessages()?.filterCharacterMessages(character.value?.data)?.size ?: 0
                }
            }
        }

        fun regenerate(
            sagaContent: SagaContent,
            selectedCharacter: Character,
        ) {
            Log.i(javaClass.simpleName, "regenerate: Regenerating character icon")
            // TODO UNCOMMENT THIS SECTION TO AVOID UNECESSARY IMAGE GENERATION
            /*if (selectedCharacter.image.isNotEmpty() && selectedCharacter.emojified && billingService.isPremium().not()) {
                return
            }*/
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
    }
