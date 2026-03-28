package com.ilustris.sagai.ui.animations

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.onboarding.data.OnboardingAsset
import com.ilustris.sagai.ui.theme.glow
import com.ilustris.sagai.ui.theme.levitate
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

@Composable
fun SpreadedAvatarsBackground(
    icons: List<String>,
    avatarSize: Dp = 64.dp,
) {
    val displayCharacters =
        remember(icons) {
            icons.distinctBy { it }
        }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxWidth = constraints.maxWidth.toFloat()
        val maxHeight = constraints.maxHeight.toFloat()

        val positions =
            remember(maxWidth, maxHeight) {
                val list = mutableListOf<Pair<Float, Float>>()
                if (maxWidth > 0 && maxHeight > 0) {
                    repeat(icons.size) {
                        var attempts = 0
                        var pos: Pair<Float, Float>
                        val minDistance = 200f

                        do {
                            pos =
                                Pair(
                                    Random.nextFloat() * 0.6f + 0.2f,
                                    Random.nextFloat() * 0.6f + 0.2f,
                                )
                            attempts++
                            val isOverlapping =
                                list.any { existing ->
                                    val dx = (existing.first - pos.first) * maxWidth
                                    val dy = (existing.second - pos.second) * maxHeight
                                    (dx * dx + dy * dy) < (minDistance * minDistance)
                                }
                        } while (isOverlapping && attempts < 20)
                        list.add(pos)
                    }
                }
                list
            }

        positions.forEachIndexed { index, pos ->
            val character = displayCharacters[index % displayCharacters.size]
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX = pos.first * maxWidth - (maxWidth / 2)
                            translationY = pos.second * maxHeight - (maxHeight / 2)
                        },
            ) {
                AsyncImage(
                    character,
                    contentDescription = null,
                    modifier =
                        Modifier
                            .clip(CircleShape)
                            .size(avatarSize)
                            .align(Alignment.Center)
                            .border(1.dp, Color.White, CircleShape)
                            .levitate(yOffset = Random.nextInt(15, 30).toFloat()),
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }
}

@Composable
fun MorphingAvatarBackground(icons: List<String>) {
    val displayCharacters =
        remember(icons) {
            icons.distinctBy { it }
        }
    var fixedCharacter by remember {
        mutableStateOf(icons.random())
    }

    LaunchedEffect(fixedCharacter) {
        delay(2.seconds)
        fixedCharacter = displayCharacters.filter { it != fixedCharacter }.random()
    }

    Box(
        modifier = Modifier.padding(vertical = 32.dp).fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(fixedCharacter, transitionSpec = {
            scaleIn() + fadeIn() togetherWith scaleOut()
        }) {
            AsyncImage(
                it,
                contentDescription = null,
                modifier =
                    Modifier
                        .clip(CircleShape)
                        .size(100.dp)
                        .levitate()
                        .glow(
                            MaterialTheme.colorScheme.primary,
                            true,
                        ),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
fun StackedCardsBackground(
    assets: List<OnboardingAsset> = emptyList(),
    genre: Genre = Genre.FANTASY,
    cycleDelay: Long = 4000L,
) {
    val displayAssets =
        remember(assets) {
            assets
        }
    var topIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(displayAssets) {
        if (displayAssets.isNotEmpty()) {
            while (true) {
                delay(cycleDelay)
                topIndex = (topIndex + 1) % displayAssets.size
            }
        }
    }

    Box(
        modifier = Modifier.padding(vertical = 32.dp).fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        StarryTextPlaceholder(Modifier.fillMaxSize())
        if (displayAssets.isEmpty()) {
            Icon(
                painter = painterResource(R.drawable.ic_spark),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier
                        .size(120.dp)
                        .levitate(true),
            )
        } else {
            for (i in 0 until 3) {
                val offsetIndex = (topIndex + (2 - i)) % displayAssets.size
                val asset = displayAssets[offsetIndex]

                val rotation =
                    when (i) {
                        0 -> -8f
                        1 -> 6f
                        else -> 0f
                    }
                val xOffset =
                    when (i) {
                        0 -> (-15).dp
                        1 -> 12.dp
                        else -> 0.dp
                    }
                val yOffset =
                    when (i) {
                        0 -> (-8).dp
                        1 -> 4.dp
                        else -> 0.dp
                    }

                AnimatedContent(
                    targetState = asset,
                    transitionSpec = {
                        val enterTransition =
                            if (i == 2) {
                                fadeIn(tween(800)) +
                                    scaleIn(
                                        tween(800, delayMillis = 100),
                                        initialScale = 0.9f,
                                    )
                            } else {
                                fadeIn(tween(1000, delayMillis = 200))
                            }

                        val exitTransition =
                            if (i == 2) {
                                slideOutVertically(tween(600)) { -it / 2 } + fadeOut(tween(600))
                            } else {
                                fadeOut(tween(800))
                            }

                        enterTransition togetherWith exitTransition
                    },
                    modifier =
                        Modifier
                            .graphicsLayer {
                                val rotationVal = rotation
                                val translationXVal = xOffset.toPx()
                                val translationYVal = yOffset.toPx()
                                rotationZ = rotationVal
                                translationX = translationXVal
                                translationY = translationYVal
                                val scale = if (i == 2) 1f else 0.95f
                                scaleX = scale
                                scaleY = scale
                                alpha =
                                    if (i == 0) {
                                        0.6f
                                    } else if (i == 1) {
                                        0.8f
                                    } else {
                                        1f
                                    }
                            },
                    label = "stacked_card_$i",
                ) { targetAsset ->
                    androidx.compose.material3.Card(
                        modifier =
                            Modifier
                                .size(width = 220.dp, height = 300.dp)
                                .levitate(i == 2),
                        shape =
                            androidx.compose.foundation.shape
                                .RoundedCornerShape(20.dp),
                        elevation =
                            androidx.compose.material3.CardDefaults.cardElevation(
                                defaultElevation = if (i == 2) 12.dp else 4.dp,
                            ),
                    ) {
                        AsyncImage(
                            model = targetAsset.image,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }
        }
    }
}
