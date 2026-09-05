package com.ilustris.sagai.core.ai.services

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
         */
        @PublishedApi
        internal suspend fun <T> holdWithLoadingLines(
            genre: Genre?,
            pool: MutableStateFlow<List<String>>,
            scope: ProducerScope<StreamingState<T>>,
            terminal: AtomicBoolean,
        ) {
            if (terminal.get() || scope.isClosedForSend) return

            try {
                pool.value = configuredPool(genre)
                var previous: String? = null
                while (!terminal.get() && !scope.isClosedForSend) {
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
                    withTimeoutOrNull(ROTATION_MS) { pool.first { it != current } }
                }
            } catch (_: CancellationException) {
                // The request finished or the collector went away — nothing to clean up.
            } catch (e: Exception) {
                Timber.e("Error rotating loading lines: ${e.message}")
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
             * Sized to the lines, which are two to five words: they are read in well under a
             * second, and holding a read line on screen is what made the old single sentence feel
             * frozen.
             */
            @PublishedApi
            internal val ROTATION_MS = 1500L
        }
    }
