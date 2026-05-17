package com.ilustris.sagai.features.saga.chat.domain.manager

/** Lightweight progression decision derived from saga metadata pointers and counts — no payloads. */
sealed class NarrativeProgressIntent {
    data object CreateAct : NarrativeProgressIntent()

    data class GenerateActIntro(
        val actId: Int,
    ) : NarrativeProgressIntent()

    data class CreateChapter(
        val actId: Int,
    ) : NarrativeProgressIntent()

    data class GenerateChapter(
        val chapterId: Int,
    ) : NarrativeProgressIntent()

    data class GenerateChapterIntro(
        val chapterId: Int,
    ) : NarrativeProgressIntent()

    data class CreateTimeline(
        val chapterId: Int,
    ) : NarrativeProgressIntent()

    data class EvolveTimeline(
        val timelineId: Int,
    ) : NarrativeProgressIntent()

    data class CloseTimeline(
        val chapterId: Int,
    ) : NarrativeProgressIntent()

    data class GenerateAct(
        val actId: Int,
    ) : NarrativeProgressIntent()

    data object GenerateEnding : NarrativeProgressIntent()
}
