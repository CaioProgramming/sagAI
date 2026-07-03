package com.ilustris.sagai.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
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
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.ThemeCover
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.iconDropShadow
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.rememberVectorShape
import com.ilustris.sagai.ui.theme.themeIconVector
import com.ilustris.sagai.ui.theme.themePainter
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

private const val GENRE_MEMORY_CROSSFADE_MS = 800

@Composable
fun GenreMemoriesLoader(
    genresConfigs: List<Pair<Genre, GenreVisualConfig?>>,
    modifier: Modifier,
) {
    var configs by remember { mutableStateOf(genresConfigs.shuffled()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1500)
            configs = genresConfigs.shuffled()
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = false,
    ) {
        items(configs, key = { it }) { genre ->
            GenreMemoryItem(
                genre = genre.first,
                modifier =
                    Modifier
                        .padding(4.dp)
                        .animateItem(),
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun GenreMemoryItem(
    genre: Genre,
    modifier: Modifier = Modifier,
) {
    SagAITheme(genre) {
        val context = LocalContext.current
        val imageUrl = ThemeCover()
        var isImageLoaded by remember(imageUrl) { mutableStateOf(false) }

        LaunchedEffect(imageUrl) {
            isImageLoaded = false
            val url = imageUrl ?: return@LaunchedEffect
            isImageLoaded =
                context.imageLoader.execute(
                    ImageRequest.Builder(context).data(url).build(),
                ) is SuccessResult
        }

        val shadowBrush = Brush.verticalGradient(morphingGradient(duration = 5.seconds))
        val sharedContentKey = "${genre.name.lowercase()}_icon"

        val imageAlpha by animateFloatAsState(
            targetValue = if (isImageLoaded) 1f else 0f,
            animationSpec = tween(GENRE_MEMORY_CROSSFADE_MS),
            label = "genreMemoryImageAlpha",
        )
        Box(
            modifier =
                modifier
                    .height(100.dp)
                    .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                isImageLoaded,
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
                                .iconDropShadow(
                                    shape = rememberVectorShape(themeIconVector()),
                                    brush = shadowBrush,
                                    progress = 1f,
                                    spreadRadius = 1.dp,
                                    blurRadius = 20.dp,
                                ).gradientFill(shadowBrush),
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
                                    alpha = .5f
                                }.border(1.dp, shadowBrush, MaterialTheme.shapes.medium)
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
