package com.ilustris.sagai.core.ai

import com.google.gson.JsonSyntaxException
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AiOutputGsonTest {
    private data class Numbers(
        val boxed: Int? = null,
        val primitive: Int = 0,
        val big: Long = 0L,
        val label: String = "",
    )

    private fun parse(json: String) = aiOutputGson.fromJson(json, Numbers::class.java)

    @Test
    fun `a decimal string in an Int field rounds to the nearest whole`() {
        // The real failure: SceneSummary.tensionLevel came back as "9.5".
        assertEquals(10, parse("""{"boxed":"9.5"}""").boxed)
        assertEquals(9, parse("""{"boxed":"9.4"}""").boxed)
    }

    @Test
    fun `a decimal number in an Int field rounds too`() {
        assertEquals(8, parse("""{"boxed":7.6,"primitive":7.6}""").boxed)
        assertEquals(8, parse("""{"primitive":7.6}""").primitive)
    }

    @Test
    fun `exact integers and numeric strings still read`() {
        val parsed = parse("""{"boxed":7,"primitive":"3","big":"12"}""")
        assertEquals(7, parsed.boxed)
        assertEquals(3, parsed.primitive)
        assertEquals(12L, parsed.big)
    }

    @Test
    fun `null and missing stay null or default`() {
        assertNull(parse("""{"boxed":null}""").boxed)
        assertEquals(0, parse("""{}""").primitive)
    }

    @Test
    fun `a non-numeric value in an Int field still fails`() {
        try {
            parse("""{"boxed":"high"}""")
            org.junit.Assert.fail("expected JsonSyntaxException")
        } catch (_: JsonSyntaxException) {
        }
    }

    @Test
    fun `strings are untouched`() {
        assertEquals("9.5", parse("""{"label":"9.5"}""").label)
    }

    @Test
    fun `the whole AIGeneration envelope parses with the tension level as a string`() {
        val json = """{"reasoning":"r","data":{"tensionLevel":"9.5"}}"""
        val result = parseAIGenerationFromJson<SceneSummary>(aiOutputGson, json)
        assertEquals(10, result.data?.tensionLevel)
    }
}
