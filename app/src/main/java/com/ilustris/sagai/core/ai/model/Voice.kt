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
    ZEPHYR("FEMALE", "Energetic, bright, and perky. Projects positivity and youthfulness."),
    PUCK("MALE", "Upbeat, energetic, and youthful."),
    CHARON("MALE", "Mature, confident, and professional. Reassuring and trustworthy."),

    // Row 2
    KORE("FEMALE", "Firm, direct, and professional."),
    FENRIR("MALE", "High-energy, excitable, and conversational. Very engaging."),
    LEDA("FEMALE", "Youthful, vibrant, and light-hearted. Fresh and perky."),

    // Row 3
    ORUS("MALE", "Solid, firm, and reliable. Steady delivery."),
    AOEDE("FEMALE", "Conversational, thoughtful, and articulate. Sounds intelligent."),
    CALLIRRHOE("FEMALE", "Confident, direct, and professional. Articulate and clear."),

    // Row 4
    AUTONOE("FEMALE", "Mature, resonant, and thoughtful. Conveys wisdom."),
    ENCELADUS("MALE", "Energetic and enthusiastic. Impactful with a promotional feel."),
    IAPETUS("MALE", "Friendly, casual, and relatable. An 'everyman' voice."),

    // Row 5
    UMBRIEL("MALE", "Smooth, authoritative, and knowledgeable. Trustworthy."),
    ALGIEBA("MALE", "Smooth-talking, polished, and confident. Conversational."),
    DESPINA("FEMALE", "Warm, inviting, and trustworthy. Friendly and engaging."),

    // Row 6
    ERINOME("FEMALE", "Clear, consistent, and straightforward."),
    ALGENIB("FEMALE", "Crisp, professional, and friendly. Warm authority."),
    RASALGETHI("MALE", "Informative, balanced, and steady."),

    // Row 7
    LAOMEDEIA("FEMALE", "Naturally upbeat, energetic, and positive."),
    ACHERNAR("FEMALE", "Energetic, crisp, and confident. High brightness."),
    ALNILAM("MALE", "Firm, steady, direct, and authoritative."),

    // Row 8
    SCHEDAR("MALE", "Even, consistent, and utility-focused. Steady pacing."),
    GACRUX("FEMALE", "Smooth, confident, and authoritative yet approachable."),
    PULCHERRIMA("FEMALE", "Forward, direct, and engaging."),

    // Row 9
    ACHIRD("FEMALE", "Youthful, friendly, and approachable. Inquisitive feel."),
    ZUBENELGENUBI("MALE", "Casual, relaxed, and natural."),
    VINDEMIATRIX("FEMALE", "Calm, thoughtful, and mature. Reassuring and gentle."),

    // Row 10
    SADACHBIA("MALE", "Lively, energetic, and distinctive."),
    SADALTAGER("MALE", "Friendly, enthusiastic, and professional. Great for presentations."),
    SULAFAT("FEMALE", "Warm, gentle, and comforting. Very trustworthy."),
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
