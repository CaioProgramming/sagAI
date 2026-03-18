package com.ilustris.sagai.features.newsaga.ui.components

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.newsaga.data.model.resolveImageUrl
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.ui.components.stylisedText
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.gradient

@Composable
fun GenreSelectionCard(
    selectedGenre: Genre? = null,
    selectItem: (Genre) -> Unit = {},
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LazyRow(
            modifier =
                Modifier
                    .wrapContentSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val genres = Genre.entries
            items(genres) {
                GenreAvatar(it, isSelected = it == selectedGenre) {
                    selectItem(it)
                }
            }
        }
    }
}

@Composable
fun GenreAvatar(
    genre: Genre,
    showText: Boolean = true,
    iconSize: Dp = 64.dp,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (Genre) -> Unit,
) {
    val resolvedColor = genre.resolveColor()
    val backgroundColor by animateColorAsState(
        if (isSelected) resolvedColor else MaterialTheme.colorScheme.surfaceContainer,
    )

    val scale by animateFloatAsState(
        if (isSelected) 1.1f else 1f,
        tween(),
    )
    Column(
        modifier =
            modifier
                .padding(4.dp)
                .scale(scale),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val backgroundBrush = Brush.verticalGradient(backgroundColor.darkerPalette())
        AsyncImage(
            model =
                ImageRequest
                    .Builder(LocalContext.current)
                    .data(genre.resolveImageUrl())
                    .crossfade(true)
                    .build(),
            contentDescription = stringResource(genre.title),
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .size(iconSize)
                    .border(1.dp, backgroundBrush, CircleShape)
                    .background(
                        backgroundBrush,
                        CircleShape,
                    ).clip(CircleShape)
                    .effectForGenre(genre, customGrain = 0f)
                    .selectiveColorHighlight(genre.selectiveHighlight())
                    .clickable {
                        onClick(genre)
                    },
        )

        if (showText) {
            Text(
                stringResource(genre.title),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Light,
            )
        }
    }
}

@Preview
@Composable
fun GenreAvatarPreview() {
    GenreAvatar(
        genre = Genre.FANTASY,
        showText = true,
        isSelected = false,
        modifier = Modifier,
        onClick = {},
    )
}

@Composable
fun GenreCard(
    genre: Genre,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    showText: Boolean = true,
    onClick: (Genre) -> Unit,
) {
    val shape = genre.bubble(isNarrator = true)
    var showDetails by remember {
        mutableStateOf(false)
    }

    val borderSize by animateDpAsState(
        if (showDetails) 1.dp else 0.dp,
    )

    val shadowRadius by animateFloatAsState(
        if (showDetails) 15f else 0f,
    )
    val borderColor = genre.gradient(true)
    val image = genre.resolveImageUrl()

    Box(
        modifier
            .dropShadow(shape) {
                radius = shadowRadius
                this.brush = borderColor
                this.spread = shadowRadius
            }.clip(shape)
            .border(borderSize, borderColor, shape)
            .background(MaterialTheme.colorScheme.background),
    ) {
        AsyncImage(
            model =
                ImageRequest
                    .Builder(LocalContext.current)
                    .data(image)
                    .crossfade(true)
                    .build(),
            contentDescription = genre.name,
            contentScale = ContentScale.Crop,
            onSuccess = {
                showDetails = true
            },
            onError = {
                showDetails = false
                Log.i("GenreComponent", "GenreCard: Failed to load -> $image ")
            },
            modifier =
                Modifier
                    .fillMaxSize()
                    .effectForGenre(genre)
                    .selectiveColorHighlight(genre),
        )

        if (showText) {
            genre.stylisedText(
                stringResource(genre.title),
                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GenreSelectionCardPreview() {
    val selectedGenre =
        remember {
            mutableStateOf(Genre.FANTASY)
        }
    GenreSelectionCard(
        selectedGenre = selectedGenre.value,
        selectItem = {
            selectedGenre.value = it
        },
    )
}

@Preview(showBackground = true)
@Composable
fun GenreCardPreview() {
    val selectedGenre =
        remember {
            mutableStateOf(Genre.CYBERPUNK)
        }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(0.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(Genre.entries.size) {
            val genre = Genre.entries[it]
            GenreCard(
                genre,
                isSelected = true,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .size(300.dp),
            ) {
                selectedGenre.value = it
            }
        }
    }
}
