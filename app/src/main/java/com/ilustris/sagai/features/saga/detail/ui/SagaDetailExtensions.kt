package com.ilustris.sagai.features.saga.detail.ui

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.sortCharactersByMessageCount
import com.ilustris.sagai.features.act.ui.ActsGalleryContent
import com.ilustris.sagai.features.chapter.ui.ChapterCardView
import com.ilustris.sagai.features.chapter.ui.ChaptersGalleryContent
import com.ilustris.sagai.features.characters.relations.ui.RelationShipCard
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.characters.ui.CharactersGalleryContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.newsaga.data.model.resolveIconColor
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.DetailSectionView
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.RequestSection
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.TimelineDrawer
import com.ilustris.sagai.features.saga.detail.ui.DetailAction.OpenSection
import com.ilustris.sagai.features.saga.detail.ui.components.RowHeader
import com.ilustris.sagai.features.timeline.ui.TimeLineContent
import com.ilustris.sagai.features.timeline.ui.TimelineContentViewCard
import com.ilustris.sagai.features.wiki.ui.WikiCard
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.chat.BubbleTailAlignment
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.levitate
import com.ilustris.sagai.ui.theme.progressiveBrush
import com.ilustris.sagai.ui.theme.shape

@Composable
fun DetailSectionView.RenderSection(
    saga: SagaContent,
    animationScopes: Pair<SharedTransitionScope, AnimatedContentScope>,
    onAction: (DetailAction) -> Unit = {},
) {
    when (this) {
        is DetailSectionView.InitialSection -> {
            SagaDetailInitialContent(
                saga = saga,
                section = this,
                onAction = onAction,
            )
        }

        is DetailSectionView.CharacterSection -> {
            CharactersGalleryContent(
                section = this,
                animationScopes = animationScopes,
                onOpenEvent = {
                    onAction(OpenSection(RequestSection.EVENTS))
                },
            )
        }

        is DetailSectionView.WikiSection -> {
            WikiContent(
                title,
                subtitle,
                insight,
                saga,
                groups = wikiGroup,
                onBackClick = {
                    onAction(OpenSection(RequestSection.START))
                },
                reviewWiki = {
                    onAction(DetailAction.ReviewWiki(it))
                },
            )
        }

        is DetailSectionView.EventsSection -> {
            TimeLineContent(
                saga,
                this.insight,
                this.title,
                this.subtitle,
                onBackClick = {
                    onAction(OpenSection(RequestSection.START))
                },
            )
        }

        is DetailSectionView.ChapterSection -> {
            ChaptersGalleryContent(
                section = this,
                onBackClick = {
                    onAction(OpenSection(RequestSection.START))
                },
            )
        }

        is DetailSectionView.ActSection -> {
            ActsGalleryContent(
                section = this,
                onBackClick = {
                    onAction(OpenSection(RequestSection.START))
                },
            )
        }
    }
}

@Composable
fun TimelineDrawer.renderDrawer(saga: SagaContent) {
    val genre = saga.data.genre
    LazyColumn(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier =
            Modifier
                .padding(16.dp)
                .fillMaxSize()
                .padding(8.dp),
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
                        ),
                    modifier =
                        Modifier
                            .background(
                                MaterialTheme.colorScheme.surfaceContainer,
                                genre.shape(),
                            ).background(
                                progressiveBrush(
                                    genre.resolveColor(),
                                    progress,
                                ),
                                genre.shape(),
                            ).padding(8.dp),
                )
            }
        }
        group.forEach {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier.gradientFill(
                            progressiveBrush(
                                genre.resolveColor(),
                                it.progress,
                            ),
                        ),
                ) {
                    Icon(
                        painterResource(genre.icon),
                        null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier =
                            Modifier
                                .padding(
                                    end = 8.dp,
                                ).size(48.dp),
                    )

                    Text(
                        it.title,
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontFamily = genre.bodyFont(),
                            ),
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
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painterResource(genre.icon),
                            null,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp),
                        )

                        Text(
                            it.title,
                            style =
                                MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = genre.bodyFont(),
                                    color = iconTint,
                                ),
                        )
                    }
                    if (it != drawerItems.last()) {
                        VerticalDivider(
                            thickness = 2.dp,
                            color = iconTint,
                            modifier =
                                Modifier
                                    .padding(horizontal = 8.dp)
                                    .fillParentMaxHeight(.05f),
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
    val genre = saga.data.genre
    val sectionStyle =
        MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
            fontFamily = genre.bodyFont(),
        )
    when (section) {
        RequestSection.ACTS -> {
            if (acts.isEmpty()) return
            Column(
                Modifier.clickable {
                    onAction(OpenSection(RequestSection.ACTS))
                },
            ) {
                val text =
                    if (saga.data.isEnded) stringResource(R.string.see_full_story) else "A história até agora"
                Icon(
                    painterResource(genre.icon),
                    null,
                    tint = genre.resolveColor(),
                    modifier = Modifier.size(50.dp),
                )

                Text(
                    text,
                    style =
                        MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = genre.bodyFont(),
                            shadow =
                                Shadow(
                                    genre.resolveColor(),
                                    Offset.Zero,
                                    10f,
                                ),
                            fontStyle = FontStyle.Italic,
                        ),
                    modifier = Modifier.levitate(),
                )
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
                Row(
                    Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.saga_detail_section_title_chapters),
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = genre.bodyFont(),
                            ),
                        modifier =
                            Modifier
                                .padding(8.dp)
                                .weight(1f),
                    )

                    IconButton(onClick = {
                        onAction(
                            OpenSection(RequestSection.CHAPTERS),
                        )
                    }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            painterResource(R.drawable.round_arrow_forward_ios_24),
                            contentDescription = stringResource(R.string.saga_detail_view_chapters_action),
                        )
                    }
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
                Row(
                    Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.saga_detail_section_title_wiki),
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = genre.bodyFont(),
                            ),
                        modifier =
                            Modifier
                                .padding(8.dp)
                                .weight(1f),
                    )

                    IconButton(onClick = {
                        onAction(
                            OpenSection(RequestSection.WIKI),
                        )
                    }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            painterResource(R.drawable.round_arrow_forward_ios_24),
                            contentDescription = null,
                        )
                    }
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
                Row(
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        stringResource(R.string.saga_detail_section_title_characters),
                        style = sectionStyle,
                        modifier =
                            Modifier
                                .padding(8.dp)
                                .weight(1f),
                    )

                    IconButton(onClick = {
                        onAction(
                            OpenSection(RequestSection.CHARACTERS),
                        )
                    }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            painterResource(R.drawable.round_arrow_forward_ios_24),
                            contentDescription = stringResource(R.string.saga_detail_view_characters_action),
                        )
                    }
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
                                    onAction(
                                        OpenSection(
                                            RequestSection.CHARACTERS,
                                        ),
                                    )
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
                if (relationships.isNotEmpty()) {
                    RowHeader(
                        stringResource(R.string.saga_detail_relationships_section_title),
                        textStyle =
                            MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = genre.headerFont(),
                                textAlign = TextAlign.Start,
                            ),
                    ) {
                        onAction(OpenSection(RequestSection.CHARACTERS))
                    }

                    LazyRow(Modifier.fillMaxWidth()) {
                        items(relationships.size) { index ->
                            val relationship = relationships[index]
                            RelationShipCard(
                                content = relationship,
                                saga = saga,
                                modifier =
                                    Modifier
                                        .padding(16.dp)
                                        .requiredWidthIn(max = 300.dp),
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
                    Row(
                        Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(R.string.saga_detail_timeline_section_title),
                            style =
                                MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = genre.bodyFont(),
                                ),
                            modifier =
                                Modifier
                                    .padding(8.dp)
                                    .weight(1f),
                        )

                        IconButton(onClick = {
                            onAction(
                                OpenSection(RequestSection.EVENTS),
                            )
                        }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                painterResource(R.drawable.round_arrow_forward_ios_24),
                                contentDescription = null,
                            )
                        }
                    }

                    lastEvent?.let {
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

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(saga.wikis) { wiki ->
                            val shape = genre.shape()

                            WikiCard(
                                wiki,
                                genre,
                                Modifier
                                    .size(250.dp)
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
        }

        RequestSection.START -> {
            Spacer(Modifier.size(50.dp))
        }
    }
}
