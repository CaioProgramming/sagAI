package com.ilustris.sagai.core.ai.services

import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.AIClient
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ModelRequirement
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.key.QuotaStatusService
import com.ilustris.sagai.core.ai.key.UserApiKeyStore
import com.ilustris.sagai.core.ai.model.LoadingLines
import com.ilustris.sagai.core.ai.model.ReasoningFallbacks
import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.prepareFromSplitPrompt
import com.ilustris.sagai.core.database.source.AIAuditLogDao
import com.ilustris.sagai.core.services.AgeVerificationService
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.features.newsaga.data.model.Genre
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReasoningSynthesizerService
    @Inject
    constructor(
        @PublishedApi internal val gemmaClient: GemmaClient,
        promptService: PromptService,
        remoteConfigService: RemoteConfigService,
        ageVerificationService: AgeVerificationService,
        aiAuditLogDao: AIAuditLogDao,
        userApiKeyStore: UserApiKeyStore,
        quotaStatusService: QuotaStatusService,
        modelCatalog: com.ilustris.sagai.core.ai.ModelCatalog,
        apiUsageTracker: com.ilustris.sagai.core.ai.key.ApiUsageTracker,
        modelFallbackNotifier: com.ilustris.sagai.core.ai.ModelFallbackNotifier,
        @PublishedApi internal val genreConfigService: GenreConfigService,
        @PublishedApi internal val stringResourceHelper: com.ilustris.sagai.core.utils.StringResourceHelper,
    ) : AIClient(
            remoteConfigService,
            promptService,
            ageVerificationService,
            aiAuditLogDao,
            userApiKeyStore,
            quotaStatusService,
            modelCatalog,
            apiUsageTracker,
            modelFallbackNotifier,
        ) {
        /**
         * Holds the screen while [sourceFlow] runs, with lines written for this request.
         *
         * The holding lines come from a second, tiny generation fired alongside the real one, and
         * the configured pool in `reasoning_fallbacks` covers the couple of seconds until it lands
         * (and the whole request if it never does). It costs one extra call per generation, which
         * is the deliberate trade: a fixed pool becomes recognisable within a few sessions, and
         * once it does the screen stops reading as the model working on *this* and starts reading
         * as an animation.
         *
         * This used to summarise the model's own thought stream instead, one call per chunk. That
         * only ever worked while the API sent thoughts back mid-stream; the replies that matter
         * are JSON, which cannot be shown half-written, so they are generated synchronously now
         * and there is no stream of thoughts to summarise.
         *
         * @param context what is being generated, as a short phrase — the task.
         * @param details what the user actually asked for, when the caller has it. Passed as
         *   variables rather than as the assembled prompt on purpose: the real prompt carries the
         *   blueprint, the lore and the history, which is a great many input tokens to spend on a
         *   decoration.
         */
        @OptIn(ExperimentalCoroutinesApi::class)
        inline fun <reified T> synthesizeReasoning(
            sourceFlow: Flow<StreamingState<T>>,
            context: String,
            showReasoning: Boolean = true,
            genre: Genre? = null,
            details: String? = null,
        ): Flow<StreamingState<T>> =
            channelFlow {
                val terminal = AtomicBoolean(false)
                val pool = MutableStateFlow<List<String>>(emptyList())

                if (showReasoning) {
                    launch { holdWithLoadingLines(genre, pool, this@channelFlow, terminal) }
                    launch { fillLoadingLines(context, details, genre, pool, terminal) }
                }

                sourceFlow.collect { state ->
                    when (state) {
                        is StreamingState.Reasoning -> {
                            if (!showReasoning) send(state)
                        }

                        is StreamingState.Success -> {
                            terminal.set(true)
                            send(state)
                        }

                        is StreamingState.Error -> {
                            terminal.set(true)
                            send(state)
                        }
                    }
                }

                // Whatever was written for this request dies with it. Reusing it on the next one
                // is how a pool stops matching what is on screen.
                terminal.set(true)
                pool.value = emptyList()
            }

        /**
         * Rotates whatever is in [pool] until the request finishes.
         *
         * Seeded from Remote Config so there is something on screen at frame zero, then swaps to
         * the generated lines the moment they arrive rather than at the next tick.
         *
         * Also listens for [modelFallbackNotifier] the whole time: a 503 mid-request means the
         * rotation the user has been watching just stopped meaning anything (the model behind it
         * got swapped out), and letting it keep cycling unrelated flavor lines while the request
         * quietly restarts elsewhere reads as the app not noticing its own hiccup. A fallback
         * interrupts whatever line is currently showing — even mid-[ROTATION_MS] — and holds
         * `R.string.generation_taking_longer` up for [TAKING_LONGER_MS] before the normal rotation
         * resumes.
         * Global rather than scoped to this one request's own model: [modelFallbackNotifier] fires
         * for any generation in the app, so a concurrent, unrelated fallback can occasionally light
         * this up too. Rare in practice (fallbacks are themselves rare) and harmless when it
         * happens — the message is true of *a* generation, just not necessarily this exact one —
         * so it isn't worth the extra plumbing a per-request tag would take.
         */
        @PublishedApi
        internal suspend fun <T> holdWithLoadingLines(
            genre: Genre?,
            pool: MutableStateFlow<List<String>>,
            scope: ProducerScope<StreamingState<T>>,
            terminal: AtomicBoolean,
        ) {
            if (terminal.get() || scope.isClosedForSend) return

            val takingLongerUntil = MutableStateFlow(0L)
            val fallbackListener =
                scope.launch {
                    modelFallbackNotifier.fellBackToSubstitute.collect {
                        takingLongerUntil.value = System.currentTimeMillis() + TAKING_LONGER_MS
                    }
                }

            try {
                pool.value = configuredPool(genre)
                var previous: String? = null
                while (!terminal.get() && !scope.isClosedForSend) {
                    val deadline = takingLongerUntil.value
                    val now = System.currentTimeMillis()
                    if (deadline > now) {
                        scope.send(StreamingState.Reasoning(stringResourceHelper.getString(R.string.generation_taking_longer)))
                        // Woken early by a fresh fallback pushing the deadline out further, same as
                        // the normal-rotation wait below is woken early by the pool changing.
                        withTimeoutOrNull(deadline - now) {
                            takingLongerUntil.first { it > deadline }
                        }
                        previous = null
                        continue
                    }
                    val current = pool.value
                    if (current.isEmpty()) {
                        // No configured pool: nothing to show until the generated one lands.
                        withTimeoutOrNull(ROTATION_MS) { pool.first { it.isNotEmpty() } }
                        continue
                    }
                    val next =
                        current.filterNot { it == previous }.randomOrNull() ?: current.random()
                    previous = next
                    scope.send(StreamingState.Reasoning(next))
                    withTimeoutOrNull(ROTATION_MS) {
                        combine(pool, takingLongerUntil) { p, until -> p to until }
                            .first { (p, until) -> p != current || until > System.currentTimeMillis() }
                    }
                }
            } catch (_: CancellationException) {
                // The request finished or the collector went away — nothing to clean up.
            } catch (e: Exception) {
                Timber.e("Error rotating loading lines: ${e.message}")
            } finally {
                fallbackListener.cancel()
            }
        }

        /** Asks for lines about this specific request and hands them to the rotation. */
        @PublishedApi
        internal suspend fun fillLoadingLines(
            context: String,
            details: String?,
            genre: Genre?,
            pool: MutableStateFlow<List<String>>,
            terminal: AtomicBoolean,
        ) {
            // A holding line is never worth someone's last request of the day.
            if (quotaStatusService.activeDailyBlock() != null) return

            try {
                val aesthetic =
                    if (genre != null) {
                        genreConfigService.aesthetic(genre)
                    } else {
                        genreConfigService.formatGenreAesthetics()
                    }

                val promptSplit =
                    buildBlueprintPrompt(
                        remoteConfigKey = LOADING_LINES_BLUEPRINT,
                        variables =
                            mapOf(
                                "task" to context,
                                "request" to details.orEmpty(),
                                "language" to getLanguage(true),
                                "aesthetic" to aesthetic,
                            ),
                        logEnabled = true,
                    )

                val generated =
                    executeBlueprintGeneration<LoadingLines>(
                        promptSplit = promptSplit,
                        requirement = ModelRequirement.MINIMAL,
                        temperatureRandomness = 1f,
                        logEnabled = true,
                        reportsQuota = false,
                    )

                val lines =
                    generated
                        ?.lines
                        ?.map { it.trim().removeSurrounding("\"") }
                        ?.filter { it.isNotBlank() }
                        .orEmpty()

                if (lines.isNotEmpty() && !terminal.get()) pool.value = lines
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.w("Could not write loading lines: ${e.message}, keeping the configured pool")
            }
        }

        /** The static pool from `reasoning_fallbacks`, by genre when there is one. */
        private suspend fun configuredPool(genre: Genre?): List<String> =
            try {
                val fallbacks =
                    remoteConfigService.getJson<ReasoningFallbacks>(REASONING_FALLBACKS_KEY)
                if (genre != null && fallbacks?.genres?.containsKey(genre.name) == true) {
                    fallbacks.genres[genre.name]
                } else {
                    fallbacks?.default
                }.orEmpty()
            } catch (e: Exception) {
                Timber.e("Error fetching fallbacks: ${e.message}")
                emptyList()
            }

        private suspend inline fun <reified T> executeBlueprintGeneration(
            promptSplit: SplitPrompt,
            requirement: ModelRequirement,
            requireTranslation: Boolean = true,
            describeOutput: Boolean = true,
            filterOutputFields: List<String> = emptyList(),
            userInteraction: Boolean = false,
            temperatureRandomness: Float = .5f,
            logEnabled: Boolean = true,
            reportsQuota: Boolean = true,
        ): T? {
            val prepared =
                prepareFromSplitPrompt<T>(
                    promptSplit = promptSplit,
                    requirement = requirement,
                    requireTranslation = requireTranslation,
                    describeOutput = describeOutput,
                    filterOutputFields = filterOutputFields,
                    userInteraction = userInteraction,
                )
            return gemmaClient.executePrepared(
                prepared = prepared,
                requirement = requirement,
                temperatureRandomness = temperatureRandomness,
                logEnabled = logEnabled,
                reportsQuota = reportsQuota,
            )
        }

        companion object {
            const val LOADING_LINES_BLUEPRINT = "reasoning_synthesizer_blueprint"
            const val REASONING_FALLBACKS_KEY = "reasoning_fallbacks"

            /**
             * How long each holding line stays up before the next one replaces it.
             *
             * 1.5s (long enough to read the two-to-five words, but not to register as a pause)
             * turned out too fast in practice — lines were swapping out mid-read. 8s paces it to
             * an actual reading cadence for a short phrase, not just the minimum time the eye needs
             * to pass over the words.
             */
            @PublishedApi
            internal val ROTATION_MS = 8000L

            /** How long a fallback's "taking longer" line holds the screen before normal rotation resumes. */
            @PublishedApi
            internal val TAKING_LONGER_MS = 15_000L
        }
    }
