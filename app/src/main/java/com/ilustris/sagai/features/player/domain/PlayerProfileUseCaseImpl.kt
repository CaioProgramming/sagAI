package com.ilustris.sagai.features.player.domain

import com.google.gson.Gson
import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.ModelRequirement
import com.ilustris.sagai.core.ai.prompts.PlayerProfilePrompts
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.player.data.model.PlayerProfileData
import com.ilustris.sagai.features.player.data.repository.PlayerProfileRepository
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

class PlayerProfileUseCaseImpl
    @Inject
    constructor(
        private val playerProfileRepository: PlayerProfileRepository,
        private val userIdentityUseCase: UserIdentityUseCase,
        private val gemmaClient: GemmaClient,
        private val promptService: PromptService,
    ) : PlayerProfileUseCase {
        override fun observeProfile(): Flow<PlayerProfileData?> = playerProfileRepository.observeProfile()

        override suspend fun recordActInsight(
            saga: SagaContent,
            act: Act,
        ) {
            try {
                val currentProfile = playerProfileRepository.getProfile() ?: PlayerProfileData()
                val userName = userIdentityUseCase.getNameNow().ifBlank { "Jogador" }
                val currentProfileJson = Gson().toJson(currentProfile)
                val actInsight =
                    buildString {
                        append("Act ID: ${act.id}, Saga ID: ${act.sagaId}\n")
                        if (!act.emotionalReview.isNullOrBlank()) {
                            append("Emotional Review: ${act.emotionalReview}\n")
                        }
                        if (!act.content.isBlank()) {
                            append("Act Content: ${act.content}\n")
                        }
                    }

                val prompt =
                    PlayerProfilePrompts.generateActProfileUpdate(
                        promptService = promptService,
                        currentPlayerProfile = currentProfileJson,
                        actInsight = actInsight,
                        sagaGenre = saga.data.genre.name,
                        sagaId = saga.data.id,
                        actId = act.id,
                        userName = userName,
                    )

                val updated =
                    gemmaClient.generate<PlayerProfileData>(
                        promptSplit = prompt,
                        requirement = ModelRequirement.LOW,
                    )

                if (updated != null) {
                    playerProfileRepository.saveProfile(updated)
                } else {
                    Timber.w("PlayerProfileUseCase: Failed to generate profile update (result is null)")
                }
            } catch (e: Exception) {
                Timber.e(e, "PlayerProfileUseCase: Error recording act insight")
                // Non-fatal error: continue narrative progression
            }
        }

        override suspend fun recordSagaInsight(saga: SagaContent) {
            try {
                val currentProfile = playerProfileRepository.getProfile() ?: PlayerProfileData()
                val userName = userIdentityUseCase.getNameNow()

                val prompt =
                    PlayerProfilePrompts.generateSagaProfileUpdate(
                        promptService = promptService,
                        currentPlayerProfile = currentProfile,
                        saga = saga.data,
                        userName = userName,
                    )

                val updated =
                    gemmaClient.generate<PlayerProfileData>(
                        promptSplit = prompt,
                        requirement = ModelRequirement.HIGH,
                    )

                if (updated != null) {
                    playerProfileRepository.saveProfile(updated)
                } else {
                    Timber.w("PlayerProfileUseCase: Failed to generate saga insight update (result is null)")
                }
            } catch (e: Exception) {
                Timber.e(e, "PlayerProfileUseCase: Error recording saga insight")
            }
        }
    }
