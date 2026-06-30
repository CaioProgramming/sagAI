package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeAction
import com.ilustris.sagai.features.saga.chat.presentation.model.toUi
import com.ilustris.sagai.ui.theme.components.MorphingThemeIcon
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.progressiveBrush
import com.ilustris.sagai.ui.theme.themeVfx

private const val PULL_PROGRESS_ANIMATION_MS = 16

@Composable
fun AdvancePullIndicator(
    action: NarrativeAction,
    pullProgress: Float,
    isGenerating: Boolean,
    modifier: Modifier = Modifier,
) {
    val actionUi = action.toUi()
    val primaryColor = MaterialTheme.colorScheme.primary
    val dragProgress = pullProgress.coerceIn(0f, 1f)

    val scale by animateFloatAsState(
        targetValue = 0.9f + (0.1f * dragProgress),
        animationSpec = tween(PULL_PROGRESS_ANIMATION_MS),
        label = "scale",
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (isGenerating || dragProgress > 0f) 1f else 0.85f,
        animationSpec = tween(300),
        label = "contentAlpha",
    )

    val labelAlpha by animateFloatAsState(
        targetValue = if (dragProgress > 0f || isGenerating) 1f else 0.6f,
        animationSpec = tween(300),
        label = "labelAlpha",
    )

    val iconSize by animateDpAsState(
        targetValue = if (dragProgress == 1f) 64.dp else 36.dp,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "iconSize",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp)
                .alpha(contentAlpha),
    ) {
        val brush =
            if (isGenerating) {
                Brush.verticalGradient(morphingGradient())
            } else {
                progressiveBrush(
                    tintColor = primaryColor,
                    progress = dragProgress,
                    animationDuration = PULL_PROGRESS_ANIMATION_MS,
                )
            }
        MorphingThemeIcon(
            modifier =
                Modifier
                    .size(iconSize)
                    .themeVfx(isGenerating),
            brush = brush,
            glowIntensity = if (isGenerating) 1f else dragProgress.coerceAtLeast(0.35f),
            tint = MaterialTheme.colorScheme.onBackground,
            contentDescription =
                if (isGenerating) {
                    stringResource(R.string.milestone_loading_cd)
                } else {
                    null
                },
        )

        AnimatedContent(isGenerating) {
            Text(
                text =
                    stringResource(
                        if (!it) {
                            (
                                actionUi.titleRes
                                    ?: R.string.continue_text
                            )
                        } else {
                            actionUi.holdingTextRes
                        },
                    ),
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f * labelAlpha),
                        textAlign = TextAlign.Center,
                    ),
            )
        }

        AnimatedVisibility(isGenerating.not()) {
            Text(
                text = stringResource(R.string.advance_pull_hint),
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                        textAlign = TextAlign.Center,
                    ),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
