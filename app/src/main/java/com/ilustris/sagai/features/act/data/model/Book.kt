package com.ilustris.sagai.features.act.data.model

data class BookPage(
    val pageNumber: Int,
    val content: String,
    val chapterTitle: String,
    val emotionalTone: String?,
)

data class Book(
    val actTitle: String,
    val sagaTitle: String,
    val coverQuote: String,
    val pages: List<BookPage>,
    val authorNote: String? = null,
)
