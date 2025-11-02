package com.ilustris.sagai.core.ai

import android.graphics.Bitmap
import android.util.Log
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.ImagePart
import com.google.ai.client.generativeai.type.TextPart
import com.google.ai.client.generativeai.type.generationConfig
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerationConfig
import com.google.firebase.ai.type.content
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.recordException
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken // <-- ADDED IMPORT
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.core.ai.models.ImageReference
import com.ilustris.sagai.core.utils.formatToJsonArray
import com.ilustris.sagai.core.utils.sanitizeAndExtractJsonString // <-- ADDED IMPORT FOR EXTENSION
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields
import com.ilustris.sagai.core.utils.toJsonMap
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.log
import kotlin.time.Duration.Companion.seconds

@Singleton
class GemmaClient
    @Inject
    constructor(
        private val firebaseRemoteConfig: FirebaseRemoteConfig,
    ) : AIClient() {
        @PublishedApi
        internal val requestRunning = AtomicBoolean(false)
        val isRequestRunning: Boolean get() = requestRunning.get()

        companion object {
            const val SUMMARIZATION_MODEL_FLAG = "summarizationModel"

            const val DEFAULT_SUMMARIZATION_MODEL = "gemini-2.0-flash-lite"
            const val DEFAULT_DELAY: Long = 1500L
        }

        fun modelName() =
            firebaseRemoteConfig.getString(SUMMARIZATION_MODEL_FLAG).let {
                it.ifEmpty {
                    Log.e(
                        javaClass.simpleName,
                        "buildModel: Firebase flag $SUMMARIZATION_MODEL_FLAG value not retrieved",
                    )
                    DEFAULT_SUMMARIZATION_MODEL
                }
            }

        override fun buildModel(generationConfig: GenerationConfig): GenerativeModel? {
            Log.i(
                this::class.java.simpleName,
                "Using summarization model: ${modelName()}",
            )
            return null
        }

        suspend inline fun <reified T> generate(
            prompt: String,
            references: List<ImageReference?> = emptyList(),
            temperatureRandomness: Float = 0f,
            requireTranslation: Boolean = true,
            skipRunning: Boolean = false,
            describeOutput: Boolean = true,
            filterOutputFields: List<String> = emptyList(),
        ): T? {
            var acquired = false
            try {
                if (!skipRunning) {
                    acquired = requestRunning.compareAndSet(false, true)
                    if (!acquired) {
                        throw IllegalStateException("Gemma request already running")
                    }
                } else if (isRequestRunning) {
                    delay(DEFAULT_DELAY)
                }

                val client =
                    com.google.ai.client.generativeai.GenerativeModel(
                        modelName = modelName(),
                        apiKey = BuildConfig.APIKEY,
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

                        if (T::class != String::class && describeOutput) {
                            appendLine("Your OUTPUT is a ${T::class.java.simpleName}")
                            appendLine("Follow this structure on your output:")
                            appendLine(toJsonMap(T::class.java, filteredFields = filterOutputFields))
                        }
                    }

                Log.i(this::class.java.simpleName, "Requesting ${modelName()}")

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
                        if (prompt.isNotEmpty()) {
                            appendLine("Main prompt { ")
                            appendLine(prompt)
                            appendLine("}")
                        }
                        appendLine("References:")
                        appendLine(references.filterNotNull().formatToJsonArray())
                    }

                Log.d(javaClass.simpleName, promptDescription)

                if (T::class == String::class) {
                    Log.i(javaClass.simpleName, "Prompt request result:\n$response")
                    return response as T
                }

                val cleanedJsonString = response.sanitizeAndExtractJsonString()
                val typeToken = object : TypeToken<T>() {}
                return Gson().fromJson(cleanedJsonString, typeToken.type)
            } catch (e: Exception) {
                Log.e(this::class.java.simpleName, "Error in Generation(${modelName()}): ${e.message}", e)
                FirebaseCrashlytics.getInstance().recordException(e, {
                    key("model", modelName())
                })
                Log.e(javaClass.simpleName, "failed to generate content for prompt: $prompt")
                return null
            } finally {
                if (!skipRunning && acquired) {
                    requestRunning.set(false)
                }
            }
        }
    }
