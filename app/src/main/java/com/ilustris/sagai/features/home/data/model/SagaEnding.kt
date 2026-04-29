package com.ilustris.sagai.features.home.data.model

import com.ilustris.sagai.features.emotional.data.model.EmotionalProfile

/**
 * Represents the complete conclusion of a Saga, merging narrative and emotional insights.
 */
data class SagaEnding(
    val endingMessage: String = "",
    val emotionalProfile: EmotionalProfile = EmotionalProfile(),
)
