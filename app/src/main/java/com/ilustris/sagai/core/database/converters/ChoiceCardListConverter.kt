package com.ilustris.sagai.core.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.ilustris.sagai.features.chapter.data.model.ChoiceOption
import com.ilustris.sagai.features.chapter.data.model.GeneratedChoiceCard

class ChoiceCardListConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromChoiceCardList(value: List<GeneratedChoiceCard>?): String? = value?.let { gson.toJson(it) }

    /**
     * Reads both shapes a stored hand can have: the current `options` list, and the flat
     * `optionAText`/`optionATag`/`optionBText`/`optionBTag` fields cards were first written with.
     * The column is plain TEXT either way, so the old shape is translated here rather than
     * rewritten by a migration.
     */
    @TypeConverter
    fun toChoiceCardList(value: String?): List<GeneratedChoiceCard>? {
        if (value == null) return null
        return JsonParser.parseString(value).asJsonArray.map { element ->
            val card = element.asJsonObject
            if (card.has("options")) {
                gson.fromJson(card, GeneratedChoiceCard::class.java)
            } else {
                card.fromLegacyShape()
            }
        }
    }

    private fun JsonObject.fromLegacyShape() =
        GeneratedChoiceCard(
            choiceTitle = string("choiceTitle"),
            options =
                listOf(
                    ChoiceOption(text = string("optionAText"), tag = string("optionATag")),
                    ChoiceOption(text = string("optionBText"), tag = string("optionBTag")),
                ),
        )

    private fun JsonObject.string(key: String): String = get(key)?.takeUnless { it.isJsonNull }?.asString.orEmpty()
}
