package com.ilustris.sagai.features.newsaga.ui.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.CreationAssist
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.usecase.AgenticFlowResponse
import com.ilustris.sagai.features.newsaga.data.usecase.AgenticUIComponent
import com.ilustris.sagai.features.newsaga.data.usecase.AgenticUIComponent.IdeaPitches
import com.ilustris.sagai.features.newsaga.data.usecase.AgenticUIComponent.PersonaPitches
import com.ilustris.sagai.features.newsaga.data.usecase.NewSagaUseCase
import com.ilustris.sagai.features.newsaga.data.usecase.SagaCreationState
import com.ilustris.sagai.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

internal data class LoadingState(
    val message: String,
)

enum class FlowPages {
    SELECT_THEME,
    CREATE_SAGA,
    CREATE_CHARACTER,
    GENERATING,
}

sealed class AgenticAction {
    data class SubmitPrompt(
        val prompt: String,
    ) : AgenticAction()

    data class SelectSaga(
        val draft: SagaDraft,
    ) : AgenticAction()

    data class SelectCharacter(
        val info: CharacterInfo,
    ) : AgenticAction()

    data object UnlockSaga : AgenticAction()

    data object UnlockCharacter : AgenticAction()

    data object SaveSaga : AgenticAction()

    data class UpdateSaga(
        val titleInput: String,
        val descriptionInput: String,
    ) : AgenticAction()

    data class UpdateCharacter(
        val nameInput: String,
        val genderInput: String,
        val descriptionInput: String,
    ) : AgenticAction()
}

@HiltViewModel
class NewSagaViewModel
    @Inject
    constructor(
        private val newSagaUseCase: NewSagaUseCase,
        private val visualConfigService: GenreVisualConfigService,
    ) : ViewModel() {
        private val _feed = MutableStateFlow<List<AgenticUIComponent>>(emptyList())
        val feed = _feed.asStateFlow()

        private val _lockedSaga = MutableStateFlow<SagaDraft?>(null)
        val lockedSaga = _lockedSaga.asStateFlow()

        private val _lockedCharacter = MutableStateFlow<CharacterInfo?>(null)
        val lockedCharacter = _lockedCharacter.asStateFlow()

        private val _isReadyToSave = MutableStateFlow(false)
        val isReadyToSave: StateFlow<Boolean> = _isReadyToSave.asStateFlow()

        private val _isSaving = MutableStateFlow(false)
        val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

        private val _loadingMessage = MutableStateFlow<String?>(null)
        val loadingMessage: StateFlow<String?> = _loadingMessage.asStateFlow()

        private val _currentAgentMessage = MutableStateFlow<String?>(null)
        val currentAgentMessage = _currentAgentMessage.asStateFlow()

        private val _isAgentLoading = MutableStateFlow(false)
        val isAgentLoading = _isAgentLoading.asStateFlow()

        private val _savingError = MutableStateFlow<String?>(null)
        val savingError: StateFlow<String?> = _savingError.asStateFlow()

        val effect = MutableStateFlow<Effect?>(null)

        val genresVisuals = MutableStateFlow<List<Pair<Genre, GenreVisualConfig?>>?>(null)

        val savedContent = MutableStateFlow<Pair<Saga?, Character?>?>(null)
        val currentConfig = MutableStateFlow<GenreVisualConfig?>(null)

        private val _sagaAssist = MutableStateFlow(CreationAssist())
        val sagaAssist = _sagaAssist.asStateFlow()

        private val _characterAssist = MutableStateFlow(CreationAssist())
        val characterAssist = _characterAssist.asStateFlow()

        private val _themeAssist = MutableStateFlow(CreationAssist())
        val themeAssist = _themeAssist.asStateFlow()

        private var assistJob: kotlinx.coroutines.Job? = null

        init {
            preFetchVisualConfigs()
            submitUserPrompt("Hello! Help me start a new saga.")
        }

        fun onAgenticAction(action: AgenticAction) {
            viewModelScope.launch(Dispatchers.IO) {
                when (action) {
                    is AgenticAction.SubmitPrompt -> {
                        submitUserPrompt(action.prompt)
                    }

                    is AgenticAction.SelectSaga -> {
                        val visualConfig = visualConfigService.getVisualConfig(action.draft.genre)
                        _lockedSaga.value = action.draft
                        currentConfig.emit(visualConfig)

                        submitUserPrompt("Suggest characters for this story")
                    }

                    is AgenticAction.SelectCharacter -> {
                        val saga = _lockedSaga.value ?: return@launch
                        visualConfigService.getVisualConfig(saga.genre)
                        _lockedCharacter.value = action.info
                        _isReadyToSave.value = true
                    }

                    is AgenticAction.UnlockSaga -> {
                        _lockedSaga.value = null
                        _lockedCharacter.value = null
                        _isReadyToSave.value = false
                        _feed.emit(
                            _feed.value.filter { it !is PersonaPitches },
                        )
                    }

                    is AgenticAction.UnlockCharacter -> {
                        _lockedCharacter.value = null
                        _isReadyToSave.value = false
                    }

                    is AgenticAction.SaveSaga -> {
                        saveSaga()
                    }

                    is AgenticAction.UpdateSaga -> {
                        _lockedSaga.value?.let { current ->
                            updateSagaDraft(
                                current.copy(
                                    title = action.titleInput,
                                    description = action.descriptionInput,
                                ),
                            )
                        }
                    }

                    is AgenticAction.UpdateCharacter -> {
                        _lockedCharacter.value?.let { current ->
                            updateCharacterDraft(
                                current.copy(
                                    name = action.nameInput,
                                    gender = action.genderInput,
                                    description = action.descriptionInput,
                                ),
                            )
                        }
                    }
                }
            }
        }

        private fun submitUserPrompt(prompt: String) {
            if (prompt.isBlank()) return

            newSagaUseCase
                .executePrompt(prompt, _lockedSaga.value, _lockedCharacter.value)
                .onStart { _isAgentLoading.value = true }
                .onCompletion { _isAgentLoading.value = false }
                .onEach { response: AgenticFlowResponse ->
                    handleAgenticResponse(response)
                }.launchIn(viewModelScope)
        }

        private fun handleAgenticResponse(response: AgenticFlowResponse) {
            viewModelScope.launch {
                when (response) {
                    is AgenticFlowResponse.Log -> {
                        _currentAgentMessage.value = response.message
                    }

                    is AgenticFlowResponse.SagaPitches -> {
                        val ideas =
                            response.ideas.mapNotNull {
                                val config =
                                    visualConfigService.getVisualConfig(it.genre)
                                        ?: return@mapNotNull null
                                it to config
                            }
                        response.message?.let {
                            _currentAgentMessage.value = it
                        }
                        _feed.value += IdeaPitches(ideas)
                    }

                    is AgenticFlowResponse.CharacterPitches -> {
                        val saga = _lockedSaga.value ?: return@launch
                        val visualConfig = visualConfigService.getVisualConfig(saga.genre)
                        val personas =
                            response.personas.map {
                                if (it.id.isBlank()) {
                                    it.copy(
                                        id = System.currentTimeMillis().toString() + it.name.hashCode(),
                                    )
                                } else {
                                    it
                                }
                            }
                        response.message?.let {
                            _currentAgentMessage.value = it
                        }
                        _feed.value += PersonaPitches(personas, saga.genre to visualConfig)
                    }

                    is AgenticFlowResponse.RefinedDraft -> {
                        // This could be used for final refinement confirmation
                    }

                    is AgenticFlowResponse.Error -> {
                        _currentAgentMessage.value = "Error: ${response.throwable.message}"
                    }
                }
            }
        }

        private fun preFetchVisualConfigs() {
            viewModelScope.launch(Dispatchers.IO) {
                genresVisuals.value =
                    Genre.entries.map {
                        it to visualConfigService.getVisualConfig(it)
                    }
            }
        }

        // Old flow methods removed to avoid StateManager dependencies

        fun updateGenre(genre: Genre) {
            _lockedSaga.value = _lockedSaga.value?.copy(genre = genre)
            // Ideally trigger adaptation via Agent
        }

        fun updateSagaDraft(draft: SagaDraft) {
            _lockedSaga.value = draft
        }

        fun updateCharacterDraft(info: CharacterInfo) {
            _lockedCharacter.value = info
        }

        fun saveSaga() {
            val sagaDraft = _lockedSaga.value ?: return
            val characterInfo = _lockedCharacter.value ?: return

            _isSaving.value = true
            _savingError.value = null

            newSagaUseCase
                .createCompleteSagaFlow(
                    sagaDraft = sagaDraft,
                    characterInfo = characterInfo,
                    sagaMessages = emptyList(),
                ).onEach { state: SagaCreationState ->
                    when (state) {
                        is SagaCreationState.Loading -> {
                            _loadingMessage.value = state.message
                        }

                        is SagaCreationState.Success -> {
                            savedContent.value = state.saga to state.character
                            _isSaving.value = false
                            _loadingMessage.value = null
                            viewModelScope.launch {
                                delay(3000)
                                reset()
                                navigateToSaga(state.saga)
                            }
                        }

                        is SagaCreationState.AgenticUpdate -> {
                            handleAgenticResponse(state.response)
                        }

                        is SagaCreationState.Error -> {
                            Log.e(
                                javaClass.simpleName,
                                "saveSaga: Error saving saga",
                                state.error,
                            )
                            _savingError.value =
                                state.error.message ?: "Unknown error occurred while saving"
                            _isSaving.value = false
                            _loadingMessage.value = null
                        }
                    }
                }.launchIn(viewModelScope)
        }

        fun retry() {
            _savingError.value = null
            saveSaga()
        }

        fun reset() {
            _lockedSaga.value = null
            _lockedCharacter.value = null
            _feed.value = emptyList()
            _isReadyToSave.value = false
            _isSaving.value = false
            _loadingMessage.value = null
            _savingError.value = null
        }

        private fun navigateToSaga(saga: Saga) {
            effect.value =
                Effect.Navigate(
                    route = Routes.CHAT,
                    arguments =
                        mapOf(
                            "sagaId" to saga.id.toString(),
                            "isDebug" to false.toString(),
                        ),
                )
        }
    }
