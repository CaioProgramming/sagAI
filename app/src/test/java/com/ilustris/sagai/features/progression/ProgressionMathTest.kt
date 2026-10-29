package com.ilustris.sagai.features.progression

import com.ilustris.sagai.features.progression.domain.LevelCurve
import com.ilustris.sagai.features.progression.domain.ProgressionConfig
import com.ilustris.sagai.features.progression.domain.ProgressionMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressionMathTest {
    private val config =
        ProgressionConfig(
            levelCap = 100,
            levelXp = LevelCurve(base = 4.3, exponent = 1.6),
            tierXp = listOf(0, 5, 15, 40, 90, 180),
            repeatDecay = listOf(1.0, 0.6, 0.3),
            eventXpCapPct = 0.3,
            chapterXpCapPct = 0.8,
        )
    private val math = ProgressionMath(config)

    @Test
    fun `tier 3 without a cost drops to 2`() {
        assertEquals(2, math.clampTier(3, "changed", null))
        assertEquals(2, math.clampTier(3, "changed", "  "))
    }

    @Test
    fun `tier 4 without a changed drops through to 1`() {
        assertEquals(1, math.clampTier(4, null, "cost"))
    }

    @Test
    fun `tier 5 with changed and cost is kept`() {
        assertEquals(5, math.clampTier(5, "the act turns", "everything"))
    }

    @Test
    fun `a clamp never raises a tier and tier 0 needs nothing`() {
        assertEquals(0, math.clampTier(0, null, null))
        assertEquals(1, math.clampTier(1, null, null))
        assertEquals(5, math.clampTier(9, "x", "y"))
        assertEquals(0, math.clampTier(-3, "x", "y"))
    }

    @Test
    fun `repeated kind decays and holds at the last step`() {
        assertEquals(40, math.featXp(3, 0))
        assertEquals(24, math.featXp(3, 1))
        assertEquals(12, math.featXp(3, 2))
        assertEquals(12, math.featXp(3, 7))
    }

    @Test
    fun `unknown tier table gives no xp`() {
        assertEquals(0, ProgressionMath(ProgressionConfig()).featXp(3, 0))
    }

    @Test
    fun `curve is monotonic and level 1 costs nothing`() {
        assertEquals(0, math.xpToReach(1))
        (2..100).forEach { assertTrue(math.xpToReach(it) > math.xpToReach(it - 1)) }
    }

    @Test
    fun `level derives from base and earned xp`() {
        assertEquals(1, math.levelFor(1, 0))
        assertEquals(40, math.levelFor(40, 0))
        assertEquals(2, math.levelFor(1, math.xpToReach(2)))
        assertEquals(1, math.levelFor(1, math.xpToReach(2) - 1))
    }

    @Test
    fun `a veteran climbs slower than a novice with the same xp`() {
        val xp = 200
        val novice = math.levelFor(1, xp) - 1
        val veteran = math.levelFor(60, xp) - 60
        assertTrue(novice > veteran)
    }

    @Test
    fun `level never passes the cap and negative xp is ignored`() {
        assertEquals(100, math.levelFor(99, 1_000_000))
        assertEquals(100, math.levelFor(250, 0))
        assertEquals(5, math.levelFor(5, -50))
        assertEquals(0, math.levelSpan(100))
    }

    @Test
    fun `budget is a share of the level span`() {
        val span = math.levelSpan(10)
        assertEquals((span * 0.3).toInt(), math.eventBudget(10))
        assertEquals((span * 0.8).toInt(), math.chapterBudget(10))
    }

    @Test
    fun `awards over budget scale down in proportion and under budget stay`() {
        assertEquals(listOf(10, 20), math.fitToBudget(listOf(10, 20), 100))
        assertEquals(listOf(10, 20), math.fitToBudget(listOf(20, 40), 30))
        assertEquals(listOf(0, 0), math.fitToBudget(listOf(20, 40), 0))
    }
}
