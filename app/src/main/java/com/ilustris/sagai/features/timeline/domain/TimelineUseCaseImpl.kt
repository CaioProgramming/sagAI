package com.ilustris.sagai.features.timeline.domain

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.GeneratedContent
import com.ilustris.sagai.core.ai.prompts.ChatPrompts
import com.ilustris.sagai.core.ai.prompts.TimelinePrompts
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.ai.services.ReasoningSynthesizerService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.services.getNarrativeRules
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.characters.relations.data.usecase.CharacterRelationUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.home.data.model.findTimeline
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.timeline.data.model.CharacterUpdates
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.features.timeline.data.model.TimelineWithAct
import com.ilustris.sagai.features.timeline.data.model.UnifiedLoreUpdate
import com.ilustris.sagai.features.timeline.data.repository.TimelineRepository
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.usecase.EmotionalUseCase
import com.ilustris.sagai.features.wiki.data.usecase.WikiUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
        private val characterRelationUseCase: CharacterRelationUseCase,
        private val sagaHistoryUseCase: com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase,
        private val gemmaClient: GemmaClient,
        private val promptService: PromptService,
        private val remoteConfigService: RemoteConfigService,
        private val genreConfigService: GenreConfigService,
        private val reasoningSynthesizerService: ReasoningSynthesizerService,
    ) : TimelineUseCase {
        override suspend fun getAllTimelines() = timelineRepository.getAllTimelines()

        override suspend fun getTimeline(id: String) = timelineRepository.getTimeline(id)

        override suspend fun generateFullLoreUpdate(
            saga: SagaMetadata,
            timeline: Timeline,
        ) = executeRequest {
            val narrativeRules =
                remoteConfigService.getJson<NarrativeRules>("narrative_rules") ?: NarrativeRules()
            val fullSaga = sagaHistoryUseCase.getSagaById(saga.data.id).first()!!
            val prompt =
                TimelinePrompts.generateUnifiedLorePrompt(
                    promptService = promptService,
                    narrativeRules = narrativeRules,
                    sagaContent = fullSaga,
                    currentTimeline = fullSaga.findTimeline(timeline.id)!!,
                    conversationDirective = genreConfigService.conversationBlueprint(saga.data.genre),
                )

            val unifiedLore =
                gemmaClient.generate<UnifiedLoreUpdate>(
                    prompt = prompt,
                    blueprintKey = TimelinePrompts.UNIFIED_LORE_GENERATION_BLUEPRINT,
                    requirement = GemmaClient.ModelRequirement.HIGH,
                )!!

            updateTimeline(
                unifiedLore.event.copy(
                    id = timeline.id,
                ),
            )

            // 2. Save Wiki Updates
            unifiedLore.wikiUpdates.forEach { wikiUpdate ->
                val existingWiki = fullSaga.wikis.find { it.title.equals(wikiUpdate.title, true) }
                val wikiToSave =
                    Wiki(
                        id = existingWiki?.id ?: 0,
                        title = wikiUpdate.title,
                        content = wikiUpdate.content,
                        type = wikiUpdate.type,
                        emojiTag = wikiUpdate.emojiTag,
                        sagaId = saga.data.id,
                        timelineId = timeline.id,
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
                    timeline.id,
                    unifiedLore.charactersUpdates,
                )
            Timber.d("generateFullLoreUpdate: Updated characters based on lore updates: ${charactersUpdates.size}")
        }

        private suspend fun updateCharactersFromLore(
            saga: SagaMetadata,
            timelineId: Int,
            updates: List<CharacterUpdates>,
        ) = updates.map { charUpdate ->
            val fullSaga = sagaHistoryUseCase.getSagaById(saga.data.id).first() as SagaContent
            val character = fullSaga.findCharacter(charUpdate.name) ?: return@map null

            characterUseCase
                .applyCharacterUpdates(fullSaga, timelineId, character.data, charUpdate)
        }

        override fun generateFullLoreUpdateStream(
            saga: SagaMetadata,
            timeline: Timeline,
        ) = flow {
            try {
                val narrativeRules =
                    remoteConfigService.getJson<NarrativeRules>("narrative_rules") ?: NarrativeRules()
                val fullSaga = sagaHistoryUseCase.getSagaById(saga.data.id).first() as SagaContent
                val prompt =
                    TimelinePrompts.generateUnifiedLorePrompt(
                        promptService = promptService,
                        narrativeRules = narrativeRules,
                        sagaContent = fullSaga,
                        currentTimeline = TimelineContent(timeline, emptyList()),
                        conversationDirective = genreConfigService.conversationBlueprint(saga.data.genre),
                    )

                val conversationStyle = genreConfigService.conversationBlueprint(saga.data.genre)

                reasoningSynthesizerService
                    .synthesizeReasoning(
                        sourceFlow =
                            gemmaClient
                                .generateStreaming<GeneratedContent<UnifiedLoreUpdate>>(
                                    prompt = prompt,
                                    blueprintKey = TimelinePrompts.UNIFIED_LORE_GENERATION_BLUEPRINT,
                                    filterOutputFields =
                                        listOf(
                                            "id",
                                            "timelineId",
                                        ),
                                ),
                        context = "Generating new lore...",
                        conversationStyle = conversationStyle,
                        genre = saga.data.genre.name,
                    ).collect { state ->
                        when (state) {
                            is StreamingState.Success -> {
                                val unifiedLore = state.data.data

                                // 1. Update Timeline Details
                                val timelineUpdate =
                                    updateTimeline(
                                        unifiedLore.event.copy(
                                            id = timeline.id,
                                            chapterId = timeline.chapterId,
                                            createdAt = timeline.createdAt,
                                        ),
                                    )

                                // 2. Save Wiki Updates
                                unifiedLore.wikiUpdates.forEach { wikiUpdate ->
                                    val existingWiki =
                                        fullSaga.wikis.find {
                                            it.title.equals(
                                                wikiUpdate.title,
                                                true,
                                            )
                                        }
                                    val wikiToSave =
                                        Wiki(
                                            id = existingWiki?.id ?: 0,
                                            title = wikiUpdate.title,
                                            content = wikiUpdate.content,
                                            type = wikiUpdate.type,
                                            emojiTag = wikiUpdate.emojiTag,
                                            sagaId = saga.data.id,
                                            timelineId = timeline.id,
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
                                        timelineId = timeline.id,
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
                if (e is CancellationException) {
                    throw e
                }
                emit(
                    StreamingState
                        .Error(e.message ?: "Unknown error"),
                )
            }
        }

        override fun generateTimelineStream(
            saga: SagaMetadata,
            currentTimeline: Timeline,
        ) = flow {
            try {
                val narrativeRules =
                    remoteConfigService.getJson<NarrativeRules>("narrative_rules") ?: NarrativeRules()
                val fullSaga = sagaHistoryUseCase.getSagaById(saga.data.id).first() ?: return@flow
                gemmaClient
                    .generateStreaming<GeneratedContent<Timeline>>(
                        prompt =
                            TimelinePrompts.generateTimelinePrompt(
                                promptService = promptService,
                                narrativeRules = narrativeRules,
                                sagaContent = fullSaga,
                                currentTimeline = TimelineContent(currentTimeline, emptyList()),
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
                                        currentTimeline.copy(
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
                if (e is CancellationException) {
                    throw e
                }
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
            saga: SagaMetadata,
            timeline: Timeline,
        ) = executeRequest {
            val fullSaga = sagaHistoryUseCase.getSagaById(saga.data.id).first() as SagaContent
            val objectivePrompt =
                ChatPrompts.sceneSummarizationPrompt(
                    promptService = promptService,
                    saga = fullSaga,
                    remoteConfigService.getNarrativeRules(),
                )
            val summary =
                gemmaClient
                    .generate<SceneSummary>(
                        objectivePrompt,
                        requirement = GemmaClient.ModelRequirement.MEDIUM,
                        useCore = true,
                    )!!

            updateTimeline(
                timeline.copy(
                    sceneSummary = summary,
                    currentObjective = summary.immediateObjective,
                ),
            )
        }

        override suspend fun generateTimelineContent(
            saga: SagaMetadata,
            timeline: Timeline,
        ): RequestResult<Unit> =
            executeRequest {
                generateFullLoreUpdate(saga, timeline).getSuccess()!!
            }

        private suspend fun updateWikis(
            timeline: Timeline,
            saga: SagaMetadata,
        ) = executeRequest {
            val fullSaga = sagaHistoryUseCase.getSagaById(saga.data.id).first() as SagaContent
            val wikisToUpdateOrAdd = wikiUseCase.generateWiki(saga, timeline).getSuccess()!!
            Timber.d("updateWikis: Updating wikis $wikisToUpdateOrAdd")
            wikisToUpdateOrAdd.forEach { generatedWiki ->
                val existingWiki =
                    fullSaga.wikis.find { wiki ->
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
            saga: SagaMetadata,
        ) = executeRequest {
            val fullSaga = sagaHistoryUseCase.getSagaById(saga.data.id).first() as SagaContent
            characterUseCase.generateCharactersUpdate(timeline, fullSaga)

            characterUseCase.generateCharacterRelations(timeline, fullSaga)
        }

        override fun getTimelineWithActBySaga(sagaId: Int): Flow<List<TimelineWithAct>> =
            timelineRepository.getTimelineWithActBySaga(sagaId)
    }
