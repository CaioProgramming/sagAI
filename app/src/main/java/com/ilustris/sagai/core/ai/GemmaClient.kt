package com.ilustris.sagai.core.ai

import android.util.Log
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.ImagePart
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.TextPart
import com.google.ai.client.generativeai.type.generationConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ilustris.sagai.core.ai.models.ImageReference
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
import kotlin.time.Duration
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
        internal var retryDelay: Duration? = null

        companion object {
            const val SUMMARIZATION_MODEL_FLAG = "summarizationModel"
        }

        suspend fun modelName() =
            remoteConfigService.getString(SUMMARIZATION_MODEL_FLAG)?.let {
                it.ifEmpty {
                    error("Couldn't fetch gemma Model")
                }
            } ?: error("Couldn't get Flag value")

        suspend fun apiConfig(): String =
            remoteConfigService.getString(KEY_FLAG)?.ifEmpty {
                error("Couldn't fetch firebase key")
            } ?: error("Flag Value unavailable.")

        suspend inline fun <reified T> generate(
            prompt: String,
            references: List<ImageReference?> = emptyList(),
            temperatureRandomness: Float = .5f,
            requireTranslation: Boolean = true,
            describeOutput: Boolean = true,
            filterOutputFields: List<String> = emptyList(),
        ): T? =
            withContext(Dispatchers.IO) {
                val model = modelName()
                retryDelay?.let {
                    Log.e(javaClass.simpleName, "generate: Trying delay to avoid rate limit.")
                    delay(it)
                }

                requestMutex.withLock {
                    try {
                        val client =
                            com.google.ai.client.generativeai.GenerativeModel(
                                modelName = model,
                                apiKey = apiConfig(),
                                generationConfig {
                                    temperature = temperatureRandomness
                                },
                                safetySettings =
                                    HarmCategory.entries.map {
                                        SafetySetting(
                                            it,
                                            BlockThreshold.NONE,
                                        )
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
                                    appendLine(toJsonMap(T::class.java, filteredFields = filterOutputFields))
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

                        val content = client.generateContent(inputContent)

                        // Request succeeded — reset any retry delay because the service is operating again.
                        retryDelay = null

                        val response = content.text
                        Log.d(javaClass.simpleName, "Input content: ${inputContent.toJsonFormatExcludingFields(AI_EXCLUDED_FIELDS)}")
                        Log.i(
                            javaClass.simpleName,
                            "Generated content: ${content.toJsonFormatExcludingFields(
                                AI_EXCLUDED_FIELDS,
                            )}",
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
                            }

                        Log.d(javaClass.simpleName, promptDescription)

                        if (T::class == String::class) {
                            Log.i(javaClass.simpleName, "Prompt request result:\n$response")
                            return@withLock response as T
                        }

                        val cleanedJsonString = response.sanitizeAndExtractJsonString()
                        val typeToken = object : TypeToken<T>() {}
                        delay(2.seconds)
                        Gson().fromJson(cleanedJsonString, typeToken.type)
                    } catch (e: Exception) {
                        retryDelay = 3.seconds
                        Log.e(
                            this@GemmaClient::class.java.simpleName,
                            "Error in Generation($model): ${e.message}",
                            e,
                        )
                        Log.e(javaClass.simpleName, "failed to generate content for prompt: $prompt")
                        null
                    }
                }
            }
    }

const val KEY_FLAG = "FIREBASE_KEY"
