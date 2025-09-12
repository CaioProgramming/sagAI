package com.ilustris.sagai.features.saga.chat.domain.model

/**
 * Narrative-safe, compact set of emotional tones for user messages.
 * Keep labels uppercase to simplify LLM classification and parsing.
 */
enum class EmotionalTone {
    NEUTRAL,
    CALM,
    CURIOUS,
    HOPEFUL,
    DETERMINED,
    EMPATHETIC,
    JOYFUL,
    CONCERNED,
    ANXIOUS,
    FRUSTRATED,
    ANGRY,
    SAD,
    MELANCHOLIC,
    CYNICAL,
    ;

    companion object {
        fun getTone(tone: String?) =
            try {
                if (tone == null) NEUTRAL
                valueOf(tone!!.uppercase())
            } catch (e: Exception) {
                NEUTRAL
            }
    }
}
