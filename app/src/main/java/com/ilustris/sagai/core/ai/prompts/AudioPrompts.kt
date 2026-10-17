package com.ilustris.sagai.core.ai.prompts

import com.ilustris.sagai.core.ai.model.SplitPrompt

/**
 * Message voicing moved to [VoicePrompts] (casting + performance script); what's left here is the
 * short "I'm listening" line of the legacy speech-recognizer flow.
 */
object AudioPrompts {
    const val AUDIO_CONFIG_BLUEPRINT = "audio_config_blueprint"

    fun transcribeInstruction(): SplitPrompt =
        SplitPrompt(
            blueprintKey = AUDIO_CONFIG_BLUEPRINT,
            instructionBuckets = emptyMap(),
            processedTemplate =
                "Generate a short, playful message about listening to the user. Example: 'I'm all ears!'",
        )
}
