package com.ilustris.sagai.core.database.converters

import com.google.gson.Gson
import com.ilustris.sagai.features.saga.chat.data.model.CharacterPresence
import com.ilustris.sagai.features.saga.chat.data.model.SceneSummary
import com.ilustris.sagai.features.saga.chat.data.model.withNonNullLists
import org.junit.Assert.assertEquals
import org.junit.Test

class CharacterPresenceListConverterTest {
    private val converter = CharacterPresenceListConverter()

    @Test
    fun `round trips the cast`() {
        val cast = listOf(CharacterPresence("Alice", "wary"))
        assertEquals(cast, converter.toCharacterPresenceList(converter.fromCharacterPresenceList(cast)))
    }

    @Test
    fun `stored null reads as an empty cast`() {
        assertEquals(emptyList<CharacterPresence>(), converter.toCharacterPresenceList(null))
        assertEquals(emptyList<CharacterPresence>(), converter.toCharacterPresenceList("null"))
    }

    @Test
    fun `model null for charactersPresent becomes an empty list`() {
        val parsed =
            Gson().fromJson(
                """{"currentLocation":"Docks","charactersPresent":null}""",
                SceneSummary::class.java,
            )
        assertEquals(emptyList<CharacterPresence>(), parsed.withNonNullLists().charactersPresent)
    }
}
