package com.ilustris.sagai.features.narrative.data.model

enum class TimeOfDay {
    DAWN,
    MORNING,
    AFTERNOON,
    DUSK,
    EVENING,
    NIGHT,
    LATE_NIGHT,
}

/** Coarse bucket for how much narrative time passed since the previous checkpoint — never an absolute date. */
enum class TimeGapMagnitude {
    MOMENTS,
    HOURS,
    SAME_DAY,
    NEXT_DAY,
    DAYS,
    WEEKS,
    MONTHS,
    YEARS,
}

/**
 * Where/when a [com.ilustris.sagai.features.chapter.data.model.Chapter] or
 * [com.ilustris.sagai.features.act.data.model.Act] begins or ends. [elapsedNote]/[timeGap]
 * describe only the delta since the previous checkpoint (AI-authored, natural language),
 * never an accumulated day count — see the world-building plan for why.
 */
data class LocationCheckpoint(
    val locationId: Int? = null,
    val locationName: String? = null,
    val timeOfDay: TimeOfDay? = null,
    val elapsedNote: String? = null,
    val timeGap: TimeGapMagnitude? = null,
)

/** AI-facing counterpart of [LocationCheckpoint]: location is a free-form name to be resolved/created. */
data class GeneratedLocationCheckpoint(
    val locationName: String = "",
    val newLocationHistory: String? = null,
    val timeOfDay: TimeOfDay? = null,
    val elapsedNote: String? = null,
    val timeGap: TimeGapMagnitude? = null,
)
