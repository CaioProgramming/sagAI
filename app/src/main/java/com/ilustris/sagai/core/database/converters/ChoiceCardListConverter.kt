package com.ilustris.sagai.core.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.ilustris.sagai.core.ai.gsonTypeOfList
import com.ilustris.sagai.features.chapter.data.model.GeneratedChoiceCard

class ChoiceCardListConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromChoiceCardList(value: List<GeneratedChoiceCard>?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toChoiceCardList(value: String?): List<GeneratedChoiceCard>? {
        if (value == null) return null
        return gson.fromJson(value, gsonTypeOfList<GeneratedChoiceCard>())
    }
}
