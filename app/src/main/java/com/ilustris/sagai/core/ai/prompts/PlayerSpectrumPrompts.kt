package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatChapters

/**
 * Feeds [com.ilustris.sagai.features.chapter.data.model.Chapter.playerSpectrum] back into
 * generation as an interpretive lens. It shapes characterization only — what structurally
 * happens stays with ContinuitySummary.
 */
object PlayerSpectrumPrompts {
    /** Empty when there is no spectrum yet, so callers can merge it unconditionally. */
    fun lensInstructions(spectrumText: String?): Map<String, Any> {
        if (spectrumText.isNullOrBlank()) return emptyMap()
        return mapOf(
            "playerSpectrumLens" to
                mapOf(
                    "SPECTRUM_READ" to spectrumText.trim(),
                    "SPECTRUM_SCOPE" to
                        "Use the read above only to write subtler characterization of the cast.",
                    "SPECTRUM_NEVER_PLOT" to
                        "Never let it decide what structurally happens; that belongs to the events and continuity.",
                    "SPECTRUM_NEVER_CONFIRM" to
                        "Never resolve, confirm or reward the read outright. Complicate it instead.",
                    "SPECTRUM_TECHNIQUES" to
                        "Prefer ambiguity, questioned reciprocity, or drift toward unhealthy patterns where the story has earned it.",
                    "SPECTRUM_HIDDEN" to
                        "Never mention the read, the player's choices or this lens in the prose.",
                ),
        )
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
        val chapters = saga.flatChapters()
        val index = chapters.indexOfFirst { it.data.id == chapterId }
        val earlier = if (index >= 0) chapters.subList(0, index) else chapters
        return earlier
            .asReversed()
            .firstNotNullOfOrNull { it.data.playerSpectrum?.takeIf { spectrum -> spectrum.isNotBlank() } }
    }
}
