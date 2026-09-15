package com.ilustris.sagai.core.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.ilustris.sagai.core.ai.gsonTypeOfList
import com.ilustris.sagai.features.act.data.model.BookChapter
import com.ilustris.sagai.features.act.data.model.BookPage
import com.ilustris.sagai.features.act.data.model.WriterNotes
import com.ilustris.sagai.features.audiobook.data.model.WordTiming

class BookConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromBookChapterList(value: List<BookChapter>?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toBookChapterList(value: String?): List<BookChapter>? {
        if (value == null) return null
        return gson.fromJson(value, gsonTypeOfList<BookChapter>())
    }

    @TypeConverter
    fun fromBookPageList(value: List<BookPage>?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toBookPageList(value: String?): List<BookPage>? {
        if (value == null) return null
        return gson.fromJson(value, gsonTypeOfList<BookPage>())
    }

    @TypeConverter
    fun fromWriterNotes(value: WriterNotes?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toWriterNotes(value: String?): WriterNotes? {
        if (value == null) return null
        return gson.fromJson(value, WriterNotes::class.java)
    }

    @TypeConverter
    fun fromWordTimings(value: List<WordTiming>?): String? = value?.let { gson.toJson(it) }

    @TypeConverter
    fun toWordTimings(value: String?): List<WordTiming>? {
        if (value == null) return null
        return gson.fromJson(value, gsonTypeOfList<WordTiming>())
    }
}
