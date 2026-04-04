@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.timeline.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.DateFormatOption
import com.ilustris.sagai.core.utils.formatDate
import com.ilustris.sagai.features.characters.relations.ui.RelationShipCard
import com.ilustris.sagai.features.characters.ui.CharacterYearbookItem
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.saga.detail.ui.DetailAction
import com.ilustris.sagai.features.timeline.domain.TimelineCardContent
import com.ilustris.sagai.features.timeline.presentation.TimelineAction
import com.ilustris.sagai.features.wiki.ui.WikiCard
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.mascot.MascotEmotionFace
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shape
import com.ilustris.sagai.ui.theme.shimmerize

@Composable
fun TimelineContentViewCard(
    saga: SagaContent,
    eventCard: TimelineCardContent,
    modifier: Modifier = Modifier,
    onAction: (DetailAction) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    val genre = remember { saga.data.genre }
    val event = remember { eventCard.timelineContent }
    val emotionalMascot = remember { eventCard.mascotEmotion }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .border(1.dp, genre.resolveColor())
                .clip(genre.shape())
                .clickable {
                    expanded = true
                },
    ) {
        eventCard.chapterNumber?.let {
            Text(
                it,
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = genre.bodyFont(),
                        color = genre.resolveColor(),
                        textAlign = TextAlign.Center,
                    ),
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }

        Column(
            modifier =
                Modifier
                    .border(1.dp, genre.resolveColor())
                    .clip(genre.shape())
                    .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AvatarTimelineIcon(
                    icon = saga.data.icon,
                    showSpark = true,
                    genre = saga.data.genre,
                    placeHolderChar =
                        saga.data.title
                            .first()
                            .uppercase(),
                    borderWidth = 1.dp,
                    borderColor = genre.resolveColor(),
                    modifier =
                        Modifier
                            .size(32.dp)
                            .effectForGenre(genre)
                            .selectiveColorHighlight(saga.data.genre),
                )

                Text(
                    event.data.title,
                    modifier =
                        Modifier.weight(1f),
                    style =
                        MaterialTheme.typography.titleSmall.copy(
                            fontFamily = genre.bodyFont(),
                            color = genre.resolveColor(),
                        ),
                )
            }

            Text(
                event.data.content,
                modifier =
                    Modifier
                        .padding(horizontal = 16.dp),
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = genre.bodyFont(),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = .8f),
                    ),
            )

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (event.updatedWikis.isNotEmpty()) {
                    Row(
                        modifier =
                            Modifier
                                .padding(horizontal = 8.dp)
                                .clip(genre.shape())
                                .clickable {
                                    expanded = true
                                },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Image(
                            painterResource(R.drawable.ic_note),
                            null,
                            colorFilter = ColorFilter.tint(genre.resolveColor()),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(24.dp),
                        )

                        Text(
                            event.updatedWikis.size.toString(),
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = genre.bodyFont(),
                                ),
                        )
                    }
                }

                if (event.updatedRelationshipDetails.isNotEmpty()) {
                    Row(
                        Modifier
                            .padding(horizontal = 8.dp)
                            .clip(genre.shape())
                            .clickable {
                                expanded = true
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Image(
                            painterResource(R.drawable.ic_relationship),
                            null,
                            colorFilter = ColorFilter.tint(genre.resolveColor()),
                            contentScale = ContentScale.Fit,
                            modifier =
                                Modifier
                                    .size(24.dp)
                                    .padding(4.dp),
                        )

                        Text(
                            event
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

                if (event.newlyAppearedCharacters.isNotEmpty()) {
                    Row(
                        Modifier
                            .padding(horizontal = 8.dp)
                            .clip(genre.shape())
                            .clickable {
                                expanded = true
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Image(
                            painterResource(R.drawable.ic_eye_mask),
                            null,
                            colorFilter = ColorFilter.tint(genre.resolveColor()),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(24.dp),
                        )

                        Text(
                            event.newlyAppearedCharacters.size.toString(),
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = genre.bodyFont(),
                                ),
                        )
                    }
                }
            }
        }

        emotionalMascot?.let {
            MascotEmotionFace(
                imageUrl = it.second,
                emotionalTone = it.first,
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .size(32.dp),
            )
        }
    }

    if (expanded) {
        ModalBottomSheet(
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            ExpandedTimeline(saga, eventCard)
        }
    }
}

@Composable
fun ExpandedTimeline(
    saga: SagaContent,
    eventCard: TimelineCardContent,
) {
    val genre = remember { saga.data.genre }
    val event = remember { eventCard.timelineContent }
    val emotionalMascot = remember { eventCard.mascotEmotion }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .clip(genre.shape())
                    .align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                painterResource(R.drawable.ic_full_spark),
                null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp),
            )

            Text(
                stringResource(R.string.review_event_label),
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = genre.headerFont(),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                    ),
            )
        }

        Text(
            event.data.createdAt.formatDate(DateFormatOption.FULL_DAY_MONTH_YEAR),
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Light,
                    fontFamily = genre.bodyFont(),
                    textAlign = TextAlign.Center,
                ),
            modifier =
                Modifier
                    .alpha(.5f)
                    .align(Alignment.CenterHorizontally),
        )

        Text(
            event.data.title,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .reactiveShimmer(true, genre.resolveColor().shimmerize()),
            style =
                MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = genre.headerFont(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    brush =
                        Brush.verticalGradient(
                            genre.resolveColor().darkerPalette(),
                        ),
                ),
        )

        Text(
            event.data.content,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = genre.bodyFont(),
                    textAlign = TextAlign.Start,
                ),
        )

        event.data.emotionalReview?.let {
            emotionalMascot?.let {
                MascotEmotionFace(
                    imageUrl = it.second,
                    emotionalTone = it.first,
                    modifier =
                        Modifier
                            .size(100.dp)
                            .align(Alignment.CenterHorizontally),
                )
            }

            Text(
                it,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = genre.bodyFont(),
                        textAlign = TextAlign.Center,
                        fontStyle = FontStyle.Italic,
                    ),
            )
        }

        if (event.newlyAppearedCharacters.isNotEmpty()) {
            Text(
                stringResource(R.string.new_characters_label),
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontFamily = genre.headerFont(),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                    ),
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(event.newlyAppearedCharacters) {
                    CharacterYearbookItem(
                        character = it,
                        saga.data.genre,
                        modifier = Modifier.clip(genre.shape()),
                    )
                }
            }
        }

        if (event.updatedWikis.isNotEmpty()) {
            Text(
                stringResource(R.string.saga_detail_section_title_wiki),
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontFamily = genre.headerFont(),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                    ),
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(event.updatedWikis) {
                    WikiCard(
                        it,
                        genre,
                        modifier =
                            Modifier
                                .border(
                                    1.dp,
                                    genre.resolveColor(),
                                    genre.shape(),
                                ).requiredWidthIn(max = 200.dp),
                        true,
                    )
                }
            }
        }

        if (event.updatedRelationshipDetails.isNotEmpty()) {
            Text(
                stringResource(R.string.saga_detail_relationships_section_title),
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontFamily = genre.headerFont(),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                    ),
            )

            LazyRow {
                items(event.updatedRelationshipDetails) {
                    RelationShipCard(
                        content = it,
                        saga = saga,
                        modifier =
                            Modifier
                                .padding(8.dp)
                                .requiredWidthIn(max = 300.dp),
                    )
                }
            }
        }
    }
}
