package com.ilustris.sagai.features.characters.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.solidGradient
import com.ilustris.sagai.ui.theme.themeShimmer

/** Avatars are small; genre shaders / heavy blur destroy legibility on tiny targets. */
private val GenreEffectMaxSize = 120.dp

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
    shape: Shape = CircleShape,
    softFocusRadius: Float? = null,
    grainRadius: Float? = null,
    pixelation: Float? = null,
) {
    val resolvedColor = MaterialTheme.colorScheme.primary
    val resolvedIconColor = MaterialTheme.colorScheme.secondary
    val characterColor = character.hexColor.hexToColor() ?: resolvedColor
    val borderBrush =
        borderColor?.solidGradient() ?: Brush.verticalGradient(
            listOf(
                characterColor,
                resolvedIconColor,
            ),
        )

    val imagePath = character.image.trim()
    var imageLoadFailed by remember(character.id, imagePath) { mutableStateOf(imagePath.isBlank()) }

    BoxWithConstraints(
        modifier
            .reactiveShimmer(isLoading, themeShimmer())
            .border(
                borderSize,
                borderBrush,
                shape,
            )
            .clip(shape)
            .padding(innerPadding)
            .background(
                Brush.verticalGradient(characterColor.darkerPalette(factor = .35f)),
                shape,
            ),
    ) {
        val skipGenreEffect = maxWidth < GenreEffectMaxSize && maxHeight < GenreEffectMaxSize

        if (imageLoadFailed) {
            Text(
                character.name.first().uppercase(),
                style =
                    textStyle.copy(
                        fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                        brush =
                            MaterialTheme.colorScheme.onBackground.gradientFade(),
                    ),
                fontWeight = FontWeight.Black,
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .alpha(.4f),
            )
        }

        AnimatedContent(imagePath, transitionSpec = {
            scaleIn() togetherWith scaleOut()
        }) {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(LocalContext.current)
                        .data(it)
                        .apply {
                            if (imagePath.isNotBlank()) {
                                memoryCacheKey("${character.id}:$imagePath")
                                diskCacheKey("${character.id}:$imagePath")
                            }
                        }.crossfade(true)
                        .build(),
                contentDescription = character.name,
                contentScale = ContentScale.Crop,
                onState = { state ->
                    imageLoadFailed =
                        when (state) {
                            is AsyncImagePainter.State.Error,
                            is AsyncImagePainter.State.Empty,
                            -> true

                            is AsyncImagePainter.State.Success -> false

                            else -> imageLoadFailed
                        }
                },
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(shape)
                        .then(
                            if (!skipGenreEffect) {
                                Modifier.effectForGenre(
                                    genre,
                                    useFallBack = useFallback,
                                    focusRadius = softFocusRadius,
                                    customGrain = grainRadius,
                                    pixelSize = pixelation,
                                )
                            } else {
                                Modifier
                            },
                        ),
            )
        }
    }
}
