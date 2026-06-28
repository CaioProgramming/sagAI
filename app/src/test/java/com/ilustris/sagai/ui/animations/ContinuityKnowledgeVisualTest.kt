package com.ilustris.sagai.ui.animations

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContinuityKnowledgeVisualTest {
    @Test
    fun `more facts produce more satellites`() {
        val low = mapFactCountToSatellites(2, 12)
        val high = mapFactCountToSatellites(25, 12)
        assertTrue(high > low)
    }

    @Test
    fun `satellite layout is stable for chapter id`() {
        val first = generateKnowledgeSatellites(chapterId = 42, count = 5)
        val second = generateKnowledgeSatellites(chapterId = 42, count = 5)
        assertEquals(first, second)
    }

    @Test
    fun `chapter anchors stay inside normalized bounds`() {
        repeat(12) { index ->
            val (x, y) = chapterAnchorPosition(index, 12)
            assertTrue(x in 0.1f..0.9f)
            assertTrue(y in 0.14f..0.86f)
        }
    }
}
