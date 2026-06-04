package com.ilustris.sagai.features.newsaga.ui.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
import com.ilustris.sagai.core.data.isFlowCancellation
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.services.getGenderPlaceholders
import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.GenderPlaceholderMap
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.LibraryPitchesResponse
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.usecase.NewSagaUseCase
import com.ilustris.sagai.features.newsaga.data.usecase.SagaBook
import com.ilustris.sagai.features.newsaga.data.usecase.SagaCreationState
import com.ilustris.sagai.ui.navigation.ChatKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class NewSagaViewModel
    @Inject
    constructor(
        private val newSagaUseCase: NewSagaUseCase,
        private val visualConfigService: GenreVisualConfigService,
        private val remoteConfig: RemoteConfigService,
        private val stringHelper: StringResourceHelper,
    ) : ViewModel() {
        private fun genericErrorMessage(): String = stringHelper.getString(R.string.unexpected_error)

        val genderPlaceholders = MutableStateFlow<GenderPlaceholderMap>(emptyMap())

        private val _uiState = MutableStateFlow(NewSagaUiState())
        val uiState: StateFlow<NewSagaUiState> = _uiState.asStateFlow()

        val effect = MutableStateFlow<Effect?>(null)

        private var promptJob: Job? = null
        private var initialEchoesJob: Job? = null
        private var echoesRequestGeneration = 0
        private var lastUserPrompt: String = ""

        init {
            preFetchVisualConfigs()
            fetchGenderPlaceholders()
            requestInitialEchoes()
        }

        private fun updateState(reducer: NewSagaUiState.() -> NewSagaUiState) {
            _uiState.update(reducer)
    }

        private fun cancelInitialEchoes() {
            echoesRequestGeneration++
            initialEchoesJob?.cancel()
            initialEchoesJob = null
        }

        private fun requestInitialEchoes() {
            val generation = echoesRequestGeneration
            initialEchoesJob?.cancel()
            initialEchoesJob =
                viewModelScope.launch {
                    try {
                        newSagaUseCase
                            .provideInitialEchoes()
                            .onSuccessAsync {
                                val state = _uiState.value
                                if (generation != echoesRequestGeneration || state.isGenerating) {
                                    return@onSuccessAsync
                                }
                                val echoes =
                                    it.suggestions.mapNotNull { echo ->
                                        val config = visualConfigService.getVisualConfig(echo.genre)
                                        if (config != null) echo to config else null
                                    }
                                updateState {
                                    copy(
                                        universeEchoes = echoes,
                                        statusMessage = it.message.ifBlank { statusMessage },
                                    )
                                }
                            }.onFailureAsync {
                                val state = _uiState.value
                                if (generation != echoesRequestGeneration || state.isGenerating) {
                                    return@onFailureAsync
                                }
                                updateState { copy(error = genericErrorMessage()) }
                            }
                    } catch (e: Exception) {
                        if (e.isFlowCancellation()) {
                            Timber.d("Initial universe echoes cancelled — prioritizing user prompt")
                            return@launch
                        }
                        throw e
                    }
                }
        }

        private fun fetchGenderPlaceholders() {
            viewModelScope.launch {
                genderPlaceholders.value = remoteConfig.getGenderPlaceholders()
            }
        }

        fun onIntent(intent: NewSagaIntent) {
            when (intent) {
                is NewSagaIntent.SubmitPrompt -> {
                    submitUserPrompt(intent.prompt)
                }

                is NewSagaIntent.SelectSaga -> {
                    selectSaga(intent.draft)
                }

                is NewSagaIntent.SelectCharacter -> {
                    selectCharacter(intent.info)
                }

                is NewSagaIntent.UnlockSaga -> {
                    unlockSaga()
                }

                is NewSagaIntent.UnlockCharacter -> {
                    unlockCharacter()
                }

                is NewSagaIntent.SaveSaga -> {
                    saveSaga()
                }

                is NewSagaIntent.UpdateSaga -> {
                    updateSaga(intent.id, intent.titleInput, intent.descriptionInput)
                }

                is NewSagaIntent.UpdateCharacter -> {
                    updateCharacter(intent.id, intent.nameInput, intent.descriptionInput)
                }

                is NewSagaIntent.SelectEcho -> {
                    selectEcho(intent)
                }

                is NewSagaIntent.LoadMore -> {
                    loadMore()
                }
            }
        }

        private fun selectSaga(draft: SagaDraft) {
            viewModelScope.launch(Dispatchers.IO) {
                val visualConfig = visualConfigService.getVisualConfig(draft.genre)
                updateState {
                    copy(
                        lockedSaga = draft,
                        lockedCharacter = null,
                        isReadyToSave = false,
                        currentVisualConfig = visualConfig,
                )
                }
            }
        }

        private fun selectCharacter(info: CharacterInfo) {
            val saga = _uiState.value.lockedSaga ?: return
            viewModelScope.launch(Dispatchers.IO) {
                visualConfigService.getVisualConfig(saga.genre)
                updateState {
                    copy(
                        lockedCharacter = info,
                        isReadyToSave = true,
                    )
                }
            }
        }

        private fun unlockSaga() {
            updateState {
                copy(
                    lockedSaga = null,
                    lockedCharacter = null,
                    isReadyToSave = false,
                    currentVisualConfig = null,
                )
            }
        }

        private fun unlockCharacter() {
            updateState {
                copy(
                    lockedCharacter = null,
                    isReadyToSave = false,
                )
            }
        }

        private fun selectEcho(intent: NewSagaIntent.SelectEcho) {
            updateState {
                copy(
                    statusMessage = "Exploring the ${intent.echo.genre.name} universe...",
                    universeEchoes = emptyList(),
                )
            }
            submitUserPrompt(intent.echo.input)
        }

        private fun loadMore() {
            val state = _uiState.value
            if (lastUserPrompt.isBlank() || state.isGenerating || state.isLoadingMore || !state.hasMoreGenres) {
                return
            }
            val usedGenres = state.libraryBooks.map { it.first.draft.genre }
            collectLibraryStream(
                prompt = lastUserPrompt,
                excludedGenres = usedGenres,
                append = true,
            )
        }

        private fun updateSaga(
            id: String?,
            titleInput: String,
            descriptionInput: String,
        ) {
            val current = _uiState.value.lockedSaga ?: return
            if (current.id != id) {
                Timber.e("Saga ID mismatch: ${current.id} != $id")
                return
            }
            val updated =
                current.copy(
                    title = titleInput,
                    description = descriptionInput,
                )
            Timber.i("updateSaga: ${updated.toJsonFormat()}")
            updateState {
                copy(
                    lockedSaga = updated,
                    libraryBooks =
                        libraryBooks.map { entry ->
                            if (entry.first.draft.id == updated.id) {
                                entry.copy(first = entry.first.copy(draft = updated))
                            } else {
                                entry
                            }
                        },
                )
            }
        }

        private fun updateCharacter(
            id: String?,
            nameInput: String,
            descriptionInput: String,
        ) {
            val lockedSagaId = _uiState.value.lockedSaga?.id ?: return
            val bookEntry =
                _uiState.value.libraryBooks.find { it.first.draft.id == lockedSagaId } ?: return
            val book = bookEntry.first
            val updatedCharacters =
                book.characters.map { character ->
                    if (character.id == id) {
                        character.copy(name = nameInput, description = descriptionInput)
                    } else {
                        character
                    }
                }
            val updatedBook = book.copy(characters = updatedCharacters)
            updateState {
                val updatedLockedCharacter =
                    lockedCharacter?.takeIf { it.id == id }?.copy(
                    name = nameInput,
                    description = descriptionInput,
                ) ?: lockedCharacter
                copy(
                libraryBooks =
                        libraryBooks.map { entry ->
                            if (entry.first.draft.id == updatedBook.draft.id) {
                                entry.copy(first = updatedBook)
                            } else {
                                entry
                            }
                        },
                    lockedCharacter = updatedLockedCharacter,
                )
            }
        }

        private fun submitUserPrompt(prompt: String) {
            if (prompt.isBlank()) return
            lastUserPrompt = prompt
            cancelInitialEchoes()
            updateState {
                copy(
                    error = null,
                    universeEchoes = emptyList(),
                    libraryBooks = emptyList(),
                    lockedSaga = null,
                    lockedCharacter = null,
                    isReadyToSave = false,
                    currentVisualConfig = null,
                )
            }
            collectLibraryStream(prompt = prompt, append = false)
        }

        private fun collectLibraryStream(
            prompt: String,
            excludedGenres: List<Genre> = emptyList(),
            append: Boolean = false,
        ) {
            promptJob?.cancel()
            promptJob =
                newSagaUseCase
                    .executePrompt(prompt, excludedGenres)
                    .onStart {
                        updateState {
                            copy(
                                isGenerating = !append,
                                isLoadingMore = append,
                                error = null,
                            )
                        }
                    }.onCompletion {
                        updateState {
                            copy(
                                isGenerating = false,
                                isLoadingMore = false,
                            )
                        }
                    }.onEach { state ->
                        when (state) {
                            is StreamingState.Reasoning -> {
                                updateState { copy(statusMessage = state.chunk) }
                            }

                            is StreamingState.Success -> {
                                viewModelScope.launch(Dispatchers.IO) {
                                    mergeBooks(state.data, append)
                                }
                            }

                            is StreamingState.Error -> {
                                if (!state.isFlowCancellation()) {
                                    updateState {
                                        copy(
                                            error = genericErrorMessage(),
                                            statusMessage = null,
                                        )
                                    }
                                }
                            }
                        }
                    }.launchIn(viewModelScope)
        }

        private suspend fun mergeBooks(
            response: LibraryPitchesResponse,
            append: Boolean,
        ) {
            val newBooks =
                response.books.mapNotNull { book ->
                    val normalized = normalizeBook(book)
                    val config = visualConfigService.getVisualConfig(normalized.draft.genre)
                    if (config != null) normalized to config else null
                }
            val welcomeMessage = response.welcomeMessage.ifBlank { null }
            updateState {
                val mergedBooks =
                    if (append) {
                        val existingGenres = libraryBooks.map { it.first.draft.genre }.toSet()
                        libraryBooks +
                            newBooks.filter { it.first.draft.genre !in existingGenres }
                    } else {
                    newBooks
                    }
                copy(
                    libraryBooks = mergedBooks,
                statusMessage = welcomeMessage ?: statusMessage,
                    isSaving = false,
                    isGenerating = false,
                    isLoadingMore = false,
                )
            }
        }

        private fun normalizeBook(book: SagaBook): SagaBook {
            val draftId = book.draft.id?.ifBlank { UUID.randomUUID().toString() }
            val draft = book.draft.copy(id = draftId)
            val characters =
                book.characters.map { character ->
                    if (character.id.isNullOrBlank()) {
                    character.copy(
                        id = "${System.currentTimeMillis()}_${character.name.hashCode()}",
                    )
                    } else {
                        character
                    }
                }
        return book.copy(draft = draft, characters = characters)
        }

        private fun preFetchVisualConfigs() {
            viewModelScope.launch(Dispatchers.IO) {
                val visuals =
                    Genre.entries.map { genre ->
                        genre to visualConfigService.getVisualConfig(genre)
                    }
                updateState { copy(genresVisuals = visuals) }
            }
        }

    private fun saveSaga() {
        val sagaDraft = _uiState.value.lockedSaga ?: return
        val characterInfo = _uiState.value.lockedCharacter ?: return
        updateState { copy(isSaving = true, savingError = null) }

            newSagaUseCase
                .sealSacredContract(
                    sagaDraft = sagaDraft,
                    characterInfo = characterInfo,
                ).onEach { state ->
                    when (state) {
                        is SagaCreationState.Loading -> {
                            updateState { copy(statusMessage = state.message) }
                        }

                        is SagaCreationState.Success -> {
                            updateState { copy(isSaving = false) }
                            viewModelScope.launch {
                                delay(3000)
                                resetAfterSave()
                                navigateToSaga(state.saga)
                            }
                        }

                        is SagaCreationState.Error -> {
                            Timber.e(state.error, "saveSaga: Error saving saga")
                            updateState {
                                copy(
                                    savingError =
                                        state.error.message
                                            ?: "Unknown error occurred while saving",
                                    isSaving = false,
                                )
                            }
                        }
                    }
                }.launchIn(viewModelScope)
        }

        fun retry() {
            updateState { copy(savingError = null) }
            saveSaga()
        }

    private fun resetAfterSave() {
        updateState {
            NewSagaUiState(
                genresVisuals = genresVisuals,
            )
        }
        lastUserPrompt = ""
        requestInitialEchoes()
        }

        private fun navigateToSaga(saga: Saga) {
            effect.value =
                Effect.Navigate(
                    key = ChatKey(saga.id.toString(), false),
                )
        }
    }
