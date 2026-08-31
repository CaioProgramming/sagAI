@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.onboarding.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
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
import com.ilustris.sagai.core.services.BillingService
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import com.ilustris.sagai.features.onboarding.data.model.OnboardingPage
import com.ilustris.sagai.features.premium.PremiumTitle
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.animations.chromaticAberration
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient
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
    saga: Saga? = null,
    force: Boolean = false,
    onDismiss: () -> Unit = {},
) {
    OnboardingHost(
        type = type,
        presentation = OnboardingPresentation.Sheet,
        genre = genre,
        saga = saga,
        force = force,
        onDismiss = onDismiss,
    )
}

@Composable
internal fun DebugBillingSimulationSheet(
    reason: String,
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onSyncSubscription: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onCancel,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            )
        },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.billing_debug_fallback_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.billing_debug_fallback_message),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                text = reason,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                shape = MaterialTheme.shapes.large,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.billing_simulate_confirm).uppercase(),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    )
                }
            }
            TextButton(
                onClick = onSyncSubscription,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.billing_check_subscription).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            TextButton(
                onClick = onCancel,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.billing_simulate_cancel).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
internal fun BillingResultSheet(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    isLoading: Boolean = false,
    onSyncSubscription: (() -> Unit)? = null,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            )
        },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_spark),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            onSyncSubscription?.let { onSync ->
                TextButton(
                    onClick = onSync,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.billing_check_subscription).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
            Button(
                onClick = onDismiss,
                enabled = !isLoading,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                shape = MaterialTheme.shapes.large,
            ) {
                Text(
                    text = stringResource(R.string.billing_result_ok).uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}

@Composable
fun OnboardingStandardContent(page: OnboardingPage) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = page.title,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = page.description,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
    }
}

@Composable
fun OnboardingMascotContent(
    mascotUrl: String?,
    genre: Genre? = null,
    color: Color? = null,
) {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        StarryTextPlaceholder(
            modifier =
                Modifier.reactiveShimmer(
                    true,
                    (color ?: holographicGradient.first()).darkerPalette(),
                ),
        )
        mascotUrl?.let {
            AsyncImage(
                model = it,
                contentDescription = null,
                modifier =
                    Modifier
                        .padding(16.dp)
                        .size(240.dp)
                        .levitate(true),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
fun CinematicBackground(config: GenreVisualConfig?) {
    AsyncImage(
        model = config?.imageUrl ?: emptyString(),
        contentDescription = null,
        modifier =
            Modifier
                .fillMaxSize()
                .zoomAnimation(),
        contentScale = ContentScale.Crop,
    )
}

/**
 * @param colors defaults to the brand gradient rather than an empty list. It used to default to
 *   empty while the icon tint read `colors.first()`, so calling this with its own defaults threw
 *   `NoSuchElementException` — and inside a pager's prefetch that surfaced as an unrelated
 *   `Cannot disable reuse from root`, because the failed pausable composition leaves the reuse
 *   state inconsistent and the next assertion is what gets reported.
 */
@Composable
fun SparkBackground(
    colors: List<Color> = holographicGradient,
    customIcon: Int? = null,
) {
    val palette = colors.ifEmpty { holographicGradient }
    Box(
        Modifier
            .fillMaxSize()
            .reactiveShimmer(true, palette, repeatMode = RepeatMode.Restart, targetValue = 1000f),
    ) {
        StarryTextPlaceholder(
            modifier =
                Modifier
                    .fillMaxSize()
                    .gradientFill(Brush.verticalGradient(palette)),
        )
        Icon(
            painter = painterResource(customIcon ?: R.drawable.ic_spark),
            contentDescription = null,
            tint = palette.first(),
            modifier =
                Modifier
                    .size(120.dp)
                    .align(Alignment.Center)
                    .levitate(true)
                    .chromaticAberration(true),
        )
    }
}

@Composable
fun StarfieldBackground() {
    StarryTextPlaceholder(
        modifier = Modifier.fillMaxSize().reactiveShimmer(true),
        starColor = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
fun PremiumBackground() {
    Box(
        modifier =
            Modifier
                .reactiveShimmer(true)
                .fillMaxSize(),
    ) {
        StarryTextPlaceholder(
            modifier = Modifier.fillMaxSize(),
            starColor = Color.White,
        )
        PremiumTitle(
            titleStyle = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .levitate(true)
                    .chromaticAberration(true, 5f, 15f),
        )
    }
}

@Composable
fun MorphingGenresBackground(
    visualConfigs: Map<Genre, GenreVisualConfig?> = emptyMap(),
    onSwitchGenre: (Genre) -> Unit = {},
) {
    val genres = remember { Genre.entries.shuffled() }
    var currentGenreIndex by remember { mutableIntStateOf(0) }
    var nextGenreIndex by remember { mutableIntStateOf(1) }

    val currentGenre = genres[currentGenreIndex]
    val nextGenre = genres[nextGenreIndex]

    val wipeProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3.seconds)
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
        GenreImage(
            genre = currentGenre,
            config = visualConfigs[currentGenre],
            modifier =
                Modifier
                    .fillMaxSize()
                    .zoomAnimation(),
        )

        GenreImage(
            genre = nextGenre,
            config = visualConfigs[nextGenre],
            modifier =
                Modifier
                    .fillMaxSize()
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
                    }.zoomAnimation(),
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
        model = config?.imageUrl ?: genre.icon,
        contentDescription = null,
        modifier =
            modifier
                .effectForGenre(genre, config, enableSelectiveHighlight = true),
        contentScale = ContentScale.Crop,
    )
}
