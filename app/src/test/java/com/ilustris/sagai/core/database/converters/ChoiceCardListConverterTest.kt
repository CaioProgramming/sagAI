package com.ilustris.sagai.core.database.converters

import com.ilustris.sagai.features.chapter.data.model.GeneratedChoiceCard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChoiceCardListConverterTest {
    private val converter = ChoiceCardListConverter()

    @Test
    fun `round trips cards including their hidden tags`() {
        val cards =
            listOf(
                GeneratedChoiceCard("Save her?", "Save her", "self-sacrificing", "Move on", "pragmatic"),
                GeneratedChoiceCard("Take the power?", "Take it", "ambitious", "Refuse", "principled"),
            )

        assertEquals(cards, converter.toChoiceCardList(converter.fromChoiceCardList(cards)))
    }

    @Test
    fun `null stays null so an unanswered chapter is distinguishable`() {
        assertNull(converter.fromChoiceCardList(null))
        assertNull(converter.toChoiceCardList(null))
    }
}
