package com.ilustris.sagai.features.newsaga.ui.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.manager.CharacterStateManager
import com.ilustris.sagai.features.newsaga.data.manager.SagaStateManager
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.CreationAssist
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.data.model.Sender
import com.ilustris.sagai.features.newsaga.data.model.isSagaBlank
import com.ilustris.sagai.features.newsaga.data.usecase.NewSagaUseCase
import com.ilustris.sagai.features.newsaga.data.usecase.SagaCreationState
import com.ilustris.sagai.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

internal data class LoadingState(
    val message: String,
)

enum class FlowPages {
    SELECT_THEME,
    CREATE_SAGA,
    CREATE_CHARACTER,
    GENERATING,
}

@HiltViewModel
class NewSagaViewModel
    @Inject
    constructor(
        private val sagaStateManager: SagaStateManager,
        private val characterStateManager: CharacterStateManager,
        private val newSagaUseCase: NewSagaUseCase,
        private val visualConfigService: GenreVisualConfigService,
    ) : ViewModel() {
        val sagaFormState = sagaStateManager.formState
        val characterState = characterStateManager.characterState

        private val messages = ArrayList<ChatMessage>()
        private val _isReadyToSave = MutableStateFlow(false)
        val isReadyToSave: StateFlow<Boolean> = _isReadyToSave.asStateFlow()

        private val _isSaving = MutableStateFlow(false)
        val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

        private val _loadingMessage = MutableStateFlow<String?>(null)
        val loadingMessage: StateFlow<String?> = _loadingMessage.asStateFlow()

        private val _savingError = MutableStateFlow<String?>(null)
        val savingError: StateFlow<String?> = _savingError.asStateFlow()

        val effect = MutableStateFlow<Effect?>(null)

        val genresVisuals = MutableStateFlow<List<Pair<Genre, GenreVisualConfig?>>?>(null)

        val savedContent = MutableStateFlow<Pair<Saga?, Character?>?>(null)

        private val _sagaAssist = MutableStateFlow(CreationAssist())
        val sagaAssist = _sagaAssist.asStateFlow()

        private val _characterAssist = MutableStateFlow(CreationAssist())
        val characterAssist = _characterAssist.asStateFlow()

        private val _themeAssist = MutableStateFlow(CreationAssist())
        val themeAssist = _themeAssist.asStateFlow()

        val genreVisualConfigService = visualConfigService

        private var assistJob: kotlinx.coroutines.Job? = null

        init {
            viewModelScope.launch(Dispatchers.IO) {
                combine(sagaFormState, characterState) { saga, character ->
                    Log.d(javaClass.simpleName, "sagaForm state: $saga ")
                    Log.d(javaClass.simpleName, "character state: $character ")
                    (saga?.isReady == true) && (character?.isReady == true)
                    messages.add(
                        ChatMessage(
                            text = "Data updated -> \n${getSagaForm().toAINormalize()}",
                            sender = Sender.AI,
                        ),
                    )
                }.collect { ready ->
                    _isReadyToSave.value = ready
                }
            }
            preFetchVisualConfigs()
            observeUpdates()
        }

        private fun observeUpdates() {
            viewModelScope.launch(Dispatchers.IO) {
                combine(sagaFormState, characterState) { saga, character ->
                    getSagaForm()
                }
            }
        }

        private fun preFetchVisualConfigs() {
            viewModelScope.launch(Dispatchers.IO) {
                genresVisuals.emit(
                    Genre.entries.map {
                        it to visualConfigService.getVisualConfig(it)
                    },
                )
            }
        }

        fun assistCreation(
            flow: FlowPages,
            force: Boolean = false,
        ) {
            if (flow != FlowPages.CREATE_SAGA && flow != FlowPages.CREATE_CHARACTER && flow != FlowPages.SELECT_THEME) return

            val currentAssist =
                when (flow) {
                    FlowPages.CREATE_SAGA -> _sagaAssist.value
                    FlowPages.CREATE_CHARACTER -> _characterAssist.value
                    FlowPages.SELECT_THEME -> _themeAssist.value
                    else -> CreationAssist()
                }
            if (currentAssist.title.isNotEmpty() && !force) return // Rough "cache" check

            assistJob?.cancel()
            assistJob =
                viewModelScope.launch(Dispatchers.IO) {
                    val result =
                        newSagaUseCase.assistCreation(
                            flow = flow,
                            sagaDraft = sagaStateManager.getSagaForm(),
                            characterInfo = characterStateManager.getCharacterInfo(),
                        )

                    result.onSuccessAsync { assist ->
                        when (flow) {
                            FlowPages.CREATE_SAGA -> _sagaAssist.emit(assist)
                            FlowPages.CREATE_CHARACTER -> _characterAssist.emit(assist)
                            FlowPages.SELECT_THEME -> _themeAssist.emit(assist)
                            else -> doNothing()
                        }
                    }
                }
        }

        fun sendSagaMessage(userInput: String) {
            messages.add(ChatMessage(text = userInput, sender = Sender.USER))
            viewModelScope.launch(Dispatchers.IO) {
                if (userInput.isEmpty()) return@launch
                sagaStateManager.refineDraft(userInput)
            }
        }

        fun sendCharacterMessage(userInput: String) {
            messages.add(ChatMessage(text = userInput, sender = Sender.USER))
            viewModelScope.launch(Dispatchers.IO) {
                if (userInput.isEmpty()) return@launch
                val sagaForm = sagaStateManager.getSagaForm()
                characterStateManager.refineDraft(userInput, sagaForm)
            }
        }

        fun updateGenre(genre: Genre) {
            sagaStateManager.updateGenre(genre)

            viewModelScope.launch(Dispatchers.IO) {
                if (getSagaForm().isSagaBlank().not()) {
                    sagaStateManager.adaptToGenre()
                    sagaStateManager.generateGenreSuggestions()
                }
            }
            viewModelScope.launch(Dispatchers.IO) {
                characterStateManager.adaptToGenre(genre.name)
            }
        }

        fun refineDraft(rawInput: String) {
            viewModelScope.launch(Dispatchers.IO) {
                sagaStateManager.refineDraft(rawInput)
            }
        }

        fun updateSagaDraft(draft: SagaDraft) {
            sagaStateManager.updateSaga(draft)
        }

        fun updateCharacterDraft(info: CharacterInfo) {
            characterStateManager.updateCharacter(info)
        }

        fun saveSaga() {
            _isSaving.value = true
            _savingError.value = null

            viewModelScope.launch(Dispatchers.IO) {
                newSagaUseCase
                    .createCompleteSagaFlow(
                        sagaDraft = sagaStateManager.getSagaForm(),
                        characterInfo = characterStateManager.getCharacterInfo(),
                        sagaMessages = messages,
                    ).collect { state ->
                        when (state) {
                            is SagaCreationState.Loading -> {
                                _loadingMessage.value = state.message
                            }

                            is SagaCreationState.Success -> {
                                savedContent.emit(state.saga to state.character)
                                _isSaving.value = false
                                _loadingMessage.value = null
                                delay(3.seconds)
                                reset()
                                navigateToSaga(state.saga)
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
                    }
            }
        }

        fun retry() {
            _savingError.value = null
            saveSaga()
        }

        fun reset() {
            sagaStateManager.reset()
            characterStateManager.reset()
            _isReadyToSave.value = false
            _isSaving.value = false
            _loadingMessage.value = null
            _savingError.value = null
        }

        private fun getSagaForm() =
            SagaForm(
                saga = sagaStateManager.getSagaForm(),
                character = characterStateManager.getCharacterInfo(),
            )

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
