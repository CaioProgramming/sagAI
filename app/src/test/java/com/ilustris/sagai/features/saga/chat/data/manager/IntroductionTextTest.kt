package com.ilustris.sagai.features.saga.chat.data.manager

import org.junit.Assert.assertEquals
import org.junit.Test

class IntroductionTextTest {
    @Test
    fun `uses the model message when present`() {
        assertEquals("hook", resolveIntroductionText("hook", "saved"))
    }

    @Test
    fun `falls back to the saved introduction when the message is null`() {
        assertEquals("saved", resolveIntroductionText(null, "saved"))
    }

    @Test
    fun `falls back to the saved introduction when the message is empty or blank`() {
        assertEquals("saved", resolveIntroductionText("", "saved"))
        assertEquals("saved", resolveIntroductionText("   ", "saved"))
    }
}
