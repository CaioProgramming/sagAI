package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeAction
import com.ilustris.sagai.features.saga.chat.presentation.model.toUi
import com.ilustris.sagai.ui.components.taskshell.TaskShellCompactClick
import com.ilustris.sagai.ui.components.taskshell.TaskShellContent
import com.ilustris.sagai.ui.components.taskshell.TaskShellExpansion
import com.ilustris.sagai.ui.components.taskshell.TaskShellScope
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.iconDropShadow
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.progressiveBrush
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.rememberVectorShape
import com.ilustris.sagai.ui.theme.sagaBrush
import com.ilustris.sagai.ui.theme.shimmerize
import com.ilustris.sagai.ui.theme.themeIconVector
import com.ilustris.sagai.ui.theme.themePainter
import kotlin.time.Duration.Companion.seconds

class ObjectiveShellContent(
    private val title: String,
    private val objective: String,
    private val progress: Float,
    private val isLoading: Boolean = false,
) : TaskShellContent {
    override val isExpandable: Boolean = true
    override val isDraggable: Boolean = false
    override val compactClick: TaskShellCompactClick = TaskShellCompactClick.Toggle

    @Composable
    override fun Compact(scope: TaskShellScope) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
            val iconColor by animateColorAsState(
                if (scope.expansion == TaskShellExpansion.Expanded) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.primary
                },
            )
            val themeBrush = sagaBrush()
            val shadowAlpha by animateFloatAsState(
                if (isLoading) 1f else 0f,
            )
            Icon(
                themePainter(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f),
                modifier =
                    Modifier
                        .clip(CircleShape)
                        .clickable {
                            scope.onToggle()
                        }.padding(8.dp)
                        .size(24.dp)
                        .dropShadow(rememberVectorShape(themeIconVector())) {
                            brush = themeBrush
                            radius = 10f
                            spread = 1f
                            alpha = shadowAlpha
                        }.gradientFill(progressiveBrush(tintColor = iconColor, progress = progress))
                        .reactiveShimmer(isLoading),
            )
        }
    }

    @Composable
    override fun Expanded(scope: TaskShellScope) {
        ObjectiveOverlay(
            title = title,
            objective = objective,
            progress = progress,
            applyStatusBarsPadding = false,
            modifier = Modifier.fillMaxWidth(),
            onDismiss = scope.onMinimize,
        )
    }
}

class NarrativeAdvanceShellContent(
    private val action: NarrativeAction,
    private val reasoning: String?,
    private val isProcessing: Boolean,
    private val dragProgress: Float,
) : TaskShellContent {
    override val isExpandable: Boolean = true
    override val isDraggable: Boolean = !isProcessing
    override val compactClick: TaskShellCompactClick = TaskShellCompactClick.RequestFull

    @Composable
    override fun Compact(scope: TaskShellScope) {
        val actionUi = action.toUi()
        val titleRes = if (isProcessing) actionUi.holdingTextRes else (actionUi.titleRes ?: R.string.continue_text)

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .gradientFill(Brush.verticalGradient(morphingGradient())),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                themePainter(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )

            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )

            if (!isProcessing) {
                Text(
                    text = stringResource(R.string.advance_pull_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }

    @Composable
    override fun Expanded(scope: TaskShellScope) {
        val actionUi = action.toUi()
        val titleRes = if (isProcessing) actionUi.holdingTextRes else (actionUi.titleRes ?: R.string.continue_text)

        Box(Modifier.fillMaxWidth().fillMaxHeight(.5f), contentAlignment = Alignment.Center) {
            val morphingGradient = Brush.verticalGradient(morphingGradient())
            Icon(
                themePainter(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.background,
                modifier =
                    Modifier
                        .size(48.dp)
                        .reactiveShimmer(
                            true,
                            repeatMode = RepeatMode.Restart,
                            shimmerColors = Color.White.shimmerize(),
                            duration = 10.seconds,
                        ).dropShadow(rememberVectorShape(themeIconVector())) {
                            brush = morphingGradient
                            radius = 10f
                            spread = 1f
                        },
            )
        }
    }
}
