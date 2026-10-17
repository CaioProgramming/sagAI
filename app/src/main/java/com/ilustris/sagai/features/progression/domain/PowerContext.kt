package com.ilustris.sagai.features.progression.domain

import kotlin.math.abs

/** A character of the scene with the level they have right now. */
data class PowerEntry(
    val name: String,
    val level: Int,
)

/**
 * Turns the levels of the characters in a scene into the sentence the reply prompt obeys: who is
 * stronger and by how much, in the vocabulary of the saga's own bands. Without it the narrator
 * would invent tension for a fight that the numbers already decided.
 */
class PowerContext(
    private val config: ProgressionConfig,
) {
    /** Null when there is nothing to compare (fewer than two placed characters). */
    fun describe(entries: List<PowerEntry>): String? {
        val sorted = entries.distinctBy { it.name }.sortedByDescending { it.level }
        if (sorted.size < 2) return null
        val lines =
            sorted
                .flatMapIndexed { index, stronger -> sorted.drop(index + 1).map { stronger to it } }
                .take(MAX_PAIRS)
                .map { (stronger, weaker) -> pair(stronger, weaker) }
        return lines.joinToString("\n")
    }

    fun bandLabel(level: Int): String = config.levelBands.firstOrNull { level in it.from..it.to }?.label.orEmpty()

    private fun pair(
        stronger: PowerEntry,
        weaker: PowerEntry,
    ): String {
        val gap = abs(stronger.level - weaker.level)
        val sentence =
            config.powerGapBands
                .sortedBy { it.maxGap }
                .firstOrNull { gap <= it.maxGap }
                ?.sentence
                .orEmpty()
        val head = "${stronger.name} (level ${stronger.level}, ${bandLabel(stronger.level)}) vs ${weaker.name} (level ${weaker.level}, ${bandLabel(weaker.level)}): gap $gap."
        return "$head $sentence".trim()
    }

    private companion object {
        const val MAX_PAIRS = 6
    }
}
