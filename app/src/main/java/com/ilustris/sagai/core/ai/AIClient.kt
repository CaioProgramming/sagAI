package com.ilustris.sagai.core.ai

import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.type.GenerationConfig
import java.util.Locale

abstract class AIClient {
    open suspend fun buildModel(generationConfig: GenerationConfig): GenerativeModel? = null

    fun getLanguage(requireTranslation: Boolean = true): String {
        val locale = if (requireTranslation) Locale.getDefault() else Locale.US
        val languageName = locale.getDisplayName(locale)
        return languageName.ifBlank { locale.toLanguageTag() }
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
