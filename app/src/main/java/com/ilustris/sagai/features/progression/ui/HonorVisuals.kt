package com.ilustris.sagai.features.progression.ui

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** The colors of a genre's coin and badge: a lit outer rim, a darker inner field, and the engraving. */
private data class HonorPalette(
    val outerTop: Color,
    val outerBottom: Color,
    val innerTop: Color,
    val innerBottom: Color,
    val shadow: Color,
    val engrave: Color,
    val edge: Color,
)

private fun p(
    outerTop: Long,
    outerBottom: Long,
    innerTop: Long,
    innerBottom: Long,
    shadow: Long,
    engrave: Long,
    edge: Long,
) = HonorPalette(
    Color(0xFF000000 or outerTop), Color(0xFF000000 or outerBottom), Color(0xFF000000 or innerTop),
    Color(0xFF000000 or innerBottom), Color(0xFF000000 or shadow), Color(0xFF000000 or engrave), Color(0xFF000000 or edge),
)

private fun Genre.honorPalette(): HonorPalette =
    when (this) {
        Genre.COWBOY -> p(0xe1cfc2, 0xb0815f, 0xa26a42, 0x61300d, 0x4c260a, 0x45210a, 0x562b0c)
        Genre.CRIME -> p(0xf9c4d6, 0xf06695, 0xed4b82, 0xa31545, 0x801036, 0x730f33, 0x90133d)
        Genre.CYBERPUNK -> p(0xe1bdff, 0xb052ff, 0xa233ff, 0x6100b2, 0x4c008c, 0x450080, 0x56009e)
        Genre.FANTASY -> p(0xe1c7ca, 0xb06b76, 0xa2515d, 0x611b25, 0x4c151d, 0x45121a, 0x561821)
        Genre.HEROES -> p(0xffeea8, 0xf2b035, 0xfcc552, 0xe07f14, 0xa8620f, 0x8a4a05, 0xb8721a)
        Genre.HORROR -> p(0xd2d5e1, 0x8992b1, 0x747ea3, 0x394262, 0x2d344d, 0x292e45, 0x323a57)
        Genre.PUNK_ROCK -> p(0xbdead2, 0x52c988, 0x33c073, 0x007b38, 0x00612c, 0x005929, 0x006d32)
        Genre.SHINOBI -> p(0xd5c7d2, 0x906c89, 0x7d5274, 0x401b39, 0x33152d, 0x2e1429, 0x391832)
        Genre.SPACE_OPERA -> p(0xbddee8, 0x52a9c3, 0x339ab9, 0x005a75, 0x00475c, 0x004054, 0x005068)
    }

/** The lit rim color of a genre, for text and accents that should match its coins. */
fun Genre.honorHighlight(): Color = honorPalette().outerTop

/** The gold-like fill of a genre's level pill and the dark engraving color that reads on it. */
fun Genre.honorFill(): Pair<Color, Color> = honorPalette().let { it.outerTop to it.engrave }

fun Genre.honorFillBottom(): Color = honorPalette().outerBottom

private const val SEAL_PATH =
    "M18.08,3.51 Q20,1.2 21.92,3.51 L24.48,6.6 Q26.4,8.91 29.36,9.42 L33.32,10.09 Q36.28,10.6 35.24,13.42 " +
        "L33.84,17.18 Q32.8,20 33.84,22.82 L35.24,26.58 Q36.28,29.4 33.32,29.91 L29.36,30.58 Q26.4,31.09 24.48,33.4 " +
        "L21.92,36.49 Q20,38.8 18.08,36.49 L15.52,33.4 Q13.6,31.09 10.64,30.58 L6.68,29.91 Q3.72,29.4 4.76,26.58 " +
        "L6.16,22.82 Q7.2,20 6.16,17.18 L4.76,13.42 Q3.72,10.6 6.68,10.09 L10.64,9.42 Q13.6,8.91 15.52,6.6Z"

private val StreakA = "M5 13L14 3.5L17 3.5L7 15Z"
private val StreakB = "M25 35L35 25L37 27.5L28 36.5Z"
private val Cream = Color(0xFFFFECB8)

private fun path(d: String): Path = PathParser().parsePathString(d).toPath()

/** A four-point sparkle of radius [r] at the origin; the inner points sit at a fifth of the radius. */
private fun DrawScope.sparkle(
    center: Offset,
    r: Float,
    alpha: Float,
    turn: Float,
) {
    if (alpha <= 0f) return
    val k = r * .2f
    val star =
        Path().apply {
            moveTo(0f, -r); lineTo(k, -k); lineTo(r, 0f); lineTo(k, k)
            lineTo(0f, r); lineTo(-k, k); lineTo(-r, 0f); lineTo(-k, -k); close()
        }
    withTransform({ translate(center.x, center.y); rotate(turn, Offset.Zero) }) { drawPath(star, Color.White.copy(alpha = alpha)) }
}

/** Twinkle curve: 0 -> 1 -> 0 over one period, the way the mockup's sparks grow and fade. */
private fun twinkle(phase: Float): Float = sin(((phase % 1f) + 1f) % 1f * PI.toFloat()).coerceAtLeast(0f)

/** Draws [content] as a flat silhouette of [color] with a light edge underneath, so an emoji reads as engraved in the metal. */
private fun Modifier.engraved(
    color: Color,
    lightDy: Float,
) = drawWithContent {
    drawIntoCanvas { canvas ->
        val bounds = Rect(-size.width, -size.height, size.width * 2, size.height * 2)
        val light = Paint().apply { colorFilter = ColorFilter.tint(Color.White.copy(alpha = .5f), BlendMode.SrcIn) }
        canvas.save()
        canvas.translate(0f, lightDy)
        canvas.saveLayer(bounds, light)
        drawContent()
        canvas.restore()
        canvas.restore()
        val dark = Paint().apply { colorFilter = ColorFilter.tint(color, BlendMode.SrcIn) }
        canvas.saveLayer(bounds, dark)
        drawContent()
        canvas.restore()
    }
}

/**
 * The chapter medal: a coin in the genre's palette (lit rim, darker field, soft highlight streaks) with the
 * emoji of the deed engraved in it as a silhouette and a spark that twinkles on the rim.
 */
@Composable
fun MedalCoin(
    emoji: String?,
    genre: Genre,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
) {
    val palette = remember(genre) { genre.honorPalette() }
    val streaks = remember { path(StreakA) to path(StreakB) }
    val transition = rememberInfiniteTransition(label = "medal")
    val phase by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(2600, easing = LinearEasing)), label = "spark")
    Box(
        modifier
            .size(size)
            .drawBehind { drawCircle(Brush.radialGradient(listOf(palette.outerBottom.copy(alpha = .4f), Color.Transparent)), this.size.minDimension * .68f) },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(size)) {
            scale(this.size.width / 40f, Offset.Zero) {
                drawOval(palette.shadow, Offset(3f, 4.4f), Size(34f, 33.6f))
                val c = Offset(20f, 19f)
                drawCircle(Brush.verticalGradient(listOf(palette.outerTop, palette.outerBottom), 2f, 36f), 17f, c)
                drawCircle(Brush.verticalGradient(listOf(palette.innerTop, palette.innerBottom), 4.8f, 33.2f), 14.2f, c)
                drawCircle(Brush.verticalGradient(listOf(palette.outerTop, palette.outerBottom), 7.4f, 30.6f), 11.6f, c)
                clipPath(Path().apply { addOval(Rect(c, 17f)) }) {
                    drawPath(streaks.first, Color.White.copy(alpha = .4f))
                    drawPath(streaks.second, Color.White.copy(alpha = .3f))
                }
                sparkle(Offset(31.5f, 8.8f), 3.6f, twinkle(phase), phase * 36f)
            }
        }
        Text(
            text = emoji?.takeIf { it.isNotBlank() } ?: "🏅",
            fontSize = (size.value * .4f).sp,
            modifier = Modifier.engraved(palette.engrave, with(androidx.compose.ui.platform.LocalDensity.current) { (size.toPx() * .02f) }),
        )
    }
}

/**
 * The act legacy: a thick seal in the genre's palette with the genre's own icon embossed on it, turning slowly
 * around its vertical axis, with a highlight sweep and sparks, levitating a little. Drawn as stacked layers so the
 * rim shows depth while it turns.
 */
@Composable
fun LegacyBadge3D(
    genre: Genre,
    modifier: Modifier = Modifier,
    size: Dp = 104.dp,
    levitate: Boolean = true,
) {
    val palette = remember(genre) { genre.honorPalette() }
    val seal = remember { path(SEAL_PATH) }
    val icon: Painter = painterResource(genre.icon)
    val transition = rememberInfiniteTransition(label = "legacy")
    val angle by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(9000, easing = LinearEasing)), label = "spin")
    val sweep by transition.animateFloat(
        -14f,
        52f,
        infiniteRepeatable(keyframes { durationMillis = 10000; -14f at 0; 52f at 1400 using EaseInOut; 52f at 10000 }),
        label = "sweep",
    )
    val sparks by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(2200, easing = LinearEasing)), label = "sparks")
    val float by transition.animateFloat(-1f, 1f, infiniteRepeatable(tween(3200, easing = EaseInOut), RepeatMode.Reverse), label = "float")

    Canvas(modifier.size(size)) {
        val s = this.size.width / 40f
        val theta = angle * PI.toFloat() / 180f
        val c = cos(theta)
        val sn = sin(theta)
        val lift = if (levitate) float * this.size.width * .03f else 0f
        val thickness = 5.5f * 40f / 104f

        translate(0f, lift) {
            drawCircle(
                Brush.radialGradient(listOf(palette.outerBottom.copy(alpha = .45f), Color.Transparent), center = center, radius = this.size.width * .62f),
                this.size.width * .62f,
                center,
            )
            fun face(z: Float, block: DrawScope.() -> Unit) =
                withTransform({
                    scale(s, s, Offset.Zero)
                    translate(z * sn, 0f)
                    scale(abs(c).coerceAtLeast(.001f), 1f, Offset(20f, 20f))
                }) { block() }

            val layers = (-5..5).map { it * thickness / 5f }
            (if (c >= 0f) layers else layers.reversed()).forEach { z -> face(z) { drawPath(seal, palette.edge) } }

            val z = if (c >= 0f) 2.3f else -2.3f
            face(z) {
                drawPath(seal, Brush.verticalGradient(listOf(palette.outerTop, palette.outerBottom), 1.2f, 38.8f))
                withTransform({ scale(.7f, .7f, Offset(20f, 20f)) }) {
                    drawPath(seal, Brush.verticalGradient(listOf(palette.innerTop, palette.innerBottom), 1.2f, 38.8f))
                }
                clipPath(seal) {
                    withTransform({ rotate(-24f, Offset(13f, 8f)) }) {
                        drawOval(
                            Brush.verticalGradient(listOf(Color.White.copy(alpha = .75f), Color.Transparent), -1f, 17f),
                            Offset(-5f, -1f),
                            Size(36f, 18f),
                        )
                    }
                    val sweepBand =
                        Path().apply {
                            moveTo(sweep + 7f, -2f); lineTo(sweep + 13f, -2f); lineTo(sweep + 6f, 44f); lineTo(sweep, 44f); close()
                        }
                    drawPath(sweepBand, Color.White.copy(alpha = .85f))
                }
            }
            // The icon is a vector: drawn inside the scaled face it would be rasterized at 13 px and then stretched,
            // so it is drawn at its real pixel size and only squashed horizontally with the turn.
            withTransform({
                translate(z * sn * s, 0f)
                scale(abs(c).coerceAtLeast(.001f), 1f, Offset(20f * s, 20f * s))
            }) {
                translate(13.25f * s, (13.25f + .9f) * s) {
                    with(icon) { draw(Size(13.5f * s, 13.5f * s), colorFilter = ColorFilter.tint(palette.engrave.copy(alpha = .85f))) }
                }
                translate(13.25f * s, 13.25f * s) {
                    with(icon) { draw(Size(13.5f * s, 13.5f * s), colorFilter = ColorFilter.tint(Cream)) }
                }
            }
            face(z) {
                repeat(5) { i ->
                    val a = (i * 72f + 17f) * PI.toFloat() / 180f
                    val r = 15f + (i % 3) * 3f
                    sparkle(Offset(20f + r * cos(a), 20f + r * sin(a)), 2.4f + (i % 2) * .8f, twinkle(sparks * (1f + i * .13f) + i * .31f), sparks * 90f + i * 20f)
                }
            }
        }
    }
}

/** The colour of a tier. The number itself is internal and never shown: only the two heaviest tiers carry a colour, the rest stay neutral so a coloured star means a rare feat. */
fun tierColor(tier: Int): Color =
    when (tier) {
        4 -> Color(0xFFFFC54D)
        5 -> Color(0xFFFF6B4A)
        else -> Color(0xFF8E8E93)
    }

/** A feat's star, coloured by its tier; the two heaviest tiers glow. */
@Composable
fun TierStar(
    tier: Int,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
) {
    val color = tierColor(tier)
    androidx.compose.material3.Icon(
        painter = painterResource(com.ilustris.sagai.R.drawable.ic_spark),
        contentDescription = null,
        tint = color,
        modifier =
            modifier
                .size(size)
                .let {
                    if (tier >= 4) {
                        it.drawBehind { drawCircle(Brush.radialGradient(listOf(color.copy(alpha = .5f), Color.Transparent)), this.size.minDimension * .9f) }
                    } else {
                        it
                    }
                },
    )
}
