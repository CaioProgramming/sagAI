@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.saga.chat.ui

import MessageStatus
import android.graphics.Matrix
import android.graphics.Shader
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.ilustris.sagai.features.saga.chat.presentation.MessageAction
import com.ilustris.sagai.features.saga.chat.ui.components.ChatBubble
import com.ilustris.sagai.ui.animations.rememberLifecycleAnimationsActive
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.components.SagaTopBar
import com.ilustris.sagai.ui.theme.levitate
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.sagaShape
import com.ilustris.sagai.ui.theme.solidGradient
import com.ilustris.sagai.ui.theme.themeBrushColors
import com.ilustris.sagai.ui.theme.themePainter
import kotlinx.coroutines.launch

/**
 * A closed, ephemeral "talk to the character again" epilogue chat. Never reads from or writes to
 * Room — [EpilogueChatViewModel] holds the whole conversation in memory, so it's gone the moment
 * this screen (and its ViewModel) is cleared or the app restarts. Deliberately mirrors
 * [ChatView]'s visual language (background genre icon, [SagaTopBar], streamed reasoning while
 * waiting) so this feels like a natural extension of the main chat rather than a bolted-on screen.
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
    val protagonist by viewModel.protagonist.collectAsStateWithLifecycle()
    val genre by viewModel.genre.collectAsStateWithLifecycle()
    val relationshipSubtitle by viewModel.relationshipSubtitle.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val isReplying by viewModel.isReplying.collectAsStateWithLifecycle()
    val reasoningChunk by viewModel.reasoningChunk.collectAsStateWithLifecycle()
    val hasError by viewModel.error.collectAsStateWithLifecycle()

    LaunchedEffect(sagaId, characterId) {
        viewModel.load(sagaId.toIntOrNull() ?: 0, characterId)
    }

    SagAITheme(genre = genre) {
        val resolvedGenre = genre ?: Genre.FANTASY

        Box(
            modifier =
                Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxSize()
                    .statusBarsPadding()
                    .imePadding(),
        ) {
            Icon(
                themePainter(),
                null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                modifier =
                    Modifier
                        .size(64.dp)
                        .align(Alignment.Center),
            )

            Column(Modifier.fillMaxSize()) {
                SagaTopBar(
                    title = character?.data?.fullName() ?: stringResource(R.string.app_name),
                    subtitle = relationshipSubtitle.orEmpty(),
                    genre = genre,
                    isLoading = isReplying,
                    onBackClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    actionContent = { EpilogueInfoAction() },
                )

                val listState = rememberLazyListState()
                LaunchedEffect(messages.size) {
                    if (messages.isNotEmpty()) {
                        listState.animateScrollToItem(messages.lastIndex)
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .weight(1f),
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
                            // This is a closed 1:1 conversation — the only character whose avatar
                            // can ever appear here is the one we're already talking to, so tapping
                            // it just closes back to the CharacterDetailsView the player came from
                            // instead of pushing a duplicate of the same detail screen on top.
                            onAction = { action ->
                                if (action is MessageAction.ClickCharacter) onBack()
                            },
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                        )
                    }

                    reasoningChunk?.let { chunk ->
                        item(key = "reasoning") {
                            AnimatedContent(
                                chunk,
                                transitionSpec = {
                                    fadeIn(tween(1200)) + slideInVertically { it } togetherWith
                                        fadeOut(tween(1500)) + slideOutVertically { it }
                                },
                            ) { text ->
                                Text(
                                    text = text,
                                    style =
                                        MaterialTheme.typography.labelMedium.copy(
                                            shadow =
                                                Shadow(
                                                    MaterialTheme.colorScheme.primary,
                                                    blurRadius = 5f,
                                                ),
                                            fontWeight = FontWeight.Normal,
                                            brush = Brush.horizontalGradient(morphingGradient()),
                                        ),
                                    textAlign = TextAlign.Center,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier =
                                        Modifier
                                            .levitate()
                                            .padding(16.dp)
                                            .fillMaxWidth()
                                            .alpha(.5f),
                                )
                            }
                        }
                    }

                    if (hasError) {
                        item {
                            Text(
                                stringResource(R.string.message_reply_error),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier =
                                    Modifier
                                        .padding(horizontal = 16.dp, vertical = 4.dp)
                                        .fillMaxWidth(),
                            )
                        }
                    }
                }

                EpilogueChatInput(
                    protagonist = protagonist?.data,
                    genre = resolvedGenre,
                    isReplying = isReplying,
                    onSend = { viewModel.sendMessage(it) },
                )
            }
        }
    }
}

/**
 * Placeholder icon ([R.drawable.ic_spark]) until a dedicated one is designed — opens a tooltip
 * explaining the conversation is temporary, replacing the old persistent disclaimer banner.
 */
@Composable
private fun EpilogueInfoAction() {
    val tooltipState = rememberTooltipState(isPersistent = true)
    val coroutineScope = rememberCoroutineScope()

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        state = tooltipState,
        tooltip = {
            PlainTooltip {
                Text(stringResource(R.string.epilogue_chat_disclaimer))
            }
        },
    ) {
        IconButton(onClick = { coroutineScope.launch { tooltipState.show() } }, modifier = Modifier.size(32.dp).padding(8.dp)) {
            Icon(
                painterResource(R.drawable.ic_temp),
                contentDescription = stringResource(R.string.epilogue_chat_disclaimer),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/** Mirrors [com.ilustris.sagai.features.saga.chat.ui.components.ChatInputView]'s rotating
 * sweep-gradient border while a reply is streaming in. */
@Composable
private fun epilogueInputBorderRotation(isReplying: Boolean): Float {
    if (!isReplying || !rememberLifecycleAnimationsActive()) return 0f
    val infiniteTransition = rememberInfiniteTransition(label = "epilogueInputBorder")
    val rotation by infiniteTransition.animateFloat(
        0f,
        360f,
        infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "rotation",
    )
    return rotation
}

/**
 * Deliberately mirrors [com.ilustris.sagai.features.saga.chat.ui.components.ChatInputView]'s
 * container styling (dropShadow + gradient border + rounded surface, matching the active genre
 * theme, rotating sweep gradient while a reply streams in) and its send-button loading treatment,
 * but stripped down to what an epilogue chat actually needs: no expressive tags, no @mention/wiki
 * lookup, no character switcher — just the protagonist's avatar (this is the player's own
 * character speaking, not the one being chatted with), a text field, and a send button.
 */
@Composable
private fun EpilogueChatInput(
    protagonist: Character?,
    genre: Genre,
    isReplying: Boolean,
    onSend: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    val resolvedColor = MaterialTheme.colorScheme.primary
    val inputBrush =
        Brush.horizontalGradient(
            if (isReplying) morphingGradient() else themeBrushColors(),
        )
    // dropShadow reallocates its shadow layer whenever brush/color changes, so it gets a fixed
    // brush regardless of isReplying — the animated color motion stays on the outline/border
    // draws below, which are cheap stroke operations with no shadow layer behind them.
    val shadowBrush = Brush.horizontalGradient(themeBrushColors())
    val inputShape = sagaShape()
    val palette = themeBrushColors()
    val rotation = epilogueInputBorderRotation(isReplying)
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
                    brush = shadowBrush
                    radius = glowRadiusState.value
                    spread = 10f
                }).fillMaxWidth()
                .clip(inputShape)
                .drawWithContent {
                    drawContent()
                    val outline = inputShape.createOutline(size, layoutDirection, this)
                    if (isReplying) {
                        val brush =
                            object : ShaderBrush() {
                                override fun createShader(size: Size): Shader {
                                    val shader =
                                        (Brush.sweepGradient(colors = palette) as ShaderBrush).createShader(size)
                                    val matrix = Matrix()
                                    matrix.setRotate(rotation, size.width / 2, size.height / 2)
                                    shader.setLocalMatrix(matrix)
                                    return shader
                                }
                            }
                        drawOutline(outline, brush, style = Stroke(1.dp.toPx()))
                    } else {
                        drawOutline(outline, inputBrush, style = Stroke(1.dp.toPx()))
                    }
                }.border(1.dp, inputBrush, inputShape)
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
                protagonist?.let {
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
