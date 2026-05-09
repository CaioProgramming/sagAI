package com.ilustris.sagai.features.home.data.model

import com.ilustris.sagai.features.newsaga.data.model.Genre

/**
 * A lightweight projection of the Saga entity.
 */
data class SagaInfo(
    val id: Int,
    val title: String,
    val genre: Genre,
    val variationId: String,
)

fun SagaInfo.toSaga() =
    Saga(
        id = id,
        title = title,
        genre = genre,
        variationId = variationId,
    )
