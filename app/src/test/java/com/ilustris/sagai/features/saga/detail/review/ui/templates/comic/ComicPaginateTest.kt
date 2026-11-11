package com.ilustris.sagai.features.saga.detail.review.ui.templates.comic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ComicPaginateTest {
    private fun band() = PanelRequest(PanelSpan.BAND)

    private fun splash() = PanelRequest(PanelSpan.SPLASH)

    private fun plate(group: String) = PanelRequest(PanelSpan.MOSAIC, group)

    private fun member() = PanelRequest(PanelSpan.GRID, CAST_GROUP)

    private fun farewell() = PanelRequest(PanelSpan.GRID, FAREWELL_GROUP)

    /** The reading order [ComicReviewExperience] emits for a complete review. */
    private fun review(
        openingPlates: Int,
        recapPlates: Int,
        members: Int,
        farewells: Int,
    ): List<PanelRequest> =
        buildList {
            add(splash()) // cover
            add(band()) // expressiveness hook
            add(splash()) // emotion
            add(band()) // intro narration
            add(band()) // playstyle hook
            add(band()) // playtime
            repeat(openingPlates) { add(plate("plate-opening")) }
            add(band()) // characters hook
            add(PanelRequest(PanelSpan.FULL)) // cast lead
            repeat(members) { add(member()) }
            add(band()) // journey hook
            if (recapPlates == 0) add(band()) else repeat(recapPlates) { add(plate("plate-journey")) }
            add(band()) // conclusion hook
            add(band()) // conclusion
            repeat(farewells) { add(farewell()) }
            add(splash()) // back cover
        }

    private fun List<ComicSheet>.covered() = flatMap { it.strips + it.panels }.sorted()

    @Test
    fun `empty review has no pages`() {
        assertTrue(paginate(emptyList()).isEmpty())
    }

    @Test
    fun `the first splash is the cover and stands alone`() {
        val sheets = paginate(listOf(splash(), splash()))

        assertEquals(SheetBody.COVER, sheets[0].body)
        assertEquals(listOf(0), sheets[0].panels)
        assertEquals(SheetBody.SPLASH, sheets[1].body)
    }

    @Test
    fun `strips before a splash lie over it instead of taking a page`() {
        val sheets = paginate(listOf(splash(), band(), splash()))

        assertEquals(2, sheets.size)
        assertEquals(SheetBody.SPLASH, sheets[1].body)
        assertEquals(listOf(1), sheets[1].strips)
        assertEquals(listOf(2), sheets[1].panels)
    }

    @Test
    fun `every panel lands on exactly one page`() {
        val requests = review(openingPlates = 3, recapPlates = 8, members = 9, farewells = 6)
        val sheets = paginate(requests)

        assertEquals(requests.indices.toList(), sheets.covered())
    }

    @Test
    fun `caption strips ride the art that follows them`() {
        val sheets = paginate(listOf(band(), plate("p"), plate("p")))

        assertEquals(1, sheets.size)
        assertEquals(listOf(0), sheets[0].strips)
        assertEquals(listOf(1, 2), sheets[0].panels)
    }

    @Test
    fun `more strips than art can carry spill onto a text page first`() {
        val sheets = paginate(listOf(band(), band(), band(), plate("p")))

        assertEquals(SheetBody.TEXT, sheets[0].body)
        assertEquals(listOf(0), sheets[0].strips)
        assertEquals(listOf(1, 2), sheets[1].strips)
    }

    @Test
    fun `trailing strips with no art become a text page`() {
        val sheets = paginate(listOf(splash(), band()))

        assertEquals(SheetBody.TEXT, sheets[1].body)
        assertEquals(listOf(1), sheets[1].strips)
    }

    @Test
    fun `a run never leaves a page holding a lone frame`() {
        // 5 plates over pages of 4 is 3 + 2, not 4 + 1.
        val sheets = paginate(List(5) { plate("p") })

        assertEquals(listOf(3, 2), sheets.map { it.panels.size })
    }

    @Test
    fun `a cast bigger than a page takes another page instead of dropping anyone`() {
        val sheets = paginate(List(11) { member() })

        assertEquals(listOf(6, 5), sheets.map { it.panels.size })
        assertEquals((0 until 11).toList(), sheets.covered())
    }

    @Test
    fun `a cast lead takes its first companions onto its page`() {
        val sheets = paginate(listOf(PanelRequest(PanelSpan.FULL)) + List(4) { member() })

        assertEquals(SheetBody.LEAD, sheets[0].body)
        assertEquals(listOf(0, 1, 2), sheets[0].panels)
        assertEquals(listOf(3, 4), sheets[1].panels)
    }

    @Test
    fun `a cast lead alone is a single frame`() {
        val sheets = paginate(listOf(PanelRequest(PanelSpan.FULL)))

        assertEquals(SheetBody.SINGLE, sheets[0].body)
    }

    @Test
    fun `groups do not merge across group keys`() {
        val sheets = paginate(listOf(member(), member(), farewell(), farewell()))

        assertEquals(listOf(listOf(0, 1), listOf(2, 3)), sheets.map { it.panels })
    }

    @Test
    fun `a short saga takes eight pages`() {
        val sheets = paginate(review(openingPlates = 3, recapPlates = 0, members = 1, farewells = 2))

        assertEquals(8, sheets.size)
    }

    @Test
    fun `a long saga takes eleven pages`() {
        val sheets = paginate(review(openingPlates = 3, recapPlates = 8, members = 9, farewells = 6))

        assertEquals(11, sheets.size)
    }

    @Test
    fun `pages already cut do not change when later sections arrive`() {
        val full = review(openingPlates = 3, recapPlates = 8, members = 9, farewells = 6)
        // The review streams in a section at a time: stop right after the cast.
        val partial = full.take(full.indexOfFirst { it.groupKey == "plate-journey" } - 1)

        val early = paginate(partial)
        val late = paginate(full)

        // Everything but the last early page (which may still be waiting on strips) is final.
        early.dropLast(1).forEachIndexed { index, sheet -> assertEquals(sheet, late[index]) }
    }

    @Test
    fun `sheet keys are unique`() {
        val sheets = paginate(review(openingPlates = 3, recapPlates = 8, members = 9, farewells = 6))

        assertEquals(sheets.size, sheets.map { it.key }.toSet().size)
    }

    @Test
    fun `dealEvenly balances and never exceeds the page size`() {
        assertEquals(listOf(4), dealEvenly(4, 4))
        assertEquals(listOf(3, 2), dealEvenly(5, 4))
        assertEquals(listOf(3, 3, 3), dealEvenly(9, 4))
        assertEquals(listOf(5, 4), dealEvenly(9, 6))
        assertEquals(emptyList<Int>(), dealEvenly(0, 4))
    }
}
