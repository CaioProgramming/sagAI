package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.core.ai.model.GenreVisualConfig
import com.ilustris.sagai.core.ai.model.LocalGenreVisualConfig
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.components.chat.BubbleTailAlignment
import com.ilustris.sagai.ui.theme.components.chat.CowboysChatBubbleShape
import com.ilustris.sagai.ui.theme.components.chat.CurvedChatBubbleShape
import com.ilustris.sagai.ui.theme.components.chat.CyberpunkChatBubbleShape
import com.ilustris.sagai.ui.theme.components.chat.FantasyChatBubbleShape
import com.ilustris.sagai.ui.theme.components.chat.HeroesSpeechBalloonShape
import com.ilustris.sagai.ui.theme.components.chat.HorrorChatBubbleShape
import com.ilustris.sagai.ui.theme.components.chat.PunkRockChatBubbleShape
import com.ilustris.sagai.ui.theme.components.chat.ShinobiChatBubbleShape
import com.ilustris.sagai.ui.theme.components.chat.SpaceChatBubbleShape
import com.ilustris.sagai.ui.theme.cornerSize

@Composable
fun Genre?.bubble(
    tailAlignment: BubbleTailAlignment = BubbleTailAlignment.BottomRight,
    tailWidth: Dp = 8.dp,
    tailHeight: Dp = 8.dp,
    isNarrator: Boolean = false,
    visualConfig: GenreVisualConfig? = LocalGenreVisualConfig.current,
): Shape {
    val cornerSize = cornerSize(visualConfig)
    if (this == null) return MaterialTheme.shapes.medium
    if (isNarrator) return RoundedCornerShape(cornerSize)
    val tailW = tailWidth
    val tailH = tailHeight
    return when (this) {
        Genre.CYBERPUNK -> {
            CyberpunkChatBubbleShape(
                cornerRadius = cornerSize,
                tailWidth = tailW,
                tailHeight = tailH,
                tailAlignment = tailAlignment,
            )
        }

        Genre.SHINOBI -> {
            RoundedCornerShape(cornerSize)
        }

        Genre.CRIME -> {
            CurvedChatBubbleShape(
                cornerRadius = cornerSize,
                tailWidth = tailW,
                tailHeight = tailH,
                tailAlignment = tailAlignment,
            )
        }

        Genre.HEROES -> {
            CurvedChatBubbleShape(
                cornerRadius = 20.dp,
                tailWidth = tailW,
                tailHeight = tailH,
                tailAlignment = tailAlignment,
            )
        }

        Genre.HORROR -> {
            HorrorChatBubbleShape(
                pixelSize = cornerSize,
                tailAlignment = tailAlignment,
                drawTail = !isNarrator,
            )
        }

        Genre.FANTASY -> {
            FantasyChatBubbleShape(
                cornerRadius = cornerSize,
                tailAlignment = tailAlignment,
                tailWidth = tailW,
                tailHeight = tailH,
            )
        }

        Genre.SPACE_OPERA -> {
            SpaceChatBubbleShape(
                tailAlignment = tailAlignment,
                cutSize = cornerSize / 2,
                largeCutSize = cornerSize,
            )
        }

        Genre.COWBOY -> {
            CowboysChatBubbleShape(
                cornerNotch = cornerSize,
                tailAlignment = tailAlignment,
                tailWidth = tailW,
                tailHeight = tailH,
                isNarrator = isNarrator,
            )
        }

        Genre.PUNK_ROCK -> {
            PunkRockChatBubbleShape(
                tailAlignment = tailAlignment,
                tailWidth = tailW,
                tailHeight = tailH,
            )
        }

        else -> {
            CurvedChatBubbleShape(
                cornerRadius = cornerSize,
                tailWidth = tailW,
                tailHeight = tailH,
                tailAlignment = tailAlignment,
            )
        }
    }
}

/**
 * Shape for the small speaker avatar next to a chat bubble. Defaults to [CircleShape] for every
 * genre — genres that opt in reuse the *same* shape class the bubble itself uses (Cyberpunk with
 * `drawTail = false`; Space Opera's [SpaceChatBubbleShape] is already tail-less, so it's used
 * as-is) instead of a separate duplicate shape class, so the avatar and bubble read as one panel
 * system and any future tweak to the cut-corner geometry only has one place to change.
 */
@Composable
fun Genre?.avatarShape(visualConfig: GenreVisualConfig? = LocalGenreVisualConfig.current): Shape {
    val cornerSize = cornerSize(visualConfig)
    return when (this) {
        Genre.CYBERPUNK -> CyberpunkChatBubbleShape(cornerRadius = cornerSize, drawTail = false)
        Genre.SPACE_OPERA -> SpaceChatBubbleShape(cutSize = cornerSize / 2, largeCutSize = cornerSize)
        else -> CircleShape
    }
}
