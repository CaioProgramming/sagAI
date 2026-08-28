package com.ilustris.sagai.features.saga.detail.review.ui.templates.crime

import com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.PanelSpan

/**
 * A review page that knows it's pinned to [CorkboardBoard]: how much of the board it wants, and
 * whether it clusters with peers. Mirrors
 * [com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.ComicPanelPage] minus the
 * balloon concept — a pin's caption lives inside its own chrome, nothing floats over it.
 */
interface CorkboardPinPage {
    val panelSpan: PanelSpan get() = PanelSpan.NORMAL

    /** Ties consecutive [PanelSpan.GRID]/[PanelSpan.MOSAIC] pins into one cluster. */
    val groupKey: String? get() = null
}
