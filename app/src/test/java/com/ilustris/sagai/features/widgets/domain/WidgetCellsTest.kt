package com.ilustris.sagai.features.widgets.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetCellsTest {
    @Test
    fun `the minimum dp of a cell count matches what the launcher guarantees`() {
        assertEquals(40, WidgetCells.minDp(1))
        assertEquals(110, WidgetCells.minDp(2))
        assertEquals(180, WidgetCells.minDp(3))
        assertEquals(250, WidgetCells.minDp(4))
    }

    @Test
    fun `every declared size maps back to its own cells`() {
        WidgetSizes.CONTINUE.forEach { cells ->
            val back = WidgetCells.fromDp(WidgetCells.minDp(cells.columns).toFloat(), WidgetCells.minDp(cells.rows).toFloat())
            assertEquals(cells, back)
        }
    }

    @Test
    fun `a launcher reporting a hair under the nominal size still gets the right cells`() {
        assertEquals(WidgetCells(4, 2), WidgetCells.fromDp(249.6f, 109.8f))
    }

    @Test
    fun `a size is never reported as zero cells`() {
        assertEquals(WidgetCells(1, 1), WidgetCells.fromDp(0f, 0f))
    }

    @Test
    fun `best fit picks the largest design that fits`() {
        assertEquals(WidgetCells(4, 2), WidgetCells(4, 2).bestFit(WidgetSizes.QUOTE))
        assertEquals(WidgetCells(4, 3), WidgetCells(4, 3).bestFit(WidgetSizes.QUOTE))
        assertEquals(WidgetCells(4, 3), WidgetCells(4, 5).bestFit(WidgetSizes.QUOTE))
    }

    @Test
    fun `three columns fall back to the two column design`() {
        assertEquals(WidgetCells(2, 2), WidgetCells(3, 2).bestFit(WidgetSizes.QUOTE))
    }

    @Test
    fun `below the smallest design the first of the smallest ones is used`() {
        // Quote has a 4x1 and a 2x2, both of area 4: the first listed wins.
        assertEquals(WidgetCells(4, 1), WidgetCells(2, 1).bestFit(WidgetSizes.QUOTE))
    }

    @Test
    fun `the stories shelf has no single row design`() {
        assertEquals(false, WidgetSizes.STORIES.any { it.rows == 1 })
    }
}
