package com.ilustris.sagai.features.widgets.domain

/**
 * How many launcher cells a widget occupies. Android sizes a widget as `70 x cells - 30` dp, so the
 * reverse is what lets one layout be picked from whatever size the launcher reports.
 */
data class WidgetCells(
    val columns: Int,
    val rows: Int,
) {
    val area: Int get() = columns * rows

    override fun toString() = "${columns}x$rows"

    companion object {
        /** Smallest size in dp that a widget spanning [cells] cells is guaranteed to get. */
        fun minDp(cells: Int): Int = 70 * cells - 30

        fun fromDp(
            widthDp: Float,
            heightDp: Float,
        ) = WidgetCells(cellCount(widthDp), cellCount(heightDp))

        // The small tolerance covers launchers that report a hair under the nominal size.
        private fun cellCount(dp: Float): Int = ((dp + 30f) / 70f + 0.05f).toInt().coerceAtLeast(1)
    }
}

/**
 * Picks the layout to draw: the largest supported one that fits in this size, or, when none fits
 * (the player resized below the smallest design), the first of the smallest ones.
 */
fun WidgetCells.bestFit(supported: List<WidgetCells>): WidgetCells {
    require(supported.isNotEmpty()) { "A widget needs at least one supported size" }
    val fitting = supported.filter { it.columns <= columns && it.rows <= rows }
    return fitting.maxByOrNull { it.area } ?: supported.minByOrNull { it.area }!!
}

/** The sizes each widget has a layout for. Order matters: it breaks ties in [bestFit]. */
object WidgetSizes {
    private fun c(
        columns: Int,
        rows: Int,
    ) = WidgetCells(columns, rows)

    val CONTINUE = listOf(c(1, 1), c(2, 1), c(4, 1), c(2, 2), c(4, 2))
    val LAST_MESSAGE = listOf(c(2, 1), c(4, 1), c(2, 2), c(4, 2))

    /** No 4x1: at 76dp tall the books and their titles fall below a readable size. */
    val STORIES = listOf(c(2, 2), c(4, 2), c(4, 3))
    val QUOTE = listOf(c(4, 1), c(2, 2), c(4, 2), c(4, 3))
    val HOOK = listOf(c(1, 1), c(4, 1), c(2, 2), c(4, 2))
}
