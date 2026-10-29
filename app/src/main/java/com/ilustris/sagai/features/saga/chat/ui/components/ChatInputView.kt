@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.ilustris.sagai.features.saga.chat.ui.components
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.characters.ui.components.buildSuggestionAnnotatedString
import com.ilustris.sagai.features.characters.ui.components.transformTextWithContent
import com.ilustris.sagai.features.home.data.model.SagaMetadata
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.ui.GlassChip
import com.ilustris.sagai.features.newsaga.ui.glassSurface
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.data.model.TypoFix
import com.ilustris.sagai.features.saga.chat.data.model.TypoStatus
import com.ilustris.sagai.features.saga.chat.data.model.icon
import com.ilustris.sagai.features.saga.chat.data.model.senderForTag
import com.ilustris.sagai.features.saga.chat.data.model.title
import com.ilustris.sagai.features.saga.chat.domain.model.Suggestion
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.ui.animations.rememberLifecycleAnimationsActive
import com.ilustris.sagai.ui.components.IosStyleMenuHost
import com.ilustris.sagai.ui.components.IosStyleMenuItem
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.sagaShape
import com.ilustris.sagai.ui.theme.solidGradient

private val ChatInputTextMaxHeight = 160.dp

/** One cell of the input row: the visible control is smaller, the touch target is this size. */
private val ChatInputCell = 48.dp
private val ChatInputControlSize = 36.dp
private const val SUGGESTION_WIDTH_FRACTION = 0.66f

/** More opaque than the other glass: what the player types has to stay readable over the colored backdrop. */
private const val ChatInputGlassAlpha = 0.7f

/** The character count ring stays hidden until the message is this close to the limit. */
private const val COUNTER_VISIBLE_FROM = 0.8f
private val ThinkBorderDash = floatArrayOf(8f, 6f)

private fun DrawScope.drawThinkTagBorders(
    layoutResult: TextLayoutResult,
    ranges: List<IntRange>,
    color: Color,
) {
    val textLength = layoutResult.layoutInput.text.length
    val stroke = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(ThinkBorderDash, 0f))
    ranges.forEach { range ->
        val start = range.first.coerceIn(0, textLength)
        val end = (range.last + 1).coerceIn(start, textLength)
        if (start >= end) return@forEach
        val startLine = layoutResult.getLineForOffset(start)
        val endLine = layoutResult.getLineForOffset(end - 1)
        if (startLine == endLine) {
            val startBox = layoutResult.getBoundingBox(start)
            val endBox = layoutResult.getBoundingBox(end - 1)
            val pad = 3.dp.toPx()
            drawRoundRect(
                color = color,
                topLeft =
                    Offset(
                        minOf(startBox.left, endBox.left) - pad,
                        minOf(startBox.top, endBox.top) - pad,
                    ),
                size =
                    Size(
                        maxOf(startBox.right, endBox.right) - minOf(startBox.left, endBox.left) + pad * 2,
                        maxOf(startBox.bottom, endBox.bottom) - minOf(startBox.top, endBox.top) + pad * 2,
                    ),
                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                style = stroke,
            )
        } else {
            drawPath(layoutResult.getPathForRange(start, end), color = color, style = stroke)
        }
    }
}

/** The glass light swells and fades while a reply is being written; null when it should sit still. */
@Composable
private fun breathingStrength(isGenerating: Boolean): State<Float>? {
    if (!isGenerating || !rememberLifecycleAnimationsActive()) return null
    return rememberInfiniteTransition(label = "inputBreathing").animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "inputBreathingValue",
    )
}

private fun isIndexInsideTagMarkup(
    text: String,
    index: Int,
): Boolean {
    val open = text.lastIndexOf('<', index)
    if (open == -1) return false
    val close = text.indexOf('>', open)
    return close == -1 || index <= close
}

private fun detectQueryType(
    text: String,
    cursorPosition: Int,
    characters: List<Character>,
    wikis: List<Wiki>,
): ItemsType? {
    val cursor = cursorPosition.coerceIn(0, text.length)
    val textBeforeCursor = text.substring(0, cursor)
    if (!textBeforeCursor.contains('@') && !textBeforeCursor.contains('/')) return null
    val lastAtIndex = textBeforeCursor.lastIndexOf('@')
    val lastSlashIndex = textBeforeCursor.lastIndexOf('/')
    val isCharacterQuery = lastAtIndex != -1 && lastAtIndex > lastSlashIndex
    val isWikiQuery = lastSlashIndex != -1 && lastSlashIndex > lastAtIndex
    return when {
        isCharacterQuery -> {
            if (isIndexInsideTagMarkup(text, lastAtIndex)) return null
            val query = textBeforeCursor.substring(lastAtIndex + 1)
            if (query.contains(' ') || query.contains('\n')) return null
            val filtered =
                characters.filter { character ->
                    character.name.isNotBlank() &&
                        (query.isEmpty() || character.name.contains(query, ignoreCase = true))
                }
            if (filtered.isNotEmpty()) ItemsType.Characters(filtered, query) else null
        }

        isWikiQuery -> {
            if (isIndexInsideTagMarkup(text, lastSlashIndex)) return null
            val query = textBeforeCursor.substring(lastSlashIndex + 1)
            if (query.contains(' ') || query.contains('\n')) return null
            val filtered =
                wikis.filter { wiki ->
                    wiki.title.isNotBlank() &&
                        (query.isEmpty() || wiki.title.contains(query, ignoreCase = true))
                }
            if (filtered.isNotEmpty()) ItemsType.Wikis(filtered, query) else null
        }

        else -> {
            null
        }
    }
}

private fun replaceQueryInText(
    text: String,
    symbol: Char,
    replacement: String,
    cursorPosition: Int,
): String {
    val cursor = cursorPosition.coerceIn(0, text.length)
    val textBeforeCursor = text.substring(0, cursor)
    val startIndex = textBeforeCursor.lastIndexOf(symbol)
    if (startIndex == -1) return text
    val bounds = getTagContentBounds(text, cursor)
    val replaceEnd = cursor.coerceAtMost(bounds?.contentEnd ?: text.length)
    return text.substring(0, startIndex) + replacement + " " + text.substring(replaceEnd)
}

private fun handleCharacterSelection(
    character: Character,
    currentInput: TextFieldValue,
    onUpdateInput: (TextFieldValue) -> Unit,
) {
    val cursor = currentInput.selection.start
    val textBeforeCursor =
        currentInput.text.substring(0, cursor.coerceIn(0, currentInput.text.length))
    val startIndex = textBeforeCursor.lastIndexOf('@')
    if (startIndex == -1) return
    val newText = replaceQueryInText(currentInput.text, '@', character.name, cursor)
    val newCursor = startIndex + character.name.length + 1
    onUpdateInput(TextFieldValue(newText, TextRange(newCursor.coerceIn(0, newText.length))))
}

private fun handleWikiSelection(
    wiki: Wiki,
    currentInput: TextFieldValue,
    onUpdateInput: (TextFieldValue) -> Unit,
) {
    val cursor = currentInput.selection.start
    val textBeforeCursor =
        currentInput.text.substring(0, cursor.coerceIn(0, currentInput.text.length))
    val startIndex = textBeforeCursor.lastIndexOf('/')
    if (startIndex == -1) return
    val newText = replaceQueryInText(currentInput.text, '/', wiki.title, cursor)
    val newCursor = startIndex + wiki.title.length + 1
    onUpdateInput(TextFieldValue(newText, TextRange(newCursor.coerceIn(0, newText.length))))
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ChatInputView(
    content: SagaMetadata,
    characters: List<Character>,
    isGenerating: Boolean,
    suggestions: List<Suggestion>,
    modifier: Modifier = Modifier,
    inputField: TextFieldValue,
    @Suppress("UNUSED_PARAMETER") sendType: SenderType,
    typoFix: TypoFix?,
    selectedCharacter: Character? = null,
    isSendingPending: Boolean = false,
    @Suppress("UNUSED_PARAMETER") sendingProgress: Float = 0f,
    onUpdateInput: (TextFieldValue) -> Unit,
    onUpdateSender: (SenderType) -> Unit,
    onSendMessage: (Boolean) -> Unit,
    onSelectCharacter: (Character) -> Unit = {},
    onRequestAudio: () -> Unit = {},
    /** When true, the send button turns into the live conversation entry while the input is empty. */
    liveEnabled: Boolean = false,
    /** Applied to the speaking character's avatar (the live screen's shared element lands on it). */
    avatarModifier: Modifier = Modifier,
    isEditing: Boolean = false,
    onCancelEdit: () -> Unit = {},
    maxContentLength: Int = 2000,
    onStopGeneration: () -> Unit = {},
) {
    var characterMenu by remember { mutableStateOf(false) }
    var speechModeMenu by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val speechModeOptions =
        remember {
            listOf(
                SenderType.CHARACTER to null,
                SenderType.NARRATOR to ExpressiveTag.NARRATOR,
                SenderType.ACTION to ExpressiveTag.ACTION,
                SenderType.THOUGHT to ExpressiveTag.THINK,
            )
        }

    LaunchedEffect(inputField.text.length) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    val actualCharacter = selectedCharacter ?: content.mainCharacter
    val genre = content.data.genre
    val resolvedColor = MaterialTheme.colorScheme.primary
    val contentColor = MaterialTheme.colorScheme.onBackground
    val textStyle =
        MaterialTheme.typography.labelMedium.copy(
            color = contentColor,
            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
        )
    val tagBg = MaterialTheme.colorScheme.background
    val thinkTagSurface = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.4f)
    val textColor = textStyle.color
    val queryItemsType =
        remember(inputField.text, inputField.selection, characters, content.wikis) {
            detectQueryType(
                inputField.text,
                inputField.selection.start,
                characters,
                content.wikis,
            )
        }
    val inputShape = sagaShape()
    val breathing = breathingStrength(isGenerating)

    val tagMarkerLabels =
        mapOf(
            "action" to stringResource(R.string.sender_type_action_title),
            "think" to stringResource(R.string.sender_type_thought_title),
            "narrator" to stringResource(R.string.sender_type_narrator_title),
        )

    val visualTransformation =
        remember(tagBg, textColor, tagMarkerLabels, thinkTagSurface) {
            VisualTransformation { text ->
                transformTextWithContent(
                    mainCharacter = null,
                    characters = emptyList(),
                    wiki = emptyList(),
                    text = text.text,
                    genreColor = resolvedColor,
                    tagBackgroundColor = tagBg,
                    textColor = textColor,
                    headerFont = null,
                    bodyFont = null,
                    tagMarkerLabels = tagMarkerLabels,
                    thinkTagSurfaceColor = thinkTagSurface,
                    annotateMentions = false,
                )
            }
        }
    val thinkRanges =
        remember(inputField.text, tagBg, textColor, tagMarkerLabels, thinkTagSurface) {
            transformTextWithContent(
                mainCharacter = null,
                characters = emptyList(),
                wiki = emptyList(),
                text = inputField.text,
                genreColor = resolvedColor,
                tagBackgroundColor = tagBg,
                textColor = textColor,
                headerFont = null,
                bodyFont = null,
                tagMarkerLabels = tagMarkerLabels,
                thinkTagSurfaceColor = thinkTagSurface,
                annotateMentions = false,
            ).text.spanStyles
                .filter { it.item.background == thinkTagSurface }
                .map { it.start until it.end }
                .sortedBy { it.first }
                .fold(mutableListOf<IntRange>()) { acc, range ->
                    val last = acc.lastOrNull()
                    if (last != null && range.first <= last.last + 1) {
                        acc[acc.lastIndex] = last.first..maxOf(last.last, range.last)
                    } else {
                        acc.add(range)
                    }
                    acc
                }
        }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun sendMessage(confirmed: Boolean = false) {
        val finalized = finalizeInputForSend(inputField)
        onUpdateInput(finalized)
        onSendMessage(confirmed)
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    Column(modifier.fillMaxWidth()) {
        if (isEditing) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.editing_message),
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            color = resolvedColor.copy(alpha = .5f),
                        ),
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                )
            }
        }
        val isImeVisible = WindowInsets.isImeVisible
        val currentTagInside =
            remember(inputField.text, inputField.selection) {
                getCursorInsideTag(
                    inputField.text,
                    inputField.selection.start,
                )
            }
        val isInsideTag = currentTagInside != null
        val activeSpeechMode =
            currentTagInside?.let { SenderType.senderForTag(it) } ?: SenderType.CHARACTER
        val isLoading = isSendingPending || isGenerating
        val progress = getCleanTextLength(inputField.text).toFloat() / maxContentLength

        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 12.dp),
        ) {
            InputRail(
                queryItemsType = queryItemsType,
                suggestions = suggestions,
                // A suggestion replaces the whole text, so it is only offered over an empty field.
                showSuggestions = isImeVisible && inputField.text.isBlank(),
                genre = genre,
                accent = resolvedColor,
                onSuggestionClick = {
                    onUpdateInput(TextFieldValue(it.text, TextRange(it.text.length)))
                },
                onCharacterClick = { handleCharacterSelection(it, inputField, onUpdateInput) },
                onWikiClick = { handleWikiSelection(it, inputField, onUpdateInput) },
            )

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
            ) {
                Box(
                    Modifier
                        .size(ChatInputCell)
                        .clip(CircleShape)
                        .clickable { characterMenu = true },
                    contentAlignment = Alignment.Center,
                ) {
                    AnimatedContent(
                        targetState =
                            actualCharacter?.let { it.id to it.image }
                                ?: (content.mainCharacter?.id to content.mainCharacter?.image),
                        modifier =
                            avatarModifier
                                .size(ChatInputControlSize)
                                .clip(CircleShape),
                        label = "ChatInputAvatar",
                    ) {
                        val character = actualCharacter ?: content.mainCharacter
                        character?.let {
                            CharacterAvatar(
                                it,
                                genre = genre,
                                grainRadius = 0f,
                                pixelation = 0f,
                                useFallback = false,
                                modifier = Modifier.fillMaxSize(),
                                borderSize = 1.dp,
                                innerPadding = 0.dp,
                            )
                        }
                    }
                }

                Row(
                    Modifier
                        .weight(1f)
                        .glassSurface(
                            inputShape,
                            resolvedColor,
                            strength = { breathing?.value ?: 1f },
                            baseAlpha = ChatInputGlassAlpha,
                        ),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    BasicTextField(
                        inputField,
                        enabled = !isGenerating,
                        maxLines = if (!isImeVisible) 1 else Int.MAX_VALUE,
                        onValueChange = { newValue ->
                            processInputChange(inputField, newValue, maxContentLength)?.let {
                                onUpdateInput(it)
                            }
                        },
                        textStyle = textStyle,
                        visualTransformation = visualTransformation,
                        cursorBrush = resolvedColor.solidGradient(),
                        onTextLayout = { textLayoutResult = it },
                        decorationBox = { inner ->
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = ChatInputCell)
                                    .padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                Box(
                                    Modifier.drawWithContent {
                                        drawContent()
                                        textLayoutResult?.let { layoutResult ->
                                            drawThinkTagBorders(
                                                layoutResult,
                                                thinkRanges,
                                                resolvedColor.copy(alpha = .6f),
                                            )
                                        }
                                    },
                                ) {
                                    inner()
                                    if (inputField.text.isEmpty()) {
                                        Text(
                                            activeSpeechMode.hint(),
                                            style = textStyle,
                                            modifier =
                                                Modifier
                                                    .alpha(.5f)
                                                    .fillMaxWidth(),
                                            maxLines = 1,
                                        )
                                    }
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = if (isInsideTag) ImeAction.Next else ImeAction.Default),
                        keyboardActions =
                            KeyboardActions(onNext = {
                                if (isInsideTag) {
                                    onUpdateInput(escapeCursorFromTagAndClean(inputField))
                                }
                            }),
                        modifier =
                            Modifier
                                .weight(1f)
                                .heightIn(min = ChatInputCell, max = ChatInputTextMaxHeight)
                                .verticalScroll(scrollState),
                    )

                    if (!isGenerating) {
                        SpeechModeButton(
                            mode = activeSpeechMode,
                            isInsideTag = isInsideTag,
                            // New players need the word; once the keyboard is up the icon is enough.
                            showLabel = !isImeVisible,
                            expanded = speechModeMenu,
                            options = speechModeOptions,
                            onExpandedChange = { speechModeMenu = it },
                            onOptionSelected = { expressiveTag ->
                                if (expressiveTag != null) {
                                    onUpdateInput(insertExpressiveTag(inputField, expressiveTag))
                                } else if (isInsideTag) {
                                    onUpdateInput(escapeCursorFromTagAndClean(inputField))
                                }
                            },
                        )
                    }
                }

                // Nothing typed and nothing generating: the button opens live mode instead.
                val showsLive = liveEnabled && inputField.text.isBlank() && !isLoading && !isEditing
                val canSend = inputField.text.isNotBlank() || isGenerating || showsLive

                SendButton(
                    isLoading = isLoading,
                    showsLive = showsLive,
                    enabled = canSend,
                    progress = progress,
                    onClick = {
                        when {
                            isLoading -> onStopGeneration()
                            showsLive -> onRequestAudio()
                            else -> sendMessage()
                        }
                    },
                )
            }
        }

        if (characterMenu) {
            ModalBottomSheet(
                onDismissRequest = { characterMenu = false },
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.large,
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text(
                        stringResource(R.string.select_character),
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        textAlign = TextAlign.Center,
                        modifier =
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(100.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(bottom = 32.dp),
                    ) {
                        items(
                            count = characters.size,
                            key = { index ->
                                val c = characters[index]
                                "${c.id}-${c.image}"
                            },
                        ) { index ->
                            val character = characters[index]
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier =
                                    Modifier
                                        .clip(MaterialTheme.shapes.medium)
                                        .clickable {
                                            onSelectCharacter(character)
                                            characterMenu = false
                                        }
                                        .padding(8.dp),
                            ) {
                                CharacterAvatar(
                                    character,
                                    genre = genre,
                                    modifier = Modifier.size(64.dp),
                                    grainRadius = 0f,
                                    pixelation = 0f,
                                )
                                Text(
                                    character.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center,
                                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        val sheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        typoFix?.let {
            if (it.status != TypoStatus.OK) {
                ModalBottomSheet(
                    { sendMessage(true) },
                    sheetState = sheet,
                    containerColor = Color.Transparent,
                ) {
                    Column(
                        Modifier
                            .padding(16.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainer, inputShape)
                            .padding(16.dp),
                    ) {
                        Text(it.friendlyMessage ?: "", modifier = Modifier.alpha(.4f))
                        Text(
                            it.suggestedText ?: "",
                            style = MaterialTheme.typography.bodyLarge.copy(brush = genre.gradient()),
                        )
                        Button({
                            it.suggestedText?.let { t ->
                                onUpdateInput(
                                    TextFieldValue(
                                        t,
                                        TextRange(t.length),
                                    ),
                                )
                            }
                            sendMessage(true)
                        }) { Text(stringResource(R.string.chat_input_fix)) }
                    }
                }
            }
        }
    }
}

/**
 * One rail above the input: the mention chips while the player is typing an `@` or `/` query,
 * otherwise the suggestions (when [showSuggestions]). It is never both, so the input stays one
 * row tall plus this.
 */
@Composable
private fun InputRail(
    queryItemsType: ItemsType?,
    suggestions: List<Suggestion>,
    showSuggestions: Boolean,
    genre: Genre,
    accent: Color,
    onSuggestionClick: (Suggestion) -> Unit,
    onCharacterClick: (Character) -> Unit,
    onWikiClick: (Wiki) -> Unit,
) {
    AnimatedVisibility(queryItemsType != null || (showSuggestions && suggestions.isNotEmpty())) {
        if (queryItemsType != null) {
            LazyRow(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentPadding = PaddingValues(horizontal = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when (queryItemsType) {
                    is ItemsType.Characters -> {
                        items(queryItemsType.filteredCharacters) { character ->
                            val col = character.hexColor.hexToColor() ?: accent
                            MentionChip(col, onClick = { onCharacterClick(character) }) {
                                CharacterAvatar(
                                    character,
                                    genre = genre,
                                    modifier = Modifier.size(20.dp),
                                    grainRadius = 0f,
                                    pixelation = 0f,
                                    innerPadding = 1.dp,
                                )
                                Text(
                                    character.name,
                                    style =
                                        MaterialTheme.typography.labelSmall.copy(
                                            color = col,
                                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                        ),
                                )
                            }
                        }
                    }

                    is ItemsType.Wikis -> {
                        items(queryItemsType.filteredWikis) { wiki ->
                            MentionChip(accent, onClick = { onWikiClick(wiki) }) {
                                Text(
                                    wiki.emojiTag ?: "📖",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                                Text(
                                    wiki.title,
                                    style =
                                        MaterialTheme.typography.labelSmall.copy(
                                            color = accent,
                                            fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                        ),
                                )
                            }
                        }
                    }
                }
            }
        } else {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val chipWidth = maxWidth * SUGGESTION_WIDTH_FRACTION
                LazyRow(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(suggestions) { suggestion ->
                        GlassChip(
                            text = remember(suggestion.text) { buildSuggestionAnnotatedString(suggestion.text) },
                            onClick = { onSuggestionClick(suggestion) },
                            modifier = Modifier.widthIn(max = chipWidth),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MentionChip(
    color: Color,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        Modifier
            .border(1.dp, color.copy(alpha = .3f), CircleShape)
            .background(color.copy(alpha = .1f), CircleShape)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )
}

/**
 * The speech mode at the end of the field. With the keyboard down it is a chip that names the mode,
 * so a new player can tell what the icon means; with the keyboard up it shrinks to the bare icon to
 * leave the room to the text. The icon swaps to the tag's own, so it needs no color to tell modes
 * apart: it takes the content color of the glass it sits on and stays legible in any theme.
 */
@Composable
private fun SpeechModeButton(
    mode: SenderType,
    isInsideTag: Boolean,
    showLabel: Boolean,
    expanded: Boolean,
    options: List<Pair<SenderType, ExpressiveTag?>>,
    onExpandedChange: (Boolean) -> Unit,
    onOptionSelected: (ExpressiveTag?) -> Unit,
) {
    IosStyleMenuHost(
        expanded = expanded,
        onDismissRequest = { onExpandedChange(false) },
        anchor = Alignment.BottomEnd,
        menu = {
            options.forEach { (senderType, expressiveTag) ->
                val enabled = expressiveTag == null || !isInsideTag
                senderType.icon()?.let { iconRes ->
                    IosStyleMenuItem(
                        text = senderType.title(),
                        icon = painterResource(iconRes),
                        enabled = enabled,
                        onClick = {
                            onExpandedChange(false)
                            onOptionSelected(expressiveTag)
                        },
                    )
                }
            }
        },
    ) {
        val contentColor = MaterialTheme.colorScheme.onBackground
        val chipFill by animateColorAsState(
            if (showLabel) contentColor.copy(alpha = 0.1f) else Color.Transparent,
            label = "speechModeChipFill",
        )
        // The chip is shorter than the field, so it sits as far from the edge as it does from top and bottom.
        val edgeInset by animateDpAsState(if (showLabel) 8.dp else 0.dp, label = "speechModeEdgeInset")
        Box(
            Modifier
                .padding(end = edgeInset)
                .defaultMinSize(minWidth = ChatInputCell, minHeight = ChatInputCell)
                .clip(CircleShape)
                .clickable(role = Role.Button) { onExpandedChange(true) },
            contentAlignment = Alignment.Center,
        ) {
            Row(
                Modifier
                    .animateContentSize()
                    .height(ChatInputControlSize - 4.dp)
                    .clip(CircleShape)
                    .background(chipFill)
                    .padding(horizontal = if (showLabel) 10.dp else 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                mode.icon()?.let {
                    Icon(
                        painterResource(it),
                        contentDescription = mode.title(),
                        tint = contentColor.copy(alpha = if (isInsideTag) 1f else .72f),
                        modifier = Modifier.size(20.dp),
                    )
                }
                AnimatedVisibility(showLabel) {
                    Text(
                        mode.title(),
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor.copy(alpha = if (isInsideTag) 1f else .72f),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/** Send, live or stop, in one 36 dp circle inside a 48 dp touch target. */
@Composable
private fun SendButton(
    isLoading: Boolean,
    showsLive: Boolean,
    enabled: Boolean,
    progress: Float,
    onClick: () -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    Box(
        Modifier
            .size(ChatInputCell)
            .clip(CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(ChatInputControlSize)
                .dropShadow(CircleShape) {
                    color = primary
                    radius = 24f
                    spread = 0f
                    alpha = if (enabled) 0.22f else 0f
                }.background(primary.copy(alpha = if (enabled) 1f else 0.4f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(isLoading to showsLive) { (loading, live) ->
                Icon(
                    painterResource(
                        when {
                            loading -> R.drawable.ic_stop
                            live -> R.drawable.ic_mic
                            else -> R.drawable.ic_send
                        },
                    ),
                    contentDescription =
                        stringResource(if (live) R.string.live_open else R.string.chat_input_send),
                    tint = onPrimary.copy(alpha = if (enabled) 1f else 0.5f),
                    modifier = Modifier.size(18.dp),
                )
            }
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    color = onPrimary,
                    trackColor = Color.Transparent,
                    strokeWidth = 1.dp,
                )
            } else if (progress >= COUNTER_VISIBLE_FROM) {
                CircularProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxSize(),
                    color = onPrimary,
                    trackColor = Color.Transparent,
                    strokeWidth = 1.dp,
                )
            }
        }
    }
}
