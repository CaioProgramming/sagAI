package com.ilustris.sagai.features.characters.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.newsaga.data.model.Genre

@Composable
fun CharacterStats(
    character: Character,
    genre: Genre,
    contentColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
) {
    val traits = character.details.physicalTraits
    val stats =
        buildList {
            if (traits.gender.isNotBlank()) {
                add(traits.gender to "Gênero")
            }
            if (traits.height > 0) {
                add("${traits.height.toInt()}cm" to "Altura")
            }
            if (traits.weight > 0) {
                add("${traits.weight.toInt()}kg" to "Peso")
            }
            if (traits.race.isNotBlank()) {
                add(traits.race to "Raça")
            }
            if (traits.ethnicity.isNotBlank()) {
                add(traits.ethnicity to "Etnia")
            }
        }

    if (stats.isEmpty()) return

    LazyRow(modifier = Modifier.padding(16.dp)) {
        stats.forEach { (value, label) ->
            item {
                VerticalLabel(value, label, genre, contentColor)
            }
        }
    }
}

@Composable
fun VerticalLabel(
    value: String,
    label: String,
    genre: Genre,
    contentColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp),
    ) {
        Text(
            value,
            style =
                MaterialTheme.typography.titleSmall.copy(
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                ),
        )

        Text(
            label,
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    textAlign = TextAlign.Center,
                    color = contentColor,
                ),
            modifier = Modifier.alpha(.4f),
        )
    }
}
