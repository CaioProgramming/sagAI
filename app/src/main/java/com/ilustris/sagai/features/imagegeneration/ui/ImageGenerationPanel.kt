package com.ilustris.sagai.features.imagegeneration.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.debug.DebugImageFallbackService
import com.ilustris.sagai.features.debug.ui.ManualImageFallbackContent
import com.ilustris.sagai.features.imagegeneration.model.ImageGenerationUiState
import com.ilustris.sagai.features.imagegeneration.model.IslandExpansion
import com.ilustris.sagai.ui.components.taskshell.TaskShellBar
import com.ilustris.sagai.ui.components.taskshell.TaskShellExpandedBody
import com.ilustris.sagai.ui.components.taskshell.TaskShellInnerShape
import com.ilustris.sagai.ui.components.taskshell.TaskShellOuterShape

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
                    .clip(TaskShellOuterShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .animateContentSize(tween(500, easing = FastOutSlowInEasing)),
        ) {
            TaskShellBar(
                title = imageGenerationTaskTitle(state),
                isExpanded = isExpanded,
                onToggleExpand = { if (isExpanded) onCollapse() else onExpand() },
                onLongClick = onCancel,
                trailingContent = {
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
                        )
                    }
                },
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(),
                exit = slideOutVertically { it },
            ) {
                TaskShellExpandedBody {
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
                            .clip(TaskShellInnerShape)
                            .background(MaterialTheme.colorScheme.background),
                ) {
                    content()
                }
            }
        }
    }
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
