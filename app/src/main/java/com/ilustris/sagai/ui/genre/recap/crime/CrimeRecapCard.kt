package com.ilustris.sagai.ui.genre.recap.crime

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.genre.crime.CrimeBackground
import com.ilustris.sagai.ui.genre.crime.CrimeBubbleFrame
import com.ilustris.sagai.ui.genre.recap.RecapCard
import com.ilustris.sagai.ui.theme.LocalSagaGenre

/**
 * The recap as a message still sitting unread in the thread.
 *
 * This is the one genre whose own medium already says what a recap card is trying to say — there is
 * something here waiting for you to open it — so the card leans on that instead of inventing a
 * separate "call to action" affordance: one bubble carrying the counts, a read receipt under it,
 * and the unread dot in the corner that makes the whole thing ask to be tapped.
 *
 * No [com.ilustris.sagai.ui.theme.themeVfx] here, unlike the other cards. Crime's is `vhs` plus
 * sparkle, and smearing tape noise over a messaging thread fights the one thing that makes this
 * treatment legible: that it looks like a real, clean app you already know how to read.
 */
@Composable
fun CrimeRecapCard(
    card: RecapCard,
    modifier: Modifier = Modifier,
) {
    val genre = LocalSagaGenre.current ?: Genre.CRIME
    val accent = MaterialTheme.colorScheme.primary

    Box(modifier) {
        CrimeBackground(Modifier.matchParentSize())

        Column(
            Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            CrimeBubbleFrame(
                isMe = false,
                genre = genre,
                showAvatar = false,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            ) { contentColor ->
                Text(
                    text = card.title,
                    color = contentColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (card.isReady) {
                    card.stats.forEach { stat ->
                        Text(
                            text = stat.sentence,
                            color = contentColor.copy(alpha = .85f),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                } else {
                    card.progress?.let {
                        Text(
                            text = it.message,
                            color = contentColor.copy(alpha = .75f),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }

            if (card.isReady) {
                Text(
                    text = card.callToAction,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .6f),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp, end = 4.dp),
                )
            }
        }

        // The dot a thread puts on something you haven't opened. Deliberately not a count: there is
        // one recap, and inventing a number for it would be inventing information.
        if (card.isReady) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(accent),
            )
        }
    }
}
