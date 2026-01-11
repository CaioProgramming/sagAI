package com.ilustris.sagai.features.milestone.domain

import android.util.Log
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.MilestonePrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import javax.inject.Inject

class MilestoneUseCaseImpl
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
    ) : MilestoneUseCase {
        override suspend fun generateCongratsMessage(
            milestone: SagaMilestone,
            saga: SagaContent,
        ): RequestResult<String> =
            executeRequest {
                Log.d(
                    "MilestoneUseCase",
                    "Generating congrats message for ${milestone.javaClass.simpleName}",
                )

                val prompt = MilestonePrompts.generateCongratsMessage(milestone, saga)

                val message =
                    gemmaClient
                        .generateText(prompt)
                        .getOrElse {
                            Log.e("MilestoneUseCase", "Failed to generate message", it)
                            getDefaultMessage(milestone, saga.data.genre)
                        }

                // Trim and ensure it's not too long
                val cleanMessage = message.trim().take(150)

                Log.d("MilestoneUseCase", "Generated message: $cleanMessage")

                return@executeRequest cleanMessage
            }

        private fun getDefaultMessage(
            milestone: SagaMilestone,
            genre: Genre,
        ): String =
            when (milestone) {
                is SagaMilestone.NewCharacter -> {
                    when (genre) {
                        Genre.FANTASY -> "A new soul joins your legend!"
                        Genre.CYBERPUNK -> "New ally in the network!"
                        Genre.SPACE_OPERA -> "A new star in your crew!"
                        Genre.HORROR -> "Another enters the darkness..."
                        Genre.COWBOY -> "New rider on the trail!"
                        Genre.SHINOBI -> "A new shadow walks with you!"
                        Genre.HEROES -> "A new hero rises!"
                        Genre.CRIME -> "New player in the game!"
                        Genre.PUNK_ROCK -> "Fresh blood in the crew!"
                    }
                }

                is SagaMilestone.NewEvent -> {
                    when (genre) {
                        Genre.FANTASY -> "Your saga grows richer!"
                        Genre.CYBERPUNK -> "Data logged. Keep going!"
                        Genre.SPACE_OPERA -> "Your odyssey continues!"
                        Genre.HORROR -> "You push forward!"
                        Genre.COWBOY -> "The legend lives on!"
                        Genre.SHINOBI -> "Your path unfolds!"
                        Genre.HEROES -> "Justice prevails!"
                        Genre.CRIME -> "The case deepens!"
                        Genre.PUNK_ROCK -> "Your rebellion echoes!"
                    }
                }

                is SagaMilestone.ChapterFinished -> {
                    when (genre) {
                        Genre.FANTASY -> "A chapter closes in splendor!"
                        Genre.CYBERPUNK -> "Chapter archived. What's next?"
                        Genre.SPACE_OPERA -> "Sector cleared!"
                        Genre.HORROR -> "You survived!"
                        Genre.COWBOY -> "Trail conquered!"
                        Genre.SHINOBI -> "This path completes!"
                        Genre.HEROES -> "Chapter complete, champion!"
                        Genre.CRIME -> "Case closed!"
                        Genre.PUNK_ROCK -> "Song's over. Next track!"
                    }
                }

                is SagaMilestone.ActFinished -> {
                    when (genre) {
                        Genre.FANTASY -> "An age ends, a new era dawns!"
                        Genre.CYBERPUNK -> "Act terminated. Level up!"
                        Genre.SPACE_OPERA -> "Sector conquered!"
                        Genre.HORROR -> "You escaped the nightmare!"
                        Genre.COWBOY -> "Territory claimed!"
                        Genre.SHINOBI -> "Training complete!"
                        Genre.HEROES -> "Arc complete!"
                        Genre.CRIME -> "Operation closed!"
                        Genre.PUNK_ROCK -> "Set's done! Encore?"
                    }
                }
            }
    }
