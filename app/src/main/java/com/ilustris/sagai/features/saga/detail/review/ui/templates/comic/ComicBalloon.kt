package com.ilustris.sagai.features.saga.detail.review.ui.templates.comic

import androidx.compose.ui.graphics.Shape
import com.ilustris.sagai.ui.genre.comic.ComicBalloonSpec
import com.ilustris.sagai.ui.genre.comic.ComicPanel
import com.ilustris.sagai.ui.genre.comic.SlantShape

/**
 * How much of the page a frame takes.
 *
 * [SPLASH] panels form the opening band — the cover and anything meant to stand beside it at the
 * same scale. They share that band rather than stacking, which is what keeps the page from running
 * away vertically when more than one frame deserves cover-sized treatment.
 *
 * [FULL] owns a normal row outright; [NORMAL] shares one with its neighbours.
 */
enum class PanelSpan {
    NORMAL,
    FULL,
    SPLASH,

    /**
     * Laid out with its neighbours in an even grid, every cell the same size. Used where a set of
     * frames are peers — a cast, a run of send-offs — and letting the row templates loose on them
     * would hand one an accidental full-width row.
     */
    GRID,

    /**
     * Like [GRID] in that it is laid out as a group, but in uneven rows rather than equal cells.
     * For sets where variety is the point — a run of chapter art — and a repeating cell size would
     * read as a contact sheet.
     */
    MOSAIC,

    /** Full width but short: a strip for a beat that is only words and needs no room for art. */
    BAND,
}

/**
 * A review page that knows it is being drawn as a comic frame: how much of the page it wants, and
 * what hangs off it. Implemented by the bespoke comic panels; pages that don't implement it fall
 * back to a normal-width frame with nothing over it.
 */
interface ComicPanelPage {
    val panelSpan: PanelSpan get() = PanelSpan.NORMAL

    /**
     * Ties consecutive [PanelSpan.GRID] panels into one block. Only a run sharing the same key is
     * gridded together, so a cast and a set of farewells never merge into one arrangement.
     */
    val groupKey: String? get() = null

    /**
     * Whether the frame draws its border and ground. A page that is only balloons sets this false
     * so its boxes read as loose on the page rather than boxed inside an empty frame.
     */
    val hasFrame: Boolean get() = true

    /**
     * The frame's outline. Null means a plain rectangle; a [SlantShape] throws its vertical edges
     * off true so neighbouring frames meet on a diagonal instead of squaring up into a table.
     */
    val panelShape: Shape? get() = null

    /** Drawn in order — later balloons stack above earlier ones. */
    val balloons: List<ComicBalloonSpec> get() = emptyList()
}
