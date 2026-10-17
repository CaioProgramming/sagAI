package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import com.ilustris.sagai.features.characters.ui.components.transformTextWithContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Edits the way the platform delivers them to `BasicTextField`: the keyboard changes the text
 * behind the input, then [processInputChange] decides what the state becomes (null = rejected).
 */
class ExpressiveTagHelpersTest {
    private fun value(
        text: String,
        cursor: Int = text.length,
    ) = TextFieldValue(text, TextRange(cursor))

    private fun backspace(previous: TextFieldValue): TextFieldValue? {
        val s = previous.selection
        val text = previous.text
        val (from, to) =
            if (s.start != s.end) {
                minOf(s.start, s.end) to maxOf(s.start, s.end)
            } else {
                if (s.start == 0) return previous
                s.start - 1 to s.start
            }
        val next = TextFieldValue(text.removeRange(from, to), TextRange(from))
        return processInputChange(previous, next, 2000)
    }

    private fun deleteForward(previous: TextFieldValue): TextFieldValue? {
        val s = previous.selection
        if (s.start != s.end || s.start >= previous.text.length) return previous
        val next = TextFieldValue(previous.text.removeRange(s.start, s.start + 1), TextRange(s.start))
        return processInputChange(previous, next, 2000)
    }

    private fun type(
        previous: TextFieldValue,
        char: Char,
    ): TextFieldValue? {
        val s = previous.selection
        val from = minOf(s.start, s.end)
        val to = maxOf(s.start, s.end)
        val text = previous.text.replaceRange(from, to, char.toString())
        return processInputChange(previous, TextFieldValue(text, TextRange(from + 1)), 2000)
    }

    private fun transformed(text: String): TransformedText =
        transformTextWithContent(
            mainCharacter = null,
            characters = emptyList(),
            wiki = emptyList(),
            text = text,
            genreColor = Color.Red,
            tagBackgroundColor = Color.Black,
            textColor = Color.White,
            headerFont = null,
            bodyFont = null,
            tagMarkerLabels = mapOf("action" to "Ação", "think" to "Pensar", "narrator" to "Narrador"),
            thinkTagSurfaceColor = Color.Gray,
            annotateMentions = false,
        )

    // ── Bugs the player reported ─────────────────────────────────────────────────────────────

    @Test
    fun `backspace at the end of a closed tag removes one character, not the whole tag`() {
        val before = value("<action>abc</action>", cursor = "<action>abc".length)

        val after = backspace(before)

        assertEquals("<action>ab</action>", after?.text)
    }

    @Test
    fun `a stray closing bracket outside any tag can be deleted`() {
        val before = value("<action>a</action> >")

        val after = backspace(before)

        assertEquals("<action>a</action> ", after?.text)
    }

    @Test
    fun `a stray opening bracket can be deleted while a tag exists elsewhere`() {
        val before = value("<action>a</action> <")

        val after = backspace(before)

        assertEquals("<action>a</action> ", after?.text)
    }

    @Test
    fun `wrapping a selection made right to left does not crash`() {
        val reversed = TextFieldValue("hello world", TextRange(8, 2))

        val wrapped = insertExpressiveTag(reversed, ExpressiveTag.ACTION)

        assertEquals("he<action>llo wo</action>rld", wrapped.text)
    }

    @Test
    fun `tapping the end of a tag's content puts the cursor inside the tag`() {
        val text = "<action>abc</action>"
        val contentEnd = "<action>abc".length
        val mapping = transformed(text).offsetMapping

        val endOfContentOnScreen = mapping.originalToTransformed(contentEnd)

        assertEquals(contentEnd, mapping.transformedToOriginal(endOfContentOnScreen))
    }

    @Test
    fun `backspace right after a closing tag removes the last visible character`() {
        val before = value("<action>abc</action>")

        val after = backspace(before)

        assertEquals("<action>ab</action>", after?.text)
        assertEquals("<action>ab".length, after?.selection?.start)
    }

    @Test
    fun `backspace at the start of a tag's content takes the tag off and keeps the text`() {
        val before = value("x<action>abc</action>", cursor = "x<action>".length)

        val after = backspace(before)

        assertEquals("xabc", after?.text)
        assertEquals(1, after?.selection?.start)
    }

    @Test
    fun `backspace at the start of an empty tag removes the tag`() {
        val before = value("x<action></action>", cursor = "x<action>".length)

        assertEquals("x", backspace(before)?.text)
    }

    @Test
    fun `delete at the end of a tag's content removes the next visible character, not the tag`() {
        val before = value("<action>abc</action>yz", cursor = "<action>abc".length)

        val after = deleteForward(before)

        assertEquals("<action>abc</action>z", after?.text)
    }

    @Test
    fun `tapping a tag's marker lands at the start of its content`() {
        val mapping = transformed("<action>abc</action>").offsetMapping

        assertEquals(0, mapping.transformedToOriginal(0))
        assertEquals("<action>".length, mapping.transformedToOriginal(2))
    }

    @Test
    fun `typing a tag by hand keeps it`() {
        var state = value("")
        for (c in "<action>") state = type(state, c) ?: state
        for (c in "hi") state = type(state, c) ?: state

        assertEquals("<action>hi", state.text)
    }

    // ── Invariants ───────────────────────────────────────────────────────────────────────────

    @Test
    fun `the offset mapping is valid and monotonic for any tag soup`() {
        val failures = mutableListOf<String>()
        val random = Random(7)
        repeat(3000) {
            val text = randomSoup(random)
            runCatching {
                val result = transformed(text)
                val transformedLength = result.text.text.length
                var previous = 0
                for (o in 0..text.length) {
                    val t = result.offsetMapping.originalToTransformed(o)
                    assertTrue("o2t($o)=$t outside 0..$transformedLength", t in 0..transformedLength)
                    assertTrue("o2t not monotonic at $o", t >= previous)
                    previous = t
                }
                for (t in 0..transformedLength) {
                    val o = result.offsetMapping.transformedToOriginal(t)
                    assertTrue("t2o($t)=$o outside 0..${text.length}", o in 0..text.length)
                }
            }.onFailure { failures += "'$text' -> ${it.message}" }
        }
        assertTrue(failures.take(5).joinToString("\n"), failures.isEmpty())
    }

    @Test
    fun `no sequence of edits throws`() {
        val failures = mutableListOf<String>()
        val random = Random(42)
        val alphabet = "<>/ab cinothkeralp"
        repeat(1500) { run ->
            var state = value("")
            val trace = StringBuilder()
            runCatching {
                repeat(40) {
                    when (random.nextInt(9)) {
                        0, 1, 2 -> {
                            val c = alphabet[random.nextInt(alphabet.length)]
                            trace.append("type($c) ")
                            state = type(state, c) ?: state
                        }

                        3 -> {
                            trace.append("backspace ")
                            state = backspace(state) ?: state
                        }

                        4 -> {
                            val tag = ExpressiveTag.entries[random.nextInt(3)]
                            val a = random.nextInt(state.text.length + 1)
                            val b = random.nextInt(state.text.length + 1)
                            trace.append("insert(${tag.tag}, sel=$a..$b) ")
                            state = insertExpressiveTag(state.copy(selection = TextRange(a, b)), tag)
                        }

                        5 -> {
                            val m = transformed(state.text)
                            val t = random.nextInt(m.text.text.length + 1)
                            val o = m.offsetMapping.transformedToOriginal(t)
                            trace.append("tap(t=$t,o=$o) ")
                            state = state.copy(selection = TextRange(o))
                        }

                        7 -> {
                            trace.append("delete ")
                            state = deleteForward(state) ?: state
                        }

                        6 -> {
                            trace.append("escape ")
                            state = escapeCursorFromTagAndClean(state)
                        }

                        else -> {
                            trace.append("finalize ")
                            state = finalizeInputForSend(state)
                        }
                    }
                    transformed(state.text)
                    getCursorInsideTag(state.text, state.selection.start)
                }
            }.onFailure { failures += "${it::class.simpleName}: ${it.message} after [$trace] text='${state.text}'" }
        }
        assertTrue(failures.take(5).joinToString("\n"), failures.isEmpty())
    }

    private fun randomSoup(random: Random): String {
        val pieces = listOf("<action>", "</action>", "<think>", "</think>", "<narrator>", "</narrator>", "<", ">", "abc", " ", "/")
        return buildString { repeat(random.nextInt(1, 9)) { append(pieces[random.nextInt(pieces.size)]) } }
    }
}
