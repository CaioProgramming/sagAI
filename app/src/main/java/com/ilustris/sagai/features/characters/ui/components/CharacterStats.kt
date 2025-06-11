package com.ilustris.sagai.features.characters.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.bodyFont

@Composable
fun CharacterStats(
    character: Character,
    genre: Genre,
) {
    Row(modifier = Modifier.padding(16.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp),
        ) {
            Text(
                "${character.details.height}cm",
                style =
                    MaterialTheme.typography.titleSmall.copy(
                        fontFamily = genre.bodyFont(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                    ),
            )

            Text(
                "Altura",
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontFamily = genre.bodyFont(),
                        textAlign = TextAlign.Center,
                    ),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp),
        ) {
            Text(
                character.details.race,
                style =
                    MaterialTheme.typography.titleSmall.copy(
                        fontFamily = genre.bodyFont(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                    ),
                maxLines = 1

            )

            Text(
                "Raça",
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontFamily = genre.bodyFont(),
                        textAlign = TextAlign.Center,
                    ),
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp),
        ) {
            Text(
                "${character.details.weight}kg",
                style =
                    MaterialTheme.typography.titleSmall.copy(
                        fontFamily = genre.bodyFont(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                    ),
            )
            Text(
                "Peso",
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontFamily = genre.bodyFont(),
                        textAlign = TextAlign.Center,
                    ),
            )
        }
    }
}
