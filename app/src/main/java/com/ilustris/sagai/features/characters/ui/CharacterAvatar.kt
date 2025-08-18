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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.ui.theme.darker
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.gradientFade
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
    pixelation: Float? = null,
) {
    val characterColor = Color(character.hexColor.toColorInt())
    Box(
        modifier
            .border(
                borderSize,
                borderColor ?: characterColor,
                CircleShape,
            ).clip(CircleShape)
            .padding(innerPadding)
            .background(
                characterColor.darker(.3f),
                CircleShape,
            ),
    ) {
        var painterState by remember {
            mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty)
        }
        AsyncImage(
            model = character.image,
            contentDescription = character.name,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.ic_spark),
            error = null,
            onError = {
                painterState = it
            },
            modifier =
                Modifier
                    .clip(CircleShape)
                    .fillMaxSize()
                    .background(characterColor.darker(.3f), CircleShape)
                    .effectForGenre(
                        genre,
                        focusRadius = softFocusRadius,
                        customGrain = grainRadius,
                        pixelSize = pixelation,
                    ),
        )

        if (painterState is AsyncImagePainter.State.Error) {
            Text(
                character.name.first().uppercase(),
                style =
                    textStyle.copy(
                        fontFamily = genre.headerFont(),
                        brush = characterColor.darker().gradientFade(),
                    ),
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}
