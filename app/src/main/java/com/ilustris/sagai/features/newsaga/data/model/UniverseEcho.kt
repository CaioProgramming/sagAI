package com.ilustris.sagai.features.newsaga.data.model

import com.ilustris.sagai.features.newsaga.data.usecase.SagaBook

data class UniverseEcho(
    val input: String,
    val genre: Genre,
)

data class UniverseSuggestions(
    val message: String = "",
    val suggestions: List<UniverseEcho>,
)

data class LibraryPitchesResponse(
    val books: List<SagaBook>,
    val welcomeMessage: String = "",
)

data class SagaIdeas(
    val ideas: List<SagaDraft>,
    val message: String = "",
)
