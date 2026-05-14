@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.newsaga.ui
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.navigation3.runtime.NavKey
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.model.LocalGenreVisualConfig
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.ui.presentation.AgenticAction
import com.ilustris.sagai.features.newsaga.ui.presentation.Effect
import com.ilustris.sagai.features.newsaga.ui.presentation.NewSagaViewModel
import com.ilustris.sagai.features.onboarding.data.OnboardingType
import com.ilustris.sagai.features.onboarding.ui.OnboardingDialog
import com.ilustris.sagai.ui.animations.chromaticAberration
import com.ilustris.sagai.ui.animations.divineAura
import com.ilustris.sagai.ui.components.CosmicBook
import com.ilustris.sagai.ui.components.GenreMemoriesLoader
import com.ilustris.sagai.ui.theme.FluidGradient
import com.ilustris.sagai.ui.theme.fadedGradientTopAndBottom
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.levitate
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.sagaShape
import com.ilustris.sagai.ui.theme.solidGradient

@Composable
fun NewSagaView(
    onBack: () -> Unit = {},
    onNavigate: (NavKey) -> Unit = {},
    viewModel: NewSagaViewModel = hiltViewModel(),
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
) {
    val isReadyToSave by viewModel.isReadyToSave.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val effect by viewModel.effect.collectAsStateWithLifecycle()
    val lockedSaga by viewModel.lockedSaga.collectAsStateWithLifecycle()
    val lockedCharacter by viewModel.lockedCharacter.collectAsStateWithLifecycle()
    val currentAgentMessage by viewModel.currentAgentMessage.collectAsStateWithLifecycle()
    val isAgentLoading by viewModel.isAgentLoading.collectAsStateWithLifecycle()
    val currentConfig by viewModel.currentConfig.collectAsStateWithLifecycle()
    val genderPlaceholders by viewModel.genderPlaceholders.collectAsStateWithLifecycle()
    val universeEchoes by viewModel.universeEchoes.collectAsStateWithLifecycle()
    val isEchoLoading by viewModel.isEchoLoading.collectAsStateWithLifecycle()
    var userInput by remember { mutableStateOf("") }
    val genreConfigs by viewModel.genresVisuals.collectAsStateWithLifecycle()
    val libraryBooks by viewModel.libraryBooks.collectAsStateWithLifecycle()
    val uiError by viewModel.uiError.collectAsStateWithLifecycle()

    LaunchedEffect(effect) {
        when (effect) {
            is Effect.Navigate -> {
                onNavigate((effect as Effect.Navigate).key)
            }

            else -> {
                doNothing()
            }
        }
    }

    CompositionLocalProvider(
        LocalGenreVisualConfig provides currentConfig,
        LocalGenderPlaceholders provides genderPlaceholders,
    ) {
        val currentPalette = lockedSaga?.genre?.colorPalette() ?: holographicGradient

        AnimatedContent(isEchoLoading) {
            if (it) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    with(sharedTransitionScope) {
                        Image(
                            painterResource(R.drawable.ic_spark),
                            contentDescription = "Loading",
                            modifier =
                                Modifier
                                    .size(100.dp)
                                    .sharedElement(
                                        rememberSharedContentState("spark_icon"),
                                        animatedVisibilityScope,
                                    ).gradientFill(Brush.verticalGradient(holographicGradient))
                                    .reactiveShimmer(true)
                                    .levitate()
                                    .divineAura(),
                        )
                    }
                }
            } else {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .imePadding(),
                ) {
                    AnimatedContent(
                        currentPalette,
                        label = "GradientTransition",
                        modifier = Modifier.fillMaxSize(),
                        transitionSpec = {
                            fadeIn(tween(1000, easing = EaseIn)) togetherWith
                                fadeOut(
                                    tween(
                                        200,
                                        easing = FastOutSlowInEasing,
                                    ),
                                )
                        },
                    ) {
                        FluidGradient(
                            colors = it,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    // 2. Gradient Overlay
                    Box(
                        modifier =
                            Modifier
                                .background(fadedGradientTopAndBottom())
                                .fillMaxSize(),
                    )

                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .statusBarsPadding(),
                    ) {
                        AnimatedVisibility(!isSaving) {
                            TopBarContent(
                                modifier = Modifier.fillMaxWidth(),
                                navigateBack = { onBack() },
                            )
                        }

                        AnimatedVisibility(libraryBooks.isNotEmpty() && isSaving.not()) {
                            val filteredBooks =
                                if (isSaving) {
                                    libraryBooks.filter { it.first.draft.id == lockedSaga?.id }
                                } else {
                                    libraryBooks
                                }
                            LibraryPager(
                                books = filteredBooks,
                                lockedSaga = lockedSaga,
                                lockedCharacter = lockedCharacter,
                                isAgentLoading = isAgentLoading || isSaving,
                                currentAgentMessage = currentAgentMessage,
                                onAction = viewModel::onAgenticAction,
                            )
                        }

                        AnimatedVisibility(
                            isSaving,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            enter =
                                fadeIn(tween(800)) +
                                    scaleIn(
                                        tween(
                                            400,
                                            easing = FastOutLinearInEasing,
                                        ),
                                    ),
                            exit = scaleOut(),
                        ) {
                            val actualBook =
                                libraryBooks.firstOrNull { it.first.draft.id == lockedSaga?.id }
                            actualBook?.let {
                                CosmicBook(
                                    book = it.first,
                                    visualConfig = it.second,
                                    isOpened = true,
                                    isLoading = true,
                                    reasoning = currentAgentMessage,
                                    onToggle = {},
                                    onAction = {},
                                    modifier =
                                        Modifier
                                            .padding(50.dp)
                                            .levitate()
                                            .divineAura()
                                            .chromaticAberration(),
                                )
                            }
                        }

                        AnimatedVisibility(
                            isAgentLoading && libraryBooks.isEmpty(),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(.6f),
                        ) {
                            GenreMemoriesLoader(
                                isLoading = isAgentLoading,
                                message = emptyString(),
                                genresConfigs = genreConfigs ?: emptyList(),
                            )
                        }

                        AnimatedContent(currentAgentMessage, transitionSpec = {
                            fadeIn() + slideInVertically { it / 2 } togetherWith fadeOut() + slideOutVertically { -it / 2 }
                        }, modifier = Modifier.fillMaxWidth()) { message ->
                            message?.let {
                                Text(
                                    text = it,
                                    style =
                                        MaterialTheme.typography.bodyMedium.copy(
                                            shadow =
                                                Shadow(
                                                    Color.White,
                                                    blurRadius = 10f,
                                                ),
                                        ),
                                    modifier =
                                        Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth()
                                            .levitate(isAgentLoading),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }

                        uiError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth(),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            AnimatedVisibility(
                                universeEchoes.isNotEmpty() && isAgentLoading.not(),
                                enter = fadeIn(tween(800)) + slideInVertically { it },
                                exit = fadeOut(tween(800)) + slideOutVertically { it },
                            ) {
                                UniverseEchoesSection(universeEchoes, {
                                    userInput = it
                                    viewModel.onAgenticAction(AgenticAction.SubmitPrompt(it))
                                })
                            }

                            AnimatedContent(
                                targetState = (isReadyToSave && lockedSaga != null && lockedCharacter != null) || isSaving,
                                label = "BottomControl",
                            ) { ready ->
                                if (ready) {
                                    AnimatedVisibility(isSaving.not()) {
                                        lockedSaga?.genre
                                        val buttonShape = sagaShape()
                                        val color =
                                            MaterialTheme.colorScheme.primary
                                                ?: MaterialTheme.colorScheme.primary
                                        val contentColor =
                                            MaterialTheme.colorScheme.secondary
                                                ?: MaterialTheme.colorScheme.onPrimary
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
                                            Text(
                                                stringResource(R.string.save_saga),
                                            )
                                        }
                                    }
                                } else {
                                    PromptBar(
                                        value = userInput,
                                        onValueChange = { newValue: String ->
                                            userInput = newValue
                                        },
                                        onSend = {
                                            viewModel.onAgenticAction(
                                                AgenticAction.SubmitPrompt(
                                                    userInput,
                                                ),
                                            )
                                            userInput = ""
                                        },
                                        isLoading = isAgentLoading || isSaving,
                                        genre = lockedSaga?.genre,
                                    )
                                }
                            }
                        }
                    }
                }
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
    val shape = sagaShape() ?: MaterialTheme.shapes.extraLarge
    val themeBrush =
        Brush.horizontalGradient(genre?.colorPalette() ?: holographicGradient)

    val primaryColor = MaterialTheme.colorScheme.primary ?: MaterialTheme.colorScheme.primary
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
                .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_spark),
            contentDescription = "Prompt",
            tint = MaterialTheme.colorScheme.onBackground,
            modifier =
                Modifier
                    .size(24.dp)
                    .gradientFill(Brush.verticalGradient(holographicGradient)),
        )

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
            if (value.isNotBlank() && !isLoading) {
                MaterialTheme.colorScheme.primary
            } else {
                Color.Transparent
            },
            label = "iconBackground",
        )

        val brush =
            if (isLoading) {
                Brush.verticalGradient(
                    holographicGradient,
                )
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
                )
            }
        }
    }
}
