package com.ilustris.sagai.core.ai.key

import kotlinx.coroutines.flow.Flow

/**
 * The user's own Gemini API key — the single credential every AI call in the app now runs on.
 *
 * There is exactly one slot, and that is deliberate. A multi-key manager would be, in function, a
 * free-tier rotation tool: quota is billed per Google Cloud project, so cycling keys across
 * projects multiplies the daily allowance, and any consequence for that lands on the user's Google
 * account. Making them retype a key each time keeps the app out of it. The sanctioned answer to
 * hitting the ceiling is enabling billing on the project, which is what the quota copy points at.
 */
interface UserApiKeyStore {
    fun observeState(): Flow<ApiKeyState>

    /** Decrypted key, or null when absent or undecryptable. Never log the return value. */
    suspend fun getKeyNow(): String?

    /** Stores [key], clears any rejection, and wipes quota cooldowns (see [QuotaStatusService]). */
    suspend fun save(key: String)

    /** Flags the stored key as rejected without discarding it, so the UI can explain why. */
    suspend fun markInvalid(reason: ApiKeyFailure)

    suspend fun clear()
}
