package com.ilustris.sagai.features.characters.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.newsaga.data.model.resolveIconColor
import com.ilustris.sagai.features.newsaga.data.model.shimmerColors
import com.ilustris.sagai.ui.theme.darker
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.solidGradient

@Composable
fun CharacterAvatar(
    character: Character,
    borderColor: Color? = null,
    innerPadding: Dp = 4.dp,
    borderSize: Dp = 2.dp,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall,
    genre: Genre,
    isLoading: Boolean = false,
    useFallback: Boolean = false,
    modifier: Modifier = Modifier,
    softFocusRadius: Float? = null,
    grainRadius: Float? = null,
    pixelation: Float? = null,
) {
    val resolvedColor = genre.resolveColor()
    val resolvedIconColor = genre.resolveIconColor()
    val characterColor = character.hexColor.hexToColor() ?: resolvedColor
    val borderBrush =
        borderColor?.solidGradient() ?: Brush.verticalGradient(
            listOf(
                characterColor,
                resolvedIconColor,
            ),
        )

    val smartZoom = character.smartZoom

    val animatedScale by animateFloatAsState(
        targetValue = smartZoom?.scale ?: 1f,
        label = "SmartZoomScale",
    )
    val animatedTranslationX by animateFloatAsState(
        targetValue = smartZoom?.translationX ?: 0f,
        label = "SmartZoomTranslationX",
    )
    val animatedTranslationY by animateFloatAsState(
        targetValue = smartZoom?.translationY ?: 0f,
        label = "SmartZoomTranslationY",
    )

    Box(
        modifier
            .reactiveShimmer(isLoading, genre.shimmerColors())
            .border(
                borderSize,
                borderBrush,
                CircleShape,
            ).clip(CircleShape)
            .padding(innerPadding)
            .background(
                characterColor.darker(.3f),
                CircleShape,
            ),
    ) {
        androidx.compose.material3.Text(
            character.name.first().uppercase(),
            style =
                textStyle.copy(
                    fontFamily = genre.headerFont(),
                    brush =
                        Brush.verticalGradient(
                            characterColor.darkerPalette(factor = .2f),
                        ),
                ),
            fontWeight = FontWeight.Black,
            modifier = Modifier.align(Alignment.Center),
        )

        AsyncImage(
            model =
                ImageRequest
                    .Builder(LocalContext.current)
                    .data(character.image)
                    .crossfade(true)
                    .build(),
            contentDescription = character.name,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .clip(CircleShape)
                    .background(
                        characterColor.copy(alpha = 0.4f),
                        CircleShape,
                    ).fillMaxSize()
                    .effectForGenre(
                        genre,
                        useFallBack = useFallback,
                        focusRadius = softFocusRadius,
                        customGrain = grainRadius,
                        pixelSize = pixelation,
                    ).graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                        translationX = animatedTranslationX * size.width
                        translationY = animatedTranslationY * size.height
                        transformOrigin = TransformOrigin.Center
                    }.clipToBounds(),
        )
    }
}
