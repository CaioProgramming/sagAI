package com.ilustris.sagai.ui.genre.recap.comic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.ui.genre.comic.COMIC_INK
import com.ilustris.sagai.ui.genre.comic.COMIC_PAPER
import com.ilustris.sagai.ui.genre.comic.ComicCaptionBox
import com.ilustris.sagai.ui.genre.comic.ComicPanel
import com.ilustris.sagai.ui.genre.comic.ComicShoutBlock
import com.ilustris.sagai.ui.genre.comic.ComicTag
import com.ilustris.sagai.ui.genre.comic.SlantShape
import com.ilustris.sagai.ui.genre.recap.RecapCard
import com.ilustris.sagai.ui.theme.themeVfx

/**
 * The recap as the cover of the issue you just finished: one slanted framed panel, the title
 * stamped in the corner the way a cover carries its logo box, the counts lettered big across the
 * middle, and the call to action shouted at the bottom.
 *
 * Every stat is on the page at once. A cover states what the issue is; a number that swapped itself
 * every two seconds would be a scoreboard, which is not what a comic puts on its front.
 *
 * Wears [themeVfx] instead of an ambient overlay — Heroes' own gentle float plus lightning. The
 * full-screen overlays the beat surfaces use would arrive here at their own scale and simply cross
 * a 150dp card end to end.
 */
@Composable
fun ComicRecapCard(
    card: RecapCard,
    modifier: Modifier = Modifier,
) {
    ComicPanel(
        modifier = modifier.themeVfx(true),
        borderColor = COMIC_INK,
        background = COMIC_PAPER,
        shape = SlantShape(topRightLean = 0.035f, bottomLeftLean = 0.025f),
    ) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            ComicTag(text = card.title)

            if (card.isReady) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    card.stats.forEach { stat ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stat.value,
                                color = COMIC_INK,
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.headlineSmall,
                                maxLines = 1,
                            )
                            Text(
                                text = stat.label,
                                color = COMIC_INK.copy(alpha = .75f),
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                ComicShoutBlock(text = card.callToAction, modifier = Modifier.fillMaxWidth())
            } else {
                card.progress?.let {
                    ComicCaptionBox(
                        text = it.message,
                        align = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
