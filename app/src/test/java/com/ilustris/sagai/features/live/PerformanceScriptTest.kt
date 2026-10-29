package com.ilustris.sagai.features.live

import com.ilustris.sagai.features.live.presentation.CaptionTimeline
import com.ilustris.sagai.features.saga.chat.data.voicing.BlockType
import com.ilustris.sagai.features.saga.chat.data.voicing.MessageBlocks
import com.ilustris.sagai.features.saga.chat.data.voicing.NARRATOR_SPEAKER
import com.ilustris.sagai.features.saga.chat.data.voicing.PerformanceLine
import com.ilustris.sagai.features.saga.chat.data.voicing.PerformanceScript
import com.ilustris.sagai.features.saga.chat.data.voicing.PerformanceScripts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PerformanceScriptTest {
    private val message =
        "<narrator>A tocha estala.</narrator> <action>Abaixa o capuz.</action> " +
            "Então você ainda acha que isso é sobre passar. <think>Ele segura a espada como meu pai.</think>"

    @Test
    fun `message splits into typed blocks in order`() {
        val blocks = MessageBlocks.split(message)

        assertEquals(listOf(BlockType.NARRATOR, BlockType.ACTION, BlockType.DIALOGUE, BlockType.THINK), blocks.map { it.type })
        assertEquals((0..3).toList(), blocks.map { it.index })
    }

    @Test
    fun `deterministic script voices narration and dialogue only`() {
        val script = PerformanceScripts.deterministic(MessageBlocks.split(message), speaker = "Irin")

        assertEquals(listOf(NARRATOR_SPEAKER, "Irin"), script.lines.map { it.speaker })
        assertEquals(listOf(0, 2), script.lines.map { it.block })
    }

    @Test
    fun `deterministic script turns vocal actions into cues on the nearest line`() {
        val lead = PerformanceScripts.deterministic(MessageBlocks.split("<action>Suspira devagar.</action> Tanto faz."), speaker = "Kuri")
        assertEquals("[sighs] Tanto faz.", lead.lines.single().text)

        val trail = PerformanceScripts.deterministic(MessageBlocks.split("Continue latindo. <action>tossindo sangue</action>"), speaker = "Kuri")
        assertEquals("Continue latindo. [coughs]", trail.lines.single().text)
    }

    @Test
    fun `silent actions add no cue`() {
        assertEquals(null, PerformanceScripts.vocalCue("puxa a espada e abre a porta"))
        assertEquals(null, PerformanceScripts.vocalCue("sorri de canto"))
        assertEquals(null, PerformanceScripts.vocalCue("para sob a chuva"))
        assertEquals("[laughs]", PerformanceScripts.vocalCue("ri baixo"))
    }

    @Test
    fun `narrator message has only the narrator speaking`() {
        val script = PerformanceScripts.deterministic(MessageBlocks.split("A chuva para."), speaker = null)

        assertTrue(script.lines.all { it.isNarrator })
    }

    @Test
    fun `sanitize drops unknown speakers, blank lines and leaked tags`() {
        val raw =
            PerformanceScript(
                style = " calma ",
                lines =
                    listOf(
                        PerformanceLine("NARRATOR", "A tocha estala.", 0),
                        PerformanceLine("Dorn", "Eu não disse isso.", 2),
                        PerformanceLine("irin", "[sighs] <action>x</action>Então você acha.", 2),
                        PerformanceLine("Irin", "   ", 3),
                        PerformanceLine("Irin", "Fim.", 99),
                    ),
            )

        val clean = PerformanceScripts.sanitize(raw, speaker = "Irin", blockCount = 4)

        assertEquals("calma", clean.style)
        assertEquals(listOf(NARRATOR_SPEAKER, "Irin", "Irin"), clean.lines.map { it.speaker })
        assertEquals("[sighs] xEntão você acha.", clean.lines[1].text)
        assertEquals(-1, clean.lines[2].block)
    }

    @Test
    fun `caption timeline splits the clip by spoken length ignoring cues`() {
        val script =
            PerformanceScript(
                lines =
                    listOf(
                        PerformanceLine(NARRATOR_SPEAKER, "abcd", 0),
                        PerformanceLine("Irin", "[sighs] abcdefghijkl", 2),
                    ),
            )

        val timeline = CaptionTimeline.from(script, durationMs = 1600)

        assertEquals(0, timeline.lineAt(0)?.block)
        assertEquals(0, timeline.lineAt(399)?.block)
        assertEquals(2, timeline.lineAt(400)?.block)
        assertEquals(2, timeline.lineAt(1599)?.block)
    }
}
