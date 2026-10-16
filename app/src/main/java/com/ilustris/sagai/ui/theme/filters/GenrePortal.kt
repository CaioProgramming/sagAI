package com.ilustris.sagai.ui.theme.filters

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import timber.log.Timber
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin

private const val PORTAL_SHADER_ASSET = "genre_portal.agsl"
private const val PORTAL_PATH_STEPS = 120

/** Margin over the farthest corner so a finished portal always covers the whole frame. */
private const val PORTAL_COVER_MARGIN = 1.12f

/**
 * Radius of the portal edge at [angle]. A few slow harmonics rather than random noise so the same
 * function can live in Kotlin (the clip path) and in `genre_portal.agsl` (the ring) and agree to the
 * pixel — change one, change the other. The wobble fades to zero at both ends of the transition
 * (`sin(pi * p)`), which is what guarantees a perfect, full-frame circle when it finishes.
 */
internal fun portalRadius(
    angle: Float,
    progress: Float,
    maxRadius: Float,
    seed: Float,
    wobble: Float,
): Float {
    val env = sin(PI.toFloat() * progress)
    val w =
        sin(2f * angle + seed + progress * 3.1f) * 0.5f +
            sin(3f * angle + seed * 1.7f - progress * 2.3f) * 0.3f +
            sin(5f * angle + seed * 2.3f + progress * 4.1f) * 0.2f
    return progress * maxRadius * (1f + w * wobble * 0.22f * env)
}

private fun portalPath(
    center: Offset,
    progress: Float,
    maxRadius: Float,
    seed: Float,
    wobble: Float,
): Path =
    Path().apply {
        for (i in 0..PORTAL_PATH_STEPS) {
            val angle = (2.0 * PI * i / PORTAL_PATH_STEPS).toFloat()
            val r = portalRadius(angle, progress, maxRadius, seed, wobble)
            val x = center.x + cos(angle) * r
            val y = center.y + sin(angle) * r
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }

private fun portalMaxRadius(
    size: Size,
    center: Offset,
): Float =
    hypot(max(center.x, size.width - center.x), max(center.y, size.height - center.y)) *
        PORTAL_COVER_MARGIN

private fun List<Color>.paddedTo4(): List<Color> {
    if (isEmpty()) return List(4) { Color.White }
    val out = take(4).toMutableList()
    while (out.size < 4) {
        // Palettes with 3 stops get a highlight derived from the last one, like the other genres' 4th.
        val last = out.last()
        out +=
            Color(
                red = last.red * 0.5f + 0.55f,
                green = last.green * 0.5f + 0.55f,
                blue = last.blue * 0.5f + 0.55f,
            )
    }
    // Pure black reads as a hole in the ring and a void in the glow; lift it to a tinted graphite.
    return out.map { if (it.red + it.green + it.blue < 0.05f) Color(0xFF120810) else it }
}

private fun RuntimeShader.applyPortalUniforms(
    size: Size,
    center: Offset,
    progress: Float,
    maxRadius: Float,
    seed: Float,
    wobble: Float,
    warp: Float,
    rim: Float,
    colors: List<Color>,
    primary: Color,
) {
    setFloatUniform("iResolution", size.width, size.height)
    setFloatUniform("u_center", center.x, center.y)
    setFloatUniform("u_progress", progress)
    setFloatUniform("u_maxR", maxRadius)
    setFloatUniform("u_seed", seed)
    setFloatUniform("u_wobble", wobble)
    setFloatUniform("u_warp", warp)
    setFloatUniform("u_rim", rim)
    setFloatUniform("u_c0", colors[0].red, colors[0].green, colors[0].blue)
    setFloatUniform("u_c1", colors[1].red, colors[1].green, colors[1].blue)
    setFloatUniform("u_c2", colors[2].red, colors[2].green, colors[2].blue)
    setFloatUniform("u_c3", colors[3].red, colors[3].green, colors[3].blue)
    setFloatUniform("u_prim", primary.red, primary.green, primary.blue)
}

/**
 * Cross-fades between two full-bleed layers through a portal that opens from [origin].
 *
 * [next] is revealed inside an irregular circle that grows with [progress]; it starts zoomed in and
 * slightly rotated and settles as the portal opens, which reads as depth. The edge is dressed with a
 * ring that cycles through [palette] (the incoming genre's `visualConfig.colorPalette`) and a hot
 * core in [primary].
 *
 * On API 33+ the ring, lens distortion and chromatic split come from `genre_portal.agsl`. Below
 * that, or if the shader can't be built, a stroked ring in the same colours is drawn instead: no
 * refraction, but the same shape of transition.
 *
 * [progress] is a lambda so the animation only invalidates draw / layer, not composition. It should
 * be eased already; this composable does not shape it.
 *
 * @param origin portal centre as a fraction of the size (0..1 on both axes)
 * @param seed any per-transition value; changes the edge's shape
 * @param wobble 0 for a perfect circle, ~0.5 for a restless edge
 * @param warp strength of the lens bend and aberration
 * @param rimFraction ring half-width as a fraction of the height
 */
@Composable
fun GenrePortal(
    progress: () -> Float,
    palette: List<Color>,
    primary: Color,
    modifier: Modifier = Modifier,
    origin: Offset = Offset(0.5f, 0.5f),
    seed: Float = 0f,
    wobble: Float = 0.55f,
    warp: Float = 0.9f,
    rimFraction: Float = 0.035f,
    current: @Composable BoxScope.() -> Unit,
    next: @Composable BoxScope.() -> Unit,
) {
    val colors = remember(palette) { palette.paddedTo4() }
    var size by remember { mutableStateOf(IntSize.Zero) }

    val shaderSource =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            loadShaderFromAssetsOnce(PORTAL_SHADER_ASSET)
        } else {
            null
        }

    // Same defence as HeatHaze: AGSL is compiled at runtime, so prove the source and every uniform
    // against a dummy frame once; anything that survives is safe to drive per frame.
    val shader =
        remember(shaderSource) {
            shaderSource?.let { source ->
                runCatching {
                    RuntimeShader(source).also {
                        it.applyPortalUniforms(
                            Size(1f, 1f),
                            Offset.Zero,
                            0.5f,
                            1f,
                            0f,
                            0f,
                            0f,
                            1f,
                            List(4) { Color.White },
                            Color.White,
                        )
                    }
                }.onFailure { Timber.e(it, "genre_portal.agsl unusable — using stroked ring") }
                    .getOrNull()
            }
        }

    Box(
        modifier
            .onSizeChanged { size = it }
            .graphicsLayer {
                val p = progress()
                if (shader == null || size.width <= 0 || size.height <= 0 || p <= 0f || p >= 1f) {
                    renderEffect = null
                    return@graphicsLayer
                }
                val sizePx = Size(size.width.toFloat(), size.height.toFloat())
                val center = Offset(origin.x * sizePx.width, origin.y * sizePx.height)
                shader.applyPortalUniforms(
                    size = sizePx,
                    center = center,
                    progress = p,
                    maxRadius = portalMaxRadius(sizePx, center),
                    seed = seed,
                    wobble = wobble,
                    warp = warp,
                    rim = rimFraction * sizePx.height,
                    colors = colors,
                    primary = primary,
                )
                renderEffect =
                    RenderEffect
                        .createRuntimeShaderEffect(shader, "composable_shader")
                        .asComposeRenderEffect()
            }.drawWithContent {
                drawContent()
                val p = progress()
                if (shader == null && p > 0f && p < 1f) {
                    val center = Offset(origin.x * this.size.width, origin.y * this.size.height)
                    val maxR = portalMaxRadius(this.size, center)
                    val ring = portalPath(center, p, maxR, seed, wobble)
                    val env = sin(PI.toFloat() * p)
                    val rim = rimFraction * this.size.height
                    val sweep = Brush.sweepGradient(colors + colors.first(), center)
                    drawPath(ring, sweep, alpha = 0.18f * env, style = Stroke(width = rim * 6f))
                    drawPath(ring, sweep, alpha = 0.45f * env, style = Stroke(width = rim * 2.5f))
                    drawPath(ring, primary.copy(alpha = env), style = Stroke(width = rim * 0.4f))
                }
            },
    ) {
        current()

        Box(
            Modifier
                .fillMaxSize()
                .drawWithContent {
                    val p = progress()
                    if (p <= 0f) return@drawWithContent
                    val center = Offset(origin.x * this.size.width, origin.y * this.size.height)
                    val path =
                        portalPath(center, p, portalMaxRadius(this.size, center), seed, wobble)
                    clipPath(path) { this@drawWithContent.drawContent() }
                },
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val p = progress().coerceIn(0f, 1f)
                        // Arrives from further in and untwists as the portal opens.
                        val zoom = 1.35f - 0.35f * p
                        scaleX = zoom
                        scaleY = zoom
                        rotationZ = (1f - p) * 10f * warp
                        transformOrigin = TransformOrigin(origin.x, origin.y)
                    },
            ) {
                next()
            }
        }
    }
}
