package com.ilustris.sagai.features.newsaga.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.model.UniverseEcho
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.newsaga.data.model.resolveIconColor
import com.ilustris.sagai.features.newsaga.data.model.resolveUrl
import com.ilustris.sagai.features.newsaga.data.usecase.AgenticUIComponent
import com.ilustris.sagai.features.newsaga.data.usecase.SagaBook
import com.ilustris.sagai.features.newsaga.ui.presentation.AgenticAction
import com.ilustris.sagai.ui.animations.genreVfx
import com.ilustris.sagai.ui.components.CosmicBook
import com.ilustris.sagai.ui.components.CosmicEditorSheet
import com.ilustris.sagai.ui.components.CosmicInputField
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.shape

val LocalSharedTransitionScope =
    staticCompositionLocalOf<SharedTransitionScope?> {
        null
    }

val LocalGenderPlaceholders =
    staticCompositionLocalOf<com.ilustris.sagai.features.newsaga.data.model.GenderPlaceholderMap> {
        emptyMap()
    }

fun AgenticUIComponent.Render(
    scope: LazyStaggeredGridScope,
    sharedTransitionScope: SharedTransitionScope?,
    lockedSaga: SagaDraft?,
    lockedCharacter: CharacterInfo?,
    isAgentLoading: Boolean,
    onAction: (AgenticAction) -> Unit,
) {
    when (this) {
        is AgenticUIComponent.AgentMessage -> {
            scope.item(span = StaggeredGridItemSpan.FullLine) {
                AgentMessageCard(text)
            }
        }

        is AgenticUIComponent.IdeaPitches -> {
            scope.SagaPitchesSection(this.ideas, sharedTransitionScope, lockedSaga, onAction)
        }

        is AgenticUIComponent.LibraryComponent -> {
            scope.item(span = StaggeredGridItemSpan.FullLine) {
                LibraryPager(
                    this@Render.books,
                    lockedSaga,
                    lockedCharacter,
                    isAgentLoading,
                    onAction,
                )
            }
        }

        is AgenticUIComponent.ExpandedSaga -> {
            scope.item(span = StaggeredGridItemSpan.FullLine) {
                LockedSagaCard(draft, visualConfig, onAction)
            }
        }

        is AgenticUIComponent.PersonaPitches -> {
            scope.CharacterPitchesSection(
                personas,
                sharedTransitionScope,
                lockedSaga,
                lockedCharacter,
                this.visuals,
                onAction,
            )
        }

        is AgenticUIComponent.ExpandedCharacter -> {
            scope.item(span = StaggeredGridItemSpan.FullLine) {
                LockedCharacterCard(persona, visuals, onAction)
            }
        }

        is AgenticUIComponent.UniverseEchoes -> {
            // Echoes are now rendered outside the grid as input suggestions
        }

        is AgenticUIComponent.ErrorComponent -> {
            scope.item(span = StaggeredGridItemSpan.FullLine) {
                ErrorCard(message, canRetry, onAction)
            }
        }
    }
}

@Composable
private fun AgentMessageCard(text: String) {
    SimpleTypewriterText(
        text = text,
        style =
            MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
            ),
        modifier =
            Modifier
                .padding(16.dp)
                .fillMaxWidth(),
    )
}

private fun LazyStaggeredGridScope.SagaPitchesSection(
    ideas: List<Pair<SagaDraft, GenreVisualConfig>>,
    sharedTransitionScope: SharedTransitionScope?,
    lockedSaga: SagaDraft?,
    onAction: (AgenticAction) -> Unit,
) {
    ideas.forEach { entry ->
        val idea = entry.first
        val visual = entry.second
        val isSelected = idea.id == lockedSaga?.id
        val otherIsSelected = lockedSaga != null && !isSelected

        if (!otherIsSelected) {
            item(
                key = "pitch-${idea.id}",
                span = if (isSelected) StaggeredGridItemSpan.FullLine else StaggeredGridItemSpan.SingleLane,
            ) {
                if (sharedTransitionScope != null) {
                    with(sharedTransitionScope) {
                        AnimatedContent(
                            targetState = isSelected,
                            label = "SagaPitchAnimation",
                            modifier = Modifier.animateItem(),
                        ) { selected ->
                            if (selected) {
                                LockedSagaCard(
                                    draft = lockedSaga ?: idea,
                                    visualConfig = visual,
                                    onAction = onAction,
                                    onUnlock = { onAction(AgenticAction.UnlockSaga) },
                                    modifier =
                                        Modifier.sharedBounds(
                                            rememberSharedContentState("draft-${idea.id}"),
                                            this@AnimatedContent,
                                        ),
                                )
                            } else {
                                IdeaPitchCard(
                                    idea = idea,
                                    visual = visual,
                                    onSelect = { onAction(AgenticAction.SelectSaga(idea)) },
                                    modifier =
                                        Modifier.sharedBounds(
                                            rememberSharedContentState("draft-${idea.id}"),
                                            this@AnimatedContent,
                                        ),
                                )
                            }
                        }
                    }
                } else {
                    if (isSelected) {
                        LockedSagaCard(
                            draft = lockedSaga ?: idea,
                            visualConfig = visual,
                            onAction = onAction,
                            onUnlock = { onAction(AgenticAction.UnlockSaga) },
                        )
                    } else {
                        IdeaPitchCard(
                            idea = idea,
                            visual = visual,
                            onSelect = { onAction(AgenticAction.SelectSaga(idea)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IdeaPitchCard(
    idea: SagaDraft,
    visual: GenreVisualConfig,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val genre = idea.genre
    val shape = MaterialTheme.shapes.large
    val color = genre.resolveColor(visual)
    val gradient = Brush.verticalGradient(genre.colorPalette(visual))

    Column(
        modifier =
            modifier
                .padding(8.dp)
                .dropShadow(shape) {
                    this.color = color
                    this.radius = 10f
                    this.spread = 5f
                    this.brush = gradient
                }.border(1.dp, genre.resolveColor(), shape)
                .background(MaterialTheme.colorScheme.surfaceContainer, shape)
                .clickable { onSelect() }
                .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painterResource(genre.icon),
            null,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(24.dp),
        )

        Text(
            text = idea.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = idea.description,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun LockedSagaCard(
    draft: SagaDraft,
    visualConfig: GenreVisualConfig?,
    onAction: (AgenticAction) -> Unit,
    onUnlock: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val genre = draft.genre
    val shape = genre.shape(visualConfig)
    val brush = Brush.verticalGradient(genre.colorPalette(visualConfig))
    val genreColor = genre.resolveColor(visualConfig)
    val iconColor = genre.resolveIconColor()
    var showEditor by remember { mutableStateOf(false) }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp)
                .dropShadow(shape) {
                    this.color = genreColor
                    this.radius = 10f
                    this.spread = 5f
                    this.brush = brush
                }.border(1.dp, genreColor.copy(alpha = 0.1f), shape)
                .clip(shape)
                .background(genre.color, shape)
                .background(MaterialTheme.colorScheme.background.copy(alpha = .2f)),
    ) {
        Icon(
            painterResource(genre.icon),
            null,
            tint = iconColor,
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .size(100.dp)
                    .alpha(.1f),
        )

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painterResource(R.drawable.round_close_24),
                    "fechar",
                    tint = iconColor,
                    modifier =
                        Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable { onAction(AgenticAction.UnlockSaga) },
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = { showEditor = true },
                ) {
                    Icon(
                        painterResource(R.drawable.ic_edit),
                        null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Text(
                text = draft.title,
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = genre.headerFont(),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    ),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .genreVfx(genre),
            )

            Text(
                text = draft.description,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = genre.bodyFont(),
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (showEditor) {
        CosmicEditorSheet(
            title = "Editar Crônica",
            genre = genre,
            fields =
                listOf(
                    CosmicInputField(
                        "title",
                        "Título da Saga",
                        draft.title,
                        hint = "Dê um nome épico",
                    ),
                    CosmicInputField(
                        "description",
                        "Descrição",
                        draft.description,
                        isMultiline = true,
                        hint = "O que move essa lenda?",
                    ),
                ),
            onSave = {
                onAction(
                    AgenticAction.UpdateSaga(
                        it["title"] ?: draft.title,
                        it["description"] ?: draft.description,
                    ),
                )
            },
            onDismiss = { showEditor = false },
        )
    }
}

private fun LazyStaggeredGridScope.CharacterPitchesSection(
    personas: List<CharacterInfo>,
    sharedTransitionScope: SharedTransitionScope?,
    lockedSaga: SagaDraft?,
    lockedCharacter: CharacterInfo?,
    genreVisuals: Pair<Genre, GenreVisualConfig?>,
    onAction: (AgenticAction) -> Unit,
) {
    personas.forEach { persona ->
        val isSelected = persona.id == lockedCharacter?.id
        val otherIsSelected = lockedCharacter != null && !isSelected

        if (!otherIsSelected) {
            item(
                key = "character-${persona.id}",
                span = if (isSelected) StaggeredGridItemSpan.FullLine else StaggeredGridItemSpan.SingleLane,
            ) {
                if (sharedTransitionScope != null) {
                    with(sharedTransitionScope) {
                        AnimatedContent(
                            targetState = isSelected,
                            label = "CharacterPitchAnimation",
                            modifier = Modifier.animateItem(),
                        ) { selected ->
                            if (selected) {
                                LockedCharacterCard(
                                    persona = lockedCharacter ?: persona,
                                    genreVisuals = genreVisuals,
                                    onAction = onAction,
                                    onUnlock = { onAction(AgenticAction.UnlockCharacter) },
                                    modifier =
                                        Modifier.sharedBounds(
                                            rememberSharedContentState("character-${persona.id}"),
                                            this@AnimatedContent,
                                        ),
                                )
                            } else {
                                CharacterPitchCard(
                                    persona = persona,
                                    genreVisuals = genreVisuals,
                                    onSelect = { onAction(AgenticAction.SelectCharacter(persona)) },
                                    modifier =
                                        Modifier.sharedBounds(
                                            rememberSharedContentState("character-${persona.id}"),
                                            this@AnimatedContent,
                                        ),
                                )
                            }
                        }
                    }
                } else {
                    if (isSelected) {
                        LockedCharacterCard(
                            persona = lockedCharacter ?: persona,
                            genreVisuals = genreVisuals,
                            onAction = onAction,
                            onUnlock = { onAction(AgenticAction.UnlockCharacter) },
                        )
                    } else {
                        CharacterPitchCard(
                            persona = persona,
                            genreVisuals = genreVisuals,
                            onSelect = { onAction(AgenticAction.SelectCharacter(persona)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CharacterPitchCard(
    persona: CharacterInfo,
    genreVisuals: Pair<Genre, GenreVisualConfig?>,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val genre = genreVisuals.first
    val visualConfig = genreVisuals.second
    val placeholders = LocalGenderPlaceholders.current
    val silhouetteUrl = placeholders.resolveUrl(genre, persona.gender)
    val shape = genre.shape(visualConfig)
    val genreColor = genre.resolveColor(visualConfig)

    Column(
        modifier =
            modifier
                .padding(8.dp)
                .dropShadow(shape) {
                    this.color = genreColor
                    this.radius = 10f
                    this.spread = 5f
                    this.brush =
                        Brush.verticalGradient(genre.colorPalette(visualConfig))
                }.border(
                    1.dp,
                    genreColor.copy(alpha = 0.1f),
                    shape,
                ).clip(shape)
                .background(
                    MaterialTheme.colorScheme.surfaceContainer,
                    shape,
                ).clickable { onSelect() },
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(genreColor.copy(alpha = 0.1f)),
        ) {
            if (silhouetteUrl.isNotEmpty()) {
                coil3.compose.AsyncImage(
                    model = silhouetteUrl,
                    contentDescription = null,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                )
            } else {
                Icon(
                    painterResource(genre.icon),
                    null,
                    tint = genreColor.copy(alpha = 0.3f),
                    modifier =
                        Modifier
                            .size(80.dp)
                            .align(Alignment.Center),
                )
            }
        }

        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = persona.name,
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontFamily = genre.headerFont(),
                        fontWeight = FontWeight.Bold,
                    ),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            Text(
                text = persona.gender.name.lowercase(),
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontFamily = genre.bodyFont(),
                        color = genreColor,
                    ),
            )
            Text(
                text = persona.description,
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontFamily = genre.bodyFont(),
                    ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                maxLines = 3,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun LockedCharacterCard(
    persona: CharacterInfo,
    genreVisuals: Pair<Genre, GenreVisualConfig?>,
    onAction: (AgenticAction) -> Unit,
    onUnlock: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val genre = genreVisuals.first
    val visualConfig = genreVisuals.second
    val shape = genre.shape(visualConfig)
    val color = genre.resolveColor(visualConfig)
    val brush = Brush.verticalGradient(genre.colorPalette(visualConfig))
    val iconColor = genre.resolveIconColor()
    var showEditor by remember { mutableStateOf(false) }

    val placeholders = LocalGenderPlaceholders.current
    val silhouetteUrl = placeholders.resolveUrl(genre, persona.gender)

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp)
                .dropShadow(shape) {
                    this.color = color
                    this.radius = 10f
                    this.spread = 5f
                    this.brush = brush
                }.border(1.dp, color.copy(alpha = 0.1f), shape)
                .clip(shape)
                .background(genre.color, shape)
                .background(MaterialTheme.colorScheme.background.copy(alpha = .2f)),
    ) {
        if (silhouetteUrl.isNotEmpty()) {
            coil3.compose.AsyncImage(
                model = silhouetteUrl,
                contentDescription = null,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .alpha(0.1f)
                        .align(Alignment.Center),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
            )
        } else {
            Icon(
                painterResource(genre.icon),
                null,
                tint = iconColor,
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .size(50.dp)
                        .alpha(.4f),
            )
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(R.drawable.round_close_24),
                    "fechar",
                    tint = iconColor,
                    modifier =
                        Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .clickable { onAction(AgenticAction.UnlockCharacter) },
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    painterResource(R.drawable.ic_edit),
                    null,
                    tint = iconColor,
                    modifier =
                        Modifier
                            .size(32.dp)
                            .clickable {
                                showEditor = true
                            },
                )
            }

            Text(
                text = persona.name,
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = genre.headerFont(),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    ),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .genreVfx(genre),
            )

            Text(
                text = persona.description,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = genre.bodyFont(),
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (showEditor) {
        CosmicEditorSheet(
            title = "Editar Herói",
            genre = genre,
            fields =
                listOf(
                    CosmicInputField(
                        "name",
                        "Nome do Personagem",
                        persona.name,
                        hint = "Como ele é conhecido?",
                    ),
                    CosmicInputField(
                        "description",
                        "Biografia",
                        persona.description,
                        isMultiline = true,
                        hint = "Descreva a essência deste buscador",
                    ),
                ),
            onSave = {
                onAction(
                    AgenticAction.UpdateCharacter(
                        it["name"] ?: persona.name,
                        it["gender"] ?: persona.gender.name,
                        it["description"] ?: persona.description,
                    ),
                )
            },
            onDismiss = { showEditor = false },
        )
    }
}

@Composable
fun LibraryPager(
    books: List<Pair<SagaBook, GenreVisualConfig>>,
    lockedSaga: SagaDraft?,
    lockedCharacter: CharacterInfo?,
    isAgentLoading: Boolean,
    onAction: (AgenticAction) -> Unit,
) {
    val pagerState =
        rememberPagerState { books.size }
    var openedBookIdx by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(lockedSaga) {
        if (lockedSaga == null) {
            openedBookIdx = null
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        androidx.compose.foundation.pager.HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 32.dp),
            pageSpacing = 16.dp,
            modifier = Modifier.fillMaxWidth(),
            userScrollEnabled = lockedSaga == null,
        ) { pageIdx ->
            val bookEntry = books[pageIdx]
            val isOpened = bookEntry.first.draft.id == lockedSaga?.id

            CosmicBook(
                book = bookEntry.first,
                visualConfig = bookEntry.second,
                isOpened = isOpened,
                lockedCharacter = lockedCharacter,
                isLoading = isAgentLoading && (lockedSaga?.id == bookEntry.first.draft.id),
                onToggle = {
                    if (isOpened) {
                        onAction(AgenticAction.UnlockSaga)
                    } else {
                        onAction(AgenticAction.SelectSaga(bookEntry.first.draft))
                    }
                },
                onAction = onAction,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Small indicator
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            repeat(books.size) { iteration ->
                val color =
                    if (pagerState.currentPage ==
                        iteration
                    ) {
                        MaterialTheme.colorScheme.onBackground
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    }

                AnimatedContent(pagerState.currentPage == iteration, transitionSpec = {
                    scaleIn() togetherWith scaleOut()
                }) {
                    if (it) {
                        val pageGenre = books[pagerState.currentPage]
                        Icon(
                            painterResource(pageGenre.first.draft.genre.icon),
                            null,
                            modifier = Modifier.size(12.dp),
                            tint = color,
                        )
                    } else {
                        Box(
                            modifier =
                                Modifier
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .size(8.dp),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun UniverseEchoesSection(
    echoes: List<Pair<UniverseEcho, GenreVisualConfig>>,
    onAction: (String) -> Unit,
) {
    LazyHorizontalGrid(
        rows = GridCells.Fixed(2),
        modifier =
            Modifier
                .fillMaxWidth()
                .height(100.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
    ) {
        items(echoes) { (echo, config) ->
            EchoBubbleCard(echo, config, modifier = Modifier.animateItem()) {
                onAction(echo.input)
            }
        }
    }
}

@Composable
private fun EchoBubbleCard(
    echo: UniverseEcho,
    visualConfig: GenreVisualConfig,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val genre = echo.genre
    val color = genre.resolveColor(visualConfig)
    val shape = MaterialTheme.shapes.extraLarge
    val genreBrush = Brush.linearGradient(genre.colorPalette(visualConfig))

    Row(
        modifier =
            modifier
                .padding(2.dp)
                .wrapContentSize()
                .dropShadow(shape) {
                    brush = genreBrush
                    radius = 5f
                    spread = 5f
                }.clip(shape)
                .background(MaterialTheme.colorScheme.background)
                .clickable {
                    onClick()
                }.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            painterResource(genre.icon),
            null,
            tint = color,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = echo.input,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ErrorCard(
    message: String,
    canRetry: Boolean,
    onAction: (AgenticAction) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
        shape = MaterialTheme.shapes.medium,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                painterResource(R.drawable.ic_warning),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp),
            )

            Text(
                text = "Cosmic Interruption",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            if (canRetry) {
                Button(
                    onClick = {
                        // For now we just reset or re-submit last prompt if we had it,
                        // but let's just use a neutral retry action for now that the user can handle
                    },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Icon(
                        painterResource(R.drawable.baseline_refresh_24),
                        null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Try Aligning Again")
                }
            }
        }
    }
}
