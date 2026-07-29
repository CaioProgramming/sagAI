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
    /**
     * "The Send-Off" — a short farewell message per top character, shown between
     * [conclusion] and the summary card. Not `@Embedded`: a variable-length list can't
     * flatten into fixed columns, so it's stored as a single Gson-serialized column
     * (see [com.ilustris.sagai.core.database.converters.FarewellListConverter]).
     */
    val farewells: List<Farewell>? = null,
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

data class Farewell(
    val characterId: Int = 0,
    val message: String = "",
)

/** AI response wrapper for the farewells step — mirrors [com.ilustris.sagai.features.saga.chat.data.model.SuggestionGen]. */
data class FarewellSet(
    val farewells: List<Farewell> = emptyList(),
)

fun Review?.isComplete(): Boolean {
    if (this == null) return false
    return introduction != null &&
        expressiveness != null &&
        playstyle != null &&
        topCharacters != null &&
        actsInsight != null &&
        conclusion != null &&
        farewells != null
}

fun Review?.hasViewablePages(): Boolean = this?.introduction != null

fun Review.completedStepCount(): Int =
    listOf(
        introduction,
        expressiveness,
        playstyle,
        topCharacters,
        actsInsight,
        conclusion,
        farewells,
    ).count { it != null }
