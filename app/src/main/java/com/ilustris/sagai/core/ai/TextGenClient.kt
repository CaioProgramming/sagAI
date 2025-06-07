package com.ilustris.sagai.core.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerationConfig
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.generationConfig
import com.google.gson.Gson
import com.ilustris.sagai.core.utils.toJsonSchema

class TextGenClient : AIClient() {
    override fun buildModel(generationConfig: GenerationConfig): GenerativeModel =
        Firebase
            .ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                "gemini-2.0-flash",
                generationConfig = generationConfig,
            )

    suspend inline fun <reified T> generate(
        prompt: String,
        requireTranslation: Boolean = true,
    ): T? {
        try {
            val model =
                buildModel(
                    generationConfig {
                        responseMimeType = "application/json"
                        responseSchema = toJsonSchema(T::class.java)
                    },
                )
            val fullPrompt =
                if (requireTranslation) {
                    "$prompt ${modelLanguage()}"
                } else {
                    prompt
                }.trimIndent()
            val content = model.generateContent(fullPrompt)
            Log.i(javaClass.simpleName, "prompt: $fullPrompt")
            Log.i(javaClass.simpleName, "generated: ${content.text}")

            val contentData = Gson().fromJson<T>(content.text, T::class.java)

            return contentData
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
