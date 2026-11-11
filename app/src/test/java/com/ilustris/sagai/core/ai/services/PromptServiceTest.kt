package com.ilustris.sagai.core.ai.services

import com.ilustris.sagai.core.ai.model.PromptBlueprint
import com.ilustris.sagai.core.services.RemoteConfigService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PromptServiceTest {
    private lateinit var promptService: PromptService
    private val remoteConfigService: RemoteConfigService = mockk()

    @Before
    fun setup() {
        promptService = PromptServiceImpl(remoteConfigService)
    }

    @Test
    fun `buildSplitBlueprint with template should replace placeholders`() =
        runBlocking {
            val blueprint =
                PromptBlueprint(
                    title = "Test Blueprint",
                    template = "Hello {name}!",
                )
            val variables = mapOf("name" to "World")
            coEvery {
                remoteConfigService.getJson<PromptBlueprint>(
                    "test_key",
                    any(),
                )
            } returns blueprint

            val result = promptService.buildSplitBlueprint("test_key", variables)

            assertTrue(result.processedTemplate.contains("Hello World!"))
        }

    @Test
    fun `buildSplitBlueprint with null template should generate TASK CONTEXT`() =
        runBlocking {
            val blueprint =
                PromptBlueprint(
                    title = "Test Blueprint",
                    template = null,
                )
            val variables = mapOf("name" to "World", "age" to "30")
            coEvery {
                remoteConfigService.getJson<PromptBlueprint>(
                    "test_key",
                    any(),
                )
            } returns blueprint

            val result = promptService.buildSplitBlueprint("test_key", variables)

            assertTrue(result.processedTemplate.contains("# TASK CONTEXT"))
            assertTrue(result.processedTemplate.contains("name: World"))
            assertTrue(result.processedTemplate.contains("age: 30"))
        }

    @Test
    fun `buildSplitBlueprint with Any variables and blank template should generate TASK CONTEXT`() =
        runBlocking {
            data class TestArgs(
                val name: String,
                val age: Int,
            )

            val blueprint =
                PromptBlueprint(
                    title = "Test Blueprint",
                    template = "",
                )
            val args = TestArgs("AI", 5)
            coEvery {
                remoteConfigService.getJson<PromptBlueprint>(
                    "test_key",
                    any(),
                )
            } returns blueprint

            val result = promptService.buildSplitBlueprint("test_key", args)

            assertTrue(result.processedTemplate.contains("# TASK CONTEXT"))
            assertTrue(result.processedTemplate.contains("name: AI"))
            assertTrue(result.processedTemplate.contains("age: 5"))
        }

    @Test
    fun `examplesPerCall renders only a draw of the examples and changes between builds`() =
        runBlocking {
            val examples = (1..20).map { mapOf("look" to "look-number-$it-end") }
            val blueprint = PromptBlueprint(title = "Test Blueprint", examples = examples, examplesPerCall = 4)
            coEvery { remoteConfigService.getJson<PromptBlueprint>("test_key", any()) } returns blueprint

            val renders = (1..8).map { promptService.buildSplitBlueprint("test_key").renderInstructions().toString() }

            renders.forEach { rendered ->
                assertEquals(4, examples.count { rendered.contains(it.getValue("look")) })
            }
            assertTrue(renders.toSet().size > 1)
        }

    @Test
    fun `without examplesPerCall every example is rendered`() =
        runBlocking {
            val examples = (1..6).map { mapOf("look" to "look-number-$it-end") }
            val blueprint = PromptBlueprint(title = "Test Blueprint", examples = examples)
            coEvery { remoteConfigService.getJson<PromptBlueprint>("test_key", any()) } returns blueprint

            val rendered = promptService.buildSplitBlueprint("test_key").renderInstructions().toString()

            assertEquals(6, examples.count { rendered.contains(it.getValue("look")) })
        }
}
