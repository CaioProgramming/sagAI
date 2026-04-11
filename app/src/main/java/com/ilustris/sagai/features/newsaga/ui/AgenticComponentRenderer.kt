package com.ilustris.sagai.features.newsaga.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.ai.model.LocalGenreVisualConfig
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.newsaga.data.model.resolveIconColor
import com.ilustris.sagai.features.newsaga.data.usecase.AgenticUIComponent
import com.ilustris.sagai.features.newsaga.ui.presentation.AgenticAction
import com.ilustris.sagai.ui.animations.genreVfx
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.darker
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.shape

val LocalSharedTransitionScope =
    staticCompositionLocalOf<SharedTransitionScope?> {
        null
    }

fun AgenticUIComponent.Render(
    scope: LazyStaggeredGridScope,
    sharedTransitionScope: SharedTransitionScope?,
    lockedSaga: SagaDraft?,
    lockedCharacter: CharacterInfo?,
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
    var titleInput by remember { mutableStateOf(draft.title) }
    var descriptionInput by remember { mutableStateOf(draft.description) }
    var isEditing by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

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
                    onClick = {
                        if (isEditing) {
                            onAction(AgenticAction.UpdateSaga(titleInput, descriptionInput))
                        } else {
                            focusRequester.requestFocus()
                        }

                        isEditing = !isEditing
                    },
                ) {
                    AnimatedContent(isEditing) {
                        val icon =
                            if (it.not()) {
                                painterResource(R.drawable.ic_edit)
                            } else {
                                painterResource(R.drawable.ic_check)
                            }
                        Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
                    }
                }
            }

            BasicTextField(
                value = titleInput,
                onValueChange = { titleInput = it },
                textStyle =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = genre.headerFont(),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    ),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .genreVfx(genre),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                enabled = isEditing,
            )

            BasicTextField(
                value = descriptionInput,
                onValueChange = { descriptionInput = it },
                textStyle =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = genre.bodyFont(),
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions =
                    KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        isEditing = false
                        onAction(AgenticAction.UpdateSaga(titleInput, descriptionInput))
                    }),
                enabled = isEditing,
            )
        }
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
    Box(
        modifier =
            modifier
                .padding(8.dp)
                .dropShadow(genreVisuals.first.shape(genreVisuals.second)) {
                    this.color = genreVisuals.first.resolveColor(genreVisuals.second)
                    this.radius = 10f
                    this.spread = 5f
                    this.brush =
                        Brush.verticalGradient(genreVisuals.first.colorPalette(genreVisuals.second))
                }.border(
                    1.dp,
                    genreVisuals.first.resolveColor(genreVisuals.second).copy(alpha = 0.1f),
                    genreVisuals.first.shape(genreVisuals.second),
                ).clip(genreVisuals.first.shape(genreVisuals.second))
                .background(
                    MaterialTheme.colorScheme.background,
                    genreVisuals.first.shape(genreVisuals.second),
                ).clickable { onSelect() },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = persona.name,
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontFamily = genreVisuals.first.headerFont(),
                    ),
            )
            Text(
                text = persona.gender,
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontFamily = genreVisuals.first.bodyFont(),
                    ),
            )
            Text(
                text = persona.description,
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontFamily = genreVisuals.first.bodyFont(),
                    ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
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
    var isEditing by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf(persona.name) }
    var genderInput by remember { mutableStateOf(persona.gender) }
    var descriptionInput by remember { mutableStateOf(persona.description) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

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

        Column(
            modifier = Modifier.padding(20.dp),
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

                IconButton(
                    onClick = {
                        if (isEditing) {
                            onAction(
                                AgenticAction.UpdateCharacter(
                                    nameInput,
                                    genderInput,
                                    descriptionInput,
                                ),
                            )
                        } else {
                            focusRequester.requestFocus()
                        }

                        isEditing = !isEditing
                    },
                ) {
                    AnimatedContent(isEditing) {
                        val icon =
                            if (it.not()) {
                                painterResource(R.drawable.ic_edit)
                            } else {
                                painterResource(R.drawable.ic_check)
                            }
                        Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
                    }
                }
            }

            BasicTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                textStyle =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = genre.headerFont(),
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .genreVfx(genre),
                enabled = isEditing,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                decorationBox = { innerTextField ->
                    if (nameInput.isEmpty()) {
                        Text(
                            "Character Name",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                    innerTextField()
                },
            )

            BasicTextField(
                value = genderInput,
                onValueChange = { genderInput = it },
                textStyle =
                    MaterialTheme.typography.titleSmall.copy(
                        fontFamily = genre.bodyFont(),
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium,
                    ),
                modifier = Modifier.fillMaxWidth(),
                enabled = isEditing,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                decorationBox = { innerTextField ->
                    if (genderInput.isEmpty()) {
                        Text(
                            "Gender/Identity",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                    innerTextField()
                },
            )

            BasicTextField(
                value = descriptionInput,
                onValueChange = { descriptionInput = it },
                textStyle =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = genre.bodyFont(),
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                modifier = Modifier.fillMaxWidth(),
                enabled = isEditing,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions =
                    KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        isEditing = false
                        onAction(
                            AgenticAction.UpdateCharacter(
                                nameInput,
                                genderInput,
                                descriptionInput,
                            ),
                        )
                    }),
                decorationBox = { innerTextField ->
                    if (descriptionInput.isEmpty()) {
                        Text(
                            "Character Bio",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                    innerTextField()
                },
            )
        }
    }
}
