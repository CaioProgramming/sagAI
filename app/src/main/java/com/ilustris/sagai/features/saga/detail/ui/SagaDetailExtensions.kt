package com.ilustris.sagai.features.saga.detail.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.features.act.data.model.Book
import com.ilustris.sagai.features.brain.ui.components.UniverseConstellationEntry
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.ui.ChapterCardView
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.usecase.SagaBook
import com.ilustris.sagai.features.saga.detail.data.model.SagaDetailResume
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.DetailSectionView
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.RequestSection
import com.ilustris.sagai.features.saga.detail.data.usecase.mapper.TimelineDrawer
import com.ilustris.sagai.features.saga.detail.ui.components.RowHeader
import com.ilustris.sagai.features.timeline.ui.TimelineContentViewCard
import com.ilustris.sagai.features.wiki.ui.WikiCard
import com.ilustris.sagai.ui.components.CosmicBook
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.progressiveBrush
import com.ilustris.sagai.ui.theme.sagaShape

@Composable
fun TimelineDrawer.renderDrawer(saga: Saga) {
    val genre = saga.genre
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
                            fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center,
                            shadow =
                                Shadow(
                                    MaterialTheme.colorScheme.primary,
                                    Offset.Zero,
                                    15f,
                                ),
                        ),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .gradientFill(
                                progressiveBrush(
                                    MaterialTheme.colorScheme.primary,
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
                                    MaterialTheme.colorScheme.primary,
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
                                fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
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
                            MaterialTheme.colorScheme.primary
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
                                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
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
    resume: SagaDetailResume,
    onAction: (DetailAction) -> Unit = {},
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val saga = resume.saga
    val genre = saga.genre
    val sectionStyle =
        MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold,
        )

    with(sharedTransitionScope) {
        Column(
            modifier =
                Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when (section) {
                RequestSection.ACTS -> {
                    RowHeader(
                        title = stringResource(R.string.the_chronicles),
                        textStyle = sectionStyle,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    ) {
                        if (resume.completedActsCount > 0) {
                            onAction(DetailAction.OpenChronicles(null))
                        }
                    }

                    if (resume.completedActsCount < 1) {
                        SagaSectionEmptyState(
                            title = stringResource(R.string.saga_detail_acts_no_acts_title),
                            subtitle = stringResource(R.string.saga_detail_acts_no_acts_subtitle),
                        )
                    } else if (resume.generatedBooks.isEmpty()) {
                        SagaSectionEmptyState(
                            title = stringResource(R.string.saga_detail_acts_no_books_title),
                            subtitle = stringResource(R.string.saga_detail_acts_no_books_subtitle),
                            actionLabel = stringResource(R.string.saga_detail_acts_create_first_book_cta),
                            onActionClick = { onAction(DetailAction.OpenChronicles(null)) },
                        )
                    } else {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                        ) {
                            items(resume.generatedBooks) { book ->
                                val sagaBook =
                                    remember(book) {
                                        SagaBook(
                                            draft =
                                                SagaDraft(
                                                    id = book.id.toString(),
                                                    title = book.actTitle,
                                                    description = book.authorNote ?: "",
                                                    genre = saga.genre,
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
                                        onAction(DetailAction.OpenChronicles(book.actId))
                                    },
                                    onIntent = {},
                                    modifier =
                                        Modifier
                                            .width(160.dp)
                                            .height(240.dp),
                                    titleModifier = Modifier,
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =
                                Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                                    .clip(sagaShape())
                                    .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.3f))
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
                                    stringResource(R.string.saga_detail_read_story_title),
                                    style =
                                        MaterialTheme.typography.bodyLarge.copy(
                                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                        ),
                                )
                                Text(
                                    stringResource(R.string.saga_detail_read_story_subtitle),
                                    style =
                                        MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
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
                    }
                }

                RequestSection.CHAPTERS -> {
                    Column(Modifier.padding(16.dp).fillMaxWidth()) {
                        val chapters = resume.chapters
                        if (chapters.isNotEmpty()) {
                            RowHeader(stringResource(R.string.saga_detail_latest_chapter_label)) {
                                onAction(DetailAction.OpenSection(RequestSection.CHAPTERS))
                            }

                            val lastValidChapter = chapters.lastOrNull { it.content.isNotEmpty() }

                            lastValidChapter?.let {
                                ChapterCardView(
                                    chapter = it,
                                    chapterIndex = chapters.indexOf(it),
                                    modifier =
                                        Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth()
                                            .height(300.dp),
                                    showTitle = true,
                                    titleStyle =
                                        MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.SemiBold,
                                        ),
                                )
                            }

                            RowHeader(
                                stringResource(R.string.saga_detail_section_title_chapters),
                                textStyle = MaterialTheme.typography.titleLarge,
                            ) {
                                onAction(DetailAction.OpenSection(RequestSection.CHAPTERS))
                            }

                            val remainingChapters = chapters.dropLast(1)

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(remainingChapters) { chapter ->
                                    ChapterCardView(
                                        chapter = chapter,
                                        chapterIndex = chapters.indexOf(chapter),
                                        showTitle = true,
                                        titleStyle =
                                            MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Normal,
                                            ),
                                        modifier =
                                            Modifier.size(170.dp).clickable {
                                                onAction(DetailAction.OpenSection(RequestSection.CHAPTERS))
                                            },
                                    )
                                }
                            }
                        }
                    }
                }

                RequestSection.CHARACTERS -> {
                    RowHeader(
                        stringResource(R.string.saga_detail_section_title_characters),
                        sectionStyle,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    ) {
                        if (resume.topCharacters.isNotEmpty()) {
                            onAction(DetailAction.OpenSection(RequestSection.CHARACTERS))
                        }
                    }

                    if (resume.topCharacters.isEmpty()) {
                        SagaSectionEmptyState(
                            title = stringResource(R.string.saga_detail_characters_empty_title),
                            subtitle = stringResource(R.string.saga_detail_characters_empty_subtitle),
                        )
                        return@Column
                    }
                    LazyHorizontalGrid(
                        rows = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        modifier = Modifier.height(200.dp),
                    ) {
                        items(resume.topCharacters) { char ->
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier =
                                        Modifier
                                            .clip(sagaShape())
                                            .clickable {
                                                onAction(DetailAction.OpenCharacter(char.data.id))
                                            }.padding(8.dp)
                                            .width(160.dp),
                                ) {
                                    CharacterAvatar(
                                        character = char.data,
                                        genre = genre,
                                        modifier =
                                            Modifier
                                                .size(48.dp)
                                                .sharedElement(
                                                    rememberSharedContentState(key = "character_${char.data.id}_icon"),
                                                    animatedVisibilityScope,
                                                ),
                                    )
                                    Text(
                                        char.data.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style =
                                            MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Light,
                                                fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                            ),
                                        modifier = Modifier.padding(start = 8.dp),
                                    )
                                }

                                HorizontalDivider(
                                    thickness = 1.dp,
                                    modifier = Modifier.fillMaxWidth().height(1.dp),
                                )
                            }
                        }
                    }
                }

                RequestSection.EVENTS -> {
                    lastEvent?.let {
                        RowHeader(
                            stringResource(R.string.saga_detail_timeline_section_title),
                            sectionStyle,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        ) {
                            onAction(DetailAction.OpenSection(RequestSection.EVENTS))
                        }

                        TimelineContentViewCard(
                            saga = saga,
                            eventCard = it,
                            onAction = onAction,
                            modifier =
                                Modifier
                                    .padding(horizontal = 16.dp)
                                    .fillMaxWidth(),
                        )
                    }
                }

                RequestSection.WIKI -> {
                    if (resume.latestWikis.isEmpty()) return@Column
                    RowHeader(
                        stringResource(R.string.saga_detail_section_title_wiki),
                        sectionStyle,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    ) {
                        onAction(DetailAction.OpenSection(RequestSection.WIKI))
                    }

                    LazyHorizontalGrid(
                        rows = GridCells.Fixed(3),
                        modifier = Modifier.height(200.dp).fillMaxWidth().padding(16.dp),
                    ) {
                        items(resume.latestWikis) { wiki ->
                            Column(Modifier.padding(8.dp).fillMaxWidth()) {
                                Row(
                                    Modifier.clip(MaterialTheme.shapes.medium).clickable {
                                        onAction(DetailAction.OpenSection(RequestSection.WIKI))
                                    },
                                ) {
                                    Text(
                                        wiki.emojiTag.orEmpty(),
                                        style = MaterialTheme.typography.labelLarge,
                                    )

                                    Spacer(Modifier.weight(1f))

                                    Text(
                                        wiki.title,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                HorizontalDivider(
                                    Modifier.fillMaxWidth().height(1.dp),
                                    thickness = 1.dp,
                                )
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun SagaSectionEmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    val contentColor = LocalContentColor.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(sagaShape())
                .background(contentColor.copy(alpha = 0.06f))
                .let {
                    if (onActionClick != null) it.clickable(onClick = onActionClick) else it
                }
                .padding(24.dp),
    ) {
        Text(
            title,
            textAlign = TextAlign.Center,
            style =
                MaterialTheme.typography.titleMedium.copy(
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    fontWeight = FontWeight.SemiBold,
                ),
        )
        Text(
            subtitle,
            textAlign = TextAlign.Center,
            style =
                MaterialTheme.typography.bodySmall.copy(
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    color = contentColor.copy(alpha = 0.7f),
                ),
            modifier = Modifier.padding(top = 4.dp),
        )
        if (actionLabel != null) {
            Text(
                actionLabel,
                textAlign = TextAlign.Center,
                style =
                    MaterialTheme.typography.labelLarge.copy(
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        fontWeight = FontWeight.Bold,
                    ),
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}
