package com.ilustris.sagai.features.saga.milestone.ui.skin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.subtitle
import com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.ComicFadeIn
import com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.ComicTag
import com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.SlantShape

/**
 * Heroes' chrome for the Milestone screen — the comic panel language
 * [com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.ComicBoardReviewContainer]
 * wears for the story review, borrowed at a single-panel scale rather than ported wholesale.
 *
 * [ComicBoard][com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.ComicBoard] is
 * built for paging through a sequence of panels with its own camera and gesture navigation — a
 * milestone is one beat, not a sequence, so none of that machinery belongs here. What carries over
 * is the visual identity: a [SlantShape]-clipped frame with an ink border (same border+background
 * clip pattern as that package's own `ComicPanel`), and a corner [ComicTag] instead of a full
 * speech balloon — wrapping all of [content] in a balloon would fight the title/rows/buttons it
 * renders internally, which this skin has no visibility into and no business reshaping.
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
    // The panel's one-time entrance, matching MilestoneClosureContent's own
    // fadeIn+scaleIn(0.92f) reveal rather than inventing a new animation idiom.
    var panelVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { panelVisible = true }

    val panelShape = SlantShape(topLeftLean = 0.02f, bottomRightLean = 0.02f)

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = panelVisible,
            enter = fadeIn(tween(450)) + scaleIn(initialScale = 0.92f, animationSpec = tween(450)),
            modifier = Modifier.fillMaxSize().padding(12.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(panelShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer, panelShape)
                        .border(3.dp, MaterialTheme.colorScheme.onBackground, panelShape),
            ) {
                content()
            }
        }

        // A cover-line tag, not a balloon wrapping the whole panel — see the class doc for why.
        ComicFadeIn(
            delayMillis = 300,
            modifier = Modifier.align(Alignment.TopEnd).padding(20.dp),
        ) {
            ComicTag(text = genre.subtitle())
        }
    }
}
