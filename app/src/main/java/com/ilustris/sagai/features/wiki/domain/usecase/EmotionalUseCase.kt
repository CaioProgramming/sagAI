package com.ilustris.sagai.features.wiki.domain.usecase

import com.ilustris.sagai.core.data.RequestResult

interface EmotionalUseCase {
    suspend fun generateEmotionalReview(content: List<String>): RequestResult<Exception, String>
}
