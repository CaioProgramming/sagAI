package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.ui.theme.TypewriterText
import com.ilustris.sagai.ui.theme.bodyFont

@Composable
fun NewCharacterView(
    messageContent: MessageContent,
    genre: Genre,
    characters: List<Character> = emptyList(),
    wiki: List<Wiki> = emptyList(),
    onSelectCharacter: (Character) -> Unit = {},
) {
    messageContent.character?.let {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CharacterAvatar(
                it,
                genre = genre,
                modifier =
                    Modifier.clip(CircleShape).size(75.dp).clickable {
                        onSelectCharacter(it)
                    },
            )

            Text(
                text = "${it.name} juntou-se a história.",
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontFamily = genre.bodyFont(),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    ),
                modifier = Modifier.padding(16.dp),
            )

            TypewriterText(
                text = messageContent.message.text,
                modifier = Modifier.padding(16.dp),
                genre = genre,
                mainCharacter = null,
                characters = characters,
                wiki = wiki,
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Normal,
                        fontFamily = genre.bodyFont(),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
            )
        }
    }
}
