package com.ilustris.sagai.features.saga.detail.review.ui.templates.crime

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType
import com.ilustris.sagai.ui.genre.crime.CorkPin
import com.ilustris.sagai.ui.genre.crime.PinBackNote
import com.ilustris.sagai.ui.genre.crime.PinCaption
import com.ilustris.sagai.ui.genre.crime.PinProse
import com.ilustris.sagai.ui.genre.crime.PinSignature
import com.ilustris.sagai.ui.genre.crime.PinTitle
import com.ilustris.sagai.ui.genre.crime.CorkboardBackground
import com.ilustris.sagai.ui.theme.themeFilter

/**
 * One suspect photo per top character, dealt along the table with the rest of the case.
 *
 * [castNote] is the cast stage's own write-up, which the review generates once for the whole group.
 * It rides on the back of the first portrait — the same job
 * [com.ilustris.sagai.features.saga.detail.review.ui.templates.comic.ComicCastLeadPanel] does for
 * the comic page. Without it that prose was simply dropped: this template used to read
 * `review.topCharacters` only to decide *whether* to show portraits, and never showed a word of it.
 */
class CorkboardCharacterPinPage(
    override val content: SagaContent,
    private val character: Character,
    private val messageCount: Int,
    private val castNote: String? = null,
) : ReviewPage,
    CorkboardPinPage {
    override val pageType: ReviewPageType = ReviewPageType.CHARACTERS
    override val pinSize: CorkPinSize = CorkPinSize.PHOTO

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val genre = content.data.genre
        val fullName = "${character.name} ${character.lastName.orEmpty()}".trim()

        CorkPin(
            modifier = modifier,
            seed = character.id,
            back =
                castNote?.let { note ->
                    { ink -> PinBackNote(text = note, ink = ink) }
                },
        ) { ink ->
            Column {
                AsyncImage(
                    model = character.image,
                    contentDescription = character.name,
                    // Without this the default is Fit, which letterboxes a portrait inside the
                    // square and leaves bare paper down both sides — the photo stops filling its
                    // own polaroid. Every other pin's photo already crops.
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f).themeFilter(),
                )
                PinCaption(
                    text = fullName,
                    ink = ink,
                    emphasized = true,
                    modifier = Modifier.padding(top = 8.dp),
                )
                PinCaption(
                    text = stringResource(R.string.messages_count_label, messageCount),
                    ink = ink,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        CorkboardBackground(modifier)
    }
}
