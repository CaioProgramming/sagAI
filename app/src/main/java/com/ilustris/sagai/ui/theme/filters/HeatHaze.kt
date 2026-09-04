package com.ilustris.sagai.ui.theme.filters

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import com.ilustris.sagai.ui.animations.rememberLifecycleAnimationsActive
import com.ilustris.sagai.ui.theme.themeBrushColors
import kotlinx.coroutines.delay
import timber.log.Timber
import kotlin.math.cos
import kotlin.math.sin

/**
 * How often the haze advances. The motion is meant to be barely perceptible, so a full 60fps buys
 * nothing but heat — at this rate the fold still reads as continuous and the shader runs a third as
 * often.
 */
private const val HAZE_FPS = 24

private const val FRAME_DELAY_MS = 1000L / HAZE_FPS

/** How long the backdrop takes to come up. Slow enough to read as lights coming on. */
private const val FADE_IN_MS = 900

/** Fallback blob travel time for one full orbit. */
private const val FALLBACK_ORBIT_SPEED_MS = 26_000

/**
 * Everything `heat_haze.agsl` takes, in one place.
 *
 * Kept as a single function so the uniforms can be applied *once at construction* to prove they all
 * exist and typecheck, and then applied per-frame from the same code. The first version of this set
 * them only inside the draw phase and guarded only the constructor, so a wrong uniform API sailed
 * past the guard and crashed on the first frame drawn instead of quietly falling back.
 *
 * Colours go through [android.graphics.RuntimeShader.setFloatUniform] as plain RGB rather than
 * `setColorUniform`, which throws for any uniform not declared `layout(color)`. That matches how
 * `genre_filter_shader.agsl` passes its own tints.
 */
private fun RuntimeShader.applyHazeUniforms(
    width: Float,
    height: Float,
    time: Float,
    colorA: Color,
    colorB: Color,
    colorC: Color,
    scale: Float,
    warp: Float,
    glow: Float,
) {
    setFloatUniform("iResolution", width, height)
    setFloatUniform("iTime", time)
    setFloatUniform("u_colorA", colorA.red, colorA.green, colorA.blue)
    setFloatUniform("u_colorB", colorB.red, colorB.green, colorB.blue)
    setFloatUniform("u_colorC", colorC.red, colorC.green, colorC.blue)
    setFloatUniform("u_scale", scale)
    setFloatUniform("u_warp", warp)
    setFloatUniform("u_glow", glow)
}

/**
 * A backdrop that flows: colour bands that bend, drift and fold into one another like heat coming
 * off tarmac, rather than a gradient whose colours cross-fade under stops that never move.
 *
 * That difference is the whole point of this existing next to
 * [com.ilustris.sagai.ui.theme.morphingGradient]. A morphing gradient animates *what colour* each
 * stop is; the geometry underneath it is frozen, which the eye reads as a slideshow. Here the
 * gradient's own coordinate space is displaced by animated noise (see `heat_haze.agsl`), so the
 * bands themselves move.
 *
 * Needs API 33 for [RuntimeShader]. Below that — and whenever the shader can't be built or fed —
 * it falls back to two large radial washes orbiting slowly over a base colour: not the same effect,
 * but movement of the right speed and softness rather than a flat rectangle. [speed] is in
 * noise-field units per second; keep it low, since the warp multiplies any motion you give it.
 */
@Composable
fun HeatHazeBackground(
    modifier: Modifier = Modifier,
    colors: List<Color> = themeBrushColors(),
    speed: Float = 0.05f,
    scale: Float = 2.4f,
    warp: Float = 1.35f,
    glow: Float = 0.06f,
) {
    val palette = remember(colors) { colors.takeIf { it.isNotEmpty() } ?: listOf(Color.Black) }
    val colorA = palette[0]
    val colorB = palette.getOrElse(1) { colorA }
    val colorC = palette.getOrElse(2) { colorB }

    val animating = rememberLifecycleAnimationsActive()

    // The fade lives out here, on a wrapper that outlives the choice below it. Put on either branch
    // it would restart when the shader finishes loading and the fallback hands over — the surface
    // would fade in twice, which reads as a flicker rather than as an entrance.
    val fade = remember { Animatable(0f) }
    LaunchedEffect(Unit) { fade.animateTo(1f, tween(FADE_IN_MS, easing = FastOutSlowInEasing)) }

    Box(
        modifier
            .fillMaxSize()
            .graphicsLayer { alpha = fade.value },
    ) {
        HeatHazeSurface(colorA, colorB, colorC, animating, speed, scale, warp, glow)
    }
}

@Composable
private fun HeatHazeSurface(
    colorA: Color,
    colorB: Color,
    colorC: Color,
    animating: Boolean,
    speed: Float,
    scale: Float,
    warp: Float,
    glow: Float,
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        HeatHazeFallback(Modifier, colorA, colorB, colorC, animating)
        return
    }

    val shaderSource = loadShaderFromAssetsOnce("heat_haze.agsl")
    if (shaderSource == null) {
        // Still loading off the IO dispatcher, or the asset is missing. Either way the fallback is
        // a complete backdrop on its own, so there is never a frame of bare background.
        HeatHazeFallback(Modifier, colorA, colorB, colorC, animating)
        return
    }

    // AGSL is compiled at runtime, so a bad shader — or a uniform this code feeds the wrong way —
    // throws rather than failing the build. Both are proven here, once, against a dummy frame:
    // whatever survives this is safe to drive every frame without a guard in the draw path.
    val shader =
        remember(shaderSource) {
            runCatching {
                RuntimeShader(shaderSource).also {
                    it.applyHazeUniforms(1f, 1f, 0f, colorA, colorB, colorC, scale, warp, glow)
                }
            }.onFailure { Timber.e(it, "heat_haze.agsl unusable — falling back") }
                .getOrNull()
        }

    if (shader == null) {
        HeatHazeFallback(Modifier, colorA, colorB, colorC, animating)
        return
    }

    val brush = remember(shader) { ShaderBrush(shader) }
    var time by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(animating) {
        if (!animating) return@LaunchedEffect
        while (true) {
            delay(FRAME_DELAY_MS)
            time += FRAME_DELAY_MS / 1000f
        }
    }

    Box(
        Modifier.fillMaxSize().drawBehind {
            if (size.minDimension <= 0f) return@drawBehind

            shader.applyHazeUniforms(
                width = size.width,
                height = size.height,
                time = time * speed,
                colorA = colorA,
                colorB = colorB,
                colorC = colorC,
                scale = scale,
                warp = warp,
                glow = glow,
            )

            drawRect(brush)
        },
    )
}

/**
 * The pre-33 stand-in: two wide radial washes orbiting over a base colour. It cannot fold the way
 * the shader does — there is no cheap domain warp without one — but drifting soft light at the same
 * pace keeps the surface alive instead of flat.
 */
@Composable
private fun HeatHazeFallback(
    modifier: Modifier,
    colorA: Color,
    colorB: Color,
    colorC: Color,
    animating: Boolean,
) {
    val transition = rememberInfiniteTransition(label = "heat-haze-fallback")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec =
            infiniteRepeatable(
                animation = tween(FALLBACK_ORBIT_SPEED_MS, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "heat-haze-phase",
    )
    val travel = if (animating) phase else 0f

    Box(
        modifier
            .fillMaxSize()
            .background(colorA)
            .drawBehind {
                val radius = size.maxDimension * 0.75f

                drawRect(
                    Brush.radialGradient(
                        colors = listOf(colorB.copy(alpha = 0.75f), Color.Transparent),
                        center =
                            Offset(
                                size.width * (0.5f + 0.28f * cos(travel)),
                                size.height * (0.5f + 0.22f * sin(travel)),
                            ),
                        radius = radius,
                    ),
                )

                drawRect(
                    Brush.radialGradient(
                        colors = listOf(colorC.copy(alpha = 0.6f), Color.Transparent),
                        center =
                            Offset(
                                size.width * (0.5f - 0.3f * sin(travel * 0.7f)),
                                size.height * (0.5f - 0.26f * cos(travel * 0.55f)),
                            ),
                        radius = radius,
                    ),
                )
            },
    )
}
