package com.ilustris.sagai.features.widgets.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetRotationTest {
    @Test
    fun `each open advances one line per open and wraps`() {
        val indexes = (0..5).map { WidgetRotation.indexFor(RotationRule.EACH_OPEN, 3, it, 0L) }
        assertEquals(listOf(0, 1, 2, 0, 1, 2), indexes)
    }

    @Test
    fun `daily depends only on the date`() {
        val today = WidgetRotation.indexFor(RotationRule.DAILY, 4, openCounter = 99, epochDay = 20_000L)
        val sameDayLater = WidgetRotation.indexFor(RotationRule.DAILY, 4, openCounter = 1, epochDay = 20_000L)
        val nextDay = WidgetRotation.indexFor(RotationRule.DAILY, 4, openCounter = 1, epochDay = 20_001L)
        assertEquals(today, sameDayLater)
        assertEquals((today + 1) % 4, nextDay)
    }

    @Test
    fun `fixed always shows the first line`() {
        assertEquals(0, WidgetRotation.indexFor(RotationRule.FIXED, 5, openCounter = 7, epochDay = 123L, seed = 9))
    }

    @Test
    fun `a single line or none never goes out of range`() {
        assertEquals(0, WidgetRotation.indexFor(RotationRule.EACH_OPEN, 1, 5, 0L))
        assertEquals(0, WidgetRotation.indexFor(RotationRule.EACH_OPEN, 0, 5, 0L))
    }

    @Test
    fun `the seed offsets two widgets that share the same lines`() {
        val first = WidgetRotation.indexFor(RotationRule.EACH_OPEN, 5, 2, 0L, seed = 0)
        val second = WidgetRotation.indexFor(RotationRule.EACH_OPEN, 5, 2, 0L, seed = 1)
        assertTrue(first != second)
    }

    @Test
    fun `a huge seed or a negative epoch day stays in range`() {
        val a = WidgetRotation.indexFor(RotationRule.EACH_OPEN, 7, 3, 0L, seed = Int.MAX_VALUE)
        val b = WidgetRotation.indexFor(RotationRule.DAILY, 7, 0, -5L, seed = 3)
        assertTrue(a in 0..6)
        assertTrue(b in 0..6)
    }
}
