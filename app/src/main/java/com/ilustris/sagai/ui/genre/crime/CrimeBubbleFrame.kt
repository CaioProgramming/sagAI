package com.ilustris.sagai.ui.genre.crime

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.ui.theme.components.chat.BubbleTailAlignment

/** Space next to a non-`isMe` bubble: either the real avatar, or a same-size blank so stacked blocks of one message still line up. */
internal val CRIME_AVATAR_SLOT = 32.dp

/** Default [CrimeBubbleFrame.reservedMargin] — exposed so callers that need to pre-measure text
 * (e.g. [CrimeTextMessagePage]'s block splitter) can approximate the same available width. */
internal val CRIME_BUBBLE_RESERVED_MARGIN = 56.dp

/** Default horizontal [CrimeBubbleFrame.contentPadding], one side. */
internal val CRIME_BUBBLE_HORIZONTAL_PADDING = 14.dp

/** Matches the outer `Row`'s own horizontal padding in [CrimeBubbleFrame]. */
internal val CRIME_BUBBLE_ROW_PADDING = 16.dp

/**
 * Shared chrome for every message in Crime's thread: left/right placement, the pop-in (scale
 * 0.6→1 with [EaseOutBack] overshoot + fade, growing from the bubble's own tail corner) every
 * bubble type uses, and the bubble shape/color/avatar rules. [useSpeechShape] switches between
 * the real speech-bubble shape (text) and a plain rounded rect (photo/card attachments — a real
 * messaging app doesn't squeeze a photo into the text tail shape). Pulled out once three+ bubble
 * types needed the identical Row/AnimatedVisibility/shape boilerplate.
 *
 * [contentColor] is handed to [content] as an explicit parameter rather than an ambient
 * `CompositionLocalProvider` override — same convention
 * [com.ilustris.sagai.features.saga.chat.ui.components.ChatBubble] already uses for its own
 * `bubbleStyle.textColor`, derived from the same left/right boolean this frame already has.
 *
 * No fixed max-width: [reservedMargin] is reserved on the side opposite the bubble instead, so the
 * available width — and therefore how wide a bubble can grow before wrapping — scales with the
 * actual screen, the same mechanism [com.ilustris.sagai.features.saga.chat.ui.components.ChatBubble]
 * uses (`Modifier.weight(1f).padding(end = 50.dp)`) rather than a dp constant that would either
 * waste space or look identical on a phone and a tablet.
 *
 * [showTail]/[showAvatar] exist for [com.ilustris.sagai.features.saga.detail.review.ui.templates.crime.CrimeTextMessagePage]'s
 * message-block splitting: a long message renders as several consecutive [CrimeBubbleFrame] calls,
 * only the last of which should draw the tail or the avatar (the avatar's reserved space still
 * needs to hold on every block, or the blocks without one would sit at a different left edge).
 *
 * [canAnimate] gates the pop-in itself — without this, a bubble that gets discarded from the
 * `LazyColumn`'s composition (scrolled far away) and later recomposed (scrolled back into view)
 * would always replay its entrance, since the `visible` state below is otherwise unconditional.
 */
@Composable
internal fun CrimeBubbleFrame(
    isMe: Boolean,
    genre: Genre,
    modifier: Modifier = Modifier,
    sender: Character? = null,
    useSpeechShape: Boolean = true,
    showTail: Boolean = true,
    showAvatar: Boolean = true,
    canAnimate: Boolean = true,
    bubbleColor: Color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
    reservedMargin: Dp = CRIME_BUBBLE_RESERVED_MARGIN,
    contentPadding: PaddingValues = PaddingValues(horizontal = CRIME_BUBBLE_HORIZONTAL_PADDING, vertical = 10.dp),
    content: @Composable ColumnScope.(contentColor: Color) -> Unit,
) {
    val shape =
        if (useSpeechShape) {
            genre.bubble(
                tailAlignment = if (isMe) BubbleTailAlignment.BottomRight else BubbleTailAlignment.BottomLeft,
                showTail = showTail,
            )
        } else {
            RoundedCornerShape(18.dp)
        }

    var visible by remember(canAnimate) { mutableStateOf(!canAnimate) }
    LaunchedEffect(canAnimate) { visible = true }

    Row(
        modifier
            .fillMaxWidth()
            .padding(
                start = if (isMe) 16.dp + reservedMargin else 16.dp,
                end = if (isMe) 16.dp else 16.dp + reservedMargin,
                top = 4.dp,
                bottom = 4.dp,
            ),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        AnimatedVisibility(
            visible = visible,
            enter =
                if (canAnimate) {
                    fadeIn(tween(200)) +
                        scaleIn(
                            animationSpec = tween(320, easing = EaseOutBack),
                            initialScale = 0.6f,
                            transformOrigin = TransformOrigin(if (isMe) 1f else 0f, 1f),
                        )
                } else {
                    fadeIn(tween(0))
                },
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                if (!isMe && sender != null) {
                    if (showAvatar) {
                        CharacterAvatar(
                            sender,
                            genre = genre,
                            modifier = Modifier.size(CRIME_AVATAR_SLOT),
                        )
                    } else {
                        Spacer(Modifier.size(CRIME_AVATAR_SLOT))
                    }
                }

                Column(
                    Modifier
                        // The typewriter reveal grows the text every frame; without this the
                        // bubble snaps to each new size instead of visibly catching up.
                        .animateContentSize(
                            animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
                        ).clip(shape)
                        .background(bubbleColor, shape)
                        .padding(contentPadding),
                ) {
                    content(contentColor)
                }
            }
        }
    }
}
