package com.ilustris.sagai.features.home.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaSummary
import com.ilustris.sagai.ui.animations.rememberLifecycleAnimationsActive
import com.ilustris.sagai.ui.components.AutoResizeText
import com.ilustris.sagai.ui.theme.SagAITheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

private val trophyAvatarSize = 64.dp
private val trophyPageWidth = 108.dp
private val trophyPagerHeight = 148.dp
private const val AUTO_ADVANCE_INTERVAL_MS = 5_000L
private const val AUTO_ADVANCE_PAUSE_AFTER_INTERACTION_MS = 8_000L

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrophyShelf(
    completedSagas: List<SagaSummary>,
    onCompletedSagaClicked: (SagaSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sorted =
        remember(completedSagas) {
            completedSagas.sortedByDescending { it.data.endedAt }
        }
    if (sorted.isEmpty()) return

    val pagerState = rememberPagerState { sorted.size }
    val animationsActive = rememberLifecycleAnimationsActive()
    var isSectionVisible by remember { mutableStateOf(false) }
    var pauseAutoAdvanceUntil by remember { mutableLongStateOf(0L) }

    val density = LocalDensity.current
    val screenHeightPx =
        with(density) {
            LocalConfiguration.current.screenHeightDp.dp
                .toPx()
        }
    val horizontalPadding =
        ((LocalConfiguration.current.screenWidthDp.dp - trophyPageWidth) / 2).coerceAtLeast(16.dp)

    val canAutoAdvance by remember {
        derivedStateOf { sorted.size > 1 }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.isScrollInProgress }
            .collect { scrolling ->
                if (scrolling) {
                    pauseAutoAdvanceUntil =
                        System.currentTimeMillis() + AUTO_ADVANCE_PAUSE_AFTER_INTERACTION_MS
                }
            }
    }

    LaunchedEffect(animationsActive, isSectionVisible, sorted.size, canAutoAdvance) {
        if (!animationsActive || sorted.size <= 1) return@LaunchedEffect

        while (isActive) {
            if (!isSectionVisible) {
                delay(500)
                continue
            }
            if (System.currentTimeMillis() < pauseAutoAdvanceUntil) {
                delay(200)
                continue
            }
            if (pagerState.isScrollInProgress) {
                delay(200)
                continue
            }

            delay(AUTO_ADVANCE_INTERVAL_MS)

            if (pagerState.isScrollInProgress || System.currentTimeMillis() < pauseAutoAdvanceUntil) {
                continue
            }

            val nextPage = (pagerState.settledPage + 1) % sorted.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp)
                .trackSectionVisibility(screenHeightPx) { isSectionVisible = it },
    ) {
        TrophySectionHeader()

        HorizontalPager(
            state = pagerState,
            pageSpacing = 12.dp,
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(trophyPagerHeight),
        ) { page ->
            val saga = sorted[page]
            val isSelected = pagerState.currentPage == page

            TrophyPagerItem(
                saga = saga,
                isSelected = isSelected,
                onClick = { onCompletedSagaClicked(saga) },
            )
        }
    }
}

@Composable
private fun TrophyPagerItem(
    saga: SagaSummary,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.12f else 0.82f,
        animationSpec = tween(400),
        label = "trophyPageScale",
    )
    val focusFactor = if (isSelected) 1f else 0.4f
    val titleAlpha = if (isSelected) 1f else 0.6f

    SagAITheme(genre = saga.data.genre) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        alpha = if (isSelected) 1f else 0.7f
                        clip = false
                    }.clickable(onClick = onClick),
        ) {
            TrophyPinVisual(
                saga = saga,
                avatarSize = trophyAvatarSize,
                focusFactor = focusFactor,
                levitateEnabled = isSelected,
            )
            AutoResizeText(
                text = saga.data.title,
                style =
                    if (isSelected) {
                        MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = titleAlpha),
                        )
                    } else {
                        MaterialTheme.typography.labelSmall.copy(
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = titleAlpha),
                        )
                    },
                maxLines = 2,
                minFontSize = 9.sp,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, start = 2.dp, end = 2.dp),
            )
        }
    }
}

@Composable
private fun TrophySectionHeader() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .alpha(0.45f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.home_completed_sagas_title),
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground,
                ),
        )
    }
}

private fun Modifier.trackSectionVisibility(
    screenHeightPx: Float,
    onVisibilityChanged: (Boolean) -> Unit,
): Modifier =
    onGloballyPositioned { coordinates ->
        val position = coordinates.positionInWindow()
        val height = coordinates.size.height.toFloat()
        val isVisible = position.y + height > 0f && position.y < screenHeightPx
        onVisibilityChanged(isVisible)
    }
