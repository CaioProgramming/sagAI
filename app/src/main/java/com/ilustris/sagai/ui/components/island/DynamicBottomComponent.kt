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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.ui.theme.SagAITheme

private val BottomFadeTween = tween<Float>(durationMillis = 220, easing = EaseInOut)

/**
 * Bottom-anchored island overlay — the counterpart to [DynamicIslandOverlay], expanding *upward*
 * from a content-sized compact pill fixed above the navigation bar. Same contract, same
 * no-scrim/no-blur philosophy; honors [IslandContent.hasSurface], [IslandContent.expandsOnTap] and
 * [IslandContent.forceExpanded]. Content reserves space for this via [islandPadding], which is
 * driven purely by presence (content null or not) — not by this overlay's measured size.
 */
@Composable
fun DynamicBottomComponent(
    content: IslandContent?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (content == null) return

    val configuration = LocalConfiguration.current
    val expandedWidth = (configuration.screenWidthDp.dp - 24.dp).coerceAtMost(520.dp)

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

    Box(modifier = modifier.fillMaxSize()) {
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
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .then(if (effectiveExpanded) Modifier.width(expandedWidth) else Modifier.wrapContentWidth())

        SagAITheme(content.compact.genre) {
            val shadowAlpha by animateFloatAsState(
                if (content.compact.isLoading) 1.5f else 1f,
            )
            val cardVisible = content.compact.showBackground || effectiveExpanded
            val cardAlpha by animateFloatAsState(if (cardVisible) 1f else 0f, label = "islandCardAlpha")
            val shape = rememberIslandShape(effectiveExpanded)
            val shadowColor = MaterialTheme.colorScheme.primary
            val bg = content.compact.backgroundColor
            val baseColor = when (bg) {
                IslandBackgroundColor.ThemePrimary -> MaterialTheme.colorScheme.primary
                IslandBackgroundColor.ThemeSurface -> MaterialTheme.colorScheme.surface
                IslandBackgroundColor.ThemeBackground -> MaterialTheme.colorScheme.background
                is IslandBackgroundColor.Fixed -> bg.color
                null -> MaterialTheme.colorScheme.background
            }

            Box(
                modifier =
                    islandModifier
                        .dropShadow(shape) {
                            color = shadowColor
                            radius = 15f
                            spread = shadowAlpha
                            alpha = cardAlpha
                        }.clip(shape)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onBackground.copy(alpha = .1f * cardAlpha),
                            shape,
                        ).background(baseColor.copy(alpha = cardAlpha), shape),
            ) {
                Column(
                    modifier = (if (effectiveExpanded) Modifier.fillMaxWidth() else Modifier.wrapContentWidth()).animateContentSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AnimatedVisibility(
                        visible = effectiveExpanded,
                        // Fade only — the card's animateContentSize is the single source of the size
                        // animation, so the two don't fight (smoother than expand + resize together).
                        enter = fadeIn(BottomFadeTween),
                        exit = fadeOut(BottomFadeTween),
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().heightIn(max = maxExpandedHeight)) {
                            content.Expanded(scope)
                        }
                    }

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
                }
            }
        }
    }
}
