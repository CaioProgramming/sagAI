package com.ilustris.sagai.features.debug.ui

import MessageStatus
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.model.ImageType
import com.ilustris.sagai.core.ai.model.LocalGenreVisualConfig
import com.ilustris.sagai.features.act.data.model.BookGenerationUiState
import com.ilustris.sagai.features.imagegeneration.model.ImageGenerationUiState
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.manager.BackgroundTask
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeAction
import com.ilustris.sagai.features.saga.chat.ui.components.ChatBubble
import com.ilustris.sagai.features.saga.chat.ui.components.ChatInputView
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.NarrativeBackgroundBanner
import com.ilustris.sagai.features.saga.detail.ui.sagaHeaderComponent
import com.ilustris.sagai.features.saga.milestone.presentation.MilestoneUiState
import com.ilustris.sagai.features.saga.milestone.ui.toStoryBeat
import com.ilustris.sagai.ui.animations.comicExtrude
import com.ilustris.sagai.ui.components.StarryLoader
import com.ilustris.sagai.ui.components.WordArtText
import com.ilustris.sagai.ui.components.island.AdvanceIslandContent
import com.ilustris.sagai.ui.components.island.BookGenerationIslandContent
import com.ilustris.sagai.ui.components.island.ImageGenerationIslandContent
import com.ilustris.sagai.ui.components.island.ObjectiveIslandContent
import com.ilustris.sagai.ui.components.stylisedText
import com.ilustris.sagai.ui.genre.recap.GenreRecapCard
import com.ilustris.sagai.ui.genre.recap.RecapCard
import com.ilustris.sagai.ui.genre.recap.RecapProgress
import com.ilustris.sagai.ui.genre.recap.RecapStat
import com.ilustris.sagai.ui.genre.surface.GenreStoryIntroduction
import com.ilustris.sagai.ui.genre.surface.GenreStoryLoading
import com.ilustris.sagai.ui.genre.surface.GenreStoryNotice
import com.ilustris.sagai.ui.genre.surface.GenreStorySurface
import com.ilustris.sagai.ui.genre.surface.StoryBeatAction
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.fadeGradientTop
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.rememberRotatingBorderAngle
import com.ilustris.sagai.ui.theme.rememberVectorShape
import com.ilustris.sagai.ui.theme.rotatingGradientBorder
import com.ilustris.sagai.ui.theme.sagaBrush
import com.ilustris.sagai.ui.theme.sagaShape
import com.ilustris.sagai.ui.theme.themeVfx
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * Dev-only sandbox to validate design-system primitives and layouts in a realistic context.
 * Features a genre pager, live theme updates, realistic headers, mini-chat previews,
 * and access to global UI debug tools like the Island and Starry Loader.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DesignSystemPreviewView(
    onBack: () -> Unit = {},
    viewModel: DesignSystemViewModel = hiltViewModel(),
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
) {
    val pagerState = rememberPagerState { Genre.entries.size }
    val genre = Genre.entries[pagerState.currentPage]
    var showStarryLoaderPreview by remember { mutableStateOf(false) }
    var milestonePreviewKind by remember { mutableStateOf<MilestonePreviewKind?>(null) }

    LaunchedEffect(showStarryLoaderPreview) {
        if (showStarryLoaderPreview) {
            delay(10.seconds)
            showStarryLoaderPreview = false
        }
    }

    Box(Modifier.fillMaxSize()) {
        AnimatedContent(genre, transitionSpec = {
            fadeIn(tween(300)) togetherWith fadeOut(tween(800))
        }) {
            SagAITheme(genre = it) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                ) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        // Genre Pager

                        // Realistic Saga Header
                        val config = LocalGenreVisualConfig.current
                        val mockSaga =
                            remember(genre, config) {
                                DesignSystemMocks.mockSaga(genre, config?.imageUrl ?: "")
                            }
                        sagaHeaderComponent(
                            saga = mockSaga,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                            onAction = {},
                        )

                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                        ) {
                            // Mini Chat Preview
                            MiniChatPreview(genre, sharedTransitionScope, animatedVisibilityScope)

                            // Advance Button Simulation
                            AdvanceSimulation()

                            // Chat Input Preview
                            ChatInputPreview(genre)

                            // Config Info Box
                            VisualConfigInfo()

                            // Recap card, both states — otherwise the only way to see one is to
                            // actually finish a saga in this genre.
                            SampleLabel("RECAP CARD")
                            RecapCardSample(genre)

                            HorizontalDivider(Modifier.padding(vertical = 16.dp))

                            // Legacy Samples (kept for detailed primitive validation)
                            SampleLabel("PRIMITIVES VALIDATION")
                            HeaderFontSample(genre)
                            BubbleShapeSample(genre)
                            GlowBorderSample(genre)
                            RotatingStrokeSample(genre)
                            ShaderFilterSample(genre)

                            Spacer(Modifier.height(48.dp))
                        }
                    }

                    StarryLoader(
                        showStarryLoaderPreview,
                        stringResource(R.string.settings_test_starry_loader_message),
                        subtitle = stringResource(R.string.settings_test_starry_loader_subtitle),
                    )

                    // Lets you flip through the Milestone screen's states with mock content —
                    // no need to actually play a saga up to a real milestone to see how it looks.
                    // A Dialog (not an inline overlay) so it gets its own solid background and an
                    // easy way out — it's a preview, it shouldn't lock the screen like the real one.
                    milestonePreviewKind?.let { kind ->
                        Dialog(
                            onDismissRequest = { milestonePreviewKind = null },
                            properties = DialogProperties(usePlatformDefaultWidth = false),
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.background,
                            ) {
                                Box(Modifier.fillMaxSize()) {
                                    // Every kind now goes through the same GenreStorySurface the
                                    // real screen uses, so what shows up here is not a lookalike —
                                    // it is the production composable with mock content. `genre` is
                                    // passed explicitly rather than left to LocalSagaGenre so the
                                    // pager can force one the ambient theme hasn't switched to yet.
                                    when (kind) {
                                        // The real screen advances out of Loading on its own once
                                        // generation finishes. Nothing drives that here, so the
                                        // preview adds its own "Next" without touching the
                                        // production composable's contract.
                                        MilestonePreviewKind.LOADING -> {
                                            Column(Modifier.fillMaxSize()) {
                                                Box(Modifier.fillMaxWidth().weight(1f)) {
                                                    GenreStoryLoading(
                                                        it.name,
                                                        message = "Weaving the next thread of your story...",
                                                        genre = genre,
                                                    )
                                                }
                                                Button(
                                                    onClick = { milestonePreviewKind = MilestonePreviewKind.EVENT },
                                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                                ) {
                                                    Text("Next")
                                                }
                                            }
                                        }

                                        MilestonePreviewKind.EVENT -> {
                                            GenreStorySurface(
                                                beat =
                                                    MilestoneUiState
                                                        .ClosureStep(
                                                            DesignSystemMocks.mockNewEventMilestone(genre),
                                                            stepIndex = 1,
                                                            stepTotal = 3,
                                                        ).toStoryBeat(
                                                            sagaId = 1,
                                                            sagaTitle = "Ashes of the Old Guard",
                                                            coverImage = null,
                                                            actCoverImages = emptyList(),
                                                            bookGenerationState = BookGenerationUiState.Idle,
                                                            onContinue = { milestonePreviewKind = MilestonePreviewKind.CHAPTER },
                                                            onNavigate = {},
                                                            onGenerateBook = {},
                                                        ),
                                                genre = genre,
                                            )
                                        }

                                        MilestonePreviewKind.CHAPTER -> {
                                            GenreStorySurface(
                                                beat =
                                                    MilestoneUiState
                                                        .ClosureStep(
                                                            DesignSystemMocks.mockChapterFinishedMilestone(genre),
                                                            stepIndex = 2,
                                                            stepTotal = 3,
                                                        ).toStoryBeat(
                                                            sagaId = 1,
                                                            sagaTitle = "Ashes of the Old Guard",
                                                            coverImage = MOCK_COVER_URL,
                                                            actCoverImages = emptyList(),
                                                            bookGenerationState = BookGenerationUiState.Idle,
                                                            onContinue = { milestonePreviewKind = MilestonePreviewKind.ACT },
                                                            onNavigate = {},
                                                            onGenerateBook = {},
                                                        ),
                                                genre = genre,
                                            )
                                        }

                                        MilestonePreviewKind.ACT -> {
                                            GenreStorySurface(
                                                beat =
                                                    MilestoneUiState
                                                        .ClosureStep(
                                                            DesignSystemMocks.mockActFinishedMilestone(),
                                                            stepIndex = 3,
                                                            stepTotal = 3,
                                                        ).toStoryBeat(
                                                            sagaId = 1,
                                                            sagaTitle = "Ashes of the Old Guard",
                                                            coverImage = null,
                                                            actCoverImages = DesignSystemMocks.mockActCoverImages(),
                                                            bookGenerationState = BookGenerationUiState.Idle,
                                                            onContinue = { milestonePreviewKind = MilestonePreviewKind.INTRO },
                                                            onNavigate = {},
                                                            onGenerateBook = {},
                                                        ),
                                                genre = genre,
                                            )
                                        }

                                        MilestonePreviewKind.INTRO -> {
                                            GenreStoryIntroduction(
                                                beat =
                                                    DesignSystemMocks.mockIntroductionMilestone().toStoryBeat(
                                                        sagaTitle = "Ashes of the Old Guard",
                                                        onContinue = { milestonePreviewKind = MilestonePreviewKind.ERROR },
                                                    ),
                                                genre = genre,
                                            )
                                        }

                                        // Never previewable before this refactor, and now the one
                                        // state whose genre treatment nobody has ever laid eyes on.
                                        MilestonePreviewKind.ERROR -> {
                                            GenreStoryNotice(
                                                title = stringResource(R.string.milestone_error_title),
                                                message = "The story lost its thread while closing this chapter.",
                                                genre = genre,
                                                action =
                                                    StoryBeatAction(
                                                        id = "retry",
                                                        label = stringResource(R.string.try_again),
                                                        onClick = { milestonePreviewKind = null },
                                                    ),
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { milestonePreviewKind = null },
                                        modifier =
                                            Modifier
                                                .align(Alignment.TopStart)
                                                .statusBarsPadding()
                                                .padding(16.dp),
                                    ) {
                                        Text(
                                            "✕",
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = MaterialTheme.colorScheme.onBackground,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        GenrePager(pagerState)

        Row(
            Modifier
                .align(Alignment.TopCenter)
                .background(fadeGradientTop())
                .statusBarsPadding()
                .padding(vertical = 8.dp)
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = {
                    onBack()
                },
                modifier =
                    Modifier
                        .background(
                            MaterialTheme.colorScheme.background.copy(alpha = .3f),
                            CircleShape,
                        ).size(24.dp)
                        .padding(4.dp),
            ) {
                Icon(
                    painterResource(R.drawable.ic_back_left),
                    contentDescription = stringResource(R.string.back_button_description),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Text(
                stringResource(R.string.design_system_preview_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )

            IconButton(
                onClick = {
                    showStarryLoaderPreview = true
                },
                modifier =
                    Modifier
                        .background(
                            MaterialTheme.colorScheme.background.copy(alpha = .3f),
                            CircleShape,
                        ).size(24.dp)
                        .padding(4.dp),
            ) {
                Icon(
                    painterResource(R.drawable.ic_full_spark),
                    null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            IconButton(
                onClick = { milestonePreviewKind = MilestonePreviewKind.LOADING },
                modifier =
                    Modifier
                        .background(
                            MaterialTheme.colorScheme.background.copy(alpha = .3f),
                            CircleShape,
                        ).size(24.dp)
                        .padding(4.dp),
            ) {
                Icon(
                    painterResource(R.drawable.ic_spark),
                    contentDescription = "Preview Milestone screens",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            IslandTestIconButton(viewModel, genre)
        }
    }
}

/** Stand-in chapter art, so the cover slot in each genre's treatment has something to show. */
private const val MOCK_COVER_URL =
    "https://i.pinimg.com/564x/0a/92/7d/0a927df0b8a6a12a5276e03882775739.jpg"

private enum class MilestonePreviewKind { LOADING, EVENT, CHAPTER, ACT, INTRO, ERROR }

@Composable
private fun BoxScope.GenrePager(pagerState: androidx.compose.foundation.pager.PagerState) {
    HorizontalPager(
        state = pagerState,
        modifier =
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(100.dp),
    ) { page ->
        val g = Genre.entries[page]
        val isSelected = pagerState.currentPage == page
        val title = stringResource(g.title)
        val shadowAlpha by animateFloatAsState(
            if (isSelected) 0.2f else 0.05f,
            label = "",
        )
        SagAITheme(g) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    painterResource(g.icon),
                    title,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier =
                        Modifier
                            .size(32.dp)
                            .gradientFill(
                                sagaBrush(),
                            ).reactiveShimmer(isSelected)
                            .themeVfx(isSelected),
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun MiniChatPreview(
    genre: Genre,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
) {
    val config = LocalGenreVisualConfig.current
    val mockMetadata =
        remember(genre, config) {
            DesignSystemMocks.mockSagaMetadata(genre, config?.imageUrl ?: "")
        }
    val npc = mockMetadata.characters.first()
    val messages =
        remember(genre) {
            listOf(
                DesignSystemMocks.mockMessageContent(
                    1,
                    "Welcome to the sandbox. Here you can see how components behave in different themes.",
                    SenderType.CHARACTER,
                ),
                DesignSystemMocks.mockMessageContent(
                    2,
                    "This looks really smooth. The colors and shapes update instantly!",
                    SenderType.USER,
                ),
                DesignSystemMocks.mockMessageContent(
                    3,
                    "Exactly. Try switching genres using the pager above.",
                    SenderType.CHARACTER,
                    npc,
                ),
                DesignSystemMocks.mockMessageContent(
                    4,
                    "This message shows the rotating border!",
                    SenderType.CHARACTER,
                    npc,
                    MessageStatus.LOADING,
                ),
            )
        }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SampleLabel("CHAT PREVIEW")
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.3f))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            messages.forEach { msg ->
                ChatBubble(
                    messageContent = msg,
                    mainCharacter = null,
                    characters = mockMetadata.characters,
                    wikis = emptyList(),
                    genre = genre,
                    flatEvents = emptyList(),
                    canAnimate = false,
                    messageEffectsEnabled = true,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedVisibilityScope = animatedVisibilityScope,
                )
            }
        }
    }
}

@Composable
private fun AdvanceSimulation() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SampleLabel("ADVANCE TRIGGER PREVIEW")
        NarrativeBackgroundBanner(
            task = BackgroundTask.ClosingScene,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ChatInputPreview(genre: Genre) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SampleLabel("CHAT INPUT PREVIEW")
        val config = LocalGenreVisualConfig.current
        val mockMetadata =
            remember(genre, config) {
                DesignSystemMocks.mockSagaMetadata(genre, config?.imageUrl ?: "")
            }
        ChatInputView(
            content = mockMetadata,
            characters = mockMetadata.characters,
            isGenerating = false,
            modifier = Modifier.fillMaxWidth(),
            inputField = TextFieldValue("Exploring the design system..."),
            sendType = SenderType.USER,
            onSendMessage = {},
            onUpdateInput = {},
            onUpdateSender = {},
            onSelectCharacter = {},
            onRequestAudio = {},
            onStopGeneration = {},
            suggestions = emptyList(),
            typoFix = null,
        )
    }
}

@Composable
private fun VisualConfigInfo() {
    val config = LocalGenreVisualConfig.current ?: return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SampleLabel("REMOTE VISUAL CONFIG")
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ConfigRow("Primary", config.primaryColor)
                ConfigRow("Icon", config.iconColor)
                ConfigRow("Corner Size", "${config.cornerSizeDp}dp")
                ConfigRow(
                    "Vibration",
                    if (config.vibrationPattern.isEmpty()) "Default" else "Custom (${config.vibrationPattern.size} steps)",
                )
                ConfigRow(
                    "Selective Highlight",
                    if (config.selectiveHighlight != null) "Active" else "Off",
                )
                ConfigRow("Shader Effects", if (config.shaderParams != null) "Active" else "Off")
                ConfigRow("Custom Font", if (config.headerFontUrl.isNotEmpty()) "Active" else "Off")
            }
        }
    }
}

@Composable
private fun ConfigRow(
    label: String,
    value: String,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.alpha(0.6f))
        Text(value, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
private fun IslandTestIconButton(
    viewModel: DesignSystemViewModel,
    genre: Genre,
) {
    var showIslandMenu by remember { mutableStateOf(false) }
    val objectiveSample = stringResource(R.string.island_test_objective_sample)
    val processingSample = stringResource(R.string.island_test_advance_processing_sample)
    val bookSagaTitle = stringResource(R.string.island_test_book_generation_saga_title)
    val bookActTitle = stringResource(R.string.island_test_book_generation_act_title)
    val bookReasoning = stringResource(R.string.island_test_book_generation_reasoning)
    val imageLabel = stringResource(R.string.island_test_image_generation_label)
    val imageReasoning = stringResource(R.string.island_test_image_generation_reasoning)
    val imageFallbackPrompt = stringResource(R.string.island_test_image_fallback_prompt)

    Box {
        IconButton(
            onClick = { showIslandMenu = true },
            modifier =
                Modifier
                    .background(
                        MaterialTheme.colorScheme.background.copy(alpha = .3f),
                        CircleShape,
                    ).size(24.dp)
                    .padding(4.dp),
        ) {
            Icon(painterResource(R.drawable.ic_cosmos), "Test Island")
        }

        DropdownMenu(
            showIslandMenu,
            onDismissRequest = { showIslandMenu = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.island_test_objective)) },
                onClick = {
                    showIslandMenu = false
                    viewModel.testIsland(
                        ObjectiveIslandContent(
                            titleRes = R.string.current_objective,
                            objective = objectiveSample,
                            genre = genre,
                            progress = 0.4f,
                        ),
                    )
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.island_test_advance_idle)) },
                onClick = {
                    showIslandMenu = false
                    viewModel.testIsland(
                        AdvanceIslandContent(
                            action = NarrativeAction.CreateAct,
                            reasoning = null,
                            isProcessing = false,
                            genre = genre,
                            onAction = {},
                        ),
                    )
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.island_test_advance_processing)) },
                onClick = {
                    showIslandMenu = false
                    viewModel.testIsland(
                        AdvanceIslandContent(
                            action = NarrativeAction.CreateAct,
                            reasoning = processingSample,
                            isProcessing = true,
                            genre = genre,
                            onAction = {},
                        ),
                    )
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.island_test_book_generation)) },
                onClick = {
                    showIslandMenu = false
                    viewModel.testIsland(
                        BookGenerationIslandContent(
                            BookGenerationUiState.Generating(
                                sagaId = 1,
                                sagaTitle = bookSagaTitle,
                                actId = 1,
                                actTitle = bookActTitle,
                                genre = genre,
                                reasoning = bookReasoning,
                            ),
                        ),
                    )
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.island_test_image_generation)) },
                onClick = {
                    showIslandMenu = false
                    viewModel.testIsland(
                        ImageGenerationIslandContent(
                            state =
                                ImageGenerationUiState.Generating(
                                    label = imageLabel,
                                    reasoning = imageReasoning,
                                    imageType = ImageType.ICON,
                                ),
                            debugImageFallbackService = viewModel.debugImageFallbackService,
                            onCancel = {},
                            onDismissReveal = {},
                        ),
                    )
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.island_test_manual_image_fallback)) },
                onClick = {
                    showIslandMenu = false
                    viewModel.testIsland(
                        ImageGenerationIslandContent(
                            state =
                                ImageGenerationUiState.AwaitingManualFallback(
                                    prompt = imageFallbackPrompt,
                                ),
                            debugImageFallbackService = viewModel.debugImageFallbackService,
                            onCancel = {},
                            onDismissReveal = {},
                        ),
                    )
                },
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = {
                    Text(
                        stringResource(R.string.island_test_dismiss),
                        color = MaterialTheme.colorScheme.error,
                    )
                },
                onClick = {
                    showIslandMenu = false
                    viewModel.testIsland(null)
                },
            )
        }
    }
}

@Composable
private fun SampleLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}

/**
 * Both recap states side by side. Goes through [GenreRecapCard] with a hand-built [RecapCard]
 * rather than through `RecapHeroCard`, so a preview doesn't need a mock Saga carrying a fully
 * generated review just to show the ready state.
 */
@Composable
private fun RecapCardSample(genre: Genre) {
    val stats =
        listOf(
            RecapStat("247", stringResource(R.string.recap_stat_messages)),
            RecapStat("11", stringResource(R.string.recap_stat_characters)),
            RecapStat("3", stringResource(R.string.recap_stat_chapters)),
        )
    val title = stringResource(R.string.recap_your_journey)
    val cta = stringResource(R.string.recap_revisit_now)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        GenreRecapCard(
            card = RecapCard(title = title, stats = stats, callToAction = cta),
            modifier = Modifier.fillMaxWidth().height(150.dp),
            genre = genre,
        )
        GenreRecapCard(
            card =
                RecapCard(
                    title = title,
                    stats = stats,
                    callToAction = cta,
                    progress =
                        RecapProgress(
                            completed = 2,
                            total = 6,
                            message = stringResource(R.string.recap_almost_ready, 2, 6),
                        ),
                ),
            modifier = Modifier.fillMaxWidth().height(150.dp),
            genre = genre,
        )
    }
}

@Composable
private fun HeaderFontSample(genre: Genre) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SampleLabel("HEADER FONT")
        genre.stylisedText(
            stringResource(genre.title).uppercase(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun BubbleShapeSample(genre: Genre) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SampleLabel("BUBBLE SHAPE")
        Text(
            stringResource(
                R.string.design_system_preview_bubble_sample_text,
                stringResource(genre.title),
            ),
            color = Color.White,
            modifier =
                Modifier
                    .clip(genre.bubble())
                    .background(genre.color)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun GlowBorderSample(genre: Genre) {
    val shape = RoundedCornerShape(50)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SampleLabel("GLOW BORDER")
        Text(
            stringResource(R.string.home_create_new_saga_title).uppercase(),
            style =
                MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                ),
            modifier =
                Modifier
                    .dropShadow(shape, Shadow(10.dp, Brush.verticalGradient(genre.colorPalette())))
                    .border(1.dp, Brush.verticalGradient(genre.colorPalette()), shape)
                    .background(Color.Black, shape)
                    .padding(horizontal = 32.dp, vertical = 16.dp),
        )
    }
}

/**
 * Same [rememberRotatingBorderAngle]/[Modifier.rotatingGradientBorder] primitive used on a
 * generating [ChatBubble]'s border — not a simplified stand-in, so this preview validates the
 * exact thing that ships, just applied to a different [shape].
 */
@Composable
private fun RotatingStrokeSample(genre: Genre) {
    val rotationValue = rememberRotatingBorderAngle()
    val shape = RoundedCornerShape(16.dp)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SampleLabel("ROTATING STROKE (GENERATING STATE)")
        Box(
            Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer, shape)
                .rotatingGradientBorder(
                    shape = shape,
                    colors = genre.colorPalette(),
                    rotationDegrees = rotationValue,
                ),
        )
    }
}

@Composable
private fun ShaderFilterSample(genre: Genre) {
    var boosted by remember { mutableStateOf(true) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Switch(checked = boosted, onCheckedChange = { boosted = it })
            SampleLabel("SHADER EFFECTS")
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(sagaShape())
                .background(Brush.linearGradient(genre.colorPalette()))
                .let { if (boosted) it.effectForGenre(genre) else it },
        )
    }
}

@Composable
private fun ComicExtrudeSample(genre: Genre) {
    var playing by remember { mutableStateOf(true) }
    val palette = genre.colorPalette()
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Switch(checked = playing, onCheckedChange = { playing = it })
            SampleLabel("COMIC EXTRUDE (TEXT vs ICON)")
        }
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer, sagaShape())
                    .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "POW",
                style =
                    MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = palette.first(),
                    ),
                modifier =
                    Modifier
                        .comicExtrude(
                            isPlaying = playing,
                            extrudeColor = palette.last(),
                            outlineColor = MaterialTheme.colorScheme.primary,
                            extrusionSteps = 5,
                            maxDepth = 10.dp,
                        ),
            )
            Icon(
                painterResource(genre.icon),
                null,
                tint = palette.first(),
                modifier =
                    Modifier
                        .size(48.dp)
                        .comicExtrude(
                            isPlaying = playing,
                            extrudeColor = palette.last(),
                            outlineColor = Color.White,
                        ),
            )
        }
    }
}
