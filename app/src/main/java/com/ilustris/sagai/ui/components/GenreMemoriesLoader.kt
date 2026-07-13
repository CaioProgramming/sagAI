package com.ilustris.sagai.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.animations.rememberLifecycleAnimationsActive
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.ThemeCover
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.sagaBrush
import com.ilustris.sagai.ui.theme.themePainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random

private const val GENRE_MEMORY_CROSSFADE_MS = 700
private const val GENRE_MEMORY_SCALE_MIN = 0.8f
private const val GENRE_MEMORY_SCALE_MAX = 1f

@Composable
fun GenreMemoriesLoader(
    genresConfigs: List<Pair<Genre, GenreVisualConfig?>>,
    modifier: Modifier,
) {
    val genres = remember(genresConfigs) { genresConfigs.map { it.first }.distinct() }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = false,
    ) {
        items(
            items = genres,
            key = { it.name },
        ) { genre ->
            GenreMemoryItem(
                genre = genre,
                modifier = Modifier.padding(4.dp),
            )
        }
    }
}

@Composable
private fun GenreMemoryItem(
    genre: Genre,
    modifier: Modifier = Modifier,
) {
    SagAITheme(genre) {
        val context = LocalContext.current
        val imageUrl = ThemeCover()
        var isImageLoaded by remember(imageUrl) { mutableStateOf(false) }
        val shadowBrush = sagaBrush()
        val animationsActive = rememberLifecycleAnimationsActive()
        var targetScale by remember { mutableFloatStateOf(randomGenreMemoryScale()) }

        LaunchedEffect(animationsActive) {
            if (!animationsActive) {
                targetScale = 1f
                return@LaunchedEffect
            }
            while (isActive) {
                delay(Random.nextLong(from = 1_400, until = 2_800))
                targetScale = randomGenreMemoryScale()
            }
        }

        LaunchedEffect(imageUrl) {
            isImageLoaded = false
            val url = imageUrl ?: return@LaunchedEffect
            isImageLoaded =
                context.imageLoader.execute(
                    ImageRequest.Builder(context).data(url).build(),
                ) is SuccessResult
        }

        val scale by animateFloatAsState(
            targetValue = targetScale,
            animationSpec = tween(GENRE_MEMORY_CROSSFADE_MS),
            label = "genreMemoryScale",
        )

        val imageAlpha by animateFloatAsState(
            targetValue = if (isImageLoaded) 1f else 0f,
            animationSpec = tween(GENRE_MEMORY_CROSSFADE_MS),
            label = "genreMemoryImageAlpha",
        )

        Box(
            modifier =
                modifier
                    .height(100.dp)
                    .fillMaxWidth()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = isImageLoaded,
                transitionSpec = {
                    fadeIn(tween(GENRE_MEMORY_CROSSFADE_MS)) +
                        scaleIn(tween(GENRE_MEMORY_CROSSFADE_MS / 2)) togetherWith
                        fadeOut(tween(GENRE_MEMORY_CROSSFADE_MS)) +
                        scaleOut(tween(GENRE_MEMORY_CROSSFADE_MS / 2))
                },
                label = "genreMemoryCover",
            ) { loaded ->
                if (!loaded) {
                    Image(
                        painter = themePainter(),
                        contentDescription = null,
                        modifier =
                            Modifier
                                .size(40.dp)
                                .gradientFill(shadowBrush),
                    )
                } else {
                    Box(
                        modifier =
                            Modifier
                                .matchParentSize()
                                .graphicsLayer { alpha = imageAlpha }
                                .dropShadow(MaterialTheme.shapes.medium) {
                                    radius = 20f
                                    spread = 1f
                                    brush = shadowBrush
                                    alpha = 0.5f
                                }
                                .border(1.dp, shadowBrush, MaterialTheme.shapes.medium)
                                .clip(MaterialTheme.shapes.medium),
                    ) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

private fun randomGenreMemoryScale(): Float =
    GENRE_MEMORY_SCALE_MIN +
        Random.nextFloat() * (GENRE_MEMORY_SCALE_MAX - GENRE_MEMORY_SCALE_MIN)
