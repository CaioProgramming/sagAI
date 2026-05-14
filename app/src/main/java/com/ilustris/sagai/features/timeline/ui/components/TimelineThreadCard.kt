package com.ilustris.sagai.features.timeline.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaInfo
import com.ilustris.sagai.features.saga.detail.ui.DetailAction
import com.ilustris.sagai.features.timeline.domain.TimelineCardContent
import com.ilustris.sagai.features.timeline.ui.AvatarTimelineIcon
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight

@Composable
fun TimelineThreadCard(
    sagaInfo: SagaInfo,
    eventCard: TimelineCardContent,
    onAction: (DetailAction) -> Unit = {},
) {
    val genre = sagaInfo.genre
    val event = eventCard.timelineContent
    val mascotEmotion = eventCard.mascotEmotion

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
    ) {
        // Root Post
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AvatarTimelineIcon(
                    icon = sagaInfo.icon,
                    showSpark = true,
                    genre = genre,
                    placeHolderChar = sagaInfo.title.first().uppercase(),
                    borderWidth = 1.dp,
                    borderColor = MaterialTheme.colorScheme.primary,
                    modifier =
                        Modifier
                            .size(48.dp)
                            .effectForGenre(genre)
                            .selectiveColorHighlight(genre),
                )

                // Thread Line
                Box(
                    modifier =
                        Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .weight(1f)
                            .background(
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            ).padding(vertical = 4.dp),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = event.data.title,
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        ),
                )

                Text(
                    text = event.data.content,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                )

                // Action Bar
                TimelineThreadActionBar(
                    eventCard = eventCard,
                    color = MaterialTheme.colorScheme.primary,
                    genre = genre,
                    onAction = onAction,
                )
            }
        }

        // Community Note (Emotional Review)
        event.data.emotionalReview?.let { review ->
            mascotEmotion?.let { emotion ->
                TimelineCommunityNote(
                    emotionalReview = review,
                    mascotEmotion = emotion,
                    genre = genre,
                    modifier = Modifier.padding(start = 60.dp), // Indented to align with content
                )
            }
        }

        // Threaded Replies (Character Events)
        event.characterEventDetails.forEach { detail ->
            TimelineReplyItem(
                avatarUrl = detail.character.image,
                title = detail.character.name,
                content = detail.event.summary,
                genre = genre,
                modifier = Modifier.padding(start = 60.dp),
            )
        }

        // Threaded Replies (Wikis)
        event.updatedWikis.forEach { wiki ->
            TimelineReplyItem(
                icon = R.drawable.ic_note,
                title = wiki.title,
                content = wiki.content.take(100) + "...",
                genre = genre,
                modifier = Modifier.padding(start = 60.dp),
                onClick = { onAction(DetailAction.ViewWiki) },
            )
        }
    }
}

@Composable
fun TimelineThreadActionBar(
    eventCard: TimelineCardContent,
    color: Color,
    genre: com.ilustris.sagai.features.newsaga.data.model.Genre,
    onAction: (DetailAction) -> Unit,
) {
    val event = eventCard.timelineContent
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TimelineActionIcon(
            icon = R.drawable.ic_eye_mask,
            count = event.newlyAppearedCharacters.size + event.characterEventDetails.size,
            color = color,
            genre = genre,
        )

        TimelineActionIcon(
            icon = R.drawable.ic_note,
            count = event.updatedWikis.size,
            color = color,
            genre = genre,
        )

        TimelineActionIcon(
            icon = R.drawable.ic_relationship,
            count = event.updatedRelationshipDetails.size,
            color = color,
            genre = genre,
        )
    }
}

@Composable
fun TimelineActionIcon(
    icon: Int,
    count: Int,
    color: Color,
    genre: com.ilustris.sagai.features.newsaga.data.model.Genre,
) {
    if (count == 0) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = color.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = count.toString(),
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                ),
        )
    }
}

@Composable
fun TimelineReplyItem(
    avatarUrl: String? = null,
    icon: Int? = null,
    title: String,
    content: String,
    genre: com.ilustris.sagai.features.newsaga.data.model.Genre,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { onClick() },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Line continuation
        Box(
            modifier =
                Modifier
                    .width(2.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (avatarUrl != null) {
                    AvatarTimelineIcon(
                        icon = avatarUrl,
                        genre = genre,
                        modifier = Modifier.size(24.dp),
                    )
                } else if (icon != null) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Text(
                    text = title,
                    style =
                        MaterialTheme.typography.labelLarge.copy(
                            fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        ),
                )
            }

            Text(
                text = content,
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    ),
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
