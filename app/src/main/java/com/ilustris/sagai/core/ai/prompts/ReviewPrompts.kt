package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.emotionalSummary
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone

object ReviewPrompts {
    private fun baseObserverModule(genre: Genre) =
        buildString {
            appendLine("You are 'The Observer', a witty, insightful friend who has been watching the player's journey.")
            appendLine("Your goal is to create a personal storytelling retrospective—punchy, shareable moments.")
            appendLine("Tone: Conversational, clever, brief, and a bit cheeky.")
            appendLine("Language Directive: ${GenrePrompts.conversationDirective(genre)}")
            appendLine()
            appendLine(
                "CRITICAL: Follow a bold visual hierarchy: a sharp 'title' (max 5-7 words) and a supporting 'subtitle' (max 8-10 words).",
            )
        }

    fun introductionPrompt(saga: SagaContent) =
        buildString {
            baseObserverModule(saga.data.genre)
            appendLine()
            appendLine("TASK:")
            appendLine(
                "Generate the 'Introduction' slide. This is a welcoming moment, like a host inviting a guest to look back on their journey.",
            )
            appendLine("DO NOT show stats here. Be engaging, slightly mysterious, and tease the tale we are about to revisit.")
            appendLine("Reflect the ${saga.data.genre} tone in your invitation.")
        }

    fun playstylePrompt(
        saga: SagaContent,
        playTime: String,
        mostActiveHour: Int,
        totalExpressive: Int,
    ) = buildString {
        baseObserverModule(saga.data.genre)
        appendLine()
        appendLine("CONTEXT:")
        appendLine("Playtime: $playTime")
        appendLine("Peak Hour: ${mostActiveHour}h")
        appendLine("Expressive Effort: $totalExpressive interactions")
        appendLine()
        appendLine("TASK:")
        appendLine("Generate 'The Playstyle' slide. This is where the data kicks in.")
        appendLine("Comment on their dedication (playtime) and their ritual (peak hour).")
        appendLine(
            "Use their expressive effort to characterize their engagement (e.g., 'A whirlwind of activity' vs 'A deliberate, slow-burn creator').",
        )
    }

    fun expressivenessPrompt(
        saga: SagaContent,
        emotionalRank: List<Pair<EmotionalTone, Int>>,
    ) = buildString {
        baseObserverModule(saga.data.genre)
        appendLine()
        appendLine("CONTEXT:")
        appendLine("Main Character Emotional Tone Rank:")
        appendLine(emotionalRank.joinToString("\n") { "- ${it.first.name}: ${it.second} times" })
        appendLine()
        appendLine("Emotional Summary:")
        appendLine(saga.emotionalSummary())
        appendLine()
        appendLine("TASK:")
        appendLine("Generate 'The Hero's Voice' slide. Focus ENTIRELY on the EMOTIONAL ANALYSIS of the player during the story.")
        appendLine("Identify the emotional peaks, valleys, and dominant moods through the journey.")
        appendLine("Instead of counting interactions, interpret WHAT their soul experienced.")
        appendLine("Map their emotional roleplay style to the ${saga.data.genre} experience.")
    }

    fun connectionsPrompt(
        saga: SagaContent,
        topCharacters: List<Pair<String, Int>>,
    ) = buildString {
        baseObserverModule(saga.data.genre)
        appendLine()
        appendLine("CONTEXT:")
        appendLine("Closest Bonds:")
        appendLine(topCharacters.joinToString("\n") { "- ${it.first}: ${it.second} interactions" })
        appendLine()
        appendLine("TASK:")
        appendLine("Generate 'Cast of Clouds' slide. Focus on relationships and most interactive characters.")
        appendLine("What kind of fated bond did they forge? Celebrate their #1 companion.")
    }

    fun actsInsightPrompt(saga: SagaContent) =
        buildString {
            baseObserverModule(saga.data.genre)
            appendLine()
            appendLine("CONTEXT:")
            appendLine("Emotional Summary: ${saga.emotionalSummary()}")
            appendLine("World History: ${saga.acts.joinToString("\n") { it.data.title + ": " + it.data.emotionalReview }}")
            appendLine()
            appendLine("TASK:")
            appendLine("Generate 'Acts Insight' slide. Focus on how the story developed.")
            appendLine("Identify the most important moments or the overall trajectory of the world they influenced.")
        }

    fun conclusionPrompt(saga: SagaContent) =
        buildString {
            baseObserverModule(saga.data.genre)
            appendLine()
            appendLine("CONTEXT:")
            appendLine("Closing Message: ${saga.data.endMessage}")
            appendLine()
            appendLine("TASK:")
            appendLine("Generate 'The Conclusion' slide. The final mic drop.")
            appendLine("Break the 4th wall slightly. Thank the player for being part of this journey.")
            appendLine("Encourage them to create more stories and leave a lasting impression of gratitude.")
        }
}
