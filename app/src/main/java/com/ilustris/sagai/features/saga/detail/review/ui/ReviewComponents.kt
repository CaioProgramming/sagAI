package com.ilustris.sagai.features.saga.detail.review.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.ui.ChapterCardView
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.home.data.model.rankByHour
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.playthrough.CounterText
import com.ilustris.sagai.features.saga.chat.domain.model.rankEmotionalTone
import com.ilustris.sagai.features.saga.chat.domain.model.rankTopCharacters
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.reactiveShimmer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

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
    enabled: Boolean = true,
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
            paths.map { Animatable(if (enabled) 0f else 1f) }
        }

    LaunchedEffect(paths, enabled) {
        if (enabled) {
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
    canAnimate: Boolean = true,
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
                modifier = Modifier.reactiveShimmer(canAnimate),
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
    val emotionalRank =
        remember {
            content
                .flatMessages()
                .filter { it.character == content.mainCharacter }
                .rankEmotionalTone()
        }
    val topCharacters =
        remember {
            content
                .flatMessages()
                .rankTopCharacters(content.getCharacters(true))
                .take(5)
        }
    val playTime =
        content.data.playTimeMs.let {
            val minutes = it / 60000
            val hours = minutes / 60
            if (hours > 0) "${hours}h ${minutes % 60}m" else "${minutes}m"
        }

    val mostActiveHour = content.rankByHour().maxByOrNull { it.value.size }?.key ?: 0
    when (mostActiveHour) {
        in 0..4 -> "The Midnight Chronicler"
        in 5..8 -> "The Dawn Speaker"
        in 9..11 -> "The Morning Muse"
        in 12..14 -> "The Midday Architect"
        in 15..17 -> "The Dusk Weaver"
        in 18..21 -> "The Evening Star"
        else -> "The Night Owl"
    }

    val shape = genre.bubble(isNarrator = true)
    val contentColor = MaterialTheme.colorScheme.background

    Column(
        modifier =
            modifier
                .background(
                    MaterialTheme.colorScheme.onBackground,
                    shape,
                )
                .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (content.data.icon.isNotBlank()) {
            AsyncImage(
                model = content.data.icon,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(shape)
                        .border(2.dp, MaterialTheme.colorScheme.background, shape)
                        .effectForGenre(genre),
            )
        }

        StrokedText(
            text = content.data.title.uppercase(),
            style =
                MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = genre.headerFont(),
                    color = genre.resolveColor(),
                    textAlign = TextAlign.Center,
                ),
            strokeColor = MaterialTheme.colorScheme.background,
            strokeWidth = 10f,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            Modifier
                .fillMaxWidth(),
        ) {
            Column(
                Modifier
                    .fillMaxWidth(if (emotionalRank.isNotEmpty()) .5f else 1f)
                    .padding(4.dp),
            ) {
                Text(
                    "TOP PERSONAGENS",
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            fontFamily = genre.bodyFont(),
                            fontWeight = FontWeight.Bold,
                            color = contentColor.copy(alpha = 0.7f),
                            textAlign = TextAlign.Start,
                        ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )

                topCharacters.forEach {
                    val position = topCharacters.indexOf(it) + 1
                    Text(
                        "$position ${it.first.name}",
                        style =
                            MaterialTheme.typography.labelLarge.copy(
                                fontFamily = genre.bodyFont(),
                                fontWeight = FontWeight.Black,
                                color = contentColor,
                            ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
            }

            if (emotionalRank.isNotEmpty()) {
                Column(
                    Modifier
                        .weight(1f)
                        .padding(4.dp),
                ) {
                    Text(
                        "LADO EMOCIONAL",
                        style =
                            MaterialTheme.typography.labelSmall.copy(
                                fontFamily = genre.bodyFont(),
                                fontWeight = FontWeight.Bold,
                                color = contentColor.copy(alpha = 0.7f),
                            ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    emotionalRank.take(2).forEachIndexed { i, tone ->
                        val position = i + 1
                        Text(
                            "$position ${tone.first.getTitle()}",
                            style =
                                MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = genre.bodyFont(),
                                    fontWeight = FontWeight.Black,
                                    color = contentColor,
                                ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                    }
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth(),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth(if (emotionalRank.isNotEmpty()) 0.5f else 1f)
                        .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    "Tempo de jogo",
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = genre.bodyFont(),
                            fontWeight = FontWeight.Medium,
                            color = contentColor,
                        ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    playTime,
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontFamily = genre.headerFont(),
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                        ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (emotionalRank.isNotEmpty()) {
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        "Emoção definitiva",
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = genre.bodyFont(),
                                fontWeight = FontWeight.Medium,
                                color = contentColor,
                            ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(8.dp),
                    )

                    Text(
                        emotionalRank.first().first.getTitle(),
                        style =
                            MaterialTheme.typography.titleLarge.copy(
                                fontFamily = genre.headerFont(),
                                fontWeight = FontWeight.Bold,
                                color = contentColor,
                            ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }
        }

        Image(
            painterResource(R.drawable.ic_spark),
            null,
            colorFilter =
                androidx.compose.ui.graphics.ColorFilter
                    .tint(genre.resolveColor()),
            modifier =
                Modifier
                    .size(32.dp)
                    .reactiveShimmer(true),
        )
    }
}

@Composable
fun DynamicCard(
    title: String,
    subtitle: String,
    titleStyle: TextStyle,
    subtitleStyle: TextStyle,
    lineColor: Color,
    modifier: Modifier,
) {
    val lineCount = Random.nextInt(1, 5)
    Box(modifier, contentAlignment = Alignment.Center) {
        DynamicLinework(lineColor, lineCount, modifier = Modifier.fillMaxSize(), strokeWidth = 2.dp)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .padding(8.dp),
        ) {
            AnimatedContent(title, transitionSpec = {
                fadeIn(animationSpec = tween(500)) +
                    slideInVertically { it } togetherWith
                    fadeOut(animationSpec = tween(500)) +
                    slideOutVertically { -it }
            }) {
                Text(
                    text = title,
                    style = titleStyle,
                )
            }

            AnimatedContent(subtitle, transitionSpec = {
                fadeIn(animationSpec = tween(500)) +
                    slideInVertically { it } togetherWith
                    fadeOut(animationSpec = tween(500)) +
                    slideOutVertically { -it }
            }) {
                Text(
                    text = subtitle,
                    style = subtitleStyle,
                )
            }
        }
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
fun JourneyCollage(
    saga: SagaContent,
    chapters: List<Chapter>,
    modifier: Modifier = Modifier,
) {
    if (chapters.isEmpty()) return

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(1.2f),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.weight(1.5f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            chapters.getOrNull(0)?.let {
                PopIn(
                    index = 0,
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                ) {
                    ChapterCardView(
                        genre = saga.data.genre,
                        chapter = it,
                        showTitle = false,
                        modifier = Modifier.fillMaxSize(),
                        chapterIndex = chapters.indexOf(it),
                    )
                }
            }
            chapters.getOrNull(1)?.let {
                PopIn(
                    index = 1,
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                ) {
                    ChapterCardView(
                        genre = saga.data.genre,
                        chapter = it,
                        showTitle = false,
                        modifier = Modifier.fillMaxSize(),
                        chapterIndex = chapters.indexOf(it),
                    )
                }
            }
        }

        // Bottom row: 3 smaller ones or variation
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            chapters.getOrNull(2)?.let {
                PopIn(
                    index = 2,
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                ) {
                    ChapterCardView(
                        genre = saga.data.genre,
                        chapter = it,
                        showTitle = false,
                        modifier = Modifier.fillMaxSize(),
                        chapterIndex = chapters.indexOf(it),
                    )
                }
            }
            chapters.getOrNull(3)?.let {
                PopIn(
                    index = 3,
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                ) {
                    ChapterCardView(
                        saga.data.genre,
                        chapter = it,
                        showTitle = false,
                        modifier = Modifier.fillMaxSize(),
                        chapterIndex = chapters.indexOf(it),
                    )
                }
            }
            chapters.getOrNull(4)?.let {
                PopIn(
                    index = 4,
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                ) {
                    ChapterCardView(
                        saga.data.genre,
                        chapter = it,
                        showTitle = false,
                        modifier = Modifier.fillMaxSize(),
                        chapterIndex = chapters.indexOf(it),
                    )
                }
            }
        }
    }
}

@Composable
fun PopIn(
    index: Int,
    modifier: Modifier = Modifier,
    delayStep: Long = 150L,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * delayStep)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter =
            fadeIn(tween(500)) +
                scaleIn(
                    tween(500, easing = androidx.compose.animation.core.EaseOutBack),
                    initialScale = 0.5f,
                ),
        modifier = modifier,
    ) {
        content()
    }
}

@Composable
private fun CollageImage(
    url: String,
    genre: Genre,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(genre.cornerSize()))
                .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
fun SagaLegendLayout(
    mainCharacter: CharacterContent,
    supportingCharacters: List<Character>,
    sagaIcon: String,
    modifier: Modifier = Modifier,
) {
    // 3x3 Grid Items
    val items =
        remember(supportingCharacters) {
            val list = supportingCharacters.toMutableList()
            List(9) { index ->
                if (index == 4) {
                    mainCharacter.data.image
                } else {
                    list.removeFirstOrNull()?.image ?: sagaIcon
                }
            }
        }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(0.75f),
    ) {
        Row(modifier = Modifier.weight(1f)) {
            PopIn(0, Modifier.weight(1.1f)) { GtaCell(items[0], Modifier.fillMaxSize()) }
            PopIn(1, Modifier.weight(1f)) { GtaCell(items[1], Modifier.fillMaxSize()) }
            PopIn(2, Modifier.weight(1.2f)) { GtaCell(items[2], Modifier.fillMaxSize()) }
        }
        Row(modifier = Modifier.weight(1.3f)) {
            PopIn(3, Modifier.weight(1f)) { GtaCell(items[3], Modifier.fillMaxSize()) }
            PopIn(4, Modifier.weight(1.4f)) {
                GtaCell(
                    items[4],
                    Modifier.fillMaxSize(),
                )
            } // Protagonist Cell
            PopIn(5, Modifier.weight(1.1f)) { GtaCell(items[5], Modifier.fillMaxSize()) }
        }
        Row(modifier = Modifier.weight(1.1f)) {
            PopIn(6, Modifier.weight(1.2f)) { GtaCell(items[6], Modifier.fillMaxSize()) }
            PopIn(7, Modifier.weight(1.1f)) { GtaCell(items[7], Modifier.fillMaxSize()) }
            PopIn(8, Modifier.weight(1f)) { GtaCell(items[8], Modifier.fillMaxSize()) }
        }
    }
}

@Composable
private fun GtaCell(
    url: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .border(2.dp, Color.Black)
                .background(Color.Black),
    ) {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
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
            color = genre.resolveColor().copy(alpha = 0.2f),
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
fun ReviewTopCharacterContent(
    character: Character,
    messageCount: Int,
    genre: Genre,
    subtitle: String?,
    imageModifier: Modifier = Modifier,
    modifier: Modifier = Modifier,
    onAnimationFinished: () -> Unit = {},
) {
    var showContent by remember { mutableStateOf(false) }

    val heightFill by animateFloatAsState(
        targetValue = if (showContent) 0.5f else 1f,
    )

    val imagePadding by animateDpAsState(
        targetValue = if (showContent) 32.dp else 0.dp,
    )

    LaunchedEffect(Unit) {
        delay(3.seconds)
        showContent = true
    }

    Column(
        modifier
            .fillMaxSize()
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AsyncImage(
            model = character.image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                imageModifier
                    .fillMaxWidth(1f)
                    .fillMaxHeight(heightFill)
                    .padding(imagePadding)
                    .clip(genre.bubble(isNarrator = true))
                    .border(2.dp, genre.resolveColor(), genre.bubble(isNarrator = true))
                    .effectForGenre(genre),
        )

        subtitle?.let {
            AnimatedVisibility(
                showContent,
                enter =
                    fadeIn(tween(500, delayMillis = 700)) +
                        scaleIn(tween(1500, easing = EaseIn)),
                exit = slideOutVertically { it } + fadeOut(),
            ) {
                Text(
                    subtitle,
                    style =
                        MaterialTheme.typography.labelMedium.copy(
                            fontFamily = genre.bodyFont(),
                            textAlign = TextAlign.Center,
                        ),
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }

        AnimatedVisibility(
            showContent,
            enter =
                fadeIn(tween(500)) +
                    scaleIn(tween(1000, easing = EaseIn)),
            exit = slideOutVertically { it } + fadeOut(),
        ) {
            Text(
                character.name,
                style =
                    MaterialTheme.typography.displayMedium.copy(
                        fontFamily = genre.headerFont(),
                        shadow =
                            Shadow(
                                (
                                    character.hexColor.hexToColor()
                                        ?: genre.resolveColor()
                                ),
                                offset = Offset(2f, 2f),
                                blurRadius = 10f,
                            ),
                    ),
            )
        }

        AnimatedVisibility(
            showContent,
            enter =
                fadeIn(tween(500, delayMillis = 1000)) +
                    slideInVertically(tween(1500, easing = EaseIn)) { -it },
            exit = slideOutVertically { it } + fadeOut(),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CounterText(
                    messageCount,
                    onAnimationFinished = {
                        onAnimationFinished()
                    },
                    textStyle =
                        MaterialTheme.typography.titleMedium.copy(
                            fontFamily = genre.bodyFont(),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        ),
                )

                Text(
                    stringResource(R.string.messages_label),
                    style =
                        MaterialTheme.typography.labelMedium.copy(
                            fontFamily = genre.bodyFont(),
                            fontWeight = FontWeight.Medium,
                        ),
                )
            }
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
            color = genre.resolveColor().copy(alpha = 0.3f),
            lineCount = 10,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                text = "Checking out the beautiful mess we made...",
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
                        color = genre.resolveColor(),
                    ),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            StoryProgressIndicator(
                progress = 0.5f,
                modifier = Modifier.fillMaxWidth(0.6f),
                color = genre.resolveColor(),
            )
        }
    }
}
