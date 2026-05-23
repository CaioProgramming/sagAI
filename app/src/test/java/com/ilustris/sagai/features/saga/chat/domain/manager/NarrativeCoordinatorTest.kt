package com.ilustris.sagai.features.saga.chat.domain.manager

import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NarrativeCoordinatorTest {
    private val coordinator = NarrativeCoordinator()

    @Test
    fun `reevaluate exposes user action for evolve timeline`() {
        val timeline =
            com.ilustris.sagai.features.timeline.data.model.TimelineContent(
                data =
                    com.ilustris.sagai.features.timeline.data.model.Timeline(
                        id = 1,
                        chapterId = 1,
                    ),
                messages = emptyList(),
            )
        val state =
            coordinator.reevaluate(
                nextResolvedAction = NarrativeAction.EvolveTimeline(timeline),
                context = NarrativeEvaluationContext(),
            )

        assertTrue(state.pendingAction is NarrativeAction.EvolveTimeline)
        assertTrue(state.phase is NarrativePhase.AwaitingAdvance)
    }

    @Test
    fun `reevaluate does not await advance for structural setup actions`() {
        val state =
            coordinator.reevaluate(
                nextResolvedAction = NarrativeAction.CreateAct,
                context = NarrativeEvaluationContext(),
            )

        assertNull(state.pendingAction)
        assertTrue(state.phase is NarrativePhase.Playing)
        assertFalse(state.showAdvanceTrigger)
    }

    @Test
    fun `failure restores awaiting advance for user actions`() {
        val chapter = com.ilustris.sagai.features.chapter.data.model.ChapterContent(
            data = com.ilustris.sagai.features.chapter.data.model.Chapter(id = 1, actId = 1),
        )
        val action = NarrativeAction.GenerateChapter(chapter)

        coordinator.onUserAdvanceRequested(action)
        coordinator.onActionCompleted(
            action,
            NarrativeExecutionResult.Failure("network"),
        )

        val state = coordinator.uiState.value
        assertEquals(action, state.pendingAction)
        assertTrue(state.lastError != null)
    }

    @Test
    fun `milestone blocks pending action`() {
        coordinator.markMilestoneActive()

        val state =
            coordinator.reevaluate(
                nextResolvedAction = NarrativeAction.CreateAct,
                context =
                    NarrativeEvaluationContext(
                        isMilestoneActive = true,
                        hasActiveMilestoneOverlay = true,
                    ),
            )

        assertNull(state.pendingAction)
        assertTrue(state.phase is NarrativePhase.MilestoneBlocking)
    }
}
