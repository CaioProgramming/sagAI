package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.ui.theme.components.chat.BubbleTailAlignment
import com.ilustris.sagai.ui.theme.components.chat.ThoughtBubbleShape
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.dashedBorder
import com.ilustris.sagai.ui.theme.gradientFade

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReactionsBottomSheet(
    message: MessageContent,
    content: SagaContent,
    onDismiss: () -> Unit,
) {
    val genre = content.data.genre
    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxWidth(),
            columns = GridCells.Fixed(2),
        ) {
            item(span = { GridItemSpan(2) }) {
                Text(
                    "Reações",
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = genre.bodyFont(),
                        ),
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                )
            }

            items(message.reactions) { reaction ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(8.dp),
                ) {
                    val thoughtBubbleShape =
                        ThoughtBubbleShape(
                            cornerRadius = genre.cornerSize(),
                            tailAlignment = BubbleTailAlignment.BottomLeft,
                            tailHeight = 0.dp,
                            tailWidth = 0.dp,
                        )

                    Text(
                        text = reaction.data.thought ?: "",
                        style = MaterialTheme.typography.labelLarge.copy(fontFamily = genre.bodyFont()),
                        modifier =
                            Modifier
                                .padding()
                                .dashedBorder(
                                    strokeWidth = 1.dp,
                                    MaterialTheme.colorScheme.onBackground.copy(),
                                    shape = thoughtBubbleShape,
                                    dashLength = 10.dp,
                                    gapLength = 5.dp,
                                )
                                .background(
                                    MaterialTheme.colorScheme.background.gradientFade(),
                                    thoughtBubbleShape,
                                )
                                .padding(8.dp),
                    )

                    Text(
                        reaction.data.emoji,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = genre.bodyFont()),
                        modifier =
                            Modifier
                                .padding(bottom = 4.dp)
                                .dashedBorder(
                                    strokeWidth = 1.dp,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    shape = CircleShape,
                                    dashLength = 4.dp,
                                    gapLength = 2.dp,
                                )
                                .background(
                                    MaterialTheme.colorScheme.background.gradientFade(),
                                    CircleShape,
                                )
                                .padding(4.dp),
                    )

                    CharacterAvatar(
                        reaction.character,
                        genre = content.data.genre,
                        modifier = Modifier.size(50.dp),
                    )
                }
            }

            item(span = { GridItemSpan(2) }) {
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
