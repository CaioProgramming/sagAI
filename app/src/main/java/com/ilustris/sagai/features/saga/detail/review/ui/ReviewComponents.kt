package com.ilustris.sagai.features.saga.detail.review.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.rankByHour
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.reactiveShimmer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun StoryProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 100, easing = LinearEasing),
        label = "progress",
    )

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier =
            modifier
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
        color = color,
        trackColor = color.copy(alpha = 0.3f),
        strokeCap = StrokeCap.Round,
    )
}

@Composable
fun DynamicLinework(
    color: Color,
    lineCount: Int,
    strokeWidth: Dp = 1.dp,
    modifier: Modifier = Modifier,
) {
    var size by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }

    // Generate minimalist, sweeping paths
    val paths =
        remember(size, lineCount) {
            if (size == androidx.compose.ui.unit.IntSize.Zero) return@remember emptyList<Path>()
            val width = size.width.toFloat()
            val height = size.height.toFloat()
            val random = Random(lineCount.toLong())

            List(lineCount) {
                val path = Path()
                // Randomly start from any of the 4 sides, well outside the view
                val startSide = random.nextInt(4)
                val endSide = (startSide + random.nextInt(1, 3)) % 4

                fun getPoint(side: Int): Offset {
                    val padding = 200f
                    return when (side) {
                        0 -> Offset(random.nextFloat() * width, -padding)

                        // Top
                        1 -> Offset(width + padding, random.nextFloat() * height)

                        // Right
                        2 -> Offset(random.nextFloat() * width, height + padding)

                        // Bottom
                        else -> Offset(-padding, random.nextFloat() * height) // Left
                    }
                }

                val start = getPoint(startSide)
                val end = getPoint(endSide)

                // Control points are deeply randomized to create wide, sweeping curves
                val cp1 = Offset(random.nextFloat() * width, random.nextFloat() * height)
                val cp2 = Offset(random.nextFloat() * width, random.nextFloat() * height)

                path.moveTo(start.x, start.y)
                path.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, end.x, end.y)
                path
            }
        }

    val animProgresses =
        remember(paths) {
            paths.map { Animatable(0f) }
        }

    LaunchedEffect(paths) {
        animProgresses.forEachIndexed { index, anim ->
            launch {
                delay(index * 300L + Random.nextLong(0, 500))
                anim.animateTo(
                    1f,
                    animationSpec =
                        tween(
                            durationMillis = 3000 + Random.nextInt(0, 2000),
                            easing = EaseOutCubic,
                        ),
                )
            }
        }
    }

    Canvas(
        modifier =
            modifier
                .fillMaxSize()
                .onSizeChanged { size = it },
    ) {
        paths.forEachIndexed { index, path ->
            val progress = animProgresses[index].value
            if (progress > 0f) {
                val pathMeasure = PathMeasure()
                pathMeasure.setPath(path, false)
                val segmentPath = Path()
                pathMeasure.getSegment(0f, pathMeasure.length * progress, segmentPath)

                drawPath(
                    path = segmentPath,
                    color = color,
                    style =
                        Stroke(
                            width = strokeWidth.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                )
            }
        }
    }
}

@Composable
fun StrokedText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    strokeColor: Color = Color.Black,
    strokeWidth: Float = 8f,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style =
                style.copy(
                    color = strokeColor,
                    drawStyle =
                        Stroke(
                            miter = 10f,
                            width = strokeWidth,
                            join = StrokeJoin.Round,
                        ),
                ),
            textAlign = textAlign,
        )
        Text(
            text = text,
            style = style,
            textAlign = textAlign,
        )
    }
}

@Composable
fun ReviewTextDisplay(
    title: String?,
    subtitle: String?,
    genre: Genre,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        title?.let {
            StrokedText(
                text = it,
                style =
                    MaterialTheme.typography.displaySmall.copy(
                        fontFamily = genre.headerFont(),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                textAlign = TextAlign.Center,
                strokeColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.reactiveShimmer(true),
            )
        }

        if (title != null && subtitle != null) {
            Spacer(modifier = Modifier.height(12.dp))
        }

        subtitle?.let {
            Text(
                text = it,
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = genre.bodyFont(),
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    ),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun HeroSummaryCard(
    content: SagaContent,
    modifier: Modifier = Modifier,
) {
    val genre = content.data.genre
    val review = content.data.review ?: return

    val playTime =
        content.data.playTimeMs.let {
            val minutes = it / 60000
            val hours = minutes / 60
            if (hours > 0) "${hours}h ${minutes % 60}m" else "${minutes}m"
        }

    val mostActiveHour = content.rankByHour().maxByOrNull { it.value.size }?.key ?: 0
    val ritualPersona =
        when (mostActiveHour) {
            in 0..4 -> "The Midnight Chronicler"
            in 5..8 -> "The Dawn Speaker"
            in 9..11 -> "The Morning Muse"
            in 12..14 -> "The Midday Architect"
            in 15..17 -> "The Dusk Weaver"
            in 18..21 -> "The Evening Star"
            else -> "The Night Owl"
        }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    RoundedCornerShape(genre.cornerSize()),
                ).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = content.data.title.uppercase(),
            style =
                MaterialTheme.typography.titleMedium.copy(
                    fontFamily = genre.headerFont(),
                    fontWeight = FontWeight.Black,
                    color = genre.color,
                ),
        )

        HorizontalDivider(color = genre.color.copy(alpha = 0.2f), thickness = 1.dp)

        SummaryMetricItem("THE RITUAL", ritualPersona, genre)
        SummaryMetricItem("THE VOICE", review.expressiveness?.content?.title ?: "Unknown", genre)
        SummaryMetricItem(
            "THE BOND",
            review.topCharacters?.content?.title ?: "Solitary Hero",
            genre,
        )
        SummaryMetricItem("THE WEIGHT", playTime, genre)
    }
}

@Composable
private fun SummaryMetricItem(
    label: String,
    value: String,
    genre: Genre,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontFamily = genre.bodyFont(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                ),
        )
        Text(
            text = value,
            style =
                MaterialTheme.typography.titleLarge.copy(
                    fontFamily = genre.headerFont(),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun ArchetypeMiniCard(
    title: String,
    subtitle: String,
    genre: Genre,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .size(width = 140.dp, height = 200.dp)
                .clip(RoundedCornerShape(genre.cornerSize()))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { onClick() }
                .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        DynamicLinework(
            color = genre.color.copy(alpha = 0.2f),
            lineCount = 3,
            modifier = Modifier.fillMaxSize(.5f),
        )

        Column {
            Text(
                text = title,
                style =
                    MaterialTheme.typography.titleSmall.copy(
                        fontFamily = genre.headerFont(),
                        fontWeight = FontWeight.Black,
                    ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontFamily = genre.bodyFont(),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun ReviewLoadingPage(
    genre: Genre,
    sagaTitle: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        DynamicLinework(
            color = genre.color.copy(alpha = 0.3f),
            lineCount = 10,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                text = "The Observer is reflecting on your tale...",
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontFamily = genre.bodyFont(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                textAlign = TextAlign.Center,
                modifier = Modifier.reactiveShimmer(true),
            )

            Text(
                text = sagaTitle,
                style =
                    MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = genre.headerFont(),
                        fontWeight = FontWeight.ExtraBold,
                        color = genre.color,
                    ),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            StoryProgressIndicator(
                progress = 0.5f,
                modifier = Modifier.fillMaxWidth(0.6f),
                color = genre.color,
            )
        }
    }
}
