package com.ilustris.sagai.features.chapter.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GeneratedChoiceCardTest {
    private val card =
        GeneratedChoiceCard(
            "Q?",
            listOf(ChoiceOption("A", "read a"), ChoiceOption("B", "read b")),
        )

    @Test
    fun `an index answer picks that option`() {
        assertEquals("B", card.pickedOption("1")?.text)
        assertEquals("A", card.pickedOption("0")?.text)
    }

    @Test
    fun `an answer from before indexes still resolves by its stored label`() {
        assertEquals("B", card.pickedOption("read b")?.text)
    }

    @Test
    fun `an answer that matches nothing picks nothing`() {
        assertNull(card.pickedOption("7"))
        assertNull(card.pickedOption("something else"))
    }
}
