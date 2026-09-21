package com.ilustris.sagai.features.audiobook.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.ui.theme.themePainter
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

/** Loudness envelope of the section currently playing, one level (0..1) per WaveformExtractor.STEP_MS. */
@Immutable
class Waveform(
    val sectionKey: String,
    val levels: FloatArray,
) {
    private val peak = levels.maxOrNull() ?: 0f

    /** How loud the narration is at [fraction] of the section, 0..1 relative to its loudest moment. */
    fun levelAt(fraction: Float): Float {
        if (peak <= 0f) return 0f
        val index = (fraction.coerceIn(0f, 1f) * (levels.size - 1)).roundToInt()
        return (levels[index] / peak).coerceIn(0f, 1f).pow(LEVEL_CURVE)
    }
}

/** How finely the loudness is sampled along the bar; the line interpolates smoothly between these. */
private val ENVELOPE_STEP = 6.dp
private val WAVE_LENGTH = 20.dp
private val STROKE_WIDTH = 2.5.dp
private val PATH_STEP = 1.5.dp
private const val PROGRESS_EASE_MS = 150
private const val MIN_LEVEL = 0.06f
private const val LEVEL_CURVE = 0.6f
private val BAR_HEIGHT = 44.dp
private const val MAX_BARS = 96
private val THUMB_SIZE = 16.dp

/**
 * The seek bar drawn as one continuous line through the chapter: a sine whose swing follows how
 * loud the narration is there (flat in silences, wide on emphasis), same fill-as-it-plays progress
 * and tap/drag-to-seek as the plain slider. The curve is built once per (levels, width); each
 * playback tick only redraws it clipped to the current progress, so the 150ms position updates
 * stay in the draw phase and never rebuild the path.
 */
@Composable
fun WaveformSeekBar(
    levels: FloatArray?,
    fraction: Float,
    onFractionChange: (Float) -> Unit,
    onFractionChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    var widthPx by remember { mutableIntStateOf(0) }
    val heightPx = remember(density) { with(density) { BAR_HEIGHT.toPx() } }
    val thumbPx = remember(density) { with(density) { THUMB_SIZE.toPx() } }
    val strokePx = remember(density) { with(density) { STROKE_WIDTH.toPx() } }

    val curve =
        remember(levels, widthPx, heightPx, density) {
            if (widthPx <= 0) return@remember null
            val envelopeStep = with(density) { ENVELOPE_STEP.toPx() }
            WaveCurve(
                samples = envelopeLevels(levels, (widthPx / envelopeStep).toInt().coerceAtLeast(2)),
                width = widthPx.toFloat(),
                centerY = heightPx / 2f,
                maxSwing = heightPx / 2f - strokePx,
                waveLength = with(density) { WAVE_LENGTH.toPx() },
            )
        }
    val path =
        remember(curve, density) {
            val wave = curve ?: return@remember Path()
            val pathStep = with(density) { PATH_STEP.toPx() }
            Path().apply {
                moveTo(0f, wave.centerY)
                var x = pathStep
                while (x < wave.width) {
                    lineTo(x, wave.yAt(x))
                    x += pathStep
                }
                lineTo(wave.width, wave.centerY)
            }
        }

    val playedColor = MaterialTheme.colorScheme.onPrimary
    val remainingColor = MaterialTheme.colorScheme.onBackground.copy(alpha = .25f)
    val stroke = remember(strokePx) { Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round) }
    val currentOnChange by rememberUpdatedState(onFractionChange)
    val currentOnFinished by rememberUpdatedState(onFractionChangeFinished)

    // Position arrives in ~150ms steps while the curve swings fast under the thumb, so following it
    // raw would hop. Easing between ticks (and snapping while a finger drives it) is read only in
    // the draw/layout lambdas below, so it animates without recomposing anything.
    var dragging by remember { mutableStateOf(false) }
    val progress =
        animateFloatAsState(
            targetValue = fraction.coerceIn(0f, 1f),
            animationSpec = if (dragging) snap() else tween(PROGRESS_EASE_MS, easing = LinearEasing),
            label = "waveformProgress",
        )

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(BAR_HEIGHT)
                .onSizeChanged { widthPx = it.width }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        currentOnChange((offset.x / size.width).coerceIn(0f, 1f))
                        currentOnFinished()
                    }
                }.pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            dragging = true
                            currentOnChange((offset.x / size.width).coerceIn(0f, 1f))
                        },
                        onDragEnd = {
                            dragging = false
                            currentOnFinished()
                        },
                        onDragCancel = {
                            dragging = false
                            currentOnFinished()
                        },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            currentOnChange((change.position.x / size.width).coerceIn(0f, 1f))
                        },
                    )
                }.drawBehind {
                    drawPath(path, remainingColor, style = stroke)
                    clipRect(left = 0f, top = 0f, right = progress.value * size.width, bottom = size.height) {
                        drawPath(path, playedColor, style = stroke)
                    }
                },
    ) {
        Icon(
            painter = themePainter(),
            contentDescription = null,
            tint = playedColor,
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    // Rides the line itself: same curve the path is drawn from, evaluated at the progress x.
                    .offset {
                        val x = progress.value * widthPx
                        val lift = curve?.let { it.yAt(x) - it.centerY } ?: 0f
                        IntOffset((x - thumbPx / 2f).roundToInt(), lift.roundToInt())
                    }
                    .size(THUMB_SIZE),
        )
    }
}

private val BAR_WIDTH = 2.5.dp
private val BAR_GAP = 2.dp
private const val BAR_MIN_HEIGHT = 0.12f

/**
 * The same loudness envelope drawn as voice-note bars: one bar per slice of the section, filling in
 * as it plays. Used by the mini player, where the chapter has to read as a recording at a glance
 * and there is no room for the full [WaveformSeekBar] line.
 *
 * Like the seek bar, the position only ever reaches the draw phase — the bar heights are computed
 * once per (levels, width) and each playback tick just re-colours them.
 */
@Composable
fun WaveformBars(
    levels: FloatArray?,
    fraction: Float,
    playedColor: Color,
    remainingColor: Color,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    var widthPx by remember { mutableIntStateOf(0) }
    val barPx = remember(density) { with(density) { BAR_WIDTH.toPx() } }
    val gapPx = remember(density) { with(density) { BAR_GAP.toPx() } }

    val bars =
        remember(levels, widthPx, barPx, gapPx) {
            if (widthPx <= 0) return@remember FloatArray(0)
            envelopeLevels(levels, ((widthPx + gapPx) / (barPx + gapPx)).toInt().coerceIn(2, MAX_BARS))
        }
    val progress =
        animateFloatAsState(
            targetValue = fraction.coerceIn(0f, 1f),
            animationSpec = tween(PROGRESS_EASE_MS, easing = LinearEasing),
            label = "waveformBarsProgress",
        )

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .onSizeChanged { widthPx = it.width }
                .drawBehind {
                    if (bars.isEmpty()) return@drawBehind
                    val step = (size.width - barPx) / (bars.size - 1).coerceAtLeast(1)
                    bars.forEachIndexed { index, level ->
                        val x = barPx / 2f + index * step
                        val height = size.height * level.coerceAtLeast(BAR_MIN_HEIGHT)
                        val played = index.toFloat() / (bars.size - 1).coerceAtLeast(1) <= progress.value
                        drawLine(
                            color = if (played) playedColor else remainingColor,
                            start = Offset(x, (size.height - height) / 2f),
                            end = Offset(x, (size.height + height) / 2f),
                            strokeWidth = barPx,
                            cap = StrokeCap.Round,
                        )
                    }
                },
    )
}

/** The line's shape: a sine swinging by the loudness at each x, smoothly interpolated between samples. */
private class WaveCurve(
    private val samples: FloatArray,
    val width: Float,
    val centerY: Float,
    private val maxSwing: Float,
    private val waveLength: Float,
) {
    fun yAt(x: Float): Float {
        val position = (x / width).coerceIn(0f, 1f) * (samples.size - 1)
        val index = floor(position).toInt().coerceIn(0, samples.size - 2)
        val t = position - index
        val eased = t * t * (3f - 2f * t)
        val level = samples[index] + (samples[index + 1] - samples[index]) * eased
        return centerY + sin(2.0 * PI * x / waveLength).toFloat() * level * maxSwing
    }
}

/** Peak level of each sample's slice of the envelope, normalised to the loudest one; flat until the envelope is ready. */
private fun envelopeLevels(
    levels: FloatArray?,
    count: Int,
): FloatArray {
    if (levels == null || levels.isEmpty()) return FloatArray(count) { MIN_LEVEL }
    val peaks =
        FloatArray(count) { sample ->
            val from = sample * levels.size / count
            val to = maxOf(from + 1, (sample + 1) * levels.size / count).coerceAtMost(levels.size)
            var peak = 0f
            for (i in from until to) peak = maxOf(peak, levels[i])
            peak
        }
    val loudest = peaks.max().takeIf { it > 0f } ?: return FloatArray(count) { MIN_LEVEL }
    return FloatArray(count) { (peaks[it] / loudest).pow(LEVEL_CURVE).coerceAtLeast(MIN_LEVEL) }
}
