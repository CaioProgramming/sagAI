package com.ilustris.sagai.core.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.ilustris.sagai.core.ai.model.Artwork

class ArtworkConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromArtwork(value: Artwork?): String? = value?.takeUnless { it.isBlank() }?.let { gson.toJson(it) }

    /**
     * Reads the JSON the column holds today, and the bare concept text older rows and backups
     * carry: the column is plain TEXT either way, so a value that is not a JSON object is the
     * concept itself.
     */
    @TypeConverter
    fun toArtwork(value: String?): Artwork? {
        if (value.isNullOrBlank()) return null
        if (!value.trimStart().startsWith("{")) return Artwork(concept = value)
        return runCatching { gson.fromJson(value, Artwork::class.java) }.getOrNull()
    }
}
