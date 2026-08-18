package com.ilustris.sagai.features.saga.detail.review.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre

/**
 * Which [ReviewTemplate] a genre's SagaReview renders with. Genres not listed here
 * fall back to [ReviewTemplate.DEFAULT] — adding a template to a new genre is a
 * one-line change here, nothing else needs to know about it.
 */
fun Genre.reviewTemplate(): ReviewTemplate =
    when (this) {
        Genre.CYBERPUNK, Genre.SPACE_OPERA -> ReviewTemplate.TERMINAL
        Genre.FANTASY, Genre.SHINOBI, Genre.COWBOY, Genre.HORROR -> ReviewTemplate.BOOK
        Genre.CRIME -> ReviewTemplate.CRIME
        Genre.PUNK_ROCK -> ReviewTemplate.COLLAGE
        else -> ReviewTemplate.DEFAULT
    }

/**
 * What the Characters/Cast stage calls itself for genres with a stronger in-world label than the
 * generic [R.string.review_stage_characters_title] — properly localized (EN/pt-BR) rather than
 * hardcoded, unlike [com.ilustris.sagai.features.newsaga.data.model.subtitle]'s pattern elsewhere:
 * the review feature specifically has to stay out of hardcoded English (see
 * `review_farewells_title`'s missing-translation fix earlier in this same body of work).
 */
@Composable
fun Genre.reviewCastTitle(): String =
    when (this) {
        Genre.FANTASY -> stringResource(R.string.review_cast_title_fantasy)
        Genre.SHINOBI -> stringResource(R.string.review_cast_title_shinobi)
        Genre.COWBOY -> stringResource(R.string.review_cast_title_cowboy)
        Genre.HORROR -> stringResource(R.string.review_cast_title_horror)
        else -> stringResource(R.string.review_stage_characters_title)
    }
