package com.ilustris.sagai.core.ai

import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.type.GenerationConfig
import java.util.Locale

abstract class AIClient {
    abstract fun buildModel(generationConfig: GenerationConfig): GenerativeModel

    fun modelLanguage(): String {
        val locale = Locale.getDefault()
        locale.language
        return "All responses must be in ${locale.displayLanguage} (${locale.language})."
    }
}
