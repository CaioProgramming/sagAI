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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.shimmerColors
import com.ilustris.sagai.ui.theme.darker
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.solidGradient
import effectForGenre
import kotlinx.coroutines.launch

@Composable
fun CharacterAvatar(
    character: Character,
    borderColor: Color? = null,
    innerPadding: Dp = 4.dp,
    borderSize: Dp = 2.dp,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall,
    genre: Genre,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
    softFocusRadius: Float? = null,
    grainRadius: Float? = null,
    pixelation: Float? = null,
    requireZoom: Boolean = true,
) {
    val viewModel: CharacterAvatarViewModel = hiltViewModel()
    val characterColor = character.hexColor.hexToColor() ?: genre.color
    val borderBrush =
        borderColor?.solidGradient() ?: Brush.verticalGradient(
            listOf(
                characterColor,
                genre.iconColor,
            ),
        )

    val smartZoom = if (requireZoom) character.smartZoom else null

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

    LaunchedEffect(character.image, character.smartZoom) {
        if (character.image.isNotEmpty() && character.smartZoom == null && requireZoom) {
            launch {
                viewModel.checkAndGenerateZoom(character)
            }
        }
    }

    Box(
        modifier
            .reactiveShimmer(isLoading, genre.shimmerColors())
            .border(
                borderSize,
                borderBrush,
                CircleShape,
            )
            .clip(CircleShape)
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
            error = null,
            onError = {
                painterState = it
            },
            modifier =
                Modifier
                    .clip(CircleShape)
                    .background(
                        characterColor,
                        CircleShape,
                    )
                    .fillMaxSize()
                    .effectForGenre(
                        genre,
                        useFallBack = character.emojified,
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

        if (painterState is AsyncImagePainter.State.Error) {
            Text(
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
        }
    }
}
