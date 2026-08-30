package com.ilustris.sagai.core.ai.key

/**
 * Whether the app has a usable Gemini API key.
 *
 * Scope is deliberately narrow: presence and validity, nothing else. Quota — including the daily
 * cooldown — belongs to [QuotaStatusService], because a rate-limited key is still a perfectly good
 * key and must not gate the same things a missing one does. Two sources of truth for "can we
 * generate right now" is how those two cases get conflated.
 */
sealed class ApiKeyState {
    /** Never configured, or the stored blob could not be decrypted. Blocks the whole app. */
    object Missing : ApiKeyState()

    /** A key is stored and has not been rejected. */
    object Present : ApiKeyState()

    /**
     * A key is stored but the API rejected it. Does not blank the app — the user keeps their
     * context and gets a sheet asking for a replacement.
     */
    data class Invalidated(
        val reason: ApiKeyFailure,
    ) : ApiKeyState()
}
