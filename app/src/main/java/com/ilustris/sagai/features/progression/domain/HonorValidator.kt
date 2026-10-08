package com.ilustris.sagai.features.progression.domain

import com.ilustris.sagai.features.progression.data.model.CharacterFeat

/** An honor the AI proposed for a character it was matched to, before the code judges it. */
data class HonorCandidate(
    val characterId: Int,
    val name: String,
    val emoji: String?,
    val description: String,
    val featRefs: List<Int>,
)

/** An honor that passed the guards; [featIds] holds only real feats of the character. */
data class ValidHonor(
    val characterId: Int,
    val name: String,
    val emoji: String?,
    val description: String,
    val featIds: List<Int>,
)

/**
 * The code half of the honors: the AI judges who deserves a medal or a legacy, this only checks
 * the judgement against the ledger. Every cited id must be a feat of that character in scope, the
 * best cited feat must clear the floor, and the caps hold. Pure, so it is tested without the AI.
 */
class HonorValidator(
    private val config: ProgressionConfig,
) {
    /** A medal recognises a deed that cost something; a feat backs at most one medal. */
    fun validMedals(
        candidates: List<HonorCandidate>,
        chapterFeats: List<CharacterFeat>,
    ): List<ValidHonor> =
        validate(
            candidates = candidates,
            feats = chapterFeats,
            minTier = config.medalMinTier,
            requireCost = true,
            perCharacterCap = config.maxMedalsPerCharacterPerChapter,
            totalCap = config.maxMedalsPerChapter,
            featBacksOnlyOne = true,
        )

    /** A legacy recognises a lasting change: one per character, a few per act. */
    fun validLegacies(
        candidates: List<HonorCandidate>,
        actFeats: List<CharacterFeat>,
    ): List<ValidHonor> =
        validate(
            candidates = candidates,
            feats = actFeats,
            minTier = config.legacyMinTier,
            requireCost = false,
            perCharacterCap = 1,
            totalCap = config.maxTitlesPerAct,
            featBacksOnlyOne = false,
        )

    /** True when at least one feat could back an honor, so the AI call is worth making. */
    fun hasMedalMaterial(chapterFeats: List<CharacterFeat>): Boolean =
        chapterFeats.any { it.tier >= config.medalMinTier && !it.cost.isNullOrBlank() }

    fun hasLegacyMaterial(actFeats: List<CharacterFeat>): Boolean = actFeats.any { it.tier >= config.legacyMinTier }

    private fun validate(
        candidates: List<HonorCandidate>,
        feats: List<CharacterFeat>,
        minTier: Int,
        requireCost: Boolean,
        perCharacterCap: Int,
        totalCap: Int,
        featBacksOnlyOne: Boolean,
    ): List<ValidHonor> {
        val byId = feats.associateBy { it.id }
        val usedFeatIds = mutableSetOf<Int>()
        val perCharacter = mutableMapOf<Int, Int>()
        val accepted = mutableListOf<ValidHonor>()
        for (candidate in candidates) {
            if (accepted.size >= totalCap) break
            if (candidate.name.isBlank() || candidate.description.isBlank()) continue
            if ((perCharacter[candidate.characterId] ?: 0) >= perCharacterCap) continue

            val refs =
                candidate.featRefs
                    .distinct()
                    .mapNotNull { byId[it] }
                    .filter { it.characterId == candidate.characterId }
                    .filter { !featBacksOnlyOne || it.id !in usedFeatIds }
            val clearsFloor = refs.any { it.tier >= minTier && (!requireCost || !it.cost.isNullOrBlank()) }
            if (!clearsFloor) continue

            usedFeatIds += refs.map { it.id }
            perCharacter[candidate.characterId] = (perCharacter[candidate.characterId] ?: 0) + 1
            accepted +=
                ValidHonor(
                    characterId = candidate.characterId,
                    name = candidate.name.trim().take(MAX_NAME),
                    emoji = candidate.emoji?.trim()?.takeIf { it.isNotEmpty() }?.take(MAX_EMOJI),
                    description = candidate.description.trim().take(MAX_DESCRIPTION),
                    featIds = refs.map { it.id },
                )
        }
        return accepted
    }

    private companion object {
        const val MAX_NAME = 60
        const val MAX_EMOJI = 12
        const val MAX_DESCRIPTION = 300
    }
}
