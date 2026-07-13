package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import kotlin.math.min

@Composable
fun MilestoneEmotionPieChart(
    breakdown: List<Pair<EmotionalTone, Int>>,
    modifier: Modifier = Modifier,
    chartSize: Dp = 88.dp,
    showLegend: Boolean = false,
    animate: Boolean = true,
) {
    if (breakdown.isEmpty()) return

    val total = breakdown.sumOf { it.second }.coerceAtLeast(1)
    val sweepProgress by animateFloatAsState(
        targetValue = if (animate) 1f else 1f,
        animationSpec = tween(700),
        label = "emotionPieSweep",
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Canvas(modifier = Modifier.size(chartSize)) {
            var startAngle = -90f
            breakdown.forEach { (tone, count) ->
                val sweep = 360f * (count.toFloat() / total) * sweepProgress
                if (sweep > 0f) {
                    drawArc(
                        color = tone.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = true,
                    )
                    startAngle += sweep
                }
            }
            drawCircle(
                color = Color.Black.copy(alpha = 0.18f),
                radius = size.minDimension / 2f,
                style =
                    androidx.compose.ui.graphics.drawscope
                        .Stroke(width = 1.5f),
            )
        }

        if (showLegend) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                breakdown.forEach { (tone, count) ->
                    val percent = (count * 100) / total
                    MilestoneEmotionLegendRow(
                        tone = tone,
                        percent = percent,
                        count = count,
                    )
                }
            }
        }
    }
}

@Composable
private fun MilestoneEmotionLegendRow(
    tone: EmotionalTone,
    percent: Int,
    count: Int,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(color = tone.color)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = tone.getTitle(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "$percent% · $count",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
    }
}

@Composable
fun MilestoneEmotionMiniLegend(
    breakdown: List<Pair<EmotionalTone, Int>>,
    maxItems: Int = 3,
    modifier: Modifier = Modifier,
) {
    if (breakdown.isEmpty()) return
    val total = breakdown.sumOf { it.second }.coerceAtLeast(1)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        breakdown.take(maxItems).forEach { (tone, count) ->
            val percent = min(100, (count * 100) / total)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Canvas(modifier = Modifier.size(8.dp)) {
                    drawCircle(color = tone.color)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${tone.getTitle()} $percent%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
