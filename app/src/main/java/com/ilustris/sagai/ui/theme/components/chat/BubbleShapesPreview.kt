package com.ilustris.sagai.ui.theme.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.cornerSize

/**
 * Preview composable to visualize all genre-specific bubble shapes.
 * Shows both NPC (left-aligned) and User (right-aligned) bubbles for each genre.
 */
@Composable
fun BubbleShapePreviewItem(
    genre: Genre,
    tailAlignment: BubbleTailAlignment,
    modifier: Modifier = Modifier,
) {
    val cornerSize = 8.dp
    val shape = when (genre) {
        Genre.CYBERPUNK -> CyberpunkChatBubbleShape(
            cornerRadius = cornerSize,
            tailWidth = 12.dp,
            tailHeight = 12.dp,
            tailAlignment = tailAlignment,
        )

        Genre.HEROES -> HeroesChatBubbleShape(
            tailAlignment = tailAlignment,
        )

        Genre.SHINOBI -> ShinobiChatBubbleShape(
            cornerRadius = cornerSize,
            tailAlignment = tailAlignment,
        )

        Genre.HORROR -> HorrorChatBubbleShape(
            pixelSize = cornerSize,
            tailAlignment = tailAlignment,
        )

        Genre.FANTASY -> FantasyChatBubbleShape(
            cornerRadius = cornerSize,
            tailAlignment = tailAlignment,
        )

        Genre.SPACE_OPERA -> SpaceChatBubbleShape(
            tailAlignment = tailAlignment,
        )

        Genre.COWBOYS -> CowboysChatBubbleShape(
            cornerNotch = cornerSize,
            tailAlignment = tailAlignment,
        )

        else -> CurvedChatBubbleShape(
            cornerRadius = cornerSize,
            tailWidth = 4.dp,
            tailHeight = 4.dp,
            tailAlignment = tailAlignment,
        )
    }

    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = genre.name,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 4.dp),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // Left bubble (NPC)
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .clip(shape)
                    .background(genre.color.copy(alpha = 0.3f))
                    .padding(12.dp),
            ) {
                Text(
                    text = "NPC",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                )
            }

            // Right bubble (User)
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .clip(shape)
                    .background(genre.color)
                    .padding(12.dp),
            ) {
                Text(
                    text = "User",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                )
            }
        }
    }
}

/**
 * Preview showing all genre bubble shapes in a grid layout.
 * Useful for comparing bubble designs across all themes.
 */
@Preview(showBackground = true, backgroundColor = 0xFF1C1C1E)
@Composable
fun AllBubbleShapesPreview() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(Genre.entries.toList()) { genre ->
            BubbleShapePreviewItem(
                genre = genre,
                tailAlignment = BubbleTailAlignment.BottomLeft,
            )
        }
    }
}
