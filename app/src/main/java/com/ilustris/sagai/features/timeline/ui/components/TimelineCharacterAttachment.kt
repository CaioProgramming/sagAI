package com.ilustris.sagai.features.timeline.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.characters.events.data.model.CharacterEventDetails
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.home.data.model.SagaInfo
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.shape

@Composable
fun TimelineCharacterAttachment(
    eventDetails: CharacterEventDetails,
    sagaContent: SagaInfo,
    showIndicator: Boolean = false,
    showSpark: Boolean = false,
    isLast: Boolean = false,
    onSelectReference: (Timeline?) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val genre = sagaContent.genre
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(genre.shape())
                .clickable { onSelectReference(eventDetails.timeline) }
                .padding(vertical = 12.dp, horizontal = 16.dp),
    ) {
        CharacterAvatar(
            eventDetails.character,
            genre = genre,
            modifier = Modifier.size(40.dp),
        )

        Column(
            modifier =
                Modifier
                    .padding(start = 12.dp)
                    .fillMaxWidth(),
        ) {
            Text(
                text = eventDetails.character.name,
                style =
                    MaterialTheme.typography.labelLarge.copy(
                        fontFamily = genre.bodyFont(),
                        fontWeight = FontWeight.Bold,
                    ),
            )

            Text(
                text = eventDetails.event.summary,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = genre.bodyFont(),
                    ),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
