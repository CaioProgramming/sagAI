package com.ilustris.sagai.features.progression.domain

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ModelRequirement
import com.ilustris.sagai.core.ai.prompts.FeatEvaluationArgs
import com.ilustris.sagai.core.ai.prompts.PlacementArgs
import com.ilustris.sagai.core.ai.prompts.ProgressionPrompts
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import com.ilustris.sagai.features.progression.data.model.CharacterFeat
import com.ilustris.sagai.features.progression.data.model.CharacterPlacementOutput
import com.ilustris.sagai.features.progression.data.model.FeatEvaluation
import com.ilustris.sagai.features.progression.data.source.ProgressionDao
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.features.timeline.data.repository.TimelineRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CharacterProgressionUseCaseImpl
    @Inject
    constructor(
        private val progressionDao: ProgressionDao,
        private val sagaHistoryUseCase: SagaHistoryUseCase,
        private val characterUseCase: CharacterUseCase,
        private val timelineRepository: TimelineRepository,
        private val gemmaClient: GemmaClient,
        private val promptService: PromptService,
        private val progressionConfigProvider: ProgressionConfigProvider,
    ) : CharacterProgressionUseCase {
        override suspend fun evaluateEvent(
            sagaId: Int,
            timelineId: Int,
        ): RequestResult<Unit> =
            executeRequest {
                val config = progressionConfigProvider.activeConfig() ?: return@executeRequest
                val saga = sagaHistoryUseCase.getSagaById(sagaId).first() ?: return@executeRequest
                if (!saga.data.progressionEnabled) return@executeRequest

                val chapterEvents =
                    saga.acts
                        .flatMap { it.chapters }
                        .first { chapter -> chapter.events.any { it.data.id == timelineId } }
                        .events
                val event = chapterEvents.first { it.data.id == timelineId }
                val participants = participantsOf(saga, event)
                if (participants.isEmpty()) {
                    markEvaluated(event)
                    return@executeRequest
                }

                val math = ProgressionMath(config)
                val otherFeats = progressionDao.getFeatsBySaga(sagaId).filter { it.timelineId != timelineId }
                val chapterFeats = otherFeats.filter { feat -> chapterEvents.any { it.data.id == feat.timelineId } }
                val levels =
                    participants.associate { character ->
                        val earned = otherFeats.filter { it.characterId == character.id }.sumOf { it.xp }
                        character.id to math.levelFor(character.baseLevel ?: 1, earned)
                    }

                val evaluation =
                    gemmaClient.generate<FeatEvaluation>(
                        promptSplit =
                            ProgressionPrompts.featEvaluation(
                                promptService,
                                FeatEvaluationArgs(
                                    event = eventSummary(event),
                                    participants =
                                        participants
                                            .map { character ->
                                                mapOf(
                                                    "name" to character.name,
                                                    "level" to levels.getValue(character.id),
                                                    "skills" to character.skills,
                                                )
                                            }.toAINormalize(),
                                    relationshipChanges = relationshipChanges(event),
                                ),
                            ),
                        requirement = ModelRequirement.MEDIUM,
                    )!!

                val validEvidenceIds = event.updatedRelationshipDetails.flatMap { it.relationshipEvents }.filter { it.timelineId == timelineId }.map { it.id }.toSet()
                val assembler = FeatAssembler(math)
                val now = System.currentTimeMillis()
                val feats = mutableListOf<CharacterFeat>()
                participants.forEach { character ->
                    val output = evaluation.characters.firstOrNull { it.name.trim().equals(character.name.trim(), ignoreCase = true) } ?: return@forEach
                    val mine = chapterFeats.filter { it.characterId == character.id }
                    val assembled =
                        assembler.assemble(
                            drafts = output.feats.map { FeatDraft(it.kind, it.title, it.changed, it.cost, it.tier, it.relationshipEvidence) },
                            level = levels.getValue(character.id),
                            priorKindCounts = mine.groupingBy { it.kind }.eachCount(),
                            chapterXpSoFar = mine.sumOf { it.xp },
                            validEvidenceIds = validEvidenceIds,
                        )
                    assembled.forEachIndexed { ordinal, feat ->
                        feats +=
                            CharacterFeat(
                                characterId = character.id,
                                sagaId = sagaId,
                                timelineId = timelineId,
                                ordinal = ordinal,
                                kind = feat.kind,
                                title = feat.title,
                                changed = feat.changed,
                                cost = feat.cost,
                                tier = feat.tier,
                                xp = feat.xp,
                                relationshipUpdateEventId = feat.relationshipEvidence,
                                createdAt = now,
                            )
                    }
                    mergeSkills(character, output.newSkills, config.maxSkills)
                }

                progressionDao.replaceFeats(timelineId, feats)
                markEvaluated(event)
            }

        override suspend fun placeCharacters(sagaId: Int): RequestResult<Unit> =
            executeRequest {
                val config = progressionConfigProvider.activeConfig() ?: return@executeRequest
                val saga = sagaHistoryUseCase.getSagaById(sagaId).first() ?: return@executeRequest
                val unplaced = saga.characters.map { it.data }.filter { it.baseLevel == null }
                if (unplaced.isEmpty()) return@executeRequest

                val placement =
                    gemmaClient.generate<CharacterPlacementOutput>(
                        promptSplit =
                            ProgressionPrompts.placement(
                                promptService,
                                PlacementArgs(
                                    characters =
                                        saga.characters
                                            .map { it.data }
                                            .map { character ->
                                                mapOf(
                                                    "name" to character.name,
                                                    "isProtagonist" to (character.id == saga.data.mainCharacterId),
                                                    "alreadyPlacedAtLevel" to character.baseLevel,
                                                    "occupation" to character.profile.occupation,
                                                    "personality" to character.profile.personality,
                                                    "backstory" to character.backstory,
                                                    "abilities" to character.details.abilities,
                                                )
                                            }.toAINormalize(),
                                    levelBands = config.levelBands.toAINormalize(),
                                    protagonistMaxLevel = config.playerStartLevelCap,
                                ),
                            ),
                        requirement = ModelRequirement.HIGH,
                    )!!

                unplaced.forEach { character ->
                    val entry = placement.placements.firstOrNull { it.name.trim().equals(character.name.trim(), ignoreCase = true) } ?: return@forEach
                    characterUseCase.updateCharacter(
                        character
                            .copy(
                                baseLevel = entry.baseLevel,
                                levelRationale = entry.levelRationale,
                                skills = entry.skills,
                            ).withPlacement(config, isProtagonist = character.id == saga.data.mainCharacterId),
                    )
                }
            }

        override suspend fun resetSaga(sagaId: Int) {
            progressionDao.deleteFeatsBySaga(sagaId)
            progressionDao.deleteHonorsBySaga(sagaId)
            val saga = sagaHistoryUseCase.getSagaById(sagaId).first() ?: return
            saga.flatEvents().filter { it.data.experienceGenerated }.forEach {
                timelineRepository.updateTimeline(it.data.copy(experienceGenerated = false))
            }
        }

        /** Characters with a recorded event in this timeline, plus both sides of any relationship it changed. */
        private fun participantsOf(
            saga: SagaContent,
            event: TimelineContent,
        ): List<Character> {
            val ids =
                event.characterEventDetails.map { it.character.id } +
                    event.updatedRelationshipDetails.flatMap { listOf(it.characterOne.id, it.characterTwo.id) }
            return ids.distinct().mapNotNull { id -> saga.characters.firstOrNull { it.data.id == id }?.data }
        }

        private fun eventSummary(event: TimelineContent): String =
            mapOf(
                "title" to event.data.title,
                "summary" to event.data.content,
                "emotionalReview" to event.data.emotionalReview,
                "characterMoments" to event.characterEventDetails.map { "${it.character.name}: ${it.event.title} - ${it.event.summary}" },
            ).toAINormalize()

        private fun relationshipChanges(event: TimelineContent): String =
            event.updatedRelationshipDetails
                .flatMap { relation ->
                    relation.relationshipEvents
                        .filter { it.timelineId == event.data.id }
                        .map {
                            mapOf(
                                "id" to it.id,
                                "between" to "${relation.characterOne.name} and ${relation.characterTwo.name}",
                                "title" to it.title,
                                "description" to it.description,
                            )
                        }
                }.toAINormalize()

        private suspend fun mergeSkills(
            character: Character,
            newSkills: List<String>,
            maxSkills: Int,
        ) {
            val additions = newSkills.map { it.trim() }.filter { it.isNotEmpty() }
            if (additions.isEmpty()) return
            val merged = (character.skills + additions).distinctBy { it.lowercase() }.take(maxSkills)
            if (merged != character.skills) characterUseCase.updateCharacter(character.copy(skills = merged))
        }

        private suspend fun markEvaluated(event: TimelineContent) {
            if (!event.data.experienceGenerated) {
                timelineRepository.updateTimeline(event.data.copy(experienceGenerated = true))
            }
        }
    }
