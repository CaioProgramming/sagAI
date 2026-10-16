package com.ilustris.sagai.features.progression.domain

import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * The deterministic half of the progression system: the AI only judges a feat's tier, everything
 * numeric lives here. Pure Kotlin with no Android dependencies so it can move to KMP unchanged.
 *
 * A character's level is placed on the curve at [xpToReach] of their `baseLevel` and advances by
 * the XP their feats earned, so a veteran climbs slower than a novice: the span of each level grows
 * with the curve instead of every character restarting the cheap early levels.
 */
class ProgressionMath(
    private val config: ProgressionConfig,
) {
    /**
     * Lowers a tier the evidence does not support. Never raises one: a tier 3 needs a cost, a tier 4
     * needs a `changed` that is filled, and a tier 2 needs a `changed` at all.
     */
    fun clampTier(
        tier: Int,
        changed: String?,
        cost: String?,
    ): Int {
        var clamped = tier.coerceIn(MIN_TIER, MAX_TIER)
        if (clamped >= 4 && changed.isNullOrBlank()) clamped = 3
        if (clamped >= 3 && cost.isNullOrBlank()) clamped = 2
        if (clamped >= 2 && changed.isNullOrBlank()) clamped = 1
        return clamped
    }

    /** XP for a feat of [tier] when [priorSameKind] feats of the same kind already happened this chapter. */
    fun featXp(
        tier: Int,
        priorSameKind: Int,
    ): Int {
        val base = config.tierXp.getOrNull(tier.coerceIn(MIN_TIER, MAX_TIER)) ?: return 0
        val decay = config.repeatDecay.getOrNull(priorSameKind.coerceAtLeast(0)) ?: config.repeatDecay.lastOrNull() ?: 1.0
        return (base * decay).roundToInt()
    }

    /** Cumulative XP needed to be at [level]; 0 for level 1. */
    fun xpToReach(level: Int): Int {
        val steps = (level.coerceIn(1, config.levelCap) - 1).toDouble()
        return (config.levelXp.base * steps.pow(config.levelXp.exponent)).roundToInt()
    }

    /** XP a character on [level] needs to reach the next one; 0 at the cap. */
    fun levelSpan(level: Int): Int {
        val current = level.coerceIn(1, config.levelCap)
        return if (current >= config.levelCap) 0 else xpToReach(current + 1) - xpToReach(current)
    }

    /** The level of a character that started at [baseLevel] and has earned [earnedXp] since. */
    fun levelFor(
        baseLevel: Int,
        earnedXp: Int,
    ): Int {
        val start = baseLevel.coerceIn(1, config.levelCap)
        val total = xpToReach(start) + earnedXp.coerceAtLeast(0)
        var level = start
        while (level < config.levelCap && xpToReach(level + 1) <= total) level++
        return level
    }

    /** Most XP an event may give a character on [level]. */
    fun eventBudget(level: Int): Int = budget(level, config.eventXpCapPct)

    /** Most XP a chapter may give a character on [level]. */
    fun chapterBudget(level: Int): Int = budget(level, config.chapterXpCapPct)

    /**
     * Scales [awards] down proportionally when their sum is over [budget], keeping the order and the
     * relative weight of the feats. Awards under the budget are returned untouched.
     */
    fun fitToBudget(
        awards: List<Int>,
        budget: Int,
    ): List<Int> {
        val total = awards.sum()
        if (total <= budget) return awards
        if (budget <= 0) return awards.map { 0 }
        return awards.map { floor(it.toDouble() * budget / total).toInt() }
    }

    private fun budget(
        level: Int,
        pct: Double,
    ): Int = floor(levelSpan(level) * pct).toInt()

    private companion object {
        const val MIN_TIER = 0
        const val MAX_TIER = 5
    }
}
