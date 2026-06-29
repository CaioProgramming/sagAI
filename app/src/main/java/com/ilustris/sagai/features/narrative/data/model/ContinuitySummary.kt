package com.ilustris.sagai.features.narrative.data.model

import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary

/**
 * Canonical narrative memory at chapter or act level.
 * Unlike [SceneSummary], this captures durable story facts rather than momentary scene state.
 */
data class ContinuitySummary(
    val establishedFacts: List<String> = emptyList(),
    val openThreads: List<String> = emptyList(),
    val consequences: List<String> = emptyList(),
    val characterStates: List<String> = emptyList(),
    val persistentSetups: List<String> = emptyList(),
) {
    fun isBlank(): Boolean =
        establishedFacts.isEmpty() &&
            openThreads.isEmpty() &&
            consequences.isEmpty() &&
            characterStates.isEmpty() &&
            persistentSetups.isEmpty()
}

fun ContinuitySummary.mergeWith(other: ContinuitySummary): ContinuitySummary =
    ContinuitySummary(
        establishedFacts = establishedFacts.mergeDeduped(other.establishedFacts),
        openThreads = openThreads.mergeDeduped(other.openThreads),
        consequences = consequences.mergeDeduped(other.consequences),
        characterStates = characterStates.mergeDeduped(other.characterStates),
        persistentSetups = persistentSetups.mergeDeduped(other.persistentSetups),
    )

/** Facts, setups and threads suitable for long-range continuity injection. */
fun ContinuitySummary.distantCanonSlice(): ContinuitySummary =
    ContinuitySummary(
        establishedFacts = establishedFacts,
        openThreads = openThreads,
        persistentSetups = persistentSetups,
    )

fun List<SceneSummary>.mergeSceneSummaries(): ContinuitySummary {
    if (isEmpty()) return ContinuitySummary()

    var rollup = ContinuitySummary()
    forEach { scene ->
        rollup =
            rollup.mergeWith(
                ContinuitySummary(
                    establishedFacts =
                        listOfNotNull(
                            scene.establishedFacts,
                            scene.relevantPastContext,
                        ).flatten(),
                    openThreads = scene.possibleOutcomes.orEmpty(),
                    consequences = scene.worldStateChanges.orEmpty(),
                    characterStates =
                        buildList {
                            if (scene.charactersPresent.isNotEmpty()) {
                                add("Present: ${scene.charactersPresent.joinToString()}")
                            }
                            scene.currentLocation.takeIf { it.isNotBlank() }?.let {
                                add("Last known location: $it")
                            }
                        },
                ),
            )
    }
    return rollup
}

private fun List<String>.mergeDeduped(other: List<String>): List<String> =
    (this + other).dedupeFacts()

fun List<String>.dedupeFacts(): List<String> {
    val seen = mutableSetOf<String>()
    return mapNotNull { fact ->
        val normalized = fact.trim()
        if (normalized.isBlank()) return@mapNotNull null
        val key = normalized.lowercase()
        if (seen.add(key)) normalized else null
    }
}

fun List<ContinuitySummary>.mergeAll(): ContinuitySummary =
    fold(ContinuitySummary()) { acc, summary -> acc.mergeWith(summary) }

fun ContinuitySummary.limitDistantFacts(maxItems: Int): ContinuitySummary {
    if (maxItems <= 0) return ContinuitySummary()
    val combined =
        (establishedFacts + persistentSetups + openThreads).dedupeFacts().take(maxItems)
    val factsEnd = minOf(establishedFacts.dedupeFacts().size, combined.size)
    val facts = combined.take(factsEnd)
    val remaining = combined.drop(factsEnd)
    val setupsEnd = minOf(persistentSetups.dedupeFacts().size, remaining.size)
    val setups = remaining.take(setupsEnd)
    val threads = remaining.drop(setupsEnd)
    return ContinuitySummary(
        establishedFacts = facts,
        persistentSetups = setups,
        openThreads = threads,
    )
}
