package com.ilustris.sagai.features.characters

import com.ilustris.sagai.core.ai.prompts.CharacterPrompts
import com.ilustris.sagai.features.characters.data.model.BodyFeatures
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterDesignReview
import com.ilustris.sagai.features.characters.data.model.CharacterProfile
import com.ilustris.sagai.features.characters.data.model.Clothing
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.data.model.FacialFeatures
import com.ilustris.sagai.features.characters.data.model.PhysicalTraits
import com.ilustris.sagai.features.characters.data.model.designChangesTo
import com.ilustris.sagai.features.characters.data.model.withDesignReview
import org.junit.Assert.assertEquals
import org.junit.Test

class CharacterDesignReviewTest {
    private fun sheet(
        name: String = "Tereza",
        hair: String = "coque",
        marks: String = "expressão de impaciência",
        outfit: String = "casaco ameixa",
        personality: String = "fria e polida",
    ) = Character(
        name = name,
        lastName = "Gimmani",
        artwork = "uma executiva",
        details =
            Details(
                physicalTraits =
                    PhysicalTraits(
                        age = 42,
                        facialDetails = FacialFeatures(hair = hair, eyes = "verdes", mouth = "ameixa", distinctiveMarks = marks, jawline = "reta"),
                        bodyFeatures = BodyFeatures(buildAndPosture = "ereta", skinAppearance = "pálida", distinguishFeatures = "elegância"),
                    ),
                clothing = Clothing(outfitDescription = outfit, accessories = "brincos", carriedItems = "comunicador"),
            ),
        profile = CharacterProfile(occupation = "Diretora", personality = personality),
    )

    @Test
    fun `an empty review changes nothing`() {
        val original = sheet()
        assertEquals(original, original.withDesignReview(CharacterDesignReview()))
    }

    @Test
    fun `blank fields keep the original and rewritten fields replace it trimmed`() {
        val result =
            sheet().withDesignReview(
                CharacterDesignReview(
                    hair = "  pompadour grisalho com raízes de cobre  ",
                    distinctiveMarks = "   ",
                    personality = "teatral, adora plateia",
                    rationale = "hair was a stock bun",
                ),
            )
        assertEquals("pompadour grisalho com raízes de cobre", result.details.physicalTraits.facialDetails.hair)
        assertEquals("expressão de impaciência", result.details.physicalTraits.facialDetails.distinctiveMarks)
        assertEquals("teatral, adora plateia", result.profile.personality)
    }

    @Test
    fun `identity and untouched fields survive a review`() {
        val original = sheet()
        val result = original.withDesignReview(CharacterDesignReview(outfitDescription = "sobretudo de oleado amarelo"))
        assertEquals(original.name, result.name)
        assertEquals(original.lastName, result.lastName)
        assertEquals(original.details.physicalTraits.age, result.details.physicalTraits.age)
        assertEquals(original.details.physicalTraits.facialDetails.jawline, result.details.physicalTraits.facialDetails.jawline)
        assertEquals(original.details.clothing.carriedItems, result.details.clothing.carriedItems)
        assertEquals("sobretudo de oleado amarelo", result.details.clothing.outfitDescription)
    }

    @Test
    fun `changes list only the rewritten fields with before and after`() {
        val original = sheet(hair = "coque")
        val reviewed = original.withDesignReview(CharacterDesignReview(hair = "pompadour grisalho"))
        assertEquals(listOf(Triple("hair", "coque", "pompadour grisalho")), original.designChangesTo(reviewed))
        assertEquals(emptyList<Triple<String, String, String>>(), original.designChangesTo(original))
    }

    @Test
    fun `null fields from the model keep the original`() {
        val original = sheet()
        val result = original.withDesignReview(CharacterDesignReview(hair = null, personality = null, rationale = null))
        assertEquals(original, result)
    }

    @Test
    fun `a missing artwork is not turned into text`() {
        val result = sheet().copy(artwork = null).withDesignReview(CharacterDesignReview())
        assertEquals("", result.artwork)
    }

    @Test
    fun `cast looks are short and carry what a new sheet must differ from`() {
        val long = "x".repeat(500)
        val roster = CharacterPrompts.designRoster(listOf(sheet(hair = long, outfit = long, personality = long)))
        assertEquals(1, roster.size)
        assertEquals("Tereza Gimmani", roster.first()["name"])
        assertEquals(160, roster.first()["hair"]!!.length)
        assertEquals(160, roster.first()["outfit"]!!.length)
        assertEquals(160, roster.first()["personality"]!!.length)
        assertEquals("brincos", roster.first()["accessories"])
        assertEquals("expressão de impaciência; elegância", roster.first()["marks"])
    }
}
