package com.ilustris.sagai.features.saga.chat.domain.manager

import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.features.home.data.model.ActMetadata
import com.ilustris.sagai.features.home.data.model.ChapterMetadata
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.home.data.model.TimelineMetadata
import com.ilustris.sagai.features.home.data.model.toNarrativeMetadata
import com.ilustris.sagai.features.saga.chat.data.model.shouldEnsureSceneSummary
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeCheck.validateProgression

object NarrativeCheck {
    /**
     * Decide the next progression step using [SagaMetadata] pointers only (no heavy relations).
     * Predicate order and thresholds mirror the historical [validateProgression] behaviour on content.
     */
    fun validateProgressionMetadata(
        saga: SagaMetadata,
        rules: NarrativeRules,
    ): NarrativeProgressIntent? {
        val completedActsCount = saga.acts.count { it.narrativelyCompleteAct(rules) }
        val currentAct =
            saga.acts.find { it.data.id == saga.data.currentActId }
        val currentChapter =
            currentAct?.chapters?.find { it.data.id == currentAct.data.currentChapterId }
        val currentTimeline =
            currentChapter?.let { chapter ->
                chapter.data.currentEventId?.let { activeId ->
                    chapter.events.find { it.data.id == activeId }
                }
            }

        return when {
            saga.data.isEnded ||
                (completedActsCount == rules.actUpdateLimit && saga.data.endMessage.isNotEmpty()) -> {
                null
            }

            completedActsCount == rules.actUpdateLimit -> {
                NarrativeProgressIntent.GenerateEnding
            }

            currentAct == null || currentAct.narrativelyCompleteAct(rules) -> {
                NarrativeProgressIntent.CreateAct
            }

            currentAct.chapters.count { chapter -> chapter.narrativelyCompleteChapter(rules) } >=
                rules.actUpdateLimit -> {
                NarrativeProgressIntent.GenerateAct(currentAct.data.id)
            }

            currentAct.data.introduction.isBlank() -> {
                NarrativeProgressIntent.GenerateActIntro(currentAct.data.id)
            }

            currentChapter == null || currentChapter.narrativelyCompleteChapter(rules) -> {
                NarrativeProgressIntent.CreateChapter(currentAct.data.id)
            }

            currentChapter.events.count { it.narrativelyCompleteTimeline(rules) } >= rules.chapterUpdateLimit -> {
                NarrativeProgressIntent.GenerateChapter(currentChapter.data.id)
            }

            currentChapter.data.introduction.isBlank() -> {
                NarrativeProgressIntent.GenerateChapterIntro(currentChapter.data.id)
            }

            currentTimeline == null -> {
                NarrativeProgressIntent.CreateTimeline(currentChapter.data.id)
            }

            currentTimeline.narrativelyCompleteTimeline(rules) -> {
                NarrativeProgressIntent.CloseTimeline(currentChapter.data.id)
            }

            currentTimeline.data.shouldEnsureSceneSummary() -> {
                NarrativeProgressIntent.EnsureTimelineSceneSummary(currentTimeline.data.id)
            }

            currentTimeline.messages.size >= rules.loreUpdateLimit -> {
                NarrativeProgressIntent.EvolveTimeline(currentTimeline.data.id)
            }

            else -> {
                null
            }
        }
    }

    fun validateProgression(
        saga: SagaContent,
        rules: NarrativeRules,
    ): NarrativeAction? {
        val intent = validateProgressionMetadata(saga.toNarrativeMetadata(), rules) ?: return null
        return NarrativeActionMaterializer.materialize(intent, saga)
    }
}

/** Matches [TimelineContent.isComplete]: lore full plus non-empty summary fields (no [isBlank] variant). */
private fun TimelineMetadata.narrativelyCompleteTimeline(rules: NarrativeRules): Boolean =
    messages.size >= rules.loreUpdateLimit &&
        data.title.isNotEmpty() &&
        data.content.isNotEmpty()

private fun ChapterMetadata.narrativelyCompleteChapter(rules: NarrativeRules): Boolean =
    events.count { event -> event.narrativelyCompleteTimeline(rules) } >= rules.chapterUpdateLimit &&
        data.title.isNotEmpty() &&
        data.overview.isNotEmpty()

private fun ActMetadata.narrativelyCompleteAct(rules: NarrativeRules): Boolean =
    chapters.count { chapter -> chapter.narrativelyCompleteChapter(rules) } >= rules.actUpdateLimit &&
        data.title.isNotEmpty() &&
        data.content.isNotEmpty()
