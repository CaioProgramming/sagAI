package com.ilustris.sagai.core.ai

import android.graphics.Bitmap
import android.util.Base64
import com.ilustris.sagai.core.ai.model.GeminiContent
import com.ilustris.sagai.core.ai.model.GeminiGenerationConfig
import com.ilustris.sagai.core.ai.model.GeminiInlineData
import com.ilustris.sagai.core.ai.model.GeminiPart
import com.ilustris.sagai.core.ai.model.GeminiRequest
import com.ilustris.sagai.core.ai.model.GeminiThinkingConfig
import com.ilustris.sagai.core.ai.model.ImageReference
import java.io.ByteArrayOutputStream

/**
 * Result of assembling a [GeminiRequest] for generate/streaming call sites.
 */
data class GeminiRequestAssembly(
    val request: GeminiRequest,
    val contentParts: List<GeminiPart>,
    val fullPromptText: String,
    val systemInstruction: String,
    val taskPrompt: String,
)

/**
 * DSL builder for Gemini API requests shared by [GemmaClient.generate] and [GemmaClient.generateStreaming].
 */
class GeminiRequestBuilder internal constructor() {
    private var taskPrompt: String = ""
    private var systemInstruction: String = ""
    private var references: List<ImageReference> = emptyList()
    private var requirement: ModelRequirement = ModelRequirement.MEDIUM
    private var temperatureRandomness: Float = 0.5f
    private var includeSystemInFullPrompt: Boolean = true
    private var thinkingLevel: String? = null

    fun task(value: String) {
        taskPrompt = value
    }

    fun system(value: String) {
        systemInstruction = value
    }

    fun references(values: List<ImageReference?>) {
        references = values.filterNotNull()
    }

    fun generation(
        requirement: ModelRequirement,
        temperatureRandomness: Float = 0.5f,
    ) {
        this.requirement = requirement
        this.temperatureRandomness = temperatureRandomness
    }

    /**
     * The Gemini `thinkingLevel` for this request, already resolved to a value the API accepts.
     * Null omits `thinkingConfig` entirely — for a tier with thinking switched off, or a model
     * that doesn't support it.
     */
    fun thinking(level: String?) {
        thinkingLevel = level
    }

    /** Whether [buildFullPromptText] should embed the system instruction (token diagnostics). */
    fun fullPrompt(includeSystemInstruction: Boolean = true) {
        includeSystemInFullPrompt = includeSystemInstruction
    }

    fun build(): GeminiRequestAssembly {
        require(taskPrompt.isNotBlank()) { "Gemini request requires a task prompt" }

        val contentParts = buildGeminiContentParts(taskPrompt, references)
        val fullPromptText =
            buildFullPromptText(
                taskPrompt = taskPrompt,
                referenceDescriptions = references.map { it.description },
                systemInstruction = systemInstruction.takeIf { includeSystemInFullPrompt },
            )

        val request =
            GeminiRequest(
                contents = listOf(GeminiContent(parts = contentParts)),
                systemInstruction =
                    GeminiContent(
                        parts = listOf(GeminiPart(text = systemInstruction)),
                    ),
                generationConfig =
                    GeminiGenerationConfig(
                        temperature =
                            resolveGenerationTemperature(
                                requirement,
                                temperatureRandomness,
                            ),
                        thinkingConfig =
                            thinkingLevel?.let { level ->
                                GeminiThinkingConfig(
                                    includeThoughts = true,
                                    thinkingLevel = level,
                                )
                            },
                    ),
            )

        return GeminiRequestAssembly(
            request = request,
            contentParts = contentParts,
            fullPromptText = fullPromptText,
            systemInstruction = systemInstruction,
            taskPrompt = taskPrompt,
        )
    }
}

fun geminiRequest(block: GeminiRequestBuilder.() -> Unit): GeminiRequestAssembly = GeminiRequestBuilder().apply(block).build()

fun buildGeminiContentParts(
    taskPrompt: String,
    references: List<ImageReference> = emptyList(),
): List<GeminiPart> =
    buildList {
        add(GeminiPart(text = taskPrompt))
        references.forEach { reference ->
            add(
                GeminiPart(
                    inlineData =
                        GeminiInlineData(
                            mimeType = "image/jpeg",
                            data = reference.bitmap.toGeminiBase64(),
                        ),
                ),
            )
            add(GeminiPart(text = reference.description))
        }
    }

internal fun resolveGenerationTemperature(
    requirement: ModelRequirement,
    temperatureRandomness: Float,
): Float =
    if (requirement == ModelRequirement.MINIMAL || requirement == ModelRequirement.LOW) {
        0.1f
    } else {
        temperatureRandomness
    }

internal fun Bitmap.toGeminiBase64(): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.NO_WRAP)
}
