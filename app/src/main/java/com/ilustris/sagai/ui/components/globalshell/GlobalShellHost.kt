package com.ilustris.sagai.ui.components.globalshell

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.BuildConfig
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.debug.DebugImageFallbackService
import com.ilustris.sagai.core.ai.model.ImageType
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
import com.ilustris.sagai.features.characters.data.model.CharacterProfile
import com.ilustris.sagai.features.characters.data.model.Details
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
import com.ilustris.sagai.ui.components.taskshell.TaskShellContentPreview
import com.ilustris.sagai.ui.components.taskshell.TaskShellExpandedBody
import com.ilustris.sagai.ui.components.taskshell.TaskShellExpansion
import com.ilustris.sagai.ui.components.taskshell.TaskShellLayout
import com.ilustris.sagai.ui.components.taskshell.TaskShellScope
import com.ilustris.sagai.ui.components.taskshell.TaskShellSlotState
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.fadeGradientTop
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer

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
    onImageDismissReveal: () -> Unit,
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
            imageGenState is AwaitingManualFallback ||
            imageGenState is ImageGenerationUiState.Reveal

    // If image generation is active (shell), it takes the "sticky work" slot.
    if (isImageGenShellActive) {
        val isReveal = imageGenState is ImageGenerationUiState.Reveal
        val imageSlotExpansion =
            if (imageGenState is Generating) {
                imageGenState.expansion
            } else if (imageGenState is AwaitingManualFallback) {
                imageGenState.expansion
            } else {
                IslandExpansion.Compact
            }
        val taskShellExpansion =
            when {
                // Compact, content-sized reveal — a quick glance, not a takeover.
                isReveal -> TaskShellExpansion.Expanded
                imageSlotExpansion == IslandExpansion.Expanded -> TaskShellExpansion.Expanded
                else -> TaskShellExpansion.Collapsed
            }

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
                    if (isReveal) {
                        // The reveal is the terminal state — a swipe-to-collapse (or the
                        // dismiss button, via scope.onMinimize()) always means "done looking
                        // at this," so it must clear the underlying state, not just re-collapse
                        // the shell while imageGenState is still stuck on Reveal.
                        if (newExpansion == TaskShellExpansion.Collapsed) {
                            onImageDismissReveal()
                        }
                    } else {
                        val islandExpansion =
                            when (newExpansion) {
                                TaskShellExpansion.Collapsed -> IslandExpansion.Compact

                                TaskShellExpansion.Expanded,
                                TaskShellExpansion.Full,
                                -> IslandExpansion.Expanded
                            }
                        onImageSetExpansion(islandExpansion)
                    }
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
        background = { top, bottom ->
            AnimatedContent(effect?.genre, modifier = Modifier.fillMaxSize()) {
                SagAITheme(it) {
                    Box(Modifier.fillMaxSize().background(fadeGradientTop(MaterialTheme.colorScheme.primary)))
                }
            }
        },
    ) {
        content()
    }
}

/**
 * Genre-scoped themed surface wrapping a piece of global shell content. Wraps in
 * [SagAITheme] so colors reflect the effect's saga genre without leaking into whatever
 * screen sits underneath, and animates its background between the collapsed/expanded
 * colors as the shell's own state changes.
 */

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
    // Draggable in every state (not just Reveal) so Reveal gets the Full-height grow
    // animation and swipe-to-collapse for free from TaskShellTopDraggableRegion — same
    // infra the milestone reveals already use.
    override val isDraggable: Boolean get() = true
    override val isExpandable: Boolean get() = true

    @Composable
    override fun Compact(scope: TaskShellScope) {
        // The reveal owns the whole Full-expanded area itself; an empty/near-empty title
        // bar above the image would just eat space for nothing.
        if (state is ImageGenerationUiState.Reveal) return

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
        val revealState = state as? ImageGenerationUiState.Reveal
        if (revealState != null) {
            ImageGenerationRevealBody(
                state = revealState,
                onDismiss = { scope.onMinimize() },
            )
            return
        }

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
private fun ImageGenerationRevealBody(
    state: ImageGenerationUiState.Reveal,
    onDismiss: () -> Unit,
) {
    val title =
        state.label
            ?: when (state.imageType) {
                ImageType.ICON -> stringResource(R.string.image_generation_reveal_icon)
                ImageType.COVER -> stringResource(R.string.image_generation_reveal_cover)
            }

    Column(
        modifier =
            Modifier
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Box(
            modifier =
                Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(20.dp)),
        ) {
            Image(
                bitmap = state.bitmap.asImageBitmap(),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(stringResource(R.string.image_generation_reveal_dismiss))
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
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
        when (state) {
            is Generating -> {
                val reasoning = state.reasoning
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = reasoning ?: stringResource(R.string.image_generation_reasoning_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().reactiveShimmer(true, repeatMode = RepeatMode.Restart),
                    )

                    TextButton(onClick = onCancel) {
                        Text(
                            text = stringResource(R.string.cancel),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
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
                    textAlign = TextAlign.Center,
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
            modifier = Modifier.size(24.dp),
        )
        return
    }

    Icon(
        painterResource(genre?.icon ?: R.drawable.ic_spark),
        null,
        tint = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.size(24.dp),
    )
}

private val previewGenre = Genre.entries.first()
private val previewCharacter =
    Character(
        name = "Elara",
        lastName = "Vance",
        details = Details(),
        profile = CharacterProfile(),
    )

/** Mirrors the real `background` lambda [GlobalShellHost] itself passes to [TaskShellLayout]. */
@Composable
private fun BoxScope.GlobalShellPreviewBackground(genre: Genre) {
    SagAITheme(genre) {
        Box(Modifier.fillMaxSize().background(fadeGradientTop(MaterialTheme.colorScheme.primary)))
    }
}

@Preview(
    name = "Image gen - reasoning",
    showBackground = false,
    device = "spec:width=1080px,height=2340px,dpi=440,isRound=true",
    showSystemUi = false,
)
@Composable
private fun ImageGenerationShellContentPreview() {
    TaskShellContentPreview(
        content =
            ImageGenerationShellContent(
                state =
                    Generating(
                        label = "Gerando retrato de personagem",
                        reasoning = "Compondo a iluminação e o enquadramento...",
                        imageType = ImageType.ICON,
                        queuePosition = 1,
                    ),
                debugImageFallbackService = DebugImageFallbackService(),
                onImageCancel = {},
                onImageSetExpansion = {},
            ),
        initialExpansion = TaskShellExpansion.Expanded,
        background = { _, _ -> GlobalShellPreviewBackground(previewGenre) },
    )
}

@Preview(name = "Book gen", showBackground = true)
@Composable
private fun BookGenerationShellContentPreview() {
    TaskShellContentPreview(
        content =
            BookGenerationShellContent(
                state =
                    BookGenerationUiState.Generating(
                        sagaId = 0,
                        sagaTitle = "Sombras de Poeira",
                        actId = 0,
                        actTitle = "Ato I: O Despertar",
                        genre = previewGenre,
                        reasoning = "Costurando os capítulos em um único volume...",
                    ),
            ),
        initialExpansion = TaskShellExpansion.Expanded,
        background = { _, _ -> GlobalShellPreviewBackground(previewGenre) },
    )
}

@Preview(name = "Chat gen", showBackground = true)
@Composable
private fun ChatGenerationShellContentPreview() {
    TaskShellContentPreview(
        content =
            ChatGenerationShellContent(
                state =
                    ChatGenerationUiState.Generating(
                        sagaId = 0,
                        sagaTitle = "Sombras de Poeira",
                        genre = previewGenre,
                        speakerName = "Elara Vance",
                        reasoning = "Elara pondera sua próxima resposta...",
                    ),
            ),
        initialExpansion = TaskShellExpansion.Expanded,
        background = { _, _ -> GlobalShellPreviewBackground(previewGenre) },
    )
}

@Preview(name = "New message", showBackground = true)
@Composable
private fun NewMessageShellContentPreview() {
    TaskShellContentPreview(
        content =
            NewMessageShellContent(
                effect =
                    NewMessageEffect(
                        messageId = 0,
                        sagaId = 0,
                        sagaTitle = "Sombras de Poeira",
                        genre = previewGenre,
                        speakerName = "Elara Vance",
                        rawText = "A porta se abriu sozinha. Alguém — ou algo — já está aqui.",
                        character = previewCharacter,
                        deepLink = "",
                    ),
                onNavigate = {},
                onDismiss = {},
            ),
        initialExpansion = TaskShellExpansion.Expanded,
        background = { _, _ -> GlobalShellPreviewBackground(previewGenre) },
    )
}

@Preview(name = "Generic - new chapter", showBackground = true)
@Composable
private fun GenericNotificationShellContentPreview() {
    TaskShellContentPreview(
        content =
            GenericNotificationShellContent(
                effect =
                    NewChapterEffect(
                        chapterId = 0,
                        sagaId = 0,
                        sagaTitle = "Sombras de Poeira",
                        genre = previewGenre,
                        chapterTitle = "O Limiar",
                        deepLink = "",
                    ),
                onNavigate = {},
                onDismiss = {},
            ),
        initialExpansion = TaskShellExpansion.Expanded,
        background = { _, _ -> GlobalShellPreviewBackground(previewGenre) },
    )
}
