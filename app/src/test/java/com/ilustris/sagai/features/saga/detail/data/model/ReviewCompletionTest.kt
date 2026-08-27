package com.ilustris.sagai.features.saga.detail.data.model

import com.ilustris.sagai.features.saga.detail.review.domain.model.ReviewSteps
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewCompletionTest {
    private val completeStagesExceptFarewells =
        Review(
            introduction = ReviewStage(),
            expressiveness = ReviewStage(),
            playstyle = ReviewStage(),
            topCharacters = ReviewStage(),
            actsInsight = ReviewStage(),
            conclusion = ReviewStage(),
            farewells = null,
        )

    @Test
    fun `review is not complete without farewells`() {
        assertFalse(completeStagesExceptFarewells.isComplete())
    }

    @Test
    fun `review is complete once farewells is present`() {
        val complete = completeStagesExceptFarewells.copy(farewells = listOf(Farewell(1, "Goodbye.")))
        assertTrue(complete.isComplete())
    }

    @Test
    fun `completedStepCount includes farewells`() {
        assertEquals(6, completeStagesExceptFarewells.completedStepCount())
        val withFarewells = completeStagesExceptFarewells.copy(farewells = listOf(Farewell(1, "Goodbye.")))
        assertEquals(7, withFarewells.completedStepCount())
    }

    @Test
    fun `ReviewSteps has exactly 7 entries matching the required stage count`() {
        assertEquals(7, ReviewSteps.entries.size)
    }
}
