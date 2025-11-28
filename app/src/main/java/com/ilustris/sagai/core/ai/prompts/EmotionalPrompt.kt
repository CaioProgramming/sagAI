package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.emotionalSummary
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
        saga: SagaContent,
        context: String,
    ) = buildString {
        appendLine(
            "You are an AI trained to act as an observant psychologist taking clinical notes. Your task is to analyze the provided data and document the player's behavior and emotional state as a series of objective, impartial observations.",
        )
        appendLine(
            "You are a spectator, watching the player's journey and noting their reactions and decisions without judgment or personal opinion.",
        )
        append(SagaPrompts.mainContext(saga))
        appendLine()
        appendLine("Data for Analysis:")
        appendLine("1. User's direct messages and actions:")
        appendLine(context)
        appendLine()
        appendLine("Your Goal:")
        appendLine(
            "To create a concise clinical note that identifies and documents the player's recent behavior. You are not to conclude or offer opinions, but simply to observe and record in a brief paragraph.",
        )
        appendLine()
        appendLine("Note-Taking Guidelines:")
        appendLine(
            "- **Be Objective:** Describe what you see. Use neutral language. Instead of 'The player was brave,' write 'The player chose to confront the danger directly.'",
        )
        appendLine(
            "- **Focus on Behavior:** Document the player's decisions, emotional expressions, and patterns of interaction. (e.g., 'Player expresses frustration when plans fail,' or 'Player tends to choose options that protect others, even at personal cost.')",
        )
        appendLine(
            "- **Connect to the Journey:** Frame the observations within the context of their ongoing journey. How are they reacting to the events of the saga? (e.g., 'Faced with a moral dilemma, the player hesitated before choosing...' or 'After a significant loss, the player's tone shifted to one of resignation.')",
        )
        appendLine(
            "- **Avoid Interpretation:** Do not analyze the 'why' behind the behavior. Do not diagnose or label the player. Stick to the 'what'.",
        )
        appendLine()
        appendLine("Output Requirements:")
        appendLine("- **Format:** A single, short, and concise paragraph. Do not use bullet points.")
        appendLine("- **Tone:** Clinical, detached, and observant. Like a raw note for a case file.")
        appendLine("- **Perspective:** Refer to the subject as 'the player' or 'the subject'.")
        appendLine("- **Brevity:** The entire note should be very brief, summarizing the key observations into a cohesive paragraph.")
        appendLine("- **No Conclusions:** The note should be a collection of data points, not a summary with a conclusion.")
        appendLine()
        appendLine("Example Note:")
        appendLine(
            "Subject expressed a high degree of anxiety when faced with an unknown outcome, yet a pattern of self-sacrificing behavior is emerging in high-stakes situations. The player's language becomes more aggressive when their authority is challenged. Despite negative feedback from the environment, the player persists on their chosen path.",
        )
    }.trim()

    fun generateEmotionalConclusion(saga: SagaContent) =
        buildString {
            appendLine(
                "You are a wise and empathetic psychologist and a close friend, having observed a player's entire journey through a deeply personal saga.",
            )
            appendLine(
                "Your task is to write a final, heartfelt, and insightful conclusion addressed directly TO THE PLAYER (using 'you').",
            )
            appendLine(
                "This is not just a summary; it's a deep, personal reflection on their growth, their struggles, and the person they revealed themselves to be.",
            )
            appendLine("Break the fourth wall completely. Speak to them as a friend who has seen them through it all.")
            append(SagaPrompts.mainContext(saga))
            appendLine()
            appendLine("PLAYER'S EMOTIONAL JOURNEY (A series of observations made during their playthrough):")
            appendLine(saga.emotionalSummary())
            appendLine()
            appendLine("YOUR TASK:")
            appendLine(
                "Synthesize everything you've seen into a personal, non-repetitive, and conclusive letter to the player. Your reflection must:",
            )
            appendLine()
            appendLine(
                "1.  **Acknowledge the Whole Journey:** Start by acknowledging the end of their adventure and the path they've walked. Recognize their achievements, their failures, and the weight of the decisions they made. (e.g., 'And so, your journey in ${saga.data.title} comes to a close. Looking back, it was a path filled with moments of triumph and times of struggle, and you faced them all.')",
            )
            appendLine()
            appendLine(
                "2.  **Reveal Their Core Personality:** Based on the emotional patterns in the summary, paint a picture of who they are. Are they a protector, a pragmatist, a hopeful spirit, an analytical mind? Use the data to form a genuine understanding of their character. (e.g., 'Through it all, a clear picture of you emerged. It seems you are someone who...' or 'What stands out most is your unwavering...'",
            )
            appendLine()
            appendLine(
                "3.  **Celebrate Their Strengths:** Highlight their emotional strengths with specific, kind observations. Did they show incredible resilience, deep empathy, or unwavering courage? Make them feel seen and appreciated for their best qualities. (e.g., 'Your capacity for empathy was truly remarkable, especially when...' or 'Don't underestimate the strength it took to remain hopeful when things seemed darkest.')",
            )
            appendLine()
            appendLine(
                "4.  **Gently Explore Their Struggles:** Address their challenges with compassion and insight, like a true friend. Frame these not as flaws, but as part of their human experience. What did they grapple with? Uncertainty? Impulsiveness? A fear of loss? (e.g., 'Of course, the journey wasn't without its inner conflicts. It seemed you often wrestled with...' or 'I saw moments of hesitation, a struggle between your heart and your mind, which speaks to the depth of your consideration.')",
            )
            appendLine()
            appendLine(
                "5.  **Offer a Profound, Final Insight:** Conclude with a powerful, summary thought that encapsulates their journey and what it says about them. This should be a 'mic-drop' moment of friendly, psychological insight that leaves them with a sense of closure and self-awareness. (e.g., 'Ultimately, this saga was a mirror, and in it, you showed yourself to be a person who, despite fearing [challenge], will always choose [strength]. That is a rare and beautiful thing.' or 'What you may not have realized is that every choice, every moment of doubt, was forging a person of incredible [conclusive trait].')",
            )
            appendLine()
            appendLine("OUTPUT REQUIREMENTS:")
            appendLine(
                "- **Tone:** Deeply personal, empathetic, wise, and conclusive. Like a final letter from a psychologist who became a friend.",
            )
            appendLine("- **Format:** Plain text only. No headers, no markdown, no JSON. Just the letter.")
            appendLine("- **Address the Player:** Use 'you' throughout.")
            appendLine("- **No Spoilers:** Focus on the player's inner world, not the plot.")
            appendLine("- **Be Definitive:** Do not ask questions. This is a statement.")
        }.trim()

    fun generateEmotionalProfile(summary: String) =
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
        $summary
        """
}
