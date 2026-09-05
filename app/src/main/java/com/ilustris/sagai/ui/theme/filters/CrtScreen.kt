package com.ilustris.sagai.ui.theme.filters

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.animations.rememberLifecycleAnimationsActive
import kotlinx.coroutines.delay

/**
 * How the panel is set up. The defaults are a phosphor dot-matrix display: a visible grid of lit
 * dots with light bleeding across the gaps between them, flat rather than curved.
 *
 * [curvature] is off by default. It exists because a tube's glass genuinely bends its picture, but
 * on a phone the bulge reads as a fisheye lens applied to a screenshot rather than as a screen —
 * the dot grid and the bloom carry the illusion on their own.
 */
data class CrtSettings(
    val curvature: Float = 0f,
    /** Dot spacing in physical pixels — larger reads as a coarser, older panel. */
    val dotPitch: Float = 4f,
    val dotIntensity: Float = 0.45f,
    val dotRadius: Float = 0.34f,
    /** Scanlines share the dot grid's rows — they have no pitch of their own, only a depth. */
    val scanlineIntensity: Float = 0.12f,
    /**
     * Horizontal RGB split in physical pixels, off by default.
     *
     * Beam misconvergence is real on a CRT, but on a set anyone actually watched it was a gentle
     * misregistration of the whole picture toward the corners — not something you could point at.
     * The softness people remember from those screens comes from the beam spot being wider than
     * the pixel it lights, which is [bloomRadius], not from the channels separating.
     */
    val aberration: Float = 0f,
    val bloomRadius: Float = 3f,
    val bloomIntensity: Float = 0.55f,
    val grain: Float = 0.03f,
    /**
     * How often the time-varying parts redraw. This is the effect's whole cost profile: the grid,
     * the bloom and the vignette do not depend on time at all, so every extra tick is a full-screen
     * shader pass bought for nothing but moving grain. Twelve reads as a machine refreshing; sixty
     * just heats the phone.
     */
    val animationFps: Int = 12,
    /**
     * Off by default. A drifting refresh band is authentic to a CRT photographed by a rolling
     * shutter, but over a near-black terminal it is simply a bright horizontal line crossing the
     * screen with nothing to justify it — the dot grid already carries the display.
     */
    val rollIntensity: Float = 0f,
    val vignette: Float = 0.5f,
    /**
     * Off by default. At a visible amplitude it reads as a fault rather than as a live tube, and
     * at an invisible one it still costs a redraw of the entire screen on every tick — the worst
     * of both. Kept as a knob for a deliberately failing display.
     */
    val flicker: Float = 0f,
) {
    companion object {
        /**
         * Tuning for a single element — a headline, a portrait — rather than a whole screen.
         *
         * Everything time-varying is off, which is the point: with no clock the effect never
         * republishes itself, so it costs one shader pass when the content changes and nothing at
         * all while the element just sits there. That is what makes it affordable to hang on text
         * and images across ordinary screens instead of only inside a dedicated flow.
         *
         * The vignette goes too: darkening the corners of a *screen* reads as a tube, darkening
         * the corners of a portrait just reads as a badly exposed photo.
         */
        val Element =
            CrtSettings(
                dotPitch = 3f,
                dotIntensity = 0.38f,
                scanlineIntensity = 0.1f,
                bloomRadius = 2.5f,
                bloomIntensity = 0.6f,
                grain = 0f,
                vignette = 0f,
                flicker = 0f,
                rollIntensity = 0f,
            )

        /**
         * A screen remembered rather than looked at — bloom doing most of the work, the grid and
         * the scanlines barely there.
         *
         * The proportions are inverted from [Element] on purpose. Where that preset wants you to
         * see a panel, this one wants the panel to be something you only notice afterwards: the
         * dots drop to a whisper and the bloom spreads twice as far, which is what turns a display
         * into a haze. The small channel split is the only hard edge left in it — just enough
         * wrongness for the haze to read as corrupted rather than as soft focus.
         */
        val Dream =
            CrtSettings(
                dotPitch = 4f,
                dotIntensity = 0.09f,
                scanlineIntensity = 0.04f,
                aberration = 1f,
                bloomRadius = 3.5f,
                bloomIntensity = 0.4f,
                grain = 0f,
                vignette = 0f,
                flicker = 0f,
                rollIntensity = 0f,
            )

        /**
         * [Dream] with the display taken out of it: halo and channel split only, no dot grid and no
         * scanlines.
         *
         * This is the "dreamy blur" look — a soft bloom around bright edges with the colour
         * channels barely out of register — for surfaces that are *not* pretending to be a screen.
         * A photo on a table is one of those: the haze belongs to the lens and the light, not to a
         * tube, and the moment a phosphor grid appears over it the object stops being paper.
         *
         * Worth knowing what this is and is not. It is not a Gaussian blur: the shader gathers four
         * neighbouring samples and adds them back as light, so bright areas spread and dark ones
         * stay put. A true blur of this quality would need tens of taps per pixel, which is the
         * expensive thing people mean when they say blur is hard on Android — and it would also
         * make the text underneath unreadable. Softness that only bright things emit is both the
         * cheaper effect and the one that actually reads as dreamy.
         *
         * Time-independent, so it costs one shader pass when the content changes and nothing while
         * it sits still.
         */
        val SoftFocus =
            CrtSettings(
                dotIntensity = 0f,
                scanlineIntensity = 0f,
                aberration = 1.4f,
                bloomRadius = 5f,
                bloomIntensity = 0.45f,
                grain = 0f,
                vignette = 0f,
                flicker = 0f,
                rollIntensity = 0f,
            )
    }
}

/** The CRT tuning that belongs to a genre, or null for genres that don't wear one. */
fun Genre.crtPreset(): CrtSettings? =
    when (this) {
        Genre.SPACE_OPERA -> CrtSettings.Element
        Genre.CYBERPUNK -> CrtSettings.Dream
        else -> null
    }

/**
 * The CRT panel treatment, applied only for the genre whose identity it is.
 *
 * Kept as an explicit opt-in per call site rather than folded into a generic image modifier: the
 * other genres have their own VFX, most of which animate, and quietly attaching per-genre effects
 * to every hero image would turn one genre's look into a cost every genre pays.
 */
@Composable
fun Modifier.genreCrtScreen(genre: Genre?): Modifier {
    val preset = genre?.crtPreset() ?: return this
    return crtScreen(settings = preset)
}

/**
 * Soft bloom and a slight channel split over whatever it wraps — [CrtSettings.SoftFocus], which is
 * the CRT shader with the display taken out of it. See that preset for why this is a halo rather
 * than a real blur, and why the halo is the effect you actually want here.
 *
 * Apply it to individual elements — a photo, a portrait — rather than to a large scrolling
 * container. It is a [RenderEffect], so everything inside the layer it is attached to is re-rendered
 * through the shader on every frame that layer changes; hanging it on a continuously moving surface
 * means paying for the whole surface, every frame, forever.
 *
 * No-op below API 33, like everything else built on [RuntimeShader].
 */
@Composable
fun Modifier.dreamyHaze(settings: CrtSettings = CrtSettings.SoftFocus): Modifier = crtScreen(settings = settings)

/**
 * Renders its content as if it were being shown on a phosphor dot-matrix panel.
 *
 * The reason this is a shader rather than an overlay: the glow has to be gathered from the content
 * *before* the dot grid darkens it, and added back *after*. That ordering is what sells it — light
 * spilling across the unlit gaps is what makes a grid of dots read as glowing emitters instead of
 * as a screen door laid over a screenshot. An overlay can only ever be the screen door, because it
 * has no access to what is underneath it.
 *
 * Dot pitch and scanline frequency are computed in device pixels rather than in normalised
 * coordinates, so the grid holds its apparent size across screen densities.
 *
 * Requires API 33 for [RuntimeShader]; below that this is a no-op and callers should keep whatever
 * drawn approximation they had (see [com.ilustris.sagai.ui.genre.terminal.TerminalBackground]).
 * Animation stops with the lifecycle, so a backgrounded screen isn't driving a per-frame shader.
 */
@Composable
fun Modifier.crtScreen(
    isPlaying: Boolean = true,
    settings: CrtSettings = CrtSettings(),
): Modifier =
    composed {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return@composed this

        val shaderSource = loadShaderFromAssetsOnce("crt_screen.agsl") ?: return@composed this
        val shader = remember(shaderSource) { RuntimeShader(shaderSource) }

        var size by remember { mutableStateOf(IntSize.Zero) }
        var time by remember { mutableFloatStateOf(0f) }

        // Only the grain, the roll band and the flicker read the clock. When all three are off —
        // which is the default — the effect is entirely static, and driving a clock would republish
        // the render effect (and so redraw the whole screen through the shader) sixty times a
        // second to produce an identical image. With none of them on, the shader runs only when the
        // content underneath it actually changes.
        val needsClock =
            settings.grain > 0f || settings.flicker > 0f || settings.rollIntensity > 0f
        val animating = isPlaying && needsClock && rememberLifecycleAnimationsActive()
        val stepMs = remember(settings.animationFps) {
            (1000L / settings.animationFps.coerceAtLeast(1)).coerceAtLeast(1L)
        }

        LaunchedEffect(animating, stepMs) {
            if (!animating) return@LaunchedEffect
            while (true) {
                delay(stepMs)
                time += stepMs / 1000f
            }
        }

        this
            .onSizeChanged { size = it }
            .graphicsLayer {
                if (size.width <= 0 || size.height <= 0) {
                    renderEffect = null
                    return@graphicsLayer
                }

                shader.setFloatUniform("iResolution", size.width.toFloat(), size.height.toFloat())
                shader.setFloatUniform("iTime", time)
                shader.setFloatUniform("u_curvature", settings.curvature)
                shader.setFloatUniform("u_dotPitch", settings.dotPitch)
                shader.setFloatUniform("u_dotIntensity", settings.dotIntensity)
                shader.setFloatUniform("u_dotRadius", settings.dotRadius)
                shader.setFloatUniform("u_scanlineIntensity", settings.scanlineIntensity)
                shader.setFloatUniform("u_aberration", settings.aberration)
                shader.setFloatUniform("u_bloomRadius", settings.bloomRadius)
                shader.setFloatUniform("u_bloomIntensity", settings.bloomIntensity)
                shader.setFloatUniform("u_grain", if (animating) settings.grain else 0f)
                shader.setFloatUniform("u_rollIntensity", settings.rollIntensity)
                shader.setFloatUniform("u_vignette", settings.vignette)
                shader.setFloatUniform("u_flicker", if (animating) settings.flicker else 0f)

                renderEffect =
                    RenderEffect
                        .createRuntimeShaderEffect(shader, "composable_shader")
                        .asComposeRenderEffect()
            }
    }
