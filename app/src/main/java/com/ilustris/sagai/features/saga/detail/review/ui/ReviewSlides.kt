package com.ilustris.sagai.features.saga.detail.review.ui

import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.features.saga.chat.domain.model.filterCharacterMessages
import com.ilustris.sagai.features.saga.chat.domain.model.rankEmotionalTone
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.VibeShapeDrawing
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.headerFont
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

@Composable
fun SlideContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(16.dp),
    ) {
        content()
    }
}

@Composable
fun DynamicLinework(
    modifier: Modifier = Modifier,
    color: Color = Color.White.copy(alpha = 0.2f),
    lineCount: Int = 4,
) {
    var size by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    val curves =
        remember(size) {
            if (size == androidx.compose.ui.unit.IntSize.Zero) {
                emptyList()
            } else {
                val width = size.width.toFloat()
                val height = size.height.toFloat()
                List(lineCount) {
                    val startSide = Random.nextInt(4) // 0: Top, 1: Right, 2: Bottom, 3: Left
                    val endSide = (startSide + Random.nextInt(1, 3)) % 4

                    fun getPointOnSide(side: Int): Offset =
                        when (side) {
                            0 -> Offset(Random.nextFloat() * width, 0f)
                            1 -> Offset(width, Random.nextFloat() * height)
                            2 -> Offset(Random.nextFloat() * width, height)
                            else -> Offset(0f, Random.nextFloat() * height)
                        }

                    val start = getPointOnSide(startSide)
                    val end = getPointOnSide(endSide)

                    // Control points to make them sweep through the center area
                    val cp1 = Offset(Random.nextFloat() * width, Random.nextFloat() * height)
                    val cp2 = Offset(Random.nextFloat() * width, Random.nextFloat() * height)

                    Path().apply {
                        moveTo(start.x, start.y)
                        cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, end.x, end.y)
                    }
                }
            }
        }

    val animProgress =
        remember {
            androidx.compose.animation.core
                .Animatable(0f)
        }
    LaunchedEffect(curves) {
        if (curves.isNotEmpty()) {
            animProgress.snapTo(0f)
            animProgress.animateTo(
                1f,
                animationSpec = tween(3000, easing = EaseOutCubic),
            )
        }
    }

    androidx.compose.foundation.Canvas(
        modifier =
            modifier
                .fillMaxSize()
                .onSizeChanged { size = it },
    ) {
        curves.forEach { path ->
            val pathMeasure = PathMeasure()
            pathMeasure.setPath(path, false)
            val segmentPath = Path()
            pathMeasure.getSegment(0f, pathMeasure.length * animProgress.value, segmentPath)

            drawPath(
                path = segmentPath,
                color = color,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
            )
        }
    }
}

@Composable
fun EntryAnimation(
    delayMillis: Int = 0,
    content: @Composable (Modifier) -> Unit,
) {
    var start by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        start = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (start) 1f else 0f,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "alpha",
    )

    val scale by animateFloatAsState(
        targetValue = if (start) 1f else 0.8f,
        animationSpec = tween(1000, easing = EaseOutBack),
        label = "scale",
    )

    content(
        Modifier
            .alpha(alpha)
            .scale(scale),
    )
}

@Composable
fun StoryIntroductionSlide(
    saga: SagaContent,
    modifier: Modifier = Modifier,
) {
    val genre = saga.data.genre
    Box(
        modifier =
            modifier
                .fillMaxSize(),
    ) {
        Column(
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            EntryAnimation(500) { animModifier ->
                SimpleTypewriterText(
                    text = saga.data.review?.introduction ?: "",
                    style =
                        MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = genre.bodyFont(),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        ),
                    modifier = animModifier,
                    duration = 5.seconds,
                )
            }
        }

        // Bottom Saga Title (Stylized like the '2025' in the image)
        EntryAnimation(800) { animModifier ->
            Box(
                modifier =
                    animModifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 64.dp)
                        .navigationBarsPadding(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = saga.data.title,
                    style =
                        MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = genre.headerFont(),
                            fontWeight = FontWeight.Black,
                            color = genre.color,
                            textAlign = TextAlign.Center,
                            shadow =
                                androidx.compose.ui.graphics.Shadow(
                                    color = MaterialTheme.colorScheme.onBackground,
                                    offset = Offset(1f, 0f),
                                    blurRadius = 0f,
                                ),
                        ),
                )
            }
        }
    }
}

@Composable
fun StoryPlaystyleSlide(
    saga: SagaContent,
    modifier: Modifier = Modifier,
) {
    val genre = saga.data.genre
    val messagesCount = saga.messagesSize()

    SlideContainer(modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            EntryAnimation(100) { animModifier ->
                Text(
                    text = "Voices from the Dark",
                    style =
                        MaterialTheme.typography.labelLarge.copy(
                            color = genre.color.copy(alpha = 0.7f),
                            letterSpacing = 4.sp,
                            fontWeight = FontWeight.Light,
                        ),
                    modifier = animModifier,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            EntryAnimation(300) { animModifier ->
                var count by remember { mutableIntStateOf(0) }
                LaunchedEffect(Unit) {
                    val step = (messagesCount / 60).coerceAtLeast(1)
                    while (count < messagesCount) {
                        delay(16)
                        count = (count + step).coerceAtMost(messagesCount)
                    }
                }

                Text(
                    text = count.toString(),
                    style =
                        MaterialTheme.typography.displayLarge.copy(
                            fontFamily = genre.headerFont(),
                            fontSize = 120.sp,
                            brush = genre.gradient(true),
                            fontWeight = FontWeight.Black,
                        ),
                    modifier = animModifier,
                )
            }

            EntryAnimation(500) { animModifier ->
                Text(
                    text = "Words exchanged in this saga",
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontFamily = genre.bodyFont(),
                            color = Color.White.copy(alpha = 0.8f),
                        ),
                    modifier = animModifier,
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            EntryAnimation(800) { animModifier ->
                Text(
                    text = saga.data.review?.playstyle ?: "",
                    style =
                        MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = genre.bodyFont(),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 32.sp,
                        ),
                    modifier = animModifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}

@Composable
fun StoryVibeSlide(
    saga: SagaContent,
    modifier: Modifier = Modifier,
) {
    val genre = saga.data.genre
    val topTone =
        remember {
            saga
                .flatMessages()
                .filterCharacterMessages(saga.mainCharacter?.data)
                .rankEmotionalTone()
                .firstOrNull()
                ?.first ?: EmotionalTone.NEUTRAL
        }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.Black),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            EntryAnimation(200) { animModifier ->
                Text(
                    text = "YOUR AURA",
                    style =
                        MaterialTheme.typography.labelLarge.copy(
                            color = Color.White.copy(alpha = 0.5f),
                            letterSpacing = 6.sp,
                        ),
                    modifier = animModifier,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            EntryAnimation(400) { animModifier ->
                VibeShapeDrawing(
                    modifier =
                        animModifier
                            .fillMaxWidth()
                            .height(300.dp),
                    emotionalTone = topTone,
                    duration = 5000,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            EntryAnimation(600) { animModifier ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = animModifier.padding(bottom = 80.dp),
                ) {
                    Text(
                        text = topTone.getTitle().uppercase(),
                        style =
                            MaterialTheme.typography.displayLarge.copy(
                                fontFamily = genre.headerFont(),
                                fontWeight = FontWeight.Black,
                                brush =
                                    Brush.verticalGradient(
                                        listOf(topTone.color, Color.White),
                                    ),
                                fontSize = 60.sp,
                                textAlign = TextAlign.Center,
                                shadow =
                                    androidx.compose.ui.graphics.Shadow(
                                        color = MaterialTheme.colorScheme.onBackground,
                                        offset = Offset(4f, 4f),
                                        blurRadius = 0f,
                                    ),
                            ),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "This was the dominant heartbeat of your story.",
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                            ),
                        modifier = Modifier.padding(horizontal = 32.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun StoryCharactersSlide(
    saga: SagaContent,
    modifier: Modifier = Modifier,
) {
    val genre = saga.data.genre
    val topChars = saga.getCharacters(true).take(5)

    SlideContainer(modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            EntryAnimation(200) { animModifier ->
                Text(
                    text = "THE SQUAD",
                    style =
                        MaterialTheme.typography.labelLarge.copy(
                            color = genre.color.copy(alpha = 0.8f),
                            letterSpacing = 4.sp,
                        ),
                    modifier = animModifier,
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                contentAlignment = Alignment.Center,
            ) {
                topChars.forEachIndexed { index, character ->
                    EntryAnimation(400 + (index * 150)) { animModifier ->
                        val offset = ((index - (topChars.size / 2)) * 60).dp
                        CharacterAvatar(
                            character = character,
                            genre = genre,
                            modifier =
                                animModifier
                                    .size(100.dp)
                                    .offset(x = offset)
                                    .clip(CircleShape)
                                    .background(Color.DarkGray),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            EntryAnimation(800) { animModifier ->
                Text(
                    text = saga.data.review?.topCharacters ?: "",
                    style =
                        MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = genre.bodyFont(),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        ),
                    modifier = animModifier.padding(horizontal = 24.dp),
                )
            }
        }
    }
}

@Composable
fun StoryActsSlide(
    saga: SagaContent,
    modifier: Modifier = Modifier,
) {
    val genre = saga.data.genre

    SlideContainer(modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
        ) {
            EntryAnimation(200) { animModifier ->
                Text(
                    text = "THE JOURNEY",
                    style =
                        MaterialTheme.typography.labelLarge.copy(
                            color = Color.White.copy(alpha = 0.4f),
                            letterSpacing = 8.sp,
                        ),
                    modifier = animModifier,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            EntryAnimation(500) { animModifier ->
                Text(
                    text = saga.data.review?.actsInsight ?: "",
                    style =
                        MaterialTheme.typography.displaySmall.copy(
                            fontFamily = genre.headerFont(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 44.sp,
                        ),
                    modifier = animModifier,
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            EntryAnimation(800) { animModifier ->
                Box(
                    modifier =
                        animModifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(genre.gradient(true)),
                )
            }
        }
    }
}

@Composable
fun StoryConclusionSlide(
    saga: SagaContent,
    modifier: Modifier = Modifier,
) {
    val genre = saga.data.genre

    SlideContainer(modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                EntryAnimation(300) { animModifier ->
                    Text(
                        text = "Fin.",
                        style =
                            MaterialTheme.typography.displayLarge.copy(
                                fontFamily = genre.headerFont(),
                                fontSize = 80.sp,
                                brush = genre.gradient(true),
                                fontWeight = FontWeight.Black,
                            ),
                        modifier = animModifier,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                EntryAnimation(600) { animModifier ->
                    Text(
                        text = saga.data.review?.conclusion ?: "",
                        style =
                            MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = genre.bodyFont(),
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                lineHeight = 36.sp,
                            ),
                        modifier = animModifier.padding(horizontal = 32.dp),
                    )
                }
            }

            EntryAnimation(1000) { animModifier ->
                Column(
                    modifier = animModifier.padding(bottom = 120.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Created with Sagas",
                        style =
                            MaterialTheme.typography.labelMedium.copy(
                                color = Color.White.copy(alpha = 0.3f),
                                letterSpacing = 2.sp,
                            ),
                    )
                }
            }
        }
    }
}
