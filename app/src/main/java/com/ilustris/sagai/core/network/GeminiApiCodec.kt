package com.ilustris.sagai.core.network

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.ilustris.sagai.core.ai.model.GeminiCandidate
import com.ilustris.sagai.core.ai.model.GeminiContent
import com.ilustris.sagai.core.ai.model.GeminiCountTokensResponse
import com.ilustris.sagai.core.ai.model.GeminiError
import com.ilustris.sagai.core.ai.model.GeminiErrorDetail
import com.ilustris.sagai.core.ai.model.GeminiGenerationConfig
import com.ilustris.sagai.core.ai.model.GeminiInlineData
import com.ilustris.sagai.core.ai.model.GeminiPart
import com.ilustris.sagai.core.ai.model.GeminiPromptFeedback
import com.ilustris.sagai.core.ai.model.GeminiQuotaViolation
import com.ilustris.sagai.core.ai.model.GeminiRequest
import com.ilustris.sagai.core.ai.model.GeminiResponse
import com.ilustris.sagai.core.ai.model.GeminiResponseContent
import com.ilustris.sagai.core.ai.model.GeminiResponsePart
import com.ilustris.sagai.core.ai.model.GeminiUsageMetadata
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Manual Gemini REST JSON encode/decode — avoids Gson [TypeToken] / [ParameterizedType] under R8.
 */
object GeminiApiCodec {
    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

    fun encodeRequest(request: GeminiRequest): RequestBody =
        encodeRequestJson(request).toRequestBody(JSON_MEDIA)

    fun encodeRequestJson(request: GeminiRequest): String {
        val root = JsonObject()
        root.add("contents", encodeContents(request.contents))
        root.add("generationConfig", encodeGenerationConfig(request.generationConfig))
        request.systemInstruction?.let {
            root.add("system_instruction", encodeContent(it))
        }
        return root.toString()
    }

    /**
     * countTokens accepts either top-level [contents] or a nested [generateContentRequest].
     * System instruction and generation config must live inside that wrapper — not at the root.
     *
     * @see <a href="https://ai.google.dev/api/tokens">Gemini countTokens API</a>
     */
    fun encodeCountTokensRequest(
        model: String,
        request: GeminiRequest,
    ): RequestBody = encodeCountTokensRequestJson(model, request).toRequestBody(JSON_MEDIA)

    fun encodeCountTokensRequestJson(
        model: String,
        request: GeminiRequest,
    ): String {
        val normalizedModel = if (model.startsWith("models/")) model else "models/$model"
        val generateContentRequest = JsonObject()
        generateContentRequest.addProperty("model", normalizedModel)
        generateContentRequest.add("contents", encodeContents(request.contents))
        generateContentRequest.add(
            "generationConfig",
            encodeGenerationConfig(request.generationConfig),
        )
        request.systemInstruction?.let {
            generateContentRequest.add("system_instruction", encodeContent(it))
        }
        return JsonObject()
            .also { root ->
                root.add("generateContentRequest", generateContentRequest)
            }.toString()
    }

    /**
     * Maps each model name to the context window the API reports for it.
     *
     * The ceiling comes from the API rather than a constant so it stays right as tiers move
     * between model families — Gemma reports 262144, Gemini 1048576, and a hardcoded number would
     * be wrong for at least one of them the moment `model_configs` straddles both.
     */
    fun decodeModelTokenLimits(json: String): Map<String, Int> {
        if (json.isBlank()) return emptyMap()
        val models = JsonParser.parseString(json).asJsonObject.optJsonArray("models")
            ?: return emptyMap()
        return models.mapNotNull { element ->
            val obj = element as? JsonObject ?: return@mapNotNull null
            val name = obj.optString("name")?.removePrefix("models/") ?: return@mapNotNull null
            val limit = obj.get("inputTokenLimit")?.takeIf { !it.isJsonNull }?.asInt
                ?: return@mapNotNull null
            name to limit
        }.toMap()
    }

    fun decodeErrorResponse(json: String): com.ilustris.sagai.core.ai.model.GeminiErrorResponse {
        if (json.isBlank()) return com.ilustris.sagai.core.ai.model.GeminiErrorResponse(error = null)
        val root = JsonParser.parseString(json).asJsonObject
        return com.ilustris.sagai.core.ai.model.GeminiErrorResponse(
            error = root.optJsonObject("error")?.let(::decodeError),
        )
    }

    fun decodeResponse(json: String): GeminiResponse {
        if (json.isBlank()) return GeminiResponse(candidates = null, usageMetadata = null, error = null)
        val root = JsonParser.parseString(json).asJsonObject
        return GeminiResponse(
            candidates = root.optJsonArray("candidates")?.let(::decodeCandidates),
            usageMetadata = root.optJsonObject("usageMetadata")?.let(::decodeUsageMetadata),
            error = root.optJsonObject("error")?.let(::decodeError),
            promptFeedback = root.optJsonObject("promptFeedback")?.let(::decodePromptFeedback),
        )
    }

    private fun decodePromptFeedback(obj: JsonObject): GeminiPromptFeedback =
        GeminiPromptFeedback(
            blockReason = obj.optString("blockReason"),
        )

    fun decodeCountTokensResponse(json: String): GeminiCountTokensResponse {
        if (json.isBlank()) {
            return GeminiCountTokensResponse(
                totalTokens = null,
                totalBillableCharacters = null,
            )
        }
        val root = JsonParser.parseString(json).asJsonObject
        return GeminiCountTokensResponse(
            totalTokens = root.optInt("totalTokens"),
            totalBillableCharacters = root.optInt("totalBillableCharacters"),
        )
    }

    private fun encodeContents(contents: List<GeminiContent>): JsonArray =
        JsonArray().also { array ->
            contents.forEach { content ->
                array.add(encodeContent(content))
            }
        }

    private fun encodeContent(content: GeminiContent): JsonObject =
        JsonObject().also { obj ->
            obj.addProperty("role", content.role)
            obj.add("parts", encodeParts(content.parts))
        }

    private fun encodeParts(parts: List<GeminiPart>): JsonArray =
        JsonArray().also { array ->
            parts.forEach { part ->
                array.add(
                    JsonObject().also { obj ->
                        part.text?.let { obj.addProperty("text", it) }
                        part.inlineData?.let { inline ->
                            obj.add(
                                "inline_data",
                                JsonObject().also { inlineObj ->
                                    inline.mimeType?.let { inlineObj.addProperty("mimeType", it) }
                                    inline.data?.let { inlineObj.addProperty("data", it) }
                                },
                            )
                        }
                    },
                )
            }
        }

    private fun encodeGenerationConfig(config: GeminiGenerationConfig): JsonObject =
        JsonObject().also { obj ->
            config.responseModalities?.let { modalities ->
                obj.add(
                    "response_modalities",
                    JsonArray().also { array -> modalities.forEach(array::add) },
                )
            }
            config.temperature?.let { obj.addProperty("temperature", it) }
            config.responseMimeType?.let { obj.addProperty("response_mime_type", it) }
            // Was silently dropped here: the builder assembled a thinkingConfig and the encoder
            // never wrote it, so no request ever carried a thinking level or asked for a summary.
            // Every model simply thought at its own default and returned no thought parts, which
            // read from the outside as "the API does not send reasoning".
            config.thinkingConfig?.let { thinking ->
                obj.add(
                    "thinkingConfig",
                    JsonObject().also { thinkingObj ->
                        thinking.includeThoughts?.let {
                            thinkingObj.addProperty("includeThoughts", it)
                        }
                        thinking.thinkingLevel?.let {
                            thinkingObj.addProperty("thinkingLevel", it)
                        }
                    },
                )
            }
            config.speechConfig?.let { speech ->
                obj.add(
                    "speech_config",
                    JsonObject().also { speechObj ->
                        speechObj.add(
                            "voice_config",
                            JsonObject().also { voiceObj ->
                                voiceObj.add(
                                    "prebuilt_voice_config",
                                    JsonObject().also { prebuilt ->
                                        prebuilt.addProperty(
                                            "voice_name",
                                            speech.voiceConfig.prebuiltVoiceConfig.voiceName,
                                        )
                                    },
                                )
                            },
                        )
                    },
                )
            }
        }

    private fun decodeCandidates(array: JsonArray): List<GeminiCandidate> =
        array.mapNotNull { element ->
            val obj = element.asJsonObject
            GeminiCandidate(
                content = obj.optJsonObject("content")?.let(::decodeResponseContent),
                finishReason = obj.optString("finishReason"),
            )
        }

    private fun decodeResponseContent(obj: JsonObject): GeminiResponseContent =
        GeminiResponseContent(
            parts = obj.optJsonArray("parts")?.let(::decodeResponseParts),
            role = obj.optString("role"),
        )

    private fun decodeResponseParts(array: JsonArray): List<GeminiResponsePart> =
        array.mapNotNull { element ->
            val obj = element.asJsonObject
            GeminiResponsePart(
                text = obj.optString("text"),
                inlineData = decodeInlineData(obj),
                thought = obj.optBoolean("thought"),
            )
        }

    private fun JsonObject.optBoolean(key: String): Boolean? = get(key)?.takeUnless { it.isJsonNull || !it.isJsonPrimitive }?.asBoolean

    private fun decodeInlineData(obj: JsonObject): GeminiInlineData? {
        val inline = obj.optJsonObject("inlineData") ?: obj.optJsonObject("inline_data") ?: return null
        return GeminiInlineData(
            mimeType = inline.optString("mimeType"),
            data = inline.optString("data"),
        )
    }

    private fun decodeUsageMetadata(obj: JsonObject): GeminiUsageMetadata =
        GeminiUsageMetadata(
            promptTokenCount = obj.optInt("promptTokenCount"),
            candidatesTokenCount = obj.optInt("candidatesTokenCount"),
            totalTokenCount = obj.optInt("totalTokenCount"),
            thoughtsTokenCount = obj.optInt("thoughtsTokenCount"),
        )

    private fun decodeError(obj: JsonObject): GeminiError =
        GeminiError(
            code = obj.optInt("code"),
            message = obj.optString("message"),
            status = obj.optString("status"),
            details = obj.optJsonArray("details")?.mapNotNull { detailEl ->
                val detail = detailEl.asJsonObject
                GeminiErrorDetail(
                    type = detail.optString("@type"),
                    retryDelay = detail.optString("retryDelay"),
                    violations =
                        detail.optJsonArray("violations")?.mapNotNull { violationEl ->
                            val violation = violationEl.asJsonObject
                            GeminiQuotaViolation(
                                quotaMetric = violation.optString("quotaMetric"),
                                quotaId = violation.optString("quotaId"),
                                quotaDimensions = decodeStringMap(violation.optJsonObject("quotaDimensions")),
                                quotaValue = violation.optString("quotaValue"),
                            )
                        },
                )
            },
        )

    private fun decodeStringMap(obj: JsonObject?): Map<String, String>? {
        if (obj == null || obj.size() == 0) return null
        return obj.entrySet().associate { (key, value) -> key to value.asString }
    }

    private fun JsonObject.optJsonObject(key: String): JsonObject? =
        get(key)?.takeUnless { it.isJsonNull }?.asJsonObject

    private fun JsonObject.optJsonArray(key: String): JsonArray? =
        get(key)?.takeUnless { it.isJsonNull }?.asJsonArray

    private fun JsonObject.optString(key: String): String? =
        get(key)?.takeUnless { it.isJsonNull || !it.isJsonPrimitive }?.asString

    private fun JsonObject.optInt(key: String): Int? =
        get(key)?.takeUnless { it.isJsonNull || !it.isJsonPrimitive }?.asInt
}
