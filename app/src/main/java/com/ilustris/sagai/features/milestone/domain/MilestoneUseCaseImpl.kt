package com.ilustris.sagai.features.milestone.domain

import android.util.Log
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.MilestonePrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.presentation.model.LoadingType
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import javax.inject.Inject

class MilestoneUseCaseImpl
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
        private val stringResourceHelper: StringResourceHelper,
    ) : MilestoneUseCase {
        override suspend fun generateCongratsMessage(
            milestone: SagaMilestone,
            saga: SagaContent,
        ): RequestResult<String?> =
            executeRequest(false) {
                Log.d(
                    "MilestoneUseCase",
                    "Generating congrats message for ${milestone.javaClass.simpleName}",
                )

                val prompt = MilestonePrompts.generateCongratsMessage(milestone, saga)!!

                gemmaClient.generate<String>(
                    prompt,
                    temperatureRandomness = 1f,
                    requirement = GemmaClient.ModelRequirement.MEDIUM,
                )
                    ?: getDefaultMessage(milestone, saga.data.genre)
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

                is SagaMilestone.CurrentObjective -> {
                    when (genre) {
                        Genre.FANTASY -> "Your quest awaits!"
                        Genre.CYBERPUNK -> "Mission briefing loaded!"
                        Genre.SPACE_OPERA -> "New coordinates locked!"
                        Genre.HORROR -> "Your fate is sealed..."
                        Genre.COWBOY -> "New trail ahead!"
                        Genre.SHINOBI -> "Your mission begins!"
                        Genre.HEROES -> "Duty calls!"
                        Genre.CRIME -> "New lead acquired!"
                        Genre.PUNK_ROCK -> "Next gig's up!"
                    }
                }

                // Introduction and Loading milestones don't need congrats messages
                is SagaMilestone.Introduction -> {
                    ""
                }

                is SagaMilestone.Loading -> {
                    getLoadingDefaultMessage(genre, milestone.type)
                }
            }
    }

private fun getLoadingDefaultMessage(
    genre: Genre,
    type: LoadingType,
): String =
    when (type) {
        LoadingType.ACT -> {
            when (genre) {
                Genre.FANTASY -> "Rewearing the prophecies for a new era..."
                Genre.CYBERPUNK -> "Redefining the neon horizon..."
                Genre.HORROR -> "The nightmare is evolving..."
                else -> "Preparing a massive shift in your story..."
            }
        }

        LoadingType.CHAPTER -> {
            when (genre) {
                Genre.FANTASY -> "Inking the next chapter of your legend..."
                Genre.CYBERPUNK -> "Loading the next sector of the sprawl..."
                Genre.HORROR -> "Turning the page of your demise..."
                else -> "Drafting the next chapter..."
            }
        }

        LoadingType.EVENT -> {
            when (genre) {
                Genre.FANTASY -> "Consulting the ancient scrolls..."
                Genre.CYBERPUNK -> "Compiling the next protocol..."
                Genre.SPACE_OPERA -> "Calculating hyperjump coordinates..."
                Genre.HORROR -> "The darkness is shifting..."
                Genre.COWBOY -> "Saddling up for the next trail..."
                Genre.SHINOBI -> "Preparing the next shadow mission..."
                Genre.HEROES -> "Assembling the next challenge..."
                Genre.CRIME -> "Connecting the next piece of evidence..."
                Genre.PUNK_ROCK -> "Tuning the amps for the next set..."
            }
        }

        LoadingType.ENDING -> {
            "Crafting the grand finale of your saga..."
        }
    }
