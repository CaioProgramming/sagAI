package com.ilustris.sagai.features.saga.detail.review.ui.templates.crime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.compiledColorPalette
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
import com.ilustris.sagai.ui.theme.hexToColor

/**
 * How many lines a note shows before the rest moves to its back. Review stages can run long, and a
 * pin sizes itself to its content, so uncapped one note would grow taller than the table it sits on.
 */
private const val BODY_MAX_LINES = 9

/**
 * A text-only pin — a note left on the table — for a stage with nothing to photograph: the closing
 * send-off, or a [sender]'s farewell.
 *
 * Follows Punk Rock's notes (see
 * [com.ilustris.sagai.features.saga.detail.review.ui.templates.collage.CollageFarewellsPage]) rather
 * than the chat row it started as: no portrait, the message in the normal face, and the character's
 * name handwritten at the end in their own color. A handwritten note is identified by its
 * signature — pinning a photo next to it says the same thing twice, and puts a face where the
 * writing should be.
 *
 * Long notes turn over: the front shows what fits, the back carries the whole thing.
 */
class CorkboardNotePinPage(
    override val content: SagaContent,
    override val pageType: ReviewPageType,
    private val body: String,
    private val sender: Character? = null,
    private val title: String? = null,
) : ReviewPage, CorkboardPinPage {
    override val pinSize: CorkPinSize = CorkPinSize.NOTE

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val accent = content.data.genre.compiledColorPalette().firstOrNull() ?: MaterialTheme.colorScheme.primary
        val signatureColor = sender?.hexColor?.hexToColor() ?: accent

        CorkPin(
            modifier = modifier,
            seed = pageType.ordinal * 31 + body.length,
            back = { ink -> PinBackNote(text = body, ink = ink, title = title ?: sender?.name) },
        ) { ink ->
            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                title?.let { PinTitle(it, ink, centered = false, isAnimated = canAnimate) }

                PinProse(text = body, ink = ink, maxLines = BODY_MAX_LINES)

                sender?.let {
                    PinSignature(
                        name = it.name,
                        color = signatureColor,
                        isAnimated = canAnimate,
                        modifier = Modifier.align(Alignment.End),
                    )
                }
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        CorkboardBackground(modifier)
    }
}
