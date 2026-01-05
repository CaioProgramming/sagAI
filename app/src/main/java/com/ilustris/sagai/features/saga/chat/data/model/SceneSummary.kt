package com.ilustris.sagai.features.saga.chat.data.model

data class SceneSummary(
    val currentLocation: String,
    val charactersPresent: List<String>,
    val immediateObjective: String?,
    val currentConflict: String?,
    val mood: String?,
    val currentTimeOfDay: String?,
    val tensionLevel: Int? = 5,
    val spatialContext: String? = null,
    val narrativePacing: String? = "Steady",
    val worldStateChanges: List<String>? = emptyList(),
    val relevantPastContext: List<String>? = emptyList(),
)
