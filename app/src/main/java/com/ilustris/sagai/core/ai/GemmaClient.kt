package com.ilustris.sagai.core.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerationConfig
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken // <-- ADDED IMPORT
import com.ilustris.sagai.core.utils.sanitizeAndExtractJsonString // <-- ADDED IMPORT FOR EXTENSION
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GemmaClient
    @Inject
    constructor(
        private val firebaseRemoteConfig: FirebaseRemoteConfig,
    ) : AIClient() {
        companion object {
            const val SUMMARIZATION_MODEL_FLAG = "summarizationModel"

            const val DEFAULT_SUMMARIZATION_MODEL = "gemma-3n-e4b-it"
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

        override fun buildModel(generationConfig: GenerationConfig): GenerativeModel {
            Log.i(
                this::class.java.simpleName,
                "Using summarization model: ${modelName()}",
            )
            return Firebase
                .ai
                .generativeModel(
                    modelName = modelName(),
                    generationConfig = generationConfig,
                )
        }

        suspend inline fun <reified T> generate(
            prompt: String,
            requireTranslation: Boolean = true,
        ): T? {
            try {
                val model =
                    buildModel(
                        generationConfig {
                            responseMimeType = "text/plain"
                        },
                    )

                val fullPrompt =
                    (if (requireTranslation) "$prompt ${modelLanguage()}" else prompt)

                Log.i(this::class.java.simpleName, "Summarization prompt: $fullPrompt")
                val content = model.generateContent(fullPrompt)
                Log.i(this::class.java.simpleName, "Summarization received raw: ${content.text}") // Log raw response
                Log.d(
                    this::class.java.simpleName,
                    "Token count for request: ${content.usageMetadata?.totalTokenCount}",
                )

                if (T::class == String::class) {
                    val model = buildModel(generationConfig { responseMimeType = "text/plain" })
                    val response = model.generateContent(if (requireTranslation) "$prompt ${modelLanguage()}" else prompt).text
                    return response as? T
                }

                val cleanedJsonString = content.text.sanitizeAndExtractJsonString()
                Log.i(this::class.java.simpleName, "Using cleaned JSON for parsing: $cleanedJsonString")

                val typeToken = object : TypeToken<T>() {}
                return Gson().fromJson(cleanedJsonString, typeToken.type)
            } catch (e: Exception) {
                Log.e(this::class.java.simpleName, "Error in Generation(${modelName()}): ${e.message}", e)
                return null
            }
        }
    }
