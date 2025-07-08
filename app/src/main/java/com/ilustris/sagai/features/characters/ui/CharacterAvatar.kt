package com.ilustris.sagai.features.characters.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.headerFont
import effectForGenre

@Composable
fun CharacterAvatar(
    character: Character,
    borderColor: Color? = null,
    innerPadding: Dp = 4.dp,
    borderSize: Dp = 2.dp,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall,
    genre: Genre,
    modifier: Modifier = Modifier,
    softFocusRadius: Float? = null,
    grainRadius: Float? = null,
) {
    val isLoaded =
        remember {
            mutableStateOf(false)
        }
    val characterColor = Color(character.hexColor.toColorInt())
    Box(
        modifier
            .border(
                borderSize,
                borderColor ?: characterColor,
                CircleShape,
            ).padding(innerPadding)
            .clip(CircleShape)
            .background(
                characterColor,
                CircleShape,
            ),
    ) {
        AsyncImage(
            model = character.image,
            contentDescription = character.name,
            onSuccess = { successState: AsyncImagePainter.State.Success ->
                isLoaded.value = true
            },
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(characterColor, CircleShape)
                    .effectForGenre(genre, focusRadius = softFocusRadius, customGrain = grainRadius),
        )

        val textAlpha by animateFloatAsState(
            if (isLoaded.value.not()) 1f else 0f,
        )

        Text(
            character.name.first().uppercase(),
            style =
                textStyle.copy(
                    fontFamily = genre.headerFont(),
                ),
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Center).graphicsLayer(textAlpha),
        )
    }
}
