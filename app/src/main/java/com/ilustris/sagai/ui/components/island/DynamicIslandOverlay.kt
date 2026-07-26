package com.ilustris.sagai.ui.components.island

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.morphingGradient

private val IslandFadeTween = tween<Float>(durationMillis = 220, easing = EaseInOut)

/**
 * Top-anchored floating island overlay — the iOS "Dynamic Island"-style surface.
 *
 * Compact form is content-sized (wrap width) and grows to a comfortable fixed width when expanded,
 * animating between the two. Deliberately **no scrim and no blur**. Honors
 * [IslandContent.hasSurface] (bare vs card), [IslandContent.expandsOnTap] and
 * [IslandContent.forceExpanded]. Content reserves space for this via [islandPadding], which is
 * driven purely by presence (content null or not) — not by this overlay's measured size.
 */
@Composable
fun DynamicIslandOverlay(
    content: IslandContent?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (content == null) return

    val configuration = LocalConfiguration.current
    val expandedWidth = (configuration.screenWidthDp.dp - 24.dp).coerceAtMost(520.dp)

    // Cap growth so the expanded body never overflows the screen; content shorter than this
    // wraps naturally — the island only reaches full height when the content genuinely needs it.
    val maxExpandedHeight = (configuration.screenHeightDp.dp - 140.dp).coerceAtLeast(200.dp)

    val forceExpanded = content.forceExpanded
    val effectiveExpanded = forceExpanded || expanded

    val scope =
        remember(effectiveExpanded, forceExpanded) {
            IslandScope(
                expansion = if (effectiveExpanded) IslandExpansionState.Expanded else IslandExpansionState.Compact,
                onCollapse = { if (!forceExpanded) onExpandedChange(false) },
            )
        }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        if (effectiveExpanded && !forceExpanded) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { onExpandedChange(false) }
                        },
            )
        }

        val islandModifier =
            Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .widthIn(max = expandedWidth)
                .wrapContentWidth()

        IslandBody(
            content = content,
            effectiveExpanded = effectiveExpanded,
            forceExpanded = forceExpanded,
            expanded = expanded,
            scope = scope,
            onExpandedChange = onExpandedChange,
            maxExpandedHeight = maxExpandedHeight,
            modifier = islandModifier,
        )
    }
}

@Composable
private fun IslandBody(
    content: IslandContent,
    effectiveExpanded: Boolean,
    forceExpanded: Boolean,
    expanded: Boolean,
    scope: IslandScope,
    onExpandedChange: (Boolean) -> Unit,
    maxExpandedHeight: Dp,
    modifier: Modifier,
) {
    SagAITheme(content.compact.genre) {
        val shadowAlpha by animateFloatAsState(
            if (content.compact.isLoading) 1.5f else 1f,
        )
        // Card (background + shadow + border) fades out while a bare compact is collapsed
        // (e.g. the objective's lone icon) and fades back in when expanded — no `hasSurface`
        // branching, just one animated alpha.
        val cardVisible = content.compact.showBackground || effectiveExpanded
        val cardAlpha by animateFloatAsState(if (cardVisible) 1f else 0f, label = "islandCardAlpha")
        val shape = rememberIslandShape(effectiveExpanded)
        val shadowColor = MaterialTheme.colorScheme.primary
        val bg = content.compact.backgroundColor
        val baseColor =
            when (bg) {
                IslandBackgroundColor.ThemePrimary -> MaterialTheme.colorScheme.primary
                IslandBackgroundColor.ThemeSurface -> MaterialTheme.colorScheme.surface
                IslandBackgroundColor.ThemeBackground -> MaterialTheme.colorScheme.background
                is IslandBackgroundColor.Fixed -> bg.color
                null -> MaterialTheme.colorScheme.background
            }

        // The shadow's color/brush must stay fixed — dropShadow reallocates its blurred shadow
        // layer every time brush/color changes, so an animated multi-stop gradient there (as
        // opposed to just animating cheap scalars like alpha/spread/radius) redraws that layer
        // every frame for as long as isLoading holds, which is a real OOM risk during long
        // generation windows. The "living" color motion instead lives on the border stroke,
        // a cheap draw-phase operation with no shadow layer behind it.
        val borderBrush =
            if (content.compact.isLoading) {
                Brush.horizontalGradient(morphingGradient(isAnimating = true))
            } else {
                MaterialTheme.colorScheme.onBackground.gradientFade()
            }

        Box(
            modifier =
                modifier
                    .dropShadow(shape) {
                        color = shadowColor
                        radius = 25f
                        spread = shadowAlpha
                        alpha = cardAlpha
                    }.clip(shape)
                    .border(1.dp, borderBrush, shape)
                    .background(baseColor.copy(alpha = cardAlpha), shape),
        ) {
            Column(
                modifier = Modifier.wrapContentSize().animateContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CompactIslandLayout(
                    data = content.compact,
                    expanded = effectiveExpanded,
                    modifier =
                        Modifier.pointerInput(content.expandsOnTap, forceExpanded) {
                            detectTapGestures {
                                when {
                                    forceExpanded -> Unit
                                    content.expandsOnTap -> onExpandedChange(!expanded)
                                    else -> content.onAction()
                                }
                            }
                        },
                )

                AnimatedVisibility(
                    visible = effectiveExpanded,
                    enter = fadeIn(IslandFadeTween),
                    exit = fadeOut(IslandFadeTween),
                ) {
                    Box(modifier = Modifier.wrapContentWidth().heightIn(max = maxExpandedHeight)) {
                        content.Expanded(scope)
                    }
                }
            }
        }
    }
}
