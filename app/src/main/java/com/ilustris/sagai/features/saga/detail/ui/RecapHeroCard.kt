package com.ilustris.sagai.features.saga.detail.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.saga.detail.data.model.completedStepCount
import com.ilustris.sagai.features.saga.detail.data.model.isComplete
import com.ilustris.sagai.features.saga.detail.review.domain.ReviewGenerationState
import com.ilustris.sagai.ui.genre.recap.GenreRecapCard
import com.ilustris.sagai.ui.genre.recap.RecapCard
import com.ilustris.sagai.ui.genre.recap.RecapProgress
import com.ilustris.sagai.ui.genre.recap.RecapStat

/** How many steps a full review is made of — the denominator the "almost ready" copy counts toward. */
private const val REVIEW_STEP_COUNT = 6

/**
 * The card offering a finished saga's recap. All of this screen's genre knowledge ends here: it
 * resolves every label to a real string, describes the card as a [RecapCard], and hands it to
 * [GenreRecapCard], which picks the treatment from the theme.
 *
 * Mirrors what `MilestoneStoryBeat` does for the Milestone screen, and for the same reason — the
 * neutral card package deliberately owns no string resources.
 */
@Composable
fun RecapHeroCard(
    saga: Saga,
    chaptersCount: Int,
    charactersCount: Int,
    messagesCount: Int,
    reviewGenerationState: ReviewGenerationState,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val stats =
        listOf(
            RecapStat(messagesCount.toString(), stringResource(R.string.recap_stat_messages)),
            RecapStat(charactersCount.toString(), stringResource(R.string.recap_stat_characters)),
            RecapStat(chaptersCount.toString(), stringResource(R.string.recap_stat_chapters)),
        )

    GenreRecapCard(
        card =
            RecapCard(
                title = stringResource(R.string.recap_your_journey),
                stats = stats,
                callToAction = stringResource(R.string.recap_revisit_now),
                progress = recapProgress(saga, reviewGenerationState),
                onClick = onClick,
            ),
        modifier = modifier,
        genre = saga.genre,
    )
}

/**
 * Null once the review is finished and readable. Until then it carries how far along generation is
 * — from the live [ReviewGenerationState] when one is running, otherwise from whatever steps are
 * already persisted on the saga, so reopening the screen mid-generation doesn't reset to zero.
 */
@Composable
private fun recapProgress(
    saga: Saga,
    state: ReviewGenerationState,
): RecapProgress? {
    if (saga.review.isComplete()) return null

    val generating = state as? ReviewGenerationState.Generating
    val completed = generating?.completedCount ?: saga.review?.completedStepCount() ?: 0
    val total = generating?.totalSteps ?: REVIEW_STEP_COUNT

    val message =
        if (completed > 0) {
            stringResource(R.string.recap_almost_ready, completed, total)
        } else {
            stringResource(R.string.recap_preparing)
        }

    return RecapProgress(completed = completed, total = total, message = message)
}
