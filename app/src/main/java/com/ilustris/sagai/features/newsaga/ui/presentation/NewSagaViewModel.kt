package com.ilustris.sagai.features.newsaga.ui.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.manager.CharacterStateManager
import com.ilustris.sagai.features.newsaga.data.manager.SagaStateManager
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
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

@HiltViewModel
class NewSagaViewModel
    @Inject
    constructor(
        private val sagaStateManager: SagaStateManager,
        private val characterStateManager: CharacterStateManager,
        private val newSagaUseCase: NewSagaUseCase,
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

        init {
            viewModelScope.launch {
                combine(sagaFormState, characterState) { saga, character ->
                    Log.d(javaClass.simpleName, "sagaForm state: $saga ")
                    Log.d(javaClass.simpleName, "character state: $character ")
                    (saga?.isReady == true) && (character?.isReady == true)
                }.collect { ready ->
                    _isReadyToSave.value = ready
                }
            }
        }

        fun startSagaChat() {
            viewModelScope.launch(Dispatchers.IO) {
                sagaStateManager.startChat()
            }
        }

        fun startCharacterCreation() {
            viewModelScope.launch(Dispatchers.IO) {
                val sagaContext = sagaStateManager.getSagaForm()
                characterStateManager.startCharacterCreation(sagaContext)
            }
        }

        fun sendSagaMessage(userInput: String) {
            viewModelScope.launch(Dispatchers.IO) {
                sagaStateManager.sendMessage(userInput)
            }
        }

        fun sendCharacterMessage(userInput: String) {
            viewModelScope.launch(Dispatchers.IO) {
                val sagaForm = sagaStateManager.getSagaForm()
                characterStateManager.sendMessage(userInput, sagaForm)
            }
        }

        fun updateGenre(genre: Genre) {
            sagaStateManager.updateGenre(genre)
        }

        fun saveSaga() {
            if (!_isReadyToSave.value) {
                Log.w(javaClass.simpleName, "saveSaga: Cannot save - saga or character not ready")
                return
            }

            _isSaving.value = true
            _savingError.value = null

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    // Step 1: Create saga
                    generateProcessMessage(SagaProcess.CREATING_SAGA)
                    val sagaResult = sagaStateManager.prepareSagaData()
                    val saga = sagaResult.getSuccess() ?: throw Exception("Failed to create saga")

                    // Step 2: Create character
                    generateProcessMessage(SagaProcess.CREATING_CHARACTER)
                    val characterResult = characterStateManager.prepareCharacterData(saga)

                    val character =
                        characterResult.getSuccess() ?: throw Exception("Failed to create character")

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
                val sagaData = sagaStateManager.getSagaForm().toJsonFormat()
                val characterData = characterStateManager.getCharacterInfo().toJsonFormat()
                newSagaUseCase
                    .generateProcessMessage(
                        process = process,
                        sagaDescription = sagaData,
                        characterDescription = characterData,
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
