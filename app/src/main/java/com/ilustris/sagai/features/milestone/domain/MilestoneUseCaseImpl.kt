package com.ilustris.sagai.features.milestone.domain

import android.util.Log
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.MilestonePrompts
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import javax.inject.Inject

class MilestoneUseCaseImpl
    @Inject
    constructor(
        private val gemmaClient: GemmaClient,
        private val stringResourceHelper: StringResourceHelper,
        private val genreConfigService: GenreConfigService,
        private val promptService: com.ilustris.sagai.core.ai.services.PromptService,
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

                if (milestone is SagaMilestone.Loading) error("Loading doesn't need message")

                val identity = genreConfigService.conversationBlueprint(saga.data.genre)

                val prompt =
                    MilestonePrompts.generateCongratsMessage(
                        promptService,
                        milestone,
                        saga,
                        identity,
                    ) ?: return@executeRequest getDefaultMessage(milestone, saga.data.genre)

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
                    stringResourceHelper.getString(
                        when (genre) {
                            Genre.FANTASY -> R.string.milestone_fallback_new_character_fantasy
                            Genre.CYBERPUNK -> R.string.milestone_fallback_new_character_cyberpunk
                            Genre.SPACE_OPERA -> R.string.milestone_fallback_new_character_space_opera
                            Genre.HORROR -> R.string.milestone_fallback_new_character_horror
                            Genre.COWBOY -> R.string.milestone_fallback_new_character_cowboy
                            Genre.SHINOBI -> R.string.milestone_fallback_new_character_shinobi
                            Genre.HEROES -> R.string.milestone_fallback_new_character_heroes
                            Genre.CRIME -> R.string.milestone_fallback_new_character_crime
                            Genre.PUNK_ROCK -> R.string.milestone_fallback_new_character_punk_rock
                        },
                    )
                }

                is SagaMilestone.NewEvent -> {
                    stringResourceHelper.getString(
                        when (genre) {
                            Genre.FANTASY -> R.string.milestone_fallback_new_event_fantasy
                            Genre.CYBERPUNK -> R.string.milestone_fallback_new_event_cyberpunk
                            Genre.SPACE_OPERA -> R.string.milestone_fallback_new_event_space_opera
                            Genre.HORROR -> R.string.milestone_fallback_new_event_horror
                            Genre.COWBOY -> R.string.milestone_fallback_new_event_cowboy
                            Genre.SHINOBI -> R.string.milestone_fallback_new_event_shinobi
                            Genre.HEROES -> R.string.milestone_fallback_new_event_heroes
                            Genre.CRIME -> R.string.milestone_fallback_new_event_crime
                            Genre.PUNK_ROCK -> R.string.milestone_fallback_new_event_punk_rock
                        },
                    )
                }

                is SagaMilestone.ChapterFinished -> {
                    stringResourceHelper.getString(
                        when (genre) {
                            Genre.FANTASY -> R.string.milestone_fallback_chapter_finished_fantasy
                            Genre.CYBERPUNK -> R.string.milestone_fallback_chapter_finished_cyberpunk
                            Genre.SPACE_OPERA -> R.string.milestone_fallback_chapter_finished_space_opera
                            Genre.HORROR -> R.string.milestone_fallback_chapter_finished_horror
                            Genre.COWBOY -> R.string.milestone_fallback_chapter_finished_cowboy
                            Genre.SHINOBI -> R.string.milestone_fallback_chapter_finished_shinobi
                            Genre.HEROES -> R.string.milestone_fallback_chapter_finished_heroes
                            Genre.CRIME -> R.string.milestone_fallback_chapter_finished_crime
                            Genre.PUNK_ROCK -> R.string.milestone_fallback_chapter_finished_punk_rock
                        },
                    )
                }

                is SagaMilestone.ActFinished -> {
                    stringResourceHelper.getString(
                        when (genre) {
                            Genre.FANTASY -> R.string.milestone_fallback_act_finished_fantasy
                            Genre.CYBERPUNK -> R.string.milestone_fallback_act_finished_cyberpunk
                            Genre.SPACE_OPERA -> R.string.milestone_fallback_act_finished_space_opera
                            Genre.HORROR -> R.string.milestone_fallback_act_finished_horror
                            Genre.COWBOY -> R.string.milestone_fallback_act_finished_cowboy
                            Genre.SHINOBI -> R.string.milestone_fallback_act_finished_shinobi
                            Genre.HEROES -> R.string.milestone_fallback_act_finished_heroes
                            Genre.CRIME -> R.string.milestone_fallback_act_finished_crime
                            Genre.PUNK_ROCK -> R.string.milestone_fallback_act_finished_punk_rock
                        },
                    )
                }

                is SagaMilestone.CurrentObjective -> {
                    stringResourceHelper.getString(
                        when (genre) {
                            Genre.FANTASY -> R.string.milestone_fallback_current_objective_fantasy
                            Genre.CYBERPUNK -> R.string.milestone_fallback_current_objective_cyberpunk
                            Genre.SPACE_OPERA -> R.string.milestone_fallback_current_objective_space_opera
                            Genre.HORROR -> R.string.milestone_fallback_current_objective_horror
                            Genre.COWBOY -> R.string.milestone_fallback_current_objective_cowboy
                            Genre.SHINOBI -> R.string.milestone_fallback_current_objective_shinobi
                            Genre.HEROES -> R.string.milestone_fallback_current_objective_heroes
                            Genre.CRIME -> R.string.milestone_fallback_current_objective_crime
                            Genre.PUNK_ROCK -> R.string.milestone_fallback_current_objective_punk_rock
                        },
                    )
                }

                is SagaMilestone.Introduction -> {
                    stringResourceHelper.getString(R.string.milestone_introduction_fallback_message)
                }

                is SagaMilestone.Loading -> {
                    getLoadingDefaultMessage(genre)
                }
            }

        private fun getLoadingDefaultMessage(genre: Genre): String =
            stringResourceHelper.getString(
                when (genre) {
                    Genre.FANTASY -> R.string.milestone_fallback_loading_fantasy
                    Genre.CYBERPUNK -> R.string.milestone_fallback_loading_cyberpunk
                    Genre.SPACE_OPERA -> R.string.milestone_fallback_loading_space_opera
                    Genre.HORROR -> R.string.milestone_fallback_loading_horror
                    Genre.COWBOY -> R.string.milestone_fallback_loading_cowboy
                    Genre.SHINOBI -> R.string.milestone_fallback_loading_shinobi
                    Genre.HEROES -> R.string.milestone_fallback_loading_heroes
                    Genre.CRIME -> R.string.milestone_fallback_loading_crime
                    Genre.PUNK_ROCK -> R.string.milestone_fallback_loading_punk_rock
                },
            )
    }
