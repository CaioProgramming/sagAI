package com.ilustris.sagai.features.saga.detail.review.ui

import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.detail.review.ui.templates.book.BookReviewExperience
import com.ilustris.sagai.features.saga.detail.review.ui.templates.collage.CollageReviewExperience
import com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.ComicReviewExperience
import com.ilustris.sagai.features.saga.detail.review.ui.templates.crime.CrimeReviewExperience
import com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal.TerminalReviewExperience
import com.ilustris.sagai.ui.genre.GenreSurfaceStyle
import com.ilustris.sagai.ui.genre.surfaceStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Written against `surfaceStyle()`, which replaced a `ReviewTemplate` enum this file still named.
 * The rename left it uncompilable, and one file that does not compile takes the whole test source
 * set with it, so nothing here had run since.
 *
 * Its last case had also become false rather than merely stale: it asserted that HORROR falls back
 * to the default experience, and HORROR reads as a book now.
 */
class ReviewExperienceFactoryTest {
    @Test
    fun `each genre resolves to the surface its style names`() {
        val expected =
            mapOf(
                Genre.CYBERPUNK to GenreSurfaceStyle.TERMINAL,
                Genre.SPACE_OPERA to GenreSurfaceStyle.TERMINAL,
                Genre.FANTASY to GenreSurfaceStyle.BOOK,
                Genre.SHINOBI to GenreSurfaceStyle.BOOK,
                Genre.COWBOY to GenreSurfaceStyle.BOOK,
                Genre.HORROR to GenreSurfaceStyle.BOOK,
                Genre.CRIME to GenreSurfaceStyle.CRIME,
                Genre.PUNK_ROCK to GenreSurfaceStyle.COLLAGE,
                Genre.HEROES to GenreSurfaceStyle.COMIC,
            )

        // Every genre, not a sample: a genre added without a style decision is the failure this
        // catches, and it would otherwise surface as a screen quietly rendering the wrong look.
        assertEquals(Genre.entries.toSet(), expected.keys)
        expected.forEach { (genre, style) ->
            assertEquals("$genre resolves to the wrong surface", style, genre.surfaceStyle())
        }
    }

    @Test
    fun `the factory builds the experience its genre's style calls for`() {
        Genre.entries.forEach { genre ->
            val experience = ReviewExperienceFactory.createExperience(sagaOf(genre))
            val matches =
                when (genre.surfaceStyle()) {
                    GenreSurfaceStyle.TERMINAL -> experience is TerminalReviewExperience
                    GenreSurfaceStyle.BOOK -> experience is BookReviewExperience
                    GenreSurfaceStyle.CRIME -> experience is CrimeReviewExperience
                    GenreSurfaceStyle.COLLAGE -> experience is CollageReviewExperience
                    GenreSurfaceStyle.COMIC -> experience is ComicReviewExperience
                    GenreSurfaceStyle.DEFAULT -> experience is DefaultReviewExperience
                }
            assertTrue(
                "$genre got ${experience.javaClass.simpleName} for ${genre.surfaceStyle()}",
                matches,
            )
        }
    }

    private fun sagaOf(genre: Genre) = SagaContent(data = Saga(genre = genre))
}
