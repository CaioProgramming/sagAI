package com.ilustris.sagai.features.newsaga.ui.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.manager.CharacterStateManager
import com.ilustris.sagai.features.newsaga.data.manager.SagaStateManager
import com.ilustris.sagai.features.newsaga.data.model.CreationAssist
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.data.model.isSagaBlank
import com.ilustris.sagai.features.newsaga.data.usecase.NewSagaUseCase
import com.ilustris.sagai.features.newsaga.data.usecase.SagaProcess
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

        private var assistJob: kotlinx.coroutines.Job? = null

        init {
            viewModelScope.launch(Dispatchers.IO) {
                combine(sagaFormState, characterState) { saga, character ->
                    Log.d(javaClass.simpleName, "sagaForm state: $saga ")
                    Log.d(javaClass.simpleName, "character state: $character ")
                    (saga?.isReady == true) && (character?.isReady == true)
                }.collect { ready ->
                    _isReadyToSave.value = ready
                }
            }
            preFetchVisualConfigs()
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

        fun assistCreation(flow: FlowPages) {
            if (flow != FlowPages.CREATE_SAGA && flow != FlowPages.CREATE_CHARACTER && flow != FlowPages.SELECT_THEME) return

            val currentAssist =
                when (flow) {
                    FlowPages.CREATE_SAGA -> _sagaAssist.value
                    FlowPages.CREATE_CHARACTER -> _characterAssist.value
                    FlowPages.SELECT_THEME -> _themeAssist.value
                    else -> CreationAssist()
                }
            if (currentAssist.title.isNotEmpty()) return // Rough "cache" check

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
            viewModelScope.launch(Dispatchers.IO) {
                sagaStateManager.refineDraft(userInput)
            }
        }

        fun sendCharacterMessage(userInput: String) {
            viewModelScope.launch(Dispatchers.IO) {
                val sagaForm = sagaStateManager.getSagaForm()
                characterStateManager.refineDraft(userInput, sagaForm)
            }
        }

        fun updateGenre(genre: Genre) {
            val previousGenre = sagaStateManager.getSagaForm().genre
            sagaStateManager.updateGenre(genre)

            if (previousGenre != genre) {
                viewModelScope.launch {
                    _sagaAssist.emit(CreationAssist())
                    _characterAssist.emit(CreationAssist())
                }
            }

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

        fun enhanceSagaDescription(
            currentInput: String,
            onEnhanced: (String) -> Unit,
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                // Implement AI enhancement call here
                // For now we'll just refine it via the current state manager if needed,
            // but the user wants to "Enhance" the description to update the input field.
            // We'll need a specific prompt for "Enhancement"
        }
    }

        fun saveSaga() {
            _isSaving.value = true
            _savingError.value = null

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    // Step 1: Create saga
                    generateProcessMessage(SagaProcess.CREATING_SAGA)
                    val sagaResult = sagaStateManager.prepareSagaData()
                    val saga = sagaResult.getSuccess() ?: throw Exception("Failed to create saga")

                    savedContent.emit(saga to null)
                    // Step 2: Create character
                    generateProcessMessage(SagaProcess.CREATING_CHARACTER)
                    val characterResult = characterStateManager.prepareCharacterData(saga)

                    val character =
                        characterResult.getSuccess() ?: throw Exception("Failed to create character")

                    savedContent.emit(saga to character)

                    // Step 3: Update saga with character ID
                    val updatedSaga = saga.copy(mainCharacterId = character.id)
                    newSagaUseCase.updateSaga(updatedSaga).getSuccess()
                        ?: throw Exception("Failed to update saga with character")

                    // Step 4: Generate icon
                    generateProcessMessage(SagaProcess.FINALIZING)
                    newSagaUseCase.generateSagaIcon(updatedSaga, character)

                    // Step 5: Success
                    generateProcessMessage(SagaProcess.SUCCESS)

                    _isSaving.value = false
                    _loadingMessage.value = null

                    delay(3.seconds)

                    // Reset and navigate
                    reset()
                    navigateToSaga(updatedSaga)
                } catch (e: Exception) {
                    Log.e(javaClass.simpleName, "saveSaga: Error saving saga", e)
                    _savingError.value = e.message ?: "Unknown error occurred while saving"
                    _isSaving.value = false
                    _loadingMessage.value = null
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

        private fun generateProcessMessage(process: SagaProcess) {
            viewModelScope.launch(Dispatchers.IO) {
                val sagaForm = sagaStateManager.getSagaForm()
                val sagaData = sagaForm.toJsonFormat()
                val characterData = characterStateManager.getCharacterInfo().toJsonFormat()
                newSagaUseCase
                    .generateProcessMessage(
                        process = process,
                        sagaDescription = sagaData,
                        characterDescription = characterData,
                        genre = sagaForm.genre,
                    ).onSuccess { message ->
                        _loadingMessage.value = message
                    }
            }
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
