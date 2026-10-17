package com.ilustris.sagai.core.ai.model

/**
 * A prebuilt Gemini TTS voice. The list itself lives in Remote Config (`tts_voices`, see
 * [com.ilustris.sagai.core.ai.VoiceCatalog]) so new voices and better descriptions ship without
 * an app release; [BUNDLED] is only the fallback when that key is missing or malformed.
 *
 * @property id The voice name the TTS API expects (lowercase, e.g. "charon"). This is also what
 * characters, sagas and books persist.
 * Reference: https://ai.google.dev/gemini-api/docs/speech-generation
 */
data class Voice(
    val id: String = "",
    val gender: String = "",
    val description: String = "",
    /**
     * What the narrator picker shows the player. [description] stays as it is: it is English text for
     * the casting prompts ([com.ilustris.sagai.core.ai.VoiceCatalog.selectionGuide]), not for people.
     * Nullable on purpose — Gson builds these without running Kotlin defaults, so a voice from an
     * older Remote Config value would otherwise hold a null in a non-null field.
     */
    val name: String? = null,
    val tagline: String? = null,
    /** A short clip of the voice greeting someone, played in the picker. Blank until hosted. */
    val sampleUrl: String? = null,
    /** [com.ilustris.sagai.features.newsaga.data.model.Genre] names this voice is preselected for. */
    val suggestedGenres: List<String>? = null,
    /** Orb colors as `#RRGGBB`; the picker falls back to the saga's genre palette when missing. */
    val palette: List<String>? = null,
    /**
     * How this voice delivers a story as the saga's narrator (pacing, attitude, where the humor or
     * weight lands), in English because it goes straight into the TTS instruction. Kept apart from
     * [description] on purpose: that one is timbre only and is all the character casting reads.
     */
    val narratorDirection: String? = null,
) {
    /** Only voices curated with a display name are offered to the player. */
    val isPickable: Boolean get() = !name.isNullOrBlank()

    fun isSuggestedFor(genre: String): Boolean = suggestedGenres?.any { it.equals(genre, ignoreCase = true) } == true

    companion object {
        val BUNDLED =
            listOf(
                Voice("zephyr", "FEMALE", "Energetic, bright, and perky. Projects positivity and youthfulness."),
                Voice("puck", "MALE", "Upbeat, energetic, and youthful."),
                Voice("charon", "MALE", "Mature, confident, and professional. Reassuring and trustworthy."),
                Voice("kore", "FEMALE", "Firm, direct, and professional."),
                Voice("fenrir", "MALE", "High-energy, excitable, and conversational. Very engaging."),
                Voice("leda", "FEMALE", "Youthful, vibrant, and light-hearted. Fresh and perky."),
                Voice("orus", "MALE", "Solid, firm, and reliable. Steady delivery."),
                Voice("aoede", "FEMALE", "Conversational, thoughtful, and articulate. Sounds intelligent."),
                Voice("callirrhoe", "FEMALE", "Confident, direct, and professional. Articulate and clear."),
                Voice("autonoe", "FEMALE", "Mature, resonant, and thoughtful. Conveys wisdom."),
                Voice("enceladus", "MALE", "Energetic and enthusiastic. Impactful with a promotional feel."),
                Voice("iapetus", "MALE", "Friendly, casual, and relatable. An 'everyman' voice."),
                Voice("umbriel", "MALE", "Smooth, authoritative, and knowledgeable. Trustworthy."),
                Voice("algieba", "MALE", "Smooth-talking, polished, and confident. Conversational."),
                Voice("despina", "FEMALE", "Warm, inviting, and trustworthy. Friendly and engaging."),
                Voice("erinome", "FEMALE", "Clear, consistent, and straightforward."),
                Voice("algenib", "FEMALE", "Crisp, professional, and friendly. Warm authority."),
                Voice("rasalgethi", "MALE", "Informative, balanced, and steady."),
                Voice("laomedeia", "FEMALE", "Naturally upbeat, energetic, and positive."),
                Voice("achernar", "FEMALE", "Energetic, crisp, and confident. High brightness."),
                Voice("alnilam", "MALE", "Firm, steady, direct, and authoritative."),
                Voice("schedar", "MALE", "Even, consistent, and utility-focused. Steady pacing."),
                Voice("gacrux", "FEMALE", "Smooth, confident, and authoritative yet approachable."),
                Voice("pulcherrima", "FEMALE", "Forward, direct, and engaging."),
                Voice("achird", "FEMALE", "Youthful, friendly, and approachable. Inquisitive feel."),
                Voice("zubenelgenubi", "MALE", "Casual, relaxed, and natural."),
                Voice("vindemiatrix", "FEMALE", "Calm, thoughtful, and mature. Reassuring and gentle."),
                Voice("sadachbia", "MALE", "Lively, energetic, and distinctive."),
                Voice("sadaltager", "MALE", "Friendly, enthusiastic, and professional. Great for presentations."),
                Voice("sulafat", "FEMALE", "Warm, gentle, and comforting. Very trustworthy."),
            )
    }
}

/** Remote Config shape of `tts_voices`. */
data class TtsVoicesConfig(
    val voices: List<Voice> = emptyList(),
)
