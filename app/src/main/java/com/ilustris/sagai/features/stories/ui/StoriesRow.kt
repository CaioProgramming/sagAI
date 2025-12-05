package com.ilustris.sagai.features.stories.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.hasMoreThanOneChapter
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.gradient

@Composable
fun StoriesRow(
    sagas: List<SagaContent>,
    loadingStoryId: Int?,
    onStoryClicked: (SagaContent) -> Unit,
) {
    val eligibleSagas = sagas.filter { it.data.isEnded.not() && it.hasMoreThanOneChapter() }
    if (eligibleSagas.isNotEmpty()) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(eligibleSagas) { saga ->
                    StoryItem(
                        saga = saga,
                        isLoading = loadingStoryId == saga.data.id,
                        onStoryClicked = onStoryClicked,
                    )
                }
            }

            HorizontalDivider(
                modifier =
                Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth(),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .1f),
            )
        }
    }
}

@Composable
fun StoryItem(
    saga: SagaContent,
    isLoading: Boolean,
    onStoryClicked: (SagaContent) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.width(64.dp),
    ) {
        Box(
            modifier =
            Modifier
                .size(64.dp)
                .clickable(enabled = !isLoading) { onStoryClicked(saga) },
        ) {
            Box(
                modifier =
                Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            ) {
                saga.mainCharacter?.let {
                    CharacterAvatar(
                        character = it.data,
                        genre = saga.data.genre,
                        modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                    )
                }
                AnimatedVisibility(visible = isLoading, modifier = Modifier.fillMaxSize()) {
                    StoryLoadingIndicator(brush = saga.data.genre.gradient())
                }
            }
        }
        Text(
            text = saga.data.title,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = saga.data.genre.bodyFont(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}