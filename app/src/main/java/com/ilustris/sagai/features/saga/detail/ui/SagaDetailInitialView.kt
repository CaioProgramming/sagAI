package com.ilustris.sagai.features.saga.detail.ui

import ai.atick.material.MaterialColor
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.relations.ui.RelationShipCard
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.characters.ui.CharacterYearbookItem
import com.ilustris.sagai.features.characters.ui.components.VerticalLabel
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.features.playthrough.toPlaytimeFormat
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.DetailSectionView
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.RequestSection
import com.ilustris.sagai.features.saga.detail.ui.components.RowHeader
import com.ilustris.sagai.features.timeline.ui.TimelineContentViewCard
import com.ilustris.sagai.ui.components.stylisedText
import com.ilustris.sagai.ui.components.views.DepthLayout
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shape
import kotlin.time.Duration.Companion.seconds

@Composable
fun SagaDetailInitialContent(
    saga: SagaContent,
    section: DetailSectionView.InitialSection,
    onAction: (DetailAction) -> Unit = {},
    showTitleOnly: Boolean = false,
) {
    val columnCount = 2
    val gridState = rememberLazyGridState()
    val genre = remember { saga.data.genre }

    AnimatedContent(showTitleOnly) {
        if (it) {
            Box(Modifier.fillMaxSize()) {
                genre.stylisedText(
                    saga.data.title,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                            .reactiveShimmer(true)
                            .padding(8.dp),
                )
            }
        } else {
            Box(Modifier.fillMaxSize()) {
                val genreHighlight = genre.selectiveHighlight()
                val chapters = section.chapters ?: emptyList()
                val eventsCount = saga.characters.size
                val messagesCount = chapters.sumOf { it.events.sumOf { it.messages.size } }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnCount),
                    modifier = Modifier.fillMaxSize(),
                    state = gridState,
                ) {
                    item(span = { GridItemSpan(columnCount) }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (saga.data.icon.isNotBlank()) {
                                Box(
                                    modifier =
                                        Modifier
                                            .background(MaterialTheme.colorScheme.background)
                                            .height(400.dp)
                                            .fillMaxWidth(),
                                ) {
                                    val currentModifier =
                                        Modifier
                                            .align(Alignment.TopCenter)
                                            .padding(16.dp)
                                            .reactiveShimmer(
                                                true,
                                                duration = 5.seconds,
                                            )
                                    if (section.segmentedImage != null) {
                                        DepthLayout(
                                            originalImage = section.segmentedImage.first,
                                            segmentedImage = section.segmentedImage.second,
                                            modifier = Modifier.fillMaxSize(),
                                            imageModifier =
                                                Modifier
                                                    .clipToBounds()
                                                    .effectForGenre(genre)
                                                    .selectiveColorHighlight(genreHighlight),
                                        ) {
                                            genre.stylisedText(
                                                saga.data.title,
                                                modifier =
                                                    currentModifier
                                                        .fillMaxWidth()
                                                        .padding(8.dp),
                                            )
                                        }
                                    } else {
                                        AsyncImage(
                                            saga.data.icon,
                                            contentDescription = saga.data.title,
                                            modifier =
                                                Modifier
                                                    .background(MaterialTheme.colorScheme.background)
                                                    .fillMaxSize()
                                                    .effectForGenre(genre)
                                                    .selectiveColorHighlight(genreHighlight),
                                            contentScale = ContentScale.Crop,
                                        )

                                        genre.stylisedText(
                                            saga.data.title,
                                            modifier = currentModifier.padding(8.dp),
                                        )
                                    }

                                    Box(
                                        Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .fillMaxHeight(.6f)
                                            .background(fadeGradientBottom()),
                                    )
                                }
                            } else {
                                Image(
                                    painterResource(genre.icon),
                                    null,
                                    modifier =
                                        Modifier
                                            .align(Alignment.CenterHorizontally)
                                            .padding(32.dp)
                                            .size(100.dp)
                                            .clickable {
                                                onAction(DetailAction.RegenerateIcon)
                                            }
                                            .gradientFill(genre.gradient()),
                                )

                                genre.stylisedText(
                                    saga.data.title,
                                    modifier =
                                        Modifier
                                            .padding(8.dp)
                                            .fillMaxWidth()
                                            .reactiveShimmer(
                                                true,
                                                duration = 5.seconds,
                                            ),
                                )
                            }
                        }
                    }

                    item(span = { GridItemSpan(columnCount) }) {
                        LazyRow(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =
                                Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                        ) {
                            item {
                                VerticalLabel(
                                    chapters.count().toString(),
                                    stringResource(R.string.saga_detail_section_title_chapters),
                                    genre,
                                )
                            }

                            item {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp),
                                ) {
                                    Text(
                                        messagesCount.toString(),
                                        style =
                                            MaterialTheme.typography.titleMedium.copy(
                                                fontFamily = genre.headerFont(),
                                                fontWeight = FontWeight.Normal,
                                                textAlign = TextAlign.Center,
                                            ),
                                        modifier =
                                            Modifier
                                                .padding(2.dp)
                                                .fillMaxWidth(),
                                    )

                                    Text(
                                        stringResource(R.string.saga_detail_messages_label),
                                        style =
                                            MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = genre.bodyFont(),
                                                fontWeight = FontWeight.Light,
                                                textAlign = TextAlign.Center,
                                            ),
                                        modifier = Modifier.alpha(.4f),
                                    )
                                }
                            }
                            item {
                                VerticalLabel(
                                    eventsCount.toString(),
                                    stringResource(R.string.saga_detail_section_title_characters),
                                    genre,
                                )
                            }

                            item {
                                VerticalLabel(
                                    saga.data.playTimeMs.toPlaytimeFormat(),
                                    stringResource(R.string.playtime_title),
                                    genre,
                                )
                            }
                        }
                    }

                    item(span = { GridItemSpan(columnCount) }) {
                        Column {
                            Text(
                                section.sagaResume,
                                modifier = Modifier.padding(16.dp),
                                style =
                                    MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = genre.bodyFont(),
                                        textAlign = TextAlign.Justify,
                                    ),
                            )
                        }
                    }

                    if (saga.data.isEnded) {
                        item {
                            RecapHeroCard(
                                saga = saga,
                                modifier =
                                    Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                onClick = {
                                    onAction(DetailAction.OpenReview)
                                },
                            )
                        }
                    }

                    section.starring?.let {
                        item(span = { GridItemSpan(columnCount) }) {
                            Text(
                                stringResource(R.string.starring),
                                style =
                                    MaterialTheme.typography.displaySmall.copy(
                                        fontFamily = genre.headerFont(),
                                        textAlign = TextAlign.Start,
                                    ),
                                modifier = Modifier.padding(16.dp),
                            )
                        }

                        item(span = { GridItemSpan(columnCount) }) {
                            val shape = genre.shape()

                            Box(
                                Modifier
                                    .padding(16.dp)
                                    .clip(shape = shape)
                                    .border(1.dp, genre.color.gradientFade(), shape)
                                    .background(genre.color.gradientFade())
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .clickable {
                                        onAction(DetailAction.OpenSection(RequestSection.CHARACTERS))
                                    },
                            ) {
                                AsyncImage(
                                    it.data.image,
                                    contentDescription = it.data.name,
                                    modifier =
                                        Modifier
                                            .fillMaxSize()
                                            .effectForGenre(genre),
                                    contentScale = ContentScale.Crop,
                                )

                                Box(
                                    Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .fillMaxHeight(.8f)
                                        .background(
                                            fadeGradientBottom(genre.color),
                                        ),
                                )

                                Column(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .align(Alignment.BottomCenter)
                                            .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        it.data.name,
                                        style =
                                            MaterialTheme.typography.headlineMedium.copy(
                                                fontFamily = genre.headerFont(),
                                                color = genre.iconColor,
                                            ),
                                    )

                                    Text(
                                        it.data.backstory,
                                        maxLines = 5,
                                        overflow = TextOverflow.Ellipsis,
                                        style =
                                            MaterialTheme.typography.labelMedium.copy(
                                                fontFamily = genre.bodyFont(),
                                                color = genre.iconColor,
                                            ),
                                    )
                                }
                            }
                        }
                    }

                    RequestSection.entries.forEach {
                        item(span = { GridItemSpan(columnCount) }) {
                            section.miniSection(it, saga, onAction)
                        }
                    }

                    item(span = {
                        GridItemSpan(columnCount)
                    }) {
                        Button(
                            onClick = {
                                onAction(DetailAction.Delete)
                            },
                            colors =
                                ButtonDefaults.textButtonColors(
                                    contentColor = MaterialColor.Red400,
                                ),
                            modifier =
                                Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                        ) {
                            Text(
                                stringResource(R.string.saga_detail_delete_saga_button),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}
