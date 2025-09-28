package com.ilustris.sagai.features.saga.chat.data.model

data class SceneSummary(
    val currentLocation: String,
    val charactersPresent: List<String>,
    val immediateObjective: String?,
    val currentConflict: String?,
    val mood: String?,
)
