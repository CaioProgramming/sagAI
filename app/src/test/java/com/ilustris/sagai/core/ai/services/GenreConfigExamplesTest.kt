package com.ilustris.sagai.core.ai.services

import com.ilustris.sagai.core.ai.model.CharacterExamples
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.features.newsaga.data.model.Genre
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GenreConfigExamplesTest {
    private val remoteConfigService: RemoteConfigService = mockk()
    private val service = GenreConfigService(remoteConfigService, mockk())

    private fun serve(examples: CharacterExamples?) {
        coEvery { remoteConfigService.getJson<CharacterExamples>("heroes_character_examples", any()) } returns examples
    }

    private fun drawn(
        items: List<String>,
        rendered: String,
    ) = items.count { rendered.contains(it) }

    private fun bucket(
        examples: Map<String, Any>,
        key: String,
    ) = examples[key]?.toString().orEmpty()

    @Test
    fun `a genre without examples adds nothing`() =
        runBlocking {
            serve(null)
            assertTrue(service.characterExamples(Genre.HEROES).isEmpty())
        }

    @Test
    fun `empty lists add nothing`() =
        runBlocking {
            serve(CharacterExamples(note = "read these"))
            assertTrue(service.characterExamples(Genre.HEROES).isEmpty())
        }

    @Test
    fun `four looks and three temperaments are drawn and the note comes along`() =
        runBlocking {
            val looks = (1..20).map { "look-number-$it-end" }
            val temperaments = (1..20).map { "temper-number-$it-end" }
            serve(CharacterExamples(note = "never copy these", looks = looks, temperaments = temperaments))

            val examples = service.characterExamples(Genre.HEROES)

            assertEquals(4, drawn(looks, bucket(examples, "ExampleLooks")))
            assertEquals(3, drawn(temperaments, bucket(examples, "ExampleTemperaments")))
            assertEquals("never copy these", examples["ExamplesNote"])
        }

    @Test
    fun `fewer items than the draw size are all used and a missing list adds no bucket`() =
        runBlocking {
            val looks = listOf("look-a-end", "look-b-end")
            serve(CharacterExamples(looks = looks))

            val examples = service.characterExamples(Genre.HEROES)

            assertEquals(2, drawn(looks, bucket(examples, "ExampleLooks")))
            assertFalse(examples.containsKey("ExampleTemperaments"))
        }

    @Test
    fun `the draw changes between calls`() =
        runBlocking {
            val temperaments = (1..20).map { "temper-number-$it-end" }
            serve(CharacterExamples(temperaments = temperaments))

            val draws = (1..8).map { bucket(service.characterExamples(Genre.HEROES), "ExampleTemperaments") }.toSet()

            assertTrue(draws.size > 1)
        }
}
