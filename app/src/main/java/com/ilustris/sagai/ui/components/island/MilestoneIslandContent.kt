package com.ilustris.sagai.ui.components.island

import androidx.compose.runtime.Composable
import com.ilustris.sagai.R
import com.ilustris.sagai.features.act.data.model.UnifiedActUpdate
import com.ilustris.sagai.features.chapter.data.model.UnifiedChapterUpdate
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.features.saga.chat.presentation.model.SagaMilestone
import com.ilustris.sagai.features.saga.chat.ui.components.milestone.ObjectiveOverlay
import com.ilustris.sagai.features.timeline.data.model.UnifiedLoreUpdate
import com.ilustris.sagai.features.wiki.data.model.Wiki

/**
 * Milestones don't get a full-screen overlay of their own anymore — they're just island content
 * whose [IslandContent.Expanded] happens to be a milestone reveal ([MilestoneIslandBody]),
 * reusing the same compact-pill + tap-to-expand mechanics as the objective/advance islands.
 * [IslandContent.autoExpandAfterMs] opens them automatically shortly after publish, so the
 * reveal still presents itself without requiring a tap.
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
 * Bottom-island reveal covering [SagaMilestone.NewEvent], [SagaMilestone.ChapterFinished] and
 * [SagaMilestone.ActFinished] — these are all the *result* of the user pulling the narrative-
 * advance trigger, so they take over the same bottom slot rather than a separate system.
 * Shows visual galleries of created characters/wikis and emotional tone instead of
 * a full dashboard — matching the compact Dynamic Island philosophy.
 */
class NarrativeMilestoneIslandContent(
    private val milestone: SagaMilestone,
    private val genre: Genre,
    private val characters: List<Character>,
    private val wikis: List<Wiki>,
    private val emotionalTone: EmotionalTone,
    private val onRevealStarted: () -> Unit,
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
            onRevealStarted = onRevealStarted,
        )
    }
}
