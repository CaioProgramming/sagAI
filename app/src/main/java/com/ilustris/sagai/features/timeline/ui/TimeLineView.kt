package com.ilustris.sagai.features.timeline.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.formatDate
import com.ilustris.sagai.features.act.ui.toRoman
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.events.data.model.CharacterEventDetails
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.chapterNumber
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.features.saga.chat.ui.CharactersTopIcons
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.features.timeline.presentation.TimelineViewModel
import com.ilustris.sagai.ui.components.EmotionalCard
import com.ilustris.sagai.ui.theme.GradientType
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.SparkLoader
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.shape
import effectForGenre
import java.util.Calendar
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun TimelineView(
    sagaId: String,
    navHostController: NavHostController,
    viewModel: TimelineViewModel = hiltViewModel(),
) {
    val saga by viewModel.saga.collectAsStateWithLifecycle()

    TimelineContentView(saga) {
        navHostController.popBackStack()
    }

    LaunchedEffect(saga) {
        if (saga == null) {
            viewModel.getSaga(sagaId)
        }
    }
}

@Composable
fun TimelineContentView(
    sagaContent: SagaContent?,
    onBack: () -> Unit = {},
) {
    AnimatedContent(sagaContent) {
        when (it) {
            null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SparkLoader(
                        gradientAnimation(holographicGradient),
                        2.dp,
                        modifier =
                            Modifier
                                .padding(12.dp)
                                .size(32.dp),
                    )
                    Text("Looking for timeline data...")

                    Button(
                        onClick = { onBack() },
                        modifier =
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.textButtonColors(),
                    ) {
                        Text("Voltar", textAlign = TextAlign.Center)
                    }
                }
            }

            else -> TimeLineContent(it)
        }
    }
}

@Composable
fun TimeLineContent(
    content: SagaContent,
    generateEmotionalReview: (TimelineContent) -> Unit = {},
    openCharacters: () -> Unit = {},
) {
    val lazyListState = rememberLazyListState()
    LazyColumn(state = lazyListState, modifier = Modifier.padding(bottom = 32.dp)) {
        val acts = content.acts
        acts.forEach { actContent ->
            stickyHeader {
                Text(
                    actContent.data.title.ifEmpty { "Ato ${(acts.indexOf(actContent) + 1).toRoman()}" },
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontFamily = content.data.genre.headerFont(),
                            brush = content.data.genre.gradient(true),
                            textAlign = TextAlign.Center,
                        ),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.background,
                            ).padding(16.dp),
                )
            }
            actContent.chapters.forEach { chapter ->
                stickyHeader {
                    Text(
                        chapter.data.title.ifEmpty {
                            "Capítulo ${content.chapterNumber(chapter.data).toRoman()}"
                        },
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontFamily = content.data.genre.headerFont(),
                                color = content.data.genre.color,
                                textAlign = TextAlign.Center,
                            ),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.background,
                                ).padding(16.dp),
                    )
                }

                items(chapter.events.filter { it.isComplete() }) {
                    TimeLineCard(
                        it,
                        content,
                        showSpark = true,
                        isLast = false,
                        openCharacters = { openCharacters.invoke() },
                        modifier =
                            Modifier
                                .animateContentSize()
                                .clip(content.data.genre.shape())
                                .clickable(it.data.emotionalReview.isNullOrEmpty()) {
                                    generateEmotionalReview(it)
                                },
                    )
                }
            }
        }
    }

    LaunchedEffect(content) {
        val lastIndex = lazyListState.layoutInfo.totalItemsCount
        lazyListState.animateScrollToItem(lastIndex)
    }
}

@Composable
fun TimeLineCard(
    eventContent: TimelineContent,
    saga: SagaContent,
    isLast: Boolean = false,
    showText: Boolean = true,
    showSpark: Boolean = true,
    modifier: Modifier = Modifier,
    openCharacters: () -> Unit = {},
) {
    val genre = saga.data.genre
    val event = eventContent.data
    val textColor by animateColorAsState(
        MaterialTheme.colorScheme.onBackground,
        tween(
            easing = EaseIn,
            durationMillis = 1.seconds.toInt(DurationUnit.MILLISECONDS),
            delayMillis = 2.seconds.toInt(DurationUnit.MILLISECONDS),
        ),
    )
    var showCharacterEvents by remember { mutableStateOf(false) }

    ConstraintLayout(
        modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        val (iconView, contentView, emotionalView) = createRefs()

        Column(
            modifier =
                Modifier.constrainAs(iconView) {
                    top.linkTo(parent.top)
                    bottom.linkTo(emotionalView.top)
                    start.linkTo(parent.start)
                    height = Dimension.fillToConstraints
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AvatarTimelineIcon(
                saga.data.icon,
                showSpark,
                genre,
                Modifier
                    .size(50.dp)
                    .border(2.dp, genre.color, CircleShape),
            )

            if (isLast.not()) {
                Box(
                    modifier =
                        Modifier
                            .padding(vertical = 8.dp)
                            .width(2.dp)
                            .weight(1f)
                            .background(genre.color, genre.shape()),
                )
            }
        }

        Column(
            modifier =
                Modifier
                    .padding(horizontal = 8.dp)
                    .constrainAs(contentView) {
                        top.linkTo(parent.top)
                        start.linkTo(iconView.end)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    },
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    event.title,
                    style =
                        MaterialTheme.typography.titleSmall.copy(
                            fontFamily = genre.bodyFont(),
                            color = genre.color,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    event.createdAt.formatDate(),
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            color = textColor.copy(alpha = .4f),
                            fontWeight = FontWeight.Light,
                            fontFamily = genre.bodyFont(),
                            textAlign = TextAlign.End,
                        ),
                    modifier = Modifier,
                )
            }

            AnimatedVisibility(showText) {
                Text(
                    if (showText) event.content else emptyString(),
                    modifier =
                        Modifier.padding(8.dp),
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = genre.bodyFont(),
                            color = textColor,
                            textAlign = TextAlign.Start,
                        ),
                )
            }

            Spacer(Modifier.height(32.dp))

            AnimatedVisibility(showCharacterEvents) {
                Column {
                    eventContent.characterEventDetails.forEach {
                        TimeLineCard(
                            it,
                            genre,
                            showText = true,
                            showReference = false,
                            showSpark = it.character.id == saga.mainCharacter?.data?.id,
                            modifier = Modifier.padding(top = 8.dp),
                            onSelectCharacter = {
                                openCharacters.invoke()
                            },
                        )

                        val isLast = eventContent.characterEventDetails.last() == it

                        if (isLast.not()) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = .1f), genre.shape()),
                            )
                        }
                    }
                }
            }
        }

        Column(
            Modifier
                .padding(horizontal = 12.dp)
                .constrainAs(emotionalView) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(contentView.bottom)
                    width = Dimension.fillToConstraints
                },
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CharactersTopIcons(
                eventContent.characterEventDetails.map { it.character },
                genre,
                false,
            ) { character ->
                showCharacterEvents = showCharacterEvents.not()
            }

            AnimatedVisibility(event.emotionalReview.isNullOrEmpty().not(), modifier = Modifier.padding(vertical = 12.dp)) {
                EmotionalCard(
                    event.emotionalReview,
                    genre,
                    isExpanded = false,
                )
            }
        }
    }
}

@Composable
private fun AvatarTimelineIcon(
    icon: String,
    showSpark: Boolean,
    genre: Genre,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        AsyncImage(
            model = icon,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .clip(CircleShape)
                    .fillMaxSize()
                    .effectForGenre(genre),
        )

        if (showSpark) {
            Image(
                painterResource(R.drawable.ic_spark),
                contentDescription = null,
                colorFilter =
                    ColorFilter.tint(
                        genre.color,
                    ),
                modifier =
                    Modifier
                        .offset(y = 14.dp, x = 0.dp)
                        .size(24.dp)
                        .align(
                            Alignment.BottomCenter,
                        ),
            )
        }
    }
}

@Composable
fun TimeLineCard(
    eventDetails: CharacterEventDetails,
    genre: Genre,
    showText: Boolean = true,
    showSpark: Boolean = true,
    showReference: Boolean = true,
    showIndicator: Boolean = false,
    isLast: Boolean = false,
    modifier: Modifier = Modifier,
    onSelectCharacter: (Character) -> Unit = {},
    onSelectReference: (Timeline) -> Unit = {},
) {
    val event = eventDetails.event

    val textColor by animateColorAsState(
        MaterialTheme.colorScheme.onBackground,
        tween(
            easing = EaseIn,
            durationMillis = 1.seconds.toInt(DurationUnit.MILLISECONDS),
            delayMillis = 2.seconds.toInt(DurationUnit.MILLISECONDS),
        ),
    )
    Column {
        ConstraintLayout(
            modifier
                .fillMaxWidth(),
        ) {
            val (iconView, contentView) = createRefs()

            Column(
                modifier =
                    Modifier.constrainAs(iconView) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        height = Dimension.fillToConstraints
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AvatarTimelineIcon(
                    eventDetails.character.image,
                    showSpark,
                    genre,
                    Modifier
                        .size(50.dp)
                        .border(2.dp, genre.color, CircleShape)
                        .clickable {
                            onSelectCharacter(eventDetails.character)
                        },
                )

                if (showIndicator && isLast.not()) {
                    Box(
                        modifier =
                            Modifier
                                .width(2.dp)
                                .weight(1f)
                                .background(genre.color, genre.shape()),
                    )
                }
            }

            Column(
                modifier =
                    Modifier
                        .padding(16.dp)
                        .constrainAs(contentView) {
                            top.linkTo(parent.top)
                            start.linkTo(iconView.end)
                            end.linkTo(parent.end)
                            width = Dimension.fillToConstraints
                        },
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        eventDetails.character.name,
                        style =
                            MaterialTheme.typography.titleSmall.copy(
                                fontFamily = genre.bodyFont(),
                                color = genre.color,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Start,
                            ),
                        modifier = Modifier.weight(1f),
                    )

                    Text(
                        event.createdAt.formatDate(),
                        style =
                            MaterialTheme.typography.labelSmall.copy(
                                color = textColor.copy(alpha = .4f),
                                fontWeight = FontWeight.Light,
                                fontFamily = genre.bodyFont(),
                                textAlign = TextAlign.End,
                            ),
                        modifier = Modifier,
                    )
                }

                if (showReference) {
                    Row(
                        modifier =
                            Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth()
                                .clip(genre.shape())
                                .clickable {
                                    eventDetails.timeline?.let {
                                        onSelectReference(it)
                                    }
                                }.alpha(.4f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Image(
                            painterResource(R.drawable.ic_spark),
                            null,
                            colorFilter = ColorFilter.tint(genre.color),
                            modifier = Modifier.size(12.dp),
                        )

                        Text(
                            eventDetails.timeline?.title ?: emptyString(),
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    color = genre.color,
                                    fontFamily = genre.bodyFont(),
                                    textAlign = TextAlign.Start,
                                ),
                            maxLines = 1,
                        )
                    }
                }

                Text(
                    event.title,
                    style =
                        MaterialTheme.typography.titleSmall.copy(
                            fontFamily = genre.bodyFont(),
                            fontWeight = FontWeight.SemiBold,
                        ),
                )
                if (showText) {
                    Text(
                        event.summary,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = genre.bodyFont(),
                                color = textColor,
                                textAlign = TextAlign.Justify,
                            ),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TimeLineContentPreview() {
    val content =
        SagaContent(
            data =
                Saga(
                    title = "My Awesome Saga",
                    description = "A saga about adventure and stuff.",
                    genre = Genre.FANTASY,
                ),
        )
    TimeLineContent(content)
}

@Preview(showBackground = true)
@Composable
fun TimeLineCardPreview() {
    val saga =
        SagaContent(
            data =
                Saga(
                    title = "My Awesome Saga",
                    description = "A saga about adventure and stuff.",
                    genre = Genre.entries.random(),
                ),
        )
    val event =
        TimelineContent(
            Timeline(
                title = "The Great Battle",
                content = "A fierce battle took place, changing the course of history.",
                createdAt = Calendar.getInstance().timeInMillis,
                chapterId = 0,
                emotionalReview = "This was a great event!",
            ),
        )
    TimeLineCard(
        event,
        saga,
    )
}
