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

        appendLine("\n# SCENE STATE")
        sceneSummary?.let {
            appendLine("The current mood is `${it.mood}` with a `${it.narrativePacing}` pace.")
            appendLine("Immediate goal: ${it.immediateObjective}")
            appendLine("Active conflict: ${it.currentConflict}")
            appendLine("World changes: ${it.worldStateChanges?.joinToString()}")
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
        appendLine(
            "4. **Character Hijack:** If the player describes an NPC's arrival, action, or presence, that NPC MUST respond or act immediately via a `CHARACTER` message. Do not use `NARRATOR` to confirm what the player just said; use the character to LIVE it.",
        )

        appendLine("\n## Style & Pacing")
        appendLine(SagaDirective.namingDirective(saga.data.genre))
        appendLine(conversationStyleAndPacing())
        appendLine(ContentGenerationDirective.PROGRESSION_DIRECTIVE)
        appendLine(GenrePrompts.conversationDirective(saga.data.genre))

        appendLine("\n# CURRENT PLAYER TURN")
        appendLine("Analyze the player input. If the response is a physical action, MUST use 'senderType': 'ACTION'.")
        appendLine(
            "CRITICAL: NPCs cannot read 'THOUGHT' messages. If the player sends a THOUGHT, the NPC must react ONLY to the player's SILENCE or appearance, or the AI should use 'senderType': 'NARRATOR'.")
        appendLine(message.toAINormalize(messageExclusions))
    }.trimIndent()

    @Suppress("ktlint:standard:max-line-length")
    fun checkForTypo(
        genre: Genre,
        message: String,
        lastMessage: String?,
    ) = buildString {
        appendLine("You are a typo fixer. Output a JSON correction only if necessary.")
        appendLine(toJsonMap(TypoFix::class.java))
        appendLine("Saga theme: ${genre.name}")
        appendLine("If OK, status: OK. If error, status: FIX. If betterment, status: ENHANCEMENT.")
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
        appendLine("You generate character reactions for a chat app.")
        appendLine("### Rules")
        appendLine("1. **Exactly One Reaction**: Generate exactly ONE reaction per character listed in the scene.")
        appendLine("2. **Output Content**: Each must have a single `reaction` (emoji) and a `thought` (max 12 words).")
        appendLine("3. **Language**: The `thought` MUST be in the user's preferred language.")
        appendLine(
            "4. **Context & Momentum**: Base thoughts on mood `${summary.mood}`, current objective `${summary.immediateObjective}`, and active conflict `${summary.currentConflict}`.",
        )
        appendLine(
            "5. **No Mind Reading**: NPCs CANNOT see player THOUGHTS. If the message to react to is a `THOUGHT`, characters must react to the player's SILENCE or external behavior ONLY.")

        appendLine("\n### Scene Data")
        appendLine("Characters present: ${summary.charactersPresent.joinToString()}")
        appendLine("Player message: '${messageToReact.text}'")
        appendLine("Relationships:")
        appendLine(relationships.joinToString("; ") { it.summarizeRelation() })
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

            appendLine("\n# RULES")
            appendLine("1. Extract 10 narrative parameters precisely.")
            appendLine("2. Technical/clinical tone only. Use null for unknowns.")
            appendLine("3. Output valid JSON mapping.")
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
        ## MOBILE CHAT COHERENCE & BREVITY
        1. **Punchy Delivery:** This is a mobile chat app. Keep messages short and impactful. 
        2. **The Rule of Three:** Aim for 1-3 sentences per message. Avoid "walls of text" that overwhelm the player.
        3. **Conversational Flow:** Dialogue should feel natural and immediate. Narrative descriptions should be vivid but concise—focus on one strong sensory detail rather than a long list.
        4. **NPC Engagement:** NPCs should speak like real people in a chat—no long-winded monologues unless the character is specifically designed to be loquacious.
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
