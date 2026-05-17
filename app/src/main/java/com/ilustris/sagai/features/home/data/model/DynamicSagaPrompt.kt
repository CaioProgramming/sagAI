package com.ilustris.sagai.features.home.data.model

import com.ilustris.sagai.features.newsaga.data.model.Genre

data class DynamicSagaPrompt(
    val title: String = "",
    val subtitle: String = "",
    val genre: Genre? = null,
)
