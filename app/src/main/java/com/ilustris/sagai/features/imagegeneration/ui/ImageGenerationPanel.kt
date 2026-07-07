package com.ilustris.sagai.features.imagegeneration.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.debug.DebugImageFallbackService
import com.ilustris.sagai.features.debug.ui.ManualImageFallbackContent
import com.ilustris.sagai.features.imagegeneration.model.ImageGenerationUiState
import com.ilustris.sagai.features.imagegeneration.model.IslandExpansion
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.rememberVectorShape
import com.ilustris.sagai.ui.theme.themeIconVector
import com.ilustris.sagai.ui.theme.themePainter

private fun panelExpandAnimation() =
    spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntSize(0, 0),
    )

private val PanelFadeIn = fadeIn(tween(280, easing = FastOutSlowInEasing))
private val PanelFadeOut = fadeOut(tween(220, easing = FastOutSlowInEasing))

private val OuterShellShape = RoundedCornerShape(24.dp)
private val InnerAppShape = RoundedCornerShape(20.dp)

@Composable
fun ImageGenerationContainer(
    state: ImageGenerationUiState,
    debugImageFallbackService: DebugImageFallbackService,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val isActive =
        state is ImageGenerationUiState.Generating ||
            state is ImageGenerationUiState.AwaitingManualFallback

    if (!isActive) {
        Box(modifier = modifier.fillMaxSize()) {
            content()
        }
        return
    }

    val expansion =
        when (state) {
            is ImageGenerationUiState.Generating -> state.expansion
            is ImageGenerationUiState.AwaitingManualFallback -> state.expansion
            else -> IslandExpansion.Compact
        }
    val isExpanded = expansion == IslandExpansion.Expanded
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    val outerShellBrush = rememberImageGenerationShellBrush()
    val innerPadding by animateDpAsState(
        if (isActive) 8.dp else 0.dp,
    )

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .statusBarsPadding()
                .animateContentSize(
                    tween(
                        durationMillis = 200,
                        easing = FastOutSlowInEasing,
                    ),
                ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .clip(OuterShellShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .animateContentSize(tween(500, easing = FastOutSlowInEasing)),
        ) {
            ImageGenerationPanelTopBar(
                state = state,
                isExpanded = isExpanded,
                onToggleExpand = { if (isExpanded) onCollapse() else onExpand() },
                onCancel = onCancel,
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(),
                exit = slideOutVertically { it },
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .heightIn(max = 320.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                ) {
                    ImageGenerationPanelBody(
                        state = state,
                        debugImageFallbackService = debugImageFallbackService,
                        onCollapse = onCollapse,
                        onCancel = onCancel,
                    )
                }
            }

            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(innerPadding),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clip(InnerAppShape)
                            .background(MaterialTheme.colorScheme.background),
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun rememberImageGenerationShellBrush(): Brush {
    val base = MaterialTheme.colorScheme.surfaceContainerHigh
    val gradientTints =
        morphingGradient().map { color ->
            color.copy(alpha = 0.14f)
        }
    return Brush.verticalGradient(
        colors =
            listOf(
                gradientTints.first(),
                base,
                gradientTints.last().copy(alpha = 0.08f),
            ),
    )
}

@Composable
private fun ImageGenerationPanelBody(
    state: ImageGenerationUiState,
    debugImageFallbackService: DebugImageFallbackService,
    onCollapse: () -> Unit,
    onCancel: () -> Unit,
) {
    when (state) {
        is ImageGenerationUiState.Generating -> {
            state.reasoning?.let { reasoning ->
                Text(
                    text = reasoning,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start,
                )
            } ?: Text(
                text = stringResource(R.string.image_generation_reasoning_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        is ImageGenerationUiState.AwaitingManualFallback -> {
            if (BuildConfig.DEBUG) {
                ManualImageFallbackContent(
                    prompt = state.prompt,
                    debugImageFallbackService = debugImageFallbackService,
                    onSubmitted = onCollapse,
                    onCancel = onCancel,
                    scrollEnabled = false,
                    autoCopyPrompt = true,
                    showHeader = false,
                )
            }
        }

        else -> {
            Unit
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ImageGenerationPanelTopBar(
    state: ImageGenerationUiState,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onCancel: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onToggleExpand,
                    onLongClick = onCancel,
                ).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val shape = rememberVectorShape(themeIconVector())
        val brush = Brush.horizontalGradient(morphingGradient())
        Icon(
            themePainter(),
            null,
            tint = MaterialTheme.colorScheme.surfaceContainer,
            modifier =
                Modifier.size(24.dp).dropShadow(shape) {
                    this.brush = brush
                    radius = 5f
                    spread = .5f
                },
        )

        AnimatedContent(
            targetState = state,
            transitionSpec = { PanelFadeIn togetherWith PanelFadeOut },
            label = "panelLabel",
            modifier = Modifier.weight(1f),
        ) { animatedState ->
            Text(
                text = imageGenerationTaskTitle(animatedState),
                style =
                    MaterialTheme.typography.titleSmall.copy(
                        shadow = Shadow(Color.White, blurRadius = 10f),
                        brush = brush,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                    ),
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        val queueBadge =
            when (state) {
                is ImageGenerationUiState.Generating -> state.queuePosition.takeIf { it > 0 }
                else -> null
            }

        queueBadge?.let { count ->
            Text(
                text = stringResource(R.string.image_generation_queue_badge, count),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
        }

        IconButton(
            onClick = onToggleExpand,
            modifier = Modifier.size(24.dp).padding(4.dp).alpha(.5f),
        ) {
            val chevronRotation by animateFloatAsState(
                targetValue = if (isExpanded) 90f else -90f,
                label = "panelExpandChevron",
            )
            Icon(
                painter = painterResource(R.drawable.round_arrow_forward_ios_24),
                contentDescription =
                    if (isExpanded) {
                        stringResource(R.string.image_generation_panel_collapse)
                    } else {
                        stringResource(R.string.image_generation_panel_expand)
                    },
                tint = MaterialTheme.colorScheme.onBackground,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .rotate(chevronRotation),
            )
        }
    }
}

@Composable
private fun imageGenerationTaskTitle(state: ImageGenerationUiState): String =
    when (state) {
        is ImageGenerationUiState.Generating -> {
            stringResource(R.string.image_generation_default_label)
        }

        is ImageGenerationUiState.AwaitingManualFallback -> {
            stringResource(R.string.image_generation_awaiting_manual)
        }

        else -> {
            ""
        }
    }
