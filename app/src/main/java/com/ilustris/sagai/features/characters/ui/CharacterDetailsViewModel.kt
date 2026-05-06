package com.ilustris.sagai.features.characters.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.data.model.ImagePalette
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.core.usecase.PaletteUseCase
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import com.ilustris.sagai.features.saga.chat.domain.model.filterCharacterMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CharacterDetailsViewModel
    @Inject
    constructor(
        private val sagaHistoryUseCase: SagaHistoryUseCase,
        private val characterUseCase: CharacterUseCase,
        private val billingService: BillingService,
        private val paletteUseCase: PaletteUseCase,
    ) : ViewModel() {
        val saga = MutableStateFlow<SagaContent?>(null)
        val character = MutableStateFlow<CharacterContent?>(null)
        val imagePalette = MutableStateFlow<ImagePalette?>(null)
        val messageCount = MutableStateFlow(0)
        val isGenerating = MutableStateFlow(false)
        val loadingMessage = MutableStateFlow<String?>(null)
        val imageReasoning = MutableStateFlow<String?>(null)

        val characterResume = MutableStateFlow<String?>(null)
        val characterDetailState = MutableStateFlow<CharacterDetailState?>(null)
        val isSummarizing = MutableStateFlow(false)
        val isEnriching = MutableStateFlow(false)
        val showPremiumSheet = MutableStateFlow(false)

        fun togglePremiumSheet() {
            showPremiumSheet.value = !showPremiumSheet.value
        }

        fun loadSagaAndCharacter(
            sagaId: String?,
            characterId: Int?,
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

                    if (sagaContent != null && foundCharacter != null) {
                        if (characterResume.value == null) {
                            generateResume(sagaContent, foundCharacter)
                        }
                        if (characterDetailState.value == null) {
                            loadEnrichment(sagaContent, foundCharacter)
                        }
                    }
                }
            }
        }

        private fun loadEnrichment(
            sagaContent: SagaContent,
            characterContent: CharacterContent,
        ) {
            viewModelScope.launch(Dispatchers.IO) {
            /* isEnriching.value = true
             characterUseCase
                 .enrichCharacter(characterContent, sagaContent)
                 .onSuccessAsync {
                     characterDetailState.emit(it)
                     isEnriching.value = false
                 }
                 .onFailure {
                     isEnriching.value = false
                 }*/
            }
        }

        private fun generateResume(
            sagaContent: SagaContent,
            characterContent: CharacterContent,
        ) {
            characterResume.value = characterContent.data.backstory
            viewModelScope.launch(Dispatchers.IO) {
                isSummarizing.value = true
                characterUseCase
                    .generateCharacterResume(characterContent, sagaContent)
                    .onSuccessAsync {
                        characterResume.emit(it)
                        isSummarizing.value = false
                    }
            }
        }

        fun regenerate(
            sagaContent: SagaContent,
            selectedCharacter: Character,
        ) {
            isGenerating.value = true
            loadingMessage.value = "Gerando ${selectedCharacter.name}..."
            imageReasoning.value = null

            viewModelScope.launch(Dispatchers.IO) {
                characterUseCase
                    .generateCharacterImageStream(
                        selectedCharacter,
                        sagaContent.data,
                    ).collect { state ->
                        when (state) {
                            is com.ilustris.sagai.core.ai.StreamingState.Reasoning -> {
                                imageReasoning.value = state.chunk
                            }

                            is com.ilustris.sagai.core.ai.StreamingState.Success -> {
                                isGenerating.value = false
                                loadingMessage.value = null
                                imageReasoning.value = null
                            }

                            is com.ilustris.sagai.core.ai.StreamingState.Error -> {
                                isGenerating.value = false
                                loadingMessage.value = null
                                imageReasoning.value = null
                            }
                        }
                    }
            }
        }

        fun init(
            characterId: Int?,
            sagaContent: SagaContent,
        ) {
            val characterContent = sagaContent.findCharacter(characterId)
            val canGenerateResume = character.value != characterContent
            viewModelScope.launch(Dispatchers.IO) {
                character.value = characterContent
                characterContent?.let {
                    if (canGenerateResume) {
                        generateResume(sagaContent, it)
                    }
                    loadEnrichment(sagaContent, it)
                    if (it.data.image.isNotEmpty()) {
                        val palette = paletteUseCase.extractPalette(it.data.image)
                        Timber.d("Extracted palette: ${palette.toAINormalize()}")
                        imagePalette.value = palette.getSuccess()
                    } else {
                        imagePalette.value = null
                    }
                }
            }
        }
    }
