package com.ilustris.sagai.features.act.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

data class BookPage(
    val content: String,
    val pageNumber: Int? = null,
)

data class BookChapter(
    val title: String,
    val pages: List<BookPage>,
)

@Entity(
    tableName = "books",
    foreignKeys = [
        ForeignKey(
            entity = Act::class,
            parentColumns = ["id"],
            childColumns = ["actId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["actId"], unique = true)],
)
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actId: Int,
    val actTitle: String,
    val sagaTitle: String,
    val coverQuote: String,
    val chapters: List<BookChapter>,
    val authorNote: String? = null,
)
