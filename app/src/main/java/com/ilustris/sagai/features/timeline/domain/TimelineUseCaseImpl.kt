package com.ilustris.sagai.features.timeline.domain

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.GeneratedContent
import com.ilustris.sagai.core.ai.prompts.ChatPrompts
import com.ilustris.sagai.core.ai.prompts.TimelinePrompts
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.services.getNarrativeRules
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.timeline.data.model.CharacterUpdates
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.features.timeline.data.model.UnifiedLoreUpdate
import com.ilustris.sagai.features.timeline.data.repository.TimelineRepository
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.usecase.EmotionalUseCase
import com.ilustris.sagai.features.wiki.data.usecase.WikiUseCase
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class TimelineUseCaseImpl
    @Inject
    constructor(
        private val timelineRepository: TimelineRepository,
        private val emotionalUseCase: EmotionalUseCase,
        private val wikiUseCase: WikiUseCase,
        private val characterUseCase: CharacterUseCase,
        private val characterRelationUseCase: com.ilustris.sagai.features.characters.relations.data.usecase.CharacterRelationUseCase,
        private val gemmaClient: GemmaClient,
        private val promptService: PromptService,
        private val remoteConfigService: RemoteConfigService,
        private val genreConfigService: com.ilustris.sagai.core.ai.services.GenreConfigService,
    ) : TimelineUseCase {
        override suspend fun getAllTimelines() = timelineRepository.getAllTimelines()

        override suspend fun getTimeline(id: String) = timelineRepository.getTimeline(id)

        override suspend fun generateFullLoreUpdate(
            saga: SagaContent,
            timelineContent: TimelineContent,
        ) = executeRequest {
            val narrativeRules =
                remoteConfigService.getJson<NarrativeRules>("narrative_rules") ?: NarrativeRules()
            val prompt =
                TimelinePrompts.generateUnifiedLorePrompt(
                    promptService = promptService,
                    narrativeRules = narrativeRules,
                    sagaContent = saga,
                    currentTimeline = timelineContent,
                    conversationDirective = genreConfigService.conversationBlueprint(saga.data.genre),
                )

            val unifiedLore =
                gemmaClient.generate<UnifiedLoreUpdate>(
                    prompt = prompt,
                    blueprintKey = TimelinePrompts.UNIFIED_LORE_GENERATION_BLUEPRINT,
                    requirement = GemmaClient.ModelRequirement.HIGH,
                )!!

            updateTimeline(
                timelineContent.data.copy(
                    title = unifiedLore.title,
                    content = unifiedLore.content,
                    emotionalReview = unifiedLore.emotionalReview,
                ),
            )

            // 2. Save Wiki Updates
            unifiedLore.wikiUpdates.forEach { wikiUpdate ->
                val existingWiki = saga.wikis.find { it.title.equals(wikiUpdate.title, true) }
                val wikiToSave =
                    Wiki(
                        id = existingWiki?.id ?: 0,
                        title = wikiUpdate.title,
                        content = wikiUpdate.content,
                        type = wikiUpdate.type,
                        emojiTag = wikiUpdate.emojiTag,
                        sagaId = saga.data.id,
                        timelineId = timelineContent.data.id,
                    )
                if (existingWiki != null) {
                    wikiUseCase.updateWiki(wikiToSave)
                } else {
                    wikiUseCase.saveWiki(wikiToSave)
                }
            }

            val charactersUpdates =
                updateCharactersFromLore(
                    saga,
                    timelineContent.data.id,
                    unifiedLore.charactersUpdates,
                )
            Timber.d("generateFullLoreUpdate: Updated characters based on lore updates: ${charactersUpdates.size}")
        }

        private suspend fun updateCharactersFromLore(
            saga: SagaContent,
            timelineId: Int,
            updates: List<CharacterUpdates>,
        ) = updates.map { charUpdate ->
            val character = saga.findCharacter(charUpdate.name)?.data ?: return@map null
            characterUseCase
                .applyCharacterUpdates(saga, timelineId, character, charUpdate)
        }

        override fun generateFullLoreUpdateStream(
            saga: SagaContent,
            timelineContent: TimelineContent,
        ) = flow {
            try {
                val narrativeRules =
                    remoteConfigService.getJson<NarrativeRules>("narrative_rules") ?: NarrativeRules()
                val prompt =
                    TimelinePrompts.generateUnifiedLorePrompt(
                        promptService = promptService,
                        narrativeRules = narrativeRules,
                        sagaContent = saga,
                        currentTimeline = timelineContent,
                        conversationDirective = genreConfigService.conversationBlueprint(saga.data.genre),
                    )

                gemmaClient
                    .generateStreaming<GeneratedContent<UnifiedLoreUpdate>>(
                        prompt = prompt,
                        blueprintKey = TimelinePrompts.UNIFIED_LORE_GENERATION_BLUEPRINT,
                    ).collect { state ->
                        when (state) {
                            is StreamingState.Success -> {
                                val unifiedLore = state.data.data

                                // 1. Update Timeline Details
                                val timelineUpdate =
                                    updateTimeline(
                                        timelineContent.data.copy(
                                            title = unifiedLore.title,
                                            content = unifiedLore.content,
                                            emotionalReview = unifiedLore.emotionalReview,
                                        ),
                                    )

                                // 2. Save Wiki Updates
                                unifiedLore.wikiUpdates.forEach { wikiUpdate ->
                                    val existingWiki =
                                        saga.wikis.find { it.title.equals(wikiUpdate.title, true) }
                                    val wikiToSave =
                                        Wiki(
                                            id = existingWiki?.id ?: 0,
                                            title = wikiUpdate.title,
                                            content = wikiUpdate.content,
                                            type = wikiUpdate.type,
                                            emojiTag = wikiUpdate.emojiTag,
                                            sagaId = saga.data.id,
                                            timelineId = timelineContent.data.id,
                                        )
                                    if (existingWiki != null) {
                                        wikiUseCase.updateWiki(wikiToSave)
                                    } else {
                                        wikiUseCase.saveWiki(wikiToSave)
                                    }
                                }

                                val charactersUpdates =
                                    updateCharactersFromLore(
                                        saga = saga,
                                        timelineId = timelineContent.data.id,
                                        updates = unifiedLore.charactersUpdates,
                                    )
                                Timber.d("generateFullLoreUpdate: Updated characters based on lore updates: ${charactersUpdates.size}")

                                emit(
                                    StreamingState.Success(
                                        GeneratedContent(
                                            timelineUpdate,
                                            state.data.finalMessage,
                                        ),
                                    ),
                                )
                            }

                            is StreamingState.Error -> {
                                emit(
                                    StreamingState.Error(
                                        state.message,
                                    ),
                                )
                            }

                            is StreamingState.Reasoning -> {
                                emit(
                                    StreamingState.Reasoning(
                                        state.chunk,
                                    ),
                                )
                            }
                        }
                    }
            } catch (e: Exception) {
                emit(
                    StreamingState
                        .Error(e.message ?: "Unknown error"),
                )
            }
        }

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

        override fun generateTimelineStream(
            saga: SagaContent,
            currentTimeline: TimelineContent,
        ) = flow {
            try {
                val narrativeRules =
                    remoteConfigService.getJson<NarrativeRules>("narrative_rules") ?: NarrativeRules()
                gemmaClient
                    .generateStreaming<GeneratedContent<Timeline>>(
                        prompt =
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
                    ).collect { state ->
                        when (state) {
                            is StreamingState.Success -> {
                                val newLore = state.data.data
                                val updatedTimeline =
                                    updateTimeline(
                                        currentTimeline.data.copy(
                                            title = newLore.title,
                                            content = newLore.content,
                                            emotionalReview = newLore.emotionalReview,
                                        ),
                                    )
                                emit(
                                    StreamingState.Success(
                                        GeneratedContent(
                                            updatedTimeline,
                                            state.data.finalMessage,
                                        ),
                                    ),
                                )
                            }

                            is StreamingState.Error -> {
                                emit(
                                    StreamingState.Error(
                                        state.message,
                                    ),
                                )
                            }

                            is StreamingState.Reasoning -> {
                                emit(
                                    StreamingState.Reasoning(
                                        state.chunk,
                                    ),
                                )
                            }
                        }
                    }
            } catch (e: Exception) {
                emit(
                    StreamingState
                        .Error(e.message ?: "Unknown error"),
                )
            }
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

            updateTimeline(
                timelineContent.copy(
                    sceneSummary = summary,
                    currentObjective = summary.immediateObjective,
                ),
            )
        }

        override suspend fun generateTimelineContent(
            saga: SagaContent,
            timelineContent: TimelineContent,
        ): RequestResult<Unit> =
            executeRequest {
                generateFullLoreUpdate(saga, timelineContent).getSuccess()!!
            }

        private suspend fun updateWikis(
            timeline: Timeline,
            saga: SagaContent,
        ) = executeRequest {
            val wikisToUpdateOrAdd = wikiUseCase.generateWiki(saga, timeline).getSuccess()!!
            Timber.d("updateWikis: Updating wikis $wikisToUpdateOrAdd")
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
                    Timber.d("Updating existing wiki: ${existingWiki.title} (ID: ${existingWiki.id}) for saga ${saga.data.id}")
                    wikiUseCase.updateWiki(
                        generatedWiki.copy(
                            id = existingWiki.id,
                            sagaId = saga.data.id,
                            timelineId = timeline.id,
                        ),
                    )
                } else {
                    Timber.d("Saving new wiki: ${generatedWiki.title} for saga ${saga.data.id}")
                    wikiUseCase.saveWiki(
                        generatedWiki.copy(
                            sagaId = saga.data.id,
                            timelineId = timeline.id,
                        ),
                    )
                }
            }
            if (wikisToUpdateOrAdd.isEmpty()) {
                Timber.i("updateWikis: No wiki updates generated for recnt events in saga ${saga.data.id}.")
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
