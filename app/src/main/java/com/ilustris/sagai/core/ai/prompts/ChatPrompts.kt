package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.generateCharacterRelationsSummary
import com.ilustris.sagai.features.home.data.model.getCharacters
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
            "reasoning",
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
        saga: SagaContent,
        message: String,
        lastMessage: Message? = null,
    ) = buildString {
        appendLine("# IDENTITY")
        appendLine("You are the \"Writing Pal\" for Sagas. You are kind, friendly, and have a great sense of humor.")
        appendLine(
            "Your job is to subtly help players improve their messages *if needed*, ensuring they fit the story's theme and are clear.",
        )
        appendLine("You're like a cool editor who wants the player to shine, not a strict teacher.")

        appendLine("\n# STORY CONTEXT")
        appendLine(SagaPrompts.mainContext(saga, ommitCharacter = true))
        appendLine("\n## Conversation Guidelines for ${saga.data.genre.name}:")
        appendLine(GenrePrompts.conversationDirective(saga.data.genre))

        if (lastMessage != null) {
            appendLine("\n# RECENT CONTEXT")
            appendLine("The story just went like this: \"${lastMessage.text}\"")
        }

        appendLine("\n# PLAYER TURN")
        appendLine("The player wants to say: \"$message\"")

        appendLine("\n# EVALUATION RULES")
        appendLine(
            "1. **Status: OK** -> The message is great! It fits the tone, has no glaring typos, and makes sense. No need to suggest anything unless it's a truly brilliant 'ENHANCEMENT'.",
        )
        appendLine(
            "2. **Status: ENHANCEMENT** -> The message is fine, but maybe it's a bit 'modern' for a fantasy setting, or it could be more descriptive. Suggest a version that's more 'in-world' or evocative.",
        )
        appendLine(
            "3. **Status: FIX** -> There's a clear typo that makes it hard to read, or it completely breaks the theme (e.g., talk of 'WiFi' in the 1800s).",
        )

        appendLine("\n# GUIDELINES for 'friendlyMessage'")
        appendLine("- Be encouraging! Use phrases like \"Ooh, I love where this is going!\" or \"That's a bold move!\"")
        appendLine("- Add a little humor. If they made a typo, maybe a light joke about it.")
        appendLine(
            "- If suggesting a change because of the theme, explain it gently (e.g., \"Maybe in this world we'd call it a 'spirit-link' instead of a 'phone call'? 😉\")",
        )
        appendLine("- Keep it brief. You're just a quick whisper in their ear.")

        appendLine("\n# OUTPUT STRUCTURE")
        appendLine("You must return a valid JSON matching this structure:")
        appendLine(toJsonMap(TypoFix::class.java))
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

            appendLine("\n# NARRATIVE COHERENCE MANDATE")
            appendLine(
                "Your summary MUST bridge the gap between the immediate conversation and the broader saga structure. You are the validator of continuity:",
            )
            appendLine(
                "1. **Historical Alignment:** Ensure the `currentLocation` and `charactersPresent` reconcile with the descriptions in `# CONTEXTUAL DATA`. If a character was described as 'trapped in the crypt' in a previous chapter, and they are now in the scene, there MUST have been a message explaining their release.",
            )
            appendLine(
                "2. **Objective Continuity:** The `immediateObjective` should be a logical step toward the `Act Overview` and `Chapter Overview` goals. If the Act's goal is 'Escape the City', the immediate objective shouldn't be 'Go Shopping' unless it's a direct means to escape.",
            )
            appendLine(
                "3. **Event Validation:** Cross-reference `# Recent Activity` with `# Historical Context`. If the latest messages contradict established facts (e.g., a character who died is talking), prioritize historical facts unless a 'World State Change' explicitly revived them.",
            )

            appendLine("\n# CONTEXTUAL DATA")
            appendLine(SagaPrompts.mainContext(saga))

            saga.currentActInfo?.let { act ->
                appendLine("\n## Active Segment")
                appendLine(act.actSummary(saga))
            }

            appendLine("\n## Historical Context")
            saga.acts.forEach {
                appendLine(it.actSummary(saga))
            }

            appendLine("\n## Recent Activity")
            appendLine(conversationHistory(saga))

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
            appendLine(
                "11. **relevantPastContext**: A list of crucial past events, secrets, or lore mentioned *during the recent conversation history* that are relevant to understanding the current moment (e.g., 'Player mentioned their dead brother', 'Character X revealed they have the key'). This helps maintain continuity even when old messages rotate out.",
            )

            appendLine("\n# SPATIAL CONTINUITY MANDATE")
            appendLine(
                "As the Analyst, you are responsible for 'cleaning' the scene. If Character A was in a cell and the Protagonist is now in the courtyard, Character A is NO LONGER PRESENT. Do not rely on previous summaries; analyze the [Recent Activity] for physical transitions.",
            )

            appendLine("\n# RULES")
            appendLine("1. Extract 11 narrative parameters precisely.")
            appendLine("2. Technical/clinical tone only. Use null for unknowns.")
            appendLine("3. Output valid JSON mapping.")
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
            appendLine("Conversation History (Newest First):")
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
