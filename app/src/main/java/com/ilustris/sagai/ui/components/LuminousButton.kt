package com.ilustris.sagai.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.ui.animations.rememberLifecycleAnimationsActive
import com.ilustris.sagai.ui.theme.iridescentGradient

private val Ink = Color(0xFF111322)
private const val LIGHT_STEPS = 6
private const val LIGHT_STEP_DP = 4
private const val LIGHT_ALPHA = 0.1f

/**
 * A call to action that is meant to be noticed: a soft, blurry gradient pill that drifts slowly,
 * lit like glass. White light seeps in from the top edge, a glowing oval sits on the bottom edge (a
 * blurred one with a sharp sliver across it, blended to brighten), and a thin translucent rim
 * closes it. The label is dark ink on the pastel fill.
 *
 * The drift and the lights are drawn, never recomposed, and the drift stops when the screen does.
 */
@Composable
fun LuminousButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    colors: List<Color> = iridescentGradient.take(4),
) {
    val shape = CircleShape
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, spring(), label = "luminous_press")
    val drift: State<Float>? =
        if (rememberLifecycleAnimationsActive()) {
            rememberInfiniteTransition(label = "luminous_drift").animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(5200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                label = "luminous_drift_value",
            )
        } else {
            null
        }

    Box(
        modifier =
            modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }.dropShadow(shape) {
                    brush = Brush.horizontalGradient(colors)
                    radius = 36f
                    spread = 2f
                    alpha = 0.5f
                }.clip(shape)
                .drawWithContent {
                    val w = size.width
                    val h = size.height
                    val outline = shape.createOutline(size, layoutDirection, this)

                    // The blurry gradient, sliding sideways and mirroring at its ends.
                    val shift = (drift?.value ?: 0.5f) * w * 0.5f
                    drawRect(
                        Brush.horizontalGradient(
                            colors = colors,
                            startX = -shift,
                            endX = w * 1.1f - shift,
                            tileMode = TileMode.Mirror,
                        ),
                    )

                    // White light seeping in from the top edge: stacked strokes that widen while
                    // they overlap (centered on the edge, the clip drops the outer half).
                    val topLight =
                        Brush.verticalGradient(0f to Color.White.copy(alpha = LIGHT_ALPHA * 1.6f), 0.55f to Color.Transparent)
                    for (step in 1..LIGHT_STEPS) {
                        drawOutline(outline, topLight, style = Stroke(width = (step * LIGHT_STEP_DP).dp.toPx()))
                    }

                    // The oval on the bottom edge: a blurred one, flattened, and a sharp sliver.
                    val cx = w / 2f
                    withTransform({ scale(scaleX = 1f, scaleY = 0.22f, pivot = Offset(cx, h)) }) {
                        drawCircle(
                            brush =
                                Brush.radialGradient(
                                    listOf(Color.White.copy(alpha = 0.8f), Color.Transparent),
                                    center = Offset(cx, h),
                                    radius = w * 0.3f,
                                ),
                            radius = w * 0.3f,
                            center = Offset(cx, h),
                            blendMode = BlendMode.Plus,
                        )
                    }
                    drawOval(
                        color = Color.White.copy(alpha = 0.9f),
                        topLeft = Offset(cx - w * 0.12f, h - 5.dp.toPx()),
                        size = Size(w * 0.24f, 2.dp.toPx()),
                        blendMode = BlendMode.Plus,
                    )

                    drawContent()

                    // The thin translucent rim.
                    drawOutline(
                        outline,
                        Brush.verticalGradient(
                            0f to Color.White.copy(alpha = 0.75f),
                            0.5f to Color.White.copy(alpha = 0.2f),
                            1f to Color.White.copy(alpha = 0.5f),
                        ),
                        style = Stroke(width = 2.dp.toPx()),
                    )
                }.clickable(
                    interactionSource = interaction,
                    indication = null,
                    role = Role.Button,
                    onClick = onClick,
                ).padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            icon?.let {
                Icon(painter = it, contentDescription = null, tint = Ink, modifier = Modifier.size(18.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black, color = Ink),
            )
        }
    }
}
