package com.ilustris.sagai.features.saga.detail.review.ui.templates.crime

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewImageSource
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.genre.crime.CrimeBackground
import com.ilustris.sagai.ui.genre.crime.CrimeBubbleFrame

/**
 * A single-photo "attachment" bubble — sent right after its related content message, same
 * side/turn as that message (see [CrimeReviewExperience] pairing them), like sending a text then
 * immediately following up with a photo. Uses [CrimeBubbleFrame]'s plain-rect mode (`useSpeechShape
 * = false`) since a photo attachment in a real messaging app is its own rectangle, not squeezed
 * into the text bubble shape. For *multiple* related photos, see [CrimeAlbumMessagePage] instead —
 * this one is for a single image.
 */
class CrimeAttachmentMessagePage(
    override val content: SagaContent,
    override val pageType: ReviewPageType,
    private val image: ReviewImageSource,
    private val isMe: Boolean,
    private val sender: Character? = null,
) : ReviewPage {
    /** No typing to wait for, just its own pop-in. */
    override val estimatedRevealDurationMs: Long = 500L

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val genre = content.data.genre

        CrimeBubbleFrame(
            isMe = isMe,
            genre = genre,
            sender = sender,
            useSpeechShape = false,
            canAnimate = canAnimate,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
            modifier = modifier,
        ) { contentColor ->
            AsyncImage(
                model = image.url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .width(200.dp)
                        .aspectRatio(0.85f)
                        .clip(RoundedCornerShape(18.dp)),
            )
            Text(
                text = image.caption,
                fontStyle = FontStyle.Italic,
                color = contentColor.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        CrimeBackground(modifier)
    }
}
