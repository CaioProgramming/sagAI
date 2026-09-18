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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.ui.theme.themePainter
import kotlin.math.pow
import kotlin.math.roundToInt

/** Loudness envelope of the section currently playing, one level (0..1) per WaveformExtractor.STEP_MS. */
@Immutable
class Waveform(
    val sectionKey: String,
    val levels: FloatArray,
)

private val BAR_STEP = 4.dp
private const val BAR_FILL_RATIO = 0.6f
private const val MIN_LEVEL = 0.08f
private const val LEVEL_CURVE = 0.6f
private val BAR_HEIGHT = 44.dp
private val THUMB_SIZE = 16.dp

/**
 * The seek bar drawn as the chapter's own waveform: same fill-as-it-plays progress as before, but
 * the shape shows where the loud and quiet parts are. The bar geometry is built once per
 * (levels, width); each playback tick only redraws it clipped to the current progress, so the
 * 150ms position updates stay in the draw phase and never rebuild the path.
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
    val barCount = remember(widthPx, density) { (widthPx / with(density) { BAR_STEP.toPx() }).toInt().coerceAtLeast(1) }

    val path =
        remember(levels, widthPx, barCount, heightPx) {
            val bars = barLevels(levels, barCount)
            val step = widthPx.toFloat() / barCount
            val barWidth = step * BAR_FILL_RATIO
            Path().apply {
                bars.forEachIndexed { index, level ->
                    val barHeight = maxOf(level * heightPx, barWidth)
                    val left = index * step + (step - barWidth) / 2f
                    val top = (heightPx - barHeight) / 2f
                    addRoundRect(RoundRect(left, top, left + barWidth, top + barHeight, CornerRadius(barWidth / 2f)))
                }
            }
        }

    val playedColor = MaterialTheme.colorScheme.onPrimary
    val remainingColor = MaterialTheme.colorScheme.onBackground.copy(alpha = .25f)
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
                    drawPath(path, remainingColor)
                    clipRect(left = 0f, top = 0f, right = fraction.coerceIn(0f, 1f) * size.width, bottom = size.height) {
                        drawPath(path, playedColor)
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

/** Peak level of each bar's slice of the envelope, normalised to the loudest one; flat until the envelope is ready. */
private fun barLevels(
    levels: FloatArray?,
    barCount: Int,
): FloatArray {
    if (levels == null || levels.isEmpty()) return FloatArray(barCount) { MIN_LEVEL }
    val peaks =
        FloatArray(barCount) { bar ->
            val from = bar * levels.size / barCount
            val to = maxOf(from + 1, (bar + 1) * levels.size / barCount).coerceAtMost(levels.size)
            var peak = 0f
            for (i in from until to) peak = maxOf(peak, levels[i])
            peak
        }
    val loudest = peaks.max().takeIf { it > 0f } ?: return FloatArray(barCount) { MIN_LEVEL }
    return FloatArray(barCount) { (peaks[it] / loudest).pow(LEVEL_CURVE).coerceAtLeast(MIN_LEVEL) }
}
