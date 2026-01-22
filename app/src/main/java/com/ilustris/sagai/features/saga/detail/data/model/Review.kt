package com.ilustris.sagai.features.saga.detail.data.model

import androidx.room.Embedded

/**
 * Data model for the Saga Review.
 * Now evolved to support "Saga Wrapped" Story Cards.
 * Mapping:
 * - [introduction]: "The Hook" / Title Card text.
 * - [playstyle]: "The Vibe" / Emotional & Playstyle summary.
 * - [topCharacters]: "The Cast" / Key relationships caption.
 * - [actsInsight]: "The Journey" / Key moments caption.
 * - [conclusion]: "The Legacy" / Final send-off.
 */
data class Review(
    @Embedded(prefix = "intro_")
    val introduction: ReviewStage? = null,
    @Embedded(prefix = "playstyle_")
    val playstyle: ReviewStage? = null,
    @Embedded(prefix = "character_")
    val topCharacters: ReviewStage? = null,
    @Embedded(prefix = "journey_")
    val actsInsight: ReviewStage? = null,
    @Embedded(prefix = "activity_")
    val expressiveness: ReviewStage? = null,
    @Embedded(prefix = "conclusion_")
    val conclusion: ReviewStage? = null,
)

data class ReviewStage(
    @Embedded(prefix = "hook_")
    val hook: ReviewText? = null,
    @Embedded(prefix = "content_")
    val content: ReviewText? = null,
)

data class ReviewText(
    val title: String? = null,
    val subtitle: String? = null,
)
