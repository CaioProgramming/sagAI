package com.ilustris.sagai.ui.genre.recap.crime

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.ui.genre.crime.CorkPin
import com.ilustris.sagai.ui.genre.recap.RecapCard
import com.ilustris.sagai.ui.genre.recap.rememberRecapHeadline
import com.ilustris.sagai.ui.theme.components.HandwrittenText

/** How wide the pinned card sits in its slot — short of the edges, so it reads as a loose object. */
private const val CARD_WIDTH_FRACTION = 0.86f

/**
 * The recap as the first card pinned from the case board — a taste of the table
 * [com.ilustris.sagai.features.saga.detail.review.ui.templates.crime.CorkboardStrip] opens onto.
 *
 * It used to be an unread message in a thread, which was right while Crime's review *was* a
 * simulated chat. Now that the review is a corkboard, a chat bubble here promises the wrong thing:
 * the card should look like the object it opens.
 *
 * Deliberately no cork behind it. The board's surface belongs to the review, where it is the room
 * the photos are pinned in; boxed into a list item it just reads as a brown rectangle around a
 * card, and the pinned paper says everything the treatment needs to say on its own.
 *
 * Only the title is handwritten. The counts underneath are the app's normal face — see
 * [com.ilustris.sagai.features.saga.detail.review.ui.templates.crime.PinTitle] for why the board
 * keeps handwriting for titles and signatures only.
 */
@Composable
fun CrimeRecapCard(
    card: RecapCard,
    modifier: Modifier = Modifier,
) {
    val headline = rememberRecapHeadline(card)

    Box(modifier, contentAlignment = Alignment.Center) {
        CorkPin(
            modifier = Modifier.fillMaxWidth(CARD_WIDTH_FRACTION),
            seed = card.title.hashCode(),
        ) { ink ->
            Column(Modifier.fillMaxWidth()) {
                HandwrittenText(
                    text = card.title,
                    color = ink,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 18.sp,
                    isBold = true,
                    centered = true,
                    maxLines = 1,
                    isAnimated = false,
                )

                Crossfade(targetState = headline, label = "recap-headline") { line ->
                    Text(
                        text = line,
                        color = ink.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.labelMedium,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    )
                }
            }
        }
    }
}
