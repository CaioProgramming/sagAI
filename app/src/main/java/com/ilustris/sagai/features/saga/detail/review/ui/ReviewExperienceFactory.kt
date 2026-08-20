package com.ilustris.sagai.features.saga.detail.review.ui

import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.detail.review.ui.templates.book.BookReviewExperience
import com.ilustris.sagai.features.saga.detail.review.ui.templates.collage.CollageReviewExperience
import com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.ComicReviewExperience
import com.ilustris.sagai.features.saga.detail.review.ui.templates.crime.CrimeReviewExperience
import com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal.TerminalReviewExperience

object ReviewExperienceFactory {
    /**
     * Escape hatch for a genre that needs a fully bespoke experience instead of a
     * shared [ReviewTemplate] — checked before template resolution. Empty for now;
     * a future genre "graduating" out of a shared template lands here as a one-line
     * addition, with no change to [ReviewExperience]/[ReviewPage]/SagaReview.kt.
     */
    private val bespokeByGenre: Map<Genre, (SagaContent) -> ReviewExperience> = emptyMap()

    fun createExperience(content: SagaContent): ReviewExperience {
        val genre = content.data.genre
        bespokeByGenre[genre]?.let { return it(content) }
        return when (genre.reviewTemplate()) {
            ReviewTemplate.TERMINAL -> TerminalReviewExperience(content)
            ReviewTemplate.BOOK -> BookReviewExperience(content)
            ReviewTemplate.CRIME -> CrimeReviewExperience(content)
            ReviewTemplate.COLLAGE -> CollageReviewExperience(content)
            ReviewTemplate.COMIC -> ComicReviewExperience(content)
            ReviewTemplate.DEFAULT -> DefaultReviewExperience(content)
        }
    }
}
