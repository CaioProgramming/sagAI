package com.ilustris.sagai.features.progression

import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterProfile
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.progression.domain.ProgressionConfig
import com.ilustris.sagai.features.progression.domain.withPlacement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CharacterPlacementTest {
    private val config = ProgressionConfig(enabled = true, levelCap = 100, playerStartLevelCap = 25, maxSkills = 3)

    private fun character(
        level: Int?,
        skills: List<String> = emptyList(),
    ) = Character(details = Details(), profile = CharacterProfile(), baseLevel = level, levelRationale = " why ", skills = skills)

    @Test
    fun `off clears every placement field`() {
        val result = character(40, listOf("Sword")).withPlacement(null, isProtagonist = false)
        assertNull(result.baseLevel)
        assertNull(result.levelRationale)
        assertEquals(emptyList<String>(), result.skills)
    }

    @Test
    fun `protagonist starts under the start cap and others under the level cap`() {
        assertEquals(25, character(80).withPlacement(config, isProtagonist = true).baseLevel)
        assertEquals(80, character(80).withPlacement(config, isProtagonist = false).baseLevel)
        assertEquals(100, character(900).withPlacement(config, isProtagonist = false).baseLevel)
        assertEquals(1, character(-4).withPlacement(config, isProtagonist = false).baseLevel)
    }

    @Test
    fun `missing level stays missing and rationale is trimmed`() {
        val result = character(null).withPlacement(config, isProtagonist = false)
        assertNull(result.baseLevel)
        assertEquals("why", result.levelRationale)
    }

    @Test
    fun `skills are tidied deduped and capped`() {
        val result = character(10, listOf(" Sword ", "sword", "", "Stealth", "Alchemy", "Archery")).withPlacement(config, isProtagonist = false)
        assertEquals(listOf("Sword", "Stealth", "Alchemy"), result.skills)
    }
}
