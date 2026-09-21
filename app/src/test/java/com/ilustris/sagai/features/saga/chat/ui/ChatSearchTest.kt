package com.ilustris.sagai.features.saga.chat.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatSearchTest {
    @Test
    fun `matches ignoring case and accents`() {
        val plan = planOf(1 to "Tomamos um café à beira-mar.")

        assertEquals(listOf(1), plan.findMatches("cafe").map { it.messageId })
        assertEquals(listOf(1), plan.findMatches("CAFÉ").map { it.messageId })
        assertEquals(listOf(1), plan.findMatches("beira-MAR").map { it.messageId })
    }

    @Test
    fun `matches the speaker name, not only mentions of it`() {
        val plan =
            planOf(
                1 to "Nada sobre ela aqui." to "Harumi",
                2 to "A Harumi chegou atrasada." to "Milena",
            )

        assertEquals(listOf(1, 2), plan.findMatches("harumi").map { it.messageId })
        assertEquals(listOf(2), plan.findMatches("atrasada").map { it.messageId })
    }

    @Test
    fun `searches inside expressive tags, including collapsed think blocks`() {
        val plan =
            planOf(
                1 to "<think>ela me caça igual um gato</think>",
                2 to "<action>acende um cigarro</action> tudo em ordem.",
            )

        assertEquals(listOf(1), plan.findMatches("gato").map { it.messageId })
        assertEquals(listOf(2), plan.findMatches("cigarro").map { it.messageId })
    }

    @Test
    fun `tag markers themselves are not searchable`() {
        val plan = planOf(1 to "<think>silêncio</think>")

        assertTrue(plan.findMatches("think").isEmpty())
    }

    @Test
    fun `results follow plan order, newest first`() {
        val plan = planOf(3 to "o sal", 2 to "o sal de novo", 1 to "sem nada")

        assertEquals(listOf(3, 2), plan.findMatches("sal").map { it.messageId })
    }

    @Test
    fun `match carries the plan index the list scrolls to`() {
        val plan = planOf(9 to "nada", 7 to "o alvo")

        val match = plan.findMatches("alvo").single()
        assertEquals(7, match.messageId)
        assertEquals(plan.indexOfFirst { it.key == "message-7" }, match.planIndex)
    }

    @Test
    fun `highlight spans every occurrence, matching accents loosely`() {
        val style = SpanStyle(background = Color.Red)
        val text = AnnotatedString("Um café, depois outro cafe.")

        val spans = text.withSearchHighlight("cafe", style).spanStyles

        assertEquals(2, spans.size)
        assertEquals(listOf(3, 22), spans.map { it.start })
        assertEquals(listOf(7, 26), spans.map { it.end })
        assertEquals("café", text.text.substring(spans[0].start, spans[0].end))
    }

    @Test
    fun `highlight keeps the annotations the message already carried`() {
        val existing = SpanStyle(background = Color.Blue)
        val text =
            buildAnnotatedString {
                append("Milena tomou café")
                addStyle(existing, 0, 6)
            }

        val result = text.withSearchHighlight("cafe", SpanStyle(background = Color.Red))

        assertEquals(2, result.spanStyles.size)
        assertTrue(result.spanStyles.any { it.item == existing && it.start == 0 && it.end == 6 })
    }

    @Test
    fun `highlight is a no-op without a match or a term`() {
        val text = AnnotatedString("nada aqui")
        val style = SpanStyle(background = Color.Red)

        assertTrue(text.withSearchHighlight("xyz", style).spanStyles.isEmpty())
        assertTrue(text.withSearchHighlight("", style).spanStyles.isEmpty())
        assertTrue(text.withSearchHighlight("   ", style).spanStyles.isEmpty())
    }

    @Test
    fun `blank query matches nothing`() {
        val plan = planOf(1 to "qualquer coisa")

        assertTrue(plan.findMatches("").isEmpty())
        assertTrue(plan.findMatches("   ").isEmpty())
    }

    private fun planOf(vararg messages: Pair<Int, String>): List<ChatEntry> =
        planOf(*messages.map { it to "Milena" }.toTypedArray())

    @JvmName("planOfWithSpeaker")
    private fun planOf(vararg messages: Pair<Pair<Int, String>, String>): List<ChatEntry> =
        messages.map { (idAndText, speaker) ->
            val (id, text) = idAndText
            ChatEntry.Message(
                content =
                    MessageContent(
                        message =
                            Message(
                                id = id,
                                text = text,
                                senderType = SenderType.CHARACTER,
                                speakerName = speaker,
                                timelineId = 1000,
                            ),
                        reactions = emptyList(),
                    ),
                chapterId = 10,
                actId = 1,
            )
        }
}
