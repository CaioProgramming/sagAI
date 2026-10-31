package com.ilustris.sagai.features.saga.detail.review.ui.templates.comic

/** What a panel asks of the page it lands on — its span, plus the group it travels with. */
internal data class PanelRequest(
    val span: PanelSpan,
    val groupKey: String? = null,
)

/** How a sheet's body (everything under its caption strips) is cut up. */
internal enum class SheetBody {
    /** The issue's cover: one frame, the whole page, no margin. */
    COVER,

    /** Text only — the sheet is nothing but caption strips. */
    TEXT,

    /** One frame owns the body. */
    SINGLE,

    /** A lead frame on top, the rest side by side underneath (a cast lead and its first members). */
    LEAD,

    /** Uneven rows of art — a run of chapter plates. */
    MOSAIC,

    /** Equal cells — a cast, a run of send-offs. */
    GRID,
}

/**
 * One printed page of the comic: caption [strips] hung across the top (text-only panels that
 * would otherwise cost a whole page for a few lines), then a [body] of framed [panels].
 * Indices point into the review's page list.
 */
internal data class ComicSheet(
    val body: SheetBody,
    val strips: List<Int>,
    val panels: List<Int>,
) {
    /**
     * Stable across the review streaming in: a sheet keeps its identity as long as it starts on the
     * same panel, so the pager does not jump when a later section arrives.
     */
    val key: Int get() = (strips + panels).minOrNull() ?: -1
}

/** A cast fits more to a page than send-offs do: faces need little room, messages need a lot. */
private const val DEFAULT_GRID_PER_SHEET = 4
private const val MOSAIC_PER_SHEET = 4
private const val LEAD_COMPANIONS = 2
private const val MAX_STRIPS_ON_ART = 2
private const val MAX_STRIPS_ON_TEXT = 2

private val GRID_PER_SHEET: Map<String, Int> =
    mapOf(
        CAST_GROUP to 9,
        FAREWELL_GROUP to 4,
    )

/**
 * Cuts the review's panels into printed pages.
 *
 * Panels arrive in reading order. Text-only panels ([PanelSpan.BAND]) wait and hang over the next
 * page of art as caption strips; a run of peers ([PanelSpan.GRID], [PanelSpan.MOSAIC]) is dealt
 * across as many pages as it needs, evenly, so no page is left holding a lone frame. Nothing is
 * dropped: a cast too big for one page simply takes another.
 *
 * Pure and prefix-stable — the review streams in a section at a time, and every section is whole
 * when it arrives, so pages already cut never change when a later one lands.
 */
internal fun paginate(requests: List<PanelRequest>): List<ComicSheet> {
    val sheets = mutableListOf<ComicSheet>()
    val strips = mutableListOf<Int>()

    /** Strips that cannot ride on art become pages of their own, oldest first. */
    fun spillStrips(keep: Int) {
        while (strips.size > keep) {
            val take = minOf(MAX_STRIPS_ON_TEXT, strips.size - keep)
            sheets += ComicSheet(SheetBody.TEXT, strips.take(take), emptyList())
            repeat(take) { strips.removeAt(0) }
        }
    }

    fun takeStrips(): List<Int> {
        spillStrips(MAX_STRIPS_ON_ART)
        return strips.toList().also { strips.clear() }
    }

    var index = 0
    while (index < requests.size) {
        val request = requests[index]
        when (request.span) {
            PanelSpan.BAND -> {
                strips += index
                index++
            }

            PanelSpan.SPLASH -> {
                val isCover = sheets.isEmpty() && strips.isEmpty()
                sheets +=
                    ComicSheet(
                        body = if (isCover) SheetBody.COVER else SheetBody.SINGLE,
                        strips = if (isCover) emptyList() else takeStrips(),
                        panels = listOf(index),
                    )
                index++
            }

            PanelSpan.FULL -> {
                val companions =
                    run {
                        val next = index + 1
                        val key = requests.getOrNull(next)?.groupKey
                        generateSequence(next) { it + 1 }
                            .takeWhile {
                                it < requests.size &&
                                    requests[it].span == PanelSpan.GRID &&
                                    requests[it].groupKey == key
                            }.take(LEAD_COMPANIONS)
                            .toList()
                    }
                sheets +=
                    ComicSheet(
                        body = if (companions.isEmpty()) SheetBody.SINGLE else SheetBody.LEAD,
                        strips = takeStrips(),
                        panels = listOf(index) + companions,
                    )
                index += 1 + companions.size
            }

            PanelSpan.MOSAIC, PanelSpan.GRID, PanelSpan.NORMAL -> {
                val key = request.groupKey
                val run =
                    generateSequence(index) { it + 1 }
                        .takeWhile {
                            it < requests.size &&
                                requests[it].span == request.span &&
                                requests[it].groupKey == key
                        }.toList()
                val perSheet =
                    if (request.span == PanelSpan.GRID) {
                        GRID_PER_SHEET[key] ?: DEFAULT_GRID_PER_SHEET
                    } else {
                        MOSAIC_PER_SHEET
                    }
                val body = if (request.span == PanelSpan.GRID) SheetBody.GRID else SheetBody.MOSAIC
                var start = 0
                dealEvenly(run.size, perSheet).forEachIndexed { sheetIndex, size ->
                    sheets +=
                        ComicSheet(
                            body = body,
                            strips = if (sheetIndex == 0) takeStrips() else emptyList(),
                            panels = run.subList(start, start + size),
                        )
                    start += size
                }
                index += run.size
            }
        }
    }

    spillStrips(0)
    return sheets
}

/**
 * Splits [count] items over the fewest pages of at most [perSheet], as evenly as it can — 5 over
 * pages of 4 is 3 + 2 rather than 4 + 1, because a page holding a single frame reads as an accident.
 */
internal fun dealEvenly(
    count: Int,
    perSheet: Int,
): List<Int> {
    if (count <= 0) return emptyList()
    val pages = (count + perSheet - 1) / perSheet
    val base = count / pages
    val extra = count % pages
    return List(pages) { if (it < extra) base + 1 else base }
}
