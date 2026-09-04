package com.ilustris.sagai.features.onboarding.data

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.premium.ui.PremiumPlansContent
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.services.GenreVisualConfigService
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

            // The key field is the last page of this type, described in Remote Config like any
            // other. It was briefly appended after them instead, which broke the assumption that
            // the last configured page is the last page shown — and both things reading that
            // assumption (the background's else branch, the finishing button) went wrong.
            val isKeyOnboarding = type == OnboardingType.API_KEY_SETUP
            val isPremiumOnboarding = type == OnboardingType.PREMIUM_GUIDE

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
                                    { MorphingGenresBackground(visualConfigs = genreConfigs) }
                                }

                                2 -> {
                                    { MorphingAvatarBackground(iconsAssets.map { it.image }) }
                                }

                                else -> {
                                    {
                                        StarfieldBackground()
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
                            // Keyed on the last page rather than on an index, because the page
                            // count is configured: the plan list moved from index 2 to index 1
                            // when the copy went from three pages to two, and an index-keyed
                            // branch silently handed it the wrong backdrop.
                            when {
                                isLastPage -> {
                                    { MorphingGenresBackground(visualConfigs = genreConfigs) }
                                }

                                index == 0 -> {
                                    { PremiumBackground() }
                                }

                                else -> {
                                    { StackedCardsBackground(assets = storyAssets) }
                                }
                            }
                        }
                    }

                val primaryButton =
                    if (isLastPage && (isKeyOnboarding || isPremiumOnboarding)) {
                        // Both of these end on a page whose content owns its own submit: only the
                        // key field knows whether what was typed is worth sending, and only the
                        // plan list knows which plan is selected. A page-level button would either
                        // duplicate them or fire without either answer.
                        null
                    } else if (isLastPage) {
                        val action = OnboardingAction.Dismiss
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

                                // Unreachable, like API_KEY_SETUP above: this type never gets a
                                // page-level button on its last page.
                                OnboardingType.PREMIUM_GUIDE -> {
                                    stringResourceHelper.getString(R.string.onboarding_finish)
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
                    if (isKeyOnboarding && isLastPage) {
                        OnboardingButton(
                            stringResourceHelper.getString(R.string.api_key_setup_open_studio),
                            OnboardingAction.OpenUrl(AI_STUDIO_URL),
                        )
                    } else if (isLastPage) {
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
                        if (isKeyOnboarding && isLastPage) {
                            ApiKeyInputContent(page)
                        } else if (isPremiumOnboarding && isLastPage) {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                OnboardingStandardContent(page)
                                PremiumPlansContent()
                            }
                        } else {
                            OnboardingStandardContent(page)
                        }
                    },
                    primaryButton = primaryButton,
                    secondaryButton = secondaryButton,
                )
            }
        }
    }
