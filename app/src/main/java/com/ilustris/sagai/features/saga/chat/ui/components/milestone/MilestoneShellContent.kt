package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterProfile
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.presentation.model.IntroductionType
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.ui.components.stylisedText
import com.ilustris.sagai.ui.components.taskshell.TaskShellContent
import com.ilustris.sagai.ui.components.taskshell.TaskShellContentPreview
import com.ilustris.sagai.ui.components.taskshell.TaskShellExpansion
import com.ilustris.sagai.ui.components.taskshell.TaskShellScope
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.levitate
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.sagaBrush
import com.ilustris.sagai.ui.theme.shimmerize
import com.ilustris.sagai.ui.theme.themePainter

/** Minimal compact header shared by milestone shell content — rarely seen in practice
 * since these are forced straight to [TaskShellExpansion.Full] the moment they appear. */
@Composable
private fun MilestoneCompactHeader(title: String) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            themePainter(),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(8.dp).size(24.dp).gradientFill(sagaBrush()),
        )

        if (title.isNotEmpty()) {
            Text(
                text = title,
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        textAlign = TextAlign.Center,
                        shadow =
                            Shadow(
                                MaterialTheme.colorScheme.primary,
                                blurRadius = 10f,
                            ),
                        brush = Brush.horizontalGradient(morphingGradient()),
                    ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Top slot, forced Full — the "open cinematic" act/chapter/resume introduction. */
class IntroductionShellContent(
    private val milestone: SagaMilestone.Introduction,
    private val saga: Saga,
) : TaskShellContent {
    override val isDraggable: Boolean get() = true
    override val isExpandable: Boolean get() = true

    @Composable
    override fun Compact(scope: TaskShellScope) {
        MilestoneCompactHeader(title = emptyString())
    }

    @Composable
    override fun Expanded(scope: TaskShellScope) {
        IntroductionOverlay(
            introduction = milestone,
            saga = saga,
            onComplete = { scope.onMinimize() },
        )
    }
}

/** Top slot, forced Full — new character reveal. */
class CharacterMilestoneShellContent(
    private val milestone: SagaMilestone.NewCharacter,
    private val saga: Saga,
    private val dashboardItems: List<MilestoneDashboardItem>,
    private val onRevealStarted: () -> Unit,
    private val onDetailAction: (MilestoneDetailAction) -> Unit,
) : TaskShellContent {
    override val isDraggable: Boolean get() = true
    override val isExpandable: Boolean get() = true

    @Composable
    override fun Compact(scope: TaskShellScope) {
        Text(
            stringResource(R.string.notification_new_character),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier =
                Modifier.fillMaxWidth().padding(8.dp).alpha(.5f).gradientFill(
                    Brush.horizontalGradient(morphingGradient()),
                ),
        )
    }

    @Composable
    override fun Expanded(scope: TaskShellScope) {
        val character = milestone.character
        val characterColor = character.hexColor.hexToColor() ?: MaterialTheme.colorScheme.primary
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize()) {
            if (character.image.isNotBlank()) {
                AsyncImage(
                    model = character.image,
                    contentDescription = null,
                    modifier =
                        Modifier
                            .padding(8.dp)
                            .border(1.dp, characterColor, MaterialTheme.shapes.large)
                            .clip(MaterialTheme.shapes.large)
                            .clickable {
                                scope.onMinimize()
                            }.fillMaxWidth()
                            .height(200.dp)
                            .effectForGenre(saga.genre)
                            .reactiveShimmer(true, Color.White.shimmerize(), repeatMode = RepeatMode.Restart),
                )
            }

            Text(
                text = character.name,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp).levitate(),
            )
        }
    }
}

/**
 * Bottom slot, forced Full — covers [SagaMilestone.Loading], [SagaMilestone.NewEvent],
 * [SagaMilestone.ChapterFinished] and [SagaMilestone.ActFinished]: these are all the
 * *result* of the user pulling the narrative-advance trigger, so they take over the same
 * slot rather than a separate full-screen system.
 */
class NarrativeMilestoneShellContent(
    private val milestone: SagaMilestone,
    private val saga: Saga,
    private val dashboardItems: List<MilestoneDashboardItem>,
    private val reasoningChunk: String?,
    private val onRevealStarted: () -> Unit,
    private val onDetailAction: (MilestoneDetailAction) -> Unit,
) : TaskShellContent {
    override val isDraggable: Boolean get() = true
    override val isExpandable: Boolean get() = true

    @Composable
    override fun Compact(scope: TaskShellScope) {
        AnimatedVisibility(scope.expansion == TaskShellExpansion.Collapsed) {
            MilestoneCompactHeader(title = stringResource(milestone.title))
        }
    }

    @Composable
    override fun Expanded(scope: TaskShellScope) {
        when (milestone) {
            is SagaMilestone.Loading -> {
                LoadingMilestoneOverlay(
                    saga = saga,
                    sparkModifier = Modifier,
                    titleModifier = Modifier,
                    contentReasoning = reasoningChunk,
                )
            }

            is SagaMilestone.NewEvent -> {
                EventMilestoneOverlay(
                    milestone = milestone,
                    genre = saga.genre,
                    dashboardItems = dashboardItems,
                    onDismiss = { scope.onMinimize() },
                    onRevealStarted = onRevealStarted,
                    onDetailAction = onDetailAction,
                )
            }

            is SagaMilestone.ChapterFinished -> {
                CinematicMilestoneOverlay(
                    milestone = milestone,
                    labelTitle = stringResource(milestone.title),
                    stylisedTitle = milestone.subtitle,
                    genre = saga.genre,
                    dashboardItems = dashboardItems,
                    sparkModifier = Modifier,
                    onDismiss = { scope.onMinimize() },
                    onRevealStarted = onRevealStarted,
                    onDetailAction = onDetailAction,
                )
            }

            is SagaMilestone.ActFinished -> {
                CinematicMilestoneOverlay(
                    milestone = milestone,
                    labelTitle = stringResource(milestone.title),
                    stylisedTitle = milestone.subtitle,
                    genre = saga.genre,
                    dashboardItems = dashboardItems,
                    sparkModifier = Modifier,
                    onDismiss = { scope.onMinimize() },
                    onRevealStarted = onRevealStarted,
                    onDetailAction = onDetailAction,
                )
            }

            is SagaMilestone.Introduction, is SagaMilestone.NewCharacter -> {
                Unit
            }
        }
    }
}

private val previewSaga = Saga(title = "Sombras de Poeira", genre = Genre.entries.first())

private val previewCharacter =
    Character(
        name = "Elara",
        lastName = "Vance",
        sagaId = previewSaga.id,
        details = Details(),
        profile = CharacterProfile(),
    )

private val previewAct = Act(title = "Ato I: O Despertar", sagaId = previewSaga.id)
private val previewChapter = Chapter(title = "O Limiar", actId = previewAct.id)
private val previewTimeline =
    Timeline(
        title = "A Revelação",
        currentObjective = "Encontrar a chave perdida antes que o sol se ponha.",
        chapterId = previewChapter.id,
    )
private val previewSagaContent = SagaContent(data = previewSaga)

private val previewDashboardItems =
    listOf(
        MilestoneDashboardItem(title = "Capítulos", subtitle = "7"),
        MilestoneDashboardItem(title = "Personagens", subtitle = "3"),
        MilestoneDashboardItem(title = "Mensagens", subtitle = "482"),
    )

@Preview(name = "Introduction", showBackground = true)
@Composable
private fun IntroductionShellContentPreview() {
    TaskShellContentPreview(
        content =
            IntroductionShellContent(
                milestone =
                    SagaMilestone.Introduction(
                        type = IntroductionType.CHAPTER,
                        titleText = "Capítulo 1",
                        introduction = "A jornada começa em uma noite escura e tempestuosa...",
                        number = "I",
                    ),
                saga = previewSaga,
            ),
        initialExpansion = TaskShellExpansion.Full,
    )
}

@Preview(name = "NewCharacter", showBackground = true)
@Composable
private fun CharacterMilestoneShellContentPreview() {
    TaskShellContentPreview(
        content =
            CharacterMilestoneShellContent(
                milestone = SagaMilestone.NewCharacter(character = previewCharacter, saga = previewSaga),
                saga = previewSaga,
                dashboardItems = previewDashboardItems,
                onRevealStarted = {},
                onDetailAction = {},
            ),
        initialExpansion = TaskShellExpansion.Full,
    )
}

@Preview(name = "Loading", showBackground = true)
@Composable
private fun NarrativeMilestoneShellContentLoadingPreview() {
    TaskShellContentPreview(
        content =
            NarrativeMilestoneShellContent(
                milestone = SagaMilestone.Loading,
                saga = previewSaga,
                dashboardItems = previewDashboardItems,
                reasoningChunk = "O vento sopra forte enquanto as portas se abrem...",
                onRevealStarted = {},
                onDetailAction = {},
            ),
        initialExpansion = TaskShellExpansion.Full,
        onTop = false,
    )
}

@Preview(name = "NewEvent", showBackground = true)
@Composable
private fun NarrativeMilestoneShellContentEventPreview() {
    TaskShellContentPreview(
        content =
            NarrativeMilestoneShellContent(
                milestone =
                    SagaMilestone.NewEvent(
                        timeline = previewTimeline,
                        emotionalMascot = null,
                        sagaContent = previewSagaContent,
                    ),
                saga = previewSaga,
                dashboardItems = previewDashboardItems,
                reasoningChunk = null,
                onRevealStarted = {},
                onDetailAction = {},
            ),
        initialExpansion = TaskShellExpansion.Full,
        onTop = false,
    )
}

@Preview(name = "ChapterFinished", showBackground = true)
@Composable
private fun NarrativeMilestoneShellContentChapterPreview() {
    TaskShellContentPreview(
        content =
            NarrativeMilestoneShellContent(
                milestone =
                    SagaMilestone.ChapterFinished(
                        chapter = previewChapter,
                        sagaContent = previewSagaContent,
                    ),
                saga = previewSaga,
                dashboardItems = previewDashboardItems,
                reasoningChunk = null,
                onRevealStarted = {},
                onDetailAction = {},
            ),
        initialExpansion = TaskShellExpansion.Full,
        onTop = false,
    )
}

@Preview(name = "ActFinished", showBackground = true)
@Composable
private fun NarrativeMilestoneShellContentActPreview() {
    TaskShellContentPreview(
        content =
            NarrativeMilestoneShellContent(
                milestone = SagaMilestone.ActFinished(act = previewAct),
                saga = previewSaga,
                dashboardItems = previewDashboardItems,
                reasoningChunk = null,
                onRevealStarted = {},
                onDetailAction = {},
            ),
        initialExpansion = TaskShellExpansion.Full,
        onTop = false,
    )
}
