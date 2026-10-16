package com.ilustris.sagai.features.progression

import com.ilustris.sagai.features.progression.data.model.CharacterFeat
import com.ilustris.sagai.features.progression.domain.HonorCandidate
import com.ilustris.sagai.features.progression.domain.HonorValidator
import com.ilustris.sagai.features.progression.domain.ProgressionConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HonorValidatorTest {
    private val validator =
        HonorValidator(
            ProgressionConfig(
                medalMinTier = 3,
                legacyMinTier = 4,
                maxMedalsPerCharacterPerChapter = 2,
                maxMedalsPerChapter = 3,
                maxTitlesPerAct = 2,
            ),
        )

    private fun feat(
        id: Int,
        character: Int = 1,
        tier: Int = 3,
        cost: String? = "lost something",
    ) = CharacterFeat(
        id = id, characterId = character, sagaId = 1, timelineId = 1, kind = "BOND", title = "t",
        changed = "c", cost = cost, tier = tier, xp = 10,
    )

    private fun candidate(
        vararg refs: Int,
        character: Int = 1,
        name: String = "Deed",
        description: String = "what happened",
    ) = HonorCandidate(character, name, "⚔", description, refs.toList())

    @Test
    fun `invented ids and other characters feats are discarded`() {
        val feats = listOf(feat(1), feat(2, character = 2))
        val medals = validator.validMedals(listOf(candidate(1, 2, 99)), feats)
        assertEquals(listOf(1), medals.single().featIds)
    }

    @Test
    fun `a medal with no real feat is rejected`() {
        assertTrue(validator.validMedals(listOf(candidate(99)), listOf(feat(1))).isEmpty())
    }

    @Test
    fun `a medal needs a feat over the floor with a cost`() {
        assertTrue(validator.validMedals(listOf(candidate(1)), listOf(feat(1, tier = 2))).isEmpty())
        assertTrue(validator.validMedals(listOf(candidate(1)), listOf(feat(1, cost = null))).isEmpty())
        assertEquals(1, validator.validMedals(listOf(candidate(1)), listOf(feat(1))).size)
    }

    @Test
    fun `a feat backs only one medal`() {
        val medals = validator.validMedals(listOf(candidate(1), candidate(1)), listOf(feat(1)))
        assertEquals(1, medals.size)
    }

    @Test
    fun `caps hold per character and per chapter`() {
        val feats = (1..6).map { feat(it, character = if (it <= 3) 1 else 2 + (it % 2)) }
        val medals = validator.validMedals(feats.map { candidate(it.id, character = it.characterId) }, feats)
        assertTrue(medals.size <= 3)
        assertTrue(medals.groupBy { it.characterId }.values.all { it.size <= 2 })
    }

    @Test
    fun `blank text is rejected and long text is trimmed`() {
        assertTrue(validator.validMedals(listOf(candidate(1, name = " ")), listOf(feat(1))).isEmpty())
        val long = validator.validMedals(listOf(candidate(1, description = "x".repeat(900))), listOf(feat(1))).single()
        assertEquals(300, long.description.length)
    }

    @Test
    fun `legacy is one per character, over tier 4, capped per act`() {
        val feats = listOf(feat(1, tier = 4), feat(2, tier = 4), feat(3, character = 2, tier = 5), feat(4, character = 3, tier = 5))
        val legacies =
            validator.validLegacies(
                listOf(candidate(1), candidate(2), candidate(3, character = 2), candidate(4, character = 3)),
                feats,
            )
        assertEquals(listOf(1, 2), legacies.map { it.characterId })
        assertTrue(validator.validLegacies(listOf(candidate(1)), listOf(feat(1, tier = 3))).isEmpty())
    }

    @Test
    fun `material check says whether the call is worth making`() {
        assertFalse(validator.hasMedalMaterial(listOf(feat(1, tier = 2), feat(2, cost = null))))
        assertTrue(validator.hasMedalMaterial(listOf(feat(1))))
        assertFalse(validator.hasLegacyMaterial(listOf(feat(1, tier = 3))))
        assertTrue(validator.hasLegacyMaterial(listOf(feat(1, tier = 4))))
    }
}
