@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.saga.detail.ui

import ai.atick.material.MaterialColor
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.R
import com.ilustris.sagai.core.data.State
import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.sortCharactersByMessageCount
import com.ilustris.sagai.features.act.ui.ActReader
import com.ilustris.sagai.features.act.ui.toRoman
import com.ilustris.sagai.features.chapter.ui.ChapterCardView
import com.ilustris.sagai.features.chapter.ui.ChapterContent
import com.ilustris.sagai.features.characters.relations.ui.RelationShipCard
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.characters.ui.CharactersGalleryContent
import com.ilustris.sagai.features.characters.ui.components.VerticalLabel
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.home.data.model.relationshipsSortedByEvents
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.features.newsaga.data.model.shimmerColors
import com.ilustris.sagai.features.playthrough.AnimatedPlaytimeCounter
import com.ilustris.sagai.features.premium.PremiumView
import com.ilustris.sagai.features.saga.detail.presentation.SagaDetailViewModel
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.features.timeline.ui.TimeLineCard
import com.ilustris.sagai.features.timeline.ui.TimeLineContent
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.ui.EmotionalSheet
import com.ilustris.sagai.features.wiki.ui.WikiCard
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.components.StarryLoader
import com.ilustris.sagai.ui.components.stylisedText
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.navigation.navigateToRoute
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.SagaTopBar
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.darker
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shape
import com.ilustris.sagai.ui.theme.zoomAnimation
import effectForGenre
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Composable
fun SagaDetailView(
    navHostController: NavHostController,
    sagaId: String,
    paddingValues: PaddingValues,
    viewModel: SagaDetailViewModel = hiltViewModel(),
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val saga by viewModel.saga.collectAsStateWithLifecycle()
    val showIntro by viewModel.showIntro.collectAsStateWithLifecycle()
    var sagaToDelete by remember { mutableStateOf<Saga?>(null) }
    var section by remember {
        mutableStateOf(
            DetailAction.BACK,
        )
    }
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val loadingMessage by viewModel.loadingMessage.collectAsStateWithLifecycle()
    val backupEnabled by viewModel.backupEnabled.collectAsStateWithLifecycle(initialValue = false)
    val emotionalCardReference by viewModel.emotionalCardReference.collectAsStateWithLifecycle()
    val showReview by viewModel.showReview.collectAsStateWithLifecycle()
    val showPremiumSheet by viewModel.showPremiumSheet.collectAsStateWithLifecycle()
    BackHandler(enabled = true) {
        if (showDeleteConfirmation) {
            showDeleteConfirmation = false
        } else {
            when (section) {
                DetailAction.BACK, DetailAction.DELETE -> {
                    navHostController.popBackStack()
                }

                else -> {
                    section = DetailAction.BACK
                }
            }
        }
    }

    SagaDetailContentView(
        state,
        section,
        paddingValues,
        showReview = showReview,
        showTitleOnly = showIntro,
        emotionalCardReference = emotionalCardReference,
        backupEnabled = backupEnabled,
        onChangeSection = {
            section = it
            if (it == DetailAction.DELETE) {
                sagaToDelete = saga?.data
                showDeleteConfirmation = true
            }

            if (it == DetailAction.REGENERATE) {
                viewModel.regenerateIcon()
            }
        },
        onExportSaga = {
            viewModel.exportSaga()
        },
        onBackClick = {
            when (it) {
                DetailAction.BACK, DetailAction.DELETE -> navHostController.popBackStack()
                else -> section = DetailAction.BACK
            }
        },
        reviewWiki = {
            viewModel.reviewWiki(it)
        },
        createReview = {
            viewModel.createReview()
        },
        openReview = {
            viewModel.createReview()
        },
        closeReview = {
            viewModel.closeReview()
        },
        createEmotionalReview = {
            if (saga?.data?.emotionalReview == null) {
                viewModel.createSagaEmotionalReview()
            }
        },
        createTimelineReview = {
            viewModel.generateTimelineContent(it)
        },
        modifier = Modifier.fillMaxSize(),
    )

    if (showDeleteConfirmation) {
        sagaToDelete?.let {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = {
                    Text(
                        stringResource(R.string.saga_detail_delete_confirmation_title),
                        style =
                            MaterialTheme.typography.titleLarge.copy(
                                fontFamily = it.genre.bodyFont(),
                            ),
                    )
                },
                text = {
                    Text(
                        stringResource(R.string.saga_detail_delete_confirmation_message),
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = it.genre.bodyFont(),
                            ),
                    )
                },
                shape = RoundedCornerShape(it.genre.cornerSize()),
                confirmButton = {
                    Button(
                        onClick = {
                            sagaToDelete?.let { viewModel.deleteSaga(it) }
                            showDeleteConfirmation = false
                            navHostController.navigateToRoute(
                                Routes.HOME,
                                popUpToRoute = Routes.SAGA_DETAIL,
                            )
                        },
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                            ),
                        shape = RoundedCornerShape(it.genre.cornerSize()),
                    ) { Text(stringResource(R.string.saga_detail_delete_button)) }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDeleteConfirmation = false },
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                        shape = RoundedCornerShape(it.genre.cornerSize()),
                    ) { Text(stringResource(R.string.saga_detail_cancel_button)) }
                },
            )
        }
    }

    if (isGenerating) {
        StarryLoader(
            isLoading = true,
            loadingMessage = loadingMessage ?: emptyString(),
            textStyle = MaterialTheme.typography.labelMedium,
            brushColors = saga?.data?.genre?.colorPalette() ?: holographicGradient,
        )
    }

    PremiumView(showPremiumSheet, { viewModel.togglePremiumSheet() })

    LocalContext.current

    val exportLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("application/octet-stream"),
        ) { uri ->
            uri?.let { viewModel.handleExportDestination(it) }
        }

    LaunchedEffect(Unit) {
        viewModel.fetchSagaDetails(sagaId)
        viewModel.exportLauncher.collect { suggestedFileName ->
            exportLauncher.launch(suggestedFileName)
        }
    }
}

fun LazyListScope.SagaDrawerContent(
    content: SagaContent,
    openReview: () -> Unit = {},
) {
    with(this) {
        item {
            Column(Modifier.fillMaxWidth()) {
                Icon(
                    painterResource(R.drawable.ic_spark),
                    null,
                    tint = content.data.genre.color,
                    modifier =
                        Modifier
                            .clip(CircleShape)
                            .padding(4.dp)
                            .size(50.dp)
                            .align(Alignment.CenterHorizontally)
                            .reactiveShimmer(
                                content.data.review != null,
                                targetValue = 250f,
                            )
                            .clickable {
                                openReview()
                            },
                )
            }
        }
        items(content.acts) {
            val index = content.acts.indexOf(it)
            val brush =
                content.data.genre.gradient(
                    content.currentActInfo?.data?.id == it.data.id,
                    targetValue = 300f,
                    duration = 1.seconds,
                )

            val chaptersInAct = it.chapters
            val shape = RoundedCornerShape(content.data.genre.cornerSize())

            Column(
                Modifier
                    .padding(horizontal = 12.dp, vertical = 0.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.onBackground.copy(alpha = .1f),
                        shape,
                    )
                    .background(
                        MaterialTheme.colorScheme.background,
                        shape,
                    )
                    .padding(4.dp)
                    .animateContentSize(
                        animationSpec = tween(500, easing = EaseIn),
                    ),
            ) {
                Text(
                    it.data.title.ifEmpty {
                        stringResource(
                            R.string.saga_drawer_act_prefix,
                            ((index + 1).toRoman()),
                        )
                    },
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontFamily = content.data.genre.bodyFont(),
                            fontWeight = FontWeight.Bold,
                            brush = brush,
                        ),
                    modifier = Modifier.padding(16.dp),
                )

                HorizontalDivider(modifier = Modifier.padding(4.dp))

                if (chaptersInAct.isNotEmpty()) {
                    chaptersInAct.forEachIndexed { chapterIndex, chapter ->
                        val eventsInChapter = chapter.events.filter { it.isComplete() }

                        var expandedEvents by remember {
                            mutableStateOf(false)
                        }
                        Row(
                            modifier =
                                Modifier
                                    .padding(8.dp)
                                    .clip(shape)
                                    .clickable {
                                        expandedEvents = !expandedEvents
                                    }
                                    .fillMaxWidth(),
                        ) {
                            Image(
                                painterResource(R.drawable.ic_spark),
                                null,
                                Modifier.size(24.dp),
                            )
                            Text(
                                stringResource(
                                    R.string.saga_drawer_chapter_events_count,
                                    chapter.data.title.ifEmpty {
                                        stringResource(
                                            id = R.string.chapter_in_progress,
                                        )
                                    },
                                    eventsInChapter.size,
                                ),
                                style =
                                    MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = content.data.genre.bodyFont(),
                                        fontWeight = FontWeight.Bold,
                                    ),
                            )
                        }

                        if (expandedEvents) {
                            eventsInChapter.forEach { event ->
                                TimeLineCard(
                                    event,
                                    content,
                                    showText = false,
                                    showSpark = false,
                                    isLast = eventsInChapter.indexOf(event) == eventsInChapter.lastIndex,
                                    modifier =
                                        Modifier
                                            .padding(horizontal = 8.dp)
                                            .alpha(.7f),
                                )
                            }
                        }

                        if (chapter.isComplete().not()) {
                            Text(
                                stringResource(id = R.string.events_count, eventsInChapter.size, UpdateRules.CHAPTER_UPDATE_LIMIT),
                                style =
                                    MaterialTheme.typography.labelSmall.copy(
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        fontFamily = content.data.genre.bodyFont(),
                                        textAlign = TextAlign.Center,
                                    ),
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .alpha(.5f),
                            )

                            HorizontalDivider(
                                modifier =
                                    Modifier
                                        .height(1.dp)
                                        .padding(4.dp),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .1f),
                            )

                            if (chapter == chaptersInAct.last()) {
                                val messageCount =
                                    chapter.events
                                        .lastOrNull()
                                        ?.messages
                                        ?.size

                                Text(
                                    stringResource(
                                        id = R.string.messages_count,
                                        messageCount ?: 0,
                                        UpdateRules.LORE_UPDATE_LIMIT,
                                    ),
                                    style =
                                        MaterialTheme.typography.labelSmall.copy(
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                            fontFamily = content.data.genre.bodyFont(),
                                            textAlign = TextAlign.Center,
                                        ),
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .alpha(.5f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SagaDetailContentView(
    state: State,
    currentSection: DetailAction = DetailAction.BACK,
    paddingValues: PaddingValues,
    showReview: Boolean = false,
    generatingReview: Boolean = false,
    backupEnabled: Boolean = false,
    emotionalCardReference: String,
    onChangeSection: (DetailAction) -> Unit = {},
    onBackClick: (DetailAction) -> Unit = {},
    reviewWiki: (List<Wiki>) -> Unit = {},
    createReview: () -> Unit,
    openReview: () -> Unit,
    closeReview: () -> Unit,
    createTimelineReview: (TimelineContent) -> Unit,
    createEmotionalReview: () -> Unit,
    onExportSaga: () -> Unit = {},
    showTitleOnly: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val saga = ((state as? State.Success)?.data as? SagaContent)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showEmotionalReview by remember { mutableStateOf(false) }

    saga?.let { sagaContent ->
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Box(modifier = modifier) {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            ModalDrawerSheet(
                                drawerShape =
                                    RoundedCornerShape(0.dp),
                                drawerContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                            ) {
                                LazyColumn(
                                    Modifier
                                        .fillMaxWidth(.8f)
                                        .fillMaxHeight(),
                                ) {
                                    item {
                                        Text(
                                            stringResource(R.string.saga_detail_progress_title),
                                            modifier =
                                                Modifier
                                                    .align(Alignment.CenterHorizontally)
                                                    .padding(16.dp),
                                            style =
                                                MaterialTheme.typography.titleLarge.copy(
                                                    fontFamily = sagaContent.data.genre.headerFont(),
                                                    textAlign = TextAlign.Center,
                                                ),
                                        )
                                    }
                                    SagaDrawerContent(sagaContent) {
                                        openReview()
                                    }
                                }
                            }
                        }
                    },
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        SharedTransitionLayout(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            AnimatedContent(
                                currentSection,
                                transitionSpec = {
                                    fadeIn(tween(500)) togetherWith
                                        fadeOut(
                                            tween(
                                                400,
                                            ),
                                        )
                                },
                                modifier =
                                    Modifier
                                        .fillMaxSize(),
                            ) { section ->
                                when (section) {
                                    DetailAction.CHARACTERS ->
                                        CharactersGalleryContent(
                                            sagaContent,
                                            animationScopes = this@SharedTransitionLayout to this,
                                            onOpenEvent = {
                                                onChangeSection(DetailAction.TIMELINE)
                                            },
                                            onBackClick = {
                                                onBackClick(currentSection)
                                            },
                                            titleModifier =
                                                Modifier.sharedElement(
                                                    rememberSharedContentState(
                                                        key =
                                                            DetailAction.CHARACTERS
                                                                .sharedElementTitleKey(
                                                                    sagaContent.data.id,
                                                                ),
                                                    ),
                                                    animatedVisibilityScope = this,
                                                ),
                                        )

                                    DetailAction.TIMELINE ->
                                        TimeLineContent(
                                            sagaContent,
                                            animationScopes = this@SharedTransitionLayout to this,
                                            titleModifier =
                                                Modifier.sharedElement(
                                                    rememberSharedContentState(
                                                        key =
                                                            DetailAction.TIMELINE
                                                                .sharedElementTitleKey(
                                                                    sagaContent.data.id,
                                                                ),
                                                    ),
                                                    animatedVisibilityScope = this,
                                                ),
                                            generateEmotionalReview = {
                                                createTimelineReview(it)
                                            },
                                            openCharacters = { onChangeSection(DetailAction.CHARACTERS) },
                                            onBackClick = {
                                                onBackClick(currentSection)
                                            },
                                        )

                                    DetailAction.CHAPTERS ->
                                        ChapterContent(
                                            sagaContent,
                                            animationScopes = this@SharedTransitionLayout to this,
                                            titleModifier =
                                                Modifier.sharedElement(
                                                    rememberSharedContentState(
                                                        key =
                                                            DetailAction.CHAPTERS
                                                                .sharedElementTitleKey(
                                                                    sagaContent.data.id,
                                                                ),
                                                    ),
                                                    animatedVisibilityScope = this,
                                                ),
                                            onBackClick = {
                                                onBackClick(currentSection)
                                            },
                                        )

                                    DetailAction.WIKI ->
                                        WikiContent(
                                            sagaContent,
                                            {
                                                onBackClick(currentSection)
                                            },
                                            reviewWiki = {
                                                reviewWiki(it)
                                            },
                                            titleModifier =
                                                Modifier.sharedElement(
                                                    rememberSharedContentState(
                                                        key =
                                                            DetailAction.WIKI
                                                                .sharedElementTitleKey(
                                                                    sagaContent.data.id,
                                                                ),
                                                    ),
                                                    animatedVisibilityScope = this,
                                                ),
                                        )

                                    DetailAction.ACTS -> ActContent(sagaContent)
                                    else -> {
                                        SagaDetailInitialView(
                                            sagaContent,
                                            emotionalReviewIconUrl = emotionalCardReference,
                                            animationScopes = this@SharedTransitionLayout to this,
                                            backupEnabled = backupEnabled,
                                            modifier =
                                                Modifier
                                                    .animateContentSize()
                                                    .fillMaxSize(),
                                            selectSection = { action ->
                                                onChangeSection(action)
                                            },
                                            openEmotionalReview = {
                                                if (sagaContent.data.emotionalReview.isNullOrEmpty()) {
                                                    createEmotionalReview()
                                                } else {
                                                    showEmotionalReview = true
                                                }
                                            },
                                            openReview = {
                                                createReview()
                                            },
                                            onBackClick = {
                                                onBackClick.invoke(currentSection)
                                            },
                                            openDrawer = {
                                                scope.launch {
                                                    if (drawerState.isClosed) {
                                                        drawerState.open()
                                                    } else {
                                                        drawerState.close()
                                                    }
                                                }
                                            },
                                            onExportSaga = onExportSaga,
                                            showTitleOnly = showTitleOnly,
                                        )
                                    }
                                }
                            }
                        }

                        if (showReview) {
                            ModalBottomSheet(
                                onDismissRequest = {
                                    closeReview()
                                },
                                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(),
                                dragHandle = {
                                    Box {}
                                },
                                containerColor = MaterialTheme.colorScheme.background,
                            ) {
                                SagaReview(content = sagaContent, generatingReview)
                            }
                        }

                        if (showEmotionalReview) {
                            ModalBottomSheet(
                                onDismissRequest = {
                                    showEmotionalReview = false
                                },
                                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(),
                                containerColor = MaterialTheme.colorScheme.background,
                            ) {
                                EmotionalSheet(sagaContent, emotionalCardReference)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SagaDetailContentViewPreview() {
    SagaDetailContentView(
        state =
            State.Success(
                SagaContent(
                    data =
                        Saga(
                            title = "The Lord of the Rings",
                            description =
                                "The Lord of the Rings is an epic high-fantasy novel written by English author and scholar J. R. R. Tolkien.",
                            genre = Genre.FANTASY,
                            icon = "",
                            isEnded = false,
                            review = null,
                        ),
                    characters =
                        emptyList(),
                    acts = emptyList(),
                ),
            ),
        emotionalCardReference = "",
        paddingValues = PaddingValues(0.dp),
        createReview = {},
        openReview = {},
        createEmotionalReview = {},
        createTimelineReview = {},
        closeReview = {},
    )
}

@Composable
fun ActContent(saga: SagaContent) {
    ActReader(saga)
}

@Composable
fun SimpleSlider(
    title: String,
    maxValue: Float = 10f,
    value: Float = 0f,
    onValueChange: (Float) -> Unit = {},
) {
    Column(Modifier.padding(16.dp)) {
        var sliderPosition by remember { mutableFloatStateOf(value) }

        Text(
            "$title - $sliderPosition",
        )
        Slider(
            value = sliderPosition,
            onValueChange = {
                sliderPosition = it
                onValueChange(it)
                Log.i(javaClass.simpleName, "Slider $title value changed to $it")
            },
            colors =
                SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            valueRange = 0f..maxValue,
        )
    }
}

@Composable
private fun SagaDetailInitialView(
    content: SagaContent?,
    emotionalReviewIconUrl: String,
    backupEnabled: Boolean,
    modifier: Modifier,
    animationScopes: Pair<SharedTransitionScope, AnimatedContentScope>,
    selectSection: (DetailAction) -> Unit = {},
    openReview: () -> Unit = {},
    openEmotionalReview: () -> Unit = {},
    openDrawer: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onExportSaga: () -> Unit = {},
    showTitleOnly: Boolean = false,
) {
    content?.let { saga ->
        val columnCount = 2
        val sectionStyle =
            MaterialTheme.typography.titleLarge.copy(
                fontFamily = saga.data.genre.bodyFont(),
                fontWeight = FontWeight.Bold,
            )
        val gridState = rememberLazyGridState()
        val genre = remember { saga.data.genre }
        with(animationScopes.first) {
            AnimatedContent(showTitleOnly) {
                if (it) {
                    Box(Modifier.fillMaxSize()) {
                        genre.stylisedText(
                            saga.data.title,
                            modifier =
                                Modifier
                                    .align(Alignment.Center)
                                    .reactiveShimmer(true)
                                    .padding(16.dp)
                                    .sharedElement(
                                        rememberSharedContentState(
                                            key = "saga-style-header",
                                        ),
                                        animatedVisibilityScope = this@AnimatedContent,
                                    ),
                        )
                    }
                } else {
                    Box(Modifier.fillMaxSize()) {
                        val genreHighlight = saga.data.genre.selectiveHighlight()
                        var highlightParams by remember {
                            mutableStateOf(genreHighlight)
                        }
                        val chapters = remember { saga.flatChapters().filter { it.isComplete() } }
                        val events = remember { saga.flatEvents().filter { it.isComplete() } }
                        val messages = remember { saga.flatMessages() }
                        val relationships = remember { saga.relationshipsSortedByEvents() }
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columnCount),
                            modifier = modifier,
                            state = gridState,
                        ) {
                            item(span = {
                                GridItemSpan(columnCount)
                            }) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (saga.data.icon.isNotEmpty()) {
                                        Box(
                                            modifier =
                                                Modifier
                                                    .background(MaterialTheme.colorScheme.background)
                                                    .fillMaxHeight(.4f)
                                                    .fillMaxWidth(),
                                        ) {
                                            AsyncImage(
                                                saga.data.icon,
                                                contentDescription = saga.data.title,
                                                modifier =
                                                    Modifier
                                                        .background(MaterialTheme.colorScheme.background)
                                                        .fillMaxSize()
                                                        .effectForGenre(saga.data.genre)
                                                        .selectiveColorHighlight(highlightParams)
                                                        .zoomAnimation(),
                                                contentScale = ContentScale.Crop,
                                            )

                                            Box(
                                                Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .fillMaxWidth()
                                                    .fillMaxHeight(.3f)
                                                    .background(fadeGradientBottom()),
                                            )

                                            Text(
                                                saga.data.title,
                                                maxLines = 2,
                                                style =
                                                    MaterialTheme.typography.displaySmall.copy(
                                                        fontFamily = saga.data.genre.headerFont(),
                                                        brush = saga.data.genre.gradient(true),
                                                        textAlign = TextAlign.Center,
                                                    ),
                                                modifier =
                                                    Modifier
                                                        .align(Alignment.BottomCenter)
                                                        .background(fadeGradientBottom())
                                                        .padding(16.dp)
                                                        .fillMaxWidth()
                                                        .sharedElement(
                                                            rememberSharedContentState(
                                                                key = "saga-style-header",
                                                            ),
                                                            animatedVisibilityScope = this@AnimatedContent,
                                                        )
                                                        .reactiveShimmer(
                                                            true,
                                                            duration = 5.seconds,
                                                        ),
                                            )
                                        }
                                    } else {
                                        Image(
                                            painterResource(R.drawable.ic_spark),
                                            null,
                                            modifier =
                                                Modifier
                                                    .align(Alignment.CenterHorizontally)
                                                    .padding(32.dp)
                                                    .size(100.dp)
                                                    .clickable {
                                                        if (saga.data.icon.isEmpty()) {
                                                            selectSection(DetailAction.REGENERATE)
                                                        }
                                                    }
                                                    .gradientFill(
                                                        saga.data.genre.gradient(),
                                                    ),
                                        )

                                        Text(
                                            saga.data.title,
                                            maxLines = 2,
                                            style =
                                                MaterialTheme.typography.displayMedium.copy(
                                                    fontFamily = saga.data.genre.headerFont(),
                                                    brush = saga.data.genre.gradient(true),
                                                    textAlign = TextAlign.Center,
                                                ),
                                            modifier =
                                                Modifier
                                                    .padding(16.dp)
                                                    .fillMaxWidth()
                                                    .sharedElement(
                                                        rememberSharedContentState(
                                                            key = "saga-style-header",
                                                        ),
                                                        animatedVisibilityScope = this@AnimatedContent,
                                                    )
                                                    .reactiveShimmer(
                                                        true,
                                                        duration = 5.seconds,
                                                    ),
                                        )
                                    }
                                }
                            }

                            item(span = {
                                GridItemSpan(columnCount)
                            }) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier =
                                        Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth(),
                                ) {
                                    item {
                                        VerticalLabel(
                                            chapters.count().toString(),
                                            stringResource(R.string.saga_detail_section_title_chapters),
                                            saga.data.genre,
                                        )
                                    }

                                    item {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(16.dp),
                                        ) {
                                            Text(
                                                messages.count().toString(),
                                                style =
                                                    MaterialTheme.typography.headlineLarge.copy(
                                                        fontFamily = saga.data.genre.headerFont(),
                                                        fontWeight = FontWeight.Normal,
                                                        textAlign = TextAlign.Center,
                                                    ),
                                                modifier =
                                                    Modifier
                                                        .padding(2.dp)
                                                        .fillMaxWidth(),
                                            )

                                            Text(
                                                stringResource(R.string.saga_detail_messages_label),
                                                style =
                                                    MaterialTheme.typography.bodySmall.copy(
                                                        fontFamily = saga.data.genre.bodyFont(),
                                                        fontWeight = FontWeight.Light,
                                                        textAlign = TextAlign.Center,
                                                    ),
                                                modifier = Modifier.alpha(.4f),
                                            )
                                        }
                                    }
                                    item {
                                        VerticalLabel(
                                            saga.characters.count().toString(),
                                            stringResource(R.string.saga_detail_section_title_characters),
                                            saga.data.genre,
                                        )
                                    }
                                }
                            }

                            if (saga.data.isEnded) {
                                item(span = {
                                    GridItemSpan(columnCount)
                                }) {
                                    RecapHeroCard(
                                        saga = saga,
                                        modifier =
                                            Modifier
                                                .padding(16.dp)
                                                .fillMaxWidth(),
                                    ) {
                                        openReview()
                                    }
                                }
                            }

                            item(span = { GridItemSpan(columnCount) }) {
                                AnimatedPlaytimeCounter(
                                    playtimeMs = saga.data.playTimeMs,
                                    label = stringResource(R.string.playtime_title),
                                    genre = saga.data.genre,
                                    textStyle = MaterialTheme.typography.headlineSmall,
                                )
                            }

                            saga.mainCharacter?.let {
                                item(span = { GridItemSpan(columnCount) }) {
                                    Text(
                                        stringResource(R.string.starring),
                                        style = sectionStyle,
                                        modifier = Modifier.padding(16.dp),
                                    )
                                }

                                item(span = { GridItemSpan(columnCount) }) {
                                    Box(
                                        Modifier
                                            .padding(16.dp)
                                            .clip(shape = genre.shape())
                                            .border(1.dp, genre.color.gradientFade(), genre.shape())
                                            .background(genre.color.gradientFade())
                                            .fillMaxWidth()
                                            .height(300.dp)
                                            .clickable {
                                                selectSection(DetailAction.CHARACTERS)
                                            },
                                    ) {
                                        AsyncImage(
                                            it.data.image,
                                            contentDescription = it.data.name,
                                            modifier =
                                                Modifier
                                                    .fillMaxSize()
                                                    .effectForGenre(genre, useFallBack = true),
                                            contentScale = ContentScale.Crop,
                                        )

                                        Box(
                                            Modifier
                                                .align(Alignment.BottomCenter)
                                                .fillMaxWidth()
                                                .fillMaxHeight(.8f)
                                                .background(
                                                    fadeGradientBottom(genre.color),
                                                ),
                                        )

                                        Column(
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .align(Alignment.BottomCenter)
                                                    .padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            Text(
                                                it.data.name,
                                                style =
                                                    MaterialTheme.typography.headlineMedium.copy(
                                                        fontFamily = genre.headerFont(),
                                                        color = genre.iconColor,
                                                    ),
                                                modifier =
                                                    Modifier.reactiveShimmer(
                                                        true,
                                                        genre.shimmerColors(),
                                                        duration = 10.seconds,
                                                    ),
                                            )

                                            Text(
                                                it.data.backstory,
                                                maxLines = 4,
                                                style =
                                                    MaterialTheme.typography.labelMedium.copy(
                                                        fontFamily = genre.bodyFont(),
                                                        color = genre.iconColor,
                                                    ),
                                            )
                                        }
                                    }
                                }
                            }

                            item(span = { GridItemSpan(columnCount) }) {
                                BackupStatusCard(
                                    backupEnabled,
                                    genre,
                                    onExportClick = {
                                        onExportSaga()
                                    },
                                    Modifier
                                        .padding(horizontal = 16.dp)
                                        .fillMaxWidth(),
                                )
                            }

                            item(span = { GridItemSpan(columnCount) }) {
                                Text(
                                    stringResource(R.string.saga_detail_description_section_title),
                                    style = sectionStyle,
                                    modifier =
                                        Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth(),
                                )
                            }

                            item(span = { GridItemSpan(columnCount) }) {
                                Text(
                                    saga.data.description,
                                    style =
                                        MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = saga.data.genre.bodyFont(),
                                            textAlign = TextAlign.Justify,
                                        ),
                                    modifier = Modifier.padding(16.dp),
                                )
                            }

                            if (saga.characters.isNotEmpty()) {
                                item(span = {
                                    GridItemSpan(columnCount)
                                }) {
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
                                                    .sharedElement(
                                                        rememberSharedContentState(
                                                            key =
                                                                DetailAction.CHARACTERS.sharedElementTitleKey(
                                                                    saga.data.id,
                                                                )!!,
                                                        ),
                                                        animatedVisibilityScope = animationScopes.second,
                                                    )
                                                    .padding(8.dp)
                                                    .weight(1f),
                                        )

                                        IconButton(onClick = {
                                            selectSection(DetailAction.CHARACTERS)
                                        }, modifier = Modifier.size(24.dp)) {
                                            Icon(
                                                painterResource(R.drawable.round_arrow_forward_ios_24),
                                                contentDescription = stringResource(R.string.saga_detail_view_characters_action),
                                            )
                                        }
                                    }
                                }

                                item(span = { GridItemSpan(columnCount) }) {
                                    LazyRow {
                                        items(
                                            sortCharactersByMessageCount(
                                                saga.getCharacters(),
                                                saga.flatMessages(),
                                            ),
                                        ) { char ->

                                            Column(
                                                Modifier
                                                    .padding(8.dp)
                                                    .clip(saga.data.genre.shape())
                                                    .clickable {
                                                        selectSection(DetailAction.CHARACTERS)
                                                    },
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                            ) {
                                                val characterModifier =
                                                    this@with.sharedTransitionActionItemModifier(
                                                        DetailAction.CHARACTERS,
                                                        animationScopes.second,
                                                        saga.data.id,
                                                        char.id,
                                                    )
                                                CharacterAvatar(
                                                    char,
                                                    borderSize = 2.dp,
                                                    genre = saga.data.genre,
                                                    modifier =
                                                        characterModifier
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

                                if (relationships.isNotEmpty()) {
                                    item(span = { GridItemSpan(columnCount) }) {
                                        Text(
                                            stringResource(R.string.saga_detail_relationships_section_title),
                                            style = sectionStyle,
                                            modifier =
                                                Modifier
                                                    .padding(16.dp)
                                                    .fillMaxWidth(),
                                        )
                                    }

                                    item(span = { GridItemSpan(columnCount) }) {
                                        LazyRow {
                                            items(
                                                relationships,
                                            ) { relation ->
                                                RelationShipCard(
                                                    content = relation,
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

                            if (events.isNotEmpty()) {
                                item(span = {
                                    GridItemSpan(columnCount)
                                }) {
                                    Column {
                                        Row(
                                            Modifier
                                                .padding(16.dp)
                                                .fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                stringResource(R.string.saga_detail_timeline_section_title),
                                                style = sectionStyle,
                                                modifier =
                                                    Modifier
                                                        .sharedElement(
                                                            rememberSharedContentState(
                                                                key =
                                                                    DetailAction.TIMELINE.sharedElementTitleKey(
                                                                        saga.data.id,
                                                                    )!!,
                                                            ),
                                                            animatedVisibilityScope = animationScopes.second,
                                                        )
                                                        .padding(8.dp)
                                                        .weight(1f),
                                            )

                                            IconButton(onClick = {
                                                selectSection(
                                                    DetailAction.TIMELINE,
                                                )
                                            }, modifier = Modifier.size(24.dp)) {
                                                Icon(
                                                    painterResource(R.drawable.round_arrow_forward_ios_24),
                                                    contentDescription = stringResource(R.string.saga_detail_view_timeline_action),
                                                )
                                            }
                                        }

                                        events.lastOrNull()?.let { event ->
                                            val eventModifier =
                                                this@with.sharedTransitionActionItemModifier(
                                                    DetailAction.TIMELINE,
                                                    animationScopes.second,
                                                    saga.data.id,
                                                    event.data.id,
                                                )
                                            TimeLineCard(
                                                event,
                                                saga,
                                                false,
                                                openCharacters = {
                                                    selectSection(
                                                        DetailAction.CHARACTERS,
                                                    )
                                                },
                                                modifier =
                                                    eventModifier
                                                        .padding(16.dp)
                                                        .clip(RoundedCornerShape(saga.data.genre.cornerSize()))
                                                        .clickable {
                                                            selectSection(
                                                                DetailAction.TIMELINE,
                                                            )
                                                        }
                                                        .fillMaxWidth()
                                                        .wrapContentHeight(),
                                            )
                                        } ?: run {
                                            Text(
                                                stringResource(R.string.saga_detail_no_events_placeholder),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth(),
                                            )
                                        }
                                    }
                                }
                            }

                            if (saga.wikis.isNotEmpty()) {
                                item(span = {
                                    GridItemSpan(columnCount)
                                }) {
                                    Column {
                                        Row(
                                            Modifier
                                                .padding(16.dp)
                                                .fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                stringResource(R.string.saga_detail_section_title_wiki),
                                                style = sectionStyle,
                                                modifier =
                                                    Modifier
                                                        .sharedElement(
                                                            rememberSharedContentState(
                                                                key =
                                                                    DetailAction.WIKI.sharedElementTitleKey(
                                                                        saga.data.id,
                                                                    )!!,
                                                            ),
                                                            animatedVisibilityScope = animationScopes.second,
                                                        )
                                                        .padding(8.dp)
                                                        .weight(1f),
                                            )

                                            IconButton(onClick = {
                                                selectSection(DetailAction.WIKI)
                                            }, modifier = Modifier.size(24.dp)) {
                                                Icon(
                                                    painterResource(R.drawable.round_arrow_forward_ios_24),
                                                    contentDescription = stringResource(R.string.saga_detail_view_wiki_action),
                                                )
                                            }
                                        }

                                        if (saga.wikis.isEmpty()) {
                                            Text(
                                                stringResource(R.string.saga_detail_no_wiki_info_placeholder),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth(),
                                            )
                                        }
                                    }
                                }

                                items(saga.wikis.takeLast(4)) { wiki ->

                                    val wikiModifier =
                                        this@with.sharedTransitionActionItemModifier(
                                            DetailAction.WIKI,
                                            animationScopes.second,
                                            saga.data.id,
                                            wiki.id,
                                        )

                                    WikiCard(
                                        wiki,
                                        saga.data.genre,
                                        modifier =
                                            wikiModifier
                                                .padding(16.dp)
                                                .fillMaxWidth(),
                                    )
                                }
                            }

                            if (chapters.isNotEmpty()) {
                                item(span = {
                                    GridItemSpan(columnCount)
                                }) {
                                    Row(
                                        Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            stringResource(R.string.saga_detail_section_title_chapters),
                                            style = sectionStyle,
                                            modifier =
                                                Modifier
                                                    .sharedElement(
                                                        rememberSharedContentState(
                                                            key =
                                                                DetailAction.CHAPTERS.sharedElementTitleKey(
                                                                    saga.data.id,
                                                                )!!,
                                                        ),
                                                        animatedVisibilityScope = animationScopes.second,
                                                    )
                                                    .padding(8.dp)
                                                    .weight(1f),
                                        )

                                        IconButton(onClick = {
                                            selectSection(
                                                DetailAction.CHAPTERS,
                                            )
                                        }, modifier = Modifier.size(24.dp)) {
                                            Icon(
                                                painterResource(R.drawable.round_arrow_forward_ios_24),
                                                contentDescription = stringResource(R.string.saga_detail_view_chapters_action),
                                            )
                                        }
                                    }
                                }

                                item(span = {
                                    GridItemSpan(columnCount)
                                }) {
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(chapters) { chapter ->
                                            val chapterModifier =
                                                this@with.sharedTransitionActionItemModifier(
                                                    DetailAction.CHAPTERS,
                                                    animationScopes.second,
                                                    saga.data.id,
                                                    chapter.data.id,
                                                )
                                            ChapterCardView(
                                                saga,
                                                chapter.data,
                                                chapterModifier
                                                    .clip(genre.shape())
                                                    .clickable {
                                                        selectSection(
                                                            DetailAction.CHAPTERS,
                                                        )
                                                    }
                                                    .size(250.dp)
                                                    .padding(8.dp),
                                            )
                                        }
                                    }
                                }
                            }

                            if (saga.data.isEnded) {
                                item(span = {
                                    GridItemSpan(columnCount)
                                }) {
                                    Box(
                                        Modifier
                                            .clickable {
                                                selectSection(DetailAction.ACTS)
                                            }
                                            .height(200.dp)
                                            .fillMaxWidth()
                                            .reactiveShimmer(
                                                true,
                                                saga.data.genre.color
                                                    .darkerPalette()
                                                    .plus(Color.Transparent),
                                            ),
                                    ) {
                                        StarryTextPlaceholder(
                                            modifier =
                                                Modifier.fillMaxSize(),
                                        )
                                        Text(
                                            stringResource(id = R.string.see_full_story),
                                            textAlign = TextAlign.Center,
                                            style =
                                                MaterialTheme.typography.headlineSmall.copy(
                                                    fontFamily = saga.data.genre.bodyFont(),
                                                    fontWeight = FontWeight.Bold,
                                                    brush = saga.data.genre.gradient(),
                                                ),
                                            modifier = Modifier.align(Alignment.Center),
                                        )
                                    }
                                }

                                item(span = {
                                    GridItemSpan(columnCount)
                                }) {
                                    Text(
                                        saga.data.endMessage,
                                        style =
                                            MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = saga.data.genre.bodyFont(),
                                                textAlign = TextAlign.Justify,
                                                brush = saga.data.genre.gradient(),
                                            ),
                                        modifier =
                                            Modifier
                                                .alpha(.6f)
                                                .padding(16.dp),
                                    )
                                }

                                item(span = { GridItemSpan(columnCount) }) {
                                    Box(
                                        Modifier
                                            .padding(16.dp)
                                            .clip(
                                                genre.shape(),
                                            )
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.onBackground.gradientFade(),
                                                genre.shape(),
                                            )
                                            .background(
                                                MaterialTheme.colorScheme.surfaceContainer,
                                                genre.shape(),
                                            )
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clickable {
                                                openEmotionalReview()
                                            },
                                    ) {
                                        AsyncImage(
                                            emotionalReviewIconUrl,
                                            null,
                                            colorFilter =
                                                ColorFilter.tint(
                                                    genre.color,
                                                    blendMode = BlendMode.Multiply,
                                                ),
                                            modifier =
                                                Modifier
                                                    .fillMaxSize()
                                                    .zoomAnimation(),
                                            contentScale = ContentScale.Crop,
                                        )

                                        Column(
                                            modifier =
                                                Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .background(
                                                        fadeGradientBottom(),
                                                    )
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 16.dp, vertical = 24.dp),
                                        ) {
                                            Text(
                                                stringResource(id = R.string.inner_journey),
                                                style =
                                                    MaterialTheme.typography.headlineSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                    ),
                                                modifier = Modifier.padding(vertical = 16.dp),
                                            )

                                            Text(
                                                stringResource(id = R.string.inner_journey_description),
                                                style =
                                                    MaterialTheme.typography.bodySmall.copy(),
                                            )
                                        }
                                    }
                                }
                            } else {
                                item(span = { GridItemSpan(columnCount) }) {
                                    Box(
                                        Modifier
                                            .height(200.dp)
                                            .fillMaxWidth()
                                            .reactiveShimmer(
                                                true,
                                                saga.data.genre.color
                                                    .darkerPalette()
                                                    .plus(Color.Transparent),
                                            ),
                                    ) {
                                        StarryTextPlaceholder(
                                            modifier =
                                                Modifier.fillMaxSize(),
                                        )
                                        Text(
                                            stringResource(id = R.string.keep_moving_forward),
                                            textAlign = TextAlign.Center,
                                            style =
                                                MaterialTheme.typography.headlineSmall.copy(
                                                    fontFamily = saga.data.genre.bodyFont(),
                                                    fontWeight = FontWeight.Bold,
                                                    brush = saga.data.genre.gradient(),
                                                ),
                                            modifier = Modifier.align(Alignment.Center),
                                        )
                                    }
                                }
                            }

                            item(span = {
                                GridItemSpan(columnCount)
                            }) {
                                Button(
                                    onClick = {
                                        selectSection(
                                            DetailAction.DELETE,
                                        )
                                    },
                                    colors =
                                        ButtonDefaults.textButtonColors(
                                            contentColor = MaterialColor.Red400,
                                        ),
                                    modifier =
                                        Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth(),
                                ) {
                                    Text(
                                        stringResource(R.string.saga_detail_delete_saga_button),
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(
                            gridState.canScrollBackward,
                            enter = fadeIn(tween(400, delayMillis = 200)),
                            exit = fadeOut(tween(200)),
                        ) {
                            val titleAndSub = DetailAction.BACK.titleAndSubtitle(saga)
                            SagaTopBar(
                                titleAndSub.first,
                                titleAndSub.second,
                                saga.data.genre,
                                onBackClick = { onBackClick() },
                                actionContent = {
                                    Icon(
                                        painterResource(R.drawable.ic_spark),
                                        null,
                                        tint = saga.data.genre.color,
                                        modifier =
                                            Modifier
                                                .clickable {
                                                    openDrawer()
                                                }
                                                .padding(horizontal = 8.dp)
                                                .size(24.dp),
                                    )
                                },
                                modifier =
                                    Modifier
                                        .background(MaterialTheme.colorScheme.background)
                                        .fillMaxWidth()
                                        .statusBarsPadding(),
                            )
                        }

                        val tooltipState =
                            androidx.compose.material3.rememberTooltipState(isPersistent = true)
                        val tooltipPositionProvider =
                            androidx.compose.material3.TooltipDefaults.rememberPlainTooltipPositionProvider(
                                spacingBetweenTooltipAndAnchor = 8.dp,
                            )
                        val coroutineScope = rememberCoroutineScope()
                        if (BuildConfig.DEBUG) {
                            Box(Modifier.align(Alignment.BottomEnd)) {
                                TooltipBox(
                                    tooltipPositionProvider,
                                    state = tooltipState,
                                    tooltip = {
                                        Column(
                                            Modifier
                                                .background(
                                                    MaterialTheme.colorScheme.surfaceContainer,
                                                    genre.shape(),
                                                )
                                                .padding(8.dp),
                                        ) {
                                            Text(stringResource(id = R.string.image_adjustment))

                                            SimpleSlider(
                                                stringResource(id = R.string.hue_tolerance),
                                                maxValue = 1f,
                                                value = highlightParams.hueTolerance,
                                            ) { value ->
                                                highlightParams =
                                                    highlightParams.copy(hueTolerance = value)
                                            }

                                            SimpleSlider(
                                                stringResource(id = R.string.saturation_threshold),
                                                maxValue = 1f,
                                                value = highlightParams.saturationThreshold,
                                            ) { value ->
                                                highlightParams =
                                                    highlightParams.copy(saturationThreshold = value)
                                            }

                                            SimpleSlider(
                                                stringResource(id = R.string.lightness_threshold),
                                                maxValue = 1f,
                                                value = highlightParams.lightnessThreshold,
                                            ) { value ->
                                                highlightParams =
                                                    highlightParams.copy(lightnessThreshold = value)
                                            }

                                            SimpleSlider(
                                                stringResource(id = R.string.highlight_boost),
                                                maxValue = 2f,
                                                value = highlightParams.highlightSaturationBoost,
                                            ) { value ->
                                                highlightParams =
                                                    highlightParams.copy(highlightSaturationBoost = value)
                                            }

                                            SimpleSlider(
                                                stringResource(id = R.string.highlight_lightness_boost),
                                                maxValue = 2f,
                                                value = highlightParams.highlightLightnessBoost,
                                            ) { value ->
                                                highlightParams =
                                                    highlightParams.copy(highlightLightnessBoost = value)
                                            }

                                            SimpleSlider(
                                                stringResource(id = R.string.desaturation_factor),
                                                maxValue = 1f,
                                                value = highlightParams.desaturationFactorNonTarget,
                                            ) { value ->
                                                highlightParams =
                                                    highlightParams.copy(desaturationFactorNonTarget = value)
                                            }
                                        }
                                    },
                                    modifier =
                                        Modifier
                                            .padding(16.dp)
                                            .align(Alignment.BottomCenter),
                                ) {
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                tooltipState.show()
                                            }
                                        },
                                        colors =
                                            IconButtonDefaults.iconButtonColors().copy(
                                                genre.color,
                                                genre.iconColor,
                                            ),
                                        modifier =
                                            Modifier
                                                .size(48.dp),
                                    ) {
                                        Icon(
                                            painterResource(R.drawable.outline_filter_vintage_24),
                                            null,
                                            tint = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.size(24.dp),
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
}

@Composable
private fun BackupStatusCard(
    backupEnabled: Boolean,
    genre: Genre,
    onExportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Background uses surfaceContainer with subtle onBackground overlay
    MaterialTheme.colorScheme.surfaceContainer
    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
    // Shape from genre
    val shape = genre.shape()
    // Icon based on status
    val iconRes = if (backupEnabled) R.drawable.ic_check_circle else R.drawable.ic_warning
    val iconTint =
        if (backupEnabled) {
            MaterialColor.GreenA100
        } else {
            MaterialColor.Yellow200
        }

    Row(
        modifier =
            modifier
                .alpha(.5f)
                .border(1.dp, iconTint.darker().gradientFade(), shape)
                .background(iconTint.copy(alpha = .2f), shape)
                .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp),
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text =
                    if (backupEnabled) {
                        stringResource(R.string.backup_enabled_label)
                    } else {
                        stringResource(
                            R.string.backup_disabled_label,
                        )
                    },
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = iconTint.darker(),
            )
            Text(
                text =
                    if (backupEnabled) {
                        stringResource(R.string.backup_enabled_message)
                    } else {
                        stringResource(R.string.backup_disabled_message)
                    },
                style = MaterialTheme.typography.bodySmall,
                color = iconTint.darker(),
            )
        }

        Button(
            onClick = onExportClick,
            colors = ButtonDefaults.textButtonColors(contentColor = iconTint.darker()),
        ) {
            Text(
                stringResource(R.string.export_button_label),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
fun RecapHeroCard(
    saga: SagaContent,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val stats =
        listOf(
            stringResource(R.string.recap_messages_sent, saga.flatMessages().size),
            stringResource(R.string.recap_characters_found, saga.characters.size),
            stringResource(R.string.recap_chapters_lived, saga.flatChapters().size),
            stringResource(R.string.recap_revisit_now),
        )
    var currentIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2500)
            currentIndex = (currentIndex + 1) % stats.size
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(170.dp)
                .dropShadow(
                    RoundedCornerShape(15.dp),
                    androidx.compose.ui.graphics.shadow.Shadow(
                        5.dp,
                        saga.data.genre.gradient(true),
                    ),
                )
                .clip(saga.data.genre.shape())
                .border(
                    1.dp,
                    saga.data.genre.gradient(),
                    saga.data.genre.shape(),
                )
                .clickable {
                    onClick()
                },
    ) {
        saga.data.icon.let {
            AsyncImage(
                it,
                contentDescription = null,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .effectForGenre(saga.data.genre),
                contentScale = ContentScale.Crop,
            )
        }

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        fadeGradientBottom(),
                    ),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .reactiveShimmer(true)
                    .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = stringResource(R.string.recap_your_journey),
                style =
                    MaterialTheme.typography.displaySmall.copy(
                        fontFamily = saga.data.genre.headerFont(),
                        brush =
                            saga.data.genre.gradient(),
                        shadow = Shadow(saga.data.genre.color, blurRadius = 15f),
                    ),
            )

            AnimatedContent(
                targetState = currentIndex,
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) + slideInVertically { it } togetherWith
                        fadeOut(animationSpec = tween(500)) + slideOutVertically { -it }
                },
            ) { index ->
                Text(
                    text = stats[index],
                    style =
                        MaterialTheme.typography.headlineSmall.copy(
                            color = saga.data.genre.iconColor,
                            fontFamily = saga.data.genre.bodyFont(),
                            textAlign = TextAlign.Center,
                        ),
                    modifier = Modifier.padding(8.dp),
                )
            }
        }
    }
}
