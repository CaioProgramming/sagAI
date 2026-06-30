package com.ilustris.sagai.core.ai.services

import com.ilustris.sagai.core.ai.AIClient
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ModelRequirement
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.ReasoningFallbacks
import com.ilustris.sagai.core.ai.model.SplitPrompt
import com.ilustris.sagai.core.ai.prepareFromSplitPrompt
import com.ilustris.sagai.core.database.source.AIAuditLogDao
import com.ilustris.sagai.core.services.AgeVerificationService
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.features.newsaga.data.model.Genre
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

@Singleton
class ReasoningSynthesizerService
    @Inject
    constructor(
        @PublishedApi internal val gemmaClient: GemmaClient,
        promptService: PromptService,
        remoteConfigService: RemoteConfigService,
        ageVerificationService: AgeVerificationService,
        aiAuditLogDao: AIAuditLogDao,
        @PublishedApi internal val genreConfigService: GenreConfigService,
    ) : AIClient(
            remoteConfigService,
            promptService,
            ageVerificationService,
            aiAuditLogDao,
        ) {
        @OptIn(ExperimentalCoroutinesApi::class)
        inline fun <reified T> synthesizeReasoning(
            sourceFlow: Flow<StreamingState<T>>,
            context: String,
            showReasoning: Boolean = true,
            genre: Genre? = null,
        ): Flow<StreamingState<T>> =
            channelFlow {
                var synthesisJob: Job? = null
                var lastReasoning = ""
                val terminal = AtomicBoolean(false)
                val fallbackEmitted = AtomicBoolean(false)

                if (showReasoning) {
                    launch {
                        if (fallbackEmitted.compareAndSet(false, true)) {
                            useFallback(genre, this@channelFlow, terminal)
                        }
                    }
                }

                sourceFlow.collect { state ->
                    when (state) {
                        is StreamingState.Reasoning -> {
                            lastReasoning = state.chunk
                            if (showReasoning) {
                                if (synthesisJob?.isActive != true && lastReasoning.length > 50) {
                                    synthesisJob =
                                        launch {
                                            try {
                                                synthesizeNow(
                                                    lastReasoning,
                                                    context,
                                                    getLanguage(true),
                                                    this@channelFlow,
                                                    genre,
                                                    terminal,
                                                )
                                            } catch (_: CancellationException) {
                                                // Replaced by a newer reasoning chunk or flow completion.
                                            }
                                        }
                                }
                            } else {
                                send(state)
                            }
                        }

                        is StreamingState.Success -> {
                            terminal.set(true)
                            synthesisJob?.cancel()
                            send(state)
                        }

                        is StreamingState.Error -> {
                            terminal.set(true)
                            synthesisJob?.cancel()
                            send(state)
                        }
                    }
                }
            }

        @PublishedApi
        internal suspend fun <T> synthesizeNow(
            reasoning: String,
            context: String,
            targetLanguage: String,
            scope: ProducerScope<StreamingState<T>>,
            genre: Genre? = null,
            terminal: AtomicBoolean = AtomicBoolean(false),
        ) {
            if (terminal.get() || scope.isClosedForSend) return

            try {
                val aesthetic =
                    if (genre != null) {
                        genreConfigService.aesthetic(genre)
                    } else {
                        genreConfigService.formatGenreAesthetics()
                    }

                val sanitizedReasoning = sanitizeReasoning(reasoning).takeLast(400)

                val promptSplit =
                    buildBlueprintPrompt(
                        remoteConfigKey = REASONING_SYNTHESIZER_BLUEPRINT,
                        variables =
                            mapOf(
                                "context" to context,
                                "thoughtStream" to sanitizedReasoning,
                                "language" to targetLanguage,
                                "aesthetic" to aesthetic,
                            ),
                        logEnabled = false,
                    )
                val translation =
                    executeBlueprintGeneration<String>(
                        promptSplit = promptSplit,
                        requirement = ModelRequirement.MINIMAL,
                        temperatureRandomness = 1f,
                        logEnabled = false,
                    )

                if (terminal.get() || scope.isClosedForSend) return

                if (translation != null) {
                    scope.send(
                        StreamingState.Reasoning(
                            translation.trim().removeSurrounding("\""),
                        ),
                    )
                    delay(3.seconds)
                } else {
                    Timber.w("AI Reasoning failed, keeping fallback...")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (terminal.get() || scope.isClosedForSend) return
                Timber.w("Failed to synthesize reasoning: ${e.message}, keeping fallback...")
            }
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
            )
        }

        @PublishedApi
        internal suspend fun <T> useFallback(
            genre: Genre?,
            scope: ProducerScope<StreamingState<T>>,
            terminal: AtomicBoolean = AtomicBoolean(false),
        ) {
            if (terminal.get() || scope.isClosedForSend) return

            try {
                val fallbacks =
                    remoteConfigService.getJson<ReasoningFallbacks>(
                        REASONING_FALLBACKS_KEY,
                    )
                val fallbackMessage =
                    if (genre != null && fallbacks?.genres?.containsKey(genre.name) == true) {
                        fallbacks.genres[genre.name]?.randomOrNull()
                    } else {
                        fallbacks?.default?.randomOrNull()
                    }

                fallbackMessage?.let {
                    if (!terminal.get() && !scope.isClosedForSend) {
                        scope.send(StreamingState.Reasoning(it))
                    }
                }
            } catch (e: Exception) {
                Timber.e("Error fetching fallbacks: ${e.message}")
            }
        }

        fun sanitizeReasoning(text: String): String =
            text
                .replace(Regex("\\{[^}]*\\}|\\[[^]]*\\]"), "")
                .replace(Regex("\"\\w+\"\\s*:\\s*\"[^\"]*\""), "")
                .replace(Regex("\"\\w+\"\\s*:\\s*[^,}]*"), "")
                .replace(Regex("[,{}:]"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()

        companion object {
            const val REASONING_SYNTHESIZER_BLUEPRINT = "reasoning_synthesizer_blueprint"
            const val REASONING_FALLBACKS_KEY = "reasoning_fallbacks"
        }
    }
