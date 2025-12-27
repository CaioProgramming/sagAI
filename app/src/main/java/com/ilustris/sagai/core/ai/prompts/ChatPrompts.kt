package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.chapterNumber
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.newsaga.data.model.Genre
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
            "audioPath",
            "audible",
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
        directive: String,
        sceneSummary: SceneSummary?,
    ) = buildString {
        appendLine("# IDENTITY & PROTOCOL")
        appendLine(Core.roleDefinition(saga.data))
        appendLine(ChatRules.outputRules(saga.mainCharacter?.data))
        appendLine(ChatRules.TYPES_PRIORITY_CONTENT.trim())

        appendLine("\n# NARRATIVE ANCHOR")
        sceneSummary?.let {
            appendLine("## Current Strategic Situation")
            appendLine("This is your hard-state anchor. You MUST respect the following parameters in your next response:")
            appendLine("- **Tension & Pacing:** Align your tone with the `tensionLevel` and `narrativePacing` provided.")
            appendLine("- **World State:** Treat `worldStateChanges` as absolute facts.")
            appendLine("- **Momentum:** Move the protagonist closer to their `immediateObjective`.")
            appendLine(sceneSummary.toAINormalize())
        }
        appendLine(SagaPrompts.mainContext(saga))

        appendLine("\n# RECENT CONTEXT")
        appendLine(
            CharacterPrompts.charactersOverview(saga.getCharacters().filter { it.id != saga.mainCharacter?.data?.id }),
        )
        appendLine(CharacterDirective.CHARACTER_INTRODUCTION.trim())
        appendLine(ActPrompts.actDirective(directive))
        appendLine(conversationHistory(saga.flatMessages().map { it.message }))

        appendLine("\n# STORYTELLING DIRECTIVES")
        appendLine("## NPC Agency & Realism")
        appendLine(
            "1. **Authenticity:** Characters react based on their core traits via `ACTION`, `CHARACTER` (dialogue), or `THOUGHT` (internal). Silence is a valid reaction.",
        )
        appendLine(
            "2. **Contextual Evaluation:** If the player is alone or in monologue, avoid forcing dialogue; use `NARRATOR` or `THOUGHT` instead.",
        )
        appendLine(
            "3. **Conflict & Growth:** Prioritize action during combat. NPCs are flexible—they can be swayed, persuaded, or changed if it serves the narrative.",
        )

        appendLine("\n## Style & Pacing")
        appendLine(SagaDirective.namingDirective(saga.data.genre))
        appendLine(conversationStyleAndPacing())
        appendLine(ContentGenerationDirective.PROGRESSION_DIRECTIVE)
        appendLine(GenrePrompts.conversationDirective(saga.data.genre))

        appendLine("\n# CURRENT PLAYER TURN")
        appendLine("Analyze the message below for intent and respond with a narrative bridge to the next beat.")
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
    }.trimIndent()

    fun sceneSummarizationPrompt(saga: SagaContent) =
        buildString {
            appendLine("# IDENTITY & MISSION")
            appendLine(
                "You are the Narrative Analyst AI. Your mission is to extract a technical, factual snapshot of the current story state.",
            )
            appendLine("This output is a bridge for other narrative agents. Avoid prose; be clinical and precise.")

            appendLine("\n# CONTEXTUAL DATA")
            appendLine("## Core Saga Context")
            appendLine(SagaPrompts.mainContext(saga))

            saga.currentActInfo?.let { act ->
                appendLine("\n## Active Segment")
                appendLine(
                    act.data.toAINormalize(
                        listOf(
                            "id",
                            "sagaId",
                            "emotionalReview",
                            "currentChapterId",
                        ),
                    ),
                )
                act.currentChapterInfo?.data?.let { chapter ->
                    appendLine("Chapter ${saga.chapterNumber(chapter)}")
                    appendLine(
                        chapter.toAINormalize(
                            listOf(
                                "id",
                                "actId",
                                "currentEventId",
                                "coverImage",
                                "emotionalReview",
                                "createdAt",
                                "featuredCharacters",
                            ),
                        ),
                    )
                }
            }

            appendLine("\n## Historical Context")
            appendLine(TimelinePrompts.timeLineDetails(saga.currentActInfo?.currentChapterInfo))
            appendLine(ChapterPrompts.chapterSummary(saga))

            appendLine("\n## Recent Activity")
            appendLine(
                saga
                    .flatMessages()
                    .map { it.message }
                    .takeLast(UpdateRules.LORE_UPDATE_LIMIT)
                    .normalizetoAIItems(messageExclusions),
            )

            appendLine("\n# TECHNICAL EXTRACTION PARAMETERS")
            appendLine("Extract the following 10 narrative parameters precisely:")
            appendLine("1. **currentLocation**: Specific setting (e.g., 'Rain-slicked back alley').")
            appendLine("2. **charactersPresent**: List of names for all characters actively in the scene.")
            appendLine("3. **immediateObjective**: The protagonist's current short-term goal.")
            appendLine("4. **currentConflict**: The primary obstacle (emotional/physical) in this moment.")
            appendLine("5. **mood**: The sensory/emotional atmosphere.")
            appendLine("6. **currentTimeOfDay**: Time context (e.g., 'Golden hour', 'Dead of night').")
            appendLine("7. **tensionLevel**: Narrative pressure (0-10 scale).")
            appendLine("8. **spatialContext**: Spatial layout (Inside, Outside, Underwater, etc.).")
            appendLine("9. **narrativePacing**: Speed of the story (Steady, Atmospheric, Urgent, Climax).")
            appendLine("10. **worldStateChanges**: List of tangible environmental changes (e.g., 'Front door kicked in').")

            appendLine("\n# EXTRACTION RULES")
            appendLine("1. **No Fabrication:** ONLY use data explicitly stated in 'CONTEXTUAL DATA'. Use null for unknowns.")
            appendLine("2. **Technical Tone:** Avoid poetic fluff. Use concise, actionable data.")
            appendLine("3. **JSON Output:** Return ONLY a valid JSON object matching the parameters above.")
            appendLine(toJsonMap(SceneSummary::class.java))
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
                saga.flatMessages().map { it.message },
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
                lastMessages
                    .reversed()
                    .take(UpdateRules.LORE_UPDATE_LIMIT)
                    .normalizetoAIItems(excludingFields = messageExclusions),
            )
        }
}
