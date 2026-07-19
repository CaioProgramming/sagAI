package com.ilustris.sagai.ui.components.island

import androidx.compose.runtime.Composable
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.ObjectiveOverlay
import com.ilustris.sagai.features.wiki.data.model.Wiki

/**
 * All milestone reveal UI is island content published by [SagaContentManagerImpl] — there is no
 * parallel per-screen rendering path. Everything here takes over the **top** (global) island slot,
 * matching Apple's Dynamic Island: one persistent surface for system-level state, not a second
 * one competing for attention at the bottom.
 */
private const val MILESTONE_AUTO_EXPAND_MS = 1_000L
private const val INTRODUCTION_AUTO_DISMISS_MS = 15_000L

/**
 * Top-island reveal for [SagaMilestone.Introduction] — the act/chapter/resume opening cinematic.
 * There's no dashboard data for an introduction (just narrative text), so — like the objective
 * island — the compact row itself carries the content (the saga's title, persistent) and the
 * expanded body is just that narrative text, tap-to-continue. `titleText` is intentionally blank
 * for RESUME-type introductions (no chapter/act to name), so `saga.title` reads correctly for
 * every type — `introduction` (not `messageText`) is the actual narrative blurb.
 */
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

/**
 * Top-island reveal covering [SagaMilestone.NewEvent], [SagaMilestone.ChapterFinished] and
 * [SagaMilestone.ActFinished] — these are all the *result* of the narrative advancing, so they
 * take over the same global slot as the objective/introduction rather than a separate bottom
 * system. Auto-expands to show the character/wiki galleries and emotional tone via
 * [MilestoneIslandBody].
 */
class NarrativeMilestoneIslandContent(
    private val milestone: SagaMilestone,
    private val genre: Genre,
    private val characters: List<Character>,
    private val wikis: List<Wiki>,
    private val emotionalTone: EmotionalTone,
    private val onContinue: () -> Unit,
) : IslandContent {
    override val compact: CompactIslandData =
        CompactIslandData(
            labelRes = milestone.title,
            iconRes = genre.icon,
            genre = genre,
            backgroundColor = IslandBackgroundColor.ThemeBackground,
        )
    override val autoExpandAfterMs: Long = MILESTONE_AUTO_EXPAND_MS

    @Composable
    override fun Expanded(scope: IslandScope) {
        MilestoneIslandBody(
            genre = genre,
            title = milestone.subtitle,
            description = milestone.message?.takeIf { it.isNotBlank() },
            characters = characters,
            wikis = wikis,
            emotionalTone = emotionalTone,
            onContinue = onContinue,
        )
    }
}
