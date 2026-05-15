package com.ilustris.sagai.features.saga.chat.ui.components

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
import com.ilustris.sagai.ui.theme.components.chat.HeroesChatBubbleShape
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

        Genre.HEROES -> {
            HeroesChatBubbleShape(
                tailWidth = tailW,
                tailHeight = tailH,
                tailAlignment = tailAlignment,
                skew = cornerSize,
            )
        }

        Genre.SHINOBI -> {
            ShinobiChatBubbleShape(
                cornerRadius = cornerSize,
                tailWidth = tailW,
                tailHeight = tailH,
                tailAlignment = tailAlignment,
            )
        }

        Genre.CRIME -> {
            CurvedChatBubbleShape(
                cornerRadius = cornerSize,
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
