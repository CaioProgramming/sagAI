package com.ilustris.sagai.features.onboarding.data

import com.ilustris.sagai.features.onboarding.data.model.OnboardingContent
import com.ilustris.sagai.features.onboarding.data.model.OnboardingPage
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingStateMapperTest {
    private val mapper = OnboardingStateMapper(mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true))

    @Test
    fun `key setup without config still has the page holding the key field`() {
        val state = runBlocking { mapper.buildOnboardingState(OnboardingType.API_KEY_SETUP, OnboardingContent()) }

        assertEquals(1, state.pages.size)
    }

    @Test
    fun `key setup with config keeps the configured pages`() {
        val content = OnboardingContent(pages = listOf(OnboardingPage("a"), OnboardingPage("b")))

        val state = runBlocking { mapper.buildOnboardingState(OnboardingType.API_KEY_SETUP, content) }

        assertEquals(2, state.pages.size)
    }

    @Test
    fun `other types without config get no invented page`() {
        val state = runBlocking { mapper.buildOnboardingState(OnboardingType.APP_INTRO, OnboardingContent()) }

        assertTrue(state.pages.isEmpty())
    }
}
