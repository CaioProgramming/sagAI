package com.ilustris.sagai.core.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ilustris.sagai.features.act.data.model.BookPage

class BookConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromBookPageList(value: List<BookPage>?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toBookPageList(value: String?): List<BookPage>? {
        if (value == null) return null
        val type = object : TypeToken<List<BookPage>>() {}.type
        return gson.fromJson(value, type)
    }
}
