package com.ilustris.sagai.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
 * Tapping lights it up: the pill lifts, the glows swell and a bloom spreads from the touch point,
 * then it all fades back slowly. The drift, the lights and the tap are drawn, never recomposed, and
 * the drift stops when the screen does.
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

    // The tap's light. It rises fast while pressed and, on release, first makes sure a quick tap
    // still reaches its peak, then fades slowly: the flash is what the player sees, not the hold.
    val lumen = remember { Animatable(0f) }
    LaunchedEffect(pressed) {
        if (pressed) {
            lumen.animateTo(1f, tween(140))
        } else if (lumen.value > 0f) {
            lumen.animateTo(1f, tween(60))
            lumen.animateTo(0f, tween(800, easing = LinearOutSlowInEasing))
        }
    }
    // Where the finger landed: the light blooms from there.
    val touch = remember { mutableStateOf<Offset?>(null) }
    LaunchedEffect(interaction) {
        interaction.interactions.collect { if (it is PressInteraction.Press) touch.value = it.pressPosition }
    }
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
                    alpha = 0.5f + 0.3f * lumen.value
                }.clip(shape)
                .drawWithContent {
                    val w = size.width
                    val h = size.height
                    val outline = shape.createOutline(size, layoutDirection, this)
                    val l = lumen.value
                    val cx = w / 2f

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

                    if (l > 0f) {
                        // The tap: the whole pill lifts a little, and a bloom spreads from the touch.
                        drawRect(Color.White.copy(alpha = 0.2f * l), blendMode = BlendMode.Screen)
                        val origin = touch.value ?: Offset(cx, h / 2f)
                        val bloom = w * 0.45f * (0.5f + 0.5f * l)
                        drawCircle(
                            brush =
                                Brush.radialGradient(
                                    listOf(Color.White.copy(alpha = 0.6f * l), Color.Transparent),
                                    center = origin,
                                    radius = bloom,
                                ),
                            radius = bloom,
                            center = origin,
                            blendMode = BlendMode.Plus,
                        )
                    }

                    // White light seeping in from the top edge: stacked strokes that widen while
                    // they overlap (centered on the edge, the clip drops the outer half).
                    val topLight =
                        Brush.verticalGradient(
                            0f to Color.White.copy(alpha = (LIGHT_ALPHA * 1.6f * (1f + l)).coerceAtMost(1f)),
                            0.55f to Color.Transparent,
                        )
                    for (step in 1..LIGHT_STEPS) {
                        drawOutline(outline, topLight, style = Stroke(width = (step * LIGHT_STEP_DP).dp.toPx()))
                    }

                    // The oval on the bottom edge: a blurred one, flattened, and a sharp sliver. Both
                    // swell with the tap.
                    val ovalRadius = w * (0.3f + 0.12f * l)
                    withTransform({ scale(scaleX = 1f, scaleY = 0.22f + 0.2f * l, pivot = Offset(cx, h)) }) {
                        drawCircle(
                            brush =
                                Brush.radialGradient(
                                    listOf(Color.White.copy(alpha = 0.8f + 0.2f * l), Color.Transparent),
                                    center = Offset(cx, h),
                                    radius = ovalRadius,
                                ),
                            radius = ovalRadius,
                            center = Offset(cx, h),
                            blendMode = BlendMode.Plus,
                        )
                    }
                    drawOval(
                        color = Color.White.copy(alpha = 0.9f),
                        topLeft = Offset(cx - w * 0.12f, h - 5.dp.toPx()),
                        size = Size(w * (0.24f + 0.1f * l), 2.dp.toPx()),
                        blendMode = BlendMode.Plus,
                    )

                    drawContent()

                    // The thin translucent rim.
                    drawOutline(
                        outline,
                        Brush.verticalGradient(
                            0f to Color.White.copy(alpha = (0.75f + 0.25f * l).coerceAtMost(1f)),
                            0.5f to Color.White.copy(alpha = 0.2f + 0.2f * l),
                            1f to Color.White.copy(alpha = 0.5f + 0.4f * l),
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
