package com.ilustris.sagai.features.imagegeneration

import android.graphics.Bitmap
import com.ilustris.sagai.core.ai.ImagenClient
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.debug.DebugImageFallbackService
import com.ilustris.sagai.core.ai.model.GeneratedContent
import com.ilustris.sagai.core.ai.model.ImageType
import com.ilustris.sagai.core.globalshell.GlobalShellService
import com.ilustris.sagai.features.imagegeneration.model.ImageGenerationRequest
import com.ilustris.sagai.features.imagegeneration.model.ImageTarget
import com.ilustris.sagai.features.imagegeneration.model.ImageTargetState
import com.ilustris.sagai.features.newsaga.data.model.Genre
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList

class ImageGenerationServiceTargetStateTest {
    private val target = ImageTarget.Chapter(1)

    private fun service(): ImageGenerationService {
        val client = mockk<ImagenClient>()
        coEvery { client.generateIntegratedImageStream(any(), any(), any(), any(), any()) } returns
            flowOf(StreamingState.Success(GeneratedContent(mockk<Bitmap>(relaxed = true), "ok")))
        val fallback = mockk<DebugImageFallbackService>(relaxed = true)
        every { fallback.isAwaitingUser } returns MutableStateFlow(false)
        return ImageGenerationService(client, fallback, mockk<GlobalShellService>(relaxed = true))
    }

    private fun request() =
        ImageGenerationRequest(
            genre = Genre.FANTASY,
            context = "ctx",
            imageType = ImageType.COVER,
            showReveal = false,
            target = target,
        )

    @Test
    fun `a generated image reveals and then settles back to idle`() =
        runBlocking {
            val service = service()
            val seen = CopyOnWriteArrayList<ImageTargetState>()
            val collector = launch(Dispatchers.Default) { service.stateOf(target).collect { seen += it } }

            service.track(target) { service.enqueue(request()) { "saved" } }.getOrThrow()

            withTimeout(5_000) { while (seen.lastOrNull() != ImageTargetState.Idle || ImageTargetState.Revealing !in seen) delay(50) }
            collector.cancel()
            assertTrue(ImageTargetState.Revealing in seen)
            assertEquals(ImageTargetState.Idle, seen.last())
        }

    @Test
    fun `a failure while saving leaves the target failed so the screen can offer a retry`() =
        runBlocking {
            val service = service()

            val result = service.enqueue<String>(request()) { error("disk full") }

            assertTrue(result.isFailure)
            assertEquals(ImageTargetState.Failed("disk full"), service.stateOf(target).first())
        }

    @Test
    fun `a failure while preparing the prompt leaves the target failed and rethrows`() =
        runBlocking {
            val service = service()

            val thrown = runCatching { service.track<Unit>(target) { error("no artwork") } }.exceptionOrNull()

            assertEquals("no artwork", thrown?.message)
            assertEquals(ImageTargetState.Failed("no artwork"), service.stateOf(target).first())
        }

    @Test
    fun `work that never reaches the queue does not leave the target stuck`() =
        runBlocking {
            val service = service()

            service.track(target) { "nothing to generate" }

            assertEquals(ImageTargetState.Idle, service.stateOf(target).first())
        }
}
