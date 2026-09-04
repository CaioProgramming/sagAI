package com.ilustris.sagai.core.ai.key

import com.ilustris.sagai.core.ai.model.GeminiErrorResponse
import com.ilustris.sagai.core.ai.parseGeminiHttpError
import com.ilustris.sagai.core.network.GeminiHttpException

/**
 * What an API failure says about the user's key.
 *
 * The whole point of this type is keeping the two 429s apart. They look identical on the wire and
 * need opposite handling: a per-minute throttle clears in seconds and the existing backoff already
 * covers it, while a spent daily quota makes every retry until midnight Pacific pure waste.
 */
sealed class ApiKeyDiagnosis {
    /** The key itself is bad. Stop retrying and ask for a new one. */
    data class Rejected(
        val failure: ApiKeyFailure,
    ) : ApiKeyDiagnosis()

    /** Free-tier daily allowance is gone. Persist a cooldown; do not ask for a new key. */
    object QuotaDaily : ApiKeyDiagnosis()

    /**
     * Short throttle. Keep the existing backoff; just tell the user why they are waiting.
     *
     * [tokenQuotaValue] is the per-minute token budget the API reported breaking, when it named
     * one. A single request larger than that budget can never fit no matter how long you wait, so
     * it is the difference between a wait worth taking and a retry loop that is already lost.
     */
    data class QuotaMinute(
        val retryDelaySeconds: Long?,
        val tokenQuotaValue: Int? = null,
    ) : ApiKeyDiagnosis()
}

/**
 * A `retryDelay` longer than this only ever comes back with a daily bucket — a per-minute throttle
 * is quoted in seconds. Used as the fallback when the quota id stops carrying its scope in the name.
 */
private const val DAILY_DELAY_THRESHOLD_SECONDS = 300L

/** @return null when [throwable] says nothing about the key (network, parsing, token limits). */
fun classifyApiKeyFailure(throwable: Throwable): ApiKeyDiagnosis? {
    val exception = throwable as? GeminiHttpException ?: return null
    val parsed = parseGeminiHttpError(exception.code, exception.errorBody)
    val haystack = "${parsed.message} ${parsed.body.orEmpty()}".lowercase()

    return when (exception.code) {
        // Not every 400 is a key problem — an oversized prompt lands here too, and misreading that
        // as a dead key would send the user off to mint a replacement for nothing. Only the API's
        // own key markers count.
        400 ->
            ApiKeyDiagnosis
                .Rejected(ApiKeyFailure.INVALID)
                .takeIf {
                    haystack.contains("api_key_invalid") ||
                        haystack.contains("api key not valid") ||
                        haystack.contains("api key expired")
                }

        403 -> ApiKeyDiagnosis.Rejected(ApiKeyFailure.FORBIDDEN)

        429 ->
            if (isDailyQuota(haystack, parsed.retryDelaySeconds)) {
                ApiKeyDiagnosis.QuotaDaily
            } else {
                ApiKeyDiagnosis.QuotaMinute(
                    retryDelaySeconds = parsed.retryDelaySeconds,
                    tokenQuotaValue = inputTokenQuotaValue(parsed.parsed),
                )
            }

        else -> null
    }
}

/**
 * Free-tier quota ids carry their window in the name — `GenerateRequestsPerDayPerProjectPerModel`
 * against `...PerMinute...`. Checked against the whole error body rather than the decoded
 * violations so a renamed or newly nested field does not silently fall through to the wrong branch.
 */
private fun isDailyQuota(
    haystack: String,
    retryDelaySeconds: Long?,
): Boolean =
    haystack.contains("perday") ||
        haystack.contains("per_day") ||
        haystack.contains("per day") ||
        haystack.contains("daily") ||
        (retryDelaySeconds != null && retryDelaySeconds > DAILY_DELAY_THRESHOLD_SECONDS)

/**
 * The per-minute *token* budget named in a quota violation, when there is one.
 *
 * Matched on input+token so a request-count violation (which says nothing about prompt size) is
 * not mistaken for a size limit.
 */
private fun inputTokenQuotaValue(response: GeminiErrorResponse?): Int? =
    response
        ?.error
        ?.details
        ?.asSequence()
        ?.flatMap { it.violations.orEmpty().asSequence() }
        ?.firstOrNull { violation ->
            val haystack = "${violation.quotaId.orEmpty()} ${violation.quotaMetric.orEmpty()}".lowercase()
            haystack.contains("input") && haystack.contains("token")
        }?.quotaValue
        ?.toIntOrNull()
