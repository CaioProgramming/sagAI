package com.ilustris.sagai.ui.genre.collage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay

/** Forced paper white — deliberately *not* theme-derived: a torn scrap only reads as paper if it contrasts hard against the page it was dropped on. */
val PAPER_WHITE = Color(0xFFF6F2E9)

/** The raw pulp exposed along a tear, brighter than the printed surface. */
private val PAPER_PULP = Color(0xFFFFFDF7)
val PAPER_INK = Color(0xFF14120F)

private const val TEAR_STEPS = 110
private const val REVEAL_STEPS = 9
private const val REVEAL_STEP_MS = 55L

/** Deterministic integer hash → `[0,1)`. */
private fun hash1(
    i: Int,
    seed: Int,
): Float {
    var h = i * 374761393 + seed * 668265263
    h = (h xor (h ushr 13)) * 1274126177
    h = h xor (h ushr 16)
    return (h and 0x7FFFFFF) / 0x7FFFFFF.toFloat()
}

/**
 * Value noise with **linear** (not smoothstep) interpolation — the kink at each lattice point is
 * the point: a smoothed curve reads as a decorative wave, while these angular breaks read as fibre
 * giving way.
 */
private fun valueNoise(
    x: Float,
    seed: Int,
): Float {
    val i = floor(x).toInt()
    val f = x - i
    val a = hash1(i, seed)
    val b = hash1(i + 1, seed)
    return a + (b - a) * f
}

/**
 * Fractal sum of [valueNoise]. Stacking octaves is what separates a real tear from a zigzag: big
 * slow undulations carry the overall break, mid frequencies add the bites, and the top octave
 * supplies the tiny ragged fibre detail — all at once, which a single-frequency jitter can never do.
 */
internal fun fbm(
    x: Float,
    seed: Int,
    octaves: Int = 5,
): Float {
    var sum = 0f
    var amp = 1f
    var freq = 1f
    var norm = 0f
    repeat(octaves) { o ->
        sum += amp * valueNoise(x * freq, seed + o * 101)
        norm += amp
        amp *= 0.55f
        freq *= 2.3f
    }
    return sum / norm
}

/**
 * Torn-paper silhouette. Top and bottom are always ripped; [tearSides] decides whether the left and
 * right are too — off for a strip meant to run clean off both screen edges, on for a loose scrap
 * that was torn out on every side.
 *
 * [inset] pulls the torn edges inward, used to draw the paper body slightly inside the pulp lip.
 * [amplitudeScale] lets the body's tear differ subtly from the lip's so the two ragged lines don't
 * run in lockstep like an offset copy.
 */
internal fun buildTornRectPath(
    size: Size,
    seed: Int,
    amplitude: Float,
    inset: Float = 0f,
    amplitudeScale: Float = 1f,
    tearSides: Boolean = false,
): Path {
    val w = size.width
    val h = size.height
    val amp = amplitude * amplitudeScale
    val sideAmp = if (tearSides) amp * 0.75f else 0f
    val sideSteps = 26
    val points = mutableListOf<Offset>()

    // Top, left → right.
    for (i in 0..TEAR_STEPS) {
        val t = i / TEAR_STEPS.toFloat()
        points += Offset(sideAmp + t * (w - 2f * sideAmp), amp * fbm(t * 6f, seed) + inset)
    }
    // Right, top → bottom.
    if (tearSides) {
        for (i in 1..sideSteps) {
            val t = i / sideSteps.toFloat()
            points += Offset(w - sideAmp * fbm(t * 5f, seed + 313) - inset, amp + t * (h - 2f * amp))
        }
    } else {
        points += Offset(w, h - amp * fbm(6f, seed + 9173) - inset)
    }
    // Bottom, right → left.
    for (i in 0..TEAR_STEPS) {
        val t = 1f - i / TEAR_STEPS.toFloat()
        points += Offset(sideAmp + t * (w - 2f * sideAmp), h - amp * fbm(t * 6f, seed + 9173) - inset)
    }
    // Left, bottom → top.
    if (tearSides) {
        for (i in 1 until sideSteps) {
            val t = 1f - i / sideSteps.toFloat()
            points += Offset(sideAmp * fbm(t * 5f, seed + 727) + inset, amp + t * (h - 2f * amp))
        }
    }

    val path = Path()
    path.moveTo(points.first().x, points.first().y)
    points.drop(1).forEach { path.lineTo(it.x, it.y) }
    path.close()
    return path
}

/**
 * Left-to-right wipe with a ragged vertical leading edge — the rip travelling across the screen.
 * The jag flattens out as [progress] reaches 1 so the finished strip isn't left with a chewed
 * right-hand side.
 */
private fun buildTearRevealPath(
    size: Size,
    progress: Float,
    seed: Int,
): Path {
    val path = Path()
    val jagAmp = size.height * 0.16f * (1f - progress)
    val edgeX = size.width * progress + jagAmp
    val steps = 36

    path.moveTo(0f, 0f)
    for (i in 0..steps) {
        val t = i / steps.toFloat()
        val y = t * size.height
        val jag = (fbm(t * 5f, seed + 4421) - 0.5f) * 2f * jagAmp
        path.lineTo(edgeX + jag, y)
    }
    path.lineTo(0f, size.height)
    path.close()
    return path
}

/**
 * Stepped 0→1 progress for a tear reveal. Discrete jumps rather than a smooth tween, matching the
 * stop-motion identity the rest of the Collage template uses ([AssemblingPiece], `RansomLetter`).
 */
@Composable
fun rememberTearReveal(
    canAnimate: Boolean,
    delayMs: Long,
): Float {
    var step by remember { mutableIntStateOf(if (canAnimate) 0 else REVEAL_STEPS) }

    LaunchedEffect(canAnimate) {
        if (!canAnimate) {
            step = REVEAL_STEPS
            return@LaunchedEffect
        }
        step = 0
        delay(delayMs)
        repeat(REVEAL_STEPS) {
            step++
            delay(REVEAL_STEP_MS)
        }
    }

    return step / REVEAL_STEPS.toFloat()
}

/** Stacked offset copies instead of a `BlurMaskFilter` — cheap, no hardware-layer caveats, and a torn scrap casts a fairly hard shadow anyway. */
internal fun DrawScope.drawStackedShadow(
    path: Path,
    steps: Int = 6,
    maxOffset: Offset = Offset(10f, 16f),
) {
    for (i in steps downTo 1) {
        val f = i / steps.toFloat()
        translate(maxOffset.x * f, maxOffset.y * f) {
            drawPath(path, Color.Black.copy(alpha = 0.10f))
        }
    }
}

/** Grain, fibre and a crease or two — the surface detail that sells "paper" once the silhouette is right. */
private fun DrawScope.drawPaperSurface(
    bodyPath: Path,
    seed: Int,
) {
    clipPath(bodyPath) {
        // Uneven print/aging tint so the sheet isn't a dead flat fill.
        drawRect(
            brush =
                Brush.linearGradient(
                    0f to PAPER_INK.copy(alpha = 0.05f),
                    0.45f to Color.Transparent,
                    1f to PAPER_INK.copy(alpha = 0.07f),
                ),
        )

        repeat(240) { i ->
            val r = Random(seed * 31 + i)
            drawCircle(
                color = PAPER_INK.copy(alpha = 0.015f + r.nextFloat() * 0.035f),
                radius = 0.5f + r.nextFloat() * 1.7f,
                center = Offset(r.nextFloat() * size.width, r.nextFloat() * size.height),
            )
        }

        repeat(34) { i ->
            val r = Random(seed * 77 + i)
            val start = Offset(r.nextFloat() * size.width, r.nextFloat() * size.height)
            val angle = r.nextFloat() * 6.2831f
            val len = 10f + r.nextFloat() * 46f
            drawLine(
                color = PAPER_INK.copy(alpha = 0.035f),
                start = start,
                end = start + Offset(cos(angle) * len, sin(angle) * len),
                strokeWidth = 0.7f,
            )
        }

        repeat(2) { i ->
            val r = Random(seed * 911 + i)
            val y = size.height * (0.25f + r.nextFloat() * 0.5f)
            drawLine(
                color = PAPER_INK.copy(alpha = 0.05f),
                start = Offset(0f, y),
                end = Offset(size.width, y + (r.nextFloat() - 0.5f) * 22f),
                strokeWidth = 1.1f,
            )
        }

        // Contact shading just inside each tear, so the edge sits on the page instead of floating.
        drawRect(
            brush =
                Brush.verticalGradient(
                    0f to PAPER_INK.copy(alpha = 0.16f),
                    0.16f to Color.Transparent,
                    0.84f to Color.Transparent,
                    1f to PAPER_INK.copy(alpha = 0.13f),
                ),
        )
    }
}

/**
 * An edge-to-edge scrap of torn paper carrying [content].
 *
 * Built in layers, which is what the earlier flat-fill-plus-wavy-outline version was missing:
 * a stacked drop shadow, the brighter pulp lip exposed by the rip, the paper body inset within it,
 * then grain/fibre/crease texture and contact shading clipped to the body. Colours are forced
 * ([PAPER_WHITE] on [PAPER_INK]) rather than theme-derived — adapting to a dark page is exactly
 * what stopped the effect from reading as paper.
 *
 * [revealProgress] wipes the whole thing in left-to-right behind a ragged edge (see
 * [rememberTearReveal]) — the rip propagating across the screen.
 */
@Composable
fun TornPaperStrip(
    seed: Int,
    modifier: Modifier = Modifier,
    paperColor: Color = PAPER_WHITE,
    revealProgress: Float = 1f,
    contentPadding: PaddingValues = PaddingValues(horizontal = 28.dp, vertical = 34.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    if (revealProgress <= 0f) return

    Box(
        modifier
            .drawWithContent {
                clipPath(buildTearRevealPath(size, revealProgress, seed)) {
                    this@drawWithContent.drawContent()
                }
            }.drawBehind {
                val amplitude = min(size.height * 0.12f, 26.dp.toPx())
                val lipPath = buildTornRectPath(size, seed, amplitude)
                val bodyPath =
                    buildTornRectPath(
                        size = size,
                        seed = seed,
                        amplitude = amplitude,
                        inset = 5.dp.toPx(),
                        amplitudeScale = 0.88f,
                    )

                drawStackedShadow(lipPath)
                drawPath(lipPath, PAPER_PULP)
                drawPath(bodyPath, paperColor)
                drawPaperSurface(bodyPath, seed)
            }.padding(contentPadding),
        content = content,
    )
}

/**
 * A photo mounted on a torn scrap — the paper shows as a ragged border around the image, the way a
 * picture cut out of a magazine keeps a margin of the page it came from. Uses [AsyncImage] rather
 * than the MLKit cutout pipeline: the paper edge already separates the photo from the background,
 * so subject segmentation buys nothing here.
 */
@Composable
fun TornPhotoScrap(
    imageUrl: String,
    seed: Int,
    modifier: Modifier = Modifier,
    paperColor: Color = PAPER_WHITE,
    borderPadding: Dp = 9.dp,
) {
    Box(
        modifier.drawBehind {
            val amplitude = min(min(size.height, size.width) * 0.10f, 11.dp.toPx())
            val lipPath = buildTornRectPath(size, seed, amplitude, tearSides = true)
            val bodyPath =
                buildTornRectPath(
                    size = size,
                    seed = seed,
                    amplitude = amplitude,
                    inset = 3.dp.toPx(),
                    amplitudeScale = 0.85f,
                    tearSides = true,
                )

            drawStackedShadow(lipPath, steps = 5, maxOffset = Offset(8f, 12f))
            drawPath(lipPath, PAPER_PULP)
            drawPath(bodyPath, paperColor)
            drawPaperSurface(bodyPath, seed)
        },
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(borderPadding),
        )
    }
}

/**
 * A loose scrap — torn on all four sides, sized to its content rather than the screen. Same layered
 * build as [TornPaperStrip] (shadow → pulp lip → body → surface), for the small notes that sit on
 * top of a strip.
 */
@Composable
fun TornPaperScrap(
    seed: Int,
    modifier: Modifier = Modifier,
    paperColor: Color = PAPER_WHITE,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier
            .drawBehind {
                val amplitude = min(min(size.height, size.width) * 0.14f, 12.dp.toPx())
                val lipPath = buildTornRectPath(size, seed, amplitude, tearSides = true)
                val bodyPath =
                    buildTornRectPath(
                        size = size,
                        seed = seed,
                        amplitude = amplitude,
                        inset = 3.dp.toPx(),
                        amplitudeScale = 0.85f,
                        tearSides = true,
                    )

                drawStackedShadow(lipPath, steps = 5, maxOffset = Offset(7f, 10f))
                drawPath(lipPath, PAPER_PULP)
                drawPath(bodyPath, paperColor)
                drawPaperSurface(bodyPath, seed)
            }.padding(contentPadding),
        content = content,
    )
}
