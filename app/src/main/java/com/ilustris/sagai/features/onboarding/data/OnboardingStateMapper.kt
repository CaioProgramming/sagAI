package com.ilustris.sagai.features.onboarding.data

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.ilustris.sagai.BuildConfig
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
import com.ilustris.sagai.features.onboarding.ui.StarfieldBackground
import com.ilustris.sagai.features.onboarding.ui.apikey.AI_STUDIO_URL
import com.ilustris.sagai.features.onboarding.ui.apikey.ApiKeyInputContent
import com.ilustris.sagai.ui.animations.MorphingAvatarBackground
import com.ilustris.sagai.ui.animations.StackedCardsBackground
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.theme.FluidGradient
import com.ilustris.sagai.ui.theme.fadeColors
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.fadeGradientTop
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.morphingColor
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.sagaBrush
import com.ilustris.sagai.ui.theme.themeBrushColors
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
                remoteConfigService.getJsonList("story_faces", OnboardingAsset::class.java) ?: emptyList()
            val iconsAssets =
                remoteConfigService.getJsonList("avatar_faces", OnboardingAsset::class.java) ?: emptyList()
            val genreConfigs = Genre.entries.associateWith { genreVisualConfig.getVisualConfig(it) }
            val mascotDesigns =
                remoteConfigService.getJsonMapStringString("mascot_full_body_designs")
                    ?: emptyMap()

            // The key field is appended after these, so for that type no page of copy is ever the
            // last one. Getting this wrong gave the final paragraph a Dismiss button, which closed
            // the whole onboarding — marking it seen and clearing its state — one page before the
            // field it exists to reach.
            val hasAppendedPage = type == OnboardingType.API_KEY_SETUP

            val copyPages =
                content.pages.mapIndexed { index, page ->
                    val isLastPage = !hasAppendedPage && index == content.pages.size - 1
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
                            OnboardingType.API_KEY_SETUP -> {
                                when (index) {
                                    0 -> {
                                        {
                                            Box(
                                                Modifier.fillMaxSize().background(
                                                    Brush.verticalGradient(
                                                        morphingGradient(),
                                                    ),
                                                ),
                                            )
                                        }
                                    }

                                    1 -> {
                                        { StarryTextPlaceholder(Modifier.fillMaxSize().reactiveShimmer(true)) }
                                    }

                                    2 -> {
                                        { StackedCardsBackground(assets = storyAssets) }
                                    }

                                    else -> {
                                        {
                                            Box(
                                                Modifier.fillMaxSize().background(
                                                    fadeGradientBottom(morphingColor(holographicGradient)),
                                                ),
                                            )
                                        }
                                    }
                                }
                            }

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
                                    OnboardingType.API_KEY_SETUP -> {
                                        stringResourceHelper.getString(R.string.onboarding_finish)
                                    }

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
                                                ?.formattedPrice
                                        if (price.isNullOrBlank() && BuildConfig.DEBUG) {
                                            stringResourceHelper.getString(R.string.subscribe_debug_fallback)
                                        } else {
                                            "${stringResourceHelper.getString(R.string.subscribe)} ${price.orEmpty()}"
                                        }
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
                                        stringResourceHelper.getString(R.string.premium_not_now),
                                        OnboardingAction.Dismiss,
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

            // The key field is a page like any other — the type carries a composable, not a title
            // and a paragraph. Appending it here rather than asking Remote Config to describe it
            // keeps the copy free to change without someone having to remember that the last entry
            // is load-bearing. It carries no buttons: the field owns its own submit, because only
            // it knows whether what was typed is worth sending.
            return if (type == OnboardingType.API_KEY_SETUP) {
                copyPages +
                    OnboardingUiPage(
                        background = { SparkBackground() },
                        content = { ApiKeyInputContent() },
                        primaryButton = null,
                        secondaryButton =
                            OnboardingButton(
                                stringResourceHelper.getString(R.string.api_key_setup_open_studio),
                                OnboardingAction.OpenUrl(AI_STUDIO_URL),
                            ),
                    )
            } else {
                copyPages
            }
        }
    }
