package com.ilustris.sagai.features.debug.ui

import MessageStatus
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.gson.GsonBuilder
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.model.ImageType
import com.ilustris.sagai.core.ai.model.LocalGenreVisualConfig
import com.ilustris.sagai.core.ai.model.ShaderParamsConfig
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.features.act.data.model.BookGenerationUiState
import com.ilustris.sagai.features.chapter.data.model.GeneratedChoiceCard
import com.ilustris.sagai.features.imagegeneration.model.ImageGenerationUiState
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.settings.ui.audit.JsonCodeBlock
import com.ilustris.sagai.features.saga.chat.domain.manager.BackgroundTask
import com.ilustris.sagai.features.saga.chat.domain.manager.NarrativeAction
import com.ilustris.sagai.features.saga.chat.ui.components.ChatBubble
import com.ilustris.sagai.features.saga.chat.ui.components.ChatInputView
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.NarrativeBackgroundBanner
import com.ilustris.sagai.features.saga.detail.ui.sagaHeaderComponent
import com.ilustris.sagai.features.saga.milestone.presentation.MilestoneUiState
import com.ilustris.sagai.features.saga.milestone.ui.ChapterChoiceCardsScreen
import com.ilustris.sagai.features.saga.milestone.ui.toStoryBeat
import com.ilustris.sagai.ui.animations.comicExtrude
import com.ilustris.sagai.ui.components.IosStyleMenu
import com.ilustris.sagai.ui.components.IosStyleMenuItem
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
import java.util.Locale
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
    var showCharacterPreview by remember { mutableStateOf(false) }
    var showMoreDebugMenu by remember { mutableStateOf(false) }
    var showIslandMenu by remember { mutableStateOf(false) }

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
                // Debug-only slider overrides for the remote shader params — lets you tune
                // contrast/brightness/etc. live against the real rendering pipeline before
                // committing values to Remote Config. Resets to the remote defaults whenever
                // the genre changes or the remote config first loads for it.
                val remoteConfig = LocalGenreVisualConfig.current
                var shaderOverrides by remember(genre) { mutableStateOf<ShaderParamsConfig?>(null) }
                LaunchedEffect(remoteConfig?.shaderParams, genre) {
                    if (shaderOverrides == null) {
                        shaderOverrides = remoteConfig?.shaderParams ?: ShaderParamsConfig()
                    }
                }
                val effectiveConfig =
                    remember(remoteConfig, shaderOverrides) {
                        shaderOverrides?.let { overrides -> remoteConfig?.copy(shaderParams = overrides) }
                            ?: remoteConfig
                    }

                CompositionLocalProvider(LocalGenreVisualConfig provides effectiveConfig) {
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
                            val screenHeightDp = LocalConfiguration.current.screenHeightDp
                            val headerHeight = remember(screenHeightDp) { (screenHeightDp * 0.7f).dp }
                            sagaHeaderComponent(
                                saga = mockSaga,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(headerHeight),
                                onAction = {},
                            )

                            ShaderParamsTuningMenu(
                                shaderParams = shaderOverrides ?: ShaderParamsConfig(),
                                remoteDefaults = remoteConfig?.shaderParams,
                                onChange = { shaderOverrides = it },
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
                                                            onContinue = { milestonePreviewKind = MilestonePreviewKind.CHOICES },
                                                            onNavigate = {},
                                                            onGenerateBook = {},
                                                        ),
                                                genre = genre,
                                            )
                                        }

                                        // Sits before the chapter closure, like the real chain. Picks
                                        // are kept locally so the mandatory-answer gating is testable;
                                        // only indices ever reach the screen, never the hidden tags.
                                        MilestonePreviewKind.CHOICES -> {
                                            var selections by remember(genre) {
                                                mutableStateOf<List<Int?>>(List(MOCK_CHOICE_CARDS.size) { null })
                                            }
                                            ChapterChoiceCardsScreen(
                                                state =
                                                    MilestoneUiState.ChoiceCardsStep(
                                                        milestone = DesignSystemMocks.mockChapterFinishedMilestone(genre),
                                                        cards = MOCK_CHOICE_CARDS,
                                                        selections = selections,
                                                    ),
                                                genre = genre,
                                                onSelect = { card, option ->
                                                    selections = selections.toMutableList().also { it[card] = option }
                                                },
                                                onSubmit = { milestonePreviewKind = MilestonePreviewKind.CHAPTER },
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
                        ).size(40.dp),
            ) {
                Icon(
                    painterResource(R.drawable.ic_back_left),
                    contentDescription = stringResource(R.string.back_button_description),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(20.dp),
                )
            }

            Text(
                stringResource(R.string.design_system_preview_title),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )

            Row(
                modifier =
                    Modifier
                        .background(
                            MaterialTheme.colorScheme.background.copy(alpha = .3f),
                            CircleShape,
                        ).padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DebugPillIconButton(
                    icon = R.drawable.ic_full_spark,
                    contentDescription = "Preview Starry Loader",
                    onClick = { showStarryLoaderPreview = true },
                )
                DebugPillIconButton(
                    icon = R.drawable.character_icon,
                    contentDescription = "Preview character generation",
                    onClick = { showCharacterPreview = true },
                )

                Box(contentAlignment = Alignment.Center) {
                    DebugPillIconButton(
                        icon = R.drawable.ic_more_vert,
                        contentDescription = "More debug tools",
                        onClick = { showMoreDebugMenu = true },
                    )
                    IosStyleMenu(
                        expanded = showMoreDebugMenu,
                        onDismissRequest = { showMoreDebugMenu = false },
                    ) {
                        IosStyleMenuItem(
                            text = "Preview Milestone screens",
                            onClick = {
                                showMoreDebugMenu = false
                                milestonePreviewKind = MilestonePreviewKind.LOADING
                            },
                        )
                        IosStyleMenuItem(
                            text = "Test Island",
                            onClick = {
                                showMoreDebugMenu = false
                                showIslandMenu = true
                            },
                        )
                    }
                    IslandTestMenu(
                        viewModel = viewModel,
                        genre = genre,
                        expanded = showIslandMenu,
                        onDismiss = { showIslandMenu = false },
                    )
                }
            }
        }
    }

    if (showCharacterPreview) {
        CharacterGenerationPreviewSheet(
            viewModel = viewModel,
            genre = genre,
            onDismiss = { showCharacterPreview = false },
        )
    }
}

/** Stand-in chapter art, so the cover slot in each genre's treatment has something to show. */
private const val MOCK_COVER_URL =
    "https://i.pinimg.com/564x/0a/92/7d/0a927df0b8a6a12a5276e03882775739.jpg"

/** Stand-in dilemmas for the chapter-closure choice cards. Tags are dummies: the screen never shows them. */
private val MOCK_CHOICE_CARDS =
    listOf(
        GeneratedChoiceCard(
            choiceTitle = "Who do you reach first?",
            optionAText = "The friend who trusted you",
            optionATag = "loyalty over pragmatism",
            optionBText = "The stranger who holds the map",
            optionBTag = "pragmatism over loyalty",
        ),
        GeneratedChoiceCard(
            choiceTitle = "Power, or the crew?",
            optionAText = "Leave them behind and take it",
            optionATag = "ambition over belonging",
            optionBText = "Refuse, and walk away with them",
            optionBTag = "belonging over ambition",
        ),
        GeneratedChoiceCard(
            choiceTitle = "Confront her, or let it go?",
            optionAText = "Forgive her and say nothing",
            optionATag = "forgives for the greater good",
            optionBText = "Confront her in front of the others",
            optionBTag = "truth over harmony",
        ),
    )

private enum class MilestonePreviewKind { LOADING, EVENT, CHOICES, CHAPTER, ACT, INTRO, ERROR }

/**
 * Runs the real character-generation and image-generation pipelines against an in-memory mock
 * saga ([DesignSystemMocks.mockSagaContent]) — nothing here is persisted, so it's safe to
 * regenerate repeatedly while tuning a genre's appearance/identity blueprint rules. Wrapped in
 * [SagAITheme] for [genre] so the sheet reads in the same palette as the preview it's testing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CharacterGenerationPreviewSheet(
    viewModel: DesignSystemViewModel,
    genre: Genre,
    onDismiss: () -> Unit,
) {
    val state by viewModel.characterPreviewState.collectAsStateWithLifecycle()

    SagAITheme(genre = genre) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
            ) {
                Text(
                    "Character preview — ${genre.name}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.generateRandomCharacterPreview(genre) },
                    enabled = !state.isGeneratingCharacter,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (state.isGeneratingCharacter) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(if (state.character == null) "Generate random character" else "Regenerate character")
                    }
                }

                state.error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                state.character?.let { character ->
                    Spacer(Modifier.height(16.dp))
                    JsonCodeBlock(character.toJsonFormat())

                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.generatePreviewImage(genre) },
                        enabled = !state.isGeneratingImage,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (state.isGeneratingImage) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(if (state.bitmap == null) "Generate portrait" else "Regenerate portrait")
                        }
                    }

                    state.bitmap?.let { bitmap ->
                        Spacer(Modifier.height(16.dp))
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Generated portrait for ${character.name}",
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(16.dp)),
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}


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

/**
 * Expandable, slider-driven live editor for [ShaderParamsConfig] — every scalar Float knob that
 * feeds the AGSL image-adjustment pipeline (see [com.ilustris.sagai.ui.theme.filters.effectForGenre]).
 * Sliders write into [onChange], which the caller re-provides through [LocalGenreVisualConfig] so
 * the header image above re-renders live with the tuned values. "Copiar JSON" exports the current
 * values in the exact shape Remote Config expects for the `shaderParams` key.
 */
@Composable
private fun ShaderParamsTuningMenu(
    shaderParams: ShaderParamsConfig,
    remoteDefaults: ShaderParamsConfig?,
    onChange: (ShaderParamsConfig) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "AJUSTES DE IMAGEM",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                )
                Text(
                    "Ajuste ao vivo dos parâmetros de shaderParams (visual config)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            Icon(
                painterResource(if (expanded) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp),
            )
        }

        AnimatedVisibility(expanded) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
            ) {
                shaderSliderGroups(shaderParams, onChange).forEach { (groupLabel, specs) ->
                    Text(
                        groupLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                    )
                    specs.forEach { spec -> SliderRow(spec) }
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = { onChange(remoteDefaults ?: ShaderParamsConfig()) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Resetar")
                    }
                    Button(
                        onClick = {
                            val json = GsonBuilder().setPrettyPrinting().create().toJson(shaderParams)
                            clipboardManager.setText(AnnotatedString(json))
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_copy),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Copiar JSON")
                    }
                }
            }
        }
    }
}

private data class SliderSpec(
    val label: String,
    val value: Float,
    val range: ClosedFloatingPointRange<Float>,
    val onValueChange: (Float) -> Unit,
)

/** Groups every scalar Float of [ShaderParamsConfig] into labeled sections for the tuning menu. */
private fun shaderSliderGroups(
    params: ShaderParamsConfig,
    onChange: (ShaderParamsConfig) -> Unit,
): List<Pair<String, List<SliderSpec>>> =
    listOf(
        "AJUSTES BÁSICOS" to
            listOf(
                SliderSpec("Brilho", params.brightness, -1f..1f) { onChange(params.copy(brightness = it)) },
                SliderSpec("Contraste", params.contrast, 0f..2f) { onChange(params.copy(contrast = it)) },
                SliderSpec("Saturação", params.saturation, 0f..2f) { onChange(params.copy(saturation = it)) },
                SliderSpec(
                    "Temperatura de Cor",
                    params.colorTemperature,
                    -1f..1f,
                ) { onChange(params.copy(colorTemperature = it)) },
                SliderSpec("Ponto Preto", params.blackPoint, 0f..1f) { onChange(params.copy(blackPoint = it)) },
                SliderSpec("Ponto Branco", params.whitePoint, 0.01f..1f) { onChange(params.copy(whitePoint = it)) },
            ),
        "FOCO & NITIDEZ" to
            listOf(
                SliderSpec(
                    "Foco Suave (Raio)",
                    params.softFocusRadius,
                    0f..20f,
                ) { onChange(params.copy(softFocusRadius = it)) },
                SliderSpec("Nitidez", params.sharpenAmount, 0f..2f) { onChange(params.copy(sharpenAmount = it)) },
            ),
        "VINHETA" to
            listOf(
                SliderSpec(
                    "Intensidade",
                    params.vignetteStrength,
                    0f..1f,
                ) { onChange(params.copy(vignetteStrength = it)) },
                SliderSpec(
                    "Suavidade",
                    params.vignetteSoftness,
                    0f..1f,
                ) { onChange(params.copy(vignetteSoftness = it)) },
            ),
        "EFEITOS" to
            listOf(
                SliderSpec("Grão", params.grainIntensity, 0f..1f) { onChange(params.copy(grainIntensity = it)) },
                SliderSpec(
                    "Bloom · Limiar",
                    params.bloomThreshold,
                    0f..1f,
                ) { onChange(params.copy(bloomThreshold = it)) },
                SliderSpec(
                    "Bloom · Intensidade",
                    params.bloomIntensity,
                    0f..2f,
                ) { onChange(params.copy(bloomIntensity = it)) },
                SliderSpec(
                    "Bloom · Raio",
                    params.bloomRadius,
                    0f..20f,
                ) { onChange(params.copy(bloomRadius = it)) },
                SliderSpec(
                    "Aberração Cromática",
                    params.chromaticAberration,
                    0f..0.3f,
                ) { onChange(params.copy(chromaticAberration = it)) },
                SliderSpec(
                    "Scanline · Intensidade",
                    params.scanlineIntensity,
                    0f..1f,
                ) { onChange(params.copy(scanlineIntensity = it)) },
                SliderSpec(
                    "Scanline · Densidade",
                    params.scanlineDensity,
                    0f..10f,
                ) { onChange(params.copy(scanlineDensity = it)) },
                SliderSpec(
                    "Posterização",
                    params.posterizeLevels,
                    0f..16f,
                ) { onChange(params.copy(posterizeLevels = it)) },
                SliderSpec(
                    "Halftone",
                    params.halftoneScale,
                    0f..50f,
                ) { onChange(params.copy(halftoneScale = it)) },
                SliderSpec(
                    "Pixelização",
                    params.pixelationBlockSize,
                    0f..50f,
                ) { onChange(params.copy(pixelationBlockSize = it)) },
                SliderSpec(
                    "Força do Tint",
                    params.tintStrength,
                    0f..1f,
                ) { onChange(params.copy(tintStrength = it)) },
                SliderSpec(
                    "Energia de Contorno",
                    params.rimEnergyIntensity,
                    0f..2f,
                ) { onChange(params.copy(rimEnergyIntensity = it)) },
                SliderSpec(
                    "Largura do Contorno",
                    params.rimEnergyWidth,
                    0f..50f,
                ) { onChange(params.copy(rimEnergyWidth = it)) },
                SliderSpec(
                    "Névoa · Intensidade",
                    params.wispIntensity,
                    0f..1f,
                ) { onChange(params.copy(wispIntensity = it)) },
                SliderSpec(
                    "Névoa · Velocidade",
                    params.wispSpeed,
                    0f..10f,
                ) { onChange(params.copy(wispSpeed = it)) },
            ),
    )

@Composable
private fun SliderRow(spec: SliderSpec) {
    Column(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(spec.label, style = MaterialTheme.typography.labelSmall)
            Text(
                String.format(Locale.US, "%.3f", spec.value),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            )
        }
        Slider(
            value = spec.value,
            onValueChange = spec.onValueChange,
            valueRange = spec.range,
            modifier = Modifier.fillMaxWidth().height(28.dp),
        )
    }
}

/** One icon inside the debug tools pill — no background of its own, the pill supplies it. */
@Composable
private fun DebugPillIconButton(
    icon: Int,
    contentDescription: String?,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, modifier = Modifier.size(44.dp)) {
        Icon(
            painterResource(icon),
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun IslandTestMenu(
    viewModel: DesignSystemViewModel,
    genre: Genre,
    expanded: Boolean,
    onDismiss: () -> Unit,
) {
    val objectiveSample = stringResource(R.string.island_test_objective_sample)
    val processingSample = stringResource(R.string.island_test_advance_processing_sample)
    val bookSagaTitle = stringResource(R.string.island_test_book_generation_saga_title)
    val bookActTitle = stringResource(R.string.island_test_book_generation_act_title)
    val bookReasoning = stringResource(R.string.island_test_book_generation_reasoning)
    val imageLabel = stringResource(R.string.island_test_image_generation_label)
    val imageReasoning = stringResource(R.string.island_test_image_generation_reasoning)
    val imageFallbackPrompt = stringResource(R.string.island_test_image_fallback_prompt)

    DropdownMenu(
        expanded,
        onDismissRequest = onDismiss,
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.island_test_objective)) },
            onClick = {
                onDismiss()
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
                onDismiss()
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
                onDismiss()
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
                onDismiss()
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
                onDismiss()
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
                onDismiss()
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
                onDismiss()
                viewModel.testIsland(null)
            },
        )
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
