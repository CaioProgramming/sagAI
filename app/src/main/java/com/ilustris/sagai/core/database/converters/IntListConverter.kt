package com.ilustris.sagai.core.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.ilustris.sagai.core.ai.gsonTypeOfIntList

object IntListConverter {
    @TypeConverter
    @JvmStatic
    fun fromString(value: String?): List<Int>? {
        if (value == null) {
            return null
        }
        return Gson().fromJson(value, gsonTypeOfIntList())
    }

    @TypeConverter
    @JvmStatic
    fun fromList(list: List<Int>?): String? {
        if (list == null) {
            return null
        }
        return Gson().toJson(list)
    }
}
