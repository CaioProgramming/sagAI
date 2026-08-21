package com.ilustris.sagai.features.saga.milestone.ui.skin

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewTemplate
import com.ilustris.sagai.features.saga.detail.review.ui.reviewTemplate

/**
 * Genre "skin" dispatcher for the Milestone screen — wraps [content] in whichever full-screen
 * chrome treatment [Genre.reviewTemplate] says this saga wears, the exact same mapping
 * [com.ilustris.sagai.features.saga.detail.ui.SagaReview] already uses to pick a review template.
 * A genre's identity is one thing worn by both screens, not two separate lookalike systems.
 *
 * [stepIndex]/[stepTotal] describe the closure step chain (both null for the un-stepped
 * introduction beat) so a skin can swap in its own step indicator instead of the plain dot one —
 * see [TerminalMilestoneSkin]. The caller is responsible for not *also* rendering the plain dot
 * indicator inside [content] when a skin has taken over that job; see
 * [com.ilustris.sagai.features.saga.milestone.ui.MilestoneClosureContent]'s `stepIndicator` slot.
 *
 * Every [ReviewTemplate] now has a matching skin — [TerminalMilestoneSkin], [BookMilestoneSkin],
 * [CollageMilestoneSkin], [ComicMilestoneSkin], [CrimeMilestoneSkin] — except
 * [ReviewTemplate.DEFAULT] (and a null [genre]), which falls straight through to [content]
 * unmodified, matching how the review feature itself leaves ungrouped genres unstyled.
 */
@Composable
fun MilestoneSkinChrome(
    genre: Genre?,
    stepIndex: Int? = null,
    stepTotal: Int? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (genre == null) {
        content()
        return
    }
    when (genre.reviewTemplate()) {
        ReviewTemplate.TERMINAL ->
            TerminalMilestoneSkin(
                genre = genre,
                stepIndex = stepIndex,
                stepTotal = stepTotal,
                modifier = modifier,
                content = content,
            )

        ReviewTemplate.BOOK ->
            BookMilestoneSkin(
                genre = genre,
                stepIndex = stepIndex,
                stepTotal = stepTotal,
                modifier = modifier,
                content = content,
            )

        ReviewTemplate.COLLAGE ->
            CollageMilestoneSkin(
                genre = genre,
                stepIndex = stepIndex,
                stepTotal = stepTotal,
                modifier = modifier,
                content = content,
            )

        ReviewTemplate.COMIC ->
            ComicMilestoneSkin(
                genre = genre,
                stepIndex = stepIndex,
                stepTotal = stepTotal,
                modifier = modifier,
                content = content,
            )

        ReviewTemplate.CRIME ->
            CrimeMilestoneSkin(
                genre = genre,
                stepIndex = stepIndex,
                stepTotal = stepTotal,
                modifier = modifier,
                content = content,
            )

        ReviewTemplate.DEFAULT -> content()
    }
}
