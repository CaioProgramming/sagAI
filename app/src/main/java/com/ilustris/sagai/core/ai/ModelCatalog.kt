package com.ilustris.sagai.core.ai

import com.ilustris.sagai.core.network.GeminiApiClient
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Context windows, straight from the API instead of a constant we maintain.
 *
 * The old [DEFAULT_INPUT_TOKEN_LIMIT] was traced from the Gemma free tier's 15k tokens-per-minute
 * budget, which is a *rate*, not a context window — the models themselves report 262144 (Gemma) and
 * 1048576 (Gemini). Once `model_configs` spans both families no single number is right for both, so
 * the ceiling is read from the same source of truth the token count already comes from.
 *
 * One `listModels` call covers every model, and the answer is static, so it is fetched once and
 * kept for the process. A failure is not fatal: the default stands in, and the next call retries.
 */
@Singleton
class ModelCatalog
    @Inject
    constructor(
        private val geminiApiClient: GeminiApiClient,
    ) {
        private val mutex = Mutex()

        @Volatile
        private var limits: Map<String, Int>? = null

        private val minimalUnsupported = ConcurrentHashMap.newKeySet<String>()

        private val perMinuteTokenBudget = ConcurrentHashMap<String, Int>()

        /**
         * @return the context window for [model], or [DEFAULT_INPUT_TOKEN_LIMIT] when the catalog
         *   could not be loaded. Never throws — a prompt check must not be what breaks generation.
         */
        suspend fun inputTokenLimit(
            model: String,
            apiKey: String,
        ): Int {
            val normalized = model.replace("models/", "")
            cached()?.get(normalized)?.let { return it }

            return mutex.withLock {
                cached()?.get(normalized) ?: run {
                    val fetched =
                        runCatching { geminiApiClient.listModels(apiKey) }
                            .onFailure {
                                Timber
                                    .tag(TAG)
                                    .w("Could not load model catalog: ${it.javaClass.simpleName}")
                            }.getOrNull()
                    if (!fetched.isNullOrEmpty()) limits = fetched
                    fetched?.get(normalized) ?: DEFAULT_INPUT_TOKEN_LIMIT
                }
            }
        }

        /**
         * The prompt ceiling to actually enforce for [model].
         *
         * The context window is the wrong number on its own. A free-tier key is capped by
         * `generate_content_free_tier_input_token_count` — 16000 tokens per minute for Gemma
         * against a 262144 context — so a prompt sized to the window is rejected every time, and
         * no retry delay fixes a single request that cannot fit inside a whole minute's budget.
         *
         * The budget is learned from the API's own quota violation rather than hardcoded, because
         * it differs per model, per tier, and Google moves it (this app's constant tracked it from
         * 15000 to 16000). Until a 429 teaches us, the context window stands.
         */
        suspend fun effectiveInputLimit(
            model: String,
            apiKey: String,
        ): Int {
            val context = inputTokenLimit(model, apiKey)
            val budget = perMinuteTokenBudget[model.replace("models/", "")]
            return if (budget != null) minOf(context, budget) else context
        }

        /** Records the per-minute token budget named in a quota violation for [model]. */
        fun recordPerMinuteTokenBudget(
            model: String,
            budget: Int,
        ) {
            val normalized = model.replace("models/", "")
            if (perMinuteTokenBudget.put(normalized, budget) != budget) {
                Timber.tag(TAG).i("$normalized per-minute input budget is $budget tokens.")
            }
        }

        /**
         * Whether [model] accepts `thinkingLevel: "minimal"`.
         *
         * Learned from the API's own rejection rather than a hardcoded list: the set of models
         * offering `minimal` changes with every release, and a stale list would either downgrade
         * tiers that did not need it or keep sending a level the model refuses.
         */
        fun supportsMinimalThinking(model: String): Boolean =
            model.replace("models/", "") !in minimalUnsupported

        fun markMinimalThinkingUnsupported(model: String) {
            val normalized = model.replace("models/", "")
            if (minimalUnsupported.add(normalized)) {
                Timber.tag(TAG).i("$normalized rejects minimal thinking — using low from now on.")
            }
        }

        private fun cached(): Map<String, Int>? = limits?.takeIf { it.isNotEmpty() }

        companion object {
            private const val TAG = "📐 ModelCatalog"

            /**
             * Fallback only. Descends from the Gemma free tier's per-minute token budget, which is
             * why it looks so small next to any model's real context window.
             */
            const val DEFAULT_INPUT_TOKEN_LIMIT = 16000
        }
    }
