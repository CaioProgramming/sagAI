package com.ilustris.sagai.core.services

import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

class RemoteConfigServiceTest {
    private val firebase = mockk<FirebaseRemoteConfig>()
    private val template = mapOf("key" to mockk<FirebaseRemoteConfigValue>())

    @Before
    fun setUp() {
        mockkStatic(FirebaseRemoteConfig::class)
        every { FirebaseRemoteConfig.getInstance() } returns firebase
        every { firebase.ensureInitialized() } returns Tasks.forResult(mockk())
    }

    @After
    fun tearDown() = unmockkStatic(FirebaseRemoteConfig::class)

    @Test
    fun `template on disk answers true without fetching`() {
        every { firebase.all } returns template

        assertTrue(runBlocking { RemoteConfigService().awaitFirstFetch() })
        verify(exactly = 0) { firebase.fetchAndActivate() }
    }

    @Test
    fun `fetch that brings a template answers true`() {
        every { firebase.all } returnsMany listOf(emptyMap(), template)
        every { firebase.fetchAndActivate() } returns Tasks.forResult(true)

        assertTrue(runBlocking { RemoteConfigService().awaitFirstFetch() })
    }

    @Test
    fun `failed fetch answers false`() {
        every { firebase.all } returns emptyMap()
        every { firebase.fetchAndActivate() } returns Tasks.forException(IllegalStateException("no network"))

        assertFalse(runBlocking { RemoteConfigService().awaitFirstFetch() })
    }

    @Test
    fun `fetch that leaves the template empty answers false`() {
        every { firebase.all } returns emptyMap()
        every { firebase.fetchAndActivate() } returns Tasks.forResult(false)

        assertFalse(runBlocking { RemoteConfigService().awaitFirstFetch() })
    }

    @Test
    fun `fetch that does not finish in time answers false`() {
        every { firebase.all } returns emptyMap()
        every { firebase.fetchAndActivate() } returns TaskCompletionSource<Boolean>().task

        assertFalse(runBlocking { RemoteConfigService().awaitFirstFetch(timeout = 50.milliseconds) })
    }

    @Test
    fun `retry after a failure fetches again`() {
        every { firebase.all } returnsMany listOf(emptyMap(), emptyMap(), template)
        every { firebase.fetchAndActivate() } returnsMany
            listOf(Tasks.forException(IllegalStateException("no network")), Tasks.forResult(true))
        val service = RemoteConfigService()

        assertFalse(runBlocking { service.awaitFirstFetch() })
        assertTrue(runBlocking { service.awaitFirstFetch() })
        verify(exactly = 2) { firebase.fetchAndActivate() }
    }
}
