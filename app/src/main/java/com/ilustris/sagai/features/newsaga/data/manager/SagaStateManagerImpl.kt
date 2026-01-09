package com.ilustris.sagai.features.newsaga.data.manager

import android.util.Log
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.CallBackAction
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.data.model.Sender
import com.ilustris.sagai.features.newsaga.data.usecase.NewSagaUseCase
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class SagaStateManagerImpl
    @Inject
    constructor(
        private val newSagaUseCase: NewSagaUseCase,
        private val sagaRepository: SagaRepository,
    ) : SagaStateManager {
        private val _formState = MutableStateFlow<FormState.NewSagaForm?>(null)
        override val formState = _formState.asStateFlow()

        override fun updateGenre(genre: Genre) {
            if (_formState.value == null) {
                _formState.value = FormState.NewSagaForm()
            }
            val draft = _formState.value?.draft ?: SagaDraft()
            _formState.value =
                _formState.value?.copy(
                    draft =
                        draft.copy(
                            genre = genre,
                        ),
                )
        }

        override fun handleCallback(action: CallBackAction) {
            if (_formState.value == null) {
                _formState.value = FormState.NewSagaForm()
            }
            _formState.value = _formState.value?.copy(isReady = action == CallBackAction.CONTENT_READY)

            when (action) {
                CallBackAction.CONTENT_READY -> {
                    Log.d(javaClass.simpleName, "handleCallback: Saga is ready to save")
                }

                CallBackAction.UPDATE_DATA,
                CallBackAction.AWAITING_CONFIRMATION,
                -> {
                    Log.d(javaClass.simpleName, "handleCallback: Saga not ready yet")
                }

                else -> {
                    doNothing()
                }
            }
        }

        private fun updateLoading(isLoading: Boolean) {
            if (_formState.value == null) {
                _formState.value = FormState.NewSagaForm()
            }
            _formState.value = _formState.value?.copy(isLoading = isLoading)
        }

        private fun updateMessages(chatMessage: ChatMessage) {
            if (_formState.value == null) {
                _formState.value = FormState.NewSagaForm()
            }
            val messages = _formState.value?.messages ?: emptyList()
            _formState.value =
                _formState.value?.copy(
                    messages = messages.plus(chatMessage),
                )
        }

        override suspend fun sendMessage(userInput: String) {
            updateLoading(true)
            val latestMessage = _formState.value?.messages?.lastOrNull()
            val userMessage = ChatMessage(text = userInput, sender = Sender.USER)
            updateMessages(userMessage)

            newSagaUseCase
                .replyAiForm(
                    currentMessages = _formState.value?.messages ?: emptyList(),
                    latestMessage = latestMessage?.text,
                    currentFormData = _formState.value?.draft ?: SagaDraft(),
                ).onSuccess { response ->
                    updateMessages(
                        ChatMessage(
                            text = response.message,
                            sender = Sender.AI,
                        ),
                    )
                    handleGeneratedContent(response)
                }.onFailure { e ->
                    Log.e(
                        javaClass.simpleName,
                        "sendMessage: Error getting generated content $e",
                    )
                }
            updateLoading(false)
        }

        override suspend fun startChat() {
            if (_formState.value?.messages?.isNotEmpty() == true) return
            updateLoading(true)
            newSagaUseCase
                .generateIntroduction()
                .onSuccess { gen ->
                    updateMessages(
                        ChatMessage(
                            text = gen.message,
                            sender = Sender.AI,
                            callback = gen.callback?.action,
                        ),
                    )
                    _formState.value = _formState.value?.copy(isLoading = false, message = gen.message)
                    handleGeneratedContent(gen)
                }.onFailure {
                    updateLoading(false)
                }
        }

        override fun getSagaForm(): SagaDraft {
            if (_formState.value == null) {
                val newForm = FormState.NewSagaForm()
                _formState.value = newForm
                return newForm.draft
            } else {
                return _formState.value?.draft ?: SagaDraft()
            }
        }

        override fun reset() {
            _formState.value = null
        }

        override suspend fun prepareSagaData(): RequestResult<Saga> =
            executeRequest {
                val chatMessages: List<ChatMessage> = _formState.value?.messages ?: emptyList()
                val generatedSaga =
                    newSagaUseCase.generateSaga(getSagaForm(), chatMessages).getSuccess()!!
                val savedSaga = sagaRepository.saveChat(generatedSaga)
                savedSaga
            }

        private fun updateGeneratedContent(
            hint: String,
            suggestions: List<String>,
        ) {
            if (_formState.value == null) {
                _formState.value = FormState.NewSagaForm()
            }
            _formState.value = _formState.value?.copy(hint = hint, suggestions = suggestions)
        }

        override fun updateSaga(form: SagaDraft) {
            if (_formState.value == null) {
                _formState.value = FormState.NewSagaForm()
            }

            _formState.value = _formState.value?.copy(draft = form)
        }

        private fun handleGeneratedContent(response: com.ilustris.sagai.features.newsaga.data.model.SagaCreationGen) {
            response.callback?.action?.let { handleCallback(it) }
            updateGeneratedContent(
                hint = response.inputHint,
                suggestions = response.suggestions,
            )

            response.callback?.let { callback ->
                Log.d(
                    javaClass.simpleName,
                    "handleGeneratedContent: Checking new generated content $callback",
                )
                val sagaForm: SagaForm? = callback.data
                sagaForm?.let {
                    Log.i(javaClass.simpleName, "handleGeneratedContent: Updating form to $it")
                    updateSaga(
                        SagaDraft(
                            title = it.saga.title,
                            description = it.saga.description,
                            genre = it.saga.genre,
                        ),
                    )
                }
            }
            updateLoading(false)
        }
    }
