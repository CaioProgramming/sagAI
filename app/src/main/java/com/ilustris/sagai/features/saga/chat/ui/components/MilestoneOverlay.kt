package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInSine
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.model.CharacterProfile
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.shimmerColors
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.reactiveShimmer

@Composable
fun MilestoneOverlay(
    milestone: SagaMilestone,
    saga: SagaContent,
    onDismiss: () -> Unit,
) {
    val genre = saga.data.genre
    genre.colorPalette()

    AnimatedContent(milestone, transitionSpec = {
        slideInVertically(tween(500, easing = EaseInSine)) { -it } +
            fadeIn(tween(600)) togetherWith fadeOut(tween(600))
    }) {
        Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painterResource(R.drawable.ic_spark),
                    null,
                    Modifier
                        .size(24.dp)
                        .gradientFill(genre.gradient(true)),
                )

                Text(
                    stringResource(it.title),
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontFamily = genre.headerFont(),
                            color = MaterialTheme.colorScheme.onBackground,
                        ),
                )

                if (milestone is SagaMilestone.NewCharacter) {
                    if (milestone.character.image.isNotEmpty()) {
                        CharacterAvatar(
                            milestone.character,
                            genre = genre,
                            modifier = Modifier.size(50.dp),
                        )
                    }
                }

                Text(
                    milestone.subtitle,
                    style =
                        MaterialTheme.typography.displaySmall.copy(
                            fontFamily = genre.headerFont(),
                            color = MaterialTheme.colorScheme.onBackground,
                            shadow = Shadow(genre.color),
                            textAlign = TextAlign.Center,
                        ),
                    modifier = Modifier.reactiveShimmer(true, genre.shimmerColors()),
                )
            }
        }
    }
}

@Preview
@Composable
private fun NewEventPreview() {
    val genre = Genre.PUNK_ROCK
    val sagaContent = SagaContent(Saga(genre = genre))
    MilestoneOverlay(
        milestone = SagaMilestone.NewEvent(Timeline(title = "the breakdown", chapterId = 0)),
        sagaContent,
        onDismiss = {},
    )
}

@Preview
@Composable
private fun NewChapterPreview() {
    val genre = Genre.CYBERPUNK
    val sagaContent = SagaContent(Saga(genre = genre))
    MilestoneOverlay(
        milestone =
            SagaMilestone.ChapterFinished(
                Chapter(
                    title = "The Dragon's Awakening",
                    actId = 0,
                ),
            ),
        saga = sagaContent,
        onDismiss = {},
    )
}

@Preview
@Composable
private fun NewActPreview() {
    val genre = Genre.FANTASY
    val sagaContent = SagaContent(Saga(genre = genre))
    SagAIScaffold {
        MilestoneOverlay(
            milestone = SagaMilestone.ActFinished(Act(title = "The dragon fate")),
            saga = sagaContent,
            onDismiss = {},
        )
    }
}

@Preview
@Composable
private fun NewCharacterPreview() {
    val genre = Genre.HORROR
    val sagaContent = SagaContent(Saga(genre = genre))
    CharacterContent(
        Character(
            name = "The Nameless One",
            details = Details(),
            profile = CharacterProfile(),
        ),
    )
    SagAIScaffold {
        MilestoneOverlay(
            milestone =
                SagaMilestone.NewCharacter(
                    Character(
                        name = "Alex",
                        profile = CharacterProfile(),
                        details = Details(),
                    ),
                ),
            saga = sagaContent,
            onDismiss = {},
        )
    }
}
