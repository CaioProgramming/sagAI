package com.ilustris.sagai.features.progression

import com.ilustris.sagai.features.progression.domain.FeatAssembler
import com.ilustris.sagai.features.progression.domain.FeatDraft
import com.ilustris.sagai.features.progression.domain.LevelCurve
import com.ilustris.sagai.features.progression.domain.ProgressionConfig
import com.ilustris.sagai.features.progression.domain.ProgressionMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FeatAssemblerTest {
    private val math =
        ProgressionMath(
            ProgressionConfig(
                levelXp = LevelCurve(4.3, 1.6),
                tierXp = listOf(0, 5, 15, 40, 90, 180),
                repeatDecay = listOf(1.0, 0.6, 0.3),
                eventXpCapPct = 0.3,
                chapterXpCapPct = 0.8,
            ),
        )
    private val assembler = FeatAssembler(math)

    private fun draft(
        kind: String = "BOND",
        tier: Int = 2,
        title: String = "t",
        changed: String? = "c",
        cost: String? = "x",
        evidence: Int? = null,
    ) = FeatDraft(kind, title, changed, cost, tier, evidence)

    private fun run(
        drafts: List<FeatDraft>,
        level: Int = 60,
        prior: Map<String, Int> = emptyMap(),
        chapterXp: Int = 0,
        evidence: Set<Int> = emptySet(),
    ) = assembler.assemble(drafts, level, prior, chapterXp, evidence)

    @Test
    fun `unknown kind and blank title are dropped`() {
        val feats = run(listOf(draft(kind = "DANCING"), draft(title = " "), draft(kind = "bond")))
        assertEquals(1, feats.size)
        assertEquals("BOND", feats.single().kind)
    }

    @Test
    fun `unsupported tier is clamped before it pays`() {
        val feat = run(listOf(draft(tier = 4, cost = null))).single()
        assertEquals(2, feat.tier)
        assertEquals(15, feat.xp)
    }

    @Test
    fun `same kind decays within the event and across the chapter`() {
        val uncapped =
            FeatAssembler(
                ProgressionMath(
                    ProgressionConfig(
                        tierXp = listOf(0, 5, 15, 40, 90, 180),
                        repeatDecay = listOf(1.0, 0.6, 0.3),
                        eventXpCapPct = 100.0,
                        chapterXpCapPct = 100.0,
                    ),
                ),
            )
        val inEvent = uncapped.assemble(listOf(draft(tier = 3), draft(tier = 3)), 60, emptyMap(), 0, emptySet())
        assertEquals(listOf(40, 24), inEvent.map { it.xp })
        val acrossChapter = uncapped.assemble(listOf(draft(tier = 3)), 60, mapOf("BOND" to 2), 0, emptySet())
        assertEquals(12, acrossChapter.single().xp)
    }

    @Test
    fun `event cap scales a runaway event down in proportion`() {
        val level = 1
        val budget = math.eventBudget(level)
        val feats = run(List(5) { draft(kind = ProgressionKinds[it], tier = 5) }, level = level)
        assertTrue(feats.sumOf { it.xp } <= budget)
        assertTrue(feats.all { it.xp == feats.first().xp })
    }

    @Test
    fun `chapter cap leaves only the remaining room`() {
        val level = 20
        val room = 10
        val chapterXp = math.chapterBudget(level) - room
        val feats = run(listOf(draft(tier = 5)), level = level, chapterXp = chapterXp)
        assertTrue(feats.single().xp <= room)
        assertEquals(0, run(listOf(draft(tier = 5)), level = level, chapterXp = 10_000).single().xp)
    }

    @Test
    fun `evidence is kept only for ids the model was shown`() {
        val feats = run(listOf(draft(evidence = 7), draft(kind = "SACRIFICE", evidence = 99)), evidence = setOf(7))
        assertEquals(7, feats[0].relationshipEvidence)
        assertNull(feats[1].relationshipEvidence)
    }

    private companion object {
        val ProgressionKinds = FeatAssembler.KINDS.toList()
    }
}
