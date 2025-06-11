package com.ilustris.sagai.features.characters.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.characters.data.model.Character

@Composable
fun CharacterAvatar(
    character: Character,
    isAnimated: Boolean = false,
    borderColor: Color = MaterialTheme.colorScheme.onBackground,
    borderSize: Dp = 2.dp,
    modifier: Modifier = Modifier,
) {
    val characterColor = Color(character.hexColor.toColorInt())
    AsyncImage(
        character.image,
        contentDescription = character.name,
        modifier =
            modifier
                .clip(CircleShape)
                .border(
                    borderSize,
                    borderColor,
                    CircleShape,
                ),
    )
}
