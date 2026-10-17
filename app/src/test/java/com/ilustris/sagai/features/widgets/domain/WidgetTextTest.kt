package com.ilustris.sagai.features.widgets.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetTextTest {
    @Test
    fun `tags go and the words stay`() {
        assertEquals(
            "Hello waves I'm nervous",
            WidgetText.plain("Hello <action>waves</action> <think>I'm nervous</think>"),
        )
    }

    @Test
    fun `a message that is only narration stays readable`() {
        assertEquals("The night was long.", WidgetText.plain("<narrator>The night was long.</narrator>"))
    }

    @Test
    fun `whitespace collapses and null is empty`() {
        assertEquals("a b", WidgetText.plain("  a \n\n  b  "))
        assertEquals("", WidgetText.plain(null))
    }
}
