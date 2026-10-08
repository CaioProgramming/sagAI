package com.ilustris.sagai.features.progression.domain

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ModelRequirement
import com.ilustris.sagai.core.ai.prompts.FeatEvaluationArgs
import com.ilustris.sagai.core.ai.prompts.PlacementArgs
import com.ilustris.sagai.core.ai.prompts.HonorsArgs
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.usecase.ActUseCase
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.usecase.ChapterUseCase
import com.ilustris.sagai.features.progression.data.model.CharacterHonor
import com.ilustris.sagai.features.progression.data.model.HonorKind
import com.ilustris.sagai.features.progression.data.model.LegaciesOutput
import com.ilustris.sagai.features.progression.data.model.MedalsOutput
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
        private val chapterUseCase: ChapterUseCase,
        private val actUseCase: ActUseCase,
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

        override suspend fun evaluateChapterHonors(
            sagaId: Int,
            chapterId: Int,
            silent: Boolean,
        ): RequestResult<Unit> =
            executeRequest {
                val config = progressionConfigProvider.activeConfig() ?: return@executeRequest
                val saga = sagaHistoryUseCase.getSagaById(sagaId).first() ?: return@executeRequest
                if (!saga.data.progressionEnabled) return@executeRequest
                val chapter = saga.acts.flatMap { it.chapters }.first { it.data.id == chapterId }
                val validator = HonorValidator(config)

                val eventIds = chapter.events.map { it.data.id }.toSet()
                val feats = progressionDao.getFeatsBySaga(sagaId).filter { it.timelineId in eventIds }
                if (!validator.hasMedalMaterial(feats)) {
                    markChapterHonored(chapter.data)
                    return@executeRequest
                }

                val output =
                    gemmaClient.generate<MedalsOutput>(
                        promptSplit =
                            ProgressionPrompts.honors(
                                promptService,
                                HonorsArgs(
                                    honorKind = "MEDALS",
                                    scope = chapter.data.toAINormalize(HONOR_SCOPE_EXCLUSIONS),
                                    feats = featsForPrompt(saga, feats),
                                    medalsOfTheAct = "",
                                ),
                            ),
                        requirement = ModelRequirement.MEDIUM,
                    )!!

                val candidates =
                    output.medals.mapNotNull { draft ->
                        val character = characterByName(saga, draft.characterName) ?: return@mapNotNull null
                        HonorCandidate(character.id, draft.name, draft.emoji, draft.reason, draft.featRefs)
                    }
                val honors = validator.validMedals(candidates, feats)
                progressionDao.deleteHonors(sagaId, HonorKind.MEDAL.name, chapterId)
                progressionDao.insertHonors(honors.map { it.toHonor(sagaId, HonorKind.MEDAL, chapterId, silent) })
                markChapterHonored(chapter.data)
            }

        override suspend fun evaluateActLegacy(
            sagaId: Int,
            actId: Int,
            silent: Boolean,
        ): RequestResult<Unit> =
            executeRequest {
                val config = progressionConfigProvider.activeConfig() ?: return@executeRequest
                val saga = sagaHistoryUseCase.getSagaById(sagaId).first() ?: return@executeRequest
                if (!saga.data.progressionEnabled) return@executeRequest
                val act = saga.acts.first { it.data.id == actId }
                val validator = HonorValidator(config)

                val eventIds = act.chapters.flatMap { chapter -> chapter.events.map { it.data.id } }.toSet()
                val feats = progressionDao.getFeatsBySaga(sagaId).filter { it.timelineId in eventIds }
                if (!validator.hasLegacyMaterial(feats)) {
                    markActLegacy(act.data)
                    return@executeRequest
                }

                val medals =
                    act.chapters.flatMap { progressionDao.getHonors(sagaId, HonorKind.MEDAL.name, it.data.id) }
                val output =
                    gemmaClient.generate<LegaciesOutput>(
                        promptSplit =
                            ProgressionPrompts.honors(
                                promptService,
                                HonorsArgs(
                                    honorKind = "LEGACIES",
                                    scope = act.data.toAINormalize(HONOR_SCOPE_EXCLUSIONS),
                                    feats = featsForPrompt(saga, feats),
                                    medalsOfTheAct =
                                        medals
                                            .map { mapOf("character" to nameOf(saga, it.characterId), "name" to it.name, "reason" to it.description) }
                                            .toAINormalize(),
                                ),
                            ),
                        requirement = ModelRequirement.MEDIUM,
                    )!!

                val candidates =
                    output.legacies.mapNotNull { draft ->
                        val character = characterByName(saga, draft.characterName) ?: return@mapNotNull null
                        HonorCandidate(character.id, draft.title, null, draft.description, draft.featRefs)
                    }
                val honors = validator.validLegacies(candidates, feats)
                progressionDao.deleteHonors(sagaId, HonorKind.TITLE.name, actId)
                progressionDao.insertHonors(honors.map { it.toHonor(sagaId, HonorKind.TITLE, actId, silent) })
                markActLegacy(act.data)
            }

        private fun ValidHonor.toHonor(
            sagaId: Int,
            kind: HonorKind,
            scopeId: Int,
            silent: Boolean,
        ) = CharacterHonor(
            characterId = characterId,
            sagaId = sagaId,
            kind = kind,
            scopeId = scopeId,
            name = name,
            emoji = emoji,
            description = description,
            featIds = featIds,
            announced = silent,
        )

        private fun featsForPrompt(
            saga: SagaContent,
            feats: List<CharacterFeat>,
        ): String =
            feats
                .map { feat ->
                    mapOf(
                        "id" to feat.id,
                        "character" to nameOf(saga, feat.characterId),
                        "kind" to feat.kind,
                        "title" to feat.title,
                        "changed" to feat.changed,
                        "cost" to feat.cost,
                        "tier" to feat.tier,
                    )
                }.toAINormalize()

        private fun nameOf(
            saga: SagaContent,
            characterId: Int,
        ) = saga.characters.firstOrNull { it.data.id == characterId }?.data?.name.orEmpty()

        private fun characterByName(
            saga: SagaContent,
            name: String,
        ) = saga.characters.map { it.data }.firstOrNull { it.name.trim().equals(name.trim(), ignoreCase = true) }

        private suspend fun markChapterHonored(chapter: Chapter) {
            if (!chapter.honorsGenerated) chapterUseCase.updateChapter(chapter.copy(honorsGenerated = true))
        }

        private suspend fun markActLegacy(act: Act) {
            if (!act.legacyGenerated) actUseCase.updateAct(act.copy(legacyGenerated = true))
        }

        override suspend fun powerContext(
            saga: SagaContent,
            presentNames: List<String>,
        ): String? {
            if (!saga.data.progressionEnabled) return null
            val config = progressionConfigProvider.activeConfig() ?: return null
            val math = ProgressionMath(config)
            val xpByCharacter = progressionDao.getEarnedXpBySaga(saga.data.id).associate { it.characterId to it.xp }
            val inScene =
                saga.characters
                    .map { it.data }
                    .filter { character ->
                        character.id == saga.data.mainCharacterId || presentNames.any { it.trim().equals(character.name.trim(), ignoreCase = true) }
                    }.mapNotNull { character ->
                        val base = character.baseLevel ?: return@mapNotNull null
                        PowerEntry(character.name, math.levelFor(base, xpByCharacter[character.id] ?: 0))
                    }
            return PowerContext(config).describe(inScene)
        }

        override suspend fun resetSaga(sagaId: Int) {
            progressionDao.deleteFeatsBySaga(sagaId)
            progressionDao.deleteHonorsBySaga(sagaId)
            val saga = sagaHistoryUseCase.getSagaById(sagaId).first() ?: return
            saga.flatEvents().filter { it.data.experienceGenerated }.forEach {
                timelineRepository.updateTimeline(it.data.copy(experienceGenerated = false))
            }
            saga.acts.forEach { act ->
                if (act.data.legacyGenerated) actUseCase.updateAct(act.data.copy(legacyGenerated = false))
                act.chapters.filter { it.data.honorsGenerated }.forEach {
                    chapterUseCase.updateChapter(it.data.copy(honorsGenerated = false))
                }
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

        private companion object {
            val HONOR_SCOPE_EXCLUSIONS =
                listOf("id", "actId", "sagaId", "coverImage", "artwork", "currentEventId", "currentChapterId")
        }
    }
