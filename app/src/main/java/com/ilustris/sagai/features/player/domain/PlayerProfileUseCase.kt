package com.ilustris.sagai.features.player.domain

import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.player.data.model.PlayerProfileData
import kotlinx.coroutines.flow.Flow

interface PlayerProfileUseCase {
    fun observeProfile(): Flow<PlayerProfileData?>

    suspend fun recordActInsight(
        saga: SagaContent,
        act: Act,
    )

    suspend fun recordSagaInsight(saga: SagaContent)
}
