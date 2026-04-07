package com.ilustris.sagai.features.newsaga.data.manager

import com.ilustris.sagai.features.characters.data.model.CharacterInfo
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

    suspend fun adaptToGenre(newGenre: String)

    suspend fun refineDraft(
        rawInput: String,
        sagaForm: SagaDraft?,
    )
}
