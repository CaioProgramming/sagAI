package com.ilustris.sagai.features.saga.chat.domain.manager

import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.features.home.data.model.ActMetadata
import com.ilustris.sagai.features.home.data.model.ChapterMetadata
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.home.data.model.TimelineMetadata
import com.ilustris.sagai.features.home.data.model.toNarrativeMetadata
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

            // The closing chapter's cards come before its synthesis, so the player's answers
            // can shape how it's written — and the chain can't move on until they're answered.
            currentChapter.events.count { it.narrativelyCompleteTimeline(rules) } >= rules.chapterUpdateLimit &&
                currentChapter.awaitsPlayerChoices() -> {
                NarrativeProgressIntent.GenerateChoiceCards(currentChapter.data.id)
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

    /**
     * Best-effort projection of how many *reveal-worthy* closures (event -> chapter -> act,
     * in that strict cascade order) will fire in the current milestone chain run, purely from
     * counts already in [saga] against [rules] — no execution. Used only to size the milestone
     * stepper UI; the real chain still walks one step at a time via [validateProgressionMetadata]
     * regardless of what this returns, so an off-by-one here is a cosmetic risk, not a
     * correctness one. Excludes silent scaffolding (CreateTimeline/CloseTimeline) and cinematic
     * introductions (CreateChapter/GenerateChapterIntro/CreateAct/GenerateActIntro) — those never
     * count toward the stepper.
     */
    fun computeClosureChainLength(
        saga: SagaMetadata,
        rules: NarrativeRules,
    ): Int {
        val currentAct = saga.acts.find { it.data.id == saga.data.currentActId } ?: return 0
        val currentChapter =
            currentAct.chapters.find { it.data.id == currentAct.data.currentChapterId } ?: return 0
        val currentTimeline =
            currentChapter.data.currentEventId?.let { activeId ->
                currentChapter.events.find { it.data.id == activeId }
            } ?: return 0

        val eventAlreadyComplete = currentTimeline.narrativelyCompleteTimeline(rules)
        val eventClosing = eventAlreadyComplete || currentTimeline.messages.size >= rules.loreUpdateLimit
        if (!eventClosing) return 0

        var steps = 1 // EvolveTimeline -> NewEvent reveal

        val completeEventsAfter =
            currentChapter.events.count { it.narrativelyCompleteTimeline(rules) } +
                if (eventAlreadyComplete) 0 else 1
        if (completeEventsAfter < rules.chapterUpdateLimit) return steps
        steps++ // GenerateChapter -> ChapterFinished reveal

        val chapterAlreadyComplete = currentChapter.narrativelyCompleteChapter(rules)
        val completeChaptersAfter =
            currentAct.chapters.count { it.narrativelyCompleteChapter(rules) } +
                if (chapterAlreadyComplete) 0 else 1
        if (completeChaptersAfter < rules.actUpdateLimit) return steps
        steps++ // GenerateAct -> ActFinished reveal

        return steps
    }
}

/** Matches [TimelineContent.isComplete]: lore full plus non-empty summary fields (no [isBlank]
 * variant). Not private — also the definition of "valid" a timeline needs to satisfy in
 * [SagaContentManagerImpl.pruneOrphanTimelines][com.ilustris.sagai.features.saga.chat.data.manager.SagaContentManagerImpl]
 * (valid = current pointer target OR narrativelyCompleteTimeline; anything else is an orphan),
 * so both stay in sync with a single source of truth instead of drifting apart. */
// "Complete" means already synthesized — title and content generated. The rules' limits decide *when*
// that synthesis fires (see validateProgressionMetadata), but must not re-judge work already done:
// they live in Remote Config and can change mid-saga, and raising LORE_UPDATE_LIMIT from 15 to 16 once
// turned every closed 15-message event back into an "incomplete" one — adding a sixth event to a
// chapter, a fourth chapter to an act, and regenerating old events' lore on every load.
@Suppress("UNUSED_PARAMETER")
fun TimelineMetadata.narrativelyCompleteTimeline(rules: NarrativeRules): Boolean =
    data.title.isNotEmpty() && data.content.isNotEmpty()

/**
 * Cards not dealt yet, or dealt and still unanswered. An empty hand means dealing was tried and
 * nothing usable came back — the chapter closes without cards rather than asking forever.
 */
private fun ChapterMetadata.awaitsPlayerChoices(): Boolean =
    data.playerChoiceAnswers == null && data.playerChoiceCards?.isEmpty() != true

@Suppress("UNUSED_PARAMETER")
private fun ChapterMetadata.narrativelyCompleteChapter(rules: NarrativeRules): Boolean =
    data.title.isNotEmpty() && data.content.isNotEmpty()

@Suppress("UNUSED_PARAMETER")
private fun ActMetadata.narrativelyCompleteAct(rules: NarrativeRules): Boolean =
    data.title.isNotEmpty() && data.content.isNotEmpty()
