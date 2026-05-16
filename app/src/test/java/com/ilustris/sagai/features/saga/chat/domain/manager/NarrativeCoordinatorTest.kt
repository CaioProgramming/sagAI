package com.ilustris.sagai.features.saga.chat.domain.manager

import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.ActContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NarrativeCoordinatorTest {
    private val coordinator = NarrativeCoordinator()

    @Test
    fun `reevaluate exposes user action`() {
        val state =
            coordinator.reevaluate(
                nextResolvedAction = NarrativeAction.CreateAct,
                context = NarrativeEvaluationContext(),
            )

        assertTrue(state.pendingAction is NarrativeAction.CreateAct)
        assertTrue(state.phase is NarrativePhase.AwaitingAdvance)
    }

    @Test
    fun `reevaluate skips when onboarding visible`() {
        coordinator.reevaluate(NarrativeAction.CreateAct, NarrativeEvaluationContext())
        val initial = coordinator.uiState.value

        coordinator.reevaluate(
            NarrativeAction.CreateAct,
            NarrativeEvaluationContext(isOnboardingVisible = true),
        )

        assertEquals(initial, coordinator.uiState.value)
    }

    @Test
    fun `failure restores awaiting advance for user actions`() {
        val act = ActContent(data = Act(id = 1, sagaId = 1))
        val action = NarrativeAction.GenerateActIntro(act)

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
