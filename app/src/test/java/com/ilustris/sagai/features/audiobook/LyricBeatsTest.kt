package com.ilustris.sagai.features.audiobook

import com.ilustris.sagai.features.audiobook.ui.MAX_BEAT_CHARS
import com.ilustris.sagai.features.audiobook.ui.splitBeats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LyricBeatsTest {
    private val long =
        "Ele olhava para a Tati e via alguém que gritava para não ter que ouvir o próprio silêncio, " +
            "e isso era algo que ele reconhecia bem demais."

    @Test
    fun `a short sentence stays one beat`() {
        val text = "Ela sorriu."
        assertEquals(listOf(0 until text.length), splitBeats(text))
    }

    @Test
    fun `a long sentence is split into more than one beat`() {
        assertTrue(splitBeats(long).size > 1)
    }

    @Test
    fun `a comma inside the limit is preferred over an arbitrary word boundary`() {
        val text = "Ele olhou para a janela devagar, depois voltou a escrever a carta que nunca enviaria."
        val beats = splitBeats(text)

        assertEquals("Ele olhou para a janela devagar, ", text.substring(beats[0].first, beats[0].last + 1))
    }

    @Test
    fun `beats tile the sentence exactly`() {
        val beats = splitBeats(long)

        assertEquals(0, beats.first().first)
        beats.zipWithNext().forEach { (a, b) -> assertEquals(a.last + 1, b.first) }
        assertEquals(long.length - 1, beats.last().last)
    }

    @Test
    fun `no beat runs past the limit`() {
        beats@ for (text in listOf(long, long.replace(",", ""), "palavra ".repeat(40).trim())) {
            splitBeats(text).forEach { beat ->
                assertTrue("beat too long: ${beat.count()}", beat.count() <= MAX_BEAT_CHARS)
            }
        }
    }

    @Test
    fun `beats start on word boundaries`() {
        val text = "palavra ".repeat(40).trim()

        splitBeats(text).drop(1).forEach { beat -> assertEquals(' ', text[beat.first - 1]) }
    }

    @Test
    fun `a clause break never strands a tiny tail`() {
        val text = "Ele saiu da sala sem dizer nada para ninguém, então voltou."
        val beats = splitBeats(text)

        beats.forEach { assertTrue(it.count() >= 18) }
    }
}
