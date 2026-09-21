package com.ilustris.sagai.core.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.ilustris.sagai.core.ai.gsonTypeOfList
import com.ilustris.sagai.features.timeline.data.model.NotableExchange

class TimelineConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromNotableExchangeList(value: List<NotableExchange>?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toNotableExchangeList(value: String?): List<NotableExchange>? {
        if (value == null) return null
        return gson.fromJson(value, gsonTypeOfList<NotableExchange>())
    }
}
