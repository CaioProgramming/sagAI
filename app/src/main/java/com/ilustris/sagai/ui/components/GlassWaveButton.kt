package com.ilustris.sagai.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.ui.animations.rememberLifecycleAnimationsActive
import com.ilustris.sagai.ui.theme.themeBrushColors
import kotlin.math.PI
import kotlin.math.sin

private const val WAVE_STEPS = 48
private const val WAVE_SECONDS = 4.6f
private val WAVE_BLUR = 5.dp

/**
 * A quiet call to action: the primary colour as its body, a small wave of the theme gradient moving
 * inside it, and a glow in the primary colour around it.
 *
 * - The body is [MaterialTheme.colorScheme.primary] and the label is `onPrimary`, so the text reads on
 *   any theme.
 * - The wave is the gradient ([colors], the theme's by default) filled below two sine curves that
 *   travel at different speeds, blurred so it reads as light in motion, while the gradient itself
 *   drifts sideways. It stops with the screen.
 * - A tap lifts the glow, spreads a bloom from the finger and presses the button in a little.
 */
@Composable
fun GlassWaveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    colors: List<Color> = themeBrushColors(),
    glow: Color = MaterialTheme.colorScheme.primary,
    textStyle: TextStyle = MaterialTheme.typography.labelMedium,
) {
    val shape = CircleShape
    val blurMargin = with(LocalDensity.current) { (WAVE_BLUR * 3).roundToPx() }
    val base = MaterialTheme.colorScheme.primary
    val content = MaterialTheme.colorScheme.onPrimary
    // Near-black backdrop tints at the head of a palette would only dirty the wave.
    val waveColors =
        remember(colors) {
            colors
                .filter { it.luminance() > 0.03f }
                .let { lit ->
                    when {
                        lit.size >= 2 -> lit
                        lit.size == 1 -> listOf(lit[0], lerp(lit[0], Color.White, 0.4f))
                        else -> listOf(Color(0xFF6EA8FF), Color(0xFFFF8FD0))
                    }
                }
        }

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, spring(), label = "glass_press")
    val lumen = remember { Animatable(0f) }
    LaunchedEffect(pressed) {
        if (pressed) {
            lumen.animateTo(1f, tween(140))
        } else if (lumen.value > 0f) {
            lumen.animateTo(1f, tween(60))
            lumen.animateTo(0f, tween(800, easing = LinearOutSlowInEasing))
        }
    }
    val touch = remember { mutableStateOf<Offset?>(null) }
    LaunchedEffect(interaction) {
        interaction.interactions.collect { if (it is PressInteraction.Press) touch.value = it.pressPosition }
    }

    val active = rememberLifecycleAnimationsActive()
    val transition = rememberInfiniteTransition(label = "glass_wave")
    val phase: State<Float>? =
        if (active) {
            transition.animateFloat(
                initialValue = 0f,
                targetValue = 2f * PI.toFloat(),
                animationSpec = infiniteRepeatable(tween((WAVE_SECONDS * 1000).toInt(), easing = LinearEasing)),
                label = "glass_wave_phase",
            )
        } else {
            null
        }
    val drift: State<Float>? =
        if (active) {
            transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(7200, easing = LinearEasing), RepeatMode.Reverse),
                label = "glass_wave_drift",
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
                    color = glow
                    radius = 36f
                    spread = 2f
                    alpha = 0.5f + 0.3f * lumen.value
                }.clip(shape)
                .drawWithContent {
                    val w = size.width
                    val h = size.height
                    val l = lumen.value

                    drawRect(base)
                    drawContent()

                    // Glass: light along the top edge and a soft sheen.
                    drawRect(
                        Brush.verticalGradient(
                            0f to Color.White.copy(alpha = 0.22f),
                            0.5f to Color.Transparent,
                        ),
                    )

                    if (l > 0f) {
                        val origin = touch.value ?: Offset(w / 2f, h / 2f)
                        val bloom = w * 0.45f * (0.5f + 0.5f * l)
                        drawCircle(
                            brush =
                                Brush.radialGradient(
                                    listOf(Color.White.copy(alpha = 0.3f * l), Color.Transparent),
                                    center = origin,
                                    radius = bloom,
                                ),
                            radius = bloom,
                            center = origin,
                            blendMode = BlendMode.Plus,
                        )
                    }

                    drawOutline(
                        shape.createOutline(size, layoutDirection, this),
                        Brush.verticalGradient(
                            0f to Color.White.copy(alpha = (0.7f + 0.25f * l).coerceAtMost(1f)),
                            0.5f to Color.White.copy(alpha = 0.18f + 0.2f * l),
                            1f to glow.copy(alpha = 0.55f + 0.4f * l),
                        ),
                        style = Stroke(width = 1.5.dp.toPx()),
                    )
                }.clickable(
                    interactionSource = interaction,
                    indication = null,
                    role = Role.Button,
                    onClick = onClick,
                ).padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        // The waves live in their own layer so only they are blurred: a fluid glow, not a flat shape.
        // The layer is larger than the button by a blur margin, so the blur never thins the visible
        // edges; the button's clip cuts the excess.
        Box(
            Modifier
                .matchParentSize()
                .layout { measurable, constraints ->
                    val w = constraints.maxWidth
                    val h = constraints.maxHeight
                    val layer = measurable.measure(Constraints.fixed(w + 2 * blurMargin, h + 2 * blurMargin))
                    layout(w, h) { layer.place(-blurMargin, -blurMargin) }
                }.blur(WAVE_BLUR, BlurredEdgeTreatment.Unbounded)
                .drawBehind {
                    val m = blurMargin.toFloat()
                    val w = size.width - 2f * m
                    val h = size.height - 2f * m
                    val shift = (drift?.value ?: 0.5f) * w * 0.5f
                    val gradient =
                        Brush.horizontalGradient(
                            colors = waveColors,
                            startX = m - shift,
                            endX = m + w * 1.1f - shift,
                            tileMode = TileMode.Mirror,
                        )
                    val p = phase?.value ?: 1.2f
                    val l = lumen.value
                    drawWave(gradient, m, w, m + h * 0.56f, h * 0.14f, 1.4f, p, 0.85f + 0.15f * l)
                    drawWave(gradient, m, w, m + h * 0.7f, h * 0.1f, 2.1f, -p * 1.35f + 1.8f, 0.65f + 0.2f * l)
                },
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            icon?.let {
                Icon(painter = it, contentDescription = null, tint = content, modifier = Modifier.size(15.dp))
            }
            Text(
                text = text,
                style = textStyle.copy(fontWeight = FontWeight.Black, color = content),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Fills everything under a sine curve with [brush], across the whole layer. The curve is measured on
 * the button's own box: [left] and [width] are where that box sits in the layer, [base] is the mean
 * height of the curve in layer coordinates.
 */
private fun DrawScope.drawWave(
    brush: Brush,
    left: Float,
    width: Float,
    base: Float,
    amplitude: Float,
    cycles: Float,
    phase: Float,
    alpha: Float,
) {
    val path =
        Path().apply {
            moveTo(0f, size.height)
            for (i in 0..WAVE_STEPS) {
                val x = size.width * i / WAVE_STEPS
                lineTo(x, base + amplitude * sin(2f * PI.toFloat() * cycles * (x - left) / width + phase))
            }
            lineTo(size.width, size.height)
            close()
        }
    drawPath(path, brush, alpha = alpha.coerceIn(0f, 1f))
}
