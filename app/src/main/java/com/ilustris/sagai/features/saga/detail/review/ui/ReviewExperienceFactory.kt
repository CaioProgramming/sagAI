package com.ilustris.sagai.features.saga.detail.review.ui

import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.detail.review.ui.templates.book.BookReviewExperience
import com.ilustris.sagai.features.saga.detail.review.ui.templates.collage.CollageReviewExperience
import com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.ComicReviewExperience
import com.ilustris.sagai.features.saga.detail.review.ui.templates.crime.CrimeReviewExperience
import com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal.TerminalReviewExperience
import com.ilustris.sagai.ui.genre.GenreSurfaceStyle
import com.ilustris.sagai.ui.genre.surfaceStyle

object ReviewExperienceFactory {
    /**
     * Escape hatch for a genre that needs a fully bespoke experience instead of a
     * shared [GenreSurfaceStyle] — checked before template resolution. Empty for now;
     * a future genre "graduating" out of a shared template lands here as a one-line
     * addition, with no change to [ReviewExperience]/[ReviewPage]/SagaReview.kt.
     */
    private val bespokeByGenre: Map<Genre, (SagaContent) -> ReviewExperience> = emptyMap()

    fun createExperience(content: SagaContent): ReviewExperience {
        val genre = content.data.genre
        bespokeByGenre[genre]?.let { return it(content) }
        return when (genre.surfaceStyle()) {
            GenreSurfaceStyle.TERMINAL -> TerminalReviewExperience(content)
            GenreSurfaceStyle.BOOK -> BookReviewExperience(content)
            GenreSurfaceStyle.CRIME -> CrimeReviewExperience(content)
            GenreSurfaceStyle.COLLAGE -> CollageReviewExperience(content)
            GenreSurfaceStyle.COMIC -> ComicReviewExperience(content)
            GenreSurfaceStyle.DEFAULT -> DefaultReviewExperience(content)
        }
    }
}
