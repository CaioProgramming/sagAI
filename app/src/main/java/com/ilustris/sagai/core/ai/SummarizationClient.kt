package com.ilustris.sagai.core.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerationConfig
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken // <-- ADDED IMPORT
import com.ilustris.sagai.core.utils.toFirebaseSchema
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SummarizationClient
    @Inject
    constructor(
        private val firebaseRemoteConfig: FirebaseRemoteConfig,
    ) : AIClient() {
        companion object {
            const val SUMMARIZATION_MODEL_FLAG = "summarizationModel"

            const val DEFAULT_SUMMARIZATION_MODEL = "gemini-1.5-flash"
        }

        override fun buildModel(generationConfig: GenerationConfig): GenerativeModel {
            val modelName =
                firebaseRemoteConfig.getString(SUMMARIZATION_MODEL_FLAG).let {
                    it.ifEmpty {
                        Log.e(javaClass.simpleName, "buildModel: Firebase flag $SUMMARIZATION_MODEL_FLAG value not retrieved")
                        DEFAULT_SUMMARIZATION_MODEL
                    }
                }
            Log.i(
                this::class.java.simpleName,
                "Using summarization model: $modelName",
            )
            return Firebase
                .ai(backend = GenerativeBackend.googleAI())
                .generativeModel(
                    modelName = modelName,
                    generationConfig = generationConfig,
                )
        }

        suspend inline fun <reified T> generate(
            prompt: String,
            customSchema: Schema? = null,
            requireTranslation: Boolean = true,
        ): T? {
            try {
                val effectiveSchema =
                    customSchema ?: if (T::class.java != String::class.java) {
                        toFirebaseSchema(T::class.java)
                    } else {
                        null
                    }

                val model =
                    buildModel(
                        generationConfig {
                            if (effectiveSchema != null) {
                                responseMimeType = "application/json"
                                responseSchema = effectiveSchema
                            }
                        },
                    )

                val fullPrompt = if (requireTranslation) "$prompt ${modelLanguage()}" else prompt

                Log.i(this::class.java.simpleName, "Summarization prompt: $fullPrompt")
                val content = model.generateContent(fullPrompt)
                Log.i(this::class.java.simpleName, "Summarization generated: ${content.text}")

                return if (T::class.java == String::class.java && effectiveSchema == null) {
                    content.text as? T
                } else {
                    val typeToken = object : TypeToken<T>() {}
                    Gson().fromJson(content.text, typeToken.type)
                }
            } catch (e: Exception) {
                Log.e(this::class.java.simpleName, "Error in summarization generate: ${e.message}", e)
                return null
            }
        }
    }
