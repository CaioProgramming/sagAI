package com.ilustris.sagai.core.ai.services

import com.ilustris.sagai.core.ai.AIClient
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ModelRequirement
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.core.ai.model.ReasoningFallbacks
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
        @PublishedApi internal val genreConfigService: GenreConfigService,
    ) : AIClient(
            remoteConfigService,
            promptService,
            ageVerificationService,
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
                            synthesisJob?.cancel()
                            send(state)
                        }

                        is StreamingState.Error -> {
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
        ) {
            try {
                useFallback(genre, scope)

                val conversationStyle =
                    genre?.let {
                        genreConfigService.conversationInstructions(it)
                    }

                val sanitizedReasoning = sanitizeReasoning(reasoning).takeLast(400)

                val variables =
                    mapOf(
                        "context" to context,
                        "thoughtStream" to sanitizedReasoning,
                        "language" to targetLanguage,
                    )

                val prompt =
                    promptService.buildSplitBlueprint(
                        REASONING_SYNTHESIZER_BLUEPRINT,
                        variables,
                        logEnabled = false,
                    )

                val translation =
                    gemmaClient.generate<String>(
                        prompt = prompt.processedTemplate,
                        systemInstructions =
                            buildMap {
                                conversationStyle?.let {
                                    putAll(it)
                                }
                                putAll(prompt.renderInstructions())
                            },
                        requirement = ModelRequirement.MINIMAL,
                        logEnabled = false,
                        temperatureRandomness = 1f,
                    )

                if (translation != null) {
                    scope.send(
                        StreamingState.Reasoning(
                            translation.trim().removeSurrounding("\""),
                        ),
                    )
                    delay(3.seconds)
                } else {
                    Timber.w("AI Reasoning failed, using fallback...")
                    useFallback(genre, scope)
                }
            } catch (e: Exception) {
                Timber.w("Failed to synthesize reasoning: ${e.message}, using fallback...")
                useFallback(genre, scope)
            }
        }

        private suspend fun <T> useFallback(
            genre: Genre?,
            scope: ProducerScope<StreamingState<T>>,
        ) {
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
                    scope.send(StreamingState.Reasoning(it))
                }
            } catch (e: Exception) {
                Timber.e("Error fetching fallbacks: ${e.message}")
            }
        }

        fun sanitizeReasoning(text: String): String {
            // Remove JSON-like structures (braces and brackets content) which are often technical noise
            return text
                .replace(Regex("\\{[^}]*\\}|\\[[^]]*\\]"), "")
                .replace(Regex("\"\\w+\"\\s*:\\s*\"[^\"]*\""), "") // Remove "key": "value" pairs
                .replace(Regex("\"\\w+\"\\s*:\\s*[^,}]*"), "") // Remove "key": value pairs
                .replace(Regex("[,{}:]"), " ") // Remove remaining technical markers
                .replace(Regex("\\s+"), " ") // Cleanup whitespace
                .trim()
        }

        companion object {
            const val REASONING_SYNTHESIZER_BLUEPRINT = "reasoning_synthesizer_blueprint"
            const val REASONING_FALLBACKS_KEY = "reasoning_fallbacks"
        }
    }
