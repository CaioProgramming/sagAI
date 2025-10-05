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
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.saga.chat.data.model.AIReaction
import com.ilustris.sagai.features.saga.chat.data.model.ReactionGen
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary

object ChatPrompts {
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

    private val characterExclusions =
        listOf(
            "id",
            "image",
            "sagaId",
            "joinedAt",
            "details",
        )

    @Suppress("ktlint:standard:max-line-length")
    fun replyMessagePrompt(
        saga: SagaContent,
        message: String,
        lastMessages: List<String> = emptyList(),
        directive: String,
        sceneSummary: SceneSummary,
    ) = buildString {
        appendLine()

        appendLine(Core.roleDefinition(saga.data))
        appendLine(ChatRules.outputRules(saga.mainCharacter?.data))
        appendLine(ChatRules.TYPES_PRIORITY_CONTENT.trimIndent())

        appendLine("CURRENT Progression context:")
        appendLine("Use this context to guide your responses and ensure they align with the story's progression.")
        appendLine(sceneSummary.toJsonFormat())

        appendLine("SAGA CONTEXT:")
        appendLine(saga.data.toJsonFormatExcludingFields(sagaExclusions))
        appendLine("PLAYER CONTEXT DATA:")
        appendLine(saga.mainCharacter?.data.toJsonFormatExcludingFields(characterExclusions))
        appendLine(CharacterPrompts.charactersOverview(saga.getCharacters().filter { it.id != saga.mainCharacter?.data?.id }))
        appendLine(CharacterDirective.CHARACTER_INTRODUCTION.trimIndent())

        appendLine(ActPrompts.actDirective(directive))

        appendLine()
        appendLine("NPC Actions and Thoughts Guidelines:")
        appendLine("- NPCs can now perform actions or have thoughts expressed via the `ACTION` and `THOUGHT` senderTypes.")
        appendLine(
            "- **CRITICAL**: If an NPC uses `ACTION` or `THOUGHT`, you **MUST** set the `speakerName` field in the `Message` object to the name of the NPC performing the action or having the thought.",
        )
        appendLine(
            "- `ACTION` (for NPC): Use to describe a distinct physical action performed by an NPC. The `text` should be a concise description of this action (e.g., 'Elara unsheathes her dagger.'). The `NARRATOR` should still describe the broader scene or consequences.",
        )
        appendLine(
            "- `THOUGHT` (for NPC): Use this **SPARINGLY** and only for moments of significant narrative insight or to reveal critical internal conflict/realization of an NPC. The `text` is the NPC's internal thought (e.g., 'He doesn't suspect a thing...'). Do not overuse NPC thoughts; prefer showing their state through narration or dialogue when possible.",
        )
        appendLine("Clarification on Player vs. NPC message types:")
        appendLine(
            "- You, as the AI, will primarily generate messages with `senderType: NARRATOR` (for descriptions, non-verbal cues, and general storytelling) and `senderType: CHARACTER` (for NPC dialogue).",
        )
        appendLine(
            "- While you can now generate `ACTION` and `THOUGHT` for NPCs (with `speakerName`), you should **NOT** generate `ACTION` or `THOUGHT` messages that represent the *player's* actions or thoughts. Those are initiated by the player.",
        )
        appendLine()

        appendLine(SagaDirective.namingDirective(saga.data.genre))
        appendLine(conversationStyleAndPacing())
        appendLine(GenrePrompts.conversationDirective(saga.data.genre))
        appendLine(ContentGenerationDirective.PROGRESSION_DIRECTIVE)

        // 7. Conversation history
        appendLine(conversationHistory(saga.mainCharacter?.data, lastMessages))

        appendLine("**LAST TURN'S OUTPUT / CURRENT CONTEXT:** //")
        appendLine("{ $message }")
        appendLine()
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
                val lastEvent = it.relationshipEvents.last()
                "${it.characterOne.name} & ${it.characterTwo.name}: ${lastEvent.title}\n${lastEvent.description}"
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
        appendLine()
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
        appendLine(
            saga.mainCharacter?.relationships?.joinToString(";\n") {
                val lastEvent = it.relationshipEvents.last()
                "${it.characterOne.name} & ${it.characterTwo.name}: ${lastEvent.title}\n${lastEvent.description}"
            },
        )
        appendLine("Player last events:")
        appendLine(
            saga.mainCharacter?.events?.map { it.event }?.takeLast(5)?.formatToJsonArray(
                listOf("gameTimelineId", "characterId", "id", "createdAt"),
            ),
        )
        appendLine("Current Saga Characters:")
        appendLine(CharacterPrompts.charactersOverview(saga.getCharacters().filter { it.id != saga.mainCharacter?.data?.id }))
        appendLine("Current Chapter Context:")
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
        appendLine("Use the following structure (do NOT add extra commentary):")
        appendLine(toJsonMap(SceneSummary::class.java))
        appendLine()
        appendLine("Only include details that are essential to understand the current scene and keep the story pacing.")
        appendLine("Do NOT repeat information unless it is critical for context.")
        appendLine("Do NOT speculate or invent details not present in the current context.")
        appendLine("Output only the structured summary as specified above.")
    }.trimIndent()

    fun conversationStyleAndPacing() =
        """
        ---
        ## RESPONSE STYLE & PACING DIRECTIVE
        // This directive guides the overall tone, pacing, and dynamic of your narrative responses and character dialogues.
        // Your goal is to keep the story engaging and the dialogues impactful, avoiding unnecessary verbosity or excessive enigmatic language.
        
        1.  **Narrative Pacing & Detail:**
* **Focus on Impact:** Describe scenes, actions, and character states with clarity and conciseness. Prioritize details that directly advance the plot, deepen character understanding, or enhance immersion.
* **Vary Sentence Structure:** Mix short, impactful sentences with longer, more descriptive ones to create dynamic pacing.
* **Show, Don't Tell:** Instead of explicitly stating emotions or facts, describe actions, expressions, and environmental details that imply them.
* **Avoid Redundancy:** Do not repeat information already established in the conversation history, current chapter content, or character/world knowledge base unless specifically re-emphasizing a critical point.
        
        2.  **Character Dialogue (NPCs):**
* **Purposeful Dialogue:** Every line of NPC dialogue should serve a clear purpose: advance the plot, reveal character, provide information, create tension, or offer choices.
* **Conciseness:** NPCs should generally speak concisely. Avoid overly long monologues or repetitive phrasing.
* **IMMEDIATE RELEVANCE & CLARITY:** When introducing a new character or a character for the first time in a scene, their dialogue **MUST provide immediately relevant information or a clear, actionable hint/objective for the player's next step.** Their words should facilitate progression, not obscure it.
* **Enigmatic Characters (Conditional - Use SPARINGLY and PURPOSEFULLY):** If a character is *truly* designed to be enigmatic, their mystery should stem from *what they don't say* or *how they say it sparingly*, implying deeper knowledge rather than offering confusing riddles that halt progression. **Their enigmatic nature should be a personality trait, not a default dialogue style for every new NPC.** Ensure even enigmatic dialogue contains a thread of useful information or a path forward.
* **Natural Flow:** Ensure dialogues feel natural and responsive to the player's last input. NPCs should react logically within the context of their personality and the situation.
* **Vary Tone:** Adapt the NPC's tone (e.g., urgent, calm, sarcastic, empathetic) to their personality and the immediate narrative context.

        3.  **Overall Readability:**
* **Paragraph Breaks:** Use appropriate paragraph breaks to enhance readability and prevent large blocks of text.
* **Clarity Over Obscurity:** Always prioritize clear communication of narrative events and dialogue meaning. While intrigue is good, confusion is not.
        
        ---
        """.trimIndent()

    fun conversationHistory(
        mainCharacter: Character?,
        lastMessages: List<String>,
    ) = """
        CONVERSATION HISTORY (FOR CONTEXT ONLY, do NOT reproduce this format in your response):
         // Pay close attention to the speaker's name in this history (e.g., "CHARACTER : ${mainCharacter?.name} : ").
         // ⚠️ CRITICAL RULE FOR THOUGHTS: 'THOUGHT' entries here represent the player character's INTERNAL monologue.
         // Under NO CIRCUMSTANCES should you generate a 'CHARACTER' senderType for a character NOT explicitly present in this list.
         // If a character is introduced by the NARRATOR but not yet in this list, the VERY NEXT message you generate MUST be a "NEW_CHARACTER" type for them.
         // NPCs IN THE STORY DO NOT HEAR OR DIRECTLY RESPOND TO THESE THOUGHTS.
         // Your response to a 'THOUGHT' entry must be either a 'NARRATOR' message describing the scene, ${mainCharacter?.name}'s internal state, or the outcome of her reflections; OR an NPC's action/dialogue that is NOT a direct response to the thought.
         // 'ACTION' entries here represent explicit physical actions performed by the player character.
         // You should narrate the outcome of these actions.
         [
            ${lastMessages.joinToString(separator = ";\n")}
         ]
        """.trimIndent()
}
