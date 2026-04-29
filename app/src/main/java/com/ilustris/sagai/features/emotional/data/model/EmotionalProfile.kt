package com.ilustris.sagai.features.emotional.data.model

import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone

data class EmotionalProfile(
    val personaTitle: String = "",
    val actionText: String = "",
    val emotionalContent: String = "",
    val dominantTone: EmotionalTone? = null,
)
