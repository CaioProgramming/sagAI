package com.ilustris.sagai.core.ai.model

import com.google.gson.JsonParser
import com.ilustris.sagai.core.utils.toJsonMap
import org.junit.Assert.assertEquals
import org.junit.Test

class ArtworkSchemaTest {
    @Test
    fun `the schema the model sees has only the three artwork fields`() {
        val schema = JsonParser.parseString(toJsonMap(Artwork::class.java)).asJsonObject

        // A constant or companion on the class would be reflected here and echoed back by the model.
        assertEquals(setOf("characterIds", "concept", "elements"), schema.keySet())
    }
}
