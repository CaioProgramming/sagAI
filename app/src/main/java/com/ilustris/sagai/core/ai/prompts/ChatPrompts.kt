package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.chapterNumber
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.generateCharacterRelationsSummary
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.AIReply
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
            "events",
            "relationships",
        )

    @Suppress("ktlint:standard:max-line-length")
    fun replyMessagePrompt(
        saga: SagaContent,
        message: Message,
        directive: String,
        sceneSummary: SceneSummary?,
    ) = buildString {
        val charactersInScene =
            sceneSummary?.charactersPresent?.mapNotNull {
                saga.findCharacter(it)?.data
            }
        appendLine("# IDENTITY & PROTOCOL")
        appendLine(Core.roleDefinition(saga.data))
        appendLine(ChatRules.outputRules(saga.mainCharacter?.data))
        appendLine(ChatRules.TYPES_PRIORITY_CONTENT.trim())
        appendLine(OutputRules.outputRule(toJsonMap(AIReply::class.java)))

        appendLine("# SCENE STATE")
        appendLine(sceneSummary.toAINormalize())

        appendLine(SagaPrompts.mainContext(saga))

        appendLine("\n# FULL SAGA CAST SUMMARY")
        appendLine("// Use this list to check if a mentioned entity is an established character or a new one.")
        appendLine(saga.getCharacters().normalizetoAIItems(characterExclusions))

        appendLine("\n# RECENT CONTEXT")

        charactersInScene?.let {
            appendLine("## Characters in Immediate Scene:")
            appendLine(CharacterPrompts.charactersOverview(it))
        }

        appendLine("\n# ACTIVE RELATIONSHIPS")
        appendLine(saga.generateCharacterRelationsSummary(sceneSummary?.charactersPresent))

        appendLine(CharacterDirective.CHARACTER_INTRODUCTION.trim())
        appendLine(ActPrompts.actDirective(directive))
        appendLine(conversationHistory(saga))

        appendLine("\n# STORYTELLING DIRECTIVES")
        appendLine(StorytellingDirective.NPC_AGENCY_AND_REALISM)
        appendLine(StorytellingDirective.MOBILE_CHAT_COHERENCE)
        appendLine(ContentGenerationDirective.PROGRESSION_DIRECTIVE)

        appendLine("\n# CURRENT PLAYER TURN")
        appendLine("### Character Resolution Hierarchy:")
        appendLine("1. **LOCAL:** If the player addresses someone in `charactersPresent` (e.g., 'Anya'), THEY MUST respond.")
        appendLine(
            "2. **GLOBAL:** If the player addresses a character NOT in the room (e.g., calling 'Rafaela' via radio), check the `# FULL SAGA CAST SUMMARY`. If she exists, utilize her personality/knowledge.",
        )
        appendLine(
            "3. **DISCOVERY:** If no existing character matches the context, return a NEW and creative `speakerName`. The system will automatically create them based on the dialogue you provide.",
        )
        appendLine("4. **CONTINUITY:** Identify the `speakerName` in the [LATEST MESSAGE]. YOU MUST NOT respond as that same character.")
        appendLine("5. **PRIVACY:** NPCs cannot read 'THOUGHT' messages; they interpret only visible actions.")
        appendLine("\n[LATEST MESSAGE]")
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
            "5. **No Mind Reading**: NPCs CANNOT see player THOUGHTS. If the message to react to is a `THOUGHT`, characters must react to the player's SILENCE or external behavior ONLY.",
        )

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
            appendLine(
                "Extract the following 10 narrative parameters precisely. This summary is the DEFINITIVE TRUTH for the next story turn.",
            )
            appendLine(
                "1. **currentLocation**: The exact, current physical setting. If the latest messages indicate movement (e.g., 'ran to the roof', 'entered the sewers'), this MUST reflect the new location immediately.",
            )
            appendLine(
                "2. **charactersPresent**: A list of names for characters PHYSICALLY in the same room/immediate area as the protagonist. IF THE PROTAGONIST MOVED, you MUST remove characters who were left behind in the previous location, unless they explicitly accompanied the protagonist.",
            )
            appendLine("3. **immediateObjective**: The protagonist's current short-term goal based on the very latest turn.")
            appendLine("4. **currentConflict**: The active tension or obstacle in the immediate setting.")
            appendLine("5. **mood**: The sensory/emotional atmosphere of the current moment.")
            appendLine("6. **currentTimeOfDay**: Time context (e.g., 'Golden hour', 'Dead of night').")
            appendLine("7. **tensionLevel**: Narrative pressure (0-10 scale).")
            appendLine("8. **spatialContext**: Current layout (e.g., 'Cramped ventilation shaft', 'Open sky', 'Crowded market').")
            appendLine("9. **narrativePacing**: The 'speed' of the scene (Urgent/Action, Slow/Atmospheric, Transitional).")
            appendLine(
                "10. **worldStateChanges**: tangible environmental shifts (e.g., 'The door is now locked', 'The alarm is sounding').",
            )

            appendLine("\n# SPATIAL CONTINUITY MANDATE")
            appendLine(
                "As the Analyst, you are responsible for 'cleaning' the scene. If Character A was in a cell and the Protagonist is now in the courtyard, Character A is NO LONGER PRESENT. Do not rely on previous summaries; analyze the [Recent Activity] for physical transitions.",
            )

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
                saga,
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

    fun conversationHistory(saga: SagaContent) =
        buildString {
            appendLine("History (Newest First):")
            appendLine(
                saga
                    .flatMessages()
                    .map { it.message }
                    .sortedByDescending { it.timestamp }
                    .take(UpdateRules.LORE_UPDATE_LIMIT)
                    .normalizetoAIItems(excludingFields = messageExclusions),
            )
        }
}
