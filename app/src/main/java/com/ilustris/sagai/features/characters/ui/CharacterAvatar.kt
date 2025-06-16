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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
// import androidx.core.graphics.drawable.toBitmap // No longer directly used here
// import androidx.palette.graphics.Palette // No longer directly used here
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.grayScale

@Composable
fun CharacterAvatar(
    character: Character,
    borderColor: Color? = null,
    innerPadding: Dp = 4.dp,
    borderSize: Dp = 2.dp,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall,
    modifier: Modifier = Modifier,
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
            )
            .padding(innerPadding)
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
            modifier =
                Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(characterColor, CircleShape)
        )

        val textAlpha by animateFloatAsState(
            if (isLoaded.value.not()) 1f else 0f,
        )

        Text(
            character.name.first().uppercase(),
            style = textStyle,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.Center).graphicsLayer(textAlpha),
        )
    }
}
