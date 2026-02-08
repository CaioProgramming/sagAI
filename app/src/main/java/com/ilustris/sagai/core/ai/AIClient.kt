package com.ilustris.sagai.core.ai

import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.type.GenerationConfig
import java.util.Locale

abstract class AIClient {
    open suspend fun buildModel(generationConfig: GenerationConfig): GenerativeModel? = null

    fun modelLanguage(): String {
        val locale = Locale.getDefault()
        return "All responses must be in ${locale.toLanguageTag()}."
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
