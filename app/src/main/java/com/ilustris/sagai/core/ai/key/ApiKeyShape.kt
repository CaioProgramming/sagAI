package com.ilustris.sagai.core.ai.key

/**
 * Whether some text looks enough like an API key to be worth offering to the user.
 *
 * Deliberately loose. Google issues at least two shapes — the classic `AIza...` and the newer
 * `AQ.A...` — and matching a fixed list is how this goes stale the next time they add a third. So
 * a known prefix is treated as a strong yes, and anything else is judged on shape alone: one
 * unbroken token, long enough to be a credential, made of the characters keys are made of.
 *
 * This never decides whether a key is *valid*. Only the API can say that, and it does, on the
 * `listModels` call made when the key is saved. Being wrong here costs a suggestion the user
 * ignores, never a working key refused.
 */
object ApiKeyShape {
    private val KNOWN_PREFIXES = listOf("AIza", "AQ.")
    private const val MIN_LENGTH = 30
    private const val MAX_LENGTH = 200
    private const val EXTRA_KEY_CHARS = "-_."
    private const val MASKABLE_MIN_LENGTH = 12
    private const val MASK = "••••••"

    /**
     * How a key is shown to the person who owns it: enough of both ends to recognise which key
     * this is, never enough to use it. Shared so the settings row and the input field cannot drift
     * into showing the same secret two different ways.
     */
    fun mask(key: String): String =
        if (key.length <= MASKABLE_MIN_LENGTH) MASK else "${key.take(4)}$MASK${key.takeLast(4)}"

    /** Below this a key is too short to reveal any of it. */
    fun isMaskable(key: String): Boolean = key.length > MASKABLE_MIN_LENGTH

    fun looksLikeKey(candidate: String?): Boolean {
        val text = candidate?.trim().orEmpty()
        if (text.length !in MIN_LENGTH..MAX_LENGTH) return false
        if (text.any { it.isWhitespace() }) return false
        if (KNOWN_PREFIXES.any { text.startsWith(it) }) return true
        return text.all { it.isLetterOrDigit() || it in EXTRA_KEY_CHARS }
    }
}
