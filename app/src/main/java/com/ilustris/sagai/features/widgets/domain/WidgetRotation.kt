package com.ilustris.sagai.features.widgets.domain

/** When a generated widget moves on to its next line. */
enum class RotationRule {
    /** Advances every time the app is opened. */
    EACH_OPEN,

    /** Advances once a day, with no state to keep: it only depends on the date. */
    DAILY,

    /** Always shows the first line. */
    FIXED,
}

object WidgetRotation {
    /**
     * Index of the line to show.
     *
     * @param openCounter how many times the app has been opened (only used by [RotationRule.EACH_OPEN])
     * @param epochDay days since 1970-01-01 (only used by [RotationRule.DAILY])
     * @param seed offsets the sequence so two widgets with the same lines do not show the same one
     */
    fun indexFor(
        rule: RotationRule,
        lineCount: Int,
        openCounter: Int,
        epochDay: Long,
        seed: Int = 0,
    ): Int {
        if (lineCount <= 1) return 0
        return when (rule) {
            RotationRule.EACH_OPEN -> Math.floorMod(openCounter + seed, lineCount)
            RotationRule.DAILY -> Math.floorMod(epochDay + seed, lineCount.toLong()).toInt()
            RotationRule.FIXED -> 0
        }
    }
}
