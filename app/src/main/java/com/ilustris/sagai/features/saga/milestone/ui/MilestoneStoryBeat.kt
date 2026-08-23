package com.ilustris.sagai.features.saga.milestone.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import com.ilustris.sagai.R
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.act.data.model.BookGenerationUiState
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.features.saga.milestone.presentation.MilestoneUiState
import com.ilustris.sagai.ui.genre.surface.StoryActionEmphasis
import com.ilustris.sagai.ui.genre.surface.StoryAside
import com.ilustris.sagai.ui.genre.surface.StoryBeat
import com.ilustris.sagai.ui.genre.surface.StoryBeatAction
import com.ilustris.sagai.ui.genre.surface.StoryBeatTone
import com.ilustris.sagai.ui.genre.surface.StoryProgress
import com.ilustris.sagai.ui.navigation.SagaActsKey
import com.ilustris.sagai.ui.navigation.SagaChaptersKey
import com.ilustris.sagai.ui.navigation.SagaEventsKey
import com.ilustris.sagai.features.wiki.data.model.Wiki

/**
 * Turns a milestone into the genre-neutral description a
 * [com.ilustris.sagai.ui.genre.surface.GenreStorySurface] can render.
 *
 * All the screen's genre knowledge ends here: nothing below this file branches on genre, and this
 * file never mentions one. Every label is resolved to a real string first, because the surfaces
 * deliberately hold no string resources of their own.
 */
@Composable
fun MilestoneUiState.ClosureStep.toStoryBeat(
    sagaId: Int,
    sagaTitle: String?,
    coverImage: String?,
    actCoverImages: List<String>,
    bookGenerationState: BookGenerationUiState,
    onContinue: () -> Unit,
    onNavigate: (NavKey) -> Unit,
    onGenerateBook: (Act) -> Unit,
): StoryBeat {
    val figures =
        when {
            milestone is SagaMilestone.ActFinished -> actCoverImages
            coverImage != null -> listOf(coverImage)
            else -> emptyList()
        }

    return StoryBeat(
        // The milestone itself, not the whole beat: covers are generated fire-and-forget and land
        // after this is already on screen, and re-keying then would restart every reveal.
        key = milestone,
        eyebrow = stringResource(milestone.title),
        title = milestone.subtitle,
        body = milestone.message?.takeIf { it.isNotBlank() },
        verb = milestone.terminalVerb(),
        source = sagaTitle,
        figures = figures,
        entries = milestone.wikis,
        entriesLabel = stringResource(R.string.milestone_wikis_created),
        cast = milestone.characters,
        castLabel = stringResource(R.string.milestone_characters_created),
        aside =
            milestone.emotionalReviewText?.takeIf { it.isNotBlank() }?.let {
                StoryAside(label = stringResource(R.string.milestone_emotional_note_label), text = it)
            },
        progress = StoryProgress(index = stepIndex, total = stepTotal),
        actions =
            buildList {
                milestone.detailDestination(sagaId)?.let { destination ->
                    add(
                        StoryBeatAction(
                            id = "details",
                            label = stringResource(R.string.milestone_view_details),
                            onClick = { onNavigate(destination) },
                        ),
                    )
                }
                if (milestone is SagaMilestone.ActFinished) {
                    val generating =
                        (bookGenerationState as? BookGenerationUiState.Generating)?.actId == milestone.act.id
                    add(
                        StoryBeatAction(
                            id = "generate_book",
                            label =
                                stringResource(
                                    if (generating) R.string.milestone_generating_book else R.string.milestone_generate_book,
                                ),
                            busy = generating,
                            onClick = { onGenerateBook(milestone.act) },
                        ),
                    )
                }
                add(
                    StoryBeatAction(
                        id = "continue",
                        label = stringResource(R.string.continue_button),
                        emphasis = StoryActionEmphasis.PRIMARY,
                        onClick = onContinue,
                    ),
                )
            },
    )
}

/**
 * The cold open of a chapter or act. Unstepped by design, and its actions are held back until the
 * surface has finished revealing — this beat exists to be read, not clicked past.
 */
@Composable
fun SagaMilestone.Introduction.toStoryBeat(
    sagaTitle: String?,
    onContinue: () -> Unit,
): StoryBeat =
    StoryBeat(
        key = this,
        eyebrow = number.takeIf { it.isNotBlank() },
        title = titleText,
        body = introduction.takeIf { it.isNotBlank() },
        verb = if (type == com.ilustris.sagai.features.saga.chat.presentation.model.IntroductionType.ACT) "act --open" else "chapter --open",
        source = sagaTitle,
        tone = StoryBeatTone.ANNOUNCEMENT,
        gateActionsOnReveal = true,
        actions =
            listOf(
                StoryBeatAction(
                    id = "continue",
                    label = stringResource(R.string.continue_button),
                    emphasis = StoryActionEmphasis.PRIMARY,
                    onClick = onContinue,
                ),
            ),
    )

/**
 * What the Terminal surface prints as the command that produced this beat. Only that one style uses
 * it; every other ignores it entirely.
 */
private fun SagaMilestone.terminalVerb(): String =
    when (this) {
        is SagaMilestone.NewEvent -> "event --log"
        is SagaMilestone.ChapterFinished -> "chapter --close"
        is SagaMilestone.ActFinished -> "act --close"
        is SagaMilestone.NewCharacter -> "cast --add"
        else -> "story --sync"
    }

/** The AI's own reflection on the emotional arc — a distinct voice from the story itself. */
private val SagaMilestone.emotionalReviewText: String?
    get() =
        when (this) {
            is SagaMilestone.NewEvent -> timeline.emotionalReview
            is SagaMilestone.ChapterFinished -> chapter.emotionalReview
            is SagaMilestone.ActFinished -> act.emotionalReview
            else -> null
        }

/** Only the three closure variants carry generated lore. */
private val SagaMilestone.wikis: List<Wiki>
    get() =
        when (this) {
            is SagaMilestone.NewEvent -> wikis
            is SagaMilestone.ChapterFinished -> wikis
            is SagaMilestone.ActFinished -> wikis
            else -> emptyList()
        }

/** Same trio of variants as [wikis]. */
private val SagaMilestone.characters: List<Character>
    get() =
        when (this) {
            is SagaMilestone.NewEvent -> characters
            is SagaMilestone.ChapterFinished -> characters
            is SagaMilestone.ActFinished -> characters
            else -> emptyList()
        }

/**
 * Pushed on top of the Milestone screen, not replacing it — the chain keeps waiting on its own
 * continue calls regardless of what's on screen, so "peeking" at the list and coming back leaves
 * the reveal exactly where the player left it.
 */
private fun SagaMilestone.detailDestination(sagaId: Int): NavKey? =
    when (this) {
        is SagaMilestone.NewEvent -> SagaEventsKey(sagaId.toString())
        is SagaMilestone.ChapterFinished -> SagaChaptersKey(sagaId.toString())
        is SagaMilestone.ActFinished -> SagaActsKey(sagaId.toString())
        else -> null
    }
