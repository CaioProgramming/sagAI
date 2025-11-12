package com.ilustris.sagai.core.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerationConfig
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.recordException
import com.google.gson.Gson
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.utils.sanitizeAndExtractJsonString
import com.ilustris.sagai.core.utils.toFirebaseSchema
import com.ilustris.sagai.core.utils.toJsonFormatExcludingFields

class TextGenClient(
    private val remoteConfigService: RemoteConfigService,
) : AIClient() {
    companion object {
        const val TEXT_GEN_MODEL_FLAG = "textGenModel"
    }

    suspend fun modelName() = remoteConfigService.getString(TEXT_GEN_MODEL_FLAG)

    override suspend fun buildModel(generationConfig: GenerationConfig): GenerativeModel {
        val model = modelName() ?: error("No Model provided")

        Log.i(
            "TextGenClient",
            "Using text model: $model from Remote Config (flag: '$TEXT_GEN_MODEL_FLAG')",
        )
        return Firebase
            .ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                modelName = model,
                generationConfig = generationConfig,
            )
    }

    suspend inline fun <reified T> generate(
        prompt: String,
        requireTranslation: Boolean = true,
        customSchema: Schema? = null,
    ): T? {
        val model = modelName() ?: error("No Model provided")

        try {
            val model =
                if (T::class.java == String::class.java) {
                    buildModel(
                        generationConfig {
                            responseMimeType = "text/plain"
                        },
                    )
                } else {
                    val schema = customSchema ?: toFirebaseSchema(T::class.java)
                    buildModel(
                        generationConfig {
                            responseMimeType = "application/json"
                            responseSchema = schema
                        },
                    )
                }
            val fullPrompt =
                if (requireTranslation) {
                    "$prompt ${modelLanguage()}"
                } else {
                    prompt
                }
            val content = model.generateContent(fullPrompt)
            Log.i(
                javaClass.simpleName,
                "generating with model: $model\nPrompt size: ${fullPrompt.length} chars.",
            )
            Log.d(
                javaClass.simpleName,
                "content generation result: ${content.toJsonFormatExcludingFields(AI_EXCLUDED_FIELDS)}",
            )

            Log.i(javaClass.simpleName, "prompt: $fullPrompt")

            val contentData =
                if (T::class.java == String::class.java) {
                    content.text as? T
                } else {
                    Gson().fromJson(content.text.sanitizeAndExtractJsonString(), T::class.java)
                }
            return contentData
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e, {
                key("model", model)
            })
            return null
        }
    }
}
