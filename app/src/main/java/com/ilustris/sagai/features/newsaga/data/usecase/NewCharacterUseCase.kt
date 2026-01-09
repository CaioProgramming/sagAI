package com.ilustris.sagai.features.newsaga.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.SagaCreationGen
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft

interface NewCharacterUseCase {
    suspend fun generateCharacterIntroduction(sagaContext: SagaDraft?): RequestResult<SagaCreationGen>

    suspend fun replyCharacterForm(
        currentMessages: List<ChatMessage>,
        latestMessage: String?,
        currentCharacterInfo: CharacterInfo,
        sagaContext: SagaDraft,
    ): RequestResult<SagaCreationGen>
}
