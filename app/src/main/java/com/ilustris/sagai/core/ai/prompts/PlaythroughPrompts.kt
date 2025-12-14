package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.emotionalSummary

object PlaythroughPrompts {
    fun extractPlaythroughReview(sagas: List<SagaContent>) =
        buildString {
            val emotionalSummary =
                buildString {
                    appendLine(
                        sagas.joinToString {
                            buildString {
                                append(it.emotionalSummary())
                                appendLine("playTimeMs: ${it.data.playTimeMs}")
                            }
                        },
                    )
                }
            appendLine("You are an insightful narrative psychologist who reads between the lines of a player's storytelling choices.")
            appendLine(
                "Your task is to deeply analyze the emotional patterns, choices, and themes from the player's sagas to reveal who they are as a storyteller and decision-maker.",
            )
            appendLine()
            appendLine("## Emotional Summaries from Player's Sagas")
            appendLine("These are the emotional reviews from the player's completed or most-played sagas:")
            appendLine(emotionalSummary)
            appendLine("## Instructions")
            appendLine(
                "1. **Deep Analysis**: Look beyond surface emotions. What do their choices reveal about their values, fears, hopes, and worldview?",
            )
            appendLine(
                "2. **Psychological Insight**: Identify what drives them - do they seek redemption, embrace chaos, value loyalty, or challenge authority?",
            )
            appendLine(
                "3. **Recurring Motifs**: Notice what themes they return to across different stories. What does this say about their inner world?",
            )
            appendLine(
                "4. **Personal Mirror**: The review should feel like you're holding up a mirror to their soul through their storytelling.",
            )
            appendLine(
                "5. **Direct and Intimate**: Write as if you truly understand them. Use 'you' and speak to their essence as a player.",
            )
            appendLine(
                "6. **Avoid Specifics**: Never mention character names, locations, or story details. Keep it universal and personal.",
            )
            appendLine("7. **Review Length**: 3-4 sentences that progressively deepen the insight.")
            appendLine(
                "8. **Title**: Create a poetic, short quote (3-6 words) that captures their essence. Think literary, philosophical, or introspective. Examples: 'Where Shadows Learn to Dance', 'The Gentle Revolutionary', 'Chaos Clothed in Mercy'",
            )
            appendLine()
            appendLine("## Example Output")
            appendLine("Response must be a valid JSON object:")
        }.trimIndent()
}
