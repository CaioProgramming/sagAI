package com.ilustris.sagai.features.onboarding.domain

import com.ilustris.sagai.core.ai.GemmaClient
import com.ilustris.sagai.core.ai.model.mergeInstructions
import com.ilustris.sagai.core.ai.ModelRequirement
import com.ilustris.sagai.core.ai.services.GenreConfigService
import com.ilustris.sagai.core.ai.services.PromptService
import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.data.executeRequest
import com.ilustris.sagai.core.datastore.DataStorePreferences
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.onboarding.data.OnboardingPrompts
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import com.ilustris.sagai.features.onboarding.data.model.OnboardingConfig
import com.ilustris.sagai.features.onboarding.data.model.OnboardingContent
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
                        genreConfigService.conversationInstructions(genre)
                    } else {
                        promptService
                            .buildSplitBlueprint(
                                OnboardingPrompts.DEFAULT_ROLE_BLUEPRINT,
                            ).renderInstructions()
                    }

                val prompt = OnboardingPrompts.getOnboardingPrompt(promptService, config)
                val content =
                    gemmaClient.generate<OnboardingContent>(
                        promptSplit = prompt.mergeInstructions(persona),
                        requirement = ModelRequirement.MINIMAL,
                    )

                content ?: getFallbackContent(type)
            }

        private suspend fun getFallbackContent(type: OnboardingType): OnboardingContent {
            val fallbacks =
                remoteConfigService.getJsonMapString(
                    OnboardingPrompts.ONBOARDING_FALLBACKS,
                    OnboardingContent::class.java,
                )
            return fallbacks?.get(type.name) ?: OnboardingContent()
        }

        override suspend fun markSeen(type: OnboardingType) {
            if (type == OnboardingType.GAMEPLAY_GUIDE) return
            dataStore.setBoolean(type.preferenceKey, true)
        }
    }
