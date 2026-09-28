package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.data.model.ChapterContent
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerSpectrumPromptsTest {
    private fun chapter(
        id: Int,
        actId: Int,
        spectrum: String? = null,
    ) = ChapterContent(data = Chapter(id = id, actId = actId, playerSpectrum = spectrum))

    private fun saga(vararg acts: Pair<Int, List<ChapterContent>>) =
        SagaContent(
            data = Saga(),
            acts = acts.map { (id, chapters) -> ActContent(data = Act(id = id), chapters = chapters) },
        )

    @Test
    fun `lens is empty without a spectrum so it can be merged unconditionally`() {
        assertTrue(PlayerSpectrumPrompts.lensInstructions(null).isEmpty())
        assertTrue(PlayerSpectrumPrompts.lensInstructions("   ").isEmpty())
    }

    @Test
    fun `lens carries the spectrum text`() {
        val lens = PlayerSpectrumPrompts.lensInstructions("Loyal to a fault.")
        @Suppress("UNCHECKED_CAST")
        val bucket = lens["playerSpectrumLens"] as Map<String, String>
        assertEquals("Loyal to a fault.", bucket["SPECTRUM_READ"])
    }

    @Test
    fun `previous spectrum is the nearest earlier non-blank one`() {
        val saga =
            saga(
                1 to listOf(chapter(1, 1, "first read"), chapter(2, 1, "second read"), chapter(3, 1, "  ")),
                2 to listOf(chapter(4, 2)),
            )

        assertEquals("second read", PlayerSpectrumPrompts.previousSpectrum(saga, chapterId = 4))
    }

    @Test
    fun `previous spectrum crosses act boundaries`() {
        val saga = saga(1 to listOf(chapter(1, 1, "act one read")), 2 to listOf(chapter(2, 2)))

        assertEquals("act one read", PlayerSpectrumPrompts.previousSpectrum(saga, chapterId = 2))
    }

    @Test
    fun `previous spectrum ignores the chapter itself and later ones`() {
        val saga = saga(1 to listOf(chapter(1, 1), chapter(2, 1, "own read"), chapter(3, 1, "later read")))

        assertNull(PlayerSpectrumPrompts.previousSpectrum(saga, chapterId = 2))
    }

    @Test
    fun `chapter exclusions keep spectrum data out of the raw context dump`() {
        listOf("playerChoiceCards", "playerSpectrum", "playerChoiceAnswers").forEach {
            assertTrue("$it must be excluded", it in ChapterPrompts.CHAPTER_EXCLUSIONS)
        }
    }
}
