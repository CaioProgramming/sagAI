package com.ilustris.sagai.core.ai

import android.util.Log
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.ImagePart
import com.google.ai.client.generativeai.type.TextPart
import com.google.ai.client.generativeai.type.generationConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ilustris.sagai.core.ai.model.ImageReference
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.utils.formatToJsonArray
import com.ilustris.sagai.core.utils.sanitizeAndExtractJsonString
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.core.utils.toJsonMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class GemmaClient
    @Inject
    constructor(
        private val remoteConfigService: RemoteConfigService,
    ) : AIClient() {
        @PublishedApi
        internal val requestMutex = Mutex()

        @PublishedApi
        @Volatile
        internal var retryDelay: Int? = null

        @Volatile
        var lastTokenCount: Int = 0

        companion object {
            const val SUMMARIZATION_MODEL_FLAG = "summarizationModel"
            const val CORE_FLAG = "SAGA_CORE"
            const val INPUT_TOKEN_LIMIT = 15000
            const val REACTIVE_DELAY_THRESHOLD = 0.7f
        }

        suspend fun modelName() =
            remoteConfigService.getString(SUMMARIZATION_MODEL_FLAG)?.let {
                it.ifEmpty {
                    error("Couldn't fetch gemma Model")
                }
            } ?: error("Couldn't get Flag value")

        suspend fun coreKey() =
            remoteConfigService.getString(CORE_FLAG)?.let {
                it.ifEmpty {
                    error("Couldn't fetch gemma Model")
                }
            } ?: error("Couldn't get Flag value")

        suspend fun apiConfig(useCore: Boolean): String =
            if (useCore) {
                coreKey()
            } else {
                remoteConfigService.getString(KEY_FLAG)?.ifEmpty {
                    error("Couldn't fetch firebase key")
                } ?: error("Flag Value unavailable.")
            }

        suspend inline fun <reified T> generate(
            prompt: String,
            references: List<ImageReference?> = emptyList(),
            temperatureRandomness: Float = .5f,
            requireTranslation: Boolean = true,
            describeOutput: Boolean = true,
            filterOutputFields: List<String> = emptyList(),
            useCore: Boolean = false,
        ): T? =
            withContext(Dispatchers.IO) {
                if (lastTokenCount > (INPUT_TOKEN_LIMIT * REACTIVE_DELAY_THRESHOLD) && retryDelay == null) {
                    Log.w(javaClass.simpleName, "Applying reactive delay due to high token count in last request.")
                    retryDelay = 5
                    delay((retryDelay ?: 5).seconds)
                }
                val model = modelName()
                if (useCore.not()) {
                    retryDelay?.let {
                        Log.e(
                            javaClass.simpleName,
                            "generate: Trying delay $retryDelay seconds to avoid rate limit.",
                        )
                        delay(it.seconds)
                    }
                } else {
                    Log.i(javaClass.simpleName, "generate: Core calls don't require delay.")
                }

                requestMutex.withLock {
                    try {
                        val client =
                            com.google.ai.client.generativeai.GenerativeModel(
                                modelName = model,
                                apiKey = apiConfig(useCore),
                                generationConfig {
                                    temperature = temperatureRandomness
                                },
                            )

                        val fullPrompt =
                            buildString {
                                appendLine(prompt)
                                if (requireTranslation) {
                                    appendLine(modelLanguage())
                                }
                                appendLine("Your OUTPUT is a ${T::class.java.simpleName}")
                                if (T::class != String::class && describeOutput) {
                                    appendLine("Follow this structure:")
                                    appendLine(
                                        toJsonMap(
                                            T::class.java,
                                            filteredFields = filterOutputFields,
                                        ),
                                    )
                                }
                            }

                        Log.i(
                            this@GemmaClient::class.java.simpleName,
                            "Requesting $model\nPrompt with ${
                                fullPrompt.length +
                                    references.filterNotNull().sumOf {
                                        it.description.length
                                    }
                            } chars.",
                        )

                        val contentParts =
                            buildList {
                                if (prompt.isNotEmpty()) {
                                    add(TextPart(fullPrompt))
                                }
                                references.filterNotNull().forEach { reference ->
                                    add(ImagePart(reference.bitmap))
                                    add(TextPart(reference.description))
                                }
                            }

                        val inputContent =
                            Content(
                                role = "user",
                                contentParts,
                            )

                        Log.d(
                            javaClass.simpleName,
                            "Input content has ${contentParts.size} parts: ${
                                contentParts.map { it.javaClass.simpleName }
                            }",
                        )

                        val content = client.generateContent(inputContent)
                        lastTokenCount = content.usageMetadata?.promptTokenCount ?: 0
                        retryDelay = null
                        if (lastTokenCount < (INPUT_TOKEN_LIMIT * REACTIVE_DELAY_THRESHOLD)) {
                            lastTokenCount = 0
                        }

                        val response = content.text

                        Log.d(
                            javaClass.simpleName,
                            "Input content: ${
                                inputContent.toJsonFormatExcludingFields(AI_EXCLUDED_FIELDS)
                            }",
                        )

                        Log.i(
                            javaClass.simpleName,
                            "Generated content: ${
                                content.toJsonFormatExcludingFields(
                                    AI_EXCLUDED_FIELDS,
                                )
                            }",
                        )

                        val promptDescription =
                            buildString {
                                appendLine("Full Prompt { ")
                                if (fullPrompt.isNotEmpty()) {
                                    appendLine("Main prompt { ")
                                    appendLine(fullPrompt)
                                    appendLine(" }")
                                }
                                if (references.isNotEmpty()) {
                                    appendLine("References:")
                                    appendLine(references.filterNotNull().formatToJsonArray())
                                }
                                appendLine(" }")
                            }

                        Log.d(javaClass.simpleName, promptDescription)

                        if (T::class == String::class) {
                            Log.i(javaClass.simpleName, "Prompt request result:\n$response")
                            return@withLock response as T
                        }

                        val cleanedJsonString = response.sanitizeAndExtractJsonString()
                        val typeToken = object : TypeToken<T>() {}
                        Gson().fromJson(cleanedJsonString, typeToken.type)
                    } catch (e: Exception) {
                        retryDelay = retryDelay?.let {
                            if (it > 30) {
                                it / 2
                            } else {
                                it + it
                            }
                        } ?: run {
                            2
                        }
                        Log.e(
                            this@GemmaClient::class.java.simpleName,
                            "Error in Generation($model): ${e.javaClass.simpleName} - ${e.message}",
                            e,
                        )
                        Log.e(
                            javaClass.simpleName,
                            "failed to generate content for prompt:\n$prompt\n",
                        )
                        Log.w(
                            javaClass.simpleName,
                            "$retryDelay seconds delay will be applied on the next request.",
                        )
                        null
                    }
                }
            }
    }

const val KEY_FLAG = "FIREBASE_KEY"
