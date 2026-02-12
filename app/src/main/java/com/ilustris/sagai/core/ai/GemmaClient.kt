package com.ilustris.sagai.core.ai

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
import timber.log.Timber
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
            const val CORE_FLAG = "SAGA_CORE"
            const val INPUT_TOKEN_LIMIT = 15000
            const val REACTIVE_DELAY_THRESHOLD = 0.7f
            const val MAX_RETRIES = 2
            const val RETRY_DELAY = 20
        }

        enum class ModelRequirement(
            val flag: String,
            val defaultModel: String,
        ) {
            LOW("gemma_low_tier", "models/gemma-3-1b-it"),
            MEDIUM("gemma_medium_tier", "models/gemma-3-12b-it"),
            HIGH("gemma_high_tier", "models/gemma-3-27b-it"),
        }

        suspend fun modelName(requirement: ModelRequirement) =
            remoteConfigService.getString(requirement.flag)?.let {
                it.ifEmpty {
                    requirement.defaultModel
                }
            } ?: requirement.defaultModel

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
            requirement: ModelRequirement = ModelRequirement.HIGH,
        ): T? =
            withContext(Dispatchers.IO) {
                if (lastTokenCount > (INPUT_TOKEN_LIMIT * REACTIVE_DELAY_THRESHOLD) && retryDelay == null) {
                    Timber.w("Applying reactive delay due to high token count in last request.")
                    retryDelay = 5
                    delay((retryDelay ?: 5).seconds)
                }
                val model = modelName(requirement)
                if (useCore.not()) {
                    retryDelay?.let {
                        Timber.e(
                            "generate: Trying delay $retryDelay seconds to avoid rate limit.",
                        )
                        delay(it.seconds)
                    }
                } else {
                    Timber.i("generate: Core calls don't require delay.")
                }

                val maxAttempts = if (requirement == ModelRequirement.HIGH) MAX_RETRIES + 1 else 1

                for (currentAttempt in 1..maxAttempts) {
                    try {
                        return@withContext requestMutex.withLock {
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
                                    } else {
                                        appendLine("Ensure the response is strictly in ENGLISH (EN-US).")
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
                                        // Add JSON string rules if the output contains string fields
                                        if (containsStringFields(T::class.java)) {
                                            appendLine()
                                            appendLine("CRITICAL JSON STRING RULES:")
                                            appendLine("- Any double quote inside a string value MUST be escaped as \\\"")
                                            appendLine("- Example: \"text\": \"He said \\\"hello\\\" to me\"")
                                            appendLine("- NEVER leave string values unquoted like: \"name\": John")
                                            appendLine("- CORRECT format: \"name\": \"John\"")
                                        }
                                    }
                                }

                            Timber.i(
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

                            Timber.d(
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

                            Timber.d(
                                "Input content: ${
                                    inputContent.toJsonFormatExcludingFields(AI_EXCLUDED_FIELDS)
                                }",
                            )

                            Timber.i(
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

                            Timber.d(promptDescription)

                            if (T::class == String::class) {
                                Timber.i("Prompt request result:\n$response")
                                return@withLock response as T
                            }

                            val cleanedJsonString =
                                response.sanitizeAndExtractJsonString(T::class.java)
                            val typeToken = object : TypeToken<T>() {}
                            Gson().fromJson(cleanedJsonString, typeToken.type)
                        }
                    } catch (e: Exception) {
                        Timber.e(
                            e,
                            "Error in Generation($model) Attempt $currentAttempt/$maxAttempts: ${e.javaClass.simpleName} - ${e.message}",
                        )

                        if (currentAttempt < maxAttempts) {
                            Timber.w(
                                "Retrying HIGH priority request in $RETRY_DELAY seconds...",
                            )
                            delay(RETRY_DELAY.seconds)
                        } else {
                            // Final failure
                            retryDelay =
                                retryDelay?.let {
                                    if (it > 30) it / 2 else it + it
                                } ?: 2
                            Timber.e(
                                "Final failure after $maxAttempts attempts.",
                            )
                            Timber.e("generate: Failed prompt")
                            Timber.w(prompt)
                            return@withContext null
                        }
                    }
                }
                return@withContext null
            }

        /**
         * Recursively checks if a class or any of its nested classes contain String fields.
         */
        @PublishedApi
        internal fun containsStringFields(
            clazz: Class<*>,
            visited: MutableSet<Class<*>> = mutableSetOf(),
        ): Boolean {
            if (clazz in visited || clazz.isPrimitive || clazz.isEnum) return false
            if (clazz == String::class.java) return true
            visited.add(clazz)

            return clazz.declaredFields.any { field ->
                val fieldType = field.type
                when {
                    fieldType == String::class.java -> true
                    fieldType.isPrimitive || fieldType.isEnum -> false
                    else -> containsStringFields(fieldType, visited)
                }
            }
        }
    }

const val KEY_FLAG = "FIREBASE_KEY"
