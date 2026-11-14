package com.ilustris.sagai.core.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.ilustris.sagai.core.ai.gsonTypeOfList
import com.ilustris.sagai.features.saga.chat.data.model.CharacterPresence

class CharacterPresenceListConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromCharacterPresenceList(value: List<CharacterPresence>?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toCharacterPresenceList(value: String?): List<CharacterPresence>? {
        // SceneSummary.charactersPresent is non-null, but rows written while the model sent `null`
        // hold SQL NULL (or "null"); handing that back crashes the whole timeline read.
        if (value == null) return emptyList()
        return try {
            gson.fromJson<List<CharacterPresence>?>(value, gsonTypeOfList<CharacterPresence>()) ?: emptyList()
        } catch (e: JsonSyntaxException) {
            // Pre-migration rows stored charactersPresent as a plain List<String>; the scene
            // summary regenerates on the next turn regardless, so drop the stale shape instead
            // of crashing on read.
            emptyList()
        }
    }
}
