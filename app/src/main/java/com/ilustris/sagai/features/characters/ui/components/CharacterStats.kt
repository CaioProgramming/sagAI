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
        VerticalLabel("${character.details.height}cm", "Altura", genre)
        VerticalLabel(character.details.race, "Raça", genre)
        VerticalLabel("${character.details.weight}kg", "Peso", genre)
    }
}

@Composable
fun VerticalLabel(
    value: String,
    label: String,
    genre: Genre,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp),
    ) {
        Text(
            value,
            style =
                MaterialTheme.typography.titleSmall.copy(
                    fontFamily = genre.bodyFont(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                ),
        )

        Text(
            label,
            style =
                MaterialTheme.typography.bodySmall.copy(
                    fontFamily = genre.bodyFont(),
                    textAlign = TextAlign.Center,
                ),
        )
    }
}
