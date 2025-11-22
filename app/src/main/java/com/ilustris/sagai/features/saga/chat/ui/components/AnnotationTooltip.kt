package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.shape

@Composable
fun AnnotationTooltip(
    data: Any,
    genre: Genre,
    shape: androidx.compose.ui.graphics.Shape = genre.shape(),
) {
    Column(
        Modifier
            .padding(horizontal = 32.dp)
            .border(
                1.dp,
                Brush.verticalGradient(genre.color.darkerPalette(factor = .3f)),
                shape,
            )
            .background(
                Brush.verticalGradient(
                    MaterialTheme.colorScheme.surfaceContainer.darkerPalette(
                        factor = .3f,
                    ),
                ),
                shape,
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when (data) {
                is Character -> {
                    val characterColor = data.hexColor.hexToColor() ?: genre.color

                    CharacterAvatar(
                        data,
                        characterColor,
                        0.dp,
                        1.dp,
                        modifier = Modifier.size(32.dp),
                        genre = genre,
                        grainRadius = 0f,
                        softFocusRadius = 0f,
                    )

                    Text(
                        text = data.name,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                brush = Brush.verticalGradient(characterColor.darkerPalette()),
                                fontFamily = genre.bodyFont(),
                                fontWeight = FontWeight.Bold,
                            ),
                    )
                }
                is Wiki -> {
                    Text(
                        text = data.emojiTag ?: emptyString(),
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = genre.bodyFont(),
                            ),
                    )

                    Text(
                        text = data.title,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = genre.bodyFont(),
                                fontWeight = FontWeight.Bold,
                            ),
                    )
                }
            }
        }

        val text =
            when (data) {
                is Character -> {
                    data.backstory
                }

                is Wiki -> {
                    data.content
                }

                else -> emptyString()
            }

        Text(
            text = text,
            maxLines = 5,
            overflow = TextOverflow.Ellipsis,
            style =
                MaterialTheme.typography.labelLarge.copy(
                    fontFamily = genre.bodyFont(),
                ),
            textAlign = TextAlign.Start,
        )
    }
}
