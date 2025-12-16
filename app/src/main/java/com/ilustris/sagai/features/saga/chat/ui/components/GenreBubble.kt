package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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

fun Genre.bubble(
    tailAlignment: BubbleTailAlignment,
    tailWidth: Dp = 12.dp,
    tailHeight: Dp = 12.dp,
    isNarrator: Boolean = false,
): Shape {
    val cornerSize = cornerSize()
    return when (this) {
        Genre.CYBERPUNK -> {
            CyberpunkChatBubbleShape(
                cornerRadius = cornerSize,
                tailWidth = tailWidth,
                tailHeight = tailHeight,
                tailAlignment = tailAlignment,
            )
        }

        Genre.HEROES -> {
            HeroesChatBubbleShape(
                tailAlignment = tailAlignment,
                tailWidth = tailWidth,
                tailHeight = tailHeight,
            )
        }

        Genre.SHINOBI -> {
            ShinobiChatBubbleShape(
                cornerRadius = cornerSize,
                tailAlignment = tailAlignment,
                tailWidth = tailWidth,
                tailHeight = tailHeight,
            )
        }

        Genre.HORROR -> {
            HorrorChatBubbleShape(
                pixelSize = cornerSize,
                tailAlignment = tailAlignment,
            )
        }

        Genre.FANTASY -> {
            FantasyChatBubbleShape(
                cornerRadius = cornerSize,
                tailAlignment = tailAlignment,
                tailWidth = tailWidth,
                tailHeight = tailHeight,
            )
        }

        Genre.SPACE_OPERA -> {
            SpaceChatBubbleShape(
                tailAlignment = tailAlignment,
            )
        }

        Genre.COWBOY -> {
            CowboysChatBubbleShape(
                cornerNotch = cornerSize,
                tailAlignment = tailAlignment,
                tailWidth = tailWidth,
                tailHeight = tailHeight,
                isNarrator = isNarrator,
            )
        }

        Genre.PUNK_ROCK -> {
            PunkRockChatBubbleShape(
                tailAlignment = tailAlignment,
                tailWidth = tailWidth,
                tailHeight = tailHeight,
            )
        }

        else -> {
            CurvedChatBubbleShape(
                cornerRadius = cornerSize,
                tailWidth = tailWidth,
                tailHeight = tailHeight,
                tailAlignment = tailAlignment,
            )
        }
    }
}
