package com.ilustris.sagai.features.saga.milestone.ui.skin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.genre.terminal.TerminalBackground
import com.ilustris.sagai.ui.genre.terminal.TerminalGlitchOverlay
import com.ilustris.sagai.ui.genre.terminal.TerminalProgress
import com.ilustris.sagai.ui.theme.filters.crtScreen

/**
 * Cyberpunk/Space Opera's chrome for the Milestone screen — the same phosphor-panel identity
 * [com.ilustris.sagai.features.saga.detail.ui.SagaReview]'s `TerminalReviewContainer` wears for
 * the story review, applied here around one milestone step instead of a review page.
 *
 * Layering deliberately mirrors that container rather than improvising a new one: the whole stack
 * — background *and* [content] — sits inside a single [crtScreen], so the tube's curvature/bloom
 * resample the actual milestone copy instead of just a backdrop sitting behind it, and
 * [TerminalGlitchOverlay] rides on top of everything as the terminal's one topmost layer. That
 * overlay is Cyberpunk's own signal corruption, not Space Opera's — a working console's picture
 * doesn't tear (see `TerminalReviewContainer`'s own note on that split).
 *
 * The step indicator is char-cell [TerminalProgress] rather than the plain dot one, drawn here as
 * an overlay pinned to the same top-of-screen spot
 * [MilestoneClosureContent][com.ilustris.sagai.features.saga.milestone.ui.MilestoneClosureContent]'s
 * own layout would otherwise reserve for it (matching its `systemBarsPadding()` +
 * `padding(horizontal = 32.dp, vertical = 24.dp)`) — not inside that layout, so the caller must
 * pass an empty `stepIndicator` slot into it whenever this skin is active, or the two would render
 * on top of each other. See [MilestoneSkinChrome]'s doc for that contract.
 */
@Composable
fun TerminalMilestoneSkin(
    genre: Genre,
    stepIndex: Int? = null,
    stepTotal: Int? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.fillMaxSize().crtScreen()) {
        TerminalBackground(Modifier.fillMaxSize())

        content()

        if (stepIndex != null && stepTotal != null && stepTotal > 1) {
            TerminalProgress(
                current = stepIndex,
                total = stepTotal,
                color = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .systemBarsPadding()
                        .padding(horizontal = 32.dp, vertical = 24.dp),
            )
        }

        // Glitch is Cyberpunk's own corruption, not the terminal's — see the container this
        // mirrors for the full reasoning.
        if (genre == Genre.CYBERPUNK) {
            TerminalGlitchOverlay(Modifier.fillMaxSize())
        }
    }
}
