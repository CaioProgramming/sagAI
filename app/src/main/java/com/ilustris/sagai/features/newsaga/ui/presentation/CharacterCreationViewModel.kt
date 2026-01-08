package com.ilustris.sagai.features.newsaga.ui.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ilustris.sagai.features.newsaga.data.model.CallBackAction
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.model.Sender
import com.ilustris.sagai.features.newsaga.data.usecase.NewSagaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterCreationViewModel
    @Inject
    constructor(
        private val newSagaUseCase: NewSagaUseCase,
    ) : ViewModel() {
        private val _characterMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
        val characterMessages: StateFlow<List<ChatMessage>> = _characterMessages

        private val _currentPrompt = MutableStateFlow("")
        val currentPrompt: StateFlow<String> = _currentPrompt

        private val _currentHint = MutableStateFlow<String?>(null)
        val currentHint: StateFlow<String?> = _currentHint

        private val _suggestions = MutableStateFlow<List<String>>(emptyList())
        val suggestions: StateFlow<List<String>> = _suggestions

        private val _isGenerating = MutableStateFlow(false)
        val isGenerating: StateFlow<Boolean> = _isGenerating

        private val _callback = MutableStateFlow<CallBackAction?>(null)
        val callback: StateFlow<CallBackAction?> = _callback

        private var sagaContext: SagaDraft? = null

        fun startCharacterCreation(context: SagaDraft?) {
            if (characterMessages.value.isNotEmpty()) {
                return
            }
            sagaContext = context
            _isGenerating.value = true
            viewModelScope.launch(Dispatchers.IO) {
                newSagaUseCase
                    .generateCharacterIntroduction(sagaContext)
                    .onSuccess { gen ->
                        _characterMessages.update {
                            it +
                                ChatMessage(
                                    text = gen.message,
                                    sender = Sender.AI,
                                )
                        }
                        _currentPrompt.value = gen.message
                        _currentHint.value = gen.inputHint
                        _suggestions.value = gen.suggestions
                        _callback.value = gen.callback?.action
                        _isGenerating.value = false
                    }.onFailure {
                        Log.e(
                            javaClass.simpleName,
                            "startCharacterCreation: Error generating intro",
                            it,
                        )
                        _isGenerating.value = false
                    }
            }
        }

        fun sendCharacterMessage(
            userInput: String,
            currentFormData: com.ilustris.sagai.features.newsaga.data.model.SagaForm,
        ) {
            _isGenerating.value = true
            viewModelScope.launch(Dispatchers.IO) {
                val userMessage = ChatMessage(text = userInput, sender = Sender.USER)
                _characterMessages.update { it + userMessage }

                val latestAiMessage = _characterMessages.value.findLast { it.sender == Sender.AI }?.text

                newSagaUseCase
                    .replyAiForm(
                        currentMessages = _characterMessages.value,
                        latestMessage = latestAiMessage,
                        currentFormData = currentFormData,
                    ).onSuccess { response ->
                        _characterMessages.update {
                            it +
                                ChatMessage(
                                    text = response.message,
                                    sender = Sender.AI,
                                    callback = response.callback?.action,
                                )
                        }
                        _currentPrompt.value = response.message
                        _currentHint.value = response.inputHint
                        _suggestions.value = response.suggestions
                        _callback.value = response.callback?.action
                        _isGenerating.value = false
                    }.onFailure { e ->
                        Log.e(
                            javaClass.simpleName,
                            "sendCharacterMessage: Error getting response $e",
                        )
                        _isGenerating.value = false
                    }
            }
        }

        fun reset() {
            _characterMessages.value = emptyList()
            _currentPrompt.value = ""
            _currentHint.value = null
            _suggestions.value = emptyList()
            _callback.value = null
            _isGenerating.value = false
        }
    }
