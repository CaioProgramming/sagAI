package com.ilustris.sagai.features.saga.chat.data.model

/**
 * How the player sent a message. Drives which `player_input_blueprint` bucket is merged into the
 * reply prompt: TYPED gets typo/name fixes only, VOICE gets the full speech → tagged message
 * formatting (and the audio itself is attached to the request).
 */
enum class InputMode {
    TYPED,
    VOICE,
    ;

    companion object {
        fun fromString(value: String?): InputMode? = entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}
