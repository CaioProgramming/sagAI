package com.ilustris.sagai.core.ai

import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.type.GenerationConfig
import java.util.Locale

abstract class AIClient {
    open suspend fun buildModel(generationConfig: GenerationConfig): GenerativeModel? = null

    fun modelLanguage(requireTranslation: Boolean = true): String {
        val locale = if (requireTranslation) Locale.getDefault() else Locale.US
        val language = locale.toLanguageTag()
        return """
            CRITICAL LANGUAGE RULE:
            - ALL string values in the response MUST be in $language.
            - Even if the prompt and instructions are in English, the output content MUST be in $language.
            """.trimIndent()
    }
}

val AI_EXCLUDED_FIELDS =
    listOf(
        "text\$delegate",
        "functionResponse\$delegate",
        "functionCall\$delegate",
        "functionCalls\$delegate",
        "\"inlineDataParts\$delegate",
    )
