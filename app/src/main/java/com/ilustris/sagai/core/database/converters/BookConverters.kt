package com.ilustris.sagai.core.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.ilustris.sagai.core.ai.gsonTypeOfList
import com.ilustris.sagai.features.act.data.model.BookChapter

class BookConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromBookChapterList(value: List<BookChapter>?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toBookChapterList(value: String?): List<BookChapter>? {
        if (value == null) return null
        return gson.fromJson(value, gsonTypeOfList<BookChapter>())
    }
}
