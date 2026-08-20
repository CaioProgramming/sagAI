package com.ilustris.sagai.features.saga.chat.data.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.characters.data.model.CharacterArc
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.EpilogueMessage
import com.ilustris.sagai.features.saga.chat.data.model.EpilogueReply

/**
 * Generates a single ephemeral reply for the epilogue chat — a no-stakes conversation with a
 * character after their saga has ended. Deliberately never touches [com.ilustris.sagai.features.saga.chat.datasource.MessageDao]
 * or [com.ilustris.sagai.features.saga.chat.repository.MessageRepository]: nothing generated
 * here is ever persisted.
 *
 * Wrapped in [RequestResult] like every other AI call in this codebase (see
 * [com.ilustris.sagai.features.saga.chat.data.usecase.MessageUseCaseImpl]) — a failed blueprint
 * fetch or generation call must surface as a recoverable error, never an uncaught exception that
 * crashes the app.
 */
interface EpilogueChatUseCase {
    suspend fun openConversation(
        saga: SagaContent,
        character: CharacterContent,
        arcs: List<CharacterArc>,
    ): RequestResult<EpilogueReply?>

    suspend fun reply(
        saga: SagaContent,
        character: CharacterContent,
        arcs: List<CharacterArc>,
        conversationSoFar: List<EpilogueMessage>,
        userMessage: String,
    ): RequestResult<EpilogueReply?>
}
