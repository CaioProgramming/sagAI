package com.ilustris.sagai.features.saga.milestone.ui.skin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.genre.crime.CrimeBackground
import com.ilustris.sagai.ui.genre.crime.CrimeBubbleFrame

/**
 * Crime's chrome for the Milestone screen — the same chat-thread identity Crime's review templates
 * wear (see the `crime` package under `saga.detail.review.ui.templates`), applied here so a
 * milestone reads as one more message arriving in that thread instead of a lookalike screen with
 * its own invented chrome.
 *
 * [content] sits inside a single [CrimeBubbleFrame] rather than the review's own scrolling-thread
 * container: Milestone shows exactly one beat at a time, not a whole thread, so only the bubble's
 * own chrome — shape, pop-in entrance, left placement — is reused, none of the review's
 * multi-message reveal-timer machinery. `isMe = false` with no `sender` puts this on the left with
 * no avatar reserved, the same "nobody in the conversation sent this" treatment
 * `CrimePlaystyleStatPage`/`CrimeVibeStatPage` already use for their own non-character stat
 * bubbles — [CrimeBubbleFrame] supports a null `sender` natively.
 *
 * [stepIndex]/[stepTotal] are unused: Crime doesn't get a custom step indicator, the plain dot one
 * [com.ilustris.sagai.features.saga.milestone.ui.MilestoneClosureContent] already draws stays
 * as-is. Accepted anyway so every skin shares [MilestoneSkinChrome]'s call shape.
 */
@Composable
fun CrimeMilestoneSkin(
    genre: Genre,
    stepIndex: Int? = null,
    stepTotal: Int? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        CrimeBackground(Modifier.fillMaxSize())

        CrimeBubbleFrame(
            isMe = false,
            genre = genre,
        ) {
            content()
        }
    }
}
