package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.Message
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.SenderType
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.TypewriterText
import com.ilustris.sagai.ui.theme.bodyFont

@Composable
fun NewCharacterView(
    messageContent: MessageContent,
    genre: Genre,
) {
    messageContent.character?.let {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CharacterAvatar(
                it,
                modifier = Modifier.size(75.dp),
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
                characters = emptyList(),
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Normal,
                        fontFamily = genre.bodyFont(),
                        textAlign = TextAlign.Center,
                    ),
            )
        }
    }
}

@Preview
@Composable
fun NewCharacterPreview() {
    SagAIScaffold {
        Column(Modifier.fillMaxWidth()) {
            NewCharacterView(
                messageContent =
                    MessageContent(
                        character =
                            Character(
                                name = "Alex",
                                details = Details(),
                            ),
                        message =
                            Message(
                                text = "Alex joined the story.",
                                id = 0,
                                senderType = SenderType.NEW_CHARACTER,
                                timestamp = 0L,
                                sagaId = 0,
                            ),
                    ),
                genre = Genre.FANTASY,
            )
        }
    }
}
