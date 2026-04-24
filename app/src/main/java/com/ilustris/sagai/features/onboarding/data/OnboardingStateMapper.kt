package com.ilustris.sagai.features.onboarding.data

import androidx.compose.runtime.Composable
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.core.services.RemoteConfigService
import com.ilustris.sagai.core.utils.StringResourceHelper
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.onboarding.data.model.OnboardingContent
import com.ilustris.sagai.features.onboarding.ui.MorphingGenresBackground
import com.ilustris.sagai.features.onboarding.ui.OnboardingAction
import com.ilustris.sagai.features.onboarding.ui.OnboardingButton
import com.ilustris.sagai.features.onboarding.ui.OnboardingMascotContent
import com.ilustris.sagai.features.onboarding.ui.OnboardingStandardContent
import com.ilustris.sagai.features.onboarding.ui.OnboardingUiPage
import com.ilustris.sagai.features.onboarding.ui.OnboardingUiState
import com.ilustris.sagai.features.onboarding.ui.PremiumBackground
import com.ilustris.sagai.features.onboarding.ui.SparkBackground
import com.ilustris.sagai.ui.animations.MorphingAvatarBackground
import com.ilustris.sagai.ui.animations.StackedCardsBackground
import com.ilustris.sagai.ui.theme.FluidGradient
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.holographicGradient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingStateMapper
    @Inject
    constructor(
        private val billingService: BillingService,
        private val stringResourceHelper: StringResourceHelper,
        private val genreVisualConfig: GenreVisualConfigService,
        private val remoteConfigService: RemoteConfigService,
    ) {
        suspend fun buildOnboardingState(
            type: OnboardingType,
            content: OnboardingContent,
            genre: Genre? = null,
            saga: Saga? = null,
        ): OnboardingUiState.Content {
            val uiPages = buildPages(type, content, saga)
            return OnboardingUiState.Content(
                type = type,
                pages = uiPages,
            )
        }

        private suspend fun buildPages(
            type: OnboardingType,
            content: OnboardingContent,
            saga: Saga?,
        ): List<OnboardingUiPage> {
            val storyAssets =
                remoteConfigService.getJson<List<OnboardingAsset>>("story_faces") ?: emptyList()
            val iconsAssets =
                remoteConfigService.getJson<List<OnboardingAsset>>("avatar_faces") ?: emptyList()
            val genreConfigs = Genre.entries.associateWith { genreVisualConfig.getVisualConfig(it) }
            val mascotDesigns =
                remoteConfigService.getJson<Map<String, String>>("mascot_full_body_designs")
                    ?: emptyMap()

            return content.pages.mapIndexed { index, page ->
                val isLastPage = index == content.pages.size - 1
                val mascotUrl =
                    when (type) {
                        OnboardingType.APP_INTRO -> {
                            if (index == 0) mascotDesigns["default"] else null
                        }

                        OnboardingType.GAMEPLAY_GUIDE -> {
                            if (index == 0) {
                                mascotDesigns[
                                    saga?.genre?.name?.lowercase()
                                        ?: "default",
                                ]
                            } else {
                                null
                            }
                        }

                        else -> {
                            null
                        }
                    }
                val background: @Composable () -> Unit =
                    when (type) {
                        OnboardingType.APP_INTRO -> {
                            when (index) {
                                0 -> {
                                    { OnboardingMascotContent(mascotUrl) }
                                }

                                1 -> {
                                    { FluidGradient(holographicGradient) }
                                }

                                2 -> {
                                    { StackedCardsBackground(assets = storyAssets) }
                                }

                                else -> {
                                    { SparkBackground() }
                                }
                            }
                        }

                        OnboardingType.CREATION_GUIDE -> {
                            when (index) {
                                0 -> {
                                    { MorphingGenresBackground(visualConfigs = genreConfigs) }
                                }

                                1 -> {
                                    { StackedCardsBackground(assets = storyAssets) }
                                }

                                2 -> {
                                    { MorphingAvatarBackground(iconsAssets.map { it.image }) }
                                }

                                else -> {
                                    { SparkBackground(holographicGradient) }
                                }
                            }
                        }

                        OnboardingType.GAMEPLAY_GUIDE -> {
                            val genreConfig = saga?.genre?.let { genreConfigs[it] }
                            val colors =
                                genreConfig
                                    ?.colorPalette
                                    ?.mapNotNull { it.hexToColor() }
                                    ?.ifEmpty { holographicGradient } ?: holographicGradient
                            when (index) {
                                0 -> {
                                    {
                                        OnboardingMascotContent(
                                            mascotUrl,
                                            saga?.genre,
                                            genreConfig?.primaryColor?.hexToColor(),
                                        )
                                    }
                                }

                                1 -> {
                                    { FluidGradient(colors = colors) }
                                }

                                else -> {
                                    {
                                        SparkBackground(colors, saga?.genre?.icon)
                                    }
                                }
                            }
                        }

                        OnboardingType.PREMIUM_GUIDE -> {
                            when (index) {
                                0 -> {
                                    { PremiumBackground() }
                                }

                                1 -> {
                                    { StackedCardsBackground(assets = storyAssets) }
                                }

                                else -> {
                                    { MorphingGenresBackground(visualConfigs = genreConfigs) }
                                }
                            }
                        }
                    }

                val primaryButton =
                    if (isLastPage) {
                        val action =
                            when (type) {
                                OnboardingType.PREMIUM_GUIDE -> {
                                    val product =
                                        (billingService.state.value as? BillingService.BillingState.SignatureDisabled)
                                            ?.products
                                            ?.firstOrNull()
                                    OnboardingAction.Subscribe(product?.productId ?: "")
                                }

                                else -> {
                                    OnboardingAction.Dismiss
                                }
                            }
                        val text =
                            when (type) {
                                OnboardingType.APP_INTRO -> {
                                    stringResourceHelper.getString(R.string.onboarding_finish)
                                }

                                OnboardingType.CREATION_GUIDE -> {
                                    stringResourceHelper.getString(R.string.onboarding_creation_guide_finish)
                                }

                                OnboardingType.GAMEPLAY_GUIDE -> {
                                    stringResourceHelper.getString(R.string.onboarding_gameplay_guide_finish)
                                }

                                OnboardingType.PREMIUM_GUIDE -> {
                                    val price =
                                        (billingService.state.value as? BillingService.BillingState.SignatureDisabled)
                                            ?.products
                                            ?.firstOrNull()
                                            ?.subscriptionOfferDetails
                                            ?.firstOrNull()
                                            ?.pricingPhases
                                            ?.pricingPhaseList
                                            ?.firstOrNull()
                                            ?.formattedPrice ?: ""
                                    "${stringResourceHelper.getString(R.string.subscribe)} $price"
                                }
                            }
                        OnboardingButton(text, action)
                    } else {
                        OnboardingButton(
                            stringResourceHelper.getString(R.string.continue_text),
                            OnboardingAction.Next,
                        )
                    }

                val secondaryButton =
                    if (isLastPage) {
                        when (type) {
                            OnboardingType.PREMIUM_GUIDE -> {
                                OnboardingButton(
                                    stringResourceHelper.getString(
                                        R.string.restore_purchases,
                                    ),
                                    OnboardingAction.Restore,
                                )
                            }

                            OnboardingType.GAMEPLAY_GUIDE -> {
                                OnboardingButton(
                                    stringResourceHelper.getString(R.string.onboarding_dont_show_again),
                                    OnboardingAction.DeactivateTutorials,
                                )
                            }

                            else -> {
                                null
                            }
                        }
                    } else {
                        OnboardingButton(
                            stringResourceHelper.getString(R.string.onboarding_skip),
                            OnboardingAction.Skip,
                        )
                    }

                OnboardingUiPage(
                    background = background,
                    content = {
                        OnboardingStandardContent(page)
                    },
                    primaryButton = primaryButton,
                    secondaryButton = secondaryButton,
                )
            }
        }
    }
