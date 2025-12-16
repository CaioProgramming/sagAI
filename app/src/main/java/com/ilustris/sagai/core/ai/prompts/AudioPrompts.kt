package com.ilustris.sagai.core.ai.prompts

object AudioPrompts {
    fun transcribeInstruction() =
        buildString {
            appendLine("Generate a message about listening to the user's input. You can be playful about it.")
            appendLine(
                "Example: 'I'm all ears! Well, metaphorically speaking.' or 'Listening closely... or at least pretending to.'",
            )
        }
}
