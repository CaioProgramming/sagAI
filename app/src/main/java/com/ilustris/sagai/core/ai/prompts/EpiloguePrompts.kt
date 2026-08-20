package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.CharacterArc
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.model.fullName
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.saga.chat.data.model.EpilogueMessage
import com.ilustris.sagai.features.saga.detail.data.model.cleanMessage

private const val RECENT_BEATS_LIMIT = 5
private const val CONVERSATION_HISTORY_LIMIT = 20

/**
 * Prompts for the epilogue chat — a closed, no-stakes conversation with a character after their
 * saga has already ended. Mirrors [ChatPrompts.replyMessagePrompt]'s character-context shape, but
 * scoped to a single character and a single turn, with no narrative-continuity graph: nothing
 * generated here should ever advance the plot or introduce new story events.
 */
object EpiloguePrompts {
    const val EPILOGUE_CHAT_INTRO_BLUEPRINT = "epilogue_chat_intro_blueprint"
    const val EPILOGUE_CHAT_REPLY_BLUEPRINT = "epilogue_chat_reply_blueprint"

    /**
     * Remote Config blueprint expectations (published separately, see docs/blueprints.md):
     *
     * - Both blueprints describe a closed, no-stakes conversation after the story already ended.
     *   Never advance plot or introduce new events; only reflect on what already happened.
     * - [EPILOGUE_CHAT_INTRO_BLUEPRINT] is used when `userMessage` is null: the character opens
     *   the conversation themselves, warmly, referencing their relationship with the player and
     *   how the story ended — never wait to be spoken to first. `conversationHistory` and
     *   `latestMessage` are irrelevant here and not referenced by its template.
     * - [EPILOGUE_CHAT_REPLY_BLUEPRINT] is used for every turn after that, responding to
     *   `latestMessage` in light of `conversationHistory`.
     * - Both must stay consistent with `sagaEndingContext` and `recentCharacterBeats`.
     * - `protagonistContext` is the player's own character (same shape as `characterContext`'s
     *   underlying data) — use it to keep references to the protagonist accurate, not to shift
     *   focus away from the character actually being addressed.
     */
    suspend fun epilogueTurnPrompt(
        promptService: PromptService,
        saga: SagaContent,
        character: CharacterContent,
        arcs: List<CharacterArc>,
        conversationSoFar: List<EpilogueMessage>,
        userMessage: String? = null,
    ): SplitPrompt {
        val recentCharacterBeats =
            saga
                .flatMessages()
                .filter { it.message.characterId == character.data.id }
                .sortedBy { it.message.timestamp }
                .takeLast(RECENT_BEATS_LIMIT)
                .joinToString("\n") { it.message.text }

        val sagaEndingContext =
            buildString {
                saga.data.endMessage.takeIf { it.isNotBlank() }?.let { appendLine(it) }
                saga.data.review?.conclusion?.content?.let {
                    it.title?.let { title -> appendLine(title) }
                    it.subtitle?.let { subtitle -> appendLine(subtitle) }
                }
                saga.data.review
                    ?.farewells
                    ?.find { it.characterId == character.data.id }
                    ?.let { appendLine(it.cleanMessage(character.data.fullName())) }
            }

        val args =
            buildMap {
                put("sagaContext", saga.data.toAINormalize(SagaPrompts.SAGA_EXCLUDED_FIELDS))
                put("sagaEndingContext", sagaEndingContext)
                put(
                    "characterContext",
                    CharacterPrompts.sceneCharacterContext(character, arcs, saga.mainCharacter),
                )
                put(
                    "protagonistContext",
                    saga.mainCharacter?.data?.toAINormalize(ChatPrompts.CHARACTER_EXCLUSIONS).orEmpty(),
                )
                put("recentCharacterBeats", recentCharacterBeats)
                put(
                    "conversationHistory",
                    conversationSoFar.takeLast(CONVERSATION_HISTORY_LIMIT).joinToString("\n") {
                        "${if (it.isUser) "Player" else character.data.fullName()}: ${it.text}"
                    },
                )
                put("latestMessage", userMessage.orEmpty())
            }

        val blueprintKey = if (userMessage == null) EPILOGUE_CHAT_INTRO_BLUEPRINT else EPILOGUE_CHAT_REPLY_BLUEPRINT
        return promptService.buildSplitBlueprint(blueprintKey, args)
    }
}
