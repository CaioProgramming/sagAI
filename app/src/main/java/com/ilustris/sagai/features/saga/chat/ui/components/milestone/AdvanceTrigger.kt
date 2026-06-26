package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeAction
import com.ilustris.sagai.features.saga.chat.presentation.model.toUi
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.progressiveBrush
import com.ilustris.sagai.ui.theme.sagaBrush
import com.ilustris.sagai.ui.theme.themeIcon
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
    val shadowBrush = sagaBrush()
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

    AnimatedContent(
        targetState = isGenerating,
        modifier = modifier.alpha(contentAlpha),
    ) { generating ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
        ) {
            if (generating) {
                Image(
                    themeIcon(),
                    contentDescription = stringResource(R.string.milestone_loading_cd),
                    modifier =
                        Modifier
                            .gradientFill(Brush.verticalGradient(morphingGradient()))
                            .size(36.dp)
                            .themeVfx(),
                )
            } else {
                Image(
                    themeIcon(),
                    contentDescription = null,
                    colorFilter =
                        ColorFilter.tint(
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        ),
                    modifier =
                        Modifier
                            .size(36.dp)
                            .scale(scale)
                            .dropShadow(CircleShape) {
                                if (dragProgress == 0f) return@dropShadow
                                radius = 15f * dragProgress
                                spread = 8f * dragProgress
                                brush = shadowBrush
                            }.clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.background.copy(alpha = dragProgress),
                                CircleShape,
                            ).gradientFill(
                                progressiveBrush(
                                    tintColor = primaryColor,
                                    progress = dragProgress,
                                    animationDuration = PULL_PROGRESS_ANIMATION_MS,
                                ),
                            ),
                )
            }

            val labelAlpha by animateFloatAsState(
                targetValue = if (dragProgress > 0f || generating) 1f else 0.6f,
                animationSpec = tween(300),
                label = "labelAlpha",
            )

            if (dragProgress > 0f || generating) {
                Text(
                    text = stringResource(actionUi.holdingTextRes),
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f * labelAlpha),
                            textAlign = TextAlign.Center,
                        ),
                    modifier = Modifier.padding(top = 8.dp),
                )
            } else {
                Text(
                    text = stringResource(actionUi.titleRes ?: R.string.continue_text),
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f * labelAlpha),
                            textAlign = TextAlign.Center,
                        ),
                    modifier = Modifier.padding(top = 8.dp),
                )
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
}
