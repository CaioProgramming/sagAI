package com.ilustris.sagai.core.ai.prompts

object EmotionalPrompt {
    fun generateEmotionalReview(texts: List<String>) =
        """
        You are an AI expert in behavioral analysis and emotional intelligence.
        Your task is to analyze a list of textual inputs and generate a thoughtful summary of the overall emotional behavior, prevailing sentiment, and underlying intentions expressed.

        The input list contains text snippets. These snippets might be:
        - Direct statements, decisions, or actions from a user/player.
        - Previously generated emotional summaries from earlier stages of analysis.

        Your goal is to distill these inputs into a thoughtful, medium-length summary (around 2-3 sentences) that not only captures the dominant emotional tone and behavioral pattern but also offers insights into the user's underlying personality traits and intentions.

        Consider the following when generating your summary:
        - **If analyzing direct user messages/actions:** Focus on the underlying emotions (e.g., curiosity, fear, determination, joy, anger), expressed intentions, and the general approach (e.g., cautious, aggressive, investigative, hesitant, strategic, empathetic, reckless). Try to infer the personality traits these actions might suggest. Crucially, analyze the *style* and *implication* of these actions, not the specific plot details. For example, instead of 'The user bravely fought the dragon to save the village', focus on 'The user demonstrates a proactive and protective nature, willing to confront significant challenges for a perceived greater good.'
        - **If analyzing existing emotional summaries:** Synthesize these summaries into a more consolidated and refined overarching emotional behavior. Identify the most prominent or consistent emotional themes and how they manifest as a general behavioral pattern, looking for deeper intentions or personality indicators. Ensure the synthesis focuses on abstracting the core emotional trajectory, removing any lingering story-specific examples from the input summaries.

        **Output Requirements:**
        - The summary should be insightful and descriptive, aiming for a medium length (around 2-3 sentences). For example: 'The user appears to be driven by a strong sense of justice, often taking bold actions even when risky, suggesting a heroic and perhaps slightly impulsive personality.' or 'A pattern of careful questioning and information gathering emerges, indicating a cautious and analytical approach, possibly stemming from a desire to avoid unnecessary conflict.'
        - Go beyond surface-level emotions. Attempt to infer the 'why' behind the actions and emotional expressions, connecting them to potential personality traits (e.g., brave, curious, pragmatic, empathetic, suspicious) and deeper intentions (e.g., seeking knowledge, protecting others, achieving a specific goal, testing boundaries).
        - It should reflect a behavioral pattern or an emotional state that drives actions, and the likely personality traits or motivations behind them.
        - Aim for a neutral and objective tone in your summary, describing the observed or inferred behavior and personality.
        - The summary must NOT contain specific details, character names (other than 'the player'), locations, or plot points from the story. It should be a pure analysis of emotional and behavioral patterns.
        - Do NOT output any introductory phrases, just the summary itself.

        Analyze the following texts and provide the single, insightful emotional behavior summary:
        ${texts.joinToString("", prefix = "- ")}
        """
}
