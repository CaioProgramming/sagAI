package com.ilustris.sagai.features.stories.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
fun StoriesRow(sagas: List<SagaContent>, onStoryClicked: (SagaContent) -> Unit) {
    val eligibleSagas = sagas.filter { it.data.isEnded.not() && it.hasMoreThanOneChapter() }
    if (eligibleSagas.isNotEmpty()) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = "Stories",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(eligibleSagas) { saga ->
                    StoryItem(saga = saga, onStoryClicked = onStoryClicked)
                }
            }
        }
    }
}

@Composable
fun StoryItem(
    saga: SagaContent,
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
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    brush = saga.data.genre.gradient(),
                    shape = CircleShape,
                )
                .clickable { onStoryClicked(saga) },
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