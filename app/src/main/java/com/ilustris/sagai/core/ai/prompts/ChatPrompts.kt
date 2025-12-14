package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
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
            "playTimeMs",
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
            "playTimeMs",
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
            "smartZoom",
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
        saga: SagaContent,
        messageToReact: Message,
        relationships: List<RelationshipContent>,
    ) = buildString {
        appendLine("You are an AI assistant that generates character reactions for an interactive story.")
        appendLine("Your task is to generate a relatable reaction to a player's message, including an emoji and a brief internal thought.")

        appendLine(SagaPrompts.mainContext(saga))

        appendLine("## Scene Summary")
        appendLine("This is the current situation:")
        appendLine(summary.toAINormalize())

        appendLine("Relationships between the player and characters currently in the scene:")

        appendLine(
            relationships.joinToString(";\n") {
                it.summarizeRelation()
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
        appendLine(
            "You are tasked with generating a factual, concise summary of the current scene based ONLY on established story context.",
        )
        appendLine("This summary will be used as context for subsequent AI requests and will NOT be shown to the user.")

        appendLine("## CRITICAL RULES - NEVER FABRICATE:")
        appendLine("- ONLY reference characters that have been explicitly mentioned in the provided context")
        appendLine(
            "- ONLY describe events that actually happened according to recent messages, character events, and chapter/act summaries",
        )
        appendLine("- ONLY mention locations, objects, or situations that are explicitly stated in the context")
        appendLine("- If information is not available in the context, omit that field completely")
        appendLine("- Do NOT invent dialogue, actions, or characters that aren't in the provided data")
        appendLine("- Do NOT assume or extrapolate beyond what's directly stated")

        appendLine("## YOUR GOAL:")
        appendLine("Create a factual summary focusing on:")
        appendLine("- Current location (if explicitly mentioned)")
        appendLine("- Characters actually present (based on recent messages)")
        appendLine("- Immediate situation (based on recent events and messages)")
        appendLine("- Current mood/atmosphere (derived from established context)")
        appendLine("- What just happened (from recent messages and character events)")

        appendLine("## ESTABLISHED STORY CONTEXT:")
        appendLine(SagaPrompts.mainContext(saga))

        // Current Chapter and Act Context
        saga.currentActInfo?.currentChapterInfo?.data?.let {
            appendLine("### Current Chapter Context:")
            appendLine(it.toAINormalize(ChapterPrompts.CHAPTER_EXCLUSIONS))
        }

        saga.currentActInfo?.data?.let {
            appendLine("### Current Act Context:")
            appendLine("Title: ${it.title}")
            appendLine("Description: ${it.content}")
            appendLine("Introduction: ${it.introduction}")
        }

        // Recent Timeline Events (what actually happened)

        appendLine(TimelinePrompts.timeLineDetails(saga.currentActInfo?.currentChapterInfo))
        appendLine(ChapterPrompts.chapterSummary(saga))
        appendLine(ActPrompts.actsOverview(saga))

        // Character Events (established character developments)
        saga.mainCharacter?.let { mainChar ->
            val recentEvents = mainChar.events.takeLast(3).map { it.event }
            if (recentEvents.isNotEmpty()) {
                appendLine("### Main Character Recent Events:")
                appendLine(
                    recentEvents.normalizetoAIItems(
                        listOf("id", "characterId", "createdAt", "gameTimelineId"),
                    ),
                )
            }
        }

        // Established Characters (only those that exist)
        val establishedCharacters =
            saga.getCharacters().filter { it.id != saga.mainCharacter?.data?.id }
        if (establishedCharacters.isNotEmpty()) {
            appendLine("### Established Characters in Saga:")
            appendLine(establishedCharacters.normalizetoAIItems(characterExclusions))
        }

        // Character Relationships (established connections)
        if (!saga.mainCharacter?.relationships.isNullOrEmpty()) {
            appendLine("### Established Character Relationships:")
            appendLine(
                saga.mainCharacter.relationships.joinToString(";\n") {
                    val lastEvent = it.relationshipEvents.lastOrNull()?.title ?: "No events yet"
                    "${it.characterOne.name} ${it.data.emoji} ${it.characterTwo.name}: $lastEvent"
                },
            )
        }

        // Recent Messages (what actually happened in conversation)
        appendLine("### Recent Conversation (What Actually Happened):")
        appendLine("These messages show the immediate context and current situation:")
        appendLine(recentMessages.normalizetoAIItems(messageExclusions))

        appendLine("## SUMMARY REQUIREMENTS:")
        appendLine("- Base your summary ONLY on the provided context above")
        appendLine("- Focus on factual information from recent messages and established events")
        appendLine("- Identify who is present based on recent message speakers")
        appendLine("- Describe the current situation based on what was actually said/done")
        appendLine("- If any information is unclear or missing, omit that aspect entirely")
        appendLine("- Create a concise but comprehensive picture of the current scene state")
    }.trimIndent()

    fun scheduledNotificationPrompt(
        saga: SagaContent,
        selectedCharacter: CharacterContent,
        sceneSummary: SceneSummary,
    ) = buildString {
        append(SagaPrompts.mainContext(saga))
        appendLine("Current Story Context:")
        appendLine(
            sceneSummary.toAINormalize(),
        )
        appendLine("Character Context:")
        append(selectedCharacter.data.toAINormalize(characterExclusions))
        appendLine()

        val relationWithCharacter = selectedCharacter.findRelationship(saga.mainCharacter!!.data.id)

        relationWithCharacter?.let {
            appendLine("### Character Relationship story with Player:")
            appendLine(it.summarizeRelation())
        }

        appendLine(
            conversationHistory(
                saga.flatMessages().map { it.message }.takeLast(UpdateRules.LORE_UPDATE_LIMIT),
            ),
        )
        appendLine()
        appendLine(
            "Task: Generate a brief, authentic message (1-2 sentences) as ${selectedCharacter.data.name} reaching out to the player who just left.",
        )

        if (selectedCharacter.data.id == saga.mainCharacter.data.id) {
            appendLine(
                "IMPORTANT: This is the MAIN CHARACTER. The message must be an INNER THOUGHT or REFLECTION about the current situation.",
            )
            appendLine("Do NOT address another person. Talk to yourself.")
        } else {
            appendLine(
                "IMPORTANT: This is an NPC. The message must be spoken DIRECTLY to the main character (${saga.mainCharacter.data.name}).",
            )
        }

        appendLine("CRITICAL STYLE INSTRUCTION: The message must NOT be a simple conversation starter or generic greeting.")
        appendLine(
            "It must feel like an IMMEDIATE INVITATION or URGENT CALL to return to the action.",
        )
        appendLine(
            "Examples of desired tone: 'Oh no Teresa caught us, what we gonna do next?', 'Damn we need to keep finding the sheriff things are getting risky here', 'I have a bad feeling about this... we should move.'",
        )

        appendLine("- Follow your established personality and voice")
        appendLine("- Consider your relationship history and emotional connection with the player")
        appendLine("- Reference current story elements and shared experiences naturally")
        append(GenrePrompts.conversationDirective(saga.data.genre))
        appendLine("Your message as ${selectedCharacter.data.name}:")
    }

    private fun conversationStyleAndPacing() =
        """
        ---
        ## RESPONSE STYLE & PACING
        1.  **Pacing & Detail:** Be clear and concise. Prioritize details that advance the plot or enhance immersion. Use varied sentence structure and show, don't tell. Avoid redundancy.
        2.  **Dialogue (NPCs):** Dialogue must be purposeful, concise, and relevant. Even enigmatic dialogue should offer a path forward. Ensure natural flow and varied tone.
        3.  **Readability:** Use paragraph breaks for clarity. Prioritize clear communication over obscurity.
        ---
        """.trimIndent()

    fun conversationHistory(lastMessages: List<Message>) =
        buildString {
            appendLine("Conversation History")
            appendLine("Use this history for context, but do NOT repeat it in your response.")
            appendLine("The messages are ordered from newest to oldest")
            appendLine("Consider the newest ones to move history forward")
            appendLine("Pay attention to `speakerName` and `senderType`.")
            appendLine(
                lastMessages.reversed().normalizetoAIItems(excludingFields = messageExclusions),
            )
        }
}
