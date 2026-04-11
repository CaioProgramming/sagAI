package com.ilustris.sagai.features.newsaga.data.model

import java.util.UUID

data class SagaDraft(
    val title: String = "",
    val description: String = "",
    val genre: Genre = Genre.entries.random(),
    val id: String = UUID.randomUUID().toString(),
)
