package com.ilustris.sagai.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.iconDropShadow
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.themeBrushColors
import kotlinx.coroutines.delay

private const val GENRE_MEMORY_CROSSFADE_MS = 300

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
                genreConfig = genre,
                modifier =
                    Modifier
                        .padding(4.dp)
                        .animateItem(),
            )
        }
    }
}

@Composable
private fun GenreMemoryItem(
    genreConfig: Pair<Genre, GenreVisualConfig?>,
    modifier: Modifier = Modifier,
) {
    val (genre, config) = genreConfig
    val imageUrl = config?.imageUrl
    var isImageLoaded by remember(imageUrl) { mutableStateOf(false) }

    val shape = RoundedCornerShape(config?.cornerSizeDp?.dp ?: 10.dp)
    val shadowBrush =
        config?.colorPalette?.let { palette ->
            Brush.verticalGradient(
                colors = palette.mapNotNull { paletteColor -> paletteColor.hexToColor() },
            )
        } ?: Brush.verticalGradient(themeBrushColors())

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
        AnimatedVisibility(
            visible = !isImageLoaded,
            enter = fadeIn(tween(GENRE_MEMORY_CROSSFADE_MS)),
            exit = fadeOut(tween(GENRE_MEMORY_CROSSFADE_MS)),
        ) {
            val morphBrush = Brush.verticalGradient(morphingGradient())
            Image(
                painter = painterResource(genre.icon),
                contentDescription = null,
                modifier =
                    Modifier
                        .size(40.dp)
                        .iconDropShadow(
                            brush = morphBrush,
                            progress = 1f,
                        ).gradientFill(morphBrush),
            )
        }

        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .graphicsLayer { alpha = imageAlpha }
                    .dropShadow(shape) {
                        radius = 15f
                        spread = 5f
                        brush = shadowBrush
                    }.clip(shape),
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onState = { state ->
                    isImageLoaded = state is AsyncImagePainter.State.Success
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
