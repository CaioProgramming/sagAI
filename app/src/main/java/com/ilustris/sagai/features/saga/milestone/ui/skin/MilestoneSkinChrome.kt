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
 * Only [ReviewTemplate.TERMINAL] has a skin so far (this phase's job is proving the pattern end to
 * end for Cyberpunk/Space Opera). Every other template — [ReviewTemplate.BOOK],
 * [ReviewTemplate.COMIC], [ReviewTemplate.COLLAGE], [ReviewTemplate.CRIME], and
 * [ReviewTemplate.DEFAULT] (including a null [genre]) — falls straight through to [content]
 * unmodified. That fallthrough is deliberate: a later phase gives each of those its own skin, this
 * one is not a half-built version of them.
 */
@Composable
fun MilestoneSkinChrome(
    genre: Genre?,
    stepIndex: Int? = null,
    stepTotal: Int? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    if (genre != null && genre.reviewTemplate() == ReviewTemplate.TERMINAL) {
        TerminalMilestoneSkin(
            genre = genre,
            stepIndex = stepIndex,
            stepTotal = stepTotal,
            modifier = modifier,
            content = content,
        )
    } else {
        content()
    }
}
