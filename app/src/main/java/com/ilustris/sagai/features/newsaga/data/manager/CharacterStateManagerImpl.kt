package com.ilustris.sagai.features.newsaga.data.manager

import android.util.Log
import com.ilustris.sagai.R
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.CallBackAction
import com.ilustris.sagai.features.newsaga.data.model.CharacterCreationGen
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.CreationSuggestion
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.model.Sender
import com.ilustris.sagai.features.newsaga.data.usecase.NewCharacterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class CharacterStateManagerImpl
    @Inject
    constructor(
        private val newCharacterUseCase: NewCharacterUseCase,
        private val characterUseCase: CharacterUseCase,
        private val stringResourceHelper: StringResourceHelper,
    ) : CharacterStateManager {
        override val chatMessages: MutableList<ChatMessage> = mutableListOf<ChatMessage>()
        private val _characterState = MutableStateFlow<FormState.CharacterForm?>(null)
        override val characterState = _characterState.asStateFlow()

        override fun updateCharacter(info: CharacterInfo) {
            if (_characterState.value == null) {
                _characterState.value = FormState.CharacterForm()
            }
            _characterState.value = _characterState.value?.copy(characterInfo = info)
        }

        override fun handleCallback(action: CallBackAction) {
            if (_characterState.value == null) {
                _characterState.value = FormState.CharacterForm()
            }
            _characterState.value =
                _characterState.value?.copy(
                    isReady = action == CallBackAction.CONTENT_READY,
                )
            when (action) {
                CallBackAction.CONTENT_READY -> {
                    Log.d(javaClass.simpleName, "handleCallback: Character is ready")
                }

                CallBackAction.UPDATE_DATA,
                CallBackAction.AWAITING_CONFIRMATION,
                -> {
                    Log.d(javaClass.simpleName, "handleCallback: Character not ready yet")
                }
            }
        }

        private fun updateLoading(isLoading: Boolean) {
            if (_characterState.value == null) {
                _characterState.value = FormState.CharacterForm()
            }
            _characterState.value = _characterState.value?.copy(isLoading = isLoading)
        }

        override fun getCharacterInfo(): CharacterInfo = _characterState.value?.characterInfo ?: CharacterInfo()

        override suspend fun sendMessage(
            userInput: String,
            sagaForm: SagaDraft,
        ) {
            updateLoading(true)
            val userMessage = ChatMessage(text = userInput, sender = Sender.USER)
            chatMessages.add(userMessage)

            newCharacterUseCase
                .replyCharacterForm(
                    currentMessages = chatMessages,
                    currentCharacterInfo = _characterState.value?.characterInfo ?: CharacterInfo(),
                    sagaContext = sagaForm,
                ).onSuccess { response ->
                    chatMessages.add(
                        ChatMessage(
                            text = response.message,
                            sender = Sender.AI,
                            callback = response.callback?.action,
                        ),
                    )
                    updateAiText(response.message)
                    handleGeneratedContent(response)
                }.onFailure { e ->
                    Log.e(
                        javaClass.simpleName,
                        "sendMessage: Error getting response $e",
                    )
                    updateLoading(false)
                }
        }

        private fun updateAiText(content: String) {
            if (_characterState.value == null) {
                _characterState.value = FormState.CharacterForm()
            }
            _characterState.value = _characterState.value?.copy(message = content)
        }

        override suspend fun startCharacterCreation(sagaContext: SagaDraft?) {
            if (chatMessages.isNotEmpty()) {
                return
            }
            updateLoading(true)
            newCharacterUseCase
                .generateCharacterIntroduction(sagaContext)
                .onSuccess { gen ->
                    chatMessages.add(
                        ChatMessage(
                            text = gen.message,
                            sender = Sender.AI,
                        ),
                    )
                    updateAiText(gen.message)
                    gen.callback?.action?.let { handleCallback(it) }
                    updateLoading(false)
                    handleGeneratedContent(
                        gen,
                    )
                }.onFailure {
                    val fallbackMessage =
                        stringResourceHelper.getString(R.string.character_intro_fallback)
                    val fallbackHint =
                        stringResourceHelper.getString(R.string.character_input_hint_fallback)
                    val fallbackGen =
                        CharacterCreationGen(
                            message = fallbackMessage,
                            inputHint = fallbackHint,
                            suggestions = emptyList(),
                            callback = null,
                        )
                    chatMessages.add(
                        ChatMessage(
                            text = fallbackMessage,
                            sender = Sender.AI,
                        ),
                    )
                    updateAiText(fallbackMessage)
                    updateLoading(false)
                    handleGeneratedContent(fallbackGen)
                }
        }

        override fun reset() {
            _characterState.value = null
        }

        override suspend fun prepareCharacterData(saga: Saga): RequestResult<Character> =
            characterUseCase
                .generateCharacter(
                    SagaContent(saga),
                    _characterState.value?.characterInfo.toAINormalize(),
                )

        private fun updateGeneratedContent(
            hint: String,
            suggestions: List<CreationSuggestion>,
        ) {
            if (_characterState.value == null) {
                _characterState.value = FormState.CharacterForm()
            }
            _characterState.value =
                _characterState.value?.copy(
                    hint = hint,
                    suggestions = suggestions,
                )
        }

        private fun handleGeneratedContent(response: CharacterCreationGen) {
            response.callback?.action?.let { handleCallback(it) }
            updateGeneratedContent(
                hint = response.inputHint,
                suggestions = response.suggestions,
            )

            response.callback?.data?.let { updatedCharacter ->
                Log.i(
                    javaClass.simpleName,
                    "handleGeneratedContent: Updating character to $updatedCharacter",
                )
                updateCharacter(updatedCharacter)
            }
            updateLoading(false)
        }

        override suspend fun adaptToGenre(newGenre: String) {
            val charInfo = getCharacterInfo()
            if (charInfo.name.isBlank() && charInfo.description.isBlank()) return

            updateLoading(true)
            newCharacterUseCase
                .adaptCharacterToGenre(charInfo, newGenre)
                .onSuccess {
                    handleGeneratedContent(it)
                    if (it.message.isNotEmpty()) {
                        chatMessages.add(
                            ChatMessage(
                                text = it.message,
                                sender = Sender.AI,
                            ),
                        )
                        updateAiText(it.message)
                    }
                }.onFailure {
                    Log.e(javaClass.simpleName, "adaptToGenre: Error adapting character to genre", it)
                }
            updateLoading(false)
        }
    }
