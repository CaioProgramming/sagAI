package com.ilustris.sagai.features.stories.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.hasMoreThanOneChapter
import com.ilustris.sagai.features.timeline.ui.AvatarTimelineIcon
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFill

@Composable
fun StoriesRow(
    sagas: List<SagaContent>,
    loadingStoryId: Int?,
    onStoryClicked: (SagaContent) -> Unit,
    isAtTop: Boolean,
) {
    val eligibleSagas = sagas.filter { it.data.isEnded.not() && it.hasMoreThanOneChapter() }
    if (eligibleSagas.isNotEmpty()) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(eligibleSagas) { saga ->
                    StoryItem(
                        saga = saga,
                        isLoading = loadingStoryId == saga.data.id,
                        onStoryClicked = onStoryClicked,
                        expanded = isAtTop,
                    )
                }
            }
        }
    }
}

@Composable
fun StoryItem(
    saga: SagaContent,
    isLoading: Boolean,
    onStoryClicked: (SagaContent) -> Unit,
    expanded: Boolean,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .animateContentSize()
            .padding(4.dp)
    ) {
        val iconSize by animateDpAsState(
            targetValue = if (expanded) 80.dp else 60.dp,
            animationSpec = tween(600, easing = EaseIn),
            label = "icon_size_animation"
        )
        Box(
            contentAlignment = Alignment.Center, modifier = Modifier
                .size(iconSize)
                .clip(CircleShape)
                .padding(4.dp)
        ) {


            this@Column.AnimatedVisibility(
                visible = isLoading, modifier = Modifier
                    .fillMaxSize()
            ) {
                StoryLoadingIndicator(
                    brush = saga.data.genre.gradient(),
                    modifier = Modifier
                        .clipToBounds()
                        .fillMaxSize()
                        .gradientFill(saga.data.genre.gradient(true, targetValue = 100f))
                )
            }



            AvatarTimelineIcon(
                saga.data.icon,
                false,
                saga.data.genre,
                saga.data.title
                    .first()
                    .uppercase(),
                borderWidth = 1.dp,
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxSize()
                    .clip(CircleShape)
                    .clickable(enabled = !isLoading) { onStoryClicked(saga) },
            )


        }
        AnimatedVisibility(expanded) {
            Text(
                text = saga.data.title,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = saga.data.genre.bodyFont(),
                fontWeight = FontWeight.Light,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

    }
}