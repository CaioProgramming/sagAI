package com.ilustris.sagai.features.timeline.ui.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.ui.theme.components.mascot.MascotEmotionFace
import com.ilustris.sagai.ui.theme.sagaShape

@Composable
fun TimelineCommunityNote(
    emotionalReview: String,
    mascotEmotion: Pair<EmotionalTone, String?>,
    genre: Genre,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(sagaShape())
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, sagaShape())
                .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            mascotEmotion.second?.let {
                MascotEmotionFace(
                    imageUrl = it,
                    emotionalTone = mascotEmotion.first,
                    modifier = Modifier.size(24.dp),
                    animate = false,
                )
            }
            Text(
                text = stringResource(id = R.string.mascot_insight_label), // Needs to be added to strings.xml or used as literal
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
            )
        }

        Text(
            text = emotionalReview,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                ),
        )
    }
}
