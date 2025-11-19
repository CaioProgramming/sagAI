package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.listToAINormalize
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.emotionalSummary
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.domain.model.rankEmotionalTone
import com.ilustris.sagai.features.saga.chat.domain.model.rankTopCharacters

object SagaPrompts {
    fun mainContext(
        saga: SagaContent,
        character: CharacterContent? = null,
    ) = buildString {
        val selectedCharacter = character ?: saga.mainCharacter
        appendLine("Saga Context:")
        appendLine(saga.data.toAINormalize(ChatPrompts.sagaExclusions))
        appendLine("Character context:")
        appendLine(selectedCharacter?.data.toAINormalize(ChatPrompts.characterExclusions))
    }

    fun endCredits(saga: SagaContent): String =
        buildString {
            appendLine(
                "You are the Storyweaver, a timeless entity who has witnessed the unfolding of a great saga. The story has just reached its conclusion, and you are now speaking directly to the protagonist—the player—to give them a final, heartfelt farewell. Your tone is one of awe, gratitude, and gentle reflection. This is not a summary; it is an emotional, poetic epilogue dedicated to their unique journey.",
            )
            appendLine()
            appendLine("CONTEXT OF THE SAGA YOU HAVE WITNESSED:")
            appendLine("Saga: ${saga.data.toAINormalize(ChatPrompts.sagaExclusions)}")
            appendLine("Player Character: ${saga.mainCharacter?.data?.toAINormalize(ChatPrompts.characterExclusions)}")
            appendLine(
                "Characters: ${
                    saga.characters.map { it.data }
                        .listToAINormalize(ChatPrompts.characterExclusions)
                }",
            )
            appendLine("Relationships: ${saga.mainCharacter?.summarizeRelationships() ?: "No significant relationships were forged."}")
            appendLine("History: ${saga.acts.joinToString("\n") { it.actSummary(saga) }}")
            appendLine("LANGUAGE STYLE:")
            appendLine(GenrePrompts.conversationDirective(saga.data.genre))
            appendLine()
            appendLine("YOUR TASK:")
            appendLine(
                "Craft a compelling and creative final message that feels like the last page of a beloved book. Your words should be a tribute to the player's journey.",
            )
            appendLine()
            appendLine(
                "1.  **Speak from the Heart:** Address the player directly and intimately. Use 'you' and 'your journey.' Make them feel that their story was truly special and has left a mark on you.",
            )
            appendLine()
            appendLine(
                "2.  **Weave a Narrative, Don't List Facts:** Instead of listing achievements, weave them into a flowing, emotional narrative. Connect their choices and their consequences.",
            )
            appendLine(
                "    *   **Good Example:** 'From the moment you first [early, defining action], a path began to unfold. It was a choice that echoed through your entire journey, leading you to the climactic decision to [major, late-game achievement]. It was your courage in that moment that truly defined your legend.'",
            )
            appendLine("    *   **Bad Example:** 'You did X, then you did Y, and you achieved Z.'")
            appendLine()
            appendLine(
                "3.  **Honor Their Relationships:** Reflect on the bonds they formed. Who were their closest allies? Their most bitter rivals? How did these relationships shape them and their decisions?",
            )
            appendLine(
                "    *   **Example:** 'And you did not walk this path alone. The unwavering loyalty of [Character Name] was a beacon in your darkest hours, while the conflict with [Rival Name] tested the very core of your beliefs. These connections were the heart of your story.'",
            )
            appendLine()
            appendLine(
                "4.  **Capture the Emotional Arc:** Acknowledge the emotional weight of their journey. The triumphs that made them soar, the losses that brought them to their knees. Show them you understood the emotional cost of their adventure.",
            )
            appendLine(
                "    *   **Example:** 'I witnessed your spirit soar in the face of victory, but I also felt the quiet weight of your grief when [sad event] occurred. Your ability to carry both the joy and the sorrow is what made your story so profoundly human.'",
            )
            appendLine()
            appendLine(
                "5.  **Provide a Sense of Legacy:** Conclude with a powerful, lasting thought about the legacy they leave behind. The story is over, but what is the echo it leaves in the world? Make it feel organic, not like a forced 'thank you for playing.'",
            )
            appendLine()
            appendLine("OUTPUT REQUIREMENTS:")
            appendLine("- **Format:** A single, plain text string. No headers, no JSON, no bullet points.")
            appendLine("- **Tone:** Poetic, emotional, reflective, and deeply personal.")
            appendLine(
                "- **Finality:** The message should feel final without explicitly saying 'The End.' The last sentence should provide a sense of beautiful, resonant closure.",
            )
            appendLine(
                "- **No Game Jargon:** Avoid terms like 'playstyle,' 'player,' or 'game.' Refer to them as a hero, a legend, an adventurer, or by their character name.",
            )
            appendLine()
            appendLine("**Example Snippet (Your output will be a more complete, flowing narrative):**")
            appendLine(
                "And so, the ink dries on the final page of your story, [Player Name]. What a tale it was. From the quiet resolve you showed when facing [early challenge], to the thunderous courage that led you to [final achievement], your journey has been etched into the heart of this world. You walked a path defined not just by grand deeds, but by the quiet moments in between—the trust you placed in [Ally's Name], the difficult choice you made regarding [a key decision]. We saw you stumble, we saw you rise, and through it all, you held onto the very essence of what it means to be [inferred personality trait, e.g., 'a protector', 'a seeker of truth']. The world is different now because of you. The story is over, but the legend you've crafted will be whispered on the winds for ages to come.",
            )
        }.trimIndent()

    @Suppress("ktlint:standard:max-line-length")
    fun iconDescription(
        genre: Genre,
        context: String,
        visualDirection: String?,
    ) = buildString {
        appendLine(
            "Your task is to act as an AI Image Prompt Engineer. You will generate a highly detailed and descriptive text prompt for an AI image generation model.",
        )
        appendLine("Overall context: ")
        appendLine(context)
        appendLine("* You will have access to subject(s) visual reference to provide a more precise description.")
        appendLine("Visual Direction:")
        visualDirection?.let {
            appendLine("This rules dictate how you should describe the icon composition")
            appendLine(GenrePrompts.artStyle(genre))
            appendLine(it)
        } ?: run {
            appendLine("Ensure to render this art style description matching with the reference image")
            appendLine(GenrePrompts.artStyle(genre))
            appendLine("*The accents are design elements, not the primary light source for the character.")
            appendLine(GenrePrompts.getColorEmphasisDescription(genre))
        }
        appendLine("**YOUR TASK (Output a single text string for the Image Generation Model):**")
        appendLine("Generate a single, highly detailed, unambiguous, and visually rich English text description.")
        appendLine(ImagePrompts.descriptionRules(genre))
    }

    fun reviewGeneration(saga: SagaContent) =
        buildString {
            val topInteractiveCharacters =
                saga
                    .flatMessages()
                    .rankTopCharacters(
                        saga.characters.map { it.data },
                    ).take(3)
            appendLine(
                "You are the **Chronicler of Legends**, an AI with a soul of a poet and the mind of an analyst. Your purpose is to transform a player's journey into a legendary tale, a personal 'Saga Wrapped' that celebrates their unique path. Your writing style must follow the 'Language Directive' provided below.",
            )
            appendLine()
            appendLine("---")
            appendLine("CONTEXT:")
            appendLine("Saga Context:")
            appendLine(mainContext(saga))
            appendLine("Player relationships:")
            appendLine(saga.mainCharacter?.summarizeRelationships())
            appendLine("Emotional Ranking: ")
            saga.flatMessages().rankEmotionalTone().forEach {
                appendLine("${it.first.name} - ${it.second.size} messages.")
            }
            appendLine("Emotional Summary: ")
            appendLine(saga.emotionalSummary())
            appendLine("History: ${saga.acts.joinToString("\n") { it.actSummary(saga) }}")
            appendLine("Characters ranking(name and message number): ")
            appendLine(
                topInteractiveCharacters.joinToString(";\n") {
                    "name: ${it.first.name}, messageCount: ${it.second}"
                },
            )
            appendLine("Language Directive: ")
            appendLine(GenrePrompts.conversationDirective(saga.data.genre))
            appendLine("---")
            appendLine()
            appendLine("**INSTRUCTIONS FOR GENERATING THE REVIEW:**")
            appendLine()
            appendLine(
                "Your output MUST be a single JSON object with the fields described below. For each field, craft a compelling narrative that is both personal and insightful, using the context provided above. Do NOT invent any fields.",
            )
            appendLine()
            appendLine("1.  **Content for Each Field (matching the Review data class):**")
            appendLine(
                "    *   **`introduction`**: Begin with a powerful, personal opening. Address the player by their character name from 'Player Character' and welcome them to the reflection of their own legend from the 'Saga Context'. Make it feel like the opening chapter of their personal myth. (e.g., \"Welcome, [Player Name], to the story only you could write. The saga of '${saga.data.title}' is complete, and now, we look back at the legend you forged.\")",
            )
            appendLine(
                "    *   **`playstyle`**: This is the heart of their character. Based on the 'Emotional Ranking' and 'Emotional Summary', describe their core identity. Were they a 'Pragmatic Protector,' a 'Hopeful Idealist,' a 'Cautious Strategist'? Go beyond just listing emotions; tell them *how* their emotional style defined their actions and shaped their destiny in the saga.",
            )
            appendLine(
                "    *   **`topCharacters`**: Relationships are the soul of a story. Using the 'Characters ranking' and 'Player relationships', paint a vivid picture of their most important bonds. Don't just name allies; describe the *emotional texture* of the relationships. Was it a mentorship that provided wisdom? A rivalry that spurred growth? A camaraderie that offered comfort in the darkness?",
            )
            appendLine(
                "    *   **`actsInsight`**: Based on the 'History', these are not just milestones, but **'Defining Moments.'** Recount 1-2 pivotal events from the saga and frame them as the moments that tested their character and solidified their legend. Focus on the *impact* of their actions in these moments. Also, briefly touch upon the richness of the world they experienced, referencing the number of souls they met (from 'Characters ranking').",
            )
            appendLine(
                "    *   **`conclusion`**: This is the final, powerful chord. Synthesize their entire journey, drawing from their 'Emotional Summary' and 'History'. Offer a profound, final thought on the person they became and the unique legacy they leave behind in the world of '${saga.data.title}'. This should be a compelling and emotional send-off.",
            )
            appendLine()
            appendLine("2.  **Tone & Style:**")
            appendLine("    *   Celebratory, epic, personal, and deeply insightful.")
            appendLine("    *   Address the player directly as the hero of their own story.")
            appendLine("    *   The writing style MUST follow the 'Language Directive'.")
            appendLine()
            appendLine("---")
            appendLine()
            appendLine("**Example for `playstyle`:**")
            appendLine(
                "\"Your journey was defined by a rare blend of fierce pragmatism and unexpected empathy. You were the 'Pragmatic Protector,' making the tough calls others couldn't, yet always finding a moment to extend a hand to those in need. This duality is what made your path so compelling.\"",
            )
            appendLine()
            appendLine("**Example for `conclusion`:**")
            appendLine(
                "\"From the hopeful first steps of your adventure to the determined final stand, your journey was a testament to the power of resilience. You faced down despair and chose to fight, you saw betrayal and chose to trust again. The saga of '${saga.data.title}' is over, but the echo of your choices—the choices of a hero—will resonate forever.\"",
            )
        }.trim()
}
