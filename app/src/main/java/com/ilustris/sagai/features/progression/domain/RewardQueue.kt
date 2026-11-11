package com.ilustris.sagai.features.progression.domain

import com.ilustris.sagai.features.progression.data.model.CharacterHonor
import com.ilustris.sagai.features.progression.data.model.HonorKind

/** One page of the rewards flow. Characters are referenced by id; the sheet resolves them from the saga. */
sealed interface RewardStep {
    data class LevelUp(
        val characterId: Int,
        val from: Int,
        val to: Int,
        val featTitle: String,
        val xp: Int,
        /** True on the first level-up the player ever sees: it carries the short explanation of the system. */
        val explainer: Boolean = false,
    ) : RewardStep

    data class Medal(
        val characterId: Int,
        val honorId: Int,
        val name: String,
        val emoji: String?,
        val detail: String,
    ) : RewardStep

    data class Legacy(
        val characterId: Int,
        val honorId: Int,
        val name: String,
        val detail: String,
        val memorial: Boolean,
    ) : RewardStep

    /** Two characters who just became connected; [relationId] resolves the full card. */
    data class Relationship(
        val characterOneId: Int,
        val characterTwoId: Int,
        val relationId: Int,
        val title: String,
        val emoji: String,
        val description: String,
    ) : RewardStep

    /** Closing page, only when more than one character earned something: one row per character. */
    data class Summary(
        val rows: List<SummaryRow>,
    ) : RewardStep
}

data class SummaryRow(
    val characterId: Int,
    val level: Int?,
    val medals: Int,
    val legacies: Int,
    val relationships: Int = 0,
)

/** A character that went up in level over the announced events. */
data class LevelUpInput(
    val characterId: Int,
    val from: Int,
    val to: Int,
    val featTitle: String,
    val xp: Int,
)

/** A relationship that came to exist over the announced events. */
data class NewRelationInput(
    val relationId: Int,
    val characterOneId: Int,
    val characterTwoId: Int,
    val title: String,
    val emoji: String,
    val description: String,
)

object RewardQueueBuilder {
    /**
     * Turns everything earned and not yet told into the pages of one flow. Grouped by character, so the story reads
     * "Tereza reached level 3, won this medal, earned this legacy": level first, then medals (by chapter), then the
     * legacy (by act), then the new bonds, placed after whichever of the two characters comes first. Characters who went up lead, the highest level first; those with only honors follow in the order
     * their first honor was earned. No cap: every achievement gets its page.
     */
    fun build(
        levelUps: List<LevelUpInput>,
        honors: List<CharacterHonor>,
        relations: List<NewRelationInput> = emptyList(),
        memorialIds: Set<Int> = emptySet(),
        showExplainer: Boolean = false,
    ): List<RewardStep> {
        val levelByCharacter = levelUps.associateBy { it.characterId }
        val honorsByCharacter = honors.groupBy { it.characterId }
        val base =
            (levelUps.sortedByDescending { it.to }.map { it.characterId } + honorsByCharacter.keys.sortedBy { id -> honorsByCharacter.getValue(id).minOf { it.id } })
                .distinct()
        // A bond hangs on the first of its two characters already in the story; a bond between two newcomers opens one.
        val sortedRelations = relations.sortedBy { it.relationId }
        val anchors = sortedRelations.associate { it.relationId to (base.firstOrNull { id -> id == it.characterOneId || id == it.characterTwoId } ?: it.characterOneId) }
        val order = (base + anchors.values).distinct()
        val relationsByCharacter = sortedRelations.groupBy { anchors.getValue(it.relationId) }

        var explainerLeft = showExplainer
        val steps = mutableListOf<RewardStep>()
        order.forEach { characterId ->
            levelByCharacter[characterId]?.let {
                steps += RewardStep.LevelUp(characterId, it.from, it.to, it.featTitle, it.xp, explainer = explainerLeft)
                explainerLeft = false
            }
            val mine = honorsByCharacter[characterId].orEmpty()
            mine
                .filter { it.kind == HonorKind.MEDAL }
                .sortedWith(compareBy({ it.scopeId }, { it.id }))
                .forEach { steps += RewardStep.Medal(characterId, it.id, it.name, it.emoji, it.description) }
            mine
                .filter { it.kind == HonorKind.TITLE }
                .sortedWith(compareBy({ it.scopeId }, { it.id }))
                .forEach { steps += RewardStep.Legacy(characterId, it.id, it.name, it.description, memorial = characterId in memorialIds) }
            relationsByCharacter[characterId].orEmpty().forEach {
                steps += RewardStep.Relationship(it.characterOneId, it.characterTwoId, it.relationId, it.title, it.emoji, it.description)
            }
        }

        if (order.size > 1) {
            steps +=
                RewardStep.Summary(
                    order.map { id ->
                        val mine = honorsByCharacter[id].orEmpty()
                        SummaryRow(
                            characterId = id,
                            level = levelByCharacter[id]?.to,
                            medals = mine.count { it.kind == HonorKind.MEDAL },
                            legacies = mine.count { it.kind == HonorKind.TITLE },
                            relationships = relationsByCharacter[id].orEmpty().size,
                        )
                    },
                )
        }
        return steps
    }
}
