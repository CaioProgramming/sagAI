package com.ilustris.sagai.features.characters.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.R
import com.ilustris.sagai.core.data.model.ImagePalette
import com.ilustris.sagai.core.theme.SagaImmersiveSession
import com.ilustris.sagai.core.theme.SagaThemeManager
import com.ilustris.sagai.core.usecase.PaletteUseCase
import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.characters.data.model.CharacterArc
import com.ilustris.sagai.features.characters.data.model.CharacterDetailData
import com.ilustris.sagai.features.characters.data.model.CharacterSagaInfo
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterDetailsViewModel
    @Inject
    constructor(
        private val characterUseCase: CharacterUseCase,
        private val paletteUseCase: PaletteUseCase,
        private val sagaThemeManager: SagaThemeManager,
        private val sagaImmersiveSession: SagaImmersiveSession,
        private val stringResourceHelper: StringResourceHelper,
    ) : ViewModel() {
        val characterDetailData = MutableStateFlow<CharacterDetailData?>(null)
        val imagePalette = MutableStateFlow<ImagePalette?>(null)
        val messageCount = MutableStateFlow(0)
        val isGenerating = MutableStateFlow(false)
        val loadingMessage = MutableStateFlow<String?>(null)
        val imageReasoning = MutableStateFlow<String?>(null)

        val characterResume = MutableStateFlow<String?>(null)
        val characterArcs = MutableStateFlow<List<CharacterArc>>(emptyList())
        val characterDetailState = MutableStateFlow<CharacterDetailState?>(null)
        val isSummarizing = MutableStateFlow(false)
        val isEnriching = MutableStateFlow(false)
        val showPremiumSheet = MutableStateFlow(false)

        /** Tracks the currently loaded character to prevent stale data from previous loads. */
        private var currentCharacterId: Int? = null

        /** Job for the active character detail collection — cancelled on re-entry. */
        private var detailJob: Job? = null

        /** Job for character arcs collection — cancelled on re-entry. */
        private var arcsJob: Job? = null

        fun onCharacterScreenVisible(sagaId: Int) {
            sagaImmersiveSession.push("character_detail", sagaId)
        }

        fun onCharacterScreenHidden() {
            sagaImmersiveSession.pop("character_detail")
        }

        /** Re-applies saga genre theme when this screen owns the immersive stack (e.g. after a neutral reset). */
        fun ensureGenreTheme(genre: Genre) {
            if (sagaImmersiveSession.isOwnerOnTop("character_detail")) {
                sagaThemeManager.updateTheme(genre)
            }
        }

        fun togglePremiumSheet() {
            showPremiumSheet.value = !showPremiumSheet.value
        }

        fun loadCharacterDetails(characterId: Int?) {
            if (characterId == null) return
            // Guard: skip if we're already loading this exact character.
            if (characterId == currentCharacterId) return

            // Cancel any in-flight collection from a previous character.
            detailJob?.cancel()
            arcsJob?.cancel()
            currentCharacterId = characterId

            // Reset state so the UI never flashes stale data from the old character.
            characterDetailData.value = null
            characterResume.value = null
            characterArcs.value = emptyList()
            characterDetailState.value = null
            imagePalette.value = null
            messageCount.value = 0

            arcsJob =
                viewModelScope.launch(Dispatchers.IO) {
                    characterUseCase.getCharacterArcs(characterId).collect { arcs ->
                        characterArcs.value = arcs
                    }
                }

            detailJob =
                viewModelScope.launch(Dispatchers.IO) {
                    characterUseCase.getCharacterDetailData(characterId).collect { data ->
                        characterDetailData.value = data
                        data?.let {
                            ensureGenreTheme(it.sagaInfo.genre)
                            messageCount.value = it.messageCount
                            if (characterResume.value == null) {
                                generateResume(it)
                            }
                            if (characterDetailState.value == null) {
                                loadEnrichment(it)
                            }
                            if (imagePalette.value == null && it.character.image.isNotEmpty()) {
                                extractPalette(it.character.image)
                            }
                        }
                    }
                }
        }

        private fun extractPalette(imageUrl: String) {
            viewModelScope.launch(Dispatchers.IO) {
                val palette = paletteUseCase.extractPalette(imageUrl)
                imagePalette.value = palette.getSuccess()
            }
        }

        private fun loadEnrichment(detailData: CharacterDetailData) {
            viewModelScope.launch(Dispatchers.IO) {
            /* isEnriching.value = true
             characterUseCase
                 .enrichCharacter(detailData.character, detailData.sagaInfo.toSaga())
                 .onSuccessAsync {
                     characterDetailState.emit(it)
                     isEnriching.value = false
                 }
                 .onFailure {
                     isEnriching.value = false
                 }*/
            }
        }

        private fun generateResume(detailData: CharacterDetailData) {
            characterResume.value =
                buildString {
                    appendLine(detailData.character.backstory)
                }
        /*viewModelScope.launch(Dispatchers.IO) {
            isSummarizing.value = true
            characterUseCase
                .generateCharacterResume(detailData.character, detailData.sagaInfo.toSaga())
                .onSuccessAsync {
                    characterResume.emit(it)
                    isSummarizing.value = false
                }
        }*/
        }

        fun regenerate(
            sagaInfo: CharacterSagaInfo,
            selectedCharacter: Character,
        ) {
            isGenerating.value = true
            loadingMessage.value =
                stringResourceHelper.getString(R.string.character_generating, selectedCharacter.name)
            imageReasoning.value = null

            viewModelScope.launch(Dispatchers.IO) {
                characterUseCase
                    .generateCharacterImageStream(
                        selectedCharacter,
                        sagaInfo.toSaga(),
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

        override fun onCleared() {
            sagaImmersiveSession.pop("character_detail")
            super.onCleared()
        }
    }
