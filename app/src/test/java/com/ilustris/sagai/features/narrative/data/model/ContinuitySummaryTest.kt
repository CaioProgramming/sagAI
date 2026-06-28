package com.ilustris.sagai.features.narrative.data.model

import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContinuitySummaryTest {
    @Test
    fun `mergeSceneSummaries deduplicates established facts`() {
        val summaries =
            listOf(
                SceneSummary(
                    currentLocation = "Tavern",
                    charactersPresent = listOf("Alice"),
                    immediateObjective = null,
                    currentConflict = null,
                    mood = null,
                    currentTimeOfDay = null,
                    establishedFacts = listOf("Alice met Bob", "The ring was stolen"),
                    relevantPastContext = listOf("Alice met Bob"),
                    worldStateChanges = listOf("Tavern burned down"),
                    possibleOutcomes = listOf("Find the thief"),
                ),
                SceneSummary(
                    currentLocation = "Forest",
                    charactersPresent = listOf("Alice", "Bob"),
                    immediateObjective = null,
                    currentConflict = null,
                    mood = null,
                    currentTimeOfDay = null,
                    establishedFacts = listOf("Bob joined the quest"),
                    worldStateChanges = listOf("Forest path blocked"),
                    possibleOutcomes = listOf("Find the thief", "Turn back"),
                ),
            )

        val rollup = summaries.mergeSceneSummaries()

        assertEquals(3, rollup.establishedFacts.size)
        assertTrue(rollup.establishedFacts.contains("Alice met Bob"))
        assertTrue(rollup.establishedFacts.contains("The ring was stolen"))
        assertTrue(rollup.establishedFacts.contains("Bob joined the quest"))
        assertEquals(2, rollup.consequences.size)
        assertEquals(2, rollup.openThreads.size)
    }

    @Test
    fun `dedupeFacts is case insensitive`() {
        val deduped =
            listOf(
                "The King is dead",
                "the king is dead",
                "  The King is dead  ",
            ).dedupeFacts()

        assertEquals(1, deduped.size)
    }

    @Test
    fun `limitDistantFacts caps combined items`() {
        val summary =
            ContinuitySummary(
                establishedFacts = (1..10).map { "Fact $it" },
                persistentSetups = (1..10).map { "Setup $it" },
                openThreads = (1..10).map { "Thread $it" },
            )

        val limited = summary.limitDistantFacts(5)

        assertEquals(
            5,
            limited.establishedFacts.size +
                limited.persistentSetups.size +
                limited.openThreads.size,
        )
    }
}
