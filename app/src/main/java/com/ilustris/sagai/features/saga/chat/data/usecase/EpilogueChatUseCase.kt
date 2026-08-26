package com.ilustris.sagai.features.saga.chat.data.usecase

import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.features.characters.data.model.CharacterArc
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.EpilogueMessage
import com.ilustris.sagai.features.saga.chat.data.model.EpilogueReply
import kotlinx.coroutines.flow.Flow

/**
 * Generates an ephemeral, streamed reply for the epilogue chat — a no-stakes conversation with a
 * character after their saga has ended. Deliberately never touches [com.ilustris.sagai.features.saga.chat.datasource.MessageDao]
 * or [com.ilustris.sagai.features.saga.chat.repository.MessageRepository]: nothing generated
 * here is ever persisted.
 *
 * Streamed like [com.ilustris.sagai.features.saga.chat.data.usecase.MessageUseCase.generateMessage]
 * so the player sees reasoning chunks while waiting instead of a static spinner, and every
 * implementation must catch its own failures and emit [StreamingState.Error] — never let a failed
 * blueprint fetch or generation call propagate as an uncaught exception.
 */
interface EpilogueChatUseCase {
    fun openConversation(
        saga: SagaContent,
        character: CharacterContent,
        arcs: List<CharacterArc>,
    ): Flow<StreamingState<EpilogueReply?>>

    fun reply(
        saga: SagaContent,
        character: CharacterContent,
        arcs: List<CharacterArc>,
        conversationSoFar: List<EpilogueMessage>,
        userMessage: String,
    ): Flow<StreamingState<EpilogueReply?>>
}
