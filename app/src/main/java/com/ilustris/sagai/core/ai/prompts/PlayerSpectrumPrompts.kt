package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.home.data.model.flatChapters

/**
 * Feeds [com.ilustris.sagai.features.chapter.data.model.Chapter.playerSpectrum] back into
 * generation as an interpretive lens. It shapes characterization only — what structurally
 * happens stays with ContinuitySummary.
 */
object PlayerSpectrumPrompts {
    /**
     * Empty when there is no spectrum yet, so callers can merge it unconditionally. Rendered as
     * one `KEY: text` block, the same shape every blueprint's own directives/rules take once
     * flattened by renderInstructions() — a nested map here reached the model as a raw
     * `{KEY=text, ...}` toString instead.
     */
    fun lensInstructions(spectrumText: String?): Map<String, Any> {
        if (spectrumText.isNullOrBlank()) return emptyMap()
        val rules =
            linkedMapOf(
                "SPECTRUM_READ" to spectrumText.trim(),
                "SPECTRUM_SCOPE" to "Use the read above only to write subtler characterization of the cast.",
                "SPECTRUM_NEVER_PLOT" to
                    "Never let it decide what structurally happens; that belongs to the events and continuity.",
                "SPECTRUM_NEVER_CONFIRM" to "Never resolve, confirm or reward the read outright. Complicate it instead.",
                "SPECTRUM_TECHNIQUES" to
                    "Prefer ambiguity, questioned reciprocity, or drift toward unhealthy patterns where the story has earned it.",
                "SPECTRUM_HIDDEN" to "Never mention the read, the player's choices or this lens in the prose.",
            )
        return mapOf(LENS_BUCKET to rules.entries.joinToString("\n") { (key, text) -> "$key: $text" })
    }

    const val LENS_BUCKET = "PlayerSpectrumLens"

    const val CARDS_BUCKET = "PlayerReadForChoiceCards"

    /**
     * Lets the cards adapt to the player: the current read goes to the card generator, which is
     * told to test it instead of confirming it. The adaptation lives in *which* dilemmas get dealt —
     * the questions themselves stay plain, and the read is never hinted at. Empty before the first
     * read exists, so callers can merge it unconditionally.
     */
    fun cardsLensInstructions(spectrumText: String?): Map<String, Any> {
        if (spectrumText.isNullOrBlank()) return emptyMap()
        val rules =
            linkedMapOf(
                "CARDS_READ" to spectrumText.trim(),
                "CARDS_TEST_THE_READ" to
                    "Choose dilemmas that test the read above: raise the cost of the pattern the player has shown, and make the option they would usually avoid tempting.",
                "CARDS_NEVER_CONFIRM" to
                    "At least one card must be a dilemma where the read predicts one answer and the other is just as defensible.",
                "CARDS_STAY_PLAIN" to
                    "Questions and options stay simple and concrete, in the story's own terms. Never hint at an analysis, a pattern or this read.",
            )
        return mapOf(CARDS_BUCKET to rules.entries.joinToString("\n") { (key, text) -> "$key: $text" })
    }

    /**
     * The spectrum a chapter should be seeded with: the most recent non-blank one among the
     * chapters that come before [chapterId], across acts so the trajectory survives act
     * boundaries. Null for the saga's first answered chapter.
     */
    fun previousSpectrum(
        saga: SagaContent,
        chapterId: Int,
    ): String? {
        val chapters = saga.flatChapters().map { it.data }
        val index = chapters.indexOfFirst { it.id == chapterId }
        return latestSpectrum(if (index >= 0) chapters.subList(0, index) else chapters)
    }

    /**
     * The read currently in force for live generation — chat replies, the next chapter's
     * introduction: the most recent answered one anywhere in the saga. The chapter being played
     * has none of its own until it closes, so this is always the last closure's read.
     */
    fun latestSpectrum(saga: SagaContent): String? = latestSpectrum(saga.flatChapters().map { it.data })

    fun latestSpectrum(saga: SagaMetadata): String? = latestSpectrum(saga.flatChapters().map { it.data })

    private fun latestSpectrum(chaptersInOrder: List<Chapter>): String? =
        chaptersInOrder
            .asReversed()
            .firstNotNullOfOrNull { it.playerSpectrum?.takeIf { spectrum -> spectrum.isNotBlank() } }
}
