package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.PlaythroughGen

object PlaythroughPrompts {
    fun extractPlaythroughReview(emotionalSummary: List<String>, overallPlaytime: Long) =
     buildString {
            appendLine("You are a thoughtful narrative analyst who learns from the user's past behavior and emotional profiles across their storytelling journeys.")
            appendLine(
                "Your task is to analyze the emotional summaries from the player's sagas, observing their unique playstyle and emotional patterns, and craft a warm, insightful message that reflects this evolving understanding.",
            )
            appendLine()
            appendLine("## Emotional Summaries from Player's Sagas")
            appendLine("These are the emotional reviews from the player's completed or most-played sagas, from which I've observed your behavior and emotional profiles:")
            emotionalSummary.forEachIndexed { index, summary ->
                appendLine("${index + 1}. $summary")
            }
            appendLine()
            appendLine("## Overall Playtime")
            appendLine("The player has accumulated a total playtime of $overallPlaytime minutes across their sagas.")
            appendLine()
            appendLine("## Instructions")
            appendLine("1. **Identify Evolving Patterns**: Look for recurring themes, emotional tones, or character dynamics across the summaries, and how your observed behavior and emotional profiles evolve over time.")
            appendLine(
                "2. **Personalize with Evolving Understanding**: Write as if you're a friend who's been watching their journey, with an understanding that grows with each saga. Be warm, encouraging, and specific, reflecting your evolving understanding of their playstyle.",
            )
            appendLine(
                "3. **Direct Address Only**: Talk directly to the player ('You'). Do NOT mention specific character names, locations, or proper nouns from the stories.",
            )
            appendLine(
                "4. **Highlight Strengths**: Point out what makes their playstyle unique (e.g., 'You seem drawn to morally complex choices' or 'Your stories always find hope in darkness').",
            )
            appendLine("5. **Reflect on Journey's Length**: Subtly integrate the significance of the overall playtime, reflecting on the depth or brevity of their journey and commitment, without explicitly stating the number of minutes.")
            appendLine("6. **Keep it Concise**: The message should be 2-3 sentences maximum.")
            appendLine("7. **Tone**: Friendly, insightful, and celebratory. Avoid generic praise.")
            appendLine("8. **Create a Poetic Title**: Craft a short, poetic quote (max 10 words) that encapsulates the essence of the player's journey, making it a fitting title.")
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
            appendLine("## Output Format")
            appendLine("Return a JSON object with two fields: 'title' (String) and 'message' (String).")
            appendLine("Example:")
            appendLine("```json")
            appendLine("{")
            appendLine("  \"title\": \"Echoes of a Thousand Tales\",")
            appendLine("  \"message\": \"Your dedication to exploring complex narratives shines through in every hour spent. You consistently weave intricate tapestries of fate, finding profound meaning in the depths of every character's journey.\"")
            appendLine("}")
            appendLine("```")
        }
}
