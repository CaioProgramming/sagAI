package com.ilustris.sagai.ui.components.island

import androidx.compose.runtime.Composable
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.ObjectiveOverlay

/**
 * Top-island reveal for [SagaMilestone.Introduction] — the act/chapter/resume opening cinematic.
 * There's no dashboard data for an introduction (just narrative text), so — like the objective
 * island — the compact row itself carries the content (the saga's title, persistent) and the
 * expanded body is just that narrative text, tap-to-continue. `titleText` is intentionally blank
 * for RESUME-type introductions (no chapter/act to name), so `saga.title` reads correctly for
 * every type — `introduction` (not `messageText`) is the actual narrative blurb.
 *
 * Narrative-result milestones (NewEvent/ChapterFinished/ActFinished) no longer get an island —
 * their reveal already renders inline in the chat's message list, so a floating bottom island
 * duplicated the same content. They now take over the chat input slot with
 * [com.ilustris.sagai.features.saga.chat.ui.components.milestone.MilestoneContinuePanel] instead.
 */
private const val MILESTONE_AUTO_EXPAND_MS = 1_000L
private const val INTRODUCTION_AUTO_DISMISS_MS = 15_000L

class IntroductionIslandContent(
    private val milestone: SagaMilestone.Introduction,
    private val saga: Saga,
    private val onContinue: () -> Unit,
) : IslandContent {
    override val compact: CompactIslandData =
        CompactIslandData(
            label = saga.title,
            iconRes = saga.genre.icon,
            genre = saga.genre,
            backgroundColor = IslandBackgroundColor.ThemeBackground,
        )
    override val autoExpandAfterMs: Long = MILESTONE_AUTO_EXPAND_MS
    override val autoDismissAfterMs: Long = INTRODUCTION_AUTO_DISMISS_MS
    override val onAction: () -> Unit = onContinue

    @Composable
    override fun Expanded(scope: IslandScope) {
        ObjectiveOverlay(
            objective = milestone.introduction.ifBlank { saga.title },
            onDismiss = onContinue,
        )
    }
}
