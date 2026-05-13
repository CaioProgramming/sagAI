package com.ilustris.sagai.features.debug.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.core.narrative.NarrativeRules
import com.ilustris.sagai.core.utils.formatToJsonArray
import com.ilustris.sagai.core.utils.toJsonFormat
import com.ilustris.sagai.core.utils.toRoman
import com.ilustris.sagai.features.debug.presentation.DebugSection
import com.ilustris.sagai.features.debug.presentation.LoreDebugViewModel
import com.ilustris.sagai.features.home.data.model.ActMetadata
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.home.data.model.chapterNumber
import com.ilustris.sagai.features.home.data.model.findCharacter
import com.ilustris.sagai.features.home.data.model.flatChapters
import com.ilustris.sagai.features.home.data.model.flatEvents
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.ui.animations.genreVfx
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.SagaTopBar
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shape
import com.ilustris.sagai.ui.theme.themeShimmer
import com.ilustris.sagai.ui.theme.utils.toJsonAnnotatedString
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun LoreDebugView(
    sagaId: String,
    onBack: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedContentScope,
    viewModel: LoreDebugViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val saga = uiState.sagaMetadata
    val scrollState = rememberLazyListState()

    LaunchedEffect(sagaId) {
        viewModel.loadSaga(sagaId.toIntOrNull() ?: 0)
    }

    val view = LocalView.current
    DisposableEffect(uiState.isFixing) {
        if (uiState.isFixing) {
            view.keepScreenOn = true
        }
        onDispose {
            view.keepScreenOn = false
        }
    }

    Box(
        Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(uiState.isLoading) {
            if (it) {
                AnimatedContent(saga) {
                    val icon = it?.data?.genre?.icon ?: R.drawable.ic_spark
                    with(sharedTransitionScope) {
                        Image(
                            painterResource(icon),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                            modifier =
                                Modifier
                                    .size(150.dp)
                                    .sharedElement(
                                        rememberSharedContentState(key = "saga_${saga?.data?.id}_icon"),
                                        animatedVisibilityScope,
                                    )
                                    .reactiveShimmer(
                                        true,
                                        themeShimmer(),
                                        1.seconds,
                                        targetValue = 250f,
                                        repeatMode = RepeatMode.Restart,
                                    ),
                        )
                    }
                }
            } else {
                if (saga == null) {
                    Text(
                        "Saga not found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                } else {
                    val genre = saga.data.genre
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        state = scrollState,
                        modifier = Modifier.padding(bottom = 80.dp),
                    ) {
                        stickyHeader {
                            SagaTopBar(
                                subtitle = "Gerenciamento da história",
                                title = saga.data.title,
                                genre = genre,
                                onBackClick = onBack,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(16.dp),
                                titleModifier =
                                    with(sharedTransitionScope) {
                                        Modifier.sharedElement(
                                            rememberSharedContentState(key = "saga_${saga.data.id}_title"),
                                            animatedVisibilityScope,
                                        )
                                    },
                            )
                        }
                        item {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text("Story state")

                                Text(
                                    saga.data.worldState ?: "Nothing stated yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .7f),
                                )
                            }
                        }

                        items(saga.acts) {
                            StoryCard(
                                saga = saga,
                                actContent = it,
                                isLoading = uiState.generatingSections.contains(it.data.id.toString()) && uiState.reasoning != null,
                                reasoning = uiState.reasoning,
                                onRegenerate = { content, section ->
                                    viewModel.regenerateData(
                                        it.data.id.toString(),
                                        content,
                                        section,
                                    )
                                },
                            )
                        }
                    }

                    // Bottom Fix Button
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Button(
                            onClick = { viewModel.toggleFixConfirmation() },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                            shape = genre.shape(),
                            enabled = !uiState.isFixing,
                        ) {
                            Text(
                                if (uiState.isFixing) "Amarrando história..." else "Consertar História",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }

        if (uiState.showFixConfirmation && saga != null) {
            val rules = NarrativeRules() // Fallback if RC not ready, but VM uses actual one
            val actsToFix =
                saga.acts.count {
                    it.isFull(rules) &&
                        (it.data.emotionalReview.isNullOrEmpty() || it.data.narrativeGuide.isNullOrEmpty() || it.data.content.isEmpty())
                }
            val chaptersToFix =
                saga
                    .flatChapters()
                    .count {
                        it.isFull(rules) &&
                            (
                                it.data.emotionalReview.isNullOrEmpty() || it.data.narrativeGuide.isNullOrEmpty() ||
                                    it.data.overview.isEmpty()
                            )
                    }
            val timelinesToFix =
                saga
                    .flatEvents()
                    .count { it.isComplete(rules) && (it.data.emotionalReview.isNullOrEmpty() || it.data.narrativeGuide.isNullOrEmpty()) }

            AlertDialog(
                onDismissRequest = { viewModel.toggleFixConfirmation() },
                title = { Text("Amarrar História") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Deseja amarrar a história desta saga? Isso preencherá revisões emocionais e guias narrativos faltantes.")
                        Text("Itens a serem atualizados:", fontWeight = FontWeight.Bold)
                        if (timelinesToFix > 0) Text("• $timelinesToFix Linhas do tempo")
                        if (chaptersToFix > 0) Text("• $chaptersToFix Capítulos")
                        if (actsToFix > 0) Text("• $actsToFix Atos")

                        if (actsToFix + chaptersToFix + timelinesToFix == 0) {
                            Text(
                                "Toda a história já está bem amarrada!",
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                },
                confirmButton = {
                    if (actsToFix + chaptersToFix + timelinesToFix > 0) {
                        TextButton(onClick = { viewModel.fixStory() }) {
                            Text("Confirmar")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.toggleFixConfirmation() }) {
                        Text("Cancelar")
                    }
                },
            )
        }

        if (uiState.isFixing) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp),
                ) {
                    val genre = saga?.data?.genre ?: Genre.FANTASY
                    Icon(
                        painterResource(genre.icon),
                        contentDescription = null,
                        modifier =
                            Modifier
                                .size(64.dp)
                                .gradientFill(genre.gradient())
                                .genreVfx(genre),
                    )

                    Text(
                        "Amarrando a história...",
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = genre.headerFont(),
                        color = genre.resolveColor(),
                    )

                    LinearProgressIndicator(
                        progress = { uiState.currentFixItem.toFloat() / uiState.fixItemsCount.toFloat() },
                        modifier = Modifier.fillMaxWidth(),
                        color = genre.resolveColor(),
                        trackColor = genre.resolveColor().copy(alpha = 0.2f),
                    )

                    Text(
                        "Item ${uiState.currentFixItem} de ${uiState.fixItemsCount}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    )

                    uiState.reasoning?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StoryCard(
    saga: SagaMetadata,
    actContent: ActMetadata,
    isLoading: Boolean,
    reasoning: String?,
    onRegenerate: (Any, DebugSection) -> Unit,
) {
    val genre = saga.data.genre
    val shape = genre.shape()
    var isExpanded by remember { mutableStateOf(false) }
    val genreColor = genre.resolveColor()
    val borderColor by animateColorAsState(
        if (isExpanded) {
            genreColor
        } else {
            MaterialTheme.colorScheme.onBackground.copy(
                alpha = .1f,
            )
        },
    )
    val extraContent =
        remember(actContent) {
            mapOf(
                "Emotional review" to actContent.data.emotionalReview,
                "Narrative guide" to actContent.data.narrativeGuide,
            )
        }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier =
            Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor, shape)
                .clip(shape)
                .background(
                    MaterialTheme.colorScheme.surfaceContainer,
                    shape,
                )
                .padding(8.dp)
                .clickable {
                    isExpanded = !isExpanded
                }
                .animateContentSize(tween(500, easing = EaseIn)),
    ) {
        val act = actContent.data
        val actNumber = saga.actNumber(act.id)
        Text(
            stringResource(R.string.act_title, actNumber.toRoman()),
            style = MaterialTheme.typography.labelMedium,
            fontFamily = genre.bodyFont(),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f),
        )

        Row {
            Text(
                act.title,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = genre.headerFont(),
                fontWeight = if (act.narrativeGuide.isNullOrEmpty()) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                color = genre.resolveColor(),
                modifier = Modifier.weight(1f),
            )

            if (isLoading.not()) {
                IconButton(
                    onClick = {
                        onRegenerate(act, DebugSection.ACT_INTRODUCTION)
                    },
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        painterResource(R.drawable.ic_restore),
                        contentDescription = "Regenerate",
                        tint = genre.resolveColor(),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }

        if (isLoading) {
            Icon(
                painterResource(genre.icon),
                null,
                modifier =
                    Modifier
                        .size(50.dp)
                        .gradientFill(genre.gradient())
                        .genreVfx(genre),
            )

            reasoning?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = genre.bodyFont(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f),
                )
            }

            return@Column
        }

        if (isExpanded) {
            Text(
                act.introduction,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start,
                fontFamily = genre.bodyFont(),
                color = MaterialTheme.colorScheme.onSurface,
            )
            actContent.chapters.forEach { chapter ->
                var chapterExpanded by remember { mutableStateOf(false) }
                val extraContent =
                    remember {
                        mapOf(
                            "emotional review" to chapter.data.emotionalReview,
                            "featured characters" to
                                chapter.data.featuredCharacters.mapNotNull {
                                    saga.findCharacter(it)?.name
                                },
                            "narrative guide" to chapter.data.narrativeGuide,
                        ).toJsonFormat()
                    }
                Column(
                    Modifier
                        .padding(4.dp)
                        .clip(shape)
                        .background(
                            MaterialTheme.colorScheme.background.copy(alpha = .4f),
                            shape = shape,
                        )
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable {
                            chapterExpanded = !chapterExpanded
                        }
                        .animateContentSize(
                            tween(300, easing = EaseIn),
                        ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val chapterNumber = saga.chapterNumber(chapter.data.id)
                    Text(
                        stringResource(R.string.chapter_number_label, chapterNumber.toRoman()),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Normal,
                        fontFamily = genre.bodyFont(),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f),
                    )

                    Row {
                        Text(
                            chapter.data.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (chapter.data.narrativeGuide.isNullOrEmpty()) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = genre.headerFont(),
                            color = genreColor.copy(alpha = .7f),
                            modifier = Modifier.weight(1f),
                        )

                        IconButton(
                            onClick = {
                                onRegenerate(chapter, DebugSection.CHAPTER_INTRODUCTION)
                            },
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_restore),
                                contentDescription = "Regenerate",
                                tint = genreColor,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }

                    if (chapterExpanded) {
                        Text(
                            chapter.data.introduction,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Start,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .7f),
                            modifier = Modifier.weight(1f),
                        )

                        chapter.events.forEach { event ->
                            var eventExpanded by remember { mutableStateOf(false) }
                            val extraContent =
                                remember {
                                    mapOf(
                                        "Emotional review" to event.data.emotionalReview,
                                        "New Characters" to event.newlyAppearedCharacters.joinToString { it.name },
                                        "New wikis" to event.updatedWikis.formatToJsonArray(),
                                        "Character events" to event.characterEventDetails.size,
                                        "Relationships updates" to event.updatedRelationshipDetails.size,
                                        "Narrative guide" to event.data.narrativeGuide,
                                        "Final context" to event.data.sceneSummary?.toJsonFormat(),
                                    ).toJsonFormat()
                                }
                            Column(
                                Modifier
                                    .padding(8.dp)
                                    .clip(shape)
                                    .background(
                                        MaterialTheme.colorScheme.background.copy(alpha = .3f),
                                        shape = shape,
                                    )
                                    .clickable {
                                        eventExpanded = !eventExpanded
                                    }
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .animateContentSize(),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val fontWeight =
                                        when {
                                            event.data.narrativeGuide.isNullOrEmpty() -> FontWeight.Bold
                                            else -> FontWeight.Normal
                                        }
                                    Text(
                                        "${
                                            saga.flatEvents().indexOf(event)
                                        }. ${event.data.title}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontFamily = genre.bodyFont(),
                                        fontWeight = fontWeight,
                                        color = genreColor.copy(alpha = .7f),
                                        modifier = Modifier.weight(1f),
                                    )

                                    IconButton(
                                        onClick = {
                                            onRegenerate(
                                                event,
                                                DebugSection.TIMELINE,
                                            )
                                        },
                                        modifier = Modifier.size(24.dp),
                                    ) {
                                        Icon(
                                            painterResource(R.drawable.ic_restore),
                                            contentDescription = "Regenerate",
                                            tint = genre.resolveColor(),
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                }

                                if (eventExpanded) {
                                    Text(
                                        event.data.content,
                                        style = MaterialTheme.typography.labelMedium,
                                        textAlign = TextAlign.Start,
                                        fontFamily = genre.bodyFont(),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = .7f),
                                    )

                                    Text(
                                        extraContent.toJsonAnnotatedString(),
                                        style = MaterialTheme.typography.labelMedium,
                                        textAlign = TextAlign.Start,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f),
                                        modifier =
                                            Modifier
                                                .background(
                                                    MaterialTheme.colorScheme.background.copy(alpha = .3f),
                                                    shape,
                                                )
                                                .padding(8.dp),
                                    )
                                }
                            }
                        }

                        Text(
                            extraContent.toJsonAnnotatedString(),
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Start,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f),
                            modifier =
                                Modifier
                                    .background(
                                        MaterialTheme.colorScheme.background.copy(alpha = .3f),
                                        shape,
                                    )
                                    .padding(8.dp),
                        )

                        TextButton(onClick = {
                            onRegenerate(
                                chapter,
                                DebugSection.CHAPTER_CONCLUSION,
                            )
                        }, enabled = isLoading.not()) {
                            Text("Regenerate Chapter")
                            if (isLoading) {
                                Icon(
                                    painterResource(genre.icon),
                                    null,
                                    modifier =
                                        Modifier
                                            .size(12.dp)
                                            .effectForGenre(genre)
                                            .gradientFill(genre.gradient()),
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isExpanded) {
            Text(
                act.content,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                extraContent.toJsonFormat().toJsonAnnotatedString(),
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Start,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f),
                modifier =
                    Modifier
                        .background(
                            MaterialTheme.colorScheme.background.copy(alpha = .3f),
                            shape,
                        )
                        .padding(8.dp),
            )

            if (act.content.isNotEmpty()) {
                TextButton(
                    onClick = { onRegenerate(actContent, DebugSection.ACT_CONCLUSION) },
                    colors = ButtonDefaults.textButtonColors().copy(contentColor = genreColor),
                ) {
                    Text("Regenerate Act")

                    if (isLoading) {
                        Icon(
                            painterResource(genre.icon),
                            null,
                            modifier =
                                Modifier
                                    .size(12.dp)
                                    .effectForGenre(genre)
                                    .gradientFill(genre.gradient()),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DebugSection(
    title: String,
    subtitle: String,
    content: String,
    extraContent: List<String> = emptyList(),
    isGenerating: Boolean,
    genre: Genre,
    reasoning: String?,
    onRegenerate: () -> Unit,
) {
    val resolvedColor = genre.resolveColor()
    var isExpanded by remember { mutableStateOf(false) }
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            subtitle,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f),
            fontFamily = genre.bodyFont(),
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Normal,
                color = resolvedColor,
                fontFamily = genre.headerFont(),
                modifier =
                    Modifier
                        .weight(1f)
                        .clickable {
                            isExpanded = !isExpanded
                        },
            )

            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = resolvedColor,
                )
            } else {
                IconButton(
                    onClick = onRegenerate,
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        painterResource(R.drawable.ic_restore),
                        contentDescription = "Regenerate",
                        tint = resolvedColor,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }

        if (isGenerating && reasoning != null) {
            AnimatedContent(reasoning) {
                Text(
                    it,
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = genre.bodyFont(),
                    textAlign = TextAlign.Justify,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f),
                )
            }
            return@Column
        }

        if (isExpanded) {
            Text(
                content.ifEmpty { "Empty content" },
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = genre.bodyFont(),
                textAlign = TextAlign.Justify,
                color = MaterialTheme.colorScheme.onSurface,
            )
            extraContent.forEach {
                Text(
                    it.ifEmpty { "Empty content" }.toJsonAnnotatedString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = genre.bodyFont(),
                    textAlign = TextAlign.Justify,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f),
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
        )
    }
}
