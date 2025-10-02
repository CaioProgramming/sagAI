
package com.ilustris.sagai.features.saga.chat.ui.components

import ReactionContent
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterProfile
import com.ilustris.sagai.features.characters.data.model.Details
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.ilustris.sagai.features.saga.chat.data.model.Reaction

@Composable
fun ReactionsView(
    reactions: List<ReactionContent>,
    onReactionsClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy((-4).dp),
        modifier =
            Modifier
                .clip(CircleShape)
                .clickable { onReactionsClick() }
                .padding(2.dp),
    ) {
        reactions.take(3).forEachIndexed { index, reaction ->
            Box(
                modifier =
                    Modifier
                        .zIndex(reactions.size - index.toFloat())
                        .graphicsLayer(translationX = (index * -10).toFloat())
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background, CircleShape)
                        .padding(4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = reaction.data.emoji, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Preview
@Composable
fun ReactionsViewPreview() {
    val reactions = listOf(
        ReactionContent(
            data = Reaction(messageId = 0, characterId = 0, emoji = "😂"),
            character = Character(details = Details(), profile = CharacterProfile())
        ),
        ReactionContent(
            data = Reaction(messageId = 0, characterId = 1, emoji = "❤️"),
            character = Character(details = Details(), profile = CharacterProfile())
        )
    )
    ReactionsView(reactions = reactions, onReactionsClick = {})
}



