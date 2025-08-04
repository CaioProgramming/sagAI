package com.ilustris.sagai.features.saga.detail.ui

import ai.atick.material.MaterialColor
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
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
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.formatDate
import com.ilustris.sagai.core.utils.getEventsForChapter
import com.ilustris.sagai.core.utils.sortCharactersByMessageCount
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.ui.ActComponent
import com.ilustris.sagai.features.act.ui.toRoman
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.chapter.ui.ChapterCardView
import com.ilustris.sagai.features.chapter.ui.ChapterContent
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.characters.ui.CharactersGalleryContent
import com.ilustris.sagai.features.characters.ui.components.CharacterSection
import com.ilustris.sagai.features.characters.ui.components.VerticalLabel
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.Message
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.SenderType
import com.ilustris.sagai.features.saga.detail.presentation.SagaDetailViewModel
import com.ilustris.sagai.features.timeline.ui.TimeLineCard
import com.ilustris.sagai.features.timeline.ui.TimeLineContent
import com.ilustris.sagai.features.wiki.ui.WikiCard
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.navigation.navigateToRoute
import com.ilustris.sagai.ui.theme.GradientType
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.SagaTopBar
import com.ilustris.sagai.ui.theme.components.SparkLoader
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.fadedGradientTopAndBottom
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.genresGradient
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.reactiveShimmer
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

    SagaDetailContentView(state, section, paddingValues, onChangeSection = {
        section = it
        if (it == DetailAction.DELETE) {
            sagaToDelete = saga?.data
            showDeleteConfirmation = true
        }

        if (it == DetailAction.REGENERATE) {
            viewModel.regenerateIcon()
        }
    }, onBackClick = {
        when (it) {
            DetailAction.BACK, DetailAction.DELETE -> navHostController.popBackStack()
            else -> section = DetailAction.BACK
        }
    }, createReview = {
        viewModel.createReview()
    }, openReview = {
        viewModel.resetReview()
    })

    if (showDeleteConfirmation) {
        sagaToDelete?.let {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = {
                    Text(
                        "Confirmar Exclusão",
                        style =
                            MaterialTheme.typography.titleLarge.copy(
                                fontFamily = it.genre.bodyFont(),
                            ),
                    )
                },
                text = {
                    Text(
                        "Tem certeza que deseja excluir esta saga? Esta ação não pode ser desfeita.",
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
                    ) { Text("Excluir") }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDeleteConfirmation = false },
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                        shape = RoundedCornerShape(it.genre.cornerSize()),
                    ) { Text("Cancelar") }
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SparkLoader(
                    brush = gradientAnimation(genresGradient(), duration = 2.seconds),
                    modifier = Modifier.size(100.dp),
                )
            }
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
                Image(
                    painterResource(R.drawable.ic_spark),
                    null,
                    Modifier
                        .clip(CircleShape)
                        .padding(4.dp)
                        .size(50.dp)
                        .align(Alignment.CenterHorizontally)
                        .gradientFill(
                            content.data.genre.gradient(
                                animated = true,
                                duration = 2.seconds,
                            ),
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
                    content.currentActInfo?.act?.id == it.id,
                    gradientType = GradientType.LINEAR,
                    targetValue = 200f,
                )

            val chaptersInAct = content.chapters.filter { chapter -> chapter.actId == it.id }
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
                    "Ato ${(index + 1).toRoman()}",
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontFamily = content.data.genre.bodyFont(),
                            fontWeight = FontWeight.Bold,
                        ),
                    modifier = Modifier.padding(16.dp),
                )

                HorizontalDivider(modifier = Modifier.padding(4.dp))

                if (chaptersInAct.isNotEmpty()) {
                    chaptersInAct.forEachIndexed { chapterIndex, chapter ->
                        val eventsInChapter =
                            content.timelines.getEventsForChapter(
                                chapter,
                                previousChapter = chaptersInAct.getOrNull(chapterIndex - 1),
                            )

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
                                "${chapter.title} - ${eventsInChapter.size} eventos",
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
                                    content.data.genre,
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
                    }
                }
            }
        }
    }

    if (content.timelines.isNotEmpty()) {
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }

        item {
            val eventReference =
                if (content.chapters.isNotEmpty()) {
                    content.timelines.find { it.id == content.chapters.last().eventReference }
                } else {
                    content.timelines.last()
                }

            val eventIndex =
                if (content.timelines.indexOf(eventReference) == -1) {
                    0
                } else {
                    content.timelines.indexOf(eventReference)
                }

            val eventSublist = content.timelines.subList(eventIndex, content.timelines.size)

            val remainEvents = (UpdateRules.CHAPTER_UPDATE_LIMIT - eventSublist.size).unaryPlus()

            AnimatedVisibility(content.data.isEnded.not()) {
                Text(
                    "${eventSublist.size} eventos desde o último capitulo.",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SagaDetailContentView(
    state: State,
    currentSection: DetailAction = DetailAction.BACK,
    paddingValues: PaddingValues,
    generatingReview: Boolean = false,
    onChangeSection: (DetailAction) -> Unit = {},
    onBackClick: (DetailAction) -> Unit = {},
    createReview: () -> Unit,
    openReview: () -> Unit,
) {
    val saga = ((state as? State.Success)?.data as? SagaContent)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showReview by remember { mutableStateOf(false) }

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
                                        "Progresso",
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
                                    if (sagaContent.data.review == null) {
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
                        }, modifier = Modifier.fillMaxSize().padding(top = 100.dp)) { section ->
                            when (section) {
                                DetailAction.CHARACTERS ->
                                    CharactersGalleryContent(
                                        sagaContent,
                                    )

                                DetailAction.TIMELINE ->
                                    TimeLineContent(
                                        sagaContent,
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
                    }
                }
            }
        }
    }
}

private fun DetailAction.titleAndSubtitle(content: SagaContent) =
    when (this) {
        DetailAction.CHARACTERS -> "Personagens" to "${content.characters.size} personagens"
        DetailAction.TIMELINE -> "Eventos" to "${content.timelines.size} eventos"
        DetailAction.CHAPTERS -> "Capítulos" to "${content.chapters.size} capítulos"
        DetailAction.WIKI -> "Wiki" to "${content.wikis.size} itens"
        DetailAction.ACTS -> "Atos" to "${content.acts.size} atos"
        else -> {
            if (content.data.isEnded) {
                content.data.title to "Finalizado em ${content.data.endedAt.formatDate()}"
            } else {
                content.data.title to "Criado em ${content.data.createdAt.formatDate()}"
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
                        .padding(8.dp)
                        .fillMaxWidth()
                        .height(300.dp),
            )
        }
    }
}

@Composable
fun ActContent(saga: SagaContent) {
    VerticalPager(
        modifier = Modifier.fillMaxSize(),
        state = rememberPagerState { saga.acts.size },
    ) {
        val act = saga.acts[it]
        ActComponent(
            act,
            saga.acts.indexOf(act) + 1,
            saga,
            modifier =
                Modifier
                    .fillMaxSize(),
        )
    }
}

@Composable
private fun SagaDetailInitialView(
    saga: SagaContent?,
    modifier: Modifier,
    onReachTop: () -> Unit = {},
    selectSection: (DetailAction) -> Unit = {},
    openReview: () -> Unit = {},
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
            item(span = {
                GridItemSpan(columnCount)
            }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clipToBounds(),
                    ) {
                        if (it.data.icon
                                .isNullOrEmpty()
                                .not()
                        ) {
                            AsyncImage(
                                it.data.icon,
                                contentDescription = it.data.title,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(300.dp)
                                        .background(
                                            it.data.genre.color
                                                .gradientFade(),
                                        ).effectForGenre(saga.data.genre)
                                        .selectiveColorHighlight(
                                            saga.data.genre.selectiveHighlight(),
                                        ).clipToBounds(),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Box(
                                Modifier
                                    .clickable {
                                        selectSection(DetailAction.REGENERATE)
                                    }.fillMaxWidth()
                                    .height(300.dp)
                                    .background(MaterialTheme.colorScheme.surfaceContainer),
                            ) {
                                Image(
                                    painterResource(R.drawable.ic_spark),
                                    null,
                                    modifier =
                                        Modifier.align(Alignment.Center).gradientFill(
                                            it.data.genre.gradient(),
                                        ),
                                )
                            }
                        }

                        Box(
                            Modifier
                                .background(fadedGradientTopAndBottom())
                                .fillMaxWidth()
                                .height(300.dp),
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
                                    mainChar,
                                    borderSize = 3.dp,
                                    genre = it.data.genre,
                                    textStyle = MaterialTheme.typography.displayMedium,
                                    modifier =
                                        Modifier
                                            .padding(8.dp)
                                            .size(170.dp),
                                )
                                Text(
                                    "A jornada de",
                                    style =
                                        MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = it.data.genre.bodyFont(),
                                            fontWeight = FontWeight.Light,
                                            textAlign = TextAlign.Center,
                                        ),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Text(
                                    it.mainCharacter.name,
                                    style =
                                        MaterialTheme.typography.displaySmall.copy(
                                            fontFamily = it.data.genre.headerFont(),
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign = TextAlign.Center,
                                        ),
                                    modifier =
                                        Modifier
                                            .padding(8.dp)
                                            .gradientFill(it.data.genre.gradient(true)),
                                )
                            }
                        }
                    }
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
                    VerticalLabel(it.chapters.count().toString(), "Capítulos", it.data.genre)
                    VerticalLabel(it.characters.count().toString(), "Personagens", it.data.genre)
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
                        it.messages.count().toString(),
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
                        "Mensagens",
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
                                openReview.invoke()
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "Veja Agora seu",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.alpha(.4f),
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            "Recap",
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
                    "Descrição",
                    it.data.description,
                    it.data.genre,
                )
            }

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
                        "Personagens",
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
                            contentDescription = "Ver personagens",
                        )
                    }
                }
            }

            item(span = { GridItemSpan(columnCount) }) {
                LazyRow {
                    items(sortCharactersByMessageCount(it.characters, it.messages)) { char ->
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

            if (it.timelines.isNotEmpty()) {
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
                                "Linha do tempo",
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
                                    contentDescription = "Ver Linha do tempo",
                                )
                            }
                        }

                        it.timelines.lastOrNull()?.let { event ->
                            TimeLineCard(
                                event,
                                it.data.genre,
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
                                "Nenhum evento registrado",
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
                                "Wiki",
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
                                    contentDescription = "Ver wiki",
                                )
                            }
                        }

                        if (it.wikis.isEmpty()) {
                            Text(
                                "Nenhuma informação salva",
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
                                .padding(4.dp)
                                .fillMaxWidth()
                                .height(270.dp),
                    )
                }
            }

            if (it.chapters.isNotEmpty()) {
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
                            "Capítulos",
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
                                contentDescription = "Ver personagens",
                            )
                        }
                    }
                }

                item(span = {
                    GridItemSpan(columnCount)
                }) {
                    LazyRow {
                        items(it.chapters) { chapter ->
                            ChapterCardView(
                                chapter,
                                it.data.genre,
                                it.characters,
                                Modifier
                                    .padding(4.dp)
                                    .width(300.dp)
                                    .height(300.dp),
                            )
                        }
                    }
                }
            }

            if (it.acts.isNotEmpty()) {
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
                            "Atos",
                            style = sectionStyle,
                            modifier =
                                Modifier
                                    .padding(8.dp)
                                    .weight(1f),
                        )

                        IconButton(onClick = {
                            selectSection(
                                DetailAction.ACTS,
                            )
                        }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = "Ver personagens",
                            )
                        }
                    }
                }

                item(span = {
                    GridItemSpan(columnCount)
                }) {
                    val act = it.acts.last()
                    ActComponent(
                        act,
                        it.acts.indexOf(act) + 1,
                        it,
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .height(300.dp),
                    )
                }
            }

            if (it.data.isEnded) {
                item(span = {
                    GridItemSpan(columnCount)
                }) {
                    Text(
                        it.data.endMessage,
                        style =
                            MaterialTheme.typography.bodySmall.copy(
                                fontFamily = it.data.genre.bodyFont(),
                                fontWeight = FontWeight.Light,
                                textAlign = TextAlign.Center,
                                brush = it.data.genre.gradient(),
                            ),
                    )
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
                    Text("Excluir Saga", textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SagaDetailContentViewLoadingPreview() {
    SagAIScaffold {
        SagaDetailContentView(
            state = State.Loading,
            paddingValues = PaddingValues(0.dp),
            createReview = {},
            openReview = {},
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_TYPE_NORMAL,
)
@Composable
fun SagaDetailContentViewPreview() {
    SagAIScaffold {
        val state =
            State.Success(
                SagaContent(
                    data =
                        Saga(
                            title = "Saga de teste",
                            description = "Descrição da saga de teste",
                            icon = null,
                            createdAt = System.currentTimeMillis(),
                            genre = Genre.SCI_FI,
                            mainCharacterId = null,
                        ),
                    acts =
                        List(3) {
                            Act()
                        },
                    mainCharacter =
                        Character(
                            name = "Personagem de teste",
                            backstory = "Descrição do personagem de teste",
                            image = emptyString(),
                            hexColor = "#FFFFFF",
                            details = Details(),
                        ),
                    messages =
                        List(10) {
                            MessageContent(
                                Message(
                                    id = 0,
                                    sagaId = 0,
                                    text = "Mensagem de teste",
                                    senderType = SenderType.entries.random(),
                                    timestamp = System.currentTimeMillis(),
                                ),
                            )
                        },
                    chapters =
                        List(3) {
                            Chapter(
                                title = "Capítulo de teste",
                                sagaId = 0,
                                overview = "Texto do capítulo de teste",
                                messageReference = 0,
                                coverImage = emptyString(),
                                actId = it,
                            )
                        },
                    characters =
                        List(5) {
                            Character(
                                name = "Personagem de teste $it",
                                backstory = "Descrição do personagem de teste",
                                image = emptyString(),
                                hexColor = "#FFFFFF",
                                details = Details(),
                            )
                        },
                ),
            )
        SagaDetailContentView(
            state = state,
            paddingValues = PaddingValues(0.dp),
            createReview = {},
            openReview = {},
        )
    }
}
