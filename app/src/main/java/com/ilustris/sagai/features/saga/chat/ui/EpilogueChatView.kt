package com.ilustris.sagai.features.saga.chat.ui

import MessageStatus
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.fullName
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.EpilogueMessage
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.presentation.EpilogueChatViewModel
import com.ilustris.sagai.features.saga.chat.ui.components.ChatBubble
import com.ilustris.sagai.ui.theme.SagAITheme

/**
 * A closed, ephemeral "talk to the character again" epilogue chat. Never reads from or writes to
 * Room — [EpilogueChatViewModel] holds the whole conversation in memory, so it's gone the moment
 * this screen (and its ViewModel) is cleared or the app restarts.
 */
@Composable
fun EpilogueChatView(
    sagaId: String,
    characterId: Int,
    onBack: () -> Unit = {},
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: EpilogueChatViewModel = hiltViewModel(),
) {
    val character by viewModel.character.collectAsStateWithLifecycle()
    val genre by viewModel.genre.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isReplying by viewModel.isReplying.collectAsStateWithLifecycle()
    val hasError by viewModel.error.collectAsStateWithLifecycle()

    LaunchedEffect(sagaId, characterId) {
        viewModel.load(sagaId.toIntOrNull() ?: 0, characterId)
    }

    SagAITheme(genre = genre) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
        ) {
            EpilogueChatTopBar(
                characterName = character?.data?.fullName() ?: stringResource(R.string.app_name),
                onBack = onBack,
            )

            EpilogueDisclaimerBanner()

            val listState = rememberLazyListState()
            LaunchedEffect(messages.size) {
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.lastIndex)
                }
            }

            val resolvedGenre = genre ?: Genre.FANTASY

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(messages, key = { it.id }) { epilogueMessage ->
                    val messageContent =
                        remember(epilogueMessage, character) {
                            epilogueMessage.toMessageContent(sagaId.toIntOrNull() ?: 0, character?.data)
                        }

                    ChatBubble(
                        messageContent = messageContent,
                        mainCharacter = null,
                        characters = listOfNotNull(character?.data),
                        wikis = emptyList(),
                        genre = resolvedGenre,
                        flatEvents = emptyList(),
                        canAnimate = true,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
                }

                if (isReplying) {
                    item {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        }
                    }
                }

                if (hasError) {
                    item {
                        Text(
                            stringResource(R.string.message_reply_error),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).fillMaxWidth(),
                        )
                    }
                }
            }

            EpilogueChatInput(
                enabled = !isReplying,
                onSend = { viewModel.sendMessage(it) },
            )
        }
    }
}

@Composable
private fun EpilogueChatTopBar(
    characterName: String,
    onBack: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(
                painterResource(R.drawable.ic_back_left),
                contentDescription = stringResource(R.string.back_button_description),
            )
        }

        Text(
            characterName,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

@Composable
private fun EpilogueDisclaimerBanner() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Text(
            stringResource(R.string.epilogue_chat_disclaimer),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EpilogueChatInput(
    enabled: Boolean,
    onSend: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(8.dp),
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            enabled = enabled,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            placeholder = { Text(stringResource(R.string.epilogue_chat_input_placeholder)) },
            colors = OutlinedTextFieldDefaults.colors(),
        )

        IconButton(
            enabled = enabled && text.isNotBlank(),
            onClick = {
                onSend(text)
                text = ""
            },
            colors = IconButtonDefaults.iconButtonColors(),
        ) {
            Icon(
                painterResource(R.drawable.ic_send),
                contentDescription = stringResource(R.string.send_button_description),
            )
        }
    }
}

private fun EpilogueMessage.toMessageContent(
    sagaId: Int,
    character: Character?,
) = MessageContent(
    message =
        Message(
            id = 0,
            text = text,
            timestamp = timestamp,
            senderType = if (isUser) SenderType.USER else SenderType.CHARACTER,
            speakerName = if (isUser) null else character?.fullName(),
            sagaId = sagaId,
            characterId = if (isUser) null else character?.id,
            timelineId = 0,
            status = MessageStatus.OK,
            viewed = true,
        ),
    character = if (isUser) null else character,
    reactions = emptyList(),
)
