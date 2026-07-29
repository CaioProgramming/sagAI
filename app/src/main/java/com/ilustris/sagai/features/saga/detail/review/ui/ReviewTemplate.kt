package com.ilustris.sagai.features.saga.detail.review.ui

/**
 * The presentation style a [ReviewExperience] renders with. Decoupled from [Genre][com.ilustris.sagai.features.newsaga.data.model.Genre]
 * on purpose, so several genres can share the same template (see [reviewTemplate]).
 */
enum class ReviewTemplate {
    DEFAULT,
    TERMINAL,
    BOOK,
}
