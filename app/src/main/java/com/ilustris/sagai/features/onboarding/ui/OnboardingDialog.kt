@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.onboarding.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInBounce
import androidx.compose.animation.core.EaseInElastic
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.ilustris.sagai.MainActivity
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.ai.model.LocalGenreVisualConfig
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.features.onboarding.data.OnboardingContent
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.animations.chromaticAberration
import com.ilustris.sagai.ui.components.WarpSpeedStarField
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.levitate
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.zoomAnimation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Composable
fun OnboardingDialog(
    type: OnboardingType,
    genre: Genre? = null,
    force: Boolean = false,
    onDismiss: () -> Unit = {},
) {
    val viewModel: OnboardingViewModel = hiltViewModel()
    val state by viewModel.onboardingState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.checkOnboarding(type, genre, force)
    }

    when (val uiState = state) {
        is OnboardingUiState.Content -> {
            OnboardingContentSheet(
                type = type,
                content = uiState.content,
                genre = genre,
                onDismiss = {
                    if (type != OnboardingType.PREMIUM_GUIDE) {
                        viewModel.markAsSeen(type)
                    }
                    onDismiss()
                },
            )
        }

        is OnboardingUiState.Error -> {
            SideEffect {
                onDismiss()
            }
        }

        else -> {}
    }
}

@Composable
private fun OnboardingContentSheet(
    type: OnboardingType,
    content: OnboardingContent,
    genre: Genre?,
    onDismiss: () -> Unit,
) {
    val viewModel: OnboardingViewModel = hiltViewModel()
    val context = LocalContext.current
    val billingState by viewModel.billingState.collectAsState()
    val pagerState = rememberPagerState { content.pages.size }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val currentConfig by viewModel.currentConfig.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null,
        shape =
            MaterialTheme.shapes.large.copy(
                bottomStart = CornerSize(0),
                bottomEnd = CornerSize(0),
            ),
    ) {
        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.95f)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true,
            ) { pageIndex ->
                OnboardingPageBackground(type, pageIndex, currentConfig) {
                    viewModel.switchVisualConfig(it)
                }
            }

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(fadeGradientBottom())
                        .padding(bottom = 32.dp),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            painterResource(R.drawable.round_close_24),
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        repeat(content.pages.size) { iteration ->
                            val isSelected = pagerState.currentPage == iteration
                            val size by animateDpAsState(
                                targetValue = if (isSelected) 24.dp else 6.dp,
                                animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
                                label = "dot_size",
                            )
                            val color by animateColorAsState(
                                targetValue =
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                                    },
                                label = "dot_color",
                            )

                            val randomGenre =
                                remember(iteration) {
                                    Genre.entries.shuffled().first()
                                }

                            Box(
                                modifier =
                                    Modifier
                                        .padding(4.dp)
                                        .size(size)
                                        .background(
                                            if (isSelected) Color.Transparent else color,
                                            MaterialTheme.shapes.small,
                                        ),
                                contentAlignment = Alignment.Center,
                            ) {
                                this@Row.AnimatedVisibility(
                                    visible = isSelected,
                                    enter = scaleIn(tween(200, easing = EaseInBounce)) + fadeIn(),
                                    exit = scaleOut(tween(500, easing = EaseInElastic)) + fadeOut(),
                                ) {
                                    Icon(
                                        painter = painterResource(randomGenre.background),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(48.dp)) // Offset the close button
                }

                Spacer(modifier = Modifier.weight(1f))

                // AI Generated Content
                val currentPage = content.pages.getOrNull(pagerState.currentPage)
                AnimatedContent(
                    targetState = currentPage,
                    transitionSpec = {
                        fadeIn(tween(600)) togetherWith fadeOut(tween(400))
                    },
                    label = "onboarding_text",
                ) { page ->
                    if (page != null) {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = page.title,
                                style =
                                    MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onBackground,
                                    ),
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = page.description,
                                style =
                                    MaterialTheme.typography.labelMedium.copy(
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Light,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                    ),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Continue Button
                val isLastPage = pagerState.currentPage == content.pages.size - 1
                val buttonText =
                    if (isLastPage) {
                        when (type) {
                            OnboardingType.APP_INTRO -> {
                                stringResource(R.string.onboarding_finish)
                            }

                            OnboardingType.CREATION_GUIDE -> {
                                stringResource(R.string.onboarding_creation_guide_finish)
                            }

                            OnboardingType.GAMEPLAY_GUIDE -> {
                                stringResource(R.string.onboarding_gameplay_guide_finish)
                            }

                            OnboardingType.PREMIUM_GUIDE -> {
                                val pricing =
                                    (billingState as? BillingService.BillingState.SignatureDisabled)
                                        ?.products
                                        ?.firstOrNull()
                                        ?.subscriptionOfferDetails
                                        ?.firstOrNull()
                                        ?.pricingPhases
                                        ?.pricingPhaseList
                                        ?.firstOrNull()
                                        ?.formattedPrice ?: emptyString()
                                "${stringResource(R.string.subscribe)} $pricing"
                            }
                        }
                    } else {
                        stringResource(R.string.onboarding_next)
                    }

                Button(
                    onClick = {
                        if (!isLastPage) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            if (type == OnboardingType.PREMIUM_GUIDE) {
                                val disabledState =
                                    billingState as? BillingService.BillingState.SignatureDisabled
                                val product = disabledState?.products?.firstOrNull()
                                val offerToken =
                                    product?.subscriptionOfferDetails?.firstOrNull()?.offerToken
                                if (product != null && offerToken != null) {
                                    viewModel.purchasePremium(
                                        activity = context as? MainActivity ?: return@Button,
                                        productDetails = product,
                                        offerToken = offerToken,
                                    )
                                }
                            } else {
                                onDismiss()
                            }
                        }
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp)
                            .height(56.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor =
                                if (isLastPage &&
                                    type == OnboardingType.PREMIUM_GUIDE
                                ) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onBackground
                                },
                            contentColor =
                                if (isLastPage &&
                                    type == OnboardingType.PREMIUM_GUIDE
                                ) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.background
                                },
                        ),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Text(
                        text = buttonText,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    )
                }

                TextButton(
                    onClick = {
                        if (type == OnboardingType.PREMIUM_GUIDE && isLastPage) {
                            viewModel.restorePurchases()
                        } else if (type == OnboardingType.PREMIUM_GUIDE && !isLastPage) {
                            scope.launch {
                                pagerState.animateScrollToPage(content.pages.size - 1)
                            }
                        } else {
                            onDismiss()
                        }
                    },
                    modifier =
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 8.dp),
                ) {
                    Text(
                        text =
                            if (type == OnboardingType.PREMIUM_GUIDE && isLastPage) {
                                stringResource(R.string.restore_purchases)
                            } else {
                                stringResource(R.string.onboarding_skip)
                            },
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

enum class OnboardingVisual {
    SPARK,
    MORPHING,
    CINEMATIC,
    STARFIELD,
    GENERIC,
}

@Composable
fun OnboardingPageBackground(
    type: OnboardingType,
    pageIndex: Int,
    currentConfig: GenreVisualConfig? = null,
    onSwitchGenre: (Genre) -> Unit,
) {
    val visual =
        when (type) {
            OnboardingType.APP_INTRO -> {
                when (pageIndex) {
                    0 -> OnboardingVisual.SPARK
                    1 -> OnboardingVisual.MORPHING
                    else -> OnboardingVisual.STARFIELD
                }
            }

            OnboardingType.CREATION_GUIDE -> {
                when (pageIndex) {
                    0 -> OnboardingVisual.CINEMATIC
                    1 -> OnboardingVisual.MORPHING
                    else -> OnboardingVisual.STARFIELD
                }
            }

            OnboardingType.GAMEPLAY_GUIDE -> {
                when (pageIndex) {
                    0 -> OnboardingVisual.CINEMATIC
                    1 -> OnboardingVisual.STARFIELD
                    else -> OnboardingVisual.SPARK
                }
            }

            OnboardingType.PREMIUM_GUIDE -> {
                when (pageIndex) {
                    0 -> OnboardingVisual.MORPHING
                    1 -> OnboardingVisual.STARFIELD
                    else -> OnboardingVisual.CINEMATIC
                }
            }
        }

    when (visual) {
        OnboardingVisual.CINEMATIC -> {
            AsyncImage(
                model = LocalGenreVisualConfig.current?.imageUrl ?: emptyString(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        OnboardingVisual.MORPHING -> {
            MorphingGenresBackground(
                currentConfig,
                onSwitchGenre,
            )
        }

        OnboardingVisual.SPARK -> {
            Box(Modifier.fillMaxSize()) {
                StarryTextPlaceholder(
                    modifier = Modifier.fillMaxSize(),
                )
                Icon(
                    painter = painterResource(R.drawable.ic_spark),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier =
                        Modifier
                            .size(120.dp)
                            .align(Alignment.Center)
                            .levitate(true)
                            .chromaticAberration(true)
                            .reactiveShimmer(true),
                )
            }
        }

        OnboardingVisual.STARFIELD -> {
            WarpSpeedStarField(
                modifier = Modifier.fillMaxSize(),
                starColor = MaterialTheme.colorScheme.onBackground,
            )
        }

        else -> {
            AsyncImage(
                model = emptyString(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
fun MorphingGenresBackground(
    visualConfig: GenreVisualConfig?,
    onSwitchGenre: (Genre) -> Unit = {},
) {
    val viewModel: OnboardingViewModel = hiltViewModel()
    val visualConfigs by viewModel.visualConfigs.collectAsStateWithLifecycle()

    val genres = remember { Genre.entries.shuffled() }
    var currentGenreIndex by remember { mutableIntStateOf(0) }
    var nextGenreIndex by remember { mutableIntStateOf(1) }

    val currentGenre = genres[currentGenreIndex]
    val nextGenre = genres[nextGenreIndex]

    val wipeProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5.seconds)
            val hasConfigs =
                visualConfigs.containsKey(currentGenre) &&
                    visualConfigs.containsKey(nextGenre)

            if (hasConfigs) {
                wipeProgress.animateTo(1f, tween(1500))

                // Swap
                currentGenreIndex = nextGenreIndex
                nextGenreIndex = (nextGenreIndex + 1) % genres.size

                onSwitchGenre(genres[currentGenreIndex])
                wipeProgress.snapTo(0f)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Current Background (Bottom)
        GenreImage(
            genre = currentGenre,
            config = visualConfigs[currentGenre],
            modifier = Modifier.fillMaxSize().zoomAnimation(),
        )

        // Next Background (Top, Wipe Effect)
        GenreImage(
            genre = nextGenre,
            config = visualConfigs[nextGenre],
            modifier =
                Modifier
                    .fillMaxSize()
                    .zoomAnimation()
                    .drawWithContent {
                        if (wipeProgress.value > 0) {
                            drawContent()
                        }
                    }.graphicsLayer {
                        clip = true
                        shape =
                            GenericShape { size, _ ->
                                addRect(
                                    Rect(
                                        0f,
                                        0f,
                                        size.width * wipeProgress.value,
                                        size.height,
                                    ),
                                )
                            }
                    },
        )
    }
}

@Composable
private fun GenreImage(
    genre: Genre,
    config: GenreVisualConfig?,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = config?.imageUrl ?: genre.background,
        contentDescription = null,
        modifier =
            modifier
                .effectForGenre(genre, config)
                .selectiveColorHighlight(genre.selectiveHighlight(config)),
        contentScale = ContentScale.Crop,
    )
}
