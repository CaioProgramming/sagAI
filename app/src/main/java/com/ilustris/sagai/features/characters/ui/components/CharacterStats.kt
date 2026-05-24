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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
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
                add(traits.gender to R.string.character_form_title_gender)
            }
            if (traits.height > 0) {
                add("${traits.height.toInt()}cm" to R.string.character_stat_height)
            }
            if (traits.weight > 0) {
                add("${traits.weight.toInt()}kg" to R.string.character_stat_weight)
            }
            if (traits.race.isNotBlank()) {
                add(traits.race to R.string.character_form_label_race)
            }
            if (traits.ethnicity.isNotBlank()) {
                add(traits.ethnicity to R.string.character_form_label_ethnicity)
            }
        }

    if (stats.isEmpty()) return

    LazyRow(modifier = Modifier.padding(16.dp)) {
        stats.forEach { (value, labelRes) ->
            item {
                VerticalLabel(
                    value = value,
                    label = stringResource(labelRes),
                    genre = genre,
                    contentColor = contentColor,
                )
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
