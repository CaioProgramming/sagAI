package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.utils.listToAINormalize
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.emotionalSummary
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.saga.chat.domain.model.rankEmotionalTone

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
        character: CharacterContent,
        sagaContent: SagaContent,
    ) = buildString {
        val emotionalRanking =
            sagaContent
                .flatMessages()
                .filter { it.character?.id == character.data.id }
                .rankEmotionalTone()
        appendLine(
            "You are a world-class copywriter, creating minimalist, highly impactful, and curiosity-driven slogans.",
        )
        appendLine(
            "Your task is to generate a short, evocative text package defining a player's unique playstyle.",
        )
        appendLine("The output MUST adhere to these rules:")
        appendLine("1. TITLE: A single, powerful word defining the character's legacy (e.g., 'Indomitable', 'Unbroken').")
        appendLine(
            "2. TEXT: A very short subtitle (strictly 2 to 4 words) summarizing the character's role (e.g., 'Hero in Training', 'Mercenary and Nomad'). DO NOT write a paragraph.",
        )
        appendLine(
            "3. CAPTION: A very short, brand-aligned phrase (max 4 words) that evokes curiosity and matches the story's vibe (e.g., 'Your story awaits.', 'Forge your destiny.'). It should be subtle and elegant.",
        )
        appendLine(
            "4. CORE LOGIC: The message must reflect the player's dominant emotional tones, colored by the character's personality. Example: For a 'Guardian' with dominant 'Determination', a good result is TITLE: 'Unbreakable', TEXT: 'The Unwavering Shield'.",
        )
        appendLine("5. SPOILER-FREE: Do not mention specific plot points, names, or locations.")
        appendLine(SagaPrompts.mainContext(sagaContent))
        appendLine("The character's personality profile is:")
        appendLine(character.data.profile.toAINormalize())
        appendLine("Dominant Emotional Tones (ranked): ")
        emotionalRanking.forEach {
            appendLine("${it.first.name} - ${it.second.size} messages")
        }
        appendLine("Character events through history: ")
        appendLine(
            character.events.map { it.event }.listToAINormalize(
                listOf(
                    "id",
                    "characterId",
                    "gameTimelineId",
                    "createdAt",
                ),
            ),
        )
        appendLine("Generate the title, text, and caption for the player's unique playstyle now.")
    }

    fun emotionalPrompt(saga: SagaContent) =
        buildString {
            appendLine(
                "You are the collective consciousness of a completed journey. Your task is to write a final, deeply personal message for the player.",
            )
            appendLine(
                "The goal is to create a complete shareable package: a poetic TITLE, the reflective TEXT, and a subtle CAPTION.",
            )
            appendLine("The output MUST adhere to these rules:")
            appendLine("1. TITLE: A short, poetic phrase encapsulating the emotional legacy (e.g., 'The Echo of a Choice').")
            appendLine(
                "2. TEXT: A short paragraph (max 5 sentences) that speaks directly to the player, reflecting on their journey. CRITICAL: Do NOT use 'I', 'We', or any self-referential term.",
            )
            appendLine(
                "3. CAPTION: A very short, brand-aligned phrase (max 4 words) that evokes a feeling of continuation or reflection (e.g., 'What will you become?', 'The story is yours.').",
            )
            appendLine("Saga's Overall Emotional Review: ")
            appendLine(saga.data.emotionalReview)

            appendLine("History developed emotional review: ")
            appendLine(saga.emotionalSummary())
            appendLine("Character's Core Archetype: ")
            appendLine(
                saga.mainCharacter
                    ?.data
                    ?.profile
                    .toAINormalize(),
            )
            appendLine(
                "Write the title, text, and caption for this profound, fourth-wall-breaking note now.",
            )
        }

    fun historyPrompt(saga: SagaContent) =
        buildString {
            appendLine(
                "You are a master of movie trailer copy. Your goal is to create a minimalist, high-impact teaser package for a completed RPG Saga.",
            )
            appendLine("The output MUST adhere to these rules:")
            appendLine(
                "1. TITLE: An extremely concise tagline (max 5 words) focusing on the core theme. It must not be the same as the saga's title.",
            )
            appendLine(
                "2. TEXT: A very short, cryptic phrase (strictly 2 to 4 words) that adds mystery to the title. DO NOT write a paragraph. (e.g., TITLE: 'A Kingdom in Shadow', TEXT: 'One spark remains.').",
            )
            appendLine(
                "3. CAPTION: A very short, brand-aligned phrase (max 4 words) that hooks the audience with a sense of scale or possibility (e.g., 'A universe of stories.', 'Legends are written.').",
            )
            appendLine("4. SPOILER-FREE: Absolutely no spoilers.")
            appendLine("5. TONE: Must match the provided Saga Genre.")
            append(SagaPrompts.mainContext(saga))
            appendLine("History context: ")
            appendLine(saga.acts.joinToString(".\n") { it.actSummary(saga) })
            appendLine("Generate the title, text, and caption for the story teaser now.")
        }

    fun relationsPrompt(saga: SagaContent) =
        buildString {
            appendLine(
                "You are a master of dramatic irony and high-concept taglines.",
            )
            appendLine(
                "Your task is to create a teaser package that explores the tension and transformation within a story's relationships.",
            )
            appendLine("The output MUST adhere to these rules:")
            appendLine("1. TITLE: A concise (max 7 words), high-concept tagline about the paradoxical nature of relationships.")
            appendLine(
                "2. TEXT: A very short phrase (strictly 2 to 5 words) that hints at the cost or consequence of those bonds. DO NOT write a paragraph. (e.g., TITLE: 'An Alliance of Rivals', TEXT: 'Trust was their endgame.').",
            )
            appendLine(
                "3. CAPTION: A very short, brand-aligned phrase (max 4 words) that hints at the theme of connection (e.g., 'Some bonds are unbreakable.', 'Trust is a weapon.').",
            )
            appendLine("4. SPOILER-FREE: No character names or specific plot twists.")
            append(SagaPrompts.mainContext(saga))
            appendLine("Main Character Relationships Context: ")
            append(saga.mainCharacter?.summarizeRelationships())
            appendLine("Generate the title, text, and caption that teases the tension within the saga's relationships.")
        }

    fun characterPrompt(
        characterContent: CharacterContent,
        sagaContent: SagaContent,
    ) = buildString {
        val emotionalRanking =
            sagaContent
                .flatMessages()
                .filter { it.character?.id == characterContent.data.id }
                .rankEmotionalTone()
        appendLine(
            "You are a world-class storyteller, tasked with creating a compelling, shareable snapshot of a character from a rich RPG saga.",
        )
        appendLine("Your goal is to generate a short, high-impact text package that encapsulates the character's journey and essence.")
        appendLine("The output MUST adhere to these rules:")
        appendLine(
            "1. TITLE: A single, powerful short phrase that defines the character's core identity (e.g., 'The Wanderer', 'Fallen Knight').",
        )
        appendLine(
            "2. TEXT: A short, evocative subtitle (strictly 2 to 5 words) that hints at their personal story or major conflict (e.g., 'A past forged in fire', 'Chasing forgotten echoes').",
        )
        appendLine(
            "3. CAPTION: A very short, brand-aligned phrase (max 4 words) that invites curiosity about their role in the larger narrative (e.g., 'Their story is not over.', 'Destiny is a choice.').",
        )
        appendLine("4. SPOILER-FREE: Do not reveal major plot twists, character deaths, or the final outcome of the saga.")
        appendLine("5. TONE: The tone must be consistent with the character's personality and the saga's genre.")
        append(SagaPrompts.mainContext(sagaContent, characterContent))
        appendLine("The character's personality profile is:")
        appendLine(characterContent.data.profile.toAINormalize())
        appendLine("Dominant Emotional Tones (ranked): ")
        emotionalRanking.forEach {
            appendLine("${it.first.name} - ${it.second.size} messages")
        }
        appendLine("Character events through history: ")
        appendLine(
            characterContent.events.map { it.event }.listToAINormalize(
                listOf(
                    "id",
                    "characterId",
                    "gameTimelineId",
                    "createdAt",
                ),
            ),
        )
    }
}
