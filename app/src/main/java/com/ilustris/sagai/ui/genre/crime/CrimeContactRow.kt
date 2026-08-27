package com.ilustris.sagai.ui.genre.crime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.newsaga.data.model.Genre

/** A single expanded cast row: avatar + name + trailing detail, tinted by the bubble's own ink. */
@Composable
fun CrimeContactRow(
    character: Character,
    genre: Genre,
    subtitle: String,
    ink: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CharacterAvatar(
            character,
            genre = genre,
            modifier = Modifier.size(36.dp),
        )

        Text(
            text = "${character.name} ${character.lastName.orEmpty()}".trim(),
            color = ink,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        Text(
            text = subtitle,
            fontStyle = FontStyle.Italic,
            color = ink.copy(alpha = 0.6f),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
