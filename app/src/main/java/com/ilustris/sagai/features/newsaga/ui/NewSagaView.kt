@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.newsaga.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.navigation.NavHostController
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.model.LocalGenreVisualConfig
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.newsaga.data.model.resolveIconColor
import com.ilustris.sagai.features.newsaga.ui.presentation.AgenticAction
import com.ilustris.sagai.features.newsaga.ui.presentation.Effect
import com.ilustris.sagai.features.newsaga.ui.presentation.NewSagaViewModel
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import com.ilustris.sagai.features.onboarding.ui.OnboardingDialog
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.components.StarryLoader
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.navigation.navigateToRoute
import com.ilustris.sagai.ui.theme.fadeGradientTop
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.levitate
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shape
import com.ilustris.sagai.ui.theme.shimmerize
import com.ilustris.sagai.ui.theme.themeShimmer

@Composable
fun NewSagaView(
    navHostController: NavHostController,
    viewModel: NewSagaViewModel = hiltViewModel(),
) {
    val feed by viewModel.feed.collectAsStateWithLifecycle()
    val isReadyToSave by viewModel.isReadyToSave.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val loadingMessage by viewModel.loadingMessage.collectAsStateWithLifecycle()
    val effect by viewModel.effect.collectAsStateWithLifecycle()
    val lockedSaga by viewModel.lockedSaga.collectAsStateWithLifecycle()
    val lockedCharacter by viewModel.lockedCharacter.collectAsStateWithLifecycle()
    val currentAgentMessage by viewModel.currentAgentMessage.collectAsStateWithLifecycle()
    val isAgentLoading by viewModel.isAgentLoading.collectAsStateWithLifecycle()
    val currentConfig by viewModel.currentConfig.collectAsStateWithLifecycle()
    var userInput by remember { mutableStateOf("") }
    val listState = rememberLazyStaggeredGridState()

    LaunchedEffect(feed.size) {
        if (feed.isNotEmpty()) {
            listState.animateScrollToItem(feed.size - 1)
        }
    }

    LaunchedEffect(effect) {
        when (effect) {
            is Effect.Navigate -> {
                navHostController.navigateToRoute(
                    (effect as Effect.Navigate).route,
                    arguments = (effect as Effect.Navigate).arguments,
                    popUpToRoute = Routes.NEW_SAGA,
                )
            }

            else -> {
                doNothing()
            }
        }
    }

    CompositionLocalProvider(
        LocalGenreVisualConfig provides currentConfig,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Cosmic Background
            val primaryColor =
                lockedSaga?.genre?.resolveColor() ?: MaterialTheme.colorScheme.primary
            StarryTextPlaceholder(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .reactiveShimmer(isAgentLoading || isSaving, primaryColor.shimmerize()),
            )

            // 2. Gradient Overlay
            Box(
                modifier =
                    Modifier
                        .background(fadeGradientTop())
                        .fillMaxSize(),
            )

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
            ) {
                // 3. Top Bar
                TopBarContent(
                    modifier = Modifier.fillMaxWidth(),
                    navigateBack = { navHostController.popBackStack() },
                )

                SharedTransitionLayout {
                    CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                        LazyVerticalStaggeredGrid(
                            state = listState,
                            columns = StaggeredGridCells.Fixed(2),
                            verticalItemSpacing = 8.dp,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .imePadding(),
                        ) {
                            item(key = "agent-messages", span = StaggeredGridItemSpan.FullLine) {
                                AnimatedContent(currentAgentMessage, transitionSpec = {
                                    fadeIn(tween(1000, easing = FastOutSlowInEasing)) +
                                        slideInVertically(
                                            tween(
                                                200,
                                                easing = EaseIn,
                                            ),
                                        ) { +it } togetherWith
                                        slideOutVertically { -it } +
                                        fadeOut(tween(1350, easing = FastOutSlowInEasing))
                                }) { message ->
                                    message?.let {
                                        Text(
                                            text = it,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier =
                                                Modifier
                                                    .background(MaterialTheme.colorScheme.background)
                                                    .padding(16.dp)
                                                    .fillMaxWidth()
                                                    .levitate(isAgentLoading)
                                                    .reactiveShimmer(
                                                        isAgentLoading,
                                                        themeShimmer(),
                                                        repeatMode = RepeatMode.Restart,
                                                    ),
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                }
                            }

                            feed.forEach {
                                it.Render(
                                    scope = this@LazyVerticalStaggeredGrid,
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    lockedSaga = lockedSaga,
                                    lockedCharacter = lockedCharacter,
                                    onAction = viewModel::onAgenticAction,
                                )
                            }

                            item(span = StaggeredGridItemSpan.FullLine) {
                                Spacer(Modifier.size(100.dp))
                            }
                        }
                    }
                }
            }
            Box(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .imePadding()
                        .fillMaxWidth(),
            ) {
                AnimatedContent(
                    targetState = isReadyToSave && lockedSaga != null && lockedCharacter != null,
                    label = "BottomControl",
                ) { ready ->
                    if (ready) {
                        val genre = lockedSaga?.genre
                        val buttonShape = lockedSaga?.genre?.shape() ?: MaterialTheme.shapes.large
                        val color = genre?.resolveColor() ?: MaterialTheme.colorScheme.primary
                        val contentColor =
                            genre?.resolveIconColor() ?: MaterialTheme.colorScheme.onPrimary

                        Button(
                            onClick = { viewModel.onAgenticAction(AgenticAction.SaveSaga) },
                            modifier =
                                Modifier
                                    .padding(32.dp)
                                    .dropShadow(
                                        buttonShape,
                                    ) {
                                        this.color = color
                                        this.radius = 5f
                                        this.spread = 5f
                                    }.fillMaxWidth(),
                            shape = buttonShape,
                            enabled = !isSaving,
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = color,
                                    contentColor = contentColor,
                                ),
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            } else {
                                Text(
                                    stringResource(R.string.save_saga),
                                )
                            }
                        }
                    } else {
                        PromptBar(
                            value = userInput,
                            onValueChange = { userInput = it },
                            onSend = {
                                viewModel.onAgenticAction(AgenticAction.SubmitPrompt(userInput))
                                userInput = ""
                            },
                            isLoading = isAgentLoading || isSaving,
                            genre = lockedSaga?.genre,
                        )
                    }
                }
            }
        }

        lockedSaga?.let { saga ->
            loadingMessage?.let {
                StarryLoader(
                    true,
                    it,
                    textStyle =
                        MaterialTheme.typography.labelMedium.copy(
                            shadow =
                                Shadow(
                                    saga.genre.resolveColor(),
                                    blurRadius = 5f,
                                ),
                        ),
                    brushColors = saga.genre.colorPalette(),
                )
            }
        }
    }

    OnboardingDialog(OnboardingType.CREATION_GUIDE)
}

@Composable
fun TopBarContent(
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
) {
    Row(
        modifier =
            modifier
                .padding(16.dp),
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
    genre: Genre?,
) {
    val shape = genre?.shape() ?: MaterialTheme.shapes.extraLarge
    val themeBrush =
        Brush.horizontalGradient(genre?.colorPalette() ?: holographicGradient)

    val primaryColor = genre?.resolveColor() ?: MaterialTheme.colorScheme.primary
    Row(
        modifier =
            Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .dropShadow(shape) {
                    this.color = primaryColor
                    this.radius = 10f
                    this.spread = 5f
                    this.brush = themeBrush
                }.border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f), shape)
                .background(MaterialTheme.colorScheme.background, shape)
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            textStyle =
                MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Light,
                ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            enabled = isLoading.not(),
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
            if (value.isNotBlank()) {
                MaterialTheme.colorScheme.primary
            } else {
                Color.Transparent
            },
            label = "iconBackground",
        )

        IconButton(
            onClick = onSend,
            enabled = value.isNotBlank() && !isLoading,
            modifier =
                Modifier
                    .background(iconBackgroundColor, CircleShape)
                    .size(32.dp)
                    .padding(8.dp),
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
                )
            }
        }
    }
}
