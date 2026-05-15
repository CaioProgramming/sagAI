package com.ilustris.sagai.features.saga.chat.domain.manager

import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.features.home.data.model.SagaContent

object NarrativeCheck {
    fun validateProgression(
        saga: SagaContent,
        rules: NarrativeRules,
    ): NarrativeAction? {
        val currentAct = saga.currentActInfo
        val currentChapter = currentAct?.currentChapterInfo
        val currentTimeline = currentChapter?.currentEventInfo

        return when {
            saga.data.isEnded || saga.isComplete(rules) -> {
                null
            }

            saga.isFull(rules) -> {
                NarrativeAction.GenerateEnding(saga)
            }

            currentAct == null || currentAct.isComplete(rules) -> {
                NarrativeAction.CreateAct
            }

            currentAct.isFull(
                rules.actUpdateLimit,
                rules,
            ) -> {
                NarrativeAction.GenerateAct(currentAct)
            }

            currentAct.data.introduction.isBlank() -> {
                NarrativeAction.GenerateActIntro(currentAct)
            }

            currentChapter == null || currentChapter.isComplete(rules) -> {
                NarrativeAction.CreateChapter(currentAct)
            }

            currentChapter.isFull(rules.chapterUpdateLimit, rules) -> {
                NarrativeAction.GenerateChapter(currentChapter)
            }

            currentChapter.data.introduction.isBlank() -> {
                NarrativeAction.GenerateChapterIntro(currentChapter)
            }

            currentTimeline == null -> {
                NarrativeAction.CreateTimeline(currentChapter)
            }

            currentTimeline.isComplete(rules) -> {
                NarrativeAction.CloseTimeline(currentChapter)
            }

            currentTimeline.isFull(rules.loreUpdateLimit) -> {
                NarrativeAction.EvolveTimeline(
                    currentTimeline,
                )
            }

            else -> {
                null
            }
        }
    }
}
