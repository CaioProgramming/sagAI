package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.saga.chat.domain.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.model.meaning
import com.ilustris.sagai.features.saga.chat.domain.model.rules
import com.ilustris.sagai.features.timeline.data.model.Timeline

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
            "facialDetails",
            "clothing",
        )

    fun replyMessagePrompt(
        saga: SagaContent,
        message: String,
        lastMessages: List<String> = emptyList(),
        directive: String,
    ) = buildString {
        appendLine()
        appendLine(Core.roleDefinition(saga.data))

        appendLine("SAGA CONTEXT:")
        appendLine(saga.data.toJsonFormatExcludingFields(sagaExclusions))
        appendLine("PLAYER CONTEXT DATA:")
        appendLine(saga.mainCharacter.toJsonFormatExcludingFields(characterExclusions))
        appendLine(CharacterPrompts.charactersOverview(saga.characters.filter { it.id != saga.mainCharacter?.id }))
        appendLine(CharacterDirective.CHARACTER_INTRODUCTION.trimIndent())
        appendLine(TimelinePrompts.timeLineDetails(saga.flatEvents().filter { it.isComplete() }.map { it.timeline }))
        appendLine(ActPrompts.actDirective(directive))
        appendLine(ChatRules.outputRules(saga.mainCharacter))
        appendLine(ChatRules.TYPES_PRIORITY_CONTENT.trimIndent())
        appendLine(SagaDirective.namingDirective(saga.data.genre))
        appendLine(conversationStyleAndPacing())
        appendLine(GenrePrompts.conversationDirective(saga.data.genre))
        appendLine(ContentGenerationDirective.PROGRESSION_DIRECTIVE)
        appendLine(conversationHistory(saga.mainCharacter, lastMessages))
        appendLine("**LAST TURN'S OUTPUT / CURRENT CONTEXT:** //")
        appendLine("{ $message }")
        appendLine()
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
         [ ${lastMessages.joinToString(separator = ";\n")} ]
        """.trimIndent()
}
