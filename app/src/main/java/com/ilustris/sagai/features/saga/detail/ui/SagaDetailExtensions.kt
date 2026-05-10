package com.ilustris.sagai.features.saga.detail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.sortCharactersByMessageCount
import com.ilustris.sagai.features.chapter.ui.ChapterCardView
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.newsaga.data.model.resolveIconColor
import com.ilustris.sagai.features.newsaga.data.usecase.SagaBook
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.DetailSectionView
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.RequestSection
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.TimelineDrawer
import com.ilustris.sagai.features.saga.detail.ui.DetailAction.OpenSection
import com.ilustris.sagai.features.saga.detail.ui.components.RowHeader
import com.ilustris.sagai.features.timeline.ui.TimelineContentViewCard
import com.ilustris.sagai.features.wiki.ui.WikiCard
import com.ilustris.sagai.ui.components.CosmicBook
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.chat.BubbleTailAlignment
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.progressiveBrush
import com.ilustris.sagai.ui.theme.shape

@Composable
fun TimelineDrawer.renderDrawer(saga: SagaContent) {
    val genre = saga.data.genre
    LazyColumn(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier =
            Modifier
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .fillMaxSize(),
    ) {
        item {
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .fillMaxWidth(),
            ) {
                Text(
                    title,
                    style =
                        MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = genre.headerFont(),
                            color = genre.resolveIconColor(),
                            textAlign = TextAlign.Center,
                            shadow =
                                Shadow(
                                    genre.resolveColor(),
                                    Offset.Zero,
                                    15f,
                                ),
                        ),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .gradientFill(
                                progressiveBrush(
                                    genre.resolveColor(),
                                    progress,
                                ),
                            ).padding(16.dp),
                )
            }
        }
        group.forEach {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .gradientFill(
                                progressiveBrush(
                                    genre.resolveColor(),
                                    it.progress,
                                ),
                            ),
                ) {
                    Icon(
                        painterResource(genre.icon),
                        null,
                        modifier =
                            Modifier
                                .padding(12.dp)
                                .size(32.dp),
                    )

                    Text(
                        it.title,
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontFamily = genre.bodyFont(),
                                fontWeight = FontWeight.ExtraBold,
                            ),
                        modifier = Modifier.padding(end = 8.dp),
                    )
                }
            }

            val drawerItems = it.items
            items(drawerItems) {
                Column {
                    val iconTint =
                        if (it.isComplete.not()) {
                            MaterialTheme.colorScheme.onBackground.copy(alpha = .4f)
                        } else {
                            genre.resolveColor()
                        }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 24.dp, top = 4.dp, bottom = 4.dp),
                    ) {
                        Icon(
                            painterResource(genre.icon),
                            null,
                            tint = iconTint,
                            modifier = Modifier.size(16.dp),
                        )

                        Text(
                            it.title,
                            style =
                                MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = genre.bodyFont(),
                                    color = iconTint,
                                ),
                        )
                    }
                    if (it != drawerItems.last()) {
                        VerticalDivider(
                            thickness = 1.dp,
                            color = iconTint,
                            modifier =
                                Modifier
                                    .padding(horizontal = 31.dp)
                                    .fillParentMaxHeight(.03f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailSectionView.InitialSection.miniSection(
    section: RequestSection,
    saga: SagaContent,
    onAction: (DetailAction) -> Unit,
) {
    val currentActs = saga.acts
    val genre = saga.data.genre
    val sectionStyle =
        MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.SemiBold,
            fontFamily = genre.bodyFont(),
        )
    when (section) {
        RequestSection.ACTS -> {
            if (currentActs.isEmpty()) {
                return
            }
            Column(
                Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth(),
            ) {
                val books = saga.acts.mapNotNull { it.book }
                RowHeader(
                    stringResource(R.string.the_chronicles),
                    sectionStyle,
                ) {
                    onAction(OpenSection(RequestSection.ACTS))
                }

                LazyRow(modifier = Modifier.padding(vertical = 8.dp)) {
                    items(books) { book ->
                        val sagaBook =
                            remember(book) {
                                SagaBook(
                                    draft =
                                        com.ilustris.sagai.features.newsaga.data.model.SagaDraft(
                                            title = book.actTitle,
                                            genre = saga.data.genre,
                                            description = "",
                                        ),
                                )
                            }
                        CosmicBook(
                            book = sagaBook,
                            visualConfig =
                                com.ilustris.sagai.core.ai.model
                                    .GenreVisualConfig(),
                            isOpened = false,
                            isLoading = false,
                            onToggle = {
                                onAction(DetailAction.OpenChronicles(null))
                            },
                            onAction = {},
                            modifier =
                                Modifier
                                    .padding(8.dp)
                                    .width(160.dp)
                                    .height(240.dp),
                            titleModifier = Modifier,
                        )
                    }
                }

                if (currentActs.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                                .clip(genre.shape())
                                .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f))
                                .clickable {
                                    onAction(DetailAction.OpenStoryReader)
                                }.padding(16.dp),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier =
                                Modifier.weight(
                                    1f,
                                ),
                        ) {
                            Text(
                                "Read your story",
                                style =
                                    MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = genre.bodyFont(),
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                            )
                            Text(
                                "Read the story from start to beginning until now",
                                style =
                                    MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = genre.bodyFont(),
                                    ),
                                modifier = Modifier.alpha(0.6f),
                            )
                        }

                        Icon(
                            painterResource(R.drawable.round_arrow_forward_ios_24),
                            contentDescription = null,
                            modifier =
                                Modifier
                                    .padding(16.dp)
                                    .size(24.dp),
                        )
                    }
                } else {
                    Text(
                        "Continue playing to see your story here",
                        style =
                            MaterialTheme.typography.labelMedium.copy(
                                fontFamily = genre.bodyFont(),
                                textAlign = TextAlign.Center,
                            ),
                        modifier =
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                    )
                }
            }
        }

        RequestSection.CHAPTERS -> {
            if (chapters.isEmpty()) {
                return
            }
            Column(
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                RowHeader(
                    stringResource(R.string.saga_detail_section_title_chapters),
                    sectionStyle,
                ) {
                    onAction(OpenSection(RequestSection.CHAPTERS))
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(chapters) { chapter ->
                        val shape =
                            genre.bubble(
                                BubbleTailAlignment.BottomRight,
                                0.dp,
                                0.dp,
                                true,
                            )

                        ChapterCardView(
                            genre,
                            chapter.data,
                            saga.flatChapters().indexOf(chapter),
                            Modifier
                                .size(250.dp)
                                .clip(shape)
                                .clickable {
                                    onAction(
                                        OpenSection(
                                            RequestSection.CHAPTERS,
                                        ),
                                    )
                                }.padding(8.dp),
                            showTitle = false,
                        )
                    }
                }
            }
        }

        RequestSection.WIKI -> {
            if (saga.wikis.isEmpty()) {
                return
            }
            Column(
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                RowHeader(
                    stringResource(R.string.saga_detail_section_title_wiki),
                    sectionStyle,
                ) {
                    onAction(OpenSection(RequestSection.WIKI))
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(saga.wikis) { wiki ->
                        val shape = genre.shape()

                        WikiCard(
                            wiki,
                            genre,
                            Modifier
                                .size(100.dp)
                                .clip(shape)
                                .clickable {
                                    onAction(
                                        OpenSection(
                                            RequestSection.WIKI,
                                        ),
                                    )
                                },
                        )
                    }
                }
            }
        }

        RequestSection.CHARACTERS -> {
            if (characters.isEmpty()) {
                return
            }
            Column {
                RowHeader(
                    stringResource(R.string.saga_detail_section_title_characters),
                    textStyle =
                    sectionStyle,
                ) {
                    onAction(OpenSection(RequestSection.CHARACTERS))
                }
                LazyRow {
                    items(
                        sortCharactersByMessageCount(
                            saga.getCharacters(),
                            saga.flatMessages(),
                        ),
                    ) { char ->

                        Column(
                            Modifier
                                .clip(genre.shape())
                                .padding(8.dp)
                                .clickable {
                                    onAction(DetailAction.OpenCharacter(char.id))
                                },
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CharacterAvatar(
                                char,
                                borderSize = 2.dp,
                                genre = saga.data.genre,
                                modifier =
                                    Modifier
                                        .padding(8.dp)
                                        .size(100.dp),
                            )

                            Text(
                                char.name,
                                style =
                                    MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Light,
                                        textAlign = TextAlign.Center,
                                        fontFamily = saga.data.genre.bodyFont(),
                                    ),
                            )
                        }
                    }
                }
            }
        }

        RequestSection.EVENTS -> {
            lastEvent?.let {
                Column(
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                ) {
                    RowHeader(
                        stringResource(R.string.saga_detail_timeline_section_title),
                        sectionStyle,
                    ) {
                        onAction(OpenSection(RequestSection.EVENTS))
                    }

                    TimelineContentViewCard(
                        saga,
                        it,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onAction(
                                        OpenSection(
                                            RequestSection.EVENTS,
                                        ),
                                    )
                                },
                    )
                }
            }
        }

        RequestSection.START -> {
            Spacer(Modifier.size(50.dp))
        }
    }
}
