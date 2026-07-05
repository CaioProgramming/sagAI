@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)

package com.ilustris.sagai.features.newsaga.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.features.newsaga.ui.presentation.Effect
import com.ilustris.sagai.features.newsaga.ui.presentation.NewSagaIntent
import com.ilustris.sagai.features.newsaga.ui.presentation.NewSagaScreenPhase
import com.ilustris.sagai.features.newsaga.ui.presentation.NewSagaUiState
import com.ilustris.sagai.features.newsaga.ui.presentation.NewSagaViewModel
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import com.ilustris.sagai.features.onboarding.ui.OnboardingDialog
import com.ilustris.sagai.ui.components.GenreMemoriesLoader
import com.ilustris.sagai.ui.components.NewSagaBookFocus
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.morphingColor
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.sagaShape
import com.ilustris.sagai.ui.theme.solidGradient
import com.ilustris.sagai.ui.theme.themeShimmer
import kotlin.time.Duration.Companion.seconds

@Composable
fun NewSagaView(
    onBack: () -> Unit = {},
    onNavigate: (NavKey) -> Unit = {},
    viewModel: NewSagaViewModel = hiltViewModel(),
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val effect by viewModel.effect.collectAsStateWithLifecycle()
    val genderPlaceholders by viewModel.genderPlaceholders.collectAsStateWithLifecycle()
    var userInput by remember { mutableStateOf("") }
    val defaultCreationMessage = stringResource(R.string.saga_description_subtitle)

    LaunchedEffect(effect) {
        when (effect) {
            is Effect.Navigate -> onNavigate((effect as Effect.Navigate).key)
            else -> doNothing()
        }
    }

    val currentGenre = uiState.lockedSaga?.genre

    SagAITheme(
        genre = currentGenre,
    ) {
        val reasoningMessage =
            if (uiState.isSaving) {
                null
            } else if (uiState.phase == NewSagaScreenPhase.Suggestions) {
                uiState.statusMessage ?: defaultCreationMessage
            } else {
                uiState.statusMessage
            }

        with(sharedTransitionScope) {
            val surface = MaterialTheme.colorScheme.background
            val bottomAccent = morphingColor(duration = 3.seconds)

            Box(
                modifier =
                    Modifier
                        .background(surface)
                        .fillMaxSize()
                        .imePadding(),
            ) {
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .fillMaxHeight(0.25f)
                        .background(
                            fadeGradientBottom(bottomAccent),
                        ),
                )

                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .statusBarsPadding(),
                ) {
                    if (!uiState.isSaving) {
                        TopBarContent(
                            modifier = Modifier.fillMaxWidth(),
                            navigateBack = {
                                if (viewModel.onBackPressed()) {
                                    onBack()
                                }
                            },
                        )
                    }

                    Box(
                        modifier =
                            Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .animateContentSize(),
                    ) {
                        AnimatedContent(uiState.phase, transitionSpec = {
                            fadeIn(tween(700)) togetherWith
                                fadeOut(tween(1000, delayMillis = 200, easing = EaseIn))
                        }) { phase ->
                            when (phase) {
                                NewSagaScreenPhase.Suggestions -> {
                                    NewSagaSuggestionsContent(
                                        uiState = uiState,
                                        reasoningMessage = reasoningMessage,
                                        onEchoSelected = { echoInput ->
                                            userInput = echoInput
                                            viewModel.onIntent(NewSagaIntent.SubmitPrompt(echoInput))
                                        },
                                    )
                                }

                                NewSagaScreenPhase.Creation -> {
                                    NewSagaCreationContent(
                                        uiState = uiState,
                                        sharedTransitionScope = sharedTransitionScope,
                                        animatedVisibilityScope = animatedVisibilityScope,
                                        onIntent = viewModel::onIntent,
                                    )
                                }
                            }
                        }
                    }

                    if (uiState.showSaveButton) {
                        SaveSagaButton(
                            enabled = !uiState.isSaving,
                            onClick = { viewModel.onIntent(NewSagaIntent.SaveSaga) },
                        )
                    } else if (!uiState.isSaving) {
                        PromptBar(
                            value = userInput,
                            onValueChange = { userInput = it },
                            onSend = {
                                viewModel.onIntent(NewSagaIntent.SubmitPrompt(userInput))
                                userInput = ""
                            },
                            isLoading = uiState.isGenerating || uiState.isSaving,
                        )
                    }
                }
            }
        }

        OnboardingDialog(OnboardingType.CREATION_GUIDE)
    }
}

@Composable
private fun NewSagaSuggestionsContent(
    uiState: NewSagaUiState,
    reasoningMessage: String?,
    onEchoSelected: (String) -> Unit,
) {
    if (uiState.showEchoes) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
        ) {
            reasoningMessage?.let { message ->
                NewSagaReasoning(
                    message = message,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .reactiveShimmer(
                                true,
                                themeShimmer(),
                                repeatMode = RepeatMode.Restart,
                            ),
                )
            }

            uiState.error?.let { error ->
                NewSagaInlineError(error)
            }

            UniverseEchoesSection(
                echoes = uiState.universeEchoes,
                onEchoSelected = onEchoSelected,
                modifier = Modifier.weight(1f),
            )
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_spark),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier =
                        Modifier
                            .size(56.dp)
                            .gradientFill(Brush.verticalGradient(morphingGradient())),
                )

                reasoningMessage?.let { message ->
                    NewSagaWelcomeHero(message = message)
                }

                uiState.error?.let { error ->
                    NewSagaInlineError(error)
                }
            }
        }
    }
}

@Composable
private fun NewSagaWelcomeHero(
    message: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = message,
        style =
            MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                shadow = Shadow(Color.White, blurRadius = 15f),
                brush = Brush.verticalGradient(morphingGradient()),
            ),
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun NewSagaInlineError(error: String) {
    Text(
        text = error,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(8.dp),
        textAlign = TextAlign.Center,
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun NewSagaCreationContent(
    uiState: NewSagaUiState,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
    onIntent: (NewSagaIntent) -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            uiState.isSaving -> {
                val bookEntry =
                    uiState.libraryBooks.firstOrNull {
                        it.first.draft.id == uiState.lockedSaga?.id
                    }
                bookEntry?.let { entry ->
                    with(sharedTransitionScope) {
                        NewSagaBookFocus(
                            book = entry.first,
                            visualConfig = entry.second,
                            reasoning = uiState.statusMessage,
                            isOpened = false,
                            isLoading = true,
                            lockedCharacter = uiState.lockedCharacter,
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedContentKey = "new-saga-book-${entry.first.draft.id}",
                            showReasoning = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            uiState.showGenreLoader -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    GenreMemoriesLoader(
                        genresConfigs = uiState.genresVisuals ?: emptyList(),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    uiState.statusMessage?.let { message ->
                        NewSagaReasoning(
                            message = message,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                                    .reactiveShimmer(
                                        uiState.useShimmerReasoning,
                                        themeShimmer(),
                                        repeatMode = RepeatMode.Restart,
                                    ),
                        )
                    }
                }
            }

            uiState.libraryBooks.isNotEmpty() -> {
                LibraryPager(
                    books = uiState.libraryBooks,
                    lockedSaga = uiState.lockedSaga,
                    lockedCharacter = uiState.lockedCharacter,
                    isGenerating = uiState.isGenerating,
                    isLoadingMore = uiState.isLoadingMore,
                    hasMoreGenres = uiState.hasMoreGenres,
                    onLoadMore = { onIntent(NewSagaIntent.LoadMore) },
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                    onIntent = onIntent,
                )
            }

            uiState.statusMessage != null -> {
                NewSagaReasoning(
                    message = uiState.statusMessage,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .reactiveShimmer(
                                uiState.useShimmerReasoning,
                                themeShimmer(),
                                repeatMode = RepeatMode.Restart,
                            ),
                )
            }
        }

        uiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(8.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SaveSagaButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val buttonShape = sagaShape()
    val color = MaterialTheme.colorScheme.primary
    Button(
        onClick = onClick,
        modifier =
            Modifier
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .dropShadow(buttonShape) {
                    this.color = color
                    this.radius = 5f
                    this.spread = 5f
                }.fillMaxWidth(),
        shape = buttonShape,
        enabled = enabled,
    ) {
        Text(stringResource(R.string.save_saga))
    }
}

private val NewSagaUiState.showGenreLoader: Boolean
    get() = phase == NewSagaScreenPhase.Creation && isGenerating && libraryBooks.isEmpty() && !isSaving

private val NewSagaUiState.showEchoes: Boolean
    get() = phase == NewSagaScreenPhase.Suggestions && universeEchoes.isNotEmpty()

private val NewSagaUiState.showSaveButton: Boolean
    get() = isReadyToSave && lockedSaga != null && lockedCharacter != null

private val NewSagaUiState.useShimmerReasoning: Boolean
    get() = isGenerating || isSaving || isLoadingMore

@Composable
fun TopBarContent(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
) {
    Row(
        modifier = modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = navigateBack) {
            Icon(
                painter = painterResource(R.drawable.ic_back_left),
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = stringResource(R.string.new_saga_title),
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
fun PromptBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean,
) {
    val shape = MaterialTheme.shapes.extraLarge
    val themeBrush = Brush.horizontalGradient(morphingGradient())
    val primaryColor = MaterialTheme.colorScheme.primary

    Row(
        modifier =
            Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .dropShadow(shape) {
                    color = primaryColor
                    radius = 15f
                    spread = 1f
                    brush = themeBrush
                }.border(1.dp, MaterialTheme.colorScheme.onBackground.gradientFade(), shape)
                .background(MaterialTheme.colorScheme.background, shape)
                .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            textStyle =
                MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Light,
                ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            enabled = !isLoading,
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        stringResource(R.string.saga_description_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                }
                innerTextField()
            },
        )

        val iconBackgroundColor by animateColorAsState(
            if (value.isNotBlank() && !isLoading) {
                MaterialTheme.colorScheme.primary
            } else {
                Color.Transparent
            },
            label = "iconBackground",
        )

        val brush =
            if (isLoading) {
                Brush.verticalGradient(holographicGradient)
            } else {
                MaterialTheme.colorScheme.onBackground.solidGradient()
            }

        IconButton(
            onClick = onSend,
            enabled = value.isNotBlank() && !isLoading,
            modifier =
                Modifier
                    .background(iconBackgroundColor, CircleShape)
                    .size(32.dp)
                    .padding(8.dp)
                    .gradientFill(brush),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_send),
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
