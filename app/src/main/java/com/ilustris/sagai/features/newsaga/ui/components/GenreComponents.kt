package com.ilustris.sagai.features.newsaga.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.defaultHeaderImage
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.filters.SelectiveColorParams
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.grayScale
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.solidGradient
import com.ilustris.sagai.ui.theme.zoomAnimation
import effectForGenre
import kotlin.time.Duration.Companion.seconds

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
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (Genre) -> Unit,
) {
    val backgroundColor by animateColorAsState(
        if (isSelected) genre.color else MaterialTheme.colorScheme.surfaceContainer,
    )

    val iconTint by animateColorAsState(
        if (isSelected) genre.iconColor else MaterialTheme.colorScheme.onBackground,
        animationSpec = tween(400, easing = EaseIn),
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
        Image(
            painterResource(genre.icon),
            genre.name,
            colorFilter =
                androidx.compose.ui.graphics.ColorFilter
                    .tint(iconTint),
            modifier =
                Modifier
                    .size(50.dp)
                    .border(1.dp, backgroundColor.gradientFade(), CircleShape)
                    .padding(4.dp)
                    .background(
                        backgroundColor,
                        CircleShape,
                    ).padding(4.dp)
                    .clip(CircleShape)
                    .clickable {
                        onClick(genre)
                    },
        )

        if (showText) {
            Text(
                genre.title,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall,
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
    onClick: (Genre) -> Unit,
) {
    val saturation by animateFloatAsState(
        if (isSelected) 1f else 0f,
        tween(500, easing = EaseIn),
    )
    val scale by animateFloatAsState(
        if (isSelected) 1f else .8f,
        tween(700, easing = EaseIn),
    )

    val borderColor = genre.gradient()

    val shape = RoundedCornerShape(genre.cornerSize())

    Box(
        modifier
            .border(
                2.dp,
                borderColor,
                shape,
            ).clip(RoundedCornerShape(genre.cornerSize()))
            .clipToBounds()
            .clickable {
                onClick(genre)
            },
    ) {
        val imageUrl = genre.defaultHeaderImage()

        Image(
            painterResource(imageUrl),
            genre.name,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .fillMaxSize()
                    .effectForGenre(genre)
                    .selectiveColorHighlight(
                        genre.selectiveHighlight(),
                    ).clipToBounds(),
        )

        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .3f)))

        Text(
            genre.title,
            textAlign = TextAlign.Center,
            style =
                MaterialTheme.typography.titleMedium.copy(
                    fontFamily = genre.headerFont(),
                    brush = genre.gradient(true, 1.seconds, 300f),
                ),
            modifier =
                Modifier.fillMaxWidth()
                    .padding(2.dp)
                    .align(Alignment.Center),
        )
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
            mutableStateOf(Genre.SCI_FI)
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
            GenreCard(genre, isSelected = true, modifier = Modifier.fillMaxWidth().size(300.dp)) {
                selectedGenre.value = it
            }
        }
    }
}
