package com.ilustris.sagai.features.progression.domain

/** A feat as the AI proposed it, before the code judges it. */
data class FeatDraft(
    val kind: String,
    val title: String,
    val changed: String?,
    val cost: String?,
    val tier: Int,
    val relationshipEvidence: Int?,
)

/** A feat after the guards, with its XP frozen. */
data class AssembledFeat(
    val kind: String,
    val title: String,
    val changed: String,
    val cost: String?,
    val tier: Int,
    val xp: Int,
    val relationshipEvidence: Int?,
)

/**
 * Turns one character's drafts for one event into feats: drops what is malformed, clamps tiers the
 * evidence does not support, applies the repeat decay and fits the XP to the event and chapter caps.
 * Pure, so the whole anti-farming pipeline is testable without the AI.
 */
class FeatAssembler(
    private val math: ProgressionMath,
) {
    /**
     * @param level the character's level before this event; the caps are a share of its span.
     * @param priorKindCounts feats of each kind already counted this chapter, before this event.
     * @param chapterXpSoFar XP the character already earned this chapter, before this event.
     * @param validEvidenceIds relationship update ids the model was shown; any other id is dropped.
     */
    fun assemble(
        drafts: List<FeatDraft>,
        level: Int,
        priorKindCounts: Map<String, Int>,
        chapterXpSoFar: Int,
        validEvidenceIds: Set<Int>,
    ): List<AssembledFeat> {
        val seenKinds = priorKindCounts.toMutableMap()
        val judged =
            drafts.mapNotNull { draft ->
                val kind = draft.kind.trim().uppercase()
                if (kind !in KINDS || draft.title.isBlank()) return@mapNotNull null
                val tier = math.clampTier(draft.tier, draft.changed, draft.cost)
                val prior = seenKinds[kind] ?: 0
                seenKinds[kind] = prior + 1
                AssembledFeat(
                    kind = kind,
                    title = draft.title.trim(),
                    changed = draft.changed.orEmpty().trim(),
                    cost = draft.cost?.trim()?.takeIf { it.isNotEmpty() },
                    tier = tier,
                    xp = math.featXp(tier, prior),
                    relationshipEvidence = draft.relationshipEvidence?.takeIf { it in validEvidenceIds },
                )
            }
        val perEvent = math.fitToBudget(judged.map { it.xp }, math.eventBudget(level))
        val chapterRoom = (math.chapterBudget(level) - chapterXpSoFar).coerceAtLeast(0)
        val capped = math.fitToBudget(perEvent, chapterRoom)
        return judged.mapIndexed { index, feat -> feat.copy(xp = capped[index]) }
    }

    companion object {
        val KINDS =
            setOf("BOND", "SACRIFICE", "VULNERABILITY", "MORAL_CHOICE", "DISCOVERY", "OVERCOMING", "LEADERSHIP")
    }
}
