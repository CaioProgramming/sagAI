package com.ilustris.sagai.features.saga.chat.data.usecase

import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.features.characters.data.model.CharacterArc
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.EpilogueMessage
import com.ilustris.sagai.features.saga.chat.data.model.EpilogueReply
import kotlinx.coroutines.flow.Flow

/**
 * The epilogue chat — a no-stakes conversation with a character after their saga has ended. The
 * conversation is persisted per character ([EpilogueMessage]) and, every few turns, folded into
 * what that character knows about the player ([com.ilustris.sagai.features.characters.data.model.CharacterKnowledge]),
 * which rides along on every turn so the character keeps knowing the player after old turns fall
 * out of the prompt. It never touches the saga's own messages: nothing here advances the story.
 *
 * Generation is streamed like [com.ilustris.sagai.features.saga.chat.data.usecase.MessageUseCase.generateMessage]
 * so the player sees reasoning chunks while waiting, and every implementation must catch its own
 * failures and emit [StreamingState.Error] — never let a failed blueprint fetch or generation call
 * propagate as an uncaught exception.
 */
interface EpilogueChatUseCase {
    fun observeConversation(characterId: Int): Flow<List<EpilogueMessage>>

    suspend fun saveMessage(message: EpilogueMessage): EpilogueMessage

    /** Starts over with this character: the turns and their audio go, what they know of the player stays. */
    suspend fun clearConversation(
        sagaId: Int,
        characterId: Int,
    )

    /**
     * The character speaks first: a first meeting when [history] is empty, a reunion that picks
     * things back up otherwise.
     */
    fun openConversation(
        saga: SagaContent,
        character: CharacterContent,
        arcs: List<CharacterArc>,
        history: List<EpilogueMessage>,
    ): Flow<StreamingState<EpilogueReply?>>

    fun reply(
        saga: SagaContent,
        character: CharacterContent,
        arcs: List<CharacterArc>,
        conversationSoFar: List<EpilogueMessage>,
        userMessage: String,
    ): Flow<StreamingState<EpilogueReply?>>

    /** Folds recent turns into the character's knowledge of the player, in the background. */
    fun compactKnowledge(
        saga: SagaContent,
        character: CharacterContent,
        leaving: Boolean = false,
    )
}
