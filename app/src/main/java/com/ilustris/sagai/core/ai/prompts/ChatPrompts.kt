package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.formatToJsonArray
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipContent
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.AIReaction
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.saga.chat.data.model.TypoFix

object ChatPrompts {
    val messageExclusions =
        listOf(
            "id",
            "timeStamp",
            "sagaId",
            "characterId",
            "timelineId",
            "status",
        )
    val sagaExclusions =
        listOf(
            "id",
            "icon",
            "review",
            "createdAt",
            "endedAt",
            "mainCharacterId",
            "currentActId",
            "isEnded",
            "isDebug",
            "endMessage",
            "review",
        )

    val characterExclusions =
        listOf(
            "id",
            "image",
            "sagaId",
            "joinedAt",
            "details",
            "emojified",
            "hexColor",
            "firstSceneId",
            "personality",
        )

    @Suppress("ktlint:standard:max-line-length")
    fun replyMessagePrompt(
        saga: SagaContent,
        message: Message,
        lastMessages: List<Message> = emptyList(),
        directive: String,
        sceneSummary: SceneSummary?,
    ) = buildString {
        appendLine(Core.roleDefinition(saga.data))
        appendLine(ChatRules.outputRules(saga.mainCharacter?.data))
        appendLine(ChatRules.TYPES_PRIORITY_CONTENT.trimIndent())

        sceneSummary?.let {
            appendLine("## Progression Context")
            appendLine("Guide your response to align with the story's progression based on this summary:")
            appendLine(sceneSummary.toJsonFormat())
        }

        appendLine("## Saga & Player Context")
        appendLine("SAGA: ${saga.data.toJsonFormatExcludingFields(sagaExclusions)}")
        appendLine(
            "PLAYER: ${
                saga.mainCharacter?.data.toJsonFormatExcludingFields(
                    characterExclusions,
                )
            }",
        )
        appendLine(
            CharacterPrompts.charactersOverview(saga.getCharacters().filter { it.id != saga.mainCharacter?.data?.id }),
        )
        appendLine(CharacterDirective.CHARACTER_INTRODUCTION.trimIndent())

        appendLine(ActPrompts.actDirective(directive))

        appendLine("## NPC Actions & Thoughts")
        appendLine("- NPCs can perform actions (`ACTION`) or have thoughts (`THOUGHT`).")
        appendLine("- **CRITICAL**: For NPC `ACTION` or `THOUGHT`, set `speakerName` to the NPC's name.")
        appendLine(
            "- `ACTION` (NPC): Concise description of a physical action (e.g., 'Elara unsheathes her dagger.'). The `NARRATOR` describes the wider scene.",
        )
        appendLine(
            "- `THOUGHT` (NPC): Use **SPARINGLY** for critical insights or internal conflict (e.g., 'He doesn't suspect a thing...'). Show, don't just tell.",
        )
        appendLine(
            "- You primarily generate `NARRATOR` (storytelling) and `CHARACTER` (dialogue) messages. Do not generate `ACTION` or `THOUGHT` for the player.",
        )

        appendLine(SagaDirective.namingDirective(saga.data.genre))
        appendLine(conversationStyleAndPacing())
        appendLine(ContentGenerationDirective.PROGRESSION_DIRECTIVE)
        appendLine("Use the conversation style to provide a natural dialogue")
        appendLine(GenrePrompts.conversationDirective(saga.data.genre))

        appendLine(conversationHistory(lastMessages))

        appendLine("**LAST TURN'S OUTPUT / CURRENT CONTEXT:**")
        appendLine(message.toJsonFormatExcludingFields(messageExclusions))
    }.trimIndent()

    @Suppress("ktlint:standard:max-line-length")
    fun checkForTypo(
        genre: Genre,
        message: String,
        lastMessage: String?,
    ) = buildString {
        appendLine("You are an assistant who only suggests corrections when truly necessary.")
        appendLine("If you spot an error that affects understanding, suggest a better version in a friendly, casual tone.")
        appendLine("Your response must be a JSON: ")
        appendLine(toJsonMap(TypoFix::class.java))
        appendLine("Saga theme: ${genre.name}")
        appendLine(
            "friendlyMessage should always be short and friendly.",
        )
        appendLine("If there is no error, status should be OK and the other fields null.")
        appendLine("If there is an error, status should be FIX and suggest the corrected text.")
        appendLine("If the message could be improved but is not wrong, status should be ENHANCEMENT and suggest a clearer version.")
        appendLine("Use the conversation style to provide a natural enhancement that fits the story theme.")
        appendLine(GenrePrompts.conversationDirective(genre))
        appendLine("Message:")
        appendLine(">>> $message")
        if (!lastMessage.isNullOrBlank()) {
            appendLine("Previous message for context:")
            appendLine(">>> $lastMessage")
        }
    }.trimIndent()

    fun generateReactionPrompt(
        summary: SceneSummary,
        saga: Saga,
        mainCharacter: CharacterContent,
        messageToReact: String,
        relationships: List<RelationshipContent>,
    ) = buildString {
        appendLine("Your task is to generate relatable reactions to the player message in the saga")
        appendLine("Saga context:")
        appendLine(saga.toJsonFormatExcludingFields(sagaExclusions))
        appendLine("History current context:")
        appendLine(summary.toJsonFormat())
        appendLine("Player context:")
        appendLine(mainCharacter.data.toJsonFormatExcludingFields(characterExclusions))
        appendLine("Player relationships with present characters:")
        appendLine(
            relationships.joinToString(";\n") {
                val lastEvent = it.relationshipEvents.lastOrNull()?.title ?: "Nothing related"
                "${it.characterOne.name} & ${it.characterTwo.name}: $lastEvent"
            },
        )

        appendLine("Generate reactions only to characters present in the scene summary.")
        appendLine("React properly to the message and the scene summary.")
        appendLine("Your reaction must be only a single emoji, no text nor descriptions.")
        appendLine("The last message in the conversation was:")
        appendLine("'$messageToReact'")
        appendLine("Base your reactions on characters personality and relationship with the player.")
        appendLine("Your output needs to be: ")
        appendLine("{ reactions: [ ${toJsonMap(AIReaction::class.java)} ]  }")
    }.trimIndent()

    fun sceneSummarizationPrompt(
        saga: SagaContent,
        recentMessages: List<String> = emptyList(),
    ) = buildString {
        appendLine("You task is generate a concise, AI-optimized summary of the current scene in an interactive story.")
        appendLine("This summary will be used exclusively as context for subsequent AI requests and will NOT be shown to the user.")
        appendLine("Your goal:")
        appendLine(
            "- Provide only the most relevant details needed to maintain accurate story progression and avoid misleading information.",
        )
        appendLine("- Avoid redundant or already established information.")
        appendLine("- Focus on immediate context: location, characters present, current objective, active conflict, and mood.")
        appendLine("- If any field is not relevant or unknown, omit it.")
        appendLine()
        appendLine("Saga Context:")
        appendLine("Title: ${saga.data.title}")
        appendLine("Description: ${saga.data.description}")
        appendLine("Genre: ${saga.data.genre}")
        appendLine("PLAYER CONTEXT DATA:")
        appendLine(saga.mainCharacter?.data.toJsonFormatExcludingFields(characterExclusions))
        appendLine("Player relationships:")
        if (saga.mainCharacter?.relationships.isNullOrEmpty()) {
            appendLine("No relationships yet.")
        } else {
            appendLine(
                saga.mainCharacter.relationships.joinToString(";\n") {
                    val lastEvent = it.relationshipEvents.lastOrNull()?.title ?: "Nothing related"
                    "${it.characterOne.name} ${it.data.emoji} ${it.characterTwo.name}: $lastEvent"
                },
            )
        }

        appendLine("Player last events:")
        if (saga.mainCharacter?.events.isNullOrEmpty()) {
            appendLine("No events yet.")
        } else {
            appendLine(
                saga.mainCharacter.events.map { it.event }.takeLast(5).formatToJsonArray(
                    listOf("gameTimelineId", "characterId", "id", "createdAt"),
                ),
            )
        }
        appendLine("Current Saga Characters:")
        val characters = saga.getCharacters().filter { it.id != saga.mainCharacter?.data?.id }
        if (characters.isEmpty()) {
            appendLine("No other characters yet.")
        } else {
            appendLine(
                characters.joinToString(";\n") {
                    it.toJsonFormatExcludingFields(characterExclusions)
                },
            )
        }
        appendLine("Current Chapter Context:")
        appendLine("Introduction: ")
        appendLine(
            saga.currentActInfo
                ?.currentChapterInfo
                ?.data
                ?.introduction ?: "No introduction available.",
        )
        appendLine(TimelinePrompts.timeLineDetails(saga.currentActInfo?.currentChapterInfo))
        appendLine("Recent Chapter Summaries:")
        appendLine(ChapterPrompts.chapterSummary(saga))
        appendLine()
        appendLine("Recent Acts Overview:")
        appendLine(ActPrompts.actsOverview(saga))
        appendLine()
        appendLine("Recent Messages (for context, do NOT repeat):")
        appendLine("[")
        appendLine(recentMessages.joinToString(separator = ";\n"))
        appendLine("]")
        appendLine()
    }.trimIndent()

    private fun conversationStyleAndPacing() =
        """
        ---
        ## RESPONSE STYLE & PACING
        1.  **Pacing & Detail:** Be clear and concise. Prioritize details that advance the plot or enhance immersion. Use varied sentence structure and show, don't tell. Avoid redundancy.
        2.  **Dialogue (NPCs):** Dialogue must be purposeful, concise, and relevant. Even enigmatic dialogue should offer a path forward. Ensure natural flow and varied tone.
        3.  **Readability:** Use paragraph breaks for clarity. Prioritize clear communication over obscurity.
        ---
        """.trimIndent()

    private fun conversationHistory(lastMessages: List<Message>) =
        buildString {
            appendLine("Conversation History")
            appendLine("Use this history for context, but do NOT repeat it in your response.")
            appendLine("Pay attention to `speakerName` and `senderType`.")
            appendLine(lastMessages.formatToJsonArray(excludingFields = messageExclusions))
        }
}
