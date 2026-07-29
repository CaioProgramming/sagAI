package com.ilustris.sagai.core.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.ilustris.sagai.core.ai.gsonTypeOfList
import com.ilustris.sagai.features.saga.detail.data.model.Farewell

class FarewellListConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromFarewellList(value: List<Farewell>?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toFarewellList(value: String?): List<Farewell>? {
        if (value == null) return null
        return gson.fromJson(value, gsonTypeOfList<Farewell>())
    }
}
