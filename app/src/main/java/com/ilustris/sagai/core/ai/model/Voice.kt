package com.ilustris.sagai.core.ai.model

/**
 * Voice options supported by Gemini's TTS API.
 * Reference: https://ai.google.dev/gemini-api/docs/speech-generation
 */
enum class Voice(
    val gender: String,
    val description: String,
) {
    // Row 1
    ZEPHYR("FEMALE", "Bright"),
    PUCK("MALE", "Upbeat"),
    CHARON("MALE", "Informative"),

    // Row 2
    KORE("FEMALE", "Firm"),
    FENRIR("MALE", "Excitable"),
    LEDA("FEMALE", "Youthful"),

    // Row 3
    ORUS("MALE", "Firm"),
    AOEDE("FEMALE", "Breezy"),
    CALLIRRHOE("FEMALE", "Easy-going"),

    // Row 4
    AUTONOE("FEMALE", "Bright"),
    ENCELADUS("MALE", "Breathy"),
    IAPETUS("MALE", "Clear"),

    // Row 5
    UMBRIEL("MALE", "Easy-going"),
    ALGIEBA("MALE", "Smooth"),
    DESPINA("FEMALE", "Smooth"),

    // Row 6
    ERINOME("FEMALE", "Clear"),
    ALGENIB("MALE", "Gravelly"),
    RASALGETHI("MALE", "Informative"),

    // Row 7
    LAOMEDEIA("FEMALE", "Upbeat"),
    ACHERNAR("FEMALE", "Soft"),
    ALNILAM("MALE", "Firm"),

    // Row 8
    SCHEDAR("FEMALE", "Even"),
    GACRUX("MALE", "Mature"),
    PULCHERRIMA("FEMALE", "Forward"),

    // Row 9
    ACHIRD("MALE", "Friendly"),
    ZUBENELGENUBI("MALE", "Casual"),
    VINDEMIATRIX("FEMALE", "Gentle"),

    // Row 10
    SADACHBIA("FEMALE", "Lively"),
    SADALTAGER("MALE", "Knowledgeable"),
    SULAFAT("FEMALE", "Warm"),
    ;

    /**
     * Returns the voice ID for the Gemini API (lowercase enum name).
     */
    val id: String get() = name.lowercase()

    companion object {
        /**
         * Returns a formatted description of all voices for AI selection prompts.
         */
        fun getVoiceSelectionGuide(): String =
            buildString {
                appendLine("Available voices:")
                entries.forEach { voice ->
                    appendLine("- ${voice.name}: ${voice.description} (${voice.gender})")
                }
            }

        /**
         * Finds a voice by name, case-insensitive.
         */
        fun findByName(name: String?): Voice? = entries.find { it.name.equals(name?.trim(), ignoreCase = true) }
    }
}
