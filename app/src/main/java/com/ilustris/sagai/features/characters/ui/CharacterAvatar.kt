package com.ilustris.sagai.features.characters.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
// import androidx.core.graphics.drawable.toBitmap // No longer directly used here
// import androidx.palette.graphics.Palette // No longer directly used here
import coil3.asDrawable
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.ilustris.sagai.core.ui.extensions.generatePaletteAsync // Import the extension
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.ui.theme.gradientFade

@Composable
fun CharacterAvatar(
    character: Character,
    isAnimated: Boolean = false, // isAnimated is not used, consider removing if not planned for this composable
    initialBorderColor: Color = MaterialTheme.colorScheme.background,
    borderSize: Dp = 2.dp,
    modifier: Modifier = Modifier,
) {
    var dynamicBorderBrush by remember {
        mutableStateOf(
            Brush.linearGradient(
                List(2) {
                    initialBorderColor
                },
            ),
        )
    }

    val context = LocalContext.current

    Box(
        modifier
            .border(
                borderSize,
                initialBorderColor, // Use the dynamic brush for the border
                CircleShape,
            ).clip(CircleShape)
            .background(
                MaterialTheme.colorScheme.surfaceContainer,
                CircleShape,
            ),
    ) {
        AsyncImage(
            model = character.image,
            contentDescription = character.name,
            onSuccess = { successState: AsyncImagePainter.State.Success ->
                val image = successState.result.image
                val drawable = image.asDrawable(context.resources)

                drawable.generatePaletteAsync { palette ->
                    // Use the extension function

                    val gradientColors = mutableListOf<Color>()
                    palette?.dominantSwatch?.rgb?.let {
                        gradientColors.add(Color(it))
                    }
                    palette?.vibrantSwatch?.rgb?.let { gradientColors.add(Color(it)) }
                    palette?.mutedSwatch?.rgb?.let { gradientColors.add(Color(it)) }

                    val distinctColors = gradientColors.distinct()

                    // Update the brush based on the palette
                    dynamicBorderBrush =
                        when {
                            distinctColors.size >= 2 -> Brush.linearGradient(colors = distinctColors)
                            distinctColors.isNotEmpty() -> distinctColors.first().gradientFade()
                            // Fallback to dominant color's gradient, or initial if dominant is not found
                            else ->
                                (
                                    palette?.dominantSwatch?.rgb?.let { Color(it) }
                                        ?: initialBorderColor
                                ).gradientFade()
                        }
                }
            },
            modifier =
                Modifier.fillMaxSize().clip(CircleShape),
        )
    }
}
