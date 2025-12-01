package com.ilustris.sagai.features.playthrough

import android.content.Context
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.model.PlaythroughGen
import com.ilustris.sagai.core.ai.prompts.PlaythroughPrompts
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.saga.chat.repository.SagaRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class PlaythroughUseCaseImpl
    @Inject
    constructor(
        private val sagaRepository: SagaRepository,
        private val gemmaClient: GemmaClient,
        @ApplicationContext
        private val context: Context,
    ) : PlaythroughUseCase {
        override suspend fun invoke(): RequestResult<PlayThroughData> =
            try {
                val sagas = sagaRepository.getChats().first()

                assert(sagas.isNotEmpty())

                // Extract genres from all sagas
                val genres = sagas.map { it.data.genre }

                // Calculate total playtime
                val totalPlaytimeMs = sagas.sumOf { it.data.playTimeMs }
                val formattedTime = totalPlaytimeMs.toPlaytimeFormat()

                // Get emotional summaries from ended sagas or most-played active saga
                val endedSagas =
                    sagas.filter { it.data.isEnded && !it.data.emotionalReview.isNullOrEmpty() }
                val activeSagas =
                    sagas.filter { !it.data.isEnded }.sortedByDescending { it.data.playTimeMs }

                val emotionalSummaries =
                    if (endedSagas.isNotEmpty()) {
                        endedSagas.map {
                            "${it.data.title}: ${it.data.emotionalReview}"
                        }
                    } else {
                        activeSagas
                            .firstOrNull()
                            ?.data
                            ?.emotionalReview
                            ?.let { listOf(it) } ?: emptyList()
                    }

                val playtimeReview =
                    if (emotionalSummaries.isNotEmpty()) {
                        gemmaClient.generate<PlaythroughGen>(
                            PlaythroughPrompts.extractPlaythroughReview(emotionalSummaries, totalPlaytimeMs / 60000) // Convert ms to minutes
                        ) ?: PlaythroughGen("A Saga Unfolds", context.getString(R.string.continue_to_play))
                    } else {
                        PlaythroughGen("A Saga Unfolds", context.getString(R.string.continue_to_play))
                    }

                RequestResult.Success(
                    PlayThroughData(
                        totalPlayTime = formattedTime,
                        totalPlaytimeMs = totalPlaytimeMs,
                        playtimeReview = playtimeReview,
                        genres = genres
                    ),
                )
            } catch (e: Exception) {
                RequestResult.Error(e)
            }

        override suspend fun getPlaythroughCardPrompt(): RequestResult<DynamicSagaPrompt> = executeRequest {
            val prompt = PlaythroughPrompts.playthroughCallToActionPrompt()
            gemmaClient.generate<DynamicSagaPrompt>(
                prompt,
                temperatureRandomness = .5f,
                requireTranslation = true,
            )!!
        }
    }
