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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.gradientFade

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

@Composable
fun GenreAvatar(
    genre: Genre,
    showText: Boolean = true,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (Genre) -> Unit,
) {
    val backgroundColor by animateColorAsState(
        if (isSelected) genre.color else Color.Transparent,
    )

    val iconTint by animateColorAsState(
        if (isSelected) genre.iconColor else MaterialTheme.colorScheme.onBackground,
        animationSpec = tween(400, easing = EaseIn),
    )
    val scale by animateFloatAsState(
        if (isSelected) 1.1f else 1f,
    )
    Column(
        modifier = modifier.padding(4.dp).scale(scale),
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
