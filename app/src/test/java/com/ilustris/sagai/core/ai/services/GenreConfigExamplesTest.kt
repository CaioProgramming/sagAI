package com.ilustris.sagai.core.ai.services

import com.ilustris.sagai.core.ai.model.AppearanceExamples
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.features.newsaga.data.model.Genre
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenreConfigExamplesTest {
    private val remoteConfigService: RemoteConfigService = mockk()
    private val service = GenreConfigService(remoteConfigService, mockk())

    private fun serve(examples: AppearanceExamples?) {
        coEvery { remoteConfigService.getJson<AppearanceExamples>("heroes_appearance_examples", any()) } returns examples
    }

    private fun drawn(
        looks: List<String>,
        rendered: String,
    ) = looks.count { rendered.contains(it) }

    @Test
    fun `a genre without examples adds nothing`() =
        runBlocking {
            serve(null)
            assertTrue(service.appearanceExamples(Genre.HEROES).isEmpty())
        }

    @Test
    fun `an empty list adds nothing`() =
        runBlocking {
            serve(AppearanceExamples(note = "read these", looks = emptyList()))
            assertTrue(service.appearanceExamples(Genre.HEROES).isEmpty())
        }

    @Test
    fun `at most four looks are drawn and the note comes along`() =
        runBlocking {
            val looks = (1..20).map { "look-number-$it-end" }
            serve(AppearanceExamples(note = "never copy these", looks = looks))

            val rendered = service.appearanceExamples(Genre.HEROES).getValue("ExampleLooks").toString()

            assertEquals(4, drawn(looks, rendered))
            assertTrue(rendered.contains("never copy these"))
        }

    @Test
    fun `fewer looks than the draw size are all used`() =
        runBlocking {
            val looks = listOf("look-a-end", "look-b-end")
            serve(AppearanceExamples(looks = looks))

            val rendered = service.appearanceExamples(Genre.HEROES).getValue("ExampleLooks").toString()

            assertEquals(2, drawn(looks, rendered))
        }

    @Test
    fun `the draw changes between calls`() =
        runBlocking {
            val looks = (1..20).map { "look-number-$it-end" }
            serve(AppearanceExamples(looks = looks))

            val draws = (1..8).map { service.appearanceExamples(Genre.HEROES).getValue("ExampleLooks").toString() }.toSet()

            assertTrue(draws.size > 1)
        }
}
