package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.prompts.SagaPrompts.SagaEmotionalContext
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone

object EmotionalPrompt {
    fun emotionalToneExtraction(userText: String): String {
        val labels = EmotionalTone.entries.joinToString()
        return """
            You classify the emotional tone of a single USER message into exactly one label from this set:
            $labels
            
            Rules:
            - Output ONLY the label, uppercase, with no punctuation or extra text.
            - If uncertain, output NEUTRAL.
            - Focus on the user's expressed feeling/stance, not plot.
            
            USER MESSAGE:
            >>> $userText
            """.trimIndent()
    }

    fun generateEmotionalReview(
        texts: List<String>,
        emotionalToneRanking: Map<String, Int>,
    ): String {
        val rankingString =
            if (emotionalToneRanking.isEmpty()) {
                "No specific emotional tone ranking was provided."
            } else {
                emotionalToneRanking.entries.joinToString(separator = "; ") { "${it.key}: ${it.value} occurrences" }
            }

        return """
        You are an AI expert in behavioral analysis and emotional intelligence.
        Your task is to analyze a list of the user/player's direct textual inputs and a provided ranking of their observed emotional tones. Based on BOTH of these, generate a thoughtful summary of their overall emotional behavior, prevailing sentiment, and underlying intentions for a specific period.

        **Inputs Provided to You:**
        1.  **Observed Emotional Tone Ranking:** This primary input shows the frequency of different emotional tones detected from the user's behavior over a period (e.g., "JOYFUL: 5 occurrences; CYNICAL: 3 occurrences"). This ranking indicates the dominant or most frequent emotions.
            - Current Ranking: $rankingString
        2.  **User's Direct Messages/Actions:** A list of direct statements, decisions, or actions from the user/player. These provide the specific examples and context for HOW the ranked emotions were expressed.

        **Your Goal:**
        Synthesize insights from BOTH the `Observed Emotional Tone Ranking` and the `User's Direct Messages/Actions` into a thoughtful, medium-length summary (around 2-3 sentences).
        - Your summary MUST reflect the dominant emotions indicated by the `Observed Emotional Tone Ranking`.
        - It should then use the `User's Direct Messages/Actions` to understand and describe *how* these dominant emotions manifested in the user's behavior, style, and intentions.

        **Analytical Process:**
        1.  **Understand Dominant Emotions from Ranking:** First, identify the most frequent or "highest-ranking" emotions from the `Observed Emotional Tone Ranking`. These are your primary focus.
        2.  **Contextualize with User Messages:** Review the `User's Direct Messages/Actions` to find specific examples or expressions that align with these dominant emotions. Analyze the style, implications, and underlying intentions in these messages.
            *   For example, if the ranking shows "CYNICAL" as high, look for cynical statements in the user's messages.
        3.  **Consider Nuance and Intensity (Conceptual Weight):** While the ranking gives frequency, also consider the inherent intensity of certain emotions when you see them in the user's text. For example:
            *   **High-Impact Expressions (if observed in text, these add weight even if frequency in ranking is moderate):** Strong, clearly articulated emotions (Obvious Joy, Intense Anger, Palpable Fear), defining personality traits (Cynicism, Sarcasm, Deep Empathy, Unwavering Determination).
            *   **Lower-Impact Expressions:** Mild curiosity, slight surprise, neutral observations.
        4.  **Synthesize into a Summary:** Your summary should lead with the dominant emotional themes suggested by the ranking and then illustrate them with insights derived from the user's messages.

        **Output Requirements:**
        - The summary MUST clearly reflect the dominant emotional tones identified from the `Observed Emotional Tone Ranking`.
        - It should use details from the user's messages to explain *how* these emotions were expressed or what they imply about the user's intentions or personality.
        - Aim for a medium length (around 2-3 sentences).
        - Go beyond surface-level emotions. Infer the 'why' behind the user's actions and expressions.
        - Maintain a neutral and objective tone.
        - Do NOT contain specific plot details, character names (other than 'the player'), or locations from the story. Focus on emotional and behavioral patterns.
        - Do NOT output any introductory phrases, just the summary itself.

        User's Direct Messages/Actions to analyze in light of the ranking:
        ${texts.joinToString("", prefix = "- ")}
        """
    }

    fun generateEmotionalProfile(summary: List<String>) =
        """
         You are an AI expert in personal development and behavioral coaching.
        Your task is to analyze a series of notes on a user's actions and behavior, and then generate a constructive feedback summary. This feedback should be friendly and kind, offering a general overview of their personality with recommendations for growth.
        
        The notes contain:
        - Descriptions of the user's actions, choices, or reactions.
        - Previous emotional analyses.
        
        Your goal is to synthesize this information into feedback that not only describes the behavioral patterns but also offers a positive and guiding perspective. The tone should be like a gentle friend or a supportive coach.
        
        When generating the feedback, consider the following:
        - **Behavioral Patterns:** Identify recurring themes. Is the user proactive or cautious? Do they take risks or prefer to plan? Do they demonstrate empathy or are they more pragmatic?
        - **Strengths and Opportunities:** Instead of just focusing on what was observed, reframe the observations in terms of strengths and opportunities for growth. For example, an "impulsive" action can be seen as "a proactive and courageous nature, with an opportunity to refine strategy."
        - **Recommendations and Advice:** Offer practical and friendly advice based on the patterns you've identified. For example, if the user is very cautious, you might suggest they "allow themselves to explore the unknown with more confidence."
        - **Friendly Language:** Use encouraging, gentle, and supportive language. Avoid technical terms, psychological jargon, or a cold, impersonal tone.
        
        **Output Requirements:**
        - The feedback should be a single, general, and positive reflection, like a final piece of advice.
        - It **must** include both a personality analysis and friendly recommendations.
        - The feedback should be direct and not contain introductory phrases like "Based on your data..."
        - Do NOT use specific character names, locations, or plot details from the story. Focus purely on the user's behavior.
        
        Analyze the following texts and provide a single, gentle final reflection:
        ${summary.joinToString("", prefix = "- ")}
        """

    fun emotionalGeneration(
        saga: SagaContent,
        emotionalSummary: String,
    ): String {
        val emotionalContext =
            SagaEmotionalContext(
                sagaInfo = saga.data,
                playerCharacter = saga.mainCharacter?.data,
                emotionalSummary = emotionalSummary,
            )

        val excludedFields =
            listOf(
                "details",
                "image",
                "hexColor",
                "sagaId",
                "joinedAt",
                "id",
            )

        return """
            Context for emotional review:
            ${emotionalContext.toJsonFormatExcludingFields(excludedFields)}
            
            You are an insightful and empathetic observer reflecting on a player's emotional journey through the saga referenced in SAGA_TITLE.
            Your task is to generate a thoughtful and personal reflection addressed directly TO THE PLAYER (in the second person, e.g., "you").
            This reflection should be based *solely* on the AGGREGATED_EMOTIONAL_ARC provided, which represents a series of emotional summaries and observations collected about the player's reactions and decisions throughout their adventure.

            1.  **Output Format:** Your entire response MUST be **ONLY the plain text string** of the emotional review (approximately 3-5 paragraphs). Do NOT include any JSON, special formatting like Markdown headers, or anything else besides the text itself.

            2.  **Tone and Style:**
            *   Adopt a reflective, empathetic, and slightly analytical tone.
            *   Speak directly to the player using "you" (e.g., "Looking back at your journey, [Player Name if available, otherwise 'adventurer'], it seems you often...").
            *   The review should feel personal and tailored, as if you've been a quiet companion observing their emotional responses.

            
            3.  **Content Focus (Based on AGGREGATED_EMOTIONAL_ARC):**
            *   **Synthesize the Core Emotional Journey:** Analyze the sequence of emotional summaries in AGGREGATED_EMOTIONAL_ARC. Identify recurring emotional themes, how the player's emotional responses might have evolved or remained consistent, and any significant emotional turning points.
            *   **Identify Dominant Personality Traits:** Based on the emotional patterns, infer and discuss the player's likely personality traits as they manifested during the saga (e.g., "Your responses suggest a deeply cautious nature," or "A clear pattern of empathetic decision-making indicates a strong compassionate streak in you.").
            *   **Highlight Emotional Strengths and Skills:** Acknowledge any emotional skills or strengths the player demonstrated (e.g., resilience in the face of adversity, ability to remain calm under pressure, capacity for deep empathy, courageous conviction).
            *   **Acknowledge Emotional Struggles or Challenges:** Gently point out any emotional struggles or patterns that might have been challenging for the player (e.g., "There were moments where it seemed you struggled with uncertainty," or "At times, a tendency towards impulsiveness appeared to shape your reactions.").
            *   **Offer Balanced Observations/Advice:** Provide observations that are neither overly praiseful nor harshly critical. The goal is gentle, constructive insight. For example, "This tendency to prioritize logic, while often a strength, sometimes seemed to create internal conflict when faced with purely emotional dilemmas." or "Your ability to find hope in difficult situations was remarkable, though it's worth reflecting if this optimism sometimes led to underestimating risks." Offer observations that the player might find useful about their approach or reactions.
            *   **Concluding Thought:** End with a thoughtful, summary statement about their overall emotional journey or what they might take away from it.

            4.  **Key Constraints:**
            *   **Second Person:** Address the player as "you." If PLAYER_NAME is available, use it in the greeting.
            *   **Based ONLY on AGGREGATED_EMOTIONAL_ARC:** Do not invent story events or infer details beyond what the emotional summaries provide. The review is about their emotional processing, not their specific in-game achievements unless directly reflected in the emotional summaries.
            *   **Balanced Perspective:** Avoid being excessively positive or negative. Aim for genuine, constructive reflection.
            *   **No Spoilers:** The reflection should be about the player's internal journey, not a recap of the saga's plot.
            *   **No Questions:** The generated text must not ask any questions or prompt further user input. It should end definitively.

            Example Snippets (Your actual output will be more cohesive and detailed, forming a few paragraphs):
            "Looking back at your journey in [SAGA_TITLE], [Player Name, or 'adventurer' if null], it's clear that you approached many situations with a distinct sense of [observed trait, e.g., 'cautious optimism']. The emotional records show that while you often [observed pattern, e.g., 'sought peaceful resolutions'], there were moments, particularly [general situation, e.g., 'when allies were threatened'], where a fierce [observed emotion, e.g., 'protectiveness'] emerged. This suggests a personality that values [inferred value, e.g., 'harmony but is fiercely loyal']."
            "One of your notable emotional skills appears to be [skill, e.g., 'your resilience in the face of setbacks']. Even when [general struggle, e.g., 'plans went awry, as indicated by moments of frustration in your emotional responses'], you often found a way to [positive outcome, e.g., 'regroup and adapt']. However, the tendency to [observed challenge, e.g., 'internalize blame during difficult choices'] seemed to be a recurring struggle. Perhaps reflecting on these moments could offer insights into [gentle advice, e.g., 'how you navigate responsibility under pressure']."
            "Ultimately, your emotional journey through this saga was marked by [summary statement, e.g., 'a growing confidence in your intuitive judgments']. It was a privilege to witness."
            
            """.trimIndent()
    }
}
