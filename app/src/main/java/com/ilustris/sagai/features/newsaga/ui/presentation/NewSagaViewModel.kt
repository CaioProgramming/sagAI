package com.ilustris.sagai.features.newsaga.ui.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.services.getGenderPlaceholders
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.CreationAssist
import com.ilustris.sagai.features.newsaga.data.model.GenderPlaceholderMap
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.model.UniverseEcho
import com.ilustris.sagai.features.newsaga.data.usecase.AgenticFlowResponse
import com.ilustris.sagai.features.newsaga.data.usecase.AgenticUIComponent
import com.ilustris.sagai.features.newsaga.data.usecase.AgenticUIComponent.IdeaPitches
import com.ilustris.sagai.features.newsaga.data.usecase.NewSagaUseCase
import com.ilustris.sagai.features.newsaga.data.usecase.SagaBook
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
        val id: String,
        val titleInput: String,
        val descriptionInput: String,
    ) : AgenticAction()

    data class EnhanceSaga(
        val draft: SagaDraft,
    ) : AgenticAction()

    data class UpdateCharacter(
        val id: String,
        val nameInput: String,
        val descriptionInput: String,
    ) : AgenticAction()

    data class EnhanceCharacter(
        val persona: CharacterInfo,
    ) : AgenticAction()

    data class SelectEcho(
        val echo: UniverseEcho,
    ) : AgenticAction()
}

@HiltViewModel
class NewSagaViewModel
    @Inject
    constructor(
        private val newSagaUseCase: NewSagaUseCase,
        private val visualConfigService: GenreVisualConfigService,
        private val remoteConfig: RemoteConfigService,
    ) : ViewModel() {
        private val _feed = MutableStateFlow<List<AgenticUIComponent>>(emptyList())
        val genderPlaceholders = MutableStateFlow<GenderPlaceholderMap>(emptyMap())
        val feed = _feed.asStateFlow()

        private val _lockedSaga = MutableStateFlow<SagaDraft?>(null)
        val lockedSaga = _lockedSaga.asStateFlow()

        private val _lockedCharacter = MutableStateFlow<CharacterInfo?>(null)
        val lockedCharacter = _lockedCharacter.asStateFlow()

        private val _selectedBook = MutableStateFlow<SagaBook?>(null)
        val selectedBook = _selectedBook.asStateFlow()

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

        private val _isEchoLoading = MutableStateFlow(false)
        val isEchoLoading = _isEchoLoading.asStateFlow()

        private val _libraryBooks =
            MutableStateFlow<List<Pair<SagaBook, GenreVisualConfig>>>(emptyList())
        val libraryBooks = _libraryBooks.asStateFlow()

        private val _uiError = MutableStateFlow<String?>(null)
        val uiError = _uiError.asStateFlow()

        private val _universeEchoes =
            MutableStateFlow<List<Pair<UniverseEcho, GenreVisualConfig>>>(emptyList())
        val universeEchoes = _universeEchoes.asStateFlow()

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
            fetchGenderPlaceholders()
            requestInitialEchoes()
        }

        private fun requestInitialEchoes() {
            _isEchoLoading.value = true
            viewModelScope.launch {
                newSagaUseCase
                    .provideInitialEchoes()
                    .onSuccessAsync {
                        _universeEchoes.value =
                            it.suggestions.mapNotNull { echo ->
                                val config = visualConfigService.getVisualConfig(echo.genre)
                                if (config != null) echo to config else null
                            }
                        _currentAgentMessage.value = it.message
                        _isEchoLoading.value = false
                    }.onFailure {
                        _uiError.value = it.localizedMessage
                        _isEchoLoading.value = false
                    }
            }
        }

        private fun fetchGenderPlaceholders() {
            viewModelScope.launch {
                genderPlaceholders.value = remoteConfig.getGenderPlaceholders()
            }
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
                        _lockedCharacter.value = null
                        currentConfig.emit(visualConfig)

                        val book =
                            _libraryBooks.value.find { it.first.draft.id == action.draft.id }?.first

                        if (book != null) {
                            _selectedBook.value = book
                        } else {
                            submitUserPrompt("Suggest characters for this story")
                        }
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
                        _selectedBook.value = null
                        _isReadyToSave.value = false
                        _feed.emit(
                            _feed.value.filter { it !is AgenticUIComponent.PersonaPitches },
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

                            Log.d(
                                javaClass.simpleName,
                                "Updating character from ${current.toJsonFormat()} ",
                            )
                            Log.d(javaClass.simpleName, "Trying to edit ${action.toJsonFormat()}")
                            if (current.id == action.id) {
                                val updated =
                                    current.copy(
                                        title = action.titleInput,
                                        description = action.descriptionInput,
                                    )
                                _lockedSaga.value = updated
                                _selectedBook.value = _selectedBook.value?.copy(draft = updated)
                                _libraryBooks.value =
                                    _libraryBooks.value.map {
                                        if (it.first.draft.id == updated.id) {
                                            it.copy(first = it.first.copy(draft = updated))
                                        } else {
                                            it
                                        }
                                    }
                                updateFeedWithSaga(updated)
                                Log.i(
                                    javaClass.simpleName,
                                    "onAgenticAction: Updated to ${updated.toJsonFormat()}",
                                )
                            } else {
                                Log.e(
                                    javaClass.simpleName,
                                    "Saga ID mismatch: ${current.id} != ${action.id}",
                                )
                            }
                        }
                    }

                    is AgenticAction.EnhanceSaga -> {
                        submitUserPrompt(
                            "Enchance the story of this saga keeping the same theme. Title: ${action.draft.title}. Description: ${action.draft.description}",
                        )
                    }

                    is AgenticAction.UpdateCharacter -> {
                        _selectedBook.value?.let { book ->
                            Log.d(
                                javaClass.simpleName,
                                "Updating character from ${book.toJsonFormat()} ",
                            )
                            Log.d(javaClass.simpleName, ": Trying to edit ${action.toJsonFormat()}")

                            val updatedCharacters =
                                book.characters.map {
                                    if (it.id == action.id) {
                                        it.copy(
                                            name = action.nameInput,
                                            description = action.descriptionInput,
                                        )
                                    } else {
                                        it
                                    }
                                }
                            val updatedBook = book.copy(characters = updatedCharacters)
                            _selectedBook.value = updatedBook
                            _libraryBooks.value =
                                _libraryBooks.value.map {
                                    if (it.first.draft.id == updatedBook.draft.id) {
                                        it.copy(first = updatedBook)
                                    } else {
                                        it
                                    }
                                }
                            _lockedCharacter.value?.let { current ->
                                if (current.id == action.id) {
                                    _lockedCharacter.value =
                                        current.copy(
                                            name = action.nameInput,
                                            description = action.descriptionInput,
                                        )
                                }
                            }
                            updateFeedWithBook(updatedBook)
                        }
                    }

                    is AgenticAction.EnhanceCharacter -> {
                        submitUserPrompt(
                            "Enchance the biography of the character ${action.persona.name} based on the saga lore. Current bio: ${action.persona.description}",
                        )
                    }

                    is AgenticAction.SelectEcho -> {
                        _currentAgentMessage.value =
                            "Exploring the ${action.echo.genre.name} universe..."
                        _feed.value = emptyList()
                        _universeEchoes.value = emptyList()
                        submitUserPrompt(action.echo.input)
                    }
                }
            }
        }

        private fun updateFeedWithSaga(updatedSaga: SagaDraft) {
            _feed.value =
                _feed.value.map { component ->
                    when (component) {
                        is AgenticUIComponent.LibraryComponent -> {
                            val updatedBooks =
                                component.books.map { bookEntry ->
                                    if (bookEntry.first.draft.id == updatedSaga.id) {
                                        bookEntry.first.copy(draft = updatedSaga) to bookEntry.second
                                    } else {
                                        bookEntry
                                    }
                                }
                            AgenticUIComponent.LibraryComponent(updatedBooks)
                        }

                        is IdeaPitches -> {
                            val updatedIdeas =
                                component.ideas.map { idea ->
                                    if (idea.first.id == updatedSaga.id) {
                                        updatedSaga to idea.second
                                    } else {
                                        idea
                                    }
                                }
                            IdeaPitches(updatedIdeas)
                        }

                        is AgenticUIComponent.ExpandedSaga -> {
                            if (component.draft.id == updatedSaga.id) {
                                component.copy(draft = updatedSaga)
                            } else {
                                component
                            }
                        }

                        else -> {
                            component
                        }
                    }
                }
        }

        private fun updateFeedWithBook(updatedBook: SagaBook) {
            _feed.value =
                _feed.value.map { component ->
                    when (component) {
                        is AgenticUIComponent.LibraryComponent -> {
                            val updatedBooks =
                                component.books.map { bookEntry ->
                                    if (bookEntry.first.draft.id == updatedBook.draft.id) {
                                        updatedBook to bookEntry.second
                                    } else {
                                        bookEntry
                                    }
                                }
                            AgenticUIComponent.LibraryComponent(updatedBooks)
                        }

                        is AgenticUIComponent.PersonaPitches -> {
                            // Also update persona pitches if they are visible for this saga
                            if (updatedBook.draft.id == _lockedSaga.value?.id) {
                                component.copy(personas = updatedBook.characters)
                            } else {
                                component
                            }
                        }

                        is AgenticUIComponent.ExpandedCharacter -> {
                            val matchingPersona =
                                updatedBook.characters.find { it.id == component.persona.id }
                            if (matchingPersona != null) {
                                component.copy(persona = matchingPersona)
                            } else {
                                component
                            }
                        }

                        else -> {
                            component
                        }
                    }
                }
        }

        private fun submitUserPrompt(prompt: String) {
            if (prompt.isBlank()) return
            _universeEchoes.value = emptyList()
            _libraryBooks.value = emptyList()
            _selectedBook.value = null
            _lockedCharacter.value = null
            _lockedSaga.value = null
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

                    is AgenticFlowResponse.LibraryPitches -> {
                        val books =
                            response.books.mapNotNull { book ->
                                val config = visualConfigService.getVisualConfig(book.draft.genre)
                                if (config != null) book to config else null
                            }
                        response.message?.let {
                            _currentAgentMessage.value = it
                        }
                        _libraryBooks.value = books
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
                        _feed.value +=
                            AgenticUIComponent.PersonaPitches(
                                personas,
                                saga.genre to visualConfig,
                            )
                    }

                    is AgenticFlowResponse.UniverseSuggestions -> {
                        // Nothing to do here
                    }

                    is AgenticFlowResponse.RefinedDraft -> {
                        // This could be used for final refinement confirmation
                    }

                    is AgenticFlowResponse.Error -> {
                        _currentAgentMessage.value = null
                        _uiError.value = response.throwable.localizedMessage
                            ?: "An unexpected cosmic alignment error occurred."
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
                .sealSacredContract(
                    sagaDraft = sagaDraft,
                    characterInfo = characterInfo,
                ).onEach { state: SagaCreationState ->
                    when (state) {
                        is SagaCreationState.Loading -> {
                            _currentAgentMessage.value = state.message
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
