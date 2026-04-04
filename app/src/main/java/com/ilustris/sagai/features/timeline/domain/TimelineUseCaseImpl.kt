package com.ilustris.sagai.features.timeline.domain

import android.util.Log
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.prompts.ChatPrompts
import com.ilustris.sagai.core.ai.prompts.TimelinePrompts
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.services.getNarrativeRules
import com.ilustris.sagai.core.utils.normalizetoAIItems
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.features.timeline.data.repository.TimelineRepository
import com.ilustris.sagai.features.wiki.data.usecase.EmotionalUseCase
import com.ilustris.sagai.features.wiki.data.usecase.WikiUseCase
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class TimelineUseCaseImpl
    @Inject
    constructor(
        private val timelineRepository: TimelineRepository,
        private val emotionalUseCase: EmotionalUseCase,
        private val wikiUseCase: WikiUseCase,
        private val characterUseCase: CharacterUseCase,
        private val gemmaClient: GemmaClient,
        private val promptService: PromptService,
        private val remoteConfigService: RemoteConfigService,
        private val genreConfigService: com.ilustris.sagai.core.ai.services.GenreConfigService,
    ) : TimelineUseCase {
        override suspend fun getAllTimelines() = timelineRepository.getAllTimelines()

        override suspend fun getTimeline(id: String) = timelineRepository.getTimeline(id)

        override suspend fun generateTimeline(
            saga: SagaContent,
            currentTimeline: TimelineContent,
        ) = executeRequest {
            val narrativeRules =
                remoteConfigService.getJson<NarrativeRules>("narrative_rules") ?: NarrativeRules()
            val newLore =
                gemmaClient
                    .generate<Timeline>(
                        TimelinePrompts.generateTimelinePrompt(
                            promptService = promptService,
                            narrativeRules = narrativeRules,
                            sagaContent = saga,
                            currentTimeline = currentTimeline,
                            conversationDirective = genreConfigService.conversationBlueprint(saga.data.genre),
                        ),
                        filterOutputFields =
                            listOf(
                                "id",
                                "chapterId",
                                "createdAt",
                                "currentObjective",
                            ),
                        useCore = true,
                    )!!

            updateTimeline(
                currentTimeline.data.copy(
                    title = newLore.title,
                    content = newLore.content,
                    emotionalReview = newLore.emotionalReview,
                ),
            )
        }

        override suspend fun saveTimeline(timeline: Timeline) = timelineRepository.saveTimeline(timeline)

        override suspend fun updateTimeline(timeline: Timeline): Timeline {
            timelineRepository.updateTimeline(timeline)
            return timeline
        }

        override suspend fun deleteTimeline(timeline: Timeline) {
            timelineRepository.deleteTimeline(timeline)
        }

        override suspend fun getTimelineObjective(
            saga: SagaContent,
            timelineContent: Timeline,
        ) = executeRequest {
            val objectivePrompt =
                ChatPrompts.sceneSummarizationPrompt(
                    promptService = promptService,
                    saga = saga,
                    remoteConfigService.getNarrativeRules(),
                )
            val summary =
                gemmaClient
                    .generate<SceneSummary>(
                        objectivePrompt,
                        useCore = true,
                    )!!
                    .immediateObjective!!

            updateTimeline(
                timelineContent.copy(
                    currentObjective = summary,
                ),
            )
        }

        override suspend fun generateTimelineContent(
            saga: SagaContent,
            timelineContent: TimelineContent,
        ): RequestResult<Unit> =
            executeRequest {
                if (timelineContent.characterEventDetails.isEmpty() ||
                    timelineContent.updatedRelationshipDetails.isEmpty()
                ) {
                    updateCharacters(timelineContent.data, saga)
                    delay(5.seconds)
                } else {
                    Timber.w("generateTimelineContent: Characters already updated on this event")
                }

                if (timelineContent.updatedWikis.isEmpty()) {
                    updateWikis(
                        timelineContent.data,
                        saga,
                    )
                } else {
                    Timber.w("generateTimelineContent: Wikis already updated on this event")
                }
            }

        private suspend fun updateWikis(
            timeline: Timeline,
            saga: SagaContent,
        ) = executeRequest {
            val wikisToUpdateOrAdd = wikiUseCase.generateWiki(saga, timeline).getSuccess()!!
            Log.d(javaClass.simpleName, "updateWikis: Updating wikis $wikisToUpdateOrAdd")
            wikisToUpdateOrAdd.forEach { generatedWiki ->
                val existingWiki =
                    saga.wikis.find { wiki ->
                        wiki.title
                            .trim()
                            .lowercase()
                            .contentEquals(
                                generatedWiki.title.trim().lowercase(),
                                true,
                            )
                    }
                if (existingWiki != null) {
                    Log.d(
                        javaClass.simpleName,
                        "Updating existing wiki: ${existingWiki.title} (ID: ${existingWiki.id}) for saga ${saga.data.id}",
                    )
                    wikiUseCase.updateWiki(
                        generatedWiki.copy(
                            id = existingWiki.id,
                            sagaId = saga.data.id,
                            timelineId = timeline.id,
                        ),
                    )
                } else {
                    Log.d(
                        javaClass.simpleName,
                        "Saving new wiki: ${generatedWiki.title} for saga ${saga.data.id}",
                    )
                    wikiUseCase.saveWiki(
                        generatedWiki.copy(
                            sagaId = saga.data.id,
                            timelineId = timeline.id,
                        ),
                    )
                }
            }
            if (wikisToUpdateOrAdd.isEmpty()) {
                Log.i(
                    javaClass.simpleName,
                    "updateWikis: No wiki updates generated for recnt events in saga ${saga.data.id}.",
                )
            }
        }

        private suspend fun updateCharacters(
            timeline: Timeline,
            saga: SagaContent,
        ) = executeRequest {
            characterUseCase.generateCharactersUpdate(timeline, saga)

            characterUseCase.generateCharacterRelations(timeline, saga)
        }
    }
