package com.ilustris.sagai.ui.genre.crime

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.ui.theme.components.HandwrittenText
import com.ilustris.sagai.ui.theme.components.mascot.BlobMascot
import com.ilustris.sagai.ui.theme.components.mascot.rememberMascotExpression

/**
 * The corkboard's typography rule, in one place so no pin has to re-decide it.
 *
 * Handwriting is for **titles and signatures only**. The first pass set every line on the board in
 * the display face, and a title stops reading as a title when the caption under it is written in
 * the same hand — the impact comes from the contrast, not from the font. Long prose in a script
 * face is also simply slower to read, which matters most on exactly the text this template was
 * losing before: the journey and cast write-ups.
 *
 * So: [PinTitle] and [PinSignature] are handwritten; [PinProse] and [PinCaption] are the app's
 * normal face.
 */
@Composable
fun PinTitle(
    text: String,
    ink: Color,
    modifier: Modifier = Modifier,
    centered: Boolean = true,
    isAnimated: Boolean = true,
) {
    HandwrittenText(
        text = text,
        color = ink,
        modifier = modifier.fillMaxWidth(),
        fontSize = 20.sp,
        isBold = true,
        centered = centered,
        maxLines = 2,
        isAnimated = isAnimated,
    )
}

/**
 * A name closing a note — the one thing on a handwritten note that really is handwriting. Takes the
 * character's own color, the way [com.ilustris.sagai.features.saga.detail.review.ui.templates.collage.CollageFarewellsPage]'s
 * notes sign off, since without a portrait the signature is what identifies the writer.
 */
@Composable
fun PinSignature(
    name: String,
    color: Color,
    modifier: Modifier = Modifier,
    isAnimated: Boolean = true,
) {
    HandwrittenText(
        text = name,
        color = color,
        modifier = modifier,
        fontSize = 18.sp,
        centered = false,
        maxLines = 1,
        isAnimated = isAnimated,
    )
}

/** Body text on paper — the normal face, so it stays readable at length. */
@Composable
fun PinProse(
    text: String,
    ink: Color,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    centered: Boolean = false,
) {
    Text(
        text = text,
        color = ink,
        style = MaterialTheme.typography.bodySmall,
        fontStyle = FontStyle.Italic,
        maxLines = maxLines,
        textAlign = if (centered) TextAlign.Center else TextAlign.Start,
        modifier = modifier.fillMaxWidth(),
    )
}

/** The small line under a photo: a name, a chapter title, a count. */
@Composable
fun PinCaption(
    text: String,
    ink: Color,
    modifier: Modifier = Modifier,
    maxLines: Int = 2,
    emphasized: Boolean = false,
) {
    Text(
        text = text,
        color = if (emphasized) ink else ink.copy(alpha = 0.7f),
        style = if (emphasized) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelSmall,
        maxLines = maxLines,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth(),
    )
}

/**
 * The back of a photo: what someone wrote on it. Scrolls rather than truncating — the whole point
 * of turning a card over is to get the text the front had to cut short, so this is the one surface
 * on the board that must never ellipsize.
 */
@Composable
fun PinBackNote(
    text: String,
    ink: Color,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        title?.let {
            PinTitle(it, ink, isAnimated = false)
        }
        PinProse(
            text = text,
            ink = ink,
            modifier = Modifier.padding(top = if (title != null) 8.dp else 0.dp),
        )
    }
}

/**
 * The emotional read as it appears on the board: the blob mascot wearing the tone, its name under
 * it, and the write-up below that. Draws nothing when the tone has no entry in Remote Config.
 *
 * Lives in the kit rather than in either caller because both the finished saga's review and the
 * milestone show this same card, and "the same as the review" is the whole requirement — two
 * hand-matched copies would drift the first time one of them was tuned.
 */
@Composable
fun PinVibeNote(
    tone: EmotionalTone,
    ink: Color,
    modifier: Modifier = Modifier,
    caption: String? = null,
    canAnimate: Boolean = true,
    captionMaxLines: Int = Int.MAX_VALUE,
) {
    Column(
        modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BlobMascot(
            expression = rememberMascotExpression(tone),
            color = tone.color,
            eyeColor = rememberCorkboardPalette().paper,
            animate = canAnimate,
            modifier = Modifier.size(VIBE_BLOB_SIZE),
        )
        PinTitle(
            text = tone.getTitle(),
            ink = ink,
            isAnimated = canAnimate,
            modifier = Modifier.padding(top = 6.dp),
        )
        caption?.takeIf { it.isNotBlank() }?.let {
            PinProse(
                text = it,
                ink = ink,
                centered = true,
                maxLines = captionMaxLines,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

private val VIBE_BLOB_SIZE = 100.dp
