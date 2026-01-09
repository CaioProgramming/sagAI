package com.ilustris.sagai.features.newsaga.data.manager

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.CallBackAction
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import kotlinx.coroutines.flow.StateFlow

interface CharacterStateManager {
    val chatMessages: MutableList<ChatMessage>
    val characterState: StateFlow<FormState.CharacterForm?>

    fun getCharacterInfo(): CharacterInfo

    fun updateCharacter(info: CharacterInfo)

    fun handleCallback(action: CallBackAction)

    suspend fun sendMessage(
        userInput: String,
        sagaForm: SagaDraft,
    )

    suspend fun startCharacterCreation(sagaContext: SagaDraft?)

    fun reset()

    suspend fun prepareCharacterData(saga: Saga): RequestResult<Character>
}
