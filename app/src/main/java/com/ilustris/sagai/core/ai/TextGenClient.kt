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
import com.ilustris.sagai.core.utils.toFirebaseSchema

class TextGenClient(
    private val firebaseRemoteConfig: FirebaseRemoteConfig,
) : AIClient() {
    companion object {
        const val TEXT_GEN_MODEL_FLAG = "textGenModel"
        const val DEFAULT_TEXT_GEN_MODEL = "gemini-2.5-flash"
    }

    fun modelName() =
        firebaseRemoteConfig.getString(TEXT_GEN_MODEL_FLAG).let {
            it.ifEmpty { DEFAULT_TEXT_GEN_MODEL }
        }

    override fun buildModel(generationConfig: GenerationConfig): GenerativeModel {
        Log.i("TextGenClient", "Using text model: ${modelName()} from Remote Config (flag: '$TEXT_GEN_MODEL_FLAG')")
        return Firebase
            .ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                modelName = modelName(),
                generationConfig = generationConfig,
            )
    }

    suspend inline fun <reified T> generate(
        prompt: String,
        requireTranslation: Boolean = true,
        customSchema: Schema? = null,
    ): T? {
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
            val content =
                model.generateContent(fullPrompt).also {
                    Log.d(javaClass.simpleName, "Token count for request: ${it.usageMetadata?.totalTokenCount}")
                }
            Log.i(javaClass.simpleName, "prompt: $fullPrompt")
            Log.i(javaClass.simpleName, "generated(${modelName()}): ${content.text}")
            val contentData =
                if (T::class.java == String::class.java) {
                    content.text as? T
                } else {
                    Gson().fromJson(content.text, T::class.java)
                }
            return contentData
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
