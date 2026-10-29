package com.ilustris.sagai.features.characters.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FindByDisplayNameTest {
    private val valenteFamily =
        listOf(
            character(id = 1, name = "Cami", lastName = "Valente"),
            character(id = 2, name = "Lucas", lastName = "Valente"),
            character(id = 3, name = "Sofia", lastName = "Valente"),
        )

    @Test
    fun `strict full name resolves exact character`() {
        assertEquals(3, valenteFamily.findByDisplayName("Sofia Valente")?.id)
    }

    @Test
    fun `unknown full name does not match relatives with shared surname`() {
        assertNull(
            listOf(
                character(id = 1, name = "Cami", lastName = "Valente"),
                character(id = 2, name = "Lucas", lastName = "Valente"),
            ).findByDisplayName("Sofia Valente"),
        )
    }

    @Test
    fun `shared surname alone is ambiguous`() {
        assertNull(valenteFamily.findByDisplayName("Valente"))
    }

    @Test
    fun `unique first name resolves character`() {
        assertEquals(2, valenteFamily.findByDisplayName("Lucas")?.id)
    }

    @Test
    fun `nickname resolves character`() {
        val characters =
            listOf(
                character(id = 4, name = "Sofia", lastName = "Valente", nicknames = listOf("Sofi")),
            )
        assertEquals(4, characters.findByDisplayName("Sofi")?.id)
    }

    @Test
    fun `a name inside someone else's nickname does not resolve to that person`() {
        val characters =
            listOf(
                character(id = 5, name = "Ren", lastName = "Vargas", nicknames = listOf("O Muralha", "Cão de guarda de Tamiko")),
            )
        assertNull(characters.findByDisplayName("Tamiko"))
        assertEquals(5, characters.findByDisplayName("Cão de guarda de Tamiko")?.id)
    }

    private fun character(
        id: Int,
        name: String,
        lastName: String,
        nicknames: List<String> = emptyList(),
    ) = Character(
        id = id,
        name = name,
        lastName = lastName,
        nicknames = nicknames,
        details = Details(),
        profile = CharacterProfile(),
    )
}
