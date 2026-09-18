package com.ilustris.sagai.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

/**
 * A tap-triggered menu styled after iOS's context menu: a rounded, softly-shadowed card that
 * scales in from the corner where it's anchored, instead of Android's default flat rectangle.
 * Built on [Popup] + [AnimatedVisibility] (not Material3's [androidx.compose.material3.DropdownMenu])
 * so the grow/shrink transform origin is explicit and the shrink-out plays fully before the popup
 * is torn down.
 */
@Composable
fun IosStyleMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    anchor: Alignment = Alignment.TopEnd,
    width: Dp = 240.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val transitionState = remember { MutableTransitionState(false) }
    transitionState.targetState = expanded
    val originX = if (anchor == Alignment.TopStart || anchor == Alignment.BottomStart) 0f else 1f
    val originY = if (anchor == Alignment.TopStart || anchor == Alignment.TopEnd) 0f else 1f
    val transformOrigin = TransformOrigin(originX, originY)

    if (transitionState.currentState || transitionState.targetState) {
        Popup(
            alignment = anchor,
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(focusable = true),
        ) {
            AnimatedVisibility(
                visibleState = transitionState,
                enter =
                    fadeIn(tween(200)) +
                        scaleIn(
                            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                            initialScale = 0.75f,
                            transformOrigin = transformOrigin,
                        ),
                exit =
                    fadeOut(tween(140)) +
                        scaleOut(tween(160), targetScale = 0.85f, transformOrigin = transformOrigin),
            ) {
                Surface(
                    modifier = modifier.width(width).padding(4.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = .94f),
                    tonalElevation = 0.dp,
                    shadowElevation = 16.dp,
                ) {
                    Column(content = content)
                }
            }
        }
    }
}

@Composable
fun IosStyleMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    enabled: Boolean = true,
    destructive: Boolean = false,
) {
    val contentColor =
        (if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
            .copy(alpha = if (enabled) 1f else .4f)
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let {
            Icon(painter = it, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
        }
        Text(text = text, style = MaterialTheme.typography.bodyLarge, color = contentColor)
    }
}

@Composable
fun IosStyleMenuDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = .1f), thickness = 0.5.dp)
}
