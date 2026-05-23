package com.ilustris.sagai.features.saga.chat.data.model

import com.ilustris.sagai.features.timeline.data.model.Timeline

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
    val establishedFacts: List<String>? = emptyList(),
    val possibleOutcomes: List<String>? = emptyList(),
    val quote: String? = null,
)

/** True when the summary can drive chat objectives and suggestions. */
fun SceneSummary.isActive(): Boolean =
    immediateObjective?.isNotBlank() == true || currentLocation.isNotBlank()

fun Timeline.hasActiveSceneSummary(): Boolean =
    !currentObjective.isNullOrBlank() || (sceneSummary?.isActive() == true)

/** Open timeline that still needs an AI-generated scene summary. */
fun Timeline.shouldEnsureSceneSummary(): Boolean =
    id != 0 && !isEmpty() && !hasActiveSceneSummary()
