package com.ilustris.sagai.features.onboarding

import com.ilustris.sagai.features.onboarding.domain.SetupStep
import com.ilustris.sagai.features.onboarding.domain.decideSetupSteps
import org.junit.Assert.assertEquals
import org.junit.Test

class SetupStepsTest {
    @Test
    fun `fresh install goes through intro, name and key in order`() {
        assertEquals(
            listOf(SetupStep.INTRO, SetupStep.NAME, SetupStep.KEY),
            decideSetupSteps(keyMissing = true, introPending = true, namePending = true),
        )
    }

    @Test
    fun `existing user missing only the key sees only the key`() {
        assertEquals(
            listOf(SetupStep.KEY),
            decideSetupSteps(keyMissing = true, introPending = false, namePending = false),
        )
    }

    @Test
    fun `answered steps are skipped independently`() {
        assertEquals(
            listOf(SetupStep.NAME, SetupStep.KEY),
            decideSetupSteps(keyMissing = true, introPending = false, namePending = true),
        )
        assertEquals(
            listOf(SetupStep.INTRO, SetupStep.KEY),
            decideSetupSteps(keyMissing = true, introPending = true, namePending = false),
        )
    }

    @Test
    fun `a user who already has a key sees nothing`() {
        assertEquals(
            emptyList<SetupStep>(),
            decideSetupSteps(keyMissing = false, introPending = true, namePending = true),
        )
    }

    @Test
    fun `key is always last`() {
        val steps = decideSetupSteps(keyMissing = true, introPending = true, namePending = true)
        assertEquals(SetupStep.KEY, steps.last())
    }
}
