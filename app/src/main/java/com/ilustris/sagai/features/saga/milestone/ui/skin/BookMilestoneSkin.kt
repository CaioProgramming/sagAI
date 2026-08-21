package com.ilustris.sagai.features.saga.milestone.ui.skin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.detail.review.ui.templates.book.BookBackground
import com.ilustris.sagai.features.saga.detail.review.ui.templates.book.CowboyBurnMarks
import com.ilustris.sagai.features.saga.detail.review.ui.templates.book.HorrorPoliceTapeOverlay
import com.ilustris.sagai.features.saga.detail.review.ui.templates.book.ShinobiInkBlooms
import com.ilustris.sagai.ui.theme.LocalSagaGenre

/**
 * Fantasy/Shinobi/Cowboy/Horror's chrome for the Milestone screen — the same storybook identity
 * [com.ilustris.sagai.features.saga.detail.ui.SagaReview]'s `ContinuousScrollReviewContainer`
 * wears for the story review (see `BookReviewExperience`), applied here around one milestone step
 * instead of a review page.
 *
 * [BookBackground] and the three genre-exclusive ambient layers below all read the saga's genre
 * from [LocalSagaGenre] rather than a parameter, which is how the review feature gets away with
 * calling them unconditionally. Milestone's own composition tree never provides that local — this
 * screen can be reached straight from a push/deep-link into [genre]'s saga without ever passing
 * through a screen that set it — so it's provided explicitly here from the [genre] this skin was
 * already given, rather than trusting whatever (if anything) is ambient above it.
 *
 * Unlike [TerminalMilestoneSkin], Book has no custom step indicator to swap in — the plain dot
 * `StepIndicator` in
 * [MilestoneClosureContent][com.ilustris.sagai.features.saga.milestone.ui.MilestoneClosureContent]
 * stays as-is for this genre group, so [stepIndex]/[stepTotal] are accepted only for signature
 * parity with the dispatcher.
 */
@Composable
fun BookMilestoneSkin(
    genre: Genre,
    @Suppress("UNUSED_PARAMETER") stepIndex: Int? = null,
    @Suppress("UNUSED_PARAMETER") stepTotal: Int? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalSagaGenre provides genre) {
        Box(modifier = modifier.fillMaxSize()) {
            BookBackground(Modifier.fillMaxSize())

            content()

            when (genre) {
                Genre.SHINOBI -> ShinobiInkBlooms(Modifier.fillMaxSize())
                Genre.COWBOY -> CowboyBurnMarks(Modifier.fillMaxSize())
                Genre.HORROR -> HorrorPoliceTapeOverlay(Modifier.fillMaxSize())
                else -> Unit
            }
        }
    }
}
