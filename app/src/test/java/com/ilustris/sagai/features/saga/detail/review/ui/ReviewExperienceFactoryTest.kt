package com.ilustris.sagai.features.saga.detail.review.ui

import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.detail.review.ui.templates.book.BookReviewExperience
import com.ilustris.sagai.features.saga.detail.review.ui.templates.terminal.TerminalReviewExperience
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewExperienceFactoryTest {
    @Test
    fun `cyberpunk resolves to the terminal template`() {
        assertEquals(ReviewTemplate.TERMINAL, Genre.CYBERPUNK.reviewTemplate())
    }

    @Test
    fun `fantasy resolves to the book template`() {
        assertEquals(ReviewTemplate.BOOK, Genre.FANTASY.reviewTemplate())
    }

    @Test
    fun `every other genre falls back to the default template`() {
        Genre.entries
            .filterNot { it == Genre.CYBERPUNK || it == Genre.FANTASY }
            .forEach { genre ->
                assertEquals(
                    "$genre should default to ReviewTemplate.DEFAULT",
                    ReviewTemplate.DEFAULT,
                    genre.reviewTemplate(),
                )
            }
    }

    @Test
    fun `factory builds a TerminalReviewExperience for cyberpunk sagas`() {
        val content = SagaContent(data = Saga(genre = Genre.CYBERPUNK))
        assertTrue(ReviewExperienceFactory.createExperience(content) is TerminalReviewExperience)
    }

    @Test
    fun `factory builds a BookReviewExperience for fantasy sagas`() {
        val content = SagaContent(data = Saga(genre = Genre.FANTASY))
        assertTrue(ReviewExperienceFactory.createExperience(content) is BookReviewExperience)
    }

    @Test
    fun `factory builds a DefaultReviewExperience for genres without a template`() {
        val content = SagaContent(data = Saga(genre = Genre.HORROR))
        assertTrue(ReviewExperienceFactory.createExperience(content) is DefaultReviewExperience)
    }
}
