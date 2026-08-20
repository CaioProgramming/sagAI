package com.ilustris.sagai.features.saga.chat.ui

import MessageStatus
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.fullName
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.EpilogueMessage
import com.ilustris.sagai.features.saga.chat.data.model.Message
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.presentation.EpilogueChatViewModel
import com.ilustris.sagai.features.saga.chat.ui.components.ChatBubble
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.sagaShape
import com.ilustris.sagai.ui.theme.solidGradient
import com.ilustris.sagai.ui.theme.themeBrushColors

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
                character = character?.data,
                genre = resolvedGenre,
                isReplying = isReplying,
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

/**
 * Deliberately mirrors [com.ilustris.sagai.features.saga.chat.ui.components.ChatInputView]'s
 * container styling (dropShadow + gradient border + rounded surface, matching the active genre
 * theme) and its send-button loading treatment, but stripped down to what an epilogue chat
 * actually needs: no expressive tags, no @mention/wiki lookup, no character switcher — just the
 * character avatar, a text field, and a send button.
 */
@Composable
private fun EpilogueChatInput(
    character: Character?,
    genre: Genre,
    isReplying: Boolean,
    onSend: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    val resolvedColor = MaterialTheme.colorScheme.primary
    val inputBrush = Brush.horizontalGradient(themeBrushColors())
    val inputShape = sagaShape()
    val glowRadiusState = animateFloatAsState(if (isReplying) 25f else 10f, label = "epilogueInputGlow")
    val textStyle =
        MaterialTheme.typography.labelMedium.copy(
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
        )

    Column(
        modifier =
            Modifier
                .padding(16.dp)
                .navigationBarsPadding()
                .imePadding()
                .dropShadow(inputShape, {
                    brush = inputBrush
                    radius = glowRadiusState.value
                    spread = 10f
                }).fillMaxWidth()
                .clip(inputShape)
                .border(1.dp, inputBrush, inputShape)
                .background(MaterialTheme.colorScheme.background, inputShape),
    ) {
        Column(
            modifier =
                Modifier
                    .padding(4.dp)
                    .clip(inputShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = .5f), inputShape)
                    .fillMaxWidth()
                    .padding(8.dp),
        ) {
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                enabled = !isReplying,
                maxLines = 4,
                textStyle = textStyle,
                cursorBrush = resolvedColor.solidGradient(),
                decorationBox = { inner ->
                    Box(Modifier.padding(8.dp), contentAlignment = Alignment.CenterStart) {
                        if (text.isEmpty()) {
                            Text(
                                stringResource(R.string.epilogue_chat_input_placeholder),
                                style = textStyle,
                                modifier = Modifier.alpha(.5f).fillMaxWidth(),
                                maxLines = 1,
                            )
                        }
                        inner()
                    }
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 40.dp, max = 160.dp),
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                character?.let {
                    CharacterAvatar(
                        it,
                        genre = genre,
                        grainRadius = 0f,
                        pixelation = 0f,
                        useFallback = false,
                        modifier = Modifier.size(32.dp).clip(CircleShape),
                        borderSize = 1.dp,
                        innerPadding = 0.dp,
                    )
                }

                Spacer(Modifier.weight(1f))

                val canSend = text.isNotBlank() && !isReplying
                Box(contentAlignment = Alignment.Center) {
                    IconButton(
                        onClick = {
                            onSend(text)
                            text = ""
                        },
                        enabled = canSend,
                        colors =
                            IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                            ),
                        modifier =
                            Modifier
                                .padding(4.dp)
                                .size(32.dp),
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_send),
                            contentDescription = stringResource(R.string.send_button_description),
                            modifier =
                                Modifier
                                    .padding(8.dp)
                                    .fillMaxSize(),
                        )
                    }

                    if (isReplying) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            trackColor = Color.Transparent,
                            strokeWidth = 1.dp,
                        )
                    }
                }
            }
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
