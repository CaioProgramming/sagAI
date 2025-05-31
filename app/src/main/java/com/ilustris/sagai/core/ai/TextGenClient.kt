package com.ilustris.sagai.core.ai

import android.util.Log
import com.google.firebase.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerateContentResponse
import com.google.firebase.ai.type.GenerationConfig
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.generationConfig

class TextGenClient : AIClient() {
    override fun buildModel(generationConfig: GenerationConfig): GenerativeModel =
        Firebase
            .ai(backend = GenerativeBackend.googleAI())
            .generativeModel(
                "gemini-2.0-flash",
                generationConfig = generationConfig,
            )

    suspend fun generate(
        prompt: String,
        requireTranslation: Boolean = true,
        generationConfig: GenerationConfig,
    ): GenerateContentResponse? {
        try {
            val model = buildModel(generationConfig)
            val fullPrompt =
                if (requireTranslation) {
                    "$prompt ${modelLanguage()}"
                } else {
                    prompt
                }
            val content = model.generateContent(fullPrompt)
            if (BuildConfig.DEBUG) {
                Log.i(javaClass.simpleName, "generated: ${content.text}")
            }
            return model.generateContent(fullPrompt)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
