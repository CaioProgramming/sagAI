package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.components.chat.BubbleTailAlignment
import com.ilustris.sagai.ui.theme.components.chat.CowboysChatBubbleShape
import com.ilustris.sagai.ui.theme.components.chat.CurvedChatBubbleShape
import com.ilustris.sagai.ui.theme.components.chat.CyberpunkChatBubbleShape
import com.ilustris.sagai.ui.theme.components.chat.FantasyChatBubbleShape
import com.ilustris.sagai.ui.theme.components.chat.HeroesChatBubbleShape
import com.ilustris.sagai.ui.theme.components.chat.HorrorChatBubbleShape
import com.ilustris.sagai.ui.theme.components.chat.ShinobiChatBubbleShape
import com.ilustris.sagai.ui.theme.components.chat.SpaceChatBubbleShape
import com.ilustris.sagai.ui.theme.cornerSize

fun Genre.bubble(tailAlignment: BubbleTailAlignment): Shape {
    val cornerSize = cornerSize()
    return when (this) {
        Genre.CYBERPUNK ->
            CyberpunkChatBubbleShape(
                cornerRadius = cornerSize,
                tailWidth = 12.dp,
                tailHeight = 12.dp,
                tailAlignment = tailAlignment,
            )

        Genre.HEROES ->
            HeroesChatBubbleShape(
                tailAlignment = tailAlignment,
            )

        Genre.SHINOBI ->
            ShinobiChatBubbleShape(
                cornerRadius = cornerSize,
                tailAlignment = tailAlignment,
            )

        Genre.HORROR ->
            HorrorChatBubbleShape(
                pixelSize = cornerSize,
                tailAlignment = tailAlignment,
            )

        Genre.FANTASY ->
            FantasyChatBubbleShape(
                cornerRadius = cornerSize,
                tailAlignment = tailAlignment,
            )

        Genre.SPACE_OPERA ->
            SpaceChatBubbleShape(
                tailAlignment = tailAlignment,
            )

        Genre.COWBOYS ->
            CowboysChatBubbleShape(
                cornerNotch = cornerSize,
                tailAlignment = tailAlignment,
            )

        else ->
            CurvedChatBubbleShape(
                cornerRadius = cornerSize,
                tailWidth = 4.dp,
                tailHeight = 4.dp,
                tailAlignment = tailAlignment,
            )
    }
}
