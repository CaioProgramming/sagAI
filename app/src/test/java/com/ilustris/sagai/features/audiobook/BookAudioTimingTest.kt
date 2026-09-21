package com.ilustris.sagai.features.audiobook

import com.ilustris.sagai.features.audiobook.data.usecase.BookAudioSegmenter
import com.ilustris.sagai.features.audiobook.data.usecase.SpokenWord
import com.ilustris.sagai.features.audiobook.data.usecase.TimingAligner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BookAudioTimingTest {
    private val pages =
        listOf(
            "A chuva caía sobre Valdoria.\n\nMira, cansada, empurrou a porta da taverna.",
            "Lá dentro, ninguém ergueu os olhos. Ela sorriu.",
        )

    @Test
    fun `packs paragraphs across pages up to the limit`() {
        val plans = BookAudioSegmenter.plan(pages, maxChars = 80)

        assertEquals(2, plans.size)
        assertEquals("A chuva caía sobre Valdoria.\n\nMira, cansada, empurrou a porta da taverna.", plans[0].text)
        assertEquals(1, plans[1].startPageIndex)
    }

    @Test
    fun `one big segment covers the whole section`() {
        val plans = BookAudioSegmenter.plan(pages, maxChars = 5000)

        assertEquals(1, plans.size)
        assertEquals(0, plans[0].startPageIndex)
        assertEquals(1, plans[0].endPageIndex)
        assertEquals(pages[1].length, plans[0].endChar)
    }

    @Test
    fun `long paragraph falls back to sentences`() {
        val plans = BookAudioSegmenter.plan(listOf(pages[1]), maxChars = 36)

        assertEquals(listOf("Lá dentro, ninguém ergueu os olhos.", "Ela sorriu."), plans.map { it.text })
    }

    @Test
    fun `rebuilt range hashes like the original plan`() {
        val plan = BookAudioSegmenter.plan(pages, maxChars = 5000).first()
        val rebuilt = BookAudioSegmenter.between(pages, 0, plan.startPageIndex, plan.startChar, plan.endPageIndex, plan.endChar)

        assertEquals(plan.textHash, rebuilt.textHash)
    }

    @Test
    fun `aligns spoken words to written text despite punctuation, accents and a skipped word`() {
        val plan = BookAudioSegmenter.plan(listOf(pages[1]), maxChars = 5000).first()
        val spoken =
            listOf(
                SpokenWord("la", 100, 300),
                SpokenWord("dentro", 300, 700),
                SpokenWord("ninguem", 900, 1400),
                // "ergueu" never heard
                SpokenWord("os", 2000, 2100),
                SpokenWord("olhos", 2100, 2600),
                SpokenWord("ela", 3000, 3200),
                SpokenWord("sorriu", 3200, 3800),
            )

        val alignment = TimingAligner.align(listOf(pages[1]), plan, spoken, durationMs = 4000)

        assertEquals(8, alignment.timings.size)
        assertEquals(7f / 8f, alignment.score, 0.001f)
        val ergueu = alignment.timings[3]
        assertEquals("ergueu", pages[1].substring(ergueu.charStart, ergueu.charEnd))
        assertTrue(ergueu.startMs >= 1400 && ergueu.endMs <= 2000)
        assertTrue(alignment.timings.zipWithNext().all { (a, b) -> a.startMs <= b.startMs })
    }

    @Test
    fun `estimate spans the whole clip with longer pauses after sentences`() {
        val plan = BookAudioSegmenter.plan(listOf(pages[1]), maxChars = 5000).first()

        val timings = TimingAligner.estimate(listOf(pages[1]), plan, durationMs = 7000)

        assertEquals(0, timings.first().startMs)
        assertEquals(7000, timings.last().endMs)
        val olhos = timings[5]
        val ela = timings[6]
        assertTrue(olhos.endMs - olhos.startMs > ela.endMs - ela.startMs)
    }
}
