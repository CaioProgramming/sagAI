package com.ilustris.sagai.features.audiobook.ui

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
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
)

/** How finely the loudness is sampled along the bar; the line interpolates smoothly between these. */
private val ENVELOPE_STEP = 6.dp
private val WAVE_LENGTH = 20.dp
private val STROKE_WIDTH = 2.5.dp
private val PATH_STEP = 1.5.dp
private const val MIN_LEVEL = 0.06f
private const val LEVEL_CURVE = 0.6f
private val BAR_HEIGHT = 44.dp
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

    val path =
        remember(levels, widthPx, heightPx, density) {
            if (widthPx <= 0) return@remember Path()
            val envelopeStep = with(density) { ENVELOPE_STEP.toPx() }
            val waveLength = with(density) { WAVE_LENGTH.toPx() }
            val pathStep = with(density) { PATH_STEP.toPx() }
            val samples = envelopeLevels(levels, (widthPx / envelopeStep).toInt().coerceAtLeast(2))
            val centerY = heightPx / 2f
            val maxSwing = centerY - strokePx
            val width = widthPx.toFloat()
            Path().apply {
                moveTo(0f, centerY)
                var x = pathStep
                while (x < width) {
                    val position = x / width * (samples.size - 1)
                    val index = floor(position).toInt().coerceIn(0, samples.size - 2)
                    val t = position - index
                    val eased = t * t * (3f - 2f * t)
                    val level = samples[index] + (samples[index + 1] - samples[index]) * eased
                    lineTo(x, centerY + sin(2.0 * PI * x / waveLength).toFloat() * level * maxSwing)
                    x += pathStep
                }
                lineTo(width, centerY)
            }
        }

    val playedColor = MaterialTheme.colorScheme.onPrimary
    val remainingColor = MaterialTheme.colorScheme.onBackground.copy(alpha = .25f)
    val stroke = remember(strokePx) { Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round) }
    val currentOnChange by rememberUpdatedState(onFractionChange)
    val currentOnFinished by rememberUpdatedState(onFractionChangeFinished)

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
                        onDragStart = { offset -> currentOnChange((offset.x / size.width).coerceIn(0f, 1f)) },
                        onDragEnd = { currentOnFinished() },
                        onDragCancel = { currentOnFinished() },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            currentOnChange((change.position.x / size.width).coerceIn(0f, 1f))
                        },
                    )
                }.drawBehind {
                    drawPath(path, remainingColor, style = stroke)
                    clipRect(left = 0f, top = 0f, right = fraction.coerceIn(0f, 1f) * size.width, bottom = size.height) {
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
                    .offset { IntOffset((fraction.coerceIn(0f, 1f) * widthPx - thumbPx / 2f).roundToInt(), 0) }
                    .size(THUMB_SIZE),
        )
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
