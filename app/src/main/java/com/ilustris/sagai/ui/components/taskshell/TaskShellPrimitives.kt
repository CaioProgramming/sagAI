package com.ilustris.sagai.ui.components.taskshell

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.rememberVectorShape
import com.ilustris.sagai.ui.theme.themeIconVector
import com.ilustris.sagai.ui.theme.themePainter

val TaskShellOuterShape = RoundedCornerShape(24.dp)
val TaskShellInnerShape = RoundedCornerShape(20.dp)
val TaskShellBottomShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

private val ShellFadeIn = fadeIn(tween(280, easing = FastOutSlowInEasing))
private val ShellFadeOut = fadeOut(tween(220, easing = FastOutSlowInEasing))

@Composable
fun TaskShellExpandedBody(
    modifier: Modifier = Modifier,
    maxHeight: Int = 320,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight.dp)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
    ) {
        content()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskShellBar(
    title: String,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    leadingPainter: Painter = themePainter(),
    trailingContent: @Composable (() -> Unit)? = null,
    expandContentDescription: Pair<Int, Int> =
        R.string.image_generation_panel_expand to R.string.image_generation_panel_collapse,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onToggleExpand,
                    onLongClick = onLongClick,
                ).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val shape = rememberVectorShape(themeIconVector())
        val brush = Brush.horizontalGradient(morphingGradient())
        Icon(
            leadingPainter,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.surfaceContainer,
            modifier =
                Modifier.size(24.dp).dropShadow(shape) {
                    this.brush = brush
                    radius = 5f
                    spread = 0.5f
                },
        )

        AnimatedContent(
            targetState = title,
            transitionSpec = { ShellFadeIn togetherWith ShellFadeOut },
            label = "taskShellTitle",
            modifier = Modifier.weight(1f),
        ) { animatedTitle ->
            Text(
                text = animatedTitle,
                style =
                    MaterialTheme.typography.titleSmall.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                    ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        trailingContent?.invoke()

        TaskShellChevron(
            isExpanded = isExpanded,
            onClick = onToggleExpand,
            expandContentDescription = expandContentDescription,
        )
    }
}

@Composable
fun TaskShellChevron(
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expandContentDescription: Pair<Int, Int> =
        R.string.image_generation_panel_expand to R.string.image_generation_panel_collapse,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(24.dp).padding(4.dp).alpha(0.5f),
    ) {
        val chevronRotation by animateFloatAsState(
            targetValue = if (isExpanded) 90f else -90f,
            label = "taskShellChevron",
        )
        Icon(
            painter = painterResource(R.drawable.round_arrow_forward_ios_24),
            contentDescription =
                stringResource(
                    if (isExpanded) expandContentDescription.second else expandContentDescription.first,
                ),
            tint = MaterialTheme.colorScheme.onBackground,
            modifier =
                Modifier
                    .fillMaxSize()
                    .rotate(chevronRotation),
        )
    }
}
