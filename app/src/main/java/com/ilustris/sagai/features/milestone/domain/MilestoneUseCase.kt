package com.ilustris.sagai.features.milestone.domain

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone

interface MilestoneUseCase {
    suspend fun generateCongratsMessage(
        milestone: SagaMilestone,
        saga: SagaContent,
    ): RequestResult<String>
}
