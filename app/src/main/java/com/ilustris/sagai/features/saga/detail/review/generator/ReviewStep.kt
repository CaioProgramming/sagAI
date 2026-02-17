package com.ilustris.sagai.features.saga.detail.review.generator

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.model.GenreConfig
import com.ilustris.sagai.core.ai.prompts.ReviewPrompts
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.rankByHour
import com.ilustris.sagai.features.home.data.model.rankMainCharacterEmotionalTones
import com.ilustris.sagai.features.saga.chat.domain.model.rankTopCharacters
import com.ilustris.sagai.features.saga.detail.data.model.Review
import com.ilustris.sagai.features.saga.detail.data.model.ReviewStage

interface ReviewStep {
    val progressMessage: String

    suspend fun generate(
        saga: SagaContent,
        currentReview: Review,
        client: GemmaClient,
        config: GenreConfig?,
    ): Review
}

class IntroStep : ReviewStep {
    override val progressMessage = "Pulling up the best bits..."

    override suspend fun generate(
        saga: SagaContent,
        currentReview: Review,
        client: GemmaClient,
        config: GenreConfig?,
    ): Review {
        val prompt = ReviewPrompts.introductionPrompt(saga, config)
        val stage =
            client.generate<ReviewStage>(prompt, requirement = GemmaClient.ModelRequirement.HIGH)
        return currentReview.copy(introduction = stage)
    }
}

class ExpressivenessStep : ReviewStep {
    override val progressMessage = "Checking out your mood swings..."

    override suspend fun generate(
        saga: SagaContent,
        currentReview: Review,
        client: GemmaClient,
        config: GenreConfig?,
    ): Review {
        val emotionalRank = saga.rankMainCharacterEmotionalTones()
        val prompt = ReviewPrompts.expressivenessPrompt(saga, config, emotionalRank)
        val stage =
            client.generate<ReviewStage>(prompt, requirement = GemmaClient.ModelRequirement.HIGH)
        return currentReview.copy(expressiveness = stage)
    }
}

class PlaystyleStep : ReviewStep {
    override val progressMessage = "Looking at when you skipped sleep..."

    override suspend fun generate(
        saga: SagaContent,
        currentReview: Review,
        client: GemmaClient,
        config: GenreConfig?,
    ): Review {
        val mainCharId = saga.data.mainCharacterId
        val playerMessages = saga.flatMessages().filter { it.message.characterId == mainCharId }

        val actionCount = playerMessages.count { it.message.text.contains("<action>") }
        val thinkCount = playerMessages.count { it.message.text.contains("<think>") }
        val narratorCount = playerMessages.count { it.message.text.contains("<narrator>") }
        val totalExpressive = actionCount + thinkCount + narratorCount

        val playTime = saga.data.playTimeMs.formatDuration()
        val mostActiveHour = saga.rankByHour().maxByOrNull { it.value.size }?.key ?: 0

        val prompt =
            ReviewPrompts.playstylePrompt(saga, config, playTime, mostActiveHour, totalExpressive)
        val stage =
            client.generate<ReviewStage>(prompt, requirement = GemmaClient.ModelRequirement.HIGH)
        return currentReview.copy(playstyle = stage)
    }

    private fun Long.formatDuration(): String {
        val minutes = this / 60000
        val hours = minutes / 60
        return if (hours > 0) "${hours}h ${minutes % 60}m" else "${minutes}m"
    }
}

class CharactersStep : ReviewStep {
    override val progressMessage = "Seeing who you hung out with..."

    override suspend fun generate(
        saga: SagaContent,
        currentReview: Review,
        client: GemmaClient,
        config: GenreConfig?,
    ): Review {
        val topCharacters =
            saga
                .flatMessages()
                .rankTopCharacters(
                    saga.characters
                        .filter { it != saga.mainCharacter }
                        .map { it.data },
                ).take(3)
                .map { it.first.name to it.second }

        val prompt = ReviewPrompts.connectionsPrompt(saga, config, topCharacters)
        val stage =
            client.generate<ReviewStage>(prompt, requirement = GemmaClient.ModelRequirement.HIGH)
        return currentReview.copy(topCharacters = stage)
    }
}

class JourneyStep : ReviewStep {
    override val progressMessage = "Recalling the chaos we made..."

    override suspend fun generate(
        saga: SagaContent,
        currentReview: Review,
        client: GemmaClient,
        config: GenreConfig?,
    ): Review {
        val prompt = ReviewPrompts.actsInsightPrompt(saga, config)
        val stage =
            client.generate<ReviewStage>(prompt, requirement = GemmaClient.ModelRequirement.HIGH)
        return currentReview.copy(actsInsight = stage)
    }
}

class ConclusionStep : ReviewStep {
    override val progressMessage = "One last toast to our story..."

    override suspend fun generate(
        saga: SagaContent,
        currentReview: Review,
        client: GemmaClient,
        config: GenreConfig?,
    ): Review {
        val prompt = ReviewPrompts.conclusionPrompt(saga, config)
        val stage =
            client.generate<ReviewStage>(prompt, requirement = GemmaClient.ModelRequirement.HIGH)
        return currentReview.copy(conclusion = stage)
    }
}
