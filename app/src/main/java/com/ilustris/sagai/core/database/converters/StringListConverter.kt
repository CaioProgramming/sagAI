package com.ilustris.sagai.core.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.ilustris.sagai.core.ai.gsonTypeOfStringList

class StringListConverter {
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        if (value == null) return null
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        if (value == null) return null
        return Gson().fromJson(value, gsonTypeOfStringList())
    }
}
