package com.ilustris.sagai.features.saga.detail.review.ui

import com.ilustris.sagai.features.newsaga.data.model.Genre

/**
 * Which [ReviewTemplate] a genre's SagaReview renders with. Genres not listed here
 * fall back to [ReviewTemplate.DEFAULT] — adding a template to a new genre is a
 * one-line change here, nothing else needs to know about it.
 */
fun Genre.reviewTemplate(): ReviewTemplate =
    when (this) {
        Genre.CYBERPUNK -> ReviewTemplate.TERMINAL
        Genre.FANTASY -> ReviewTemplate.BOOK
        else -> ReviewTemplate.DEFAULT
    }
