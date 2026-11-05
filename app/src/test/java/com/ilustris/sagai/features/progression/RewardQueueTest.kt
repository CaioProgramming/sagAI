package com.ilustris.sagai.features.progression

import com.ilustris.sagai.features.progression.data.model.CharacterHonor
import com.ilustris.sagai.features.progression.data.model.HonorKind
import com.ilustris.sagai.features.progression.domain.LevelUpInput
import com.ilustris.sagai.features.progression.domain.NewRelationInput
import com.ilustris.sagai.features.progression.domain.RewardQueueBuilder
import com.ilustris.sagai.features.progression.domain.RewardStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RewardQueueTest {
    private fun medal(id: Int, character: Int, scope: Int) = CharacterHonor(id = id, characterId = character, sagaId = 1, kind = HonorKind.MEDAL, scopeId = scope, name = "m$id", emoji = "x")

    private fun legacy(id: Int, character: Int, scope: Int) = CharacterHonor(id = id, characterId = character, sagaId = 1, kind = HonorKind.TITLE, scopeId = scope, name = "t$id")

    private fun levelUp(character: Int, from: Int, to: Int) = LevelUpInput(character, from, to, "feat$character", 50)

    private fun bond(id: Int, one: Int, two: Int) = NewRelationInput(id, one, two, "bond$id", "x", "")

    @Test
    fun `nothing earned gives an empty flow`() {
        assertTrue(RewardQueueBuilder.build(emptyList(), emptyList()).isEmpty())
    }

    @Test
    fun `one character reads level then medals by chapter then legacy`() {
        val steps = RewardQueueBuilder.build(listOf(levelUp(1, 2, 3)), listOf(legacy(3, 1, 9), medal(2, 1, 5), medal(1, 1, 4)))
        assertEquals(
            listOf("LevelUp", "Medal:m1", "Medal:m2", "Legacy:t3"),
            steps.map { it.label() },
        )
        assertFalse(steps.any { it is RewardStep.Summary })
    }

    @Test
    fun `the highest level leads and each character stays together`() {
        val steps = RewardQueueBuilder.build(listOf(levelUp(1, 1, 2), levelUp(2, 4, 5)), listOf(medal(1, 1, 3)))
        assertEquals(listOf(2, 1, 1), steps.filterNot { it is RewardStep.Summary }.map { it.characterId() })
    }

    @Test
    fun `characters with only honors follow the ones who leveled`() {
        val steps = RewardQueueBuilder.build(listOf(levelUp(2, 1, 2)), listOf(medal(1, 7, 1)))
        assertEquals(listOf(2, 7), steps.filterNot { it is RewardStep.Summary }.map { it.characterId() })
    }

    @Test
    fun `summary closes the flow only with more than one character`() {
        val steps = RewardQueueBuilder.build(listOf(levelUp(1, 1, 2), levelUp(2, 4, 5)), listOf(medal(1, 1, 3), legacy(2, 2, 1)))
        val summary = steps.last() as RewardStep.Summary
        assertEquals(listOf(2, 1), summary.rows.map { it.characterId })
        assertEquals(1, summary.rows.first { it.characterId == 1 }.medals)
        assertEquals(1, summary.rows.first { it.characterId == 2 }.legacies)
    }

    @Test
    fun `only the first level up carries the explainer`() {
        val steps = RewardQueueBuilder.build(listOf(levelUp(1, 1, 2), levelUp(2, 4, 5)), emptyList(), showExplainer = true)
        val flags = steps.filterIsInstance<RewardStep.LevelUp>().map { it.explainer }
        assertEquals(listOf(true, false), flags)
    }

    @Test
    fun `a bond hangs on the first of its characters in the story`() {
        val steps = RewardQueueBuilder.build(listOf(levelUp(1, 1, 2), levelUp(2, 4, 5)), emptyList(), relations = listOf(bond(1, 1, 2)))
        // Character 2 leads (level 5), so the bond comes right after their level up.
        assertEquals(listOf("LevelUp", "Bond:bond1", "LevelUp"), steps.filterNot { it is RewardStep.Summary }.map { it.label() })
    }

    @Test
    fun `a bond between newcomers is a flow on its own`() {
        val steps = RewardQueueBuilder.build(emptyList(), emptyList(), relations = listOf(bond(1, 5, 6)))
        assertEquals(listOf("Bond:bond1"), steps.map { it.label() })
    }

    @Test
    fun `memorial is marked for characters who are gone`() {
        val steps = RewardQueueBuilder.build(emptyList(), listOf(legacy(1, 3, 1)), memorialIds = setOf(3))
        assertTrue((steps.single() as RewardStep.Legacy).memorial)
    }

    private fun RewardStep.characterId(): Int =
        when (this) {
            is RewardStep.LevelUp -> characterId
            is RewardStep.Medal -> characterId
            is RewardStep.Legacy -> characterId
            is RewardStep.Relationship -> characterOneId
            is RewardStep.Summary -> -1
        }

    private fun RewardStep.label(): String =
        when (this) {
            is RewardStep.LevelUp -> "LevelUp"
            is RewardStep.Medal -> "Medal:$name"
            is RewardStep.Legacy -> "Legacy:$name"
            is RewardStep.Relationship -> "Bond:$title"
            is RewardStep.Summary -> "Summary"
        }
}
