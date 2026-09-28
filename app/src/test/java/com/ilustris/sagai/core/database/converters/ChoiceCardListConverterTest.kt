package com.ilustris.sagai.core.database.converters

import com.ilustris.sagai.features.chapter.data.model.ChoiceOption
import com.ilustris.sagai.features.chapter.data.model.GeneratedChoiceCard
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChoiceCardListConverterTest {
    private val converter = ChoiceCardListConverter()

    @Test
    fun `round trips cards including their hidden tags and tones`() {
        val cards =
            listOf(
                GeneratedChoiceCard(
                    "Save her?",
                    listOf(
                        ChoiceOption("Save her", "self-sacrificing", EmotionalTone.EMPATHETIC),
                        ChoiceOption("Move on", "pragmatic", EmotionalTone.CYNICAL),
                    ),
                ),
            )

        assertEquals(cards, converter.toChoiceCardList(converter.fromChoiceCardList(cards)))
    }

    @Test
    fun `reads hands stored in the original flat shape`() {
        val legacy =
            """[{"choiceTitle":"Save her?","optionAText":"Save her","optionATag":"self-sacrificing",""" +
                """"optionBText":"Move on","optionBTag":"pragmatic"}]"""

        val expected =
            listOf(
                GeneratedChoiceCard(
                    "Save her?",
                    listOf(ChoiceOption("Save her", "self-sacrificing"), ChoiceOption("Move on", "pragmatic")),
                ),
            )
        assertEquals(expected, converter.toChoiceCardList(legacy))
    }

    @Test
    fun `null stays null so an unanswered chapter is distinguishable`() {
        assertNull(converter.fromChoiceCardList(null))
        assertNull(converter.toChoiceCardList(null))
    }
}
