package com.ilustris.sagai.features.widgets.domain

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetLinesTest {
    private fun facts(
        type: LinesType = LinesType.QUOTE,
        objective: String? = "Reach the temple before dawn",
        avoid: List<String> = emptyList(),
        events: List<String> = listOf("The bridge: fell"),
    ) = LinesFacts(
        type = type,
        characterName = "Ren",
        occupation = "Ronin",
        personality = "Quiet and exact",
        backstory = "Raised by the monastery",
        sagaTitle = "Two Blades",
        genreAesthetic = "Ink and blood",
        arcs = listOf("Silence: learned to wait"),
        events = events,
        relationships = listOf("Ren - Sayuri: sisters"),
        objective = objective,
        count = 10,
        avoid = avoid,
    )

    @Test
    fun `the prompt carries the character but never chat messages`() {
        val prompt = WidgetLinesPrompt.build(facts())
        assertTrue(prompt.contains("Name: Ren"))
        assertTrue(prompt.contains("Silence: learned to wait"))
        assertTrue(prompt.contains("Ren - Sayuri: sisters"))
        assertFalse(prompt.contains("# MESSAGES"))
    }

    @Test
    fun `only hooks mention the current objective`() {
        assertFalse(WidgetLinesPrompt.build(facts(type = LinesType.QUOTE)).contains("Reach the temple"))
        assertTrue(WidgetLinesPrompt.build(facts(type = LinesType.HOOK)).contains("Reach the temple"))
    }

    @Test
    fun `lines already written are listed so a second batch does not repeat them`() {
        val prompt = WidgetLinesPrompt.build(facts(avoid = listOf("Blood dries. The name does not.")))
        assertTrue(prompt.contains("ALREADY WRITTEN"))
        assertTrue(prompt.contains("Blood dries. The name does not."))
    }

    @Test
    fun `the prompt stays small however long the history is`() {
        val long = List(200) { "Event $it: " + "x".repeat(500) }
        val prompt = WidgetLinesPrompt.build(facts(events = long))
        assertTrue("prompt was ${prompt.length} chars", prompt.length < 4_000)
    }

    @Test
    fun `instructions differ by kind`() {
        val quote = WidgetLinesPrompt.instructions(LinesType.QUOTE).toString()
        val hook = WidgetLinesPrompt.instructions(LinesType.HOOK).toString()
        assertTrue(quote.contains("Quotes"))
        assertTrue(hook.contains("Never guilt"))
    }

    @Test
    fun `sanitizer drops blanks, repeats, list markers and wrapping quotes`() {
        val cleaned =
            WidgetLinesSanitizer.clean(
                listOf("  1. “The wind forgets.” ", "", "the wind forgets", "- Silence has an owner.", "   "),
                LinesType.QUOTE,
            )
        assertEquals(listOf("The wind forgets.", "Silence has an owner."), cleaned)
    }

    @Test
    fun `sanitizer drops lines too long for the widget`() {
        val tooLong = "a".repeat(WidgetLinesPrompt.QUOTE_MAX_CHARS + 1)
        val fits = "b".repeat(WidgetLinesPrompt.QUOTE_MAX_CHARS)
        assertEquals(listOf(fits), WidgetLinesSanitizer.clean(listOf(tooLong, fits), LinesType.QUOTE))
    }

    @Test
    fun `sanitizer skips what the player already has and caps the batch`() {
        val avoid = listOf("Already here.")
        val raw = listOf("Already here!") + List(20) { "Line number $it is new" }
        val cleaned = WidgetLinesSanitizer.clean(raw, LinesType.HOOK, avoid)
        assertFalse(cleaned.any { it.startsWith("Already") })
        assertEquals(WidgetLimits.BATCH_SIZE, cleaned.size)
    }

    @Test
    fun `a character with almost nothing recorded is flagged`() {
        assertTrue(WidgetLinesSanitizer.hasLittleHistory(eventCount = 1, arcCount = 0))
        assertFalse(WidgetLinesSanitizer.hasLittleHistory(eventCount = 2, arcCount = 0))
        assertFalse(WidgetLinesSanitizer.hasLittleHistory(eventCount = 0, arcCount = 2))
    }

    @Test
    fun `a widget configuration survives being stored as json`() {
        val config =
            WidgetConfig(
                type = LinesType.HOOK,
                sagaId = 3,
                characterId = 9,
                characterName = "Ren",
                lines = listOf("One.", "Two."),
                rotation = RotationRule.DAILY,
                discreet = true,
                seed = 12345,
            )
        val gson = Gson()
        assertEquals(config, gson.fromJson(gson.toJson(config), WidgetConfig::class.java))
    }
}
