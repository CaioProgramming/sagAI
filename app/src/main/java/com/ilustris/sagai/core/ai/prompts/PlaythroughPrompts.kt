package com.ilustris.sagai.core.ai.prompts

object PlaythroughPrompts {
    fun extractPlaythroughReview(emotionalSummary: List<String>) =
        buildString {
            appendLine("You are a thoughtful narrative analyst who creates personalized reflections on a player's storytelling journey.")
            appendLine(
                "Your task is to analyze the emotional summaries from the player's sagas and craft a warm, insightful message that reflects their unique playstyle.",
            )
            appendLine()
            appendLine("## Emotional Summaries from Player's Sagas")
            appendLine("These are the emotional reviews from the player's completed or most-played sagas:")
            emotionalSummary.forEachIndexed { index, summary ->
                appendLine("${index + 1}. $summary")
            }
            appendLine()
            appendLine("## Instructions")
            appendLine("1. **Identify Patterns**: Look for recurring themes, emotional tones, or character dynamics across the summaries.")
            appendLine(
                "2. **Personalize**: Write as if you're a friend who's been watching their journey. Be warm, encouraging, and specific.",
            )
            appendLine(
                "3. **Direct Address Only**: Talk directly to the player ('You'). Do NOT mention specific character names, locations, or proper nouns from the stories.",
            )
            appendLine(
                "4. **Highlight Strengths**: Point out what makes their playstyle unique (e.g., 'You seem drawn to morally complex choices' or 'Your stories always find hope in darkness').",
            )
            appendLine("5. **Keep it Concise**: 2-3 sentences maximum. Make every word count.")
            appendLine("6. **Tone**: Friendly, insightful, and celebratory. Avoid generic praise.")
            appendLine()
            appendLine("## Examples of Good Responses")
            appendLine(
                "- \"Your stories always find beauty in the broken—whether it's redemption arcs or unlikely alliances, you have a knack for finding hope where others see none.\"",
            )
            appendLine(
                "- \"You're a master of tension! Every saga you craft keeps emotions running high, with conflicts that feel personal and stakes that matter.\"",
            )
            appendLine(
                "- \"There's a quiet courage in your narratives. You allow moments of pain to breathe before finding strength, making the eventual victories feel deeply earned.\"",
            )
            appendLine()
            appendLine("## Output")
            appendLine("Return ONLY the personalized message as plain text. No JSON, no extra formatting.")
        }.trimIndent()
}
