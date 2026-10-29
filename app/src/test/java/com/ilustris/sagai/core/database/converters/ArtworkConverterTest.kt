package com.ilustris.sagai.core.database.converters

import com.google.gson.Gson
import com.ilustris.sagai.core.ai.model.Artwork
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArtworkConverterTest {
    private val converter = ArtworkConverter()

    @Test
    fun `round trips the concept, the cover cast and the story elements`() {
        val artwork =
            Artwork(
                concept = "Two rivals share a lantern over the flooded archive.",
                characterIds = listOf(12, 15, 18),
                elements = listOf("the flooded archive", "a cracked lantern"),
            )

        assertEquals(artwork, converter.toArtwork(converter.fromArtwork(artwork)))
    }

    @Test
    fun `reads the bare concept text older rows and backups carry`() {
        assertEquals(
            Artwork(concept = "A lone rider crossing the salt flats."),
            converter.toArtwork("A lone rider crossing the salt flats."),
        )
    }

    @Test
    fun `reads what the 41 to 42 migration writes for a plain text row`() {
        val migrated = Gson().toJson(Artwork(concept = "A lone rider crossing the salt flats."))

        assertEquals(
            Artwork(concept = "A lone rider crossing the salt flats."),
            converter.toArtwork(migrated),
        )
    }

    @Test
    fun `an empty or missing column is no artwork`() {
        assertNull(converter.toArtwork(null))
        assertNull(converter.toArtwork(""))
        assertNull(converter.toArtwork("   "))
    }

    @Test
    fun `a blank artwork is stored as no artwork`() {
        assertNull(converter.fromArtwork(null))
        assertNull(converter.fromArtwork(Artwork()))
    }

    @Test
    fun `a malformed json object reads as no artwork instead of crashing the row`() {
        assertNull(converter.toArtwork("{not json"))
    }
}
