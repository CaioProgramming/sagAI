package com.ilustris.sagai.features.saga.detail.ui

import ai.atick.material.MaterialColor
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.core.data.State
import com.ilustris.sagai.core.narrative.UpdateRules
import com.ilustris.sagai.core.utils.formatDate
import com.ilustris.sagai.core.utils.sortCharactersByMessageCount
import com.ilustris.sagai.features.act.ui.ActReader
import com.ilustris.sagai.features.act.ui.toRoman
import com.ilustris.sagai.features.chapter.ui.ChapterCardView
import com.ilustris.sagai.features.chapter.ui.ChapterContent
import com.ilustris.sagai.features.characters.relations.ui.RelationShipCard
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.characters.ui.CharactersGalleryContent
import com.ilustris.sagai.features.characters.ui.components.CharacterSection
import com.ilustris.sagai.features.characters.ui.components.VerticalLabel
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.features.saga.detail.presentation.SagaDetailViewModel
import com.ilustris.sagai.features.timeline.data.model.TimelineContent
import com.ilustris.sagai.features.timeline.ui.TimeLineCard
import com.ilustris.sagai.features.timeline.ui.TimeLineContent
import com.ilustris.sagai.features.wiki.ui.EmotionalSheet
import com.ilustris.sagai.features.wiki.ui.WikiCard
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.navigation.navigateToRoute
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.SagaTopBar
import com.ilustris.sagai.ui.theme.components.SparkLoader
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.fadeColors
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.fadeGradientTop
import com.ilustris.sagai.ui.theme.filters.SelectiveColorParams
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.genresGradient
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shape
import com.ilustris.sagai.ui.theme.zoomAnimation
import effectForGenre
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

enum class DetailAction {
    CHARACTERS,
    TIMELINE,
    CHAPTERS,
    WIKI,
    ACTS,
    BACK,
    DELETE,
    REGENERATE,
}

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
    var sagaToDelete by remember { mutableStateOf<Saga?>(null) }
    var section by remember {
        mutableStateOf(
            DetailAction.BACK,
        )
    }
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val emotionalCardReference by viewModel.emotionalCardReference.collectAsStateWithLifecycle()
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
        emotionalCardReference = emotionalCardReference,
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
        onBackClick = {
            when (it) {
                DetailAction.BACK, DetailAction.DELETE -> navHostController.popBackStack()
                else -> section = DetailAction.BACK
            }
        },
        createReview = {
            viewModel.createReview()
        },
        openReview = {
            // viewModel.resetReview()
        },
        createEmotionalReview = {
            if (saga?.data?.emotionalReview == null) {
                viewModel.createSagaEmotionalReview()
            }
        },
        createTimelineReview = {
            viewModel.createEmotionalReview(it)
        },
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
        Dialog(
            onDismissRequest = { },
            properties =
                DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                ),
        ) {
            val brush = saga?.data?.genre?.colorPalette() ?: holographicGradient
            StarryTextPlaceholder(
                modifier = Modifier.fillMaxSize().gradientFill(gradientAnimation(brush)),
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchSagaDetails(sagaId)
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
                            ).clickable {
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
                    .padding(16.dp)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.onBackground.copy(alpha = .1f),
                        shape,
                    ).background(
                        MaterialTheme.colorScheme.background,
                        shape,
                    ).padding(4.dp)
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
                                    }.fillMaxWidth(),
                        ) {
                            Image(
                                painterResource(R.drawable.ic_spark),
                                null,
                                Modifier.size(24.dp),
                            )
                            Text(
                                stringResource(
                                    R.string.saga_drawer_chapter_events_count,
                                    chapter.data.title.ifEmpty { "Capítulo em andamento..." },
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
                                    titleStyle = MaterialTheme.typography.bodyMedium,
                                    showText = false,
                                    showSpark = false,
                                    isLast = eventsInChapter.indexOf(event) == eventsInChapter.lastIndex,
                                    modifier =
                                        Modifier
                                            .padding(horizontal = 16.dp)
                                            .alpha(.7f),
                                )
                            }
                        }

                        if (chapter.isComplete().not()) {
                            Text(
                                "${eventsInChapter.size} of ${UpdateRules.CHAPTER_UPDATE_LIMIT} events",
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
                                modifier = Modifier.height(1.dp).padding(4.dp),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .1f),
                            )

                            if (chapter == chaptersInAct.last()) {
                                val messageCount =
                                    chapter.events
                                        .lastOrNull()
                                        ?.messages
                                        ?.size

                                Text(
                                    "$messageCount of ${UpdateRules.LORE_UPDATE_LIMIT} messages",
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
    generatingReview: Boolean = false,
    emotionalCardReference: String,
    onChangeSection: (DetailAction) -> Unit = {},
    onBackClick: (DetailAction) -> Unit = {},
    createReview: () -> Unit,
    openReview: () -> Unit,
    createTimelineReview: (TimelineContent) -> Unit,
    createEmotionalReview: () -> Unit,
) {
    val saga = ((state as? State.Success)?.data as? SagaContent)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showReview by remember { mutableStateOf(false) }
    var showEmotionalReview by remember { mutableStateOf(false) }

    saga?.let { sagaContent ->
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
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
                                    if (sagaContent.data.review == null && sagaContent.data.isEnded) {
                                        createReview.invoke()
                                    } else {
                                        showReview = true
                                    }
                                }
                            }
                        }
                    }
                },
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Scaffold(topBar = {
                        val titleAndSubtitle = currentSection.titleAndSubtitle(sagaContent)

                        AnimatedContent(titleAndSubtitle) { titleAndSub ->
                            SagaTopBar(
                                titleAndSub.first,
                                titleAndSub.second,
                                sagaContent.data.genre,
                                onBackClick = { onBackClick(currentSection) },
                                actionContent = {
                                    Icon(
                                        painterResource(R.drawable.ic_spark),
                                        null,
                                        tint = sagaContent.data.genre.color,
                                        modifier =
                                            Modifier
                                                .clickable {
                                                    scope.launch {
                                                        if (drawerState.isClosed) {
                                                            drawerState.open()
                                                        } else {
                                                            drawerState.close()
                                                        }
                                                    }
                                                }.padding(horizontal = 8.dp)
                                                .size(24.dp),
                                    )
                                },
                                modifier =
                                    Modifier
                                        .background(MaterialTheme.colorScheme.background)
                                        .fillMaxWidth()
                                        .padding(top = 50.dp, start = 16.dp),
                            )
                        }
                    }) { _ ->

                        AnimatedContent(currentSection, transitionSpec = {
                            fadeIn(tween(500)) + slideInVertically() togetherWith
                                fadeOut(
                                    tween(
                                        400,
                                    ),
                                )
                        }, modifier = Modifier.fillMaxSize().padding(top = 120.dp)) { section ->
                            when (section) {
                                DetailAction.CHARACTERS ->
                                    CharactersGalleryContent(
                                        sagaContent,
                                        onOpenEvent = {
                                            onChangeSection(DetailAction.TIMELINE)
                                        },
                                    )

                                DetailAction.TIMELINE ->
                                    TimeLineContent(
                                        sagaContent,
                                        generateEmotionalReview = {
                                            createTimelineReview(it)
                                        },
                                        openCharacters = { onChangeSection(DetailAction.CHARACTERS) },
                                    )

                                DetailAction.CHAPTERS ->
                                    ChapterContent(
                                        sagaContent,
                                    )

                                DetailAction.WIKI -> WikiContent(sagaContent)
                                DetailAction.ACTS -> ActContent(sagaContent)
                                else ->
                                    SagaDetailInitialView(
                                        sagaContent,
                                        Modifier
                                            .animateContentSize(),
                                        selectSection = { action ->
                                            onChangeSection(action)
                                        },
                                        emotionalReviewUrl = emotionalCardReference,
                                        openEmotionalReview = {
                                            if (sagaContent.data.emotionalReview == null) {
                                                createEmotionalReview()
                                            } else {
                                                showEmotionalReview = true
                                            }
                                        },
                                        openReview = {
                                            if (sagaContent.data.review != null) {
                                                showReview = true
                                            } else {
                                                createReview()
                                            }
                                        },
                                    )
                            }
                        }

                        if (showReview) {
                            ModalBottomSheet(
                                onDismissRequest = {
                                    showReview = false
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
    )
}

@Composable
private fun DetailAction.titleAndSubtitle(content: SagaContent): Pair<String, String> =
    when (this) {
        DetailAction.CHARACTERS ->
            stringResource(R.string.saga_detail_section_title_characters) to
                stringResource(
                    R.string.saga_detail_section_subtitle_characters,
                    content.characters.size,
                )

        DetailAction.TIMELINE ->
            stringResource(R.string.saga_detail_section_title_timeline) to
                stringResource(
                    R.string.saga_detail_section_subtitle_timeline,
                    content.eventsSize(),
                )

        DetailAction.CHAPTERS ->
            stringResource(R.string.saga_detail_section_title_chapters) to
                stringResource(
                    R.string.saga_detail_section_subtitle_chapters,
                    content.chaptersSize(),
                )

        DetailAction.WIKI ->
            stringResource(R.string.saga_detail_section_title_wiki) to
                stringResource(R.string.saga_detail_section_subtitle_wiki, content.wikis.size)

        DetailAction.ACTS ->
            stringResource(R.string.saga_detail_section_title_acts) to
                stringResource(R.string.saga_detail_section_subtitle_acts, content.acts.size)

        else -> {
            if (content.data.isEnded) {
                content.data.title to
                    stringResource(
                        R.string.saga_detail_status_ended,
                        content.data.endedAt.formatDate(),
                    )
            } else {
                content.data.title to
                    stringResource(
                        R.string.saga_detail_status_created,
                        content.data.createdAt.formatDate(),
                    )
            }
        }
    }

@Composable
fun WikiContent(saga: SagaContent) {
    LazyVerticalGrid(columns = GridCells.Fixed(2)) {
        items(saga.wikis) { wiki ->
            WikiCard(
                wiki,
                saga.data.genre,
                modifier =
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
            )
        }
    }
}

@Composable
fun ActContent(saga: SagaContent) {
    ActReader(saga)
}

@Composable
fun SimpleSlider(
    title: String,
    maxValue: Float = 10f,
    onValueChange: (Float) -> Unit = {},
) {
    Column(Modifier.padding(16.dp)) {
        var sliderPosition by remember { mutableFloatStateOf(0f) }

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
    saga: SagaContent?,
    modifier: Modifier,
    emotionalReviewUrl: String,
    onReachTop: () -> Unit = {},
    selectSection: (DetailAction) -> Unit = {},
    openReview: () -> Unit = {},
    openEmotionalReview: () -> Unit = {},
) {
    val columnCount = 2
    val sectionStyle =
        MaterialTheme.typography.headlineMedium.copy(
            fontFamily = saga?.data?.genre?.bodyFont(),
            fontWeight = FontWeight.Bold,
        )
    val gridState = rememberLazyGridState()

    LaunchedEffect(remember { derivedStateOf { gridState.firstVisibleItemIndex } }) {
        if (gridState.firstVisibleItemIndex == 0) {
            onReachTop()
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columnCount),
        modifier = modifier,
        state = gridState,
    ) {
        saga?.let {
            val acts = saga.acts.filter { it.isComplete() }
            val chapters = saga.flatChapters().filter { it.isComplete() }
            val events = saga.flatEvents().filter { it.isComplete() }
            val messages = saga.flatMessages()

            item(span = {
                GridItemSpan(columnCount)
            }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    var highlightParams by
                        remember {
                            mutableStateOf(
                                SelectiveColorParams(
                                    targetColor = it.data.genre.color,
                                    hueTolerance = .5f,
                                    saturationThreshold = .5f,
                                    highlightSaturationBoost = 1.6f,
                                    desaturationFactorNonTarget = .7f,
                                ),
                            )
                        }
                    Box(
                        modifier =
                            Modifier
                                .background(
                                    it.data.genre.color
                                        .gradientFade(),
                                ).height(400.dp)
                                .fillMaxWidth()
                                .clipToBounds(),
                    ) {
                        Image(
                            painterResource(R.drawable.ic_spark),
                            null,
                            modifier =
                                Modifier
                                    .clickable {
                                        if (it.data.icon.isEmpty()) {
                                            selectSection(DetailAction.REGENERATE)
                                        }
                                    }.align(Alignment.TopCenter)
                                    .gradientFill(
                                        it.data.genre.gradient(),
                                    ),
                        )
                        AsyncImage(
                            it.data.icon,
                            contentDescription = it.data.title,
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .selectiveColorHighlight(saga.data.genre.selectiveHighlight())
                                    .effectForGenre(saga.data.genre)
                                    .clipToBounds(),
                            contentScale = ContentScale.Crop,
                        )

                        Box(
                            Modifier
                                .align(Alignment.TopCenter)
                                .background(fadeGradientTop())
                                .fillMaxWidth()
                                .fillMaxHeight(.25f),
                        )

                        Box(
                            Modifier
                                .align(Alignment.BottomCenter)
                                .background(fadeGradientBottom())
                                .fillMaxWidth()
                                .fillMaxHeight(.3f),
                        )

                        it.mainCharacter?.let { mainChar ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier =
                                    Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(top = 150.dp),
                            ) {
                                CharacterAvatar(
                                    mainChar.data,
                                    borderSize = 3.dp,
                                    borderColor = it.data.genre.color,
                                    genre = it.data.genre,
                                    textStyle = MaterialTheme.typography.displayMedium,
                                    modifier =
                                        Modifier
                                            .padding(8.dp)
                                            .size(150.dp),
                                )
                                Text(
                                    stringResource(R.string.saga_detail_journey_of),
                                    style =
                                        MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = it.data.genre.bodyFont(),
                                            fontWeight = FontWeight.Light,
                                            textAlign = TextAlign.Center,
                                        ),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Text(
                                    it.mainCharacter.data.name,
                                    style =
                                        MaterialTheme.typography.displaySmall.copy(
                                            fontFamily = it.data.genre.headerFont(),
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign = TextAlign.Center,
                                            brush = it.data.genre.gradient(),
                                        ),
                                    modifier =
                                        Modifier
                                            .padding(8.dp)
                                            .reactiveShimmer(true),
                                )
                            }
                        }
                    }

                    /*SimpleSlider(
                        "Hue tolerance",
                        maxValue = 1f,
                    ) { value ->
                        highlightParams = highlightParams.copy(hueTolerance = value)
                    }

                    SimpleSlider(
                        "Saturation threshold",
                        maxValue = 1f,
                    ) { value ->
                        highlightParams = highlightParams.copy(saturationThreshold = value)
                    }

                    SimpleSlider(
                        "Lightness threshold",
                        maxValue = 1f,
                    ) { value ->
                        highlightParams = highlightParams.copy(lightnessThreshold = value)
                    }

                    SimpleSlider(
                        "Highlight boost",
                        maxValue = 2f,
                    ) { value ->
                        highlightParams = highlightParams.copy(highlightSaturationBoost = value)
                    }

                    SimpleSlider(
                        "Highlight lightness boost",
                        maxValue = 1f,
                    ) { value ->
                        highlightParams = highlightParams.copy(highlightLightnessBoost = value)
                    }

                    SimpleSlider(
                        "Desaturation Factor",
                        maxValue = 1f,
                    ) { value ->
                        highlightParams = highlightParams.copy(desaturationFactorNonTarget = value)
                    }*/
                }
            }

            item(span = {
                GridItemSpan(columnCount)
            }) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier =
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                ) {
                    VerticalLabel(
                        chapters.count().toString(),
                        stringResource(R.string.saga_detail_section_title_chapters),
                        it.data.genre,
                    )
                    VerticalLabel(
                        it.characters.count().toString(),
                        stringResource(R.string.saga_detail_section_title_characters),
                        it.data.genre,
                    )
                }
            }

            item(span = {
                GridItemSpan(columnCount)
            }) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        messages.count().toString(),
                        style =
                            MaterialTheme.typography.displaySmall.copy(
                                fontFamily = it.data.genre.headerFont(),
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center,
                            ),
                        modifier =
                            Modifier
                                .padding(8.dp)
                                .fillMaxWidth(),
                    )

                    Text(
                        stringResource(R.string.saga_detail_messages_label),
                        style =
                            MaterialTheme.typography.bodySmall.copy(
                                fontFamily = it.data.genre.bodyFont(),
                                fontWeight = FontWeight.Light,
                                textAlign = TextAlign.Center,
                            ),
                    )
                }
            }

            if (it.data.isEnded) {
                item(span = {
                    GridItemSpan(columnCount)
                }) {
                    Column(
                        modifier =
                            Modifier.padding(16.dp).fillMaxWidth().clickable {
                                openReview()
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            stringResource(R.string.saga_detail_see_your_now),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.alpha(.4f),
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            stringResource(R.string.saga_detail_recap_button),
                            style =
                                MaterialTheme.typography.displaySmall.copy(
                                    fontFamily = it.data.genre.headerFont(),
                                    fontWeight = FontWeight.Bold,
                                    brush = it.data.genre.gradient(),
                                    textAlign = TextAlign.Center,
                                ),
                            modifier =
                                Modifier.reactiveShimmer(
                                    true,
                                ),
                        )
                    }
                }
            }

            item(span = {
                GridItemSpan(columnCount)
            }) {
                CharacterSection(
                    stringResource(R.string.saga_detail_description_section_title),
                    it.data.description,
                    it.data.genre,
                )
            }

            if (it.characters.isNotEmpty()) {
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
                                    .padding(8.dp)
                                    .weight(1f),
                        )

                        IconButton(onClick = {
                            selectSection(DetailAction.CHARACTERS)
                        }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = stringResource(R.string.saga_detail_view_characters_action),
                            )
                        }
                    }
                }

                item(span = { GridItemSpan(columnCount) }) {
                    LazyRow {
                        items(
                            sortCharactersByMessageCount(
                                it.getCharacters(),
                                it.flatMessages(),
                            ),
                        ) { char ->
                            Column(
                                Modifier
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(it.data.genre.cornerSize()))
                                    .clickable {
                                        selectSection(DetailAction.CHARACTERS)
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                CharacterAvatar(
                                    char,
                                    borderSize = 2.dp,
                                    genre = it.data.genre,
                                    modifier =
                                        Modifier
                                            .padding(8.dp)
                                            .clip(CircleShape)
                                            .size(120.dp)
                                            .padding(8.dp),
                                )

                                Text(
                                    char.name,
                                    style =
                                        MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Light,
                                            textAlign = TextAlign.Center,
                                            fontFamily = it.data.genre.bodyFont(),
                                        ),
                                )
                            }
                        }
                    }
                }

                if (it.relationships.isNotEmpty()) {
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
                            items(it.relationships.sortedBy { it.data.lastUpdated }) { relation ->
                                RelationShipCard(
                                    content = relation,
                                    genre = it.data.genre,
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
                                        .padding(8.dp)
                                        .weight(1f),
                            )

                            IconButton(onClick = {
                                selectSection(
                                    DetailAction.TIMELINE,
                                )
                            }, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = stringResource(R.string.saga_detail_view_timeline_action),
                                )
                            }
                        }

                        events.lastOrNull()?.let { event ->
                            TimeLineCard(
                                event,
                                it,
                                false,
                                modifier =
                                    Modifier
                                        .padding(16.dp)
                                        .clip(RoundedCornerShape(it.data.genre.cornerSize()))
                                        .clickable {
                                            selectSection(
                                                DetailAction.TIMELINE,
                                            )
                                        }.fillMaxWidth()
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

            if (it.wikis.isNotEmpty()) {
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
                                        .padding(8.dp)
                                        .weight(1f),
                            )

                            IconButton(onClick = {
                                selectSection(DetailAction.WIKI)
                            }, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = stringResource(R.string.saga_detail_view_wiki_action),
                                )
                            }
                        }

                        if (it.wikis.isEmpty()) {
                            Text(
                                stringResource(R.string.saga_detail_no_wiki_info_placeholder),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                items(it.wikis.takeLast(4)) { wiki ->
                    WikiCard(
                        wiki,
                        it.data.genre,
                        modifier =
                            Modifier
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
                                    .padding(8.dp)
                                    .weight(1f),
                        )

                        IconButton(onClick = {
                            selectSection(
                                DetailAction.CHAPTERS,
                            )
                        }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = stringResource(R.string.saga_detail_view_chapters_action),
                            )
                        }
                    }
                }

                item(span = {
                    GridItemSpan(columnCount)
                }) {
                    LazyRow {
                        items(chapters) { chapter ->
                            ChapterCardView(
                                it,
                                chapter.data,
                                Modifier
                                    .padding(4.dp)
                                    .width(300.dp)
                                    .height(300.dp),
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
                            }.height(200.dp)
                            .fillMaxWidth()
                            .reactiveShimmer(
                                true,
                                it.data.genre.color
                                    .darkerPalette()
                                    .plus(Color.Transparent),
                            ),
                    ) {
                        StarryTextPlaceholder(
                            modifier =
                                Modifier.fillMaxSize(),
                        )
                        Text(
                            "Veja sua história completa",
                            textAlign = TextAlign.Center,
                            style =
                                MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = it.data.genre.bodyFont(),
                                    fontWeight = FontWeight.Bold,
                                    brush = it.data.genre.gradient(),
                                ),
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }

                item(span = {
                    GridItemSpan(columnCount)
                }) {
                    Text(
                        it.data.endMessage,
                        style =
                            MaterialTheme.typography.bodySmall.copy(
                                fontFamily = it.data.genre.bodyFont(),
                                textAlign = TextAlign.Justify,
                                brush = it.data.genre.gradient(),
                            ),
                        modifier = Modifier.alpha(.6f).padding(16.dp),
                    )
                }

                item(span = { GridItemSpan(columnCount) }) {
                    Box(
                        Modifier
                            .padding(16.dp)
                            .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = .2f), it.data.genre.shape())
                            .background(MaterialTheme.colorScheme.surfaceContainer, it.data.genre.shape())
                            .fillMaxWidth()
                            .height(200.dp)
                            .clickable {
                                openEmotionalReview()
                            },
                    ) {
                        AsyncImage(
                            emotionalReviewUrl,
                            null,
                            modifier = Modifier.fillMaxSize().zoomAnimation(),
                            contentScale = ContentScale.Crop,
                        )

                        Column(
                            modifier =
                                Modifier
                                    .align(Alignment.BottomCenter)
                                    .background(
                                        fadeGradientBottom(
                                            it.data.genre.color,
                                        ),
                                    ).fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 24.dp),
                        ) {
                            Text(
                                "Jornada Interior",
                                style =
                                    MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                    ),
                                modifier = Modifier.padding(vertical = 16.dp),
                            )

                            Text(
                                "Sua história não está apenas no que você fez, mas em como você sentiu.",
                                style =
                                    MaterialTheme.typography.bodySmall.copy(),
                            )
                        }
                    }
                }
            } else {
                item(span = { GridItemSpan(columnCount) }) {
                    Box(
                        Modifier.height(200.dp).fillMaxWidth().reactiveShimmer(
                            true,
                            it.data.genre.color
                                .darkerPalette()
                                .plus(Color.Transparent),
                        ),
                    ) {
                        StarryTextPlaceholder(
                            modifier =
                                Modifier.fillMaxSize(),
                        )
                        Text(
                            "Continue avançando em sua história...",
                            textAlign = TextAlign.Center,
                            style =
                                MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = it.data.genre.bodyFont(),
                                    fontWeight = FontWeight.Bold,
                                    brush = it.data.genre.gradient(),
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
    }
}
