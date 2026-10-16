package com.ilustris.sagai.features.progression

import com.ilustris.sagai.features.progression.domain.LevelBand
import com.ilustris.sagai.features.progression.domain.PowerContext
import com.ilustris.sagai.features.progression.domain.PowerEntry
import com.ilustris.sagai.features.progression.domain.PowerGapBand
import com.ilustris.sagai.features.progression.domain.ProgressionConfig
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PowerContextTest {
    private val context =
        PowerContext(
            ProgressionConfig(
                levelBands = listOf(LevelBand(1, 30, "trained"), LevelBand(31, 100, "elite")),
                powerGapBands = listOf(PowerGapBand(4, "Even footing."), PowerGapBand(14, "Clear advantage."), PowerGapBand(100, "Overwhelming.")),
            ),
        )

    @Test
    fun `nothing to compare gives nothing`() {
        assertNull(context.describe(emptyList()))
        assertNull(context.describe(listOf(PowerEntry("Maya", 10))))
    }

    @Test
    fun `the stronger one leads and the gap picks the sentence`() {
        val text = context.describe(listOf(PowerEntry("Maya", 12), PowerEntry("Orc", 52)))!!
        assertTrue(text.startsWith("Orc (level 52, elite) vs Maya (level 12, trained): gap 40."))
        assertTrue(text.endsWith("Overwhelming."))
    }

    @Test
    fun `small gap reads as even footing and pairs are capped`() {
        assertTrue(context.describe(listOf(PowerEntry("A", 10), PowerEntry("B", 12)))!!.endsWith("Even footing."))
        val many = context.describe((1..6).map { PowerEntry("C$it", it * 10) })!!
        assertTrue(many.lines().size == 6)
    }
}
