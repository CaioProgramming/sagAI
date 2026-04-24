package com.ilustris.sagai.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.ai.model.LocalGenreVisualConfig
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.ui.theme.cornerSize
import kotlinx.coroutines.delay

@Composable
fun GenreMemoriesLoader(
    genresConfigs: List<Pair<Genre, GenreVisualConfig?>>,
    isLoading: Boolean,
    message: String,
    modifier: Modifier = Modifier,
) {
    var configs by remember { mutableStateOf(genresConfigs.shuffled()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1500)
            configs = genresConfigs.shuffled()
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .fillMaxSize()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            userScrollEnabled = false,
        ) {
            items(configs, key = { it }) { genre ->
                GenreMemoryItem(
                    genreConfig = genre,
                    modifier = Modifier.padding(4.dp).animateItem(),
                )
            }
        }
    }
}

@Composable
private fun GenreMemoryItem(
    genreConfig: Pair<Genre, GenreVisualConfig?>,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "Alpha",
    )
    val genre = genreConfig.first

    CompositionLocalProvider(
        LocalGenreVisualConfig provides genreConfig.second,
    ) {
        val color = genre.resolveColor()
        val shape = RoundedCornerShape(genre.cornerSize())

        AsyncImage(
            genreConfig.second?.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                modifier
                    .aspectRatio(1f)
                    .dropShadow(shape) {
                        this.color = color.copy(alpha = glowAlpha)
                        this.radius = 15f
                        this.spread = 5f
                    }.clip(shape)
                    .background(color)
                    .fillMaxSize(),
        )
    }
}
