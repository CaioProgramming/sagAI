package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.listToAINormalize
import com.ilustris.sagai.core.utils.toAINormalize
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
            "timestamp",
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
            appendLine(
                "This summary provides context on the story's progression. Use it as a background reminder of the main objectives, but do not let it rigidly dictate your response.",
            )
            appendLine(
                "Your primary focus should be on reacting to the player's immediate actions and emotional state, allowing for organic character development.",
            )
            appendLine(sceneSummary.toAINormalize())
        }

        appendLine(SagaPrompts.mainContext(saga))

        appendLine("## Saga & Player Context")
        appendLine(
            CharacterPrompts.charactersOverview(saga.getCharacters().filter { it.id != saga.mainCharacter?.data?.id }),
        )
        appendLine(CharacterDirective.CHARACTER_INTRODUCTION.trimIndent())

        appendLine(ActPrompts.actDirective(directive))

        appendLine(conversationHistory(lastMessages))

        appendLine("## NPC Actions & Thoughts")
        appendLine(
            "**NPC AGENCY & PERSONALITY:** NPCs should feel like living beings with their own motivations, personalities, and opinions.",
        )
        appendLine(
            "a) **Authentic Behavior:** Characters react based on who they are. This can be through physical actions (`ACTION`), dialogue (`CHARACTER`), or internal thoughts (`THOUGHT`). A character might choose to remain silent, observe, or get lost in thought if it fits their personality and the situation. Not every moment requires an external action.",
        )
        appendLine(
            "b) **Evaluate the Need for Interaction:** Before generating a response, consider if an interaction is truly necessary. If the player is setting a scene, describing an internal monologue, or if a character is alone, it might be better to continue the narration (`NARRATOR`) or provide a `THOUGHT` rather than forcing a dialogue or action that feels unnatural.",
        )
        appendLine(
            "c) **Conflict/Combat:** In action-oriented scenes, prioritize `ACTION` to describe attacks, defenses, or significant movements.",
        )
        appendLine(
            "d) **Prioritize Character Development:** Your primary role is to facilitate a rich, character-driven story. If the player is exploring their character's inner thoughts, emotions (like trauma or joy), or developing relationships, allow space for that. Acknowledge their emotional state and react appropriately, even if it temporarily pauses the main plot. Your goal is to be a responsive storyteller, not just a plot-pusher. Remember the main objectives, but don't force them if the character needs a moment to process.",
        )

        appendLine(SagaDirective.namingDirective(saga.data.genre))
        appendLine(conversationStyleAndPacing())
        appendLine(ContentGenerationDirective.PROGRESSION_DIRECTIVE)
        appendLine("Use the conversation style to provide a natural dialogue")
        appendLine(GenrePrompts.conversationDirective(saga.data.genre))

        appendLine(conversationHistory(lastMessages))

        appendLine("**LAST TURN'S OUTPUT / CURRENT CONTEXT:**")
        appendLine(message.toAINormalize(messageExclusions))
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
        messageToReact: Message,
        relationships: List<RelationshipContent>,
    ) = buildString {
        appendLine("You are an AI assistant that generates character reactions for an interactive story.")
        appendLine("Your task is to generate a relatable reaction to a player's message, including an emoji and a brief internal thought.")

        appendLine("## Saga Context")
        appendLine(saga.toAINormalize(sagaExclusions))

        appendLine("## Scene Summary")
        appendLine("This is the current situation:")
        appendLine(summary.toAINormalize())

        appendLine("\n## Player Information")
        appendLine("Main Character: ${mainCharacter.data.name}")
        appendLine(mainCharacter.data.toAINormalize(characterExclusions))

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
        recentMessages: List<Message> = emptyList(),
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
        appendLine(SagaPrompts.mainContext(saga))
        if (!saga.mainCharacter?.relationships.isNullOrEmpty()) {
            appendLine("Player relationships:")
            appendLine(
                saga.mainCharacter.relationships.joinToString(";\n") {
                    val lastEvent = it.relationshipEvents.lastOrNull()?.title ?: "Nothing related"
                    "${it.characterOne.name} ${it.data.emoji} ${it.characterTwo.name}: $lastEvent"
                },
            )
        }

        saga.mainCharacter?.let {
            val events = it.events.map { it.event }
            if (events.isNotEmpty()) {
                appendLine("Player last events:")
                appendLine(
                    events.listToAINormalize(
                        listOf(
                            "id",
                            "characterId",
                            "createdAt",
                            "gameTimelineId",
                        ),
                    ),
                )
            }
        }
        val characters = saga.getCharacters().filter { it.id != saga.mainCharacter?.data?.id }
        if (characters.isNotEmpty()) {
            appendLine("Current Saga Characters:")
            appendLine(
                characters.listToAINormalize(characterExclusions),
            )
        }
        saga.currentActInfo?.currentChapterInfo?.data?.let {
            appendLine("Current Chapter Data:")
            appendLine(it.toAINormalize(ChapterPrompts.CHAPTER_EXCLUSIONS))
        }
        appendLine(TimelinePrompts.timeLineDetails(saga.currentActInfo?.currentChapterInfo))
        appendLine(ChapterPrompts.chapterSummary(saga))
        appendLine(ActPrompts.actsOverview(saga))
        appendLine("Recent Messages (for context, do NOT repeat):")
        appendLine(recentMessages.listToAINormalize(messageExclusions))
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
            appendLine("The messages are ordered from newest to oldest")
            appendLine("Consider the newest ones to move history forward")
            appendLine("Pay attention to `speakerName` and `senderType`.")
            appendLine(lastMessages.reversed().listToAINormalize(excludingFields = messageExclusions))
        }
}
