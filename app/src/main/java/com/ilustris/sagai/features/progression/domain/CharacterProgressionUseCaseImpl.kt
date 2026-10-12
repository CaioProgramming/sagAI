package com.ilustris.sagai.features.progression.domain

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ModelRequirement
import com.ilustris.sagai.core.ai.prompts.FeatEvaluationArgs
import com.ilustris.sagai.core.ai.prompts.PlacementArgs
import com.ilustris.sagai.core.ai.prompts.StatusArgs
import com.ilustris.sagai.features.progression.data.model.StatusOutput
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
import com.ilustris.sagai.core.globalshell.GlobalShellService
import com.ilustris.sagai.core.globalshell.ProgressionEffect
import com.ilustris.sagai.core.globalshell.ProgressionEffectKind
import com.ilustris.sagai.core.globalshell.ProgressionRow
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.utils.toAINormalize
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterStatus
import com.ilustris.sagai.features.characters.data.usecase.CharacterUseCase
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.home.data.usecase.SagaHistoryUseCase
import com.ilustris.sagai.features.progression.data.model.CharacterFeat
import com.ilustris.sagai.features.progression.data.model.CharacterPlacementOutput
import com.ilustris.sagai.features.progression.data.model.FeatEvaluation
import com.ilustris.sagai.features.progression.data.source.ProgressionDao
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.features.timeline.data.repository.TimelineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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
        private val globalShellService: GlobalShellService,
    ) : CharacterProgressionUseCase {

        override suspend fun evaluateEvent(
            sagaId: Int,
            timelineId: Int,
            silent: Boolean,
        ): RequestResult<List<ProgressionChange>> =
            executeRequest {
                val config = progressionConfigProvider.activeConfig() ?: return@executeRequest emptyList()
                val saga = sagaHistoryUseCase.getSagaById(sagaId).first() ?: return@executeRequest emptyList()
                if (!saga.data.progressionEnabled) return@executeRequest emptyList()

                val chapterEvents =
                    saga.acts
                        .flatMap { it.chapters }
                        .first { chapter -> chapter.events.any { it.data.id == timelineId } }
                        .events
                val event = chapterEvents.first { it.data.id == timelineId }
                val participants = participantsOf(saga, event)
                if (participants.isEmpty()) {
                    markEvaluated(event, silent)
                    return@executeRequest emptyList()
                }

                val math = ProgressionMath(config)
                // Events are numbered in story order, so only what came before this one counts for its level.
                val otherFeats = progressionDao.getFeatsBySaga(sagaId).filter { it.timelineId < timelineId }
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
                val changes = mutableListOf<ProgressionChange>()
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
                    if (assembled.isNotEmpty()) {
                        val before = levels.getValue(character.id)
                        val earnedBefore = otherFeats.filter { it.characterId == character.id }.sumOf { it.xp }
                        val gained = assembled.sumOf { it.xp }
                        changes +=
                            ProgressionChange.Xp(
                                characterId = character.id,
                                characterName = character.name,
                                xp = gained,
                                levelBefore = before,
                                levelAfter = math.levelFor(character.baseLevel ?: 1, earnedBefore + gained),
                                featTitles = assembled.map { it.title },
                            )
                    }
                }

                progressionDao.replaceFeats(timelineId, feats)
                if (!silent && !event.data.progressionAnnounced) announceLevelUps(saga, changes, config.levelUpRowsMax)
                markEvaluated(event, silent)
                changes
            }

        override suspend fun placeCharacters(sagaId: Int): RequestResult<List<ProgressionChange>> =
            executeRequest {
                val config = progressionConfigProvider.activeConfig() ?: return@executeRequest emptyList()
                val saga = sagaHistoryUseCase.getSagaById(sagaId).first() ?: return@executeRequest emptyList()
                val unplaced = saga.characters.map { it.data }.filter { it.baseLevel == null }
                if (unplaced.isEmpty()) return@executeRequest emptyList()

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

                val changes = mutableListOf<ProgressionChange>()
                unplaced.forEach { character ->
                    val entry = placement.placements.firstOrNull { it.name.trim().equals(character.name.trim(), ignoreCase = true) } ?: return@forEach
                    val placed =
                        character
                            .copy(
                                baseLevel = entry.baseLevel,
                                levelRationale = entry.levelRationale,
                                skills = entry.skills,
                            ).withPlacement(config, isProtagonist = character.id == saga.data.mainCharacterId)
                    changes += ProgressionChange.Placed(character.name, placed.baseLevel ?: entry.baseLevel)
                    characterUseCase.updateCharacter(placed)
                }
                changes
            }

        override suspend fun evaluateChapterHonors(
            sagaId: Int,
            chapterId: Int,
            silent: Boolean,
        ): RequestResult<List<ProgressionChange>> =
            executeRequest {
                val config = progressionConfigProvider.activeConfig() ?: return@executeRequest emptyList()
                val saga = sagaHistoryUseCase.getSagaById(sagaId).first() ?: return@executeRequest emptyList()
                if (!saga.data.progressionEnabled) return@executeRequest emptyList()
                val chapter = saga.acts.flatMap { it.chapters }.first { it.data.id == chapterId }
                val validator = HonorValidator(config)

                val eventIds = chapter.events.map { it.data.id }.toSet()
                val feats = progressionDao.getFeatsBySaga(sagaId).filter { it.timelineId in eventIds }
                if (!validator.hasMedalMaterial(feats)) {
                    markChapterHonored(chapter.data)
                    return@executeRequest emptyList()
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
                val ids = progressionDao.insertHonors(honors.map { it.toHonor(sagaId, HonorKind.MEDAL, chapterId, silent, isDead(saga, it.characterId)) })
                markChapterHonored(chapter.data)
                val changes = honors.map { it.toChange(saga, HonorKind.MEDAL) }
                if (!silent) announceHonors(saga, ProgressionEffectKind.MEDAL, "$chapterId", changes, ids)
                changes
            }

        override suspend fun evaluateActLegacy(
            sagaId: Int,
            actId: Int,
            silent: Boolean,
        ): RequestResult<List<ProgressionChange>> =
            executeRequest {
                val config = progressionConfigProvider.activeConfig() ?: return@executeRequest emptyList()
                val saga = sagaHistoryUseCase.getSagaById(sagaId).first() ?: return@executeRequest emptyList()
                if (!saga.data.progressionEnabled) return@executeRequest emptyList()
                val act = saga.acts.first { it.data.id == actId }
                val validator = HonorValidator(config)

                val eventIds = act.chapters.flatMap { chapter -> chapter.events.map { it.data.id } }.toSet()
                val feats = progressionDao.getFeatsBySaga(sagaId).filter { it.timelineId in eventIds }
                if (!validator.hasLegacyMaterial(feats)) {
                    markActLegacy(act.data)
                    return@executeRequest emptyList()
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
                                    previousLegacies =
                                        saga.acts
                                            .filter { it.data.id < actId }
                                            .flatMap { progressionDao.getHonors(sagaId, HonorKind.TITLE.name, it.data.id) }
                                            .map { mapOf("character" to nameOf(saga, it.characterId), "title" to it.name, "description" to it.description) }
                                            .toAINormalize(),
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
                val ids = progressionDao.insertHonors(honors.map { it.toHonor(sagaId, HonorKind.TITLE, actId, silent, isDead(saga, it.characterId)) })
                markActLegacy(act.data)
                val changes = honors.map { it.toChange(saga, HonorKind.TITLE) }
                if (!silent) announceHonors(saga, ProgressionEffectKind.LEGACY, "$actId", changes, ids)
                changes
            }

        private fun ValidHonor.toChange(
            saga: SagaContent,
            kind: HonorKind,
        ) = ProgressionChange.Honor(characterId, nameOf(saga, characterId), kind, name, emoji, description)

        private fun ValidHonor.toHonor(
            sagaId: Int,
            kind: HonorKind,
            scopeId: Int,
            silent: Boolean,
            posthumous: Boolean,
        ) = CharacterHonor(
            characterId = characterId,
            sagaId = sagaId,
            kind = kind,
            scopeId = scopeId,
            name = name,
            emoji = emoji,
            description = description,
            featIds = featIds,
            posthumous = posthumous,
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

        private fun isDead(
            saga: SagaContent,
            characterId: Int,
        ) = saga.characters.firstOrNull { it.data.id == characterId }?.data?.status == CharacterStatus.DEAD

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

        override suspend fun recomputeXp(sagaId: Int): RequestResult<Unit> =
            executeRequest {
                val config = progressionConfigProvider.activeConfig() ?: return@executeRequest
                val saga = sagaHistoryUseCase.getSagaById(sagaId).first() ?: return@executeRequest
                val math = ProgressionMath(config)
                val assembler = FeatAssembler(math)
                val stored = progressionDao.getFeatsBySaga(sagaId).groupBy { it.timelineId }
                val earned = mutableMapOf<Int, Int>()
                val rewritten = mutableListOf<CharacterFeat>()

                saga.acts.flatMap { it.chapters }.forEach { chapter ->
                    val chapterXp = mutableMapOf<Int, Int>()
                    val kindCounts = mutableMapOf<Pair<Int, String>, Int>()
                    chapter.events.forEach { event ->
                        stored[event.data.id].orEmpty().groupBy { it.characterId }.forEach { (characterId, feats) ->
                            val ordered = feats.sortedBy { it.ordinal }
                            val base = saga.characters.firstOrNull { it.data.id == characterId }?.data?.baseLevel ?: 1
                            val assembled =
                                assembler.assemble(
                                    drafts = ordered.map { FeatDraft(it.kind, it.title, it.changed, it.cost, it.tier, it.relationshipUpdateEventId) },
                                    level = math.levelFor(base, earned[characterId] ?: 0),
                                    priorKindCounts = kindCounts.filterKeys { it.first == characterId }.mapKeys { it.key.second },
                                    chapterXpSoFar = chapterXp[characterId] ?: 0,
                                    validEvidenceIds = ordered.mapNotNull { it.relationshipUpdateEventId }.toSet(),
                                )
                            // A feat the assembler would now drop keeps its old weight: this is a re-weigh, not a re-judge.
                            val xps = if (assembled.size == ordered.size) assembled.map { it.xp } else ordered.map { it.xp }
                            ordered.forEachIndexed { index, feat ->
                                rewritten += feat.copy(xp = xps[index])
                                kindCounts.merge(characterId to feat.kind, 1, Int::plus)
                            }
                            earned.merge(characterId, xps.sum(), Int::plus)
                            chapterXp.merge(characterId, xps.sum(), Int::plus)
                        }
                    }
                }
                progressionDao.updateFeats(rewritten)
            }

        override suspend fun inferStatuses(sagaId: Int): RequestResult<List<ProgressionChange>> =
            executeRequest {
                progressionConfigProvider.activeConfig() ?: return@executeRequest emptyList()
                val saga = sagaHistoryUseCase.getSagaById(sagaId).first() ?: return@executeRequest emptyList()
                val events = saga.flatEvents().filter { it.data.content.isNotEmpty() }.takeLast(STATUS_EVENTS_WINDOW)
                if (events.isEmpty()) return@executeRequest emptyList()

                val output =
                    gemmaClient.generate<StatusOutput>(
                        promptSplit =
                            ProgressionPrompts.status(
                                promptService,
                                StatusArgs(
                                    characters = saga.characters.map { it.data.name }.toAINormalize(),
                                    events = events.map { mapOf("title" to it.data.title, "summary" to it.data.content) }.toAINormalize(),
                                ),
                            ),
                        requirement = ModelRequirement.MEDIUM,
                    )!!

                output.statuses.mapNotNull { entry ->
                    val status = entry.status ?: return@mapNotNull null
                    if (status == CharacterStatus.ACTIVE || entry.statusReason.isNullOrBlank()) return@mapNotNull null
                    val character = characterByName(saga, entry.name) ?: return@mapNotNull null
                    characterUseCase.updateCharacter(
                        character.copy(
                            status = status,
                            statusNote = entry.statusNote?.trim()?.takeIf { it.isNotEmpty() },
                            statusTimelineId = events.last().data.id,
                        ),
                    )
                    ProgressionChange.Status(character.name, status, entry.statusNote)
                }
            }

        override fun observeProgress(character: Character): Flow<CharacterProgress?> =
            flow {
                val config = progressionConfigProvider.activeConfig()
                val base = character.baseLevel
                if (config == null || base == null) {
                    emit(null)
                    return@flow
                }
                val math = ProgressionMath(config)
                emitAll(
                    combine(
                        progressionDao.observeFeats(character.id),
                        progressionDao.observeHonors(character.id),
                    ) { feats, honors ->
                        val earned = feats.sumOf { it.xp }
                        val level = math.levelFor(base, earned)
                        CharacterProgress(
                            baseLevel = base,
                            level = level,
                            bandLabel = PowerContext(config).bandLabel(level),
                            earnedXp = earned,
                            xpIntoLevel = (math.xpToReach(base) + earned - math.xpToReach(level)).coerceAtLeast(0),
                            levelSpan = math.levelSpan(level),
                            rationale = character.levelRationale,
                            feats = feats,
                            honors = honors,
                        )
                    },
                )
            }

        override fun observeEventXp(
            sagaId: Int,
            characters: List<Character>,
        ): Flow<Map<Int, Map<Int, EventXp>>> =
            flow {
                val config = progressionConfigProvider.activeConfig()
                if (config == null) {
                    emit(emptyMap())
                    return@flow
                }
                val math = ProgressionMath(config)
                emitAll(
                    progressionDao.observeFeatsBySaga(sagaId).map { feats ->
                        val earned = mutableMapOf<Int, Int>()
                        val result = mutableMapOf<Int, MutableMap<Int, EventXp>>()
                        // Events are numbered in story order, so walking them by id replays the levels as they happened.
                        feats.groupBy { it.timelineId }.toSortedMap().forEach { (timelineId, eventFeats) ->
                            eventFeats.groupBy { it.characterId }.forEach { (characterId, mine) ->
                                val base = characters.firstOrNull { it.id == characterId }?.baseLevel ?: return@forEach
                                val before = math.levelFor(base, earned[characterId] ?: 0)
                                val gained = mine.sumOf { it.xp }
                                earned[characterId] = (earned[characterId] ?: 0) + gained
                                val after = math.levelFor(base, earned.getValue(characterId))
                                result.getOrPut(timelineId) { mutableMapOf() }[characterId] = EventXp(gained, after, after > before, mine.sortedBy { it.ordinal })
                            }
                        }
                        result
                    },
                )
            }

        override fun observeBadges(
            sagaId: Int,
            characters: List<Character>,
        ): Flow<Map<Int, CharacterBadge>> =
            flow {
                val config = progressionConfigProvider.activeConfig()
                if (config == null) {
                    emit(emptyMap())
                    return@flow
                }
                val math = ProgressionMath(config)
                emitAll(
                    combine(progressionDao.observeFeatsBySaga(sagaId), progressionDao.observeHonorsBySaga(sagaId)) { feats, honors ->
                        characters
                            .filter { it.baseLevel != null }
                            .associate { character ->
                                val earned = feats.filter { it.characterId == character.id }.sumOf { it.xp }
                                val mine = honors.filter { it.characterId == character.id }
                                character.id to
                                    CharacterBadge(
                                        level = math.levelFor(character.baseLevel ?: 1, earned),
                                        lastMedalEmoji = mine.lastOrNull { it.kind == HonorKind.MEDAL }?.emoji,
                                        hasLegacy = mine.any { it.kind == HonorKind.TITLE },
                                        inMemory = character.status == CharacterStatus.DEAD,
                                    )
                            }
                    },
                )
            }

        override suspend fun ledger(sagaId: Int): List<CharacterLedger> {
            val saga = sagaHistoryUseCase.getSagaById(sagaId).first() ?: return emptyList()
            val math = progressionConfigProvider.activeConfig()?.let { ProgressionMath(it) }
            val feats = progressionDao.getFeatsBySaga(sagaId)
            val honors = progressionDao.getHonorsBySaga(sagaId)
            return saga.characters.map { it.data }.map { character ->
                val mine = feats.filter { it.characterId == character.id }
                val xp = mine.sumOf { it.xp }
                CharacterLedger(
                    name = character.name,
                    baseLevel = character.baseLevel,
                    level = character.baseLevel?.let { math?.levelFor(it, xp) },
                    earnedXp = xp,
                    feats = mine,
                    honors = honors.filter { it.characterId == character.id },
                )
            }
        }

        override suspend fun pendingCount(sagaId: Int): Int {
            val saga = sagaHistoryUseCase.getSagaById(sagaId).first() ?: return 0
            if (!saga.data.progressionEnabled) return 0
            return pending(saga).let { it.events.size + it.chapters.size + it.acts.size }
        }

        override suspend fun retryPending(sagaId: Int): RequestResult<Int> =
            executeRequest {
                val saga = sagaHistoryUseCase.getSagaById(sagaId).first() ?: return@executeRequest 0
                var done = 0
                val todo = pending(saga)
                todo.events.forEach { if (evaluateEvent(sagaId, it.data.id, silent = true).isSuccess) done++ }
                todo.chapters.forEach { if (evaluateChapterHonors(sagaId, it.data.id, silent = true).isSuccess) done++ }
                todo.acts.forEach { if (evaluateActLegacy(sagaId, it.data.id, silent = true).isSuccess) done++ }
                done
            }

        private class Pending(
            val events: List<TimelineContent>,
            val chapters: List<com.ilustris.sagai.features.chapter.data.model.ChapterContent>,
            val acts: List<com.ilustris.sagai.features.act.data.model.ActContent>,
        )

        private fun pending(saga: SagaContent) =
            Pending(
                events = saga.flatEvents().filter { it.data.title.isNotEmpty() && it.data.content.isNotEmpty() && !it.data.experienceGenerated },
                chapters = saga.flatChapters().filter { it.data.content.isNotEmpty() && !it.data.honorsGenerated },
                acts = saga.acts.filter { it.data.content.isNotEmpty() && !it.data.legacyGenerated },
            )

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

        private fun announceLevelUps(
            saga: SagaContent,
            changes: List<ProgressionChange>,
            maxRows: Int,
        ) {
            val ups = changes.filterIsInstance<ProgressionChange.Xp>().filter { it.levelAfter > it.levelBefore }
            if (ups.isEmpty()) return
            val shown =
                ups.sortedByDescending { it.levelAfter }.take(maxRows).mapNotNull { up ->
                    val character = saga.characters.firstOrNull { it.data.id == up.characterId }?.data ?: return@mapNotNull null
                    ProgressionRow(character = character, title = up.featTitles.firstOrNull().orEmpty(), level = up.levelAfter, xp = up.xp)
                }
            if (shown.isEmpty()) return
            globalShellService.post(
                ProgressionEffect(
                    kind = ProgressionEffectKind.LEVEL_UP,
                    announcementKey = "${saga.data.id}_${ups.joinToString("_") { "${it.characterId}${it.levelAfter}" }}",
                    rows = shown,
                    sagaId = saga.data.id,
                    sagaTitle = saga.data.title,
                    genre = saga.data.genre,
                    deepLink = "saga://character_detail/${shown.first().character.id}",
                    moreDeepLink = "saga://character_detail/${shown.first().character.id}",
                ),
            )
        }

        private suspend fun announceHonors(
            saga: SagaContent,
            kind: ProgressionEffectKind,
            scopeKey: String,
            changes: List<ProgressionChange>,
            ids: List<Long>,
        ) {
            val honors = changes.filterIsInstance<ProgressionChange.Honor>()
            if (honors.isEmpty()) return
            val rows =
                honors.groupBy { it.characterId }.mapNotNull { (characterId, mine) ->
                    val character = saga.characters.firstOrNull { it.data.id == characterId }?.data ?: return@mapNotNull null
                    val latest = mine.last()
                    ProgressionRow(
                        character = character,
                        title = latest.name,
                        detail = latest.description,
                        emoji = latest.emoji,
                        extra = mine.size - 1,
                        memorial = character.status == CharacterStatus.DEAD,
                    )
                }
            if (rows.isEmpty()) return
            globalShellService.post(
                ProgressionEffect(
                    kind = kind,
                    announcementKey = "${saga.data.id}_$scopeKey",
                    rows = rows,
                    sagaId = saga.data.id,
                    sagaTitle = saga.data.title,
                    genre = saga.data.genre,
                    deepLink = "saga://character_detail/${rows.first().character.id}",
                    moreDeepLink =
                        if (kind == ProgressionEffectKind.LEGACY) "saga://saga_acts/${saga.data.id}" else "saga://saga_chapters/${saga.data.id}",
                ),
            )
            progressionDao.markHonorsAnnounced(ids.map { it.toInt() })
        }

        /** Evaluated and told (or deliberately kept quiet): nothing is left to announce for this event. */
        private suspend fun markEvaluated(
            event: TimelineContent,
            silent: Boolean,
        ) {
            if (!event.data.experienceGenerated || !event.data.progressionAnnounced) {
                timelineRepository.updateTimeline(
                    event.data.copy(experienceGenerated = true, progressionAnnounced = true),
                )
            }
        }

        private companion object {
            const val STATUS_EVENTS_WINDOW = 15
            val HONOR_SCOPE_EXCLUSIONS =
                listOf("id", "actId", "sagaId", "coverImage", "artwork", "currentEventId", "currentChapterId")
        }
    }
