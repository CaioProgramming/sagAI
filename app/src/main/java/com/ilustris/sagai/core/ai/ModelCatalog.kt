package com.ilustris.sagai.core.ai

import com.ilustris.sagai.core.ai.model.GeminiThinkingConfig
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

        /** Per-model, the thinking levels that model has taught us it rejects with a 400. */
        private val unsupportedLevelsByModel = ConcurrentHashMap<String, MutableSet<String>>()

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
         * Whether [model] accepts `thinkingLevel: [level]`.
         *
         * Learned from the API's own rejection rather than a hardcoded list: the set of models
         * offering a given level changes with every release, and a stale list would either
         * downgrade tiers that did not need it or keep resending a level the model refuses.
         *
         * This used to only ever track `minimal`, back when the sole known gap was some Gemini
         * models omitting that one level. It generalized once a model swap could put `high` in
         * front of a model nobody had ever asked for `high` before — the fallback Gemma model a
         * 503 retry can now fall over to serves the LOW tier in production, which only ever
         * requests `minimal` from it, so whether it tolerates `high` is untested territory the
         * first time it actually happens.
         */
        /**
         * Whether [model] accepts inline audio in a text-generation request. Gemini models do;
         * Gemma models on the Gemini API are text + image only.
         */
        fun supportsAudioInput(model: String): Boolean = !model.replace("models/", "").startsWith("gemma", ignoreCase = true)

        fun supportsThinkingLevel(
            model: String,
            level: String,
        ): Boolean = level !in unsupportedLevelsByModel[model.replace("models/", "")].orEmpty()

        fun markThinkingLevelUnsupported(
            model: String,
            level: String,
        ) {
            val normalized = model.replace("models/", "")
            val rejected = unsupportedLevelsByModel.getOrPut(normalized) { ConcurrentHashMap.newKeySet() }
            if (rejected.add(level)) {
                Timber.tag(TAG).i("$normalized rejects '$level' thinking.")
            }
        }

        /**
         * The `thinkingConfig` a request to [model] should carry for the [requested] level, or null
         * to omit it.
         *
         * Resolved at assembly time rather than trusting the level frozen into the params, because
         * the params are fixed before the retry loop starts — so a rejection recorded mid-flight
         * would otherwise be re-sent verbatim on every remaining attempt and fail identically.
         *
         * Families take their reasoning control differently (measured against the API, not assumed):
         *  - Gemini 2.5 answers `thinkingLevel` with a 400 and is steered by `thinkingBudget`. Only
         *    `minimal` has a counterpart (a zero budget); any other level leaves the model's own
         *    default rather than inventing a level-to-tokens table.
         *  - Gemma 4 accepts `minimal` and nothing above it — `low` is a 400 — so every level clamps
         *    to it, and the rejection never has to be paid for with a failed request.
         *  - Everything else (Gemini 3) takes the level, stepping down the ladder past any level this
         *    exact model has taught us it rejects. That learned path stays as the net for exceptions
         *    inside a family, which the family checks above are too coarse to know about.
         */
        fun thinkingConfig(
            model: String,
            requested: String?,
        ): GeminiThinkingConfig? {
            if (requested == null) return null
            val name = model.replace("models/", "")
            return when {
                name.startsWith("gemini-2.5", ignoreCase = true) -> {
                    GeminiThinkingConfig(thinkingBudget = 0).takeIf { requested == MINIMAL }
                }

                name.startsWith("gemma", ignoreCase = true) -> {
                    GeminiThinkingConfig(includeThoughts = false, thinkingLevel = MINIMAL)
                }

                else -> {
                    val level = steppedLevel(model, requested)
                    // No point asking for summaries of reasoning we just asked the model not to do —
                    // `minimal` returns zero thought tokens.
                    GeminiThinkingConfig(includeThoughts = level != MINIMAL, thinkingLevel = level)
                }
            }
        }

        private fun steppedLevel(
            model: String,
            requested: String,
        ): String {
            // `minimal` is the one direction proven in production: some models omit it from the low
            // end entirely, and the safe fallback is one step up, not a step down.
            if (requested == MINIMAL && !supportsThinkingLevel(model, MINIMAL)) return "low"

            val startIndex = THINKING_LEVEL_LADDER.indexOf(requested)
            if (startIndex < 0) return requested
            return THINKING_LEVEL_LADDER
                .drop(startIndex)
                .firstOrNull { supportsThinkingLevel(model, it) }
                ?: THINKING_LEVEL_LADDER.last()
        }

        private fun cached(): Map<String, Int>? = limits?.takeIf { it.isNotEmpty() }

        companion object {
            private const val TAG = "📐 ModelCatalog"
            private const val MINIMAL = "minimal"

            /**
             * Fallback only. Descends from the Gemma free tier's per-minute token budget, which is
             * why it looks so small next to any model's real context window.
             */
            const val DEFAULT_INPUT_TOKEN_LIMIT = 16000
        }
    }
