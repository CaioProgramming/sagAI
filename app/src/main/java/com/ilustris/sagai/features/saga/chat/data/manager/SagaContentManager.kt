package com.ilustris.sagai.features.saga.chat.domain.manager

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.Message
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow // Added import
import java.io.File // Added import

interface SagaContentManager {
    val content: MutableStateFlow<SagaContent?>
    val contentUpdateMessages: MutableSharedFlow<Message>
    val endMessage: MutableSharedFlow<String?>
    val ambientMusicFile: StateFlow<File?>
    val narrativeProcessingUiState: StateFlow<Boolean>

    suspend fun loadSaga(sagaId: String)

    suspend fun generateCharacter(description: String): RequestResult<Character>

    suspend fun generateCharacterImage(character: Character): RequestResult<Character>

    fun getDirective(): String

    fun setDebugMode(enabled: Boolean)

    fun isInDebugMode(): Boolean

    fun setProcessing(bool: Boolean)

    fun checkNarrativeProgression(
        saga: SagaContent?,
        isRetrying: Boolean = false,
    )
}
