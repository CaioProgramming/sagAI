package com.ilustris.sagai.features.voicepicker

import com.google.gson.Gson
import com.ilustris.sagai.core.ai.model.TtsVoicesConfig
import com.ilustris.sagai.core.ai.model.Voice
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.voicepicker.presentation.VoicePickerViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VoicePickerTest {
    private val gson = Gson()

    @Test
    fun `a voice from the old remote config shape still parses and is not pickable`() {
        val config = gson.fromJson("""{"voices":[{"id":"puck","gender":"MALE","description":"Upbeat."}]}""", TtsVoicesConfig::class.java)

        val voice = config.voices.single()
        assertNull(voice.name)
        assertNull(voice.suggestedGenres)
        assertFalse(voice.isPickable)
        assertFalse(voice.isSuggestedFor("FANTASY"))
    }

    @Test
    fun `display fields are read and make the voice pickable`() {
        val config =
            gson.fromJson(
                """{"voices":[{"id":"puck","gender":"MALE","description":"Upbeat.","name":"Pip","tagline":"Bagunça.","sampleUrl":"https://x/a.wav","suggestedGenres":["HEROES"],"palette":["#FF0000"]}]}""",
                TtsVoicesConfig::class.java,
            )

        val voice = config.voices.single()
        assertTrue(voice.isPickable)
        assertEquals("Pip", voice.name)
        assertEquals(listOf("#FF0000"), voice.palette)
        assertTrue(voice.isSuggestedFor("heroes"))
    }

    @Test
    fun `picker opens on the first voice suggested for the genre`() {
        val voices =
            listOf(
                Voice(id = "a", name = "A"),
                Voice(id = "b", name = "B", suggestedGenres = listOf("CRIME", "HORROR")),
                Voice(id = "c", name = "C", suggestedGenres = listOf("HORROR")),
            )

        assertEquals(1, VoicePickerViewModel.initialPage(voices, Genre.HORROR))
    }

    @Test
    fun `picker opens on the first voice when nothing is suggested`() {
        val voices = listOf(Voice(id = "a", name = "A"), Voice(id = "b", name = "B"))

        assertEquals(0, VoicePickerViewModel.initialPage(voices, Genre.FANTASY))
        assertEquals(0, VoicePickerViewModel.initialPage(voices, null))
    }
}
