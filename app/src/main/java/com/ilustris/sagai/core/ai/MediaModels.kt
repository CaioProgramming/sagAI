package com.ilustris.sagai.core.ai

import com.ilustris.sagai.core.ai.key.ApiKeyDiagnosis
import com.ilustris.sagai.core.ai.key.QuotaStatus
import com.ilustris.sagai.core.ai.key.QuotaStatusService
import com.ilustris.sagai.core.ai.key.classifyApiKeyFailure
import com.ilustris.sagai.core.network.GeminiHttpException
import com.ilustris.sagai.core.services.RemoteConfigService
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/** One `model_configs` entry: a bare model name, or `{ model, enabled, availableModels }`. */
internal data class TierModelConfig(
    val enabled: Boolean,
    val primary: String,
    /** Best first. The `availableModels` array when set, else just [primary]. Never empty. */
    val candidates: List<String>,
)

/** Null when the entry is missing or malformed. Shared by text tiers and media tiers. */
internal fun Any?.toTierModelConfig(): TierModelConfig? =
    when (this) {
        is String -> {
            val model = replace("models/", "").takeIf { it.isNotBlank() }
            model?.let { TierModelConfig(enabled = true, primary = it, candidates = listOf(it)) }
        }

        is Map<*, *> -> {
            val primary = (this["model"] as? String)?.replace("models/", "")?.takeIf { it.isNotBlank() }
            primary?.let {
                val available =
                    (this["availableModels"] as? List<*>)
                        ?.mapNotNull { entry -> (entry as? String)?.replace("models/", "") }
                        ?.filter { entry -> entry.isNotBlank() }
                        ?.takeIf { entries -> entries.isNotEmpty() }
                TierModelConfig(
                    enabled = this["enabled"] as? Boolean ?: true,
                    primary = it,
                    candidates = available ?: listOf(it),
                )
            }
        }

        else -> {
            null
        }
    }

/**
 * Tiers for non-text generation, configured in `model_configs` next to the text tiers but never
 * mixed with them: there is no LOW fallback for an image or a voice, so these only rotate within
 * their own candidates.
 */
enum class MediaRequirement(
    /** The single-model Remote Config key this tier replaced, read while `model_configs` lacks it. */
    val legacyFlag: String?,
) {
    IMAGE("imageGenModelPremium"),
    AUDIO("audioGenModel"),

    /** Its legacy model lived inside `book_audio_config`, so callers pass it in instead. */
    TRANSCRIBE(null),

    /** RAG's embedding model — see [com.ilustris.sagai.core.ai.rag.EmbeddingClient]. No legacy flag. */
    EMBEDDING(null),
}

@Singleton
class MediaModelResolver
    @Inject
    constructor(
        private val remoteConfigService: RemoteConfigService,
        private val quotaStatusService: QuotaStatusService,
        private val auditLogger: AIAuditLogger,
    ) {
        /** Configured candidates, best first; empty when the tier is disabled or not configured at all. */
        suspend fun candidates(
            requirement: MediaRequirement,
            legacyModel: String? = null,
        ): List<String> {
            val config =
                remoteConfigService
                    .getJsonMapStringAny(MODEL_CONFIGS_KEY)
                    ?.get(requirement.name)
                    .toTierModelConfig()
            if (config != null) return if (config.enabled) config.candidates else emptyList()

            val legacy = legacyModel ?: requirement.legacyFlag?.let { remoteConfigService.getString(it) }
            return listOfNotNull(legacy?.replace("models/", "")?.takeIf { it.isNotBlank() })
        }

        /**
         * [requirement]'s quota state, scoped to its own candidates — the pre-flight check a caller
         * runs before offering to generate at all, mirroring [AIClient.tierQuotaStatus] for the text
         * tiers. Only reports [QuotaStatus.DailyExhausted] once every candidate is spent for the day.
         */
        suspend fun tierQuotaStatus(
            requirement: MediaRequirement,
            legacyModel: String? = null,
        ): Flow<QuotaStatus> = quotaStatusService.statusForModels(candidates(requirement, legacyModel))

        /**
         * Runs [block] against [requirement]'s candidates in order, skipping ones already spent for
         * the day. A 503 or a daily-quota 429 moves on to the next candidate (the latter also
         * recording that model as spent); any other failure, or running out of candidates, throws.
         */
        suspend fun <T> withRotation(
            requirement: MediaRequirement,
            legacyModel: String? = null,
            block: suspend (model: String) -> T,
        ): T {
            val candidates = candidates(requirement, legacyModel)
            if (candidates.isEmpty()) error("No model configured for ${requirement.name}")

            val usable = candidates.filterNot { quotaStatusService.isModelDailyExhausted(it) }
            if (usable.isEmpty()) {
                val until = candidates.mapNotNull { quotaStatusService.dailyExhaustionFor(it) }.minOrNull() ?: 0L
                auditLogger.record(
                    AIAuditSnapshot.error(
                        model = candidates.first(),
                        blueprintKey = null,
                        dataType = requirement.name,
                        errorMessage = "Every ${requirement.name} candidate is daily-exhausted: $candidates",
                        responseTimeMs = 0,
                        systemInstruction = null,
                        sentVariables = null,
                    ),
                )
                throw QuotaExhaustedException(until = until, model = candidates.first())
            }

            var lastFailure: GeminiHttpException? = null
            for (model in usable) {
                val attemptStart = System.currentTimeMillis()
                try {
                    return block(model)
                } catch (e: GeminiHttpException) {
                    lastFailure = e
                    auditLogger.record(
                        AIAuditSnapshot.error(
                            model = model,
                            blueprintKey = null,
                            dataType = requirement.name,
                            errorMessage = e.message ?: "HTTP ${e.code}",
                            responseTimeMs = System.currentTimeMillis() - attemptStart,
                            systemInstruction = null,
                            sentVariables = null,
                        ),
                    )
                    val rotate =
                        when {
                            e.code == 503 -> {
                                true
                            }

                            // "limit: 0" is a model the key's plan doesn't include (every image model
                            // on the free tier), not a spent allowance — recording it as exhausted
                            // would hide the billing notice behind a fake "come back tomorrow".
                            e.code == 429 && e.errorBody.contains("limit: 0") -> {
                                true
                            }

                            classifyApiKeyFailure(e) == ApiKeyDiagnosis.QuotaDaily -> {
                                quotaStatusService.reportDailyExhausted(model)
                                true
                            }

                            else -> {
                                false
                            }
                        }
                    if (!rotate) throw e
                    Timber.tag(TAG).w("${requirement.name}: $model failed (${e.code}), trying the next candidate.")
                }
            }
            throw checkNotNull(lastFailure)
        }

        private companion object {
            const val MODEL_CONFIGS_KEY = "model_configs"
            const val TAG = "MediaModelResolver"
        }
    }
