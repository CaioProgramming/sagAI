package com.ilustris.sagai.core.ai.services

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.StreamingState
import com.ilustris.sagai.features.onboarding.data.OnboardingPrompts
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
        @PublishedApi internal val promptService: PromptService,
    ) {
        @OptIn(ExperimentalCoroutinesApi::class)
        inline fun <reified T> synthesizeReasoning(
            sourceFlow: Flow<StreamingState<T>>,
            context: String,
            conversationStyle: String? = null,
            targetLanguage: String = "Portuguese",
            showReasoning: Boolean = true,
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
                                            synthesizeNow(
                                                lastReasoning,
                                                context,
                                                conversationStyle,
                                                targetLanguage,
                                                this@channelFlow,
                                            )
                                        }
                                }
                            } else {
                                send(state)
                            }
                        }

                        is StreamingState.Success -> {
                            if (showReasoning && lastReasoning.isNotBlank()) {
                                synthesizeNow(
                                    lastReasoning,
                                    context,
                                    conversationStyle,
                                    targetLanguage,
                                    this,
                                )
                            }
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
            conversationStyle: String?,
            targetLanguage: String,
            scope: ProducerScope<StreamingState<T>>,
        ) {
            try {
                val style =
                    conversationStyle
                        ?: promptService.buildRemotePrompt(
                            OnboardingPrompts.DEFAULT_ROLE_BLUEPRINT,
                            logEnabled = false,
                        )

                val sanitizedReasoning = sanitizeReasoning(reasoning).takeLast(400)

                val variables =
                    mapOf(
                        "context" to context,
                        "thoughtStream" to sanitizedReasoning,
                        "conversationStyle" to style,
                        "language" to targetLanguage,
                    )

                val prompt =
                    promptService.buildRemotePrompt(
                        REASONING_SYNTHESIZER_BLUEPRINT,
                        variables,
                        logEnabled = false,
                    )

                val translation =
                    gemmaClient.generateText(
                        prompt = prompt,
                        requirement = GemmaClient.ModelRequirement.TINY,
                        logEnabled = false,
                    )

                if (translation != null) {
                    scope.send(
                        StreamingState.Reasoning(
                            translation.trim().removeSurrounding("\""),
                        ),
                    )
                    delay(3.seconds)
                }
            } catch (e: Exception) {
                Timber.w("Failed to synthesize reasoning: ${e.message}")
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
        }
    }
