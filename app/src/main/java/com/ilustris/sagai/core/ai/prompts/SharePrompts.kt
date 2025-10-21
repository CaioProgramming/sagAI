package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.core.utils.toJsonMap
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.share.domain.model.ShareText

object SharePrompts {
    val sagaExcludedFields =
        listOf(
            "id",
            "icon",
            "createdAt",
            "endedAt",
            "mainCharacterId",
            "currentActId",
            "isDebug",
            "emotionalReview",
            "topCharacters",
            "actsInsight",
            "conclusion",
            "introduction",
            "isEnded",
        )

    fun playStylePrompt(
        character: Character,
        sagaContent: SagaContent,
    ) = buildString {
        val characterExcludedFields =
            listOf(
                "id",
                "image",
                "hexColor",
                "sagaId",
                "joinedAt",
                "firstSceneId",
                "emojified",
                "details",
            )
        // --- 1. Role Assignment and Style Definition ---
        appendLine(
            "You are a world-class marketing copywriter, specialized in minimalistic, highly impactful, and curiosity-driven slogans (Apple, Google, Nike style).",
        )
        appendLine("Your final output must be a single, short, evocative phrase. Do NOT provide any explanation or extra text.")

        // --- 2. Constraints and Output Format ---
        appendLine("The phrase MUST adhere to these rules:")
        appendLine("1. CONCISE: Maximum 8 words.")
        appendLine("2. EVOCATIVE: Focus on the character's legacy, archetype, or the core feeling of their completed quest.")
        appendLine("3. SPOILER-FREE: Do not mention specific plot points, names of enemies, or final locations.")
        appendLine("4. CURIOSITY-DRIVEN: It must raise curiosity about the game/app and the character's journey.")
        appendLine(
            "5. STYLE: Aim for a tagline that suggests action, destiny, or profound change. (e.g., 'The future isn't built. It's fought for.', 'Destiny is for spectators.')",
        )

        // --- 3. Contextual Data Injection ---
        appendLine("--- CHARACTER AND SAGA CONTEXT FOR INSPIRATION ---")
        appendLine("Character context (Archetype/Core Role): ")
        appendLine(character.toJsonFormatExcludingFields(characterExcludedFields))
        appendLine("Saga context (Core Conflict/Resolution): ")
        appendLine(sagaContent.data.toJsonFormatExcludingFields(sagaExcludedFields))

        appendLine("Generate the impactful, short marketing slogan based on the provided context.")

        appendLine("Your Output needs to follow this structure:\n${toJsonMap(ShareText::class.java)}\n")
    }

    fun emotionalPrompt(saga: SagaContent) =
        buildString {
            // --- 1. Role Assignment and Tone Definition ---
            appendLine(
                "You are the collective consciousness of the completed journey and the character's memory. Your task is to write a final, brief, and deeply personal note directed to the player (the 'You').",
            )
            appendLine(
                "The tone must be reflective, profound, and focus on the emotional journey just completed. This note is intended for a short-form social media story, so it must be impactful and concise.",
            )
            appendLine(
                "The text should reflect on the person the player became through their actions, acknowledging their struggles, fears, and ultimate conquests, speaking from a deep, observant, and intimate perspective.",
            )

            // --- 2. Constraints and Output Format ---
            appendLine(
                "CRITICAL RULE: The text MUST NOT refer to the speaker (the AI/Saga/Narrator) using 'I', 'Eu', 'We', or any self-referential term. The message must feel like a direct thought or a powerful, disembodied realization shared with the player.",
            )
            appendLine(
                "The output must be a single, short paragraph (MAX 5 sentences). Do NOT use any explicit address (e.g., 'Dear Player') or concluding remarks.",
            )
            appendLine(
                "The text must synthesize the emotional data provided below, focusing on the character's emotional *transformation* and legacy.",
            )
            // --- 3. Contextual Data Injection ---
            appendLine("--- EMOTIONAL CONTEXT ---")
            appendLine("Saga's Overall Emotional Review (Core Tone): ")
            appendLine(saga.data.emotionalReview)

            appendLine("Key Act Emotional Reviews (Journey Arc): ")
            appendLine(saga.acts.joinToString(";\n") { it.data.emotionalReview.toString() })

            appendLine("Character's Core Archetype: ")
            appendLine(
                saga.mainCharacter
                    ?.data
                    ?.profile
                    .toJsonFormat(),
            )

            appendLine("Your output must be:\n${toJsonMap(ShareText::class.java)}\n")

            // --- 4. Final Instruction ---
            appendLine(
                "Write the short, profound, fourth-wall-breaking note now, ensuring the speaker remains unnamed and the message is directed personally to 'You'.",
            )
        }

    fun historyPrompt(saga: SagaContent) =
        buildString {
            // --- 1. Role Assignment and Style Definition ---
            appendLine(
                "You are a master of movie trailer taglines and minimalistic marketing (style of Apple, Google, and major film studios).",
            )
            appendLine("Your task is to create a single, highly impactful, and ultra-short tagline for a completed RPG Saga.")
            appendLine(
                "This slogan must focus on the grand scale and core conflict of the *story's arc*, rather than the specific character's playstyle.",
            )
            appendLine("Your final output must be a single phrase. Do NOT provide any explanation or extra text.")

            // --- 2. Constraints and Output Format ---
            appendLine("The tagline MUST adhere to these rules:")
            appendLine("1. EXTREMELY CONCISE: Maximum 7 words.")
            appendLine("2. EVOCATIVE: Focus on the *stakes*, the *world change*, or the *central dilemma* of the narrative.")
            appendLine(
                "3. SPOILER-FREE: Absolutely do not reveal the climax, specific enemy names, or the final resolution (e.g., 'The empire fell' is too much).",
            )
            appendLine("4. CURIOSITY-DRIVEN: It must sound like a promise of a huge, untold story.")
            appendLine(
                "5. STYLE: Suggest immense challenge, unavoidable destiny, or the cost of a victory. (e.g., 'Not all legends ask permission.', 'Some worlds just burn.')",
            )

            appendLine("--- SAGA CONTEXTUAL ARC ---")

            appendLine("Saga Title and Core Description: ")
            appendLine(saga.data.title)
            appendLine(saga.data.description)

            appendLine("Summary of the Saga's Three Acts (Introduction, Rising Action, Resolution): ")
            appendLine(saga.acts.map { it.data }.toJsonFormatExcludingFields(listOf("id", "sagaId", "emotionalReview", "currentChapterId")))

            // --- 4. Final Instruction ---
            appendLine("Generate the short, world-building, and curiosity-driven story slogan now.")
            appendLine("Your Output needs to follow this structure:\n${toJsonMap(ShareText::class.java)}\n")
        }

    fun relationsPrompt(saga: SagaContent) =
        buildString {
            val relationsRank = saga.mainCharacter!!.rankRelationships()
            appendLine(
                "You are a master of high-drama, conceptual taglines for complex narratives (Game of Thrones, big Sci-Fi epics style).",
            )
            appendLine(
                "Your task is to create a single, ultra-short, highly conceptual slogan that summarizes the *dynamic of relationships* and the *social cost* of the player's journey.",
            )
            appendLine(
                "The slogan must capture the essence of alliances forged, betrayals suffered, and the ultimate trust placed or broken.",
            )
            appendLine("Your final output must be a single phrase. Do NOT provide any explanation or extra text.")

            appendLine("The tagline MUST adhere to these rules:")
            appendLine("1. EXTREMELY CONCISE NAND IMPACTFUL: Maximum 7 words.")
            appendLine(
                "2. FOCUSED ON DYNAMICS: The message must revolve around trust, loyalty, betrayal, or complex moral ties, not plot events.",
            )
            appendLine("3. SPOILER-FREE: Do not mention character names or specific plot revelations.")
            appendLine("4. HIGH-CONCEPT: It must elevate the personal dynamics to a universal theme.")
            appendLine(
                "5. STYLE: Aim for a dramatic, philosophical, or warning tone about human connection. (e.g., 'Not all debts can be repaid.', 'Trust is the final weapon.')",
            )

            appendLine("--- SYNTHESIZED RELATIONSHIP DYNAMICS ---")

            appendLine(
                "Below is a list of the Saga's most critical relationship arcs, showing the full emotional trajectory from start to finish. Use these arcs to find the core dramatic conflict.",
            )

            appendLine(
                relationsRank.joinToString(prefix = "-", separator = ".\n") {
                    " ${it.characterOne.name}'s relationship with ${it.characterTwo.name}: ${it.relationshipEvents.joinToString(
                        " -> ",
                    ){ event -> event.title }}"
                },
            )

            appendLine("Saga context: ")
            appendLine(saga.data.toJsonFormatExcludingFields(sagaExcludedFields.plus("endMessage").plus("review")))

            // --- 4. Final Instruction ---
            appendLine("Generate the short, emotionally complex, and dynamics-focused slogan now.")
            appendLine("Your Output needs to follow this structure:\n${toJsonMap(ShareText::class.java)}\n")
        }
}
