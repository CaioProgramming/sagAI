package com.ilustris.sagai.features.characters.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFade

@Composable
fun CharacterAvatar(
    character: Character,
    isAnimated: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val characterColor = Color(character.hexColor.toColorInt())
    AsyncImage(
        character.image,
        contentDescription = character.name,
        modifier =
            modifier
                .clip(CircleShape)
                .background(
                    if (isAnimated.not()) {
                        characterColor.gradientFade()
                    } else {
                        gradientAnimation(
                            characterColor.darkerPalette(),
                        )
                    },
                    CircleShape,
                ).border(
                    1.dp,
                    characterColor,
                    CircleShape,
                ),
    )
}
