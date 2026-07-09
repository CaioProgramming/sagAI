package com.ilustris.sagai.ui.components.globalshell

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.debug.DebugImageFallbackService
import com.ilustris.sagai.core.globalshell.BookGenerationWorkEffect
import com.ilustris.sagai.core.globalshell.BookReadyEffect
import com.ilustris.sagai.core.globalshell.ChatGenerationWorkEffect
import com.ilustris.sagai.core.globalshell.GlobalShellEffect
import com.ilustris.sagai.core.globalshell.GlobalShellExpansion
import com.ilustris.sagai.core.globalshell.GlobalShellUiState
import com.ilustris.sagai.core.globalshell.ImageGenerationWorkEffect
import com.ilustris.sagai.core.globalshell.NewChapterEffect
import com.ilustris.sagai.core.globalshell.NewCharacterEffect
import com.ilustris.sagai.core.globalshell.NewMessageEffect
import com.ilustris.sagai.core.globalshell.ReviewReadyEffect
import com.ilustris.sagai.core.navigation.SagaNavigationTracker
import com.ilustris.sagai.features.act.data.model.BookGenerationUiState
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.debug.ui.ManualImageFallbackContent
import com.ilustris.sagai.features.imagegeneration.model.ImageGenerationUiState
import com.ilustris.sagai.features.imagegeneration.model.ImageGenerationUiState.AwaitingManualFallback
import com.ilustris.sagai.features.imagegeneration.model.ImageGenerationUiState.Generating
import com.ilustris.sagai.features.imagegeneration.model.IslandExpansion
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.resolveBackground
import com.ilustris.sagai.features.saga.chat.data.model.ChatGenerationUiState
import com.ilustris.sagai.features.saga.chat.ui.components.ExpressiveText
import com.ilustris.sagai.ui.components.taskshell.TaskShellBar
import com.ilustris.sagai.ui.components.taskshell.TaskShellChevron
import com.ilustris.sagai.ui.components.taskshell.TaskShellCompactClick
import com.ilustris.sagai.ui.components.taskshell.TaskShellContent
import com.ilustris.sagai.ui.components.taskshell.TaskShellExpandedBody
import com.ilustris.sagai.ui.components.taskshell.TaskShellExpansion
import com.ilustris.sagai.ui.components.taskshell.TaskShellLayout
import com.ilustris.sagai.ui.components.taskshell.TaskShellScope
import com.ilustris.sagai.ui.components.taskshell.TaskShellSlotState
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.morphingGradient

/**
 * Global overlay host that replaces [com.ilustris.sagai.features.imagegeneration.ui.ImageGenerationContainer]
 * and the legacy notification banner/router.
 */
@Composable
fun GlobalShellHost(
    globalState: GlobalShellUiState,
    imageGenState: ImageGenerationUiState,
    bookGenState: BookGenerationUiState,
    chatGenState: Map<Int, ChatGenerationUiState.Generating>,
    sagaNavigationTracker: SagaNavigationTracker,
    debugImageFallbackService: DebugImageFallbackService,
    onImageSetExpansion: (IslandExpansion) -> Unit,
    onImageCancel: () -> Unit,
    onNavigate: (deepLink: String) -> Unit,
    onDismiss: () -> Unit,
    onSetGlobalExpansion: (GlobalShellExpansion) -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Read-only; establishes recomposition whenever the visible screen changes so the
    // isOnChronicle/isOnChatForSaga checks below stay fresh.
    @Suppress("UNUSED_VARIABLE")
    val currentKey by sagaNavigationTracker.currentKey.collectAsState()

    val isImageGenShellActive =
        imageGenState is Generating ||
            imageGenState is AwaitingManualFallback

    // If image generation is active (shell), it takes the "sticky work" slot.
    if (isImageGenShellActive) {
        val imageSlotExpansion =
            if (imageGenState is Generating) {
                imageGenState.expansion
            } else if (imageGenState is AwaitingManualFallback) {
                imageGenState.expansion
            } else {
                IslandExpansion.Compact
            }
        val taskShellExpansion =
            if (imageSlotExpansion == IslandExpansion.Expanded) TaskShellExpansion.Expanded else TaskShellExpansion.Collapsed

        val topSlot =
            TaskShellSlotState(
                content =
                    ImageGenerationShellContent(
                        state = imageGenState,
                        debugImageFallbackService = debugImageFallbackService,
                        onImageCancel = onImageCancel,
                        onImageSetExpansion = onImageSetExpansion,
                    ),
                expansion = taskShellExpansion,
                onExpansionChange = { newExpansion ->
                    val islandExpansion =
                        when (newExpansion) {
                            TaskShellExpansion.Collapsed -> IslandExpansion.Compact

                            TaskShellExpansion.Expanded,
                            TaskShellExpansion.Full,
                            -> IslandExpansion.Expanded
                        }
                    onImageSetExpansion(islandExpansion)
                },
            )

        return TaskShellLayout(
            modifier = modifier,
            topSlot = topSlot,
        ) {
            content()
        }
    }

    // Book generation takes the "sticky work" slot next (image gen already claimed it above).
    // Skip if the user is already on Chronicle watching the same progress inline.
    if (bookGenState is BookGenerationUiState.Generating &&
        !sagaNavigationTracker.isOnChronicle(bookGenState.sagaId)
    ) {
        var bookGenExpansion by remember { mutableStateOf(TaskShellExpansion.Collapsed) }

        val topSlot =
            TaskShellSlotState(
                content = BookGenerationShellContent(state = bookGenState),
                expansion = bookGenExpansion,
                onExpansionChange = { bookGenExpansion = it },
            )

        return TaskShellLayout(
            modifier = modifier,
            topSlot = topSlot,
        ) {
            content()
        }
    }

    // Chat reply generation takes the slot next. Multiple sagas can be generating at
    // once; show the oldest one that isn't the chat currently open (already visible inline).
    val visibleChatGen =
        chatGenState.values.firstOrNull { !sagaNavigationTracker.isOnChatForSaga(it.sagaId) }
    if (visibleChatGen != null) {
        var chatGenExpansion by remember { mutableStateOf(TaskShellExpansion.Collapsed) }

        val topSlot =
            TaskShellSlotState(
                content = ChatGenerationShellContent(state = visibleChatGen),
                expansion = chatGenExpansion,
                onExpansionChange = { chatGenExpansion = it },
            )

        return TaskShellLayout(
            modifier = modifier,
            topSlot = topSlot,
        ) {
            content()
        }
    }

    val effect = globalState.effect

    val topSlot =
        effect?.let {
            val taskShellExpansion =
                when (globalState.expansion) {
                    GlobalShellExpansion.Collapsed -> TaskShellExpansion.Collapsed

                    GlobalShellExpansion.Expanded,
                    GlobalShellExpansion.Full,
                    -> TaskShellExpansion.Expanded
                }

            TaskShellSlotState(
                content = effect.toTaskShellContent(onNavigate, onDismiss),
                expansion = taskShellExpansion,
                onExpansionChange = { newExpansion ->
                    val next =
                        when (newExpansion) {
                            TaskShellExpansion.Collapsed -> GlobalShellExpansion.Collapsed

                            TaskShellExpansion.Expanded,
                            TaskShellExpansion.Full,
                            -> GlobalShellExpansion.Expanded
                        }
                    onSetGlobalExpansion(next)
                },
            )
        }

    TaskShellLayout(
        modifier = modifier,
        topSlot = topSlot,
    ) {
        content()
    }
}

/**
 * Genre-scoped background layer for global shell content. Rendered once by
 * [com.ilustris.sagai.ui.components.taskshell.TaskShellLayout] behind Compact+Expanded
 * (see [com.ilustris.sagai.ui.components.taskshell.TaskShellContent.Background]), so pass
 * `Modifier.matchParentSize()` from the call site. Wraps in [SagAITheme] so colors reflect
 * the effect's saga genre without leaking into whatever screen sits underneath (that
 * screen composes from a separate branch of [TaskShellLayout], not from inside this).
 */
@Composable
private fun GlobalShellThemedBackground(
    genre: Genre,
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
) {
    SagAITheme(genre) {
        val backgroundColor by animateColorAsState(
            targetValue = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
            animationSpec = tween(360, easing = FastOutSlowInEasing),
        )
        Box(modifier.background(backgroundColor))
    }
}

@Composable
private fun GlobalShellEffect.toTaskShellContent(
    onNavigate: (String) -> Unit,
    onDismiss: () -> Unit,
): TaskShellContent =
    when (this) {
        is NewMessageEffect -> {
            NewMessageShellContent(this, onNavigate, onDismiss)
        }

        is NewChapterEffect,
        is NewCharacterEffect,
        is ReviewReadyEffect,
        is BookReadyEffect,
        -> {
            GenericNotificationShellContent(this, onNavigate, onDismiss)
        }

        is ImageGenerationWorkEffect,
        is BookGenerationWorkEffect,
        is ChatGenerationWorkEffect,
        -> {
            GenericNotificationShellContent(
                this,
                onNavigate,
                onDismiss,
            )
        }
    }

private class ImageGenerationShellContent(
    val state: ImageGenerationUiState,
    val debugImageFallbackService: DebugImageFallbackService,
    val onImageCancel: () -> Unit,
    val onImageSetExpansion: (IslandExpansion) -> Unit,
) : TaskShellContent {
    override val isDraggable: Boolean get() = false
    override val isExpandable: Boolean get() = true

    @Composable
    override fun Compact(scope: TaskShellScope) {
        val isExpanded = scope.expansion != TaskShellExpansion.Collapsed

        TaskShellBar(
            title = imageGenerationTaskTitle(state),
            isExpanded = isExpanded,
            onToggleExpand = {
                if (scope.expansion == TaskShellExpansion.Collapsed) {
                    onImageSetExpansion(IslandExpansion.Expanded)
                } else {
                    onImageSetExpansion(IslandExpansion.Compact)
                }
            },
            onLongClick = onImageCancel,
            modifier = Modifier.statusBarsPadding(),
            trailingContent = {
                val queueBadge =
                    when (state) {
                        is Generating -> state.queuePosition.takeIf { it > 0 }
                        else -> null
                    }
                queueBadge?.let { count ->
                    Text(
                        text = stringResource(R.string.image_generation_queue_badge, count),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
        )
    }

    @Composable
    override fun Expanded(scope: TaskShellScope) {
        TaskShellExpandedBody {
            ImageGenerationPanelBody(
                state = state,
                debugImageFallbackService = debugImageFallbackService,
                onCollapse = { onImageSetExpansion(IslandExpansion.Compact) },
                onCancel = onImageCancel,
            )
        }
    }
}

@Composable
private fun ImageGenerationPanelBody(
    state: ImageGenerationUiState,
    debugImageFallbackService: DebugImageFallbackService,
    onCollapse: () -> Unit,
    onCancel: () -> Unit,
) {
    when (state) {
        is Generating -> {
            val reasoning = state.reasoning
            Text(
                text = reasoning ?: stringResource(R.string.image_generation_reasoning_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start,
            )
        }

        is AwaitingManualFallback -> {
            if (BuildConfig.DEBUG) {
                ManualImageFallbackContent(
                    prompt = state.prompt,
                    debugImageFallbackService = debugImageFallbackService,
                    onSubmitted = onCollapse,
                    onCancel = onCancel,
                    scrollEnabled = false,
                    autoCopyPrompt = true,
                    showHeader = false,
                )
            }
        }

        else -> {
            Unit
        }
    }
}

@Composable
private fun imageGenerationTaskTitle(state: ImageGenerationUiState): String =
    when (state) {
        is Generating -> stringResource(R.string.image_generation_default_label)
        is AwaitingManualFallback -> stringResource(R.string.image_generation_awaiting_manual)
        else -> ""
    }

private class BookGenerationShellContent(
    val state: BookGenerationUiState.Generating,
) : TaskShellContent {
    override val isDraggable: Boolean get() = false
    override val isExpandable: Boolean get() = true

    @Composable
    override fun BoxScope.Background(scope: TaskShellScope) {
        GlobalShellThemedBackground(
            genre = state.genre,
            isExpanded = scope.expansion != TaskShellExpansion.Collapsed,
            modifier = Modifier.matchParentSize(),
        )
    }

    @Composable
    override fun Compact(scope: TaskShellScope) {
        val isExpanded = scope.expansion != TaskShellExpansion.Collapsed

        SagAITheme(state.genre) {
            TaskShellBar(
                title = state.actTitle,
                isExpanded = isExpanded,
                onToggleExpand = {
                    if (scope.expansion == TaskShellExpansion.Collapsed) scope.onToggle() else scope.onMinimize()
                },
                modifier = Modifier.statusBarsPadding(),
            )
        }
    }

    @Composable
    override fun Expanded(scope: TaskShellScope) {
        SagAITheme(state.genre) {
            TaskShellExpandedBody {
                Text(
                    text =
                        state.reasoning
                            ?: stringResource(R.string.book_generation_reasoning_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start,
                )
            }
        }
    }
}

private class ChatGenerationShellContent(
    val state: ChatGenerationUiState.Generating,
) : TaskShellContent {
    override val isDraggable: Boolean get() = false
    override val isExpandable: Boolean get() = true

    @Composable
    override fun BoxScope.Background(scope: TaskShellScope) {
        GlobalShellThemedBackground(
            genre = state.genre,
            isExpanded = scope.expansion != TaskShellExpansion.Collapsed,
            modifier = Modifier.matchParentSize(),
        )
    }

    @Composable
    override fun Compact(scope: TaskShellScope) {
        val isExpanded = scope.expansion != TaskShellExpansion.Collapsed

        SagAITheme(state.genre) {
            TaskShellBar(
                title = state.reasoning ?: (state.speakerName ?: state.sagaTitle),
                isExpanded = isExpanded,
                titleBrush = Brush.horizontalGradient(morphingGradient()),
                onToggleExpand = {
                    if (scope.expansion == TaskShellExpansion.Collapsed) scope.onToggle() else scope.onMinimize()
                },
                modifier = Modifier.statusBarsPadding(),
            )
        }
    }

    @Composable
    override fun Expanded(scope: TaskShellScope) {
        SagAITheme(state.genre) {
            TaskShellExpandedBody {
                Text(
                    text =
                        state.reasoning
                            ?: stringResource(R.string.chat_generation_reasoning_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start,
                )
            }
        }
    }
}

private class NewMessageShellContent(
    val effect: NewMessageEffect,
    val onNavigate: (String) -> Unit,
    val onDismiss: () -> Unit,
) : TaskShellContent {
    override val isDraggable: Boolean get() = true
    override val compactClick: TaskShellCompactClick
        get() = TaskShellCompactClick.Toggle

    @Composable
    override fun BoxScope.Background(scope: TaskShellScope) {
        GlobalShellThemedBackground(
            genre = effect.genre,
            isExpanded = scope.expansion != TaskShellExpansion.Collapsed,
            modifier = Modifier.matchParentSize(),
        )
    }

    @Composable
    override fun Compact(scope: TaskShellScope) {
        val isExpanded = scope.expansion != TaskShellExpansion.Collapsed

        SagAITheme(effect.genre) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ShellAvatar(character = effect.character, icon = effect.icon, genre = effect.genre)

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.notification_new_message),
                        style =
                            MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .6f),
                            ),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                    Text(
                        text = effect.speakerName,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                }

                TaskShellChevron(
                    isExpanded = isExpanded,
                    onClick = {
                        if (scope.expansion == TaskShellExpansion.Collapsed) scope.onToggle() else scope.onMinimize()
                    },
                )
            }
        }
    }

    @Composable
    override fun Expanded(scope: TaskShellScope) {
        SagAITheme(effect.genre) {
            TaskShellExpandedBody {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Render rich tags without showing the tag syntax.
                    ExpressiveText(
                        text = effect.rawText,
                        genre = effect.genre,
                        style =
                            MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Start,
                            ),
                        modifier = Modifier.fillMaxWidth(),
                        shouldAnimate = false,
                        characters = emptyList(),
                        wiki = emptyList(),
                        mainCharacter = null,
                        onAnnotationClick = { _ -> },
                    )

                    Button(
                        onClick = {
                            onNavigate(effect.deepLink)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(text = stringResource(R.string.notification_open_chat))
                    }
                }
            }
        }
    }
}

private class GenericNotificationShellContent(
    val effect: GlobalShellEffect,
    val onNavigate: (String) -> Unit,
    val onDismiss: () -> Unit,
) : TaskShellContent {
    override val isDraggable: Boolean get() = true

    @Composable
    override fun BoxScope.Background(scope: TaskShellScope) {
        GlobalShellThemedBackground(
            genre = effect.genre,
            isExpanded = scope.expansion != TaskShellExpansion.Collapsed,
            modifier = Modifier.matchParentSize(),
        )
    }

    @Composable
    override fun Compact(scope: TaskShellScope) {
        val isExpanded = scope.expansion != TaskShellExpansion.Collapsed

        SagAITheme(effect.genre) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ShellAvatar(character = effect.character, icon = effect.icon, genre = effect.genre)

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = genericLabelFor(effect),
                        style =
                            MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .6f),
                            ),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                    Text(
                        text = effect.message,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                }

                TaskShellChevron(
                    isExpanded = isExpanded,
                    onClick = {
                        if (scope.expansion == TaskShellExpansion.Collapsed) scope.onToggle() else scope.onMinimize()
                    },
                )
            }
        }
    }

    @Composable
    override fun Expanded(scope: TaskShellScope) {
        SagAITheme(effect.genre) {
            TaskShellExpandedBody {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = effect.message,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium,
                            ),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    if (effect !is ImageGenerationWorkEffect) {
                        Button(
                            onClick = {
                                onNavigate(effect.deepLink)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                        ) {
                            Text(text = stringResource(R.string.notification_open_chat))
                        }
                    }
                }
            }
        }
    }

    private fun genericLabelFor(effect: GlobalShellEffect): String =
        when (effect) {
            is NewChapterEffect -> "Novo capítulo"
            is NewCharacterEffect -> "Novo personagem"
            is ImageGenerationWorkEffect -> "Gerando imagem"
            is BookGenerationWorkEffect -> "Gerando livro"
            is ChatGenerationWorkEffect -> "Gerando resposta"
            is ReviewReadyEffect -> "Review pronto"
            is BookReadyEffect -> "Livro gerado"
            else -> "Atualização"
        }
}

@Composable
private fun ShellAvatar(
    character: Character?,
    icon: Bitmap?,
    genre: Genre,
) {
    if (character != null) {
        CharacterAvatar(
            character = character,
            genre = genre,
            innerPadding = 0.dp,
            borderSize = 1.dp,
            modifier = Modifier.size(28.dp),
        )
        return
    }

    val fallbackRes =
        genre?.resolveBackground(null) as? Int ?: R.drawable.ic_spark

    if (icon != null) {
        Image(
            bitmap = icon.asImageBitmap(),
            contentDescription = null,
            modifier =
                Modifier
                    .size(28.dp)
                    .clip(CircleShape),
        )
    } else {
        Image(
            painter = painterResource(fallbackRes),
            contentDescription = null,
            modifier =
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(6.dp),
        )
    }
}
