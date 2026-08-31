package com.ilustris.sagai.core.ai.key

import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.network.GeminiApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

sealed class ApiKeyVerification {
    object Unknown : ApiKeyVerification()

    object Checking : ApiKeyVerification()

    object Valid : ApiKeyVerification()

    /** The API turned the key down. Distinct from [Unreachable], which proves nothing. */
    object Invalid : ApiKeyVerification()

    /** No answer. The key may be perfectly good, so nothing is concluded from it. */
    object Unreachable : ApiKeyVerification()
}

/**
 * Whether the stored key still works, shared by every screen that shows it.
 *
 * A single holder rather than a result handed from one screen to another: the settings row and the
 * key sheet are looking at the same key, and passing status between them would let them disagree
 * the moment one refreshed and the other did not.
 *
 * Cheap to ask. Verification is a `GET /models`, which is not a generation call, so it costs
 * nothing from the daily request budget the user actually feels.
 */
@Singleton
class ApiKeyVerificationService
    @Inject
    constructor(
        private val geminiApiClient: GeminiApiClient,
        private val userApiKeyStore: UserApiKeyStore,
    ) {
        private val _status = MutableStateFlow<ApiKeyVerification>(ApiKeyVerification.Unknown)
        val status: StateFlow<ApiKeyVerification> = _status.asStateFlow()

        suspend fun verify() {
            val key = userApiKeyStore.getKeyNow()
            if (key.isNullOrBlank()) {
                _status.value = ApiKeyVerification.Unknown
                return
            }

            // Already known to be rejected: asking again would flash "checking" on the way to a
            // fact we are holding. Saving a new key clears that state, so this unblocks itself.
            if (userApiKeyStore.observeState().first() is ApiKeyState.Invalidated) {
                _status.value = ApiKeyVerification.Invalid
                return
            }

            _status.value = ApiKeyVerification.Checking
            executeRequest(reportCrash = false) { geminiApiClient.listModels(key) }
                .onSuccess { _status.value = ApiKeyVerification.Valid }
                .onFailureAsync { error ->
                    val rejected = classifyApiKeyFailure(error) is ApiKeyDiagnosis.Rejected
                    _status.value =
                        if (rejected) ApiKeyVerification.Invalid else ApiKeyVerification.Unreachable
                    // A rejection is durable, so it belongs in the store too: it is the same fact
                    // the app gates and warns on, learned here instead of mid-generation.
                    if (rejected) userApiKeyStore.markInvalid(ApiKeyFailure.INVALID)
                }
        }

        /** Called after a key is saved, since saving has already proved it works. */
        fun markVerified() {
            _status.value = ApiKeyVerification.Valid
        }
    }
