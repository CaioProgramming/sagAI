package com.ilustris.sagai.core.network

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.ilustris.sagai.core.ai.model.GeminiRequest
import com.ilustris.sagai.core.ai.model.OpenRouterChoice
import com.ilustris.sagai.core.ai.model.OpenRouterError
import com.ilustris.sagai.core.ai.model.OpenRouterMessage
import com.ilustris.sagai.core.ai.model.OpenRouterReasoningConfig
import com.ilustris.sagai.core.ai.model.OpenRouterRequest
import com.ilustris.sagai.core.ai.model.OpenRouterResponse
import com.ilustris.sagai.core.ai.model.OpenRouterUsage
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Manual OpenRouter REST JSON encode/decode — avoids Gson [TypeToken] / [ParameterizedType] under R8.
 * Converts between Gemini format and OpenRouter format for unified handling.
 */
object OpenRouterApiCodec {
    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

    /**
     * Converts a GeminiRequest to OpenRouterRequest format.
     * Extracts text from all parts and combines them into a single message content.
     */
    fun geminiToOpenRouterRequest(
        geminiRequest: GeminiRequest,
        temperature: Float,
    ): OpenRouterRequest {
        // Extract all text parts from the GeminiRequest
        val textParts = mutableListOf<String>()
        geminiRequest.contents.forEach { content ->
            content.parts.forEach { part ->
                part.text?.let { textParts.add(it) }
            }
        }
        val combinedContent = textParts.joinToString("\n\n")

        return OpenRouterRequest(
            model = "openrouter/free",
            messages =
                listOf(
                    OpenRouterMessage(
                        role = "user",
                        content = combinedContent,
                    ),
                ),
            temperature = temperature,
            reasoning = OpenRouterReasoningConfig(enabled = true),
        )
    }

    fun encodeRequest(request: OpenRouterRequest): RequestBody = encodeRequestJson(request).toRequestBody(JSON_MEDIA)

    fun encodeRequestJson(request: OpenRouterRequest): String {
        val root = JsonObject()
        root.addProperty("model", request.model)
        root.add("messages", encodeMessages(request.messages))
        request.temperature?.let { root.addProperty("temperature", it) }
        request.reasoning?.let { reasoning ->
            root.add(
                "reasoning",
                JsonObject().also { obj ->
                    obj.addProperty("enabled", reasoning.enabled)
                },
            )
        }
        return root.toString()
    }

    fun decodeErrorResponse(json: String): OpenRouterError {
        if (json.isBlank()) return OpenRouterError(code = null, message = null, status = null)
        return try {
            val root = JsonParser.parseString(json).asJsonObject
            val error = root.optJsonObject("error")
            if (error != null) {
                decodeError(error)
            } else {
                OpenRouterError(code = null, message = null, status = null)
            }
        } catch (e: Exception) {
            OpenRouterError(code = null, message = null, status = null)
        }
    }

    fun decodeResponse(json: String): OpenRouterResponse {
        if (json.isBlank()) {
            return OpenRouterResponse(
                model = null,
                choices = null,
                usage = null,
                error = null,
            )
        }
        val root = JsonParser.parseString(json).asJsonObject
        return OpenRouterResponse(
            model = root.optString("model"),
            choices = root.optJsonArray("choices")?.let(::decodeChoices),
            usage = root.optJsonObject("usage")?.let(::decodeUsage),
            error = root.optJsonObject("error")?.let(::decodeError),
        )
    }

    private fun encodeMessages(messages: List<OpenRouterMessage>): JsonArray =
        JsonArray().also { array ->
            messages.forEach { message ->
                array.add(
                    JsonObject().also { obj ->
                        obj.addProperty("role", message.role)
                        obj.addProperty("content", message.content)
                    },
                )
            }
        }

    private fun decodeChoices(array: JsonArray): List<OpenRouterChoice> =
        array.mapNotNull { element ->
            val obj = element.asJsonObject
            OpenRouterChoice(
                message =
                    obj.optJsonObject("message")?.let {
                        OpenRouterMessage(
                            role = it.optString("role") ?: "assistant",
                            content = it.optString("content") ?: "",
                        )
                    },
                finishReason = obj.optString("finish_reason"),
                index = obj.optInt("index"),
            )
        }

    private fun decodeUsage(obj: JsonObject): OpenRouterUsage =
        OpenRouterUsage(
            promptTokens = obj.optInt("prompt_tokens"),
            completionTokens = obj.optInt("completion_tokens"),
            totalTokens = obj.optInt("total_tokens"),
        )

    private fun decodeError(obj: JsonObject): OpenRouterError =
        OpenRouterError(
            code = obj.optInt("code"),
            message = obj.optString("message"),
            status = obj.optString("status"),
        )

    private fun JsonObject.optJsonObject(key: String): JsonObject? = get(key)?.takeUnless { it.isJsonNull }?.asJsonObject

    private fun JsonObject.optJsonArray(key: String): JsonArray? = get(key)?.takeUnless { it.isJsonNull }?.asJsonArray

    private fun JsonObject.optString(key: String): String? = get(key)?.takeUnless { it.isJsonNull || !it.isJsonPrimitive }?.asString

    private fun JsonObject.optInt(key: String): Int? = get(key)?.takeUnless { it.isJsonNull || !it.isJsonPrimitive }?.asInt
}
