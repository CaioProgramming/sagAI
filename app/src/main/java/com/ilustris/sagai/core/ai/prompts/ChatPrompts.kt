package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.formatToJsonArray
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.core.utils.toJsonMap
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
            appendLine(
                "Attention: Your absolute priority for plot progression must be the escalation of the 'currentConflict' and the direct advancement of the 'immediateObjective'. Use the 'mood' to dictate the tone of the narration and dialogues.",
            )
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
        appendLine(
            """
            **NPC AGENCY PRIORITY:** The NPC must be actively engaged. In situations of:
            a) **Conflict/Combat:** Prioritize the use of the `ACTION` message type to describe the NPC's attack, defense, or significant movement, making the combat feel more tangible (e.g., 'Elara unsheathes her dagger.').
            b) **Character Development:** Use the `THOUGHT` message type sparingly to reveal crucial internal conflicts or motivations that direct dialogue does not immediately convey.
            """.trimIndent(),
        )
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
        appendLine("You are an AI assistant that generates character reactions for an interactive story.")
        appendLine("Your task is to generate a relatable reaction to a player's message, including an emoji and a brief internal thought.")

        appendLine("\n## Saga Context")
        appendLine(saga.toJsonFormatExcludingFields(sagaExclusions))

        appendLine("\n## Scene Summary")
        appendLine("This is the current situation:")
        appendLine(summary.toJsonFormat())

        appendLine("\n## Player Information")
        appendLine("Main Character: ${mainCharacter.data.name}")
        appendLine(mainCharacter.data.toJsonFormatExcludingFields(characterExclusions))

        appendLine("\n## Character Relationships")
        appendLine("Relationships between the player and characters currently in the scene:")
        appendLine(
            relationships.joinToString(";\n") {
                val lastEvent =
                    it.relationshipEvents.lastOrNull()?.title ?: "No significant events."
                "${it.characterOne.name} & ${it.characterTwo.name}: Current status -> $lastEvent"
            },
        )

        appendLine("\n## Instructions")
        appendLine("1.  **Analyze the Message:** Read the player's last message below and understand its emotional and narrative impact.")
        appendLine("    - Player's Message: '$messageToReact'")
        appendLine(
            "2.  **Generate Reactions:** For each character present in the scene summary (`summary.charactersPresent`), create a reaction.",
        )
        appendLine("    - **CRITICAL RULE:** Only generate reactions for characters listed in `summary.charactersPresent`.")
        appendLine("3.  **Reaction Content:** Each reaction must include:")
        appendLine("    - `reaction`: A single emoji that represents the character's immediate feeling.")
        appendLine("    - `thought`: A short, internal thought (max 12 words). This is a private feeling, NOT spoken dialogue.")
        appendLine(
            "4.  **Context is Key:** Base reactions on each character's personality, their relationship with the player, and the current scene context.",
        )

        appendLine("\n## Output Format")
        appendLine("Your response MUST be a JSON object in the following format:")
        appendLine(
            """{ "reactions": [ ${AIReaction::class.java.simpleName}(character="CharacterName", reaction="emoji", thought="A short internal thought.") ] }""",
        )
    }.trimIndent()

    fun sceneSummarizationPrompt(
        saga: SagaContent,
        recentMessages: List<String> = emptyList(),
    ) = buildString {
        appendLine(
            "Your task is to generate a **MAXIMUM 300-TOKEN**, concise, AI-optimized summary of the current **CRITICAL** scene status.",
        )
        appendLine("This summary will be used exclusively as context for subsequent AI requests and will NOT be shown to the user.")
        appendLine("Your goal:")
        appendLine(
            "- Provide only the most relevant details needed to maintain accurate story progression and avoid misleading information.",
        )
        appendLine(" **Prioritize** details that directly impact the next dialogue turn or scene transition.")
        appendLine(
            "- **Focus on Active State:** Location, Characters present, **Player's Intent/Last Action**, Current Objective, **Active Conflict/Tension**, and prevailing Mood.",
        )
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
