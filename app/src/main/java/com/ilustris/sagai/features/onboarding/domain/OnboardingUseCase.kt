package com.ilustris.sagai.features.onboarding.domain

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.onboarding.data.OnboardingContent
import com.ilustris.sagai.features.onboarding.data.OnboardingType

interface OnboardingUseCase {
    suspend fun shouldShow(type: OnboardingType): Boolean

    suspend fun getContent(
        type: OnboardingType,
        genre: Genre? = null,
    ): RequestResult<OnboardingContent>

    suspend fun markSeen(type: OnboardingType)
}
