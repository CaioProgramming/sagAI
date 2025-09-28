package com.ilustris.sagai.core.ai.prompts

object EmotionalPrompt {
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
}
