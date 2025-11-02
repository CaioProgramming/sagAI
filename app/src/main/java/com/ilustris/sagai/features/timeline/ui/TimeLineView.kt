@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.ilustris.sagai.features.timeline.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInBounce
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.formatDate
import com.ilustris.sagai.features.act.ui.toRoman
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.events.data.model.CharacterEventDetails
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipContent
import com.ilustris.sagai.features.characters.relations.ui.RelationShipCard
import com.ilustris.sagai.features.characters.ui.CharacterHorizontalView
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.chapterNumber
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.shimmerColors
import com.ilustris.sagai.features.saga.chat.ui.CharactersTopIcons
import com.ilustris.sagai.features.saga.detail.ui.DetailAction
import com.ilustris.sagai.features.saga.detail.ui.sharedTransitionActionItemModifier
import com.ilustris.sagai.features.saga.detail.ui.titleAndSubtitle
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.ui.WikiCard
import com.ilustris.sagai.ui.components.EmotionalCard
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.LargeHorizontalHeader
import com.ilustris.sagai.ui.theme.components.SagaTopBar
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.shape
import com.ilustris.sagai.ui.theme.solidGradient
import effectForGenre
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun TimeLineContent(
    saga: SagaContent,
    animationScopes: Pair<SharedTransitionScope, AnimatedContentScope>,
    generateEmotionalReview: (TimelineContent) -> Unit = {},
    openCharacters: () -> Unit = {},
    onBackClick: () -> Unit = {},
    titleModifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    val genre = saga.data.genre
    val topPadding by animateDpAsState(
        if (lazyListState.canScrollBackward) 100.dp else 0.dp,
    )
    with(animationScopes.first) {
        Box {
            val titleAndSubtitle =
                DetailAction.TIMELINE.titleAndSubtitle(saga)
            LazyColumn(
                state = lazyListState,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(top = topPadding, bottom = 32.dp),
            ) {
                item {
                    LargeHorizontalHeader(
                        titleAndSubtitle.first,
                        titleAndSubtitle.second,
                        titleStyle =
                            MaterialTheme.typography.displaySmall.copy(
                                fontFamily = genre.headerFont(),
                            ),
                        subtitleStyle =
                            MaterialTheme.typography.labelMedium.copy(
                                fontFamily = genre.bodyFont(),
                            ),
                        modifier =
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                        titleModifier = titleModifier,
                    )
                }
                val acts = saga.acts
                acts.forEach { actContent ->
                    item {
                        Text(
                            actContent.data.title.ifEmpty { "Ato ${(acts.indexOf(actContent) + 1).toRoman()}" },
                            style =
                                MaterialTheme.typography.displaySmall.copy(
                                    fontFamily = genre.headerFont(),
                                    brush = genre.gradient(true),
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
                                    "Capítulo ${saga.chapterNumber(chapter.data).toRoman()}"
                                },
                                style =
                                    MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = genre.headerFont(),
                                        color = saga.data.genre.color,
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
                            val eventModifier =
                                this@with.sharedTransitionActionItemModifier(
                                    DetailAction.CHARACTERS,
                                    animationScopes.second,
                                    it.data.id,
                                    saga.data.id,
                                )
                            val cardEnabled =
                                remember {
                                    it.data.emotionalReview.isNullOrEmpty() ||
                                        it.characterEventDetails.isEmpty() ||
                                        it.updatedRelationshipDetails.isEmpty() ||
                                        it.updatedWikis.isEmpty()
                                }
                            TimeLineCard(
                                it,
                                saga,
                                showSpark = true,
                                isLast = false,
                                openCharacters = { openCharacters.invoke() },
                                modifier =
                                    eventModifier
                                        .animateContentSize()
                                        .clip(genre.shape())
                                        .clickable(cardEnabled) {
                                            generateEmotionalReview(it)
                                        },
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                lazyListState.canScrollBackward,
                enter = fadeIn(tween(400, delayMillis = 200)),
                exit = fadeOut(tween(200)),
            ) {
                SagaTopBar(
                    titleAndSubtitle.first,
                    titleAndSubtitle.second,
                    genre,
                    onBackClick = { onBackClick() },
                    actionContent = { Box(Modifier.size(24.dp)) },
                    modifier =
                        Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .fillMaxWidth()
                            .padding(top = 50.dp, start = 16.dp),
                )
            }
        }
    }

    LaunchedEffect(saga) {
        delay(500)
        val lastIndex = lazyListState.layoutInfo.totalItemsCount
        lazyListState.animateScrollToItem(lastIndex)
    }
}

sealed interface TimelineSheet {
    data class WikiSheet(
        val items: List<Wiki>,
    ) : TimelineSheet

    data class CharacterSheet(
        val items: List<Character>,
    ) : TimelineSheet

    data class RelationSheet(
        val relations: List<RelationshipContent>,
    ) : TimelineSheet
}

@Composable
fun TimelineSheet.sheetTitle() =
    when (this) {
        is TimelineSheet.CharacterSheet -> stringResource(R.string.saga_detail_section_title_characters)
        is TimelineSheet.RelationSheet -> stringResource(R.string.saga_detail_relationships_section_title)
        is TimelineSheet.WikiSheet -> stringResource(R.string.saga_detail_section_title_wiki)
    }

@OptIn(ExperimentalMaterial3Api::class)
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

    var timelineSheet by remember { mutableStateOf<TimelineSheet?>(null) }
    var showCharacterEvents by remember { mutableStateOf(false) }

    ConstraintLayout(
        modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        val (iconView, contentView, emotionalView) = createRefs()

        Column(
            modifier =
                Modifier
                    .padding(horizontal = 8.dp)
                    .constrainAs(iconView) {
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
                saga.data.title
                    .first()
                    .uppercase(),
                modifier =
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
            Text(
                event.title,
                style =
                    MaterialTheme.typography.titleSmall.copy(
                        fontFamily = genre.bodyFont(),
                        color = genre.color,
                        fontWeight = FontWeight.SemiBold,
                    ),
                modifier = Modifier.fillMaxWidth(),
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
            )

            AnimatedVisibility(showText) {
                Column {
                    Text(
                        if (showText) event.content else emptyString(),
                        modifier =
                            Modifier.padding(vertical = 8.dp),
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = genre.bodyFont(),
                                color = textColor,
                                textAlign = TextAlign.Start,
                            ),
                    )

                    Row(
                        modifier =
                            Modifier
                                .alpha(.5f)
                                .padding(8.dp)
                                .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        if (eventContent.updatedWikis.isNotEmpty()) {
                            Row(
                                modifier =
                                    Modifier
                                        .padding(horizontal = 8.dp)
                                        .clip(genre.shape())
                                        .clickable {
                                            timelineSheet =
                                                TimelineSheet.WikiSheet(eventContent.updatedWikis)
                                        },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Image(
                                    painterResource(R.drawable.ic_note),
                                    null,
                                    colorFilter = ColorFilter.tint(genre.color),
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(24.dp),
                                )

                                Text(
                                    eventContent.updatedWikis.size.toString(),
                                    style =
                                        MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = genre.bodyFont(),
                                        ),
                                )
                            }
                        }

                        if (eventContent.updatedRelationshipDetails.isNotEmpty()) {
                            Row(
                                Modifier
                                    .padding(horizontal = 8.dp)
                                    .clip(genre.shape())
                                    .clickable {
                                        timelineSheet =
                                            TimelineSheet.RelationSheet(eventContent.updatedRelationshipDetails)
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Image(
                                    painterResource(R.drawable.ic_relationship),
                                    null,
                                    colorFilter = ColorFilter.tint(genre.color),
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(24.dp).padding(4.dp),
                                )

                                Text(
                                    eventContent
                                        .updatedRelationshipDetails
                                        .size
                                        .toString(),
                                    style =
                                        MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = genre.bodyFont(),
                                        ),
                                )
                            }
                        }

                        if (eventContent.newlyAppearedCharacters.isNotEmpty()) {
                            Row(
                                Modifier
                                    .padding(horizontal = 8.dp)
                                    .clip(genre.shape())
                                    .clickable {
                                        timelineSheet =
                                            TimelineSheet.CharacterSheet(eventContent.newlyAppearedCharacters)
                                    },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Image(
                                    painterResource(R.drawable.ic_eye_mask),
                                    null,
                                    colorFilter = ColorFilter.tint(genre.color),
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(24.dp),
                                )

                                Text(
                                    eventContent.newlyAppearedCharacters.size.toString(),
                                    style =
                                        MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = genre.bodyFont(),
                                        ),
                                )
                            }
                        }
                    }
                }
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
                            iconSize = 32.dp,
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
                                    .background(
                                        MaterialTheme.colorScheme.onBackground.copy(alpha = .1f),
                                        genre.shape(),
                                    ),
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

            AnimatedVisibility(
                event.emotionalReview.isNullOrEmpty().not(),
                modifier = Modifier.padding(vertical = 12.dp),
            ) {
                EmotionalCard(
                    event.emotionalReview,
                    genre,
                    isExpanded = false,
                )
            }
        }
    }

    timelineSheet?.let { sheet ->
        ModalBottomSheet(
            onDismissRequest = {
                timelineSheet = null
            },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            LazyColumn {
                stickyHeader {
                    Text(
                        sheet.sheetTitle(),
                        style =
                            MaterialTheme.typography.titleLarge.copy(
                                fontFamily = genre.headerFont(),
                                textAlign = TextAlign.Center,
                            ),
                        modifier =
                            Modifier
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .padding(16.dp)
                                .fillMaxWidth(),
                    )
                }

                when (sheet) {
                    is TimelineSheet.CharacterSheet -> {
                        items(sheet.items) {
                            CharacterHorizontalView(
                                modifier =
                                    Modifier
                                        .padding(vertical = 8.dp)
                                        .fillMaxWidth(),
                                it,
                                genre,
                            )
                        }
                    }

                    is TimelineSheet.RelationSheet -> {
                        items(sheet.relations) {
                            RelationShipCard(saga, it, modifier = Modifier.padding(16.dp))
                        }
                    }

                    is TimelineSheet.WikiSheet -> {
                        items(sheet.items) {
                            WikiCard(
                                it,
                                genre,
                                modifier =
                                    Modifier
                                        .padding(16.dp)
                                        .shadow(3.dp, genre.shape(), spotColor = genre.color)
                                        .background(
                                            MaterialTheme.colorScheme.background,
                                            genre.shape(),
                                        ).fillMaxWidth(),
                                true,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimeLineSimpleCard(
    eventContent: TimelineContent,
    saga: SagaContent,
    showText: Boolean = true,
    modifier: Modifier = Modifier,
    requestReview: (TimelineContent) -> Unit = {},
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

    Column(
        modifier
            .border(2.dp, genre.color.copy(alpha = .3f), genre.shape())
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = .5f), genre.shape())
            .padding(16.dp)
            .animateContentSize(tween(600, easing = EaseInBounce)),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AvatarTimelineIcon(
                saga.data.icon,
                true,
                genre,
                saga.data.title
                    .first()
                    .uppercase(),
                modifier =
                    Modifier
                        .size(32.dp)
                        .border(1.dp, genre.color, CircleShape),
            )

            Column(modifier = Modifier.padding(8.dp).weight(1f)) {
                Text(
                    event.title,
                    style =
                        MaterialTheme.typography.titleSmall.copy(
                            fontFamily = genre.bodyFont(),
                            color = genre.color,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
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
                )
            }
        }

        AnimatedVisibility(showText) {
            Text(
                event.content,
                overflow = TextOverflow.Ellipsis,
                maxLines = 4,
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontFamily = genre.bodyFont(),
                        color = textColor,
                        textAlign = TextAlign.Start,
                    ),
            )
        }

        AnimatedVisibility(eventContent.characterEventDetails.isNotEmpty()) {
            CharactersTopIcons(
                eventContent.characterEventDetails.map { it.character },
                genre,
                false,
            )
        }

        AnimatedVisibility(
            eventContent.canBeReviewed(),
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Row(
                modifier =
                    Modifier
                        .padding(8.dp)
                        .clip(genre.shape())
                        .clickable {
                            requestReview(eventContent)
                        }.gradientFill(genre.gradient())
                        .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painterResource(R.drawable.ic_review),
                    null,
                    tint = genre.color,
                    modifier = Modifier.padding(4.dp).size(24.dp).padding(2.dp),
                )
                Text(
                    text = "Revisar evento",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
fun AvatarTimelineIcon(
    icon: String,
    showSpark: Boolean,
    genre: Genre,
    placeHolderChar: String = "S",
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    modifier: Modifier = Modifier,
) {
    val border =
        borderColor?.solidGradient() ?: Brush.verticalGradient(
            listOf(genre.color, genre.colorPalette().last(), genre.iconColor),
        )
    val background =
        backgroundColor?.gradientFade() ?: genre.color.gradientFade()
    Box(
        modifier
            .border(borderWidth, border, CircleShape)
            .background(
                background,
                CircleShape,
            ),
    ) {
        var textVisible by remember {
            mutableFloatStateOf(0f)
        }

        AsyncImage(
            model = icon,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            onState = {
                textVisible =
                    if (it !is coil3.compose.AsyncImagePainter.State.Success) {
                        1f
                    } else {
                        0f
                    }
            },
            modifier =
                Modifier
                    .clip(CircleShape)
                    .fillMaxSize()
                    .effectForGenre(genre),
        )

        Text(
            placeHolderChar,
            style =
                MaterialTheme.typography.titleSmall.copy(
                    fontFamily = genre.headerFont(),
                    brush =
                        Brush.verticalGradient(
                            genre.color.darkerPalette(factor = .2f),
                        ),
                ),
            modifier = Modifier.alpha(textVisible).align(Alignment.Center),
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
    iconSize: Dp = 50.dp,
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
                    Modifier
                        .padding(horizontal = 8.dp)
                        .constrainAs(iconView) {
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
                    eventDetails.character.name
                        .first()
                        .uppercase(),
                    modifier =
                        Modifier
                            .size(iconSize)
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
                        .padding(horizontal = 16.dp)
                        .constrainAs(contentView) {
                            top.linkTo(iconView.top)
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
                                textAlign = TextAlign.Start,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
fun TimelineCharacterAttachment(
    eventDetails: CharacterEventDetails,
    sagaContent: SagaContent,
    showSpark: Boolean = true,
    showIndicator: Boolean = false,
    isLast: Boolean = false,
    modifier: Modifier = Modifier,
    onSelectCharacter: (Character) -> Unit = {},
    onSelectReference: (Timeline) -> Unit = {},
) {
    val genre = sagaContent.data.genre
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
                    Modifier
                        .padding(8.dp)
                        .constrainAs(iconView) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            height = Dimension.fillToConstraints
                        },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AvatarTimelineIcon(
                    eventDetails.character.image,
                    true,
                    genre,
                    eventDetails.character.name
                        .first()
                        .uppercase(),
                    backgroundColor = eventDetails.character.hexColor.hexToColor(),
                    modifier =
                        Modifier
                            .size(32.dp)
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
                Row(verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            event.title,
                            maxLines = 1,
                            style =
                                MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = genre.bodyFont(),
                                    fontWeight = FontWeight.SemiBold,
                                    color = genre.color,
                                ),
                        )

                        Text(
                            eventDetails.character.name,
                            style =
                                MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = genre.bodyFont(),
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = .4f),
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Start,
                                ),
                        )
                    }

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

                Text(
                    event.summary,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = genre.bodyFont(),
                            color = textColor,
                            textAlign = TextAlign.Start,
                        ),
                )

                val timelineReference =
                    remember {
                        sagaContent
                            .flatEvents()
                            .find { it.data.id == eventDetails.event.gameTimelineId }
                    }

                timelineReference?.let {
                    TimeLineSimpleCard(
                        it,
                        sagaContent,
                        true,
                        Modifier
                            .clip(genre.shape())
                            .clickable {
                                onSelectReference(it.data)
                            },
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
    SharedTransitionLayout {
        AnimatedContent(content) {
            TimeLineContent(
                it,
                animationScopes = this@SharedTransitionLayout to this,
            )
        }
    }
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
