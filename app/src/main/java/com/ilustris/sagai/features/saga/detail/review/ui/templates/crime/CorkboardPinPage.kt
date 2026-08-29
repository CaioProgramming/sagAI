package com.ilustris.sagai.features.saga.detail.review.ui.templates.crime

/**
 * How much of the table one pin takes up. Deliberately its own small vocabulary rather than the
 * comic board's `PanelSpan`: a comic span describes how a frame packs into a *page* of rows, which
 * is the shape the corkboard was borrowing and the reason it still read as a comic page. Photos
 * spread on a table only need to differ in size, so that's all this says.
 */
enum class CorkPinSize(
    /** Fraction of the viewport's width this pin occupies. */
    val widthFraction: Float,
) {
    /** The opening photo — the biggest thing on the table. */
    COVER(0.74f),

    /** A portrait or chapter still. */
    PHOTO(0.5f),

    /** An index card of handwriting, wider than a photo because it's read, not looked at. */
    NOTE(0.62f),
}

/**
 * A review page that knows it's pinned to [CorkboardStrip] — how big a card it wants on the table.
 *
 * Height is not part of this: the strip measures every pin against its own content, which is what
 * stops long handwriting from spilling over its neighbours the way fixed panel rects did.
 */
interface CorkboardPinPage {
    val pinSize: CorkPinSize get() = CorkPinSize.PHOTO
}
