package com.ilustris.sagai.features.widgets.domain

/** The two kinds of widget a player creates from a character. */
enum class LinesType {
    QUOTE,
    HOOK,
}

/**
 * What the player built in the creation flow, stored inside the widget it was pinned as.
 *
 * Kept as plain data so it round-trips through Gson without adapters.
 */
data class WidgetConfig(
    val type: LinesType = LinesType.QUOTE,
    val sagaId: Int = 0,
    val characterId: Int = 0,
    val characterName: String = "",
    val lines: List<String> = emptyList(),
    val rotation: RotationRule = RotationRule.EACH_OPEN,
    val discreet: Boolean = false,
    /** Differs between widgets so two with the same lines do not rotate in lockstep. */
    val seed: Int = 0,
)

object WidgetLimits {
    const val MAX_WIDGETS = 10
    const val MAX_LINES = 10
    const val BATCH_SIZE = 10
}
