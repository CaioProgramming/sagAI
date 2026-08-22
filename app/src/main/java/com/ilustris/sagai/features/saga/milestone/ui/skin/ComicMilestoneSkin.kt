package com.ilustris.sagai.features.saga.milestone.ui.skin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.subtitle
import com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.ComicCaptionBox
import com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.ComicFadeIn

/**
 * Heroes' chrome for the Milestone screen — the comic language
 * [com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.ComicBoardReviewContainer]
 * wears for the story review, borrowed at a single-beat scale rather than ported wholesale.
 *
 * A bordered [com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.SlantShape] panel
 * made sense when there was a cover image to fill it, but most milestones (a bare `NewEvent`, for
 * one) have none — a `surfaceContainer` box around empty space just looked like an empty box. So
 * [content] renders straight over the screen's own background instead
 * ([com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.ComicBoardReviewContainer]
 * uses the same plain background for its own board, there's no comic-specific texture to borrow
 * here), with two [ComicCaptionBox] narration boxes — the same flat, hard-edged "narrator's voice"
 * box
 * [com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.ComicNarrationPanel] scatters
 * across a bare panel — pinned to opposite corners as pure decoration, replacing the single corner
 * tag this used to carry (a location/issue-cover stamp that had nothing to stamp here).
 *
 * [stepIndex]/[stepTotal] are accepted only for signature parity with [MilestoneSkinChrome]'s other
 * skins; Heroes keeps the plain dot `StepIndicator`
 * [MilestoneClosureContent][com.ilustris.sagai.features.saga.milestone.ui.MilestoneClosureContent]
 * already renders inline rather than growing a comic-styled one for this phase.
 */
@Composable
fun ComicMilestoneSkin(
    genre: Genre,
    stepIndex: Int? = null,
    stepTotal: Int? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    // content's own one-time entrance, matching MilestoneClosureContent's own
    // fadeIn+scaleIn(0.92f) reveal rather than inventing a new animation idiom.
    var panelVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { panelVisible = true }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AnimatedVisibility(
            visible = panelVisible,
            enter = fadeIn(tween(450)) + scaleIn(initialScale = 0.92f, animationSpec = tween(450)),
            modifier = Modifier.fillMaxSize(),
        ) {
            content()
        }

        ComicFadeIn(
            delayMillis = 300,
            modifier = Modifier.align(Alignment.TopStart).padding(20.dp),
        ) {
            ComicCaptionBox(text = genre.subtitle())
        }

        ComicFadeIn(
            delayMillis = 600,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
        ) {
            ComicCaptionBox(text = stringResource(genre.title))
        }
    }
}
