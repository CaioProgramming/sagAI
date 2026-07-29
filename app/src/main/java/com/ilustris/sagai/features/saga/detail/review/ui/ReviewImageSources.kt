package com.ilustris.sagai.features.saga.detail.review.ui

import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.saga.chat.domain.model.rankTopCharacters

/** A resolved image to show in a review page, with a short caption for it. */
data class ReviewImageSource(
    val url: String,
    val caption: String,
)

/** The saga's own cover art, for an opening/cover-style page. */
fun SagaContent.coverImageSource(): ReviewImageSource? =
    data.icon.takeIf { it.isNotBlank() }?.let { ReviewImageSource(it, data.title) }

/** The most talked-about character's portrait — same ranking used by the Characters stage. */
fun SagaContent.topCharacterImageSource(): ReviewImageSource? {
    val topCharacter =
        flatMessages()
            .rankTopCharacters(characters.filter { it != mainCharacter }.map { it.data })
            .firstOrNull()
            ?.first
            ?: return null
    return topCharacter.image.takeIf { it.isNotBlank() }?.let { ReviewImageSource(it, topCharacter.name) }
}

/** The first chapter with a real cover image — not every chapter has one. */
fun SagaContent.notableChapterImageSource(): ReviewImageSource? {
    val chapter = flatChapters().map { it.data }.firstOrNull { it.coverImage.isNotBlank() } ?: return null
    return ReviewImageSource(chapter.coverImage, chapter.title)
}
