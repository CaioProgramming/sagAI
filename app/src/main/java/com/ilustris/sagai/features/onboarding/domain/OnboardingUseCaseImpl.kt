package com.ilustris.sagai.features.onboarding.domain

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.datastore.DataStorePreferences
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.onboarding.data.OnboardingConfig
import com.ilustris.sagai.features.onboarding.data.OnboardingContent
import com.ilustris.sagai.features.onboarding.data.OnboardingPrompts
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import javax.inject.Inject

class OnboardingUseCaseImpl
    @Inject
    constructor(
        private val remoteConfigService: RemoteConfigService,
        private val gemmaClient: GemmaClient,
        private val dataStore: DataStorePreferences,
        private val promptService: PromptService,
        private val genreConfigService: GenreConfigService,
    ) : OnboardingUseCase {
        override suspend fun shouldShow(type: OnboardingType): Boolean =
            when (type) {
                OnboardingType.GAMEPLAY_GUIDE -> {
                    dataStore.getBooleanNow(type.preferenceKey)
                }

                else -> {
                    dataStore
                        .getBooleanNow(type.preferenceKey, false)
                        .not()
                }
            }

        override suspend fun getContent(
            type: OnboardingType,
            genre: Genre?,
        ): RequestResult<OnboardingContent> =
            executeRequest {
                val config = remoteConfigService.getJson<OnboardingConfig>(type.configKey)!!

                val persona =
                    if (type == OnboardingType.GAMEPLAY_GUIDE && genre != null) {
                        genreConfigService.conversationBlueprint(genre)
                    } else {
                        promptService.buildRemotePrompt(
                            OnboardingPrompts.DEFAULT_ROLE_BLUEPRINT,
                            emptyMap<String, String>(),
                        )
                    }

                val prompt = OnboardingPrompts.getOnboardingPrompt(promptService, config, persona)
                val content =
                    gemmaClient.generate<OnboardingContent>(
                        prompt = prompt,
                        requirement = GemmaClient.ModelRequirement.LOW,
                        blueprintKey = OnboardingPrompts.ONBOARDING_BLUEPRINT,
                    )

                content!!
            }

        override suspend fun markSeen(type: OnboardingType) {
            if (type == OnboardingType.GAMEPLAY_GUIDE) return
            dataStore.setBoolean(type.preferenceKey, true)
        }
    }
