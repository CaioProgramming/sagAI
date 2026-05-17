package com.ilustris.sagai.features.timeline.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.characters.events.data.model.CharacterEventDetails
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.sagaShape

@Composable
fun CharacterEventCard(
    genre: Genre,
    characterEvent: CharacterEventDetails,
    modifier: Modifier = Modifier,
) {
    val character = remember { characterEvent.character }
    val shape = sagaShape()
    Column(
        modifier
            .clip(shape)
            .border(1.dp, MaterialTheme.colorScheme.primary, shape)
            .padding(4.dp)
            .background(
                MaterialTheme.colorScheme.surfaceContainer,
                shape,
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CharacterAvatar(
                character,
                genre = genre,
                borderColor = character.hexColor.hexToColor() ?: MaterialTheme.colorScheme.primary,
                useFallback = true,
                borderSize = 1.dp,
                modifier = Modifier.size(48.dp),
            )

            Text(
                characterEvent.event.title,
                modifier = Modifier.weight(1f),
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        fontWeight = FontWeight.SemiBold,
                    ),
            )
        }

        Text(
            characterEvent.event.summary,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                ),
            modifier = Modifier.alpha(.7f),
        )
    }
}
