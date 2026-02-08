package com.ilustris.sagai.features.newsaga.data.model

data class SagaDraft(
    val title: String = "",
    val description: String = "",
    val genre: Genre = Genre.entries.random(),
)
