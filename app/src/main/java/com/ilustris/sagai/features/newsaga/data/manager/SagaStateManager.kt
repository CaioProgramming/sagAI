package com.ilustris.sagai.features.newsaga.data.manager

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.CallBackAction
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import kotlinx.coroutines.flow.StateFlow

interface SagaStateManager {
    val formState: StateFlow<FormState.NewSagaForm?>

    fun updateSaga(form: SagaDraft)

    fun updateGenre(genre: Genre)

    fun handleCallback(action: CallBackAction)

    suspend fun sendMessage(
        userInput: String,
        currentCharacter: CharacterInfo?,
    )

    suspend fun startChat()

    fun getSagaForm(): SagaDraft

    fun reset()

    suspend fun prepareSagaData(): RequestResult<Saga>
}

sealed interface FormState {
    val message: String?
    val hint: String?
    val isLoading: Boolean
    val isReady: Boolean
    val suggestions: List<String>

    data class NewSagaForm(
        val messages: List<ChatMessage> = emptyList(),
        val draft: SagaDraft = SagaDraft(),
        override val message: String? = null,
        override val hint: String? = null,
        override val isLoading: Boolean = false,
        override val isReady: Boolean = false,
        override val suggestions: List<String> = emptyList(),
    ) : FormState

    data class CharacterForm(
        val characterInfo: CharacterInfo = CharacterInfo(),
        override val message: String? = null,
        override val hint: String? = null,
        override val isLoading: Boolean = false,
        override val isReady: Boolean = false,
        override val suggestions: List<String> = emptyList(),
    ) : FormState
}
