@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.ilustris.sagai.features.saga.chat.ui.components
import android.graphics.Matrix
import android.graphics.Shader
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
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
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.data.model.TypoFix
import com.ilustris.sagai.features.saga.chat.data.model.TypoStatus
import com.ilustris.sagai.features.saga.chat.data.model.icon
import com.ilustris.sagai.features.saga.chat.data.model.senderForTag
import com.ilustris.sagai.features.saga.chat.data.model.title
import com.ilustris.sagai.features.saga.chat.domain.model.Suggestion
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.sagaShape
import com.ilustris.sagai.ui.theme.solidGradient
import com.ilustris.sagai.ui.theme.themeBrushColors

private val ChatInputTextMaxHeight = 160.dp

@Composable
private fun generatingBorderRotation(isGenerating: Boolean): Float {
    if (isGenerating) {
        val infiniteTransition = rememberInfiniteTransition(label = "border")
        val rotation by infiniteTransition.animateFloat(
            0f,
            360f,
            infiniteRepeatable(tween(3000, easing = LinearEasing)),
            label = "rotation",
        )
        return rotation
    }
    return 0f
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
    sendType: SenderType,
    typoFix: TypoFix?,
    selectedCharacter: Character? = null,
    isSendingPending: Boolean = false,
    @Suppress("UNUSED_PARAMETER") sendingProgress: Float = 0f,
    onUpdateInput: (TextFieldValue) -> Unit,
    onUpdateSender: (SenderType) -> Unit,
    onSendMessage: (Boolean) -> Unit,
    onSelectCharacter: (Character) -> Unit = {},
    onRequestAudio: () -> Unit = {},
    isEditing: Boolean = false,
    onCancelEdit: () -> Unit = {},
    maxContentLength: Int = 2000,
    onStopGeneration: () -> Unit = {},
) {
    var characterMenu by remember { mutableStateOf(false) }
    var speechModeSheet by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(inputField.text.length) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    val actualCharacter = selectedCharacter ?: content.mainCharacter
    val genre = content.data.genre
    val resolvedColor = MaterialTheme.colorScheme.primary
    val resolvedIconColor = MaterialTheme.colorScheme.onPrimary
    val inputBrush =
        Brush.horizontalGradient(
            if (isGenerating) morphingGradient() else themeBrushColors(),
        )
    val textStyle =
        MaterialTheme.typography.labelMedium.copy(
            color = MaterialTheme.colorScheme.onBackground,
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
    val glowRadiusState =
        animateFloatAsState(if (isGenerating.not()) 10f else 25f, label = "glowRadius")
    val inputShape = sagaShape()
    val palette = themeBrushColors()
    val rotation = generatingBorderRotation(isGenerating)

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
        AnimatedVisibility(suggestions.isNotEmpty() && isImeVisible) {
            LazyRow(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .heightIn(max = 60.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(suggestions) {
                    Button(
                        onClick = {
                            onUpdateInput(
                                TextFieldValue(
                                    it.text,
                                    TextRange(it.text.length),
                                ),
                            )
                            onUpdateSender(it.type)
                        },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                contentColor = resolvedColor,
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            ),
                    ) {
                        Icon(
                            painterResource(genre.icon),
                            null,
                            modifier =
                                Modifier
                                    .padding(4.dp)
                                    .size(12.dp),
                        )
                        Text(
                            remember(it.text) { buildSuggestionAnnotatedString(it.text) },
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }
        val currentTagInside =
            remember(inputField.text, inputField.selection) {
                getCursorInsideTag(
                    inputField.text,
                    inputField.selection.start,
                )
            }
        val bubbleColorState =
            animateColorAsState(
                if (currentTagInside != null) resolvedColor else MaterialTheme.colorScheme.background,
                label = "bubbleColor",
            )
        Column(
            modifier =
                Modifier
                    .padding(16.dp)
                    .dropShadow(inputShape, {
                        brush = inputBrush
                        radius = glowRadiusState.value
                        spread = 10f
                    })
                    .fillMaxWidth()
                    .clip(inputShape)
                    .drawWithContent {
                        drawContent()
                        val outline = inputShape.createOutline(size, layoutDirection, this)
                        if (isGenerating) {
                            val brush =
                                object : ShaderBrush() {
                                    override fun createShader(size: Size): Shader {
                                        val shader =
                                            (sweepGradient(palette) as ShaderBrush).createShader(
                                                size,
                                            )
                                        val matrix = Matrix()
                                        matrix.setRotate(
                                            rotation,
                                            size.width / 2,
                                            size.height / 2,
                                        )
                                        shader.setLocalMatrix(matrix)
                                        return shader
                                    }
                                }
                            drawOutline(outline, brush, style = Stroke(1.dp.toPx()))
                        } else {
                            drawOutline(outline, inputBrush, style = Stroke(1.dp.toPx()))
                        }
                    }.border(1.dp, inputBrush, inputShape)
                    .background(bubbleColorState.value, inputShape),
        ) {
            AnimatedVisibility(currentTagInside != null) {
                currentTagInside?.let { tag ->
                    SenderType.senderForTag(tag)?.let { senderType ->
                        Row(
                            Modifier
                                .alpha(.7f)
                                .padding(8.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            senderType.icon()?.let {
                                Icon(
                                    painterResource(it),
                                    null,
                                    modifier = Modifier.size(12.dp),
                                    tint = resolvedIconColor,
                                )
                            }
                            Text(
                                stringResource(R.string.tag_inside_hint, senderType.title()),
                                style =
                                    MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = resolvedIconColor,
                                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                    ),
                            )
                        }
                    }
                }
            }

            Column(
                Modifier
                    .padding(4.dp)
                    .clip(inputShape)
                    .background(
                        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = .5f),
                        inputShape,
                    ).fillMaxWidth()
                    .padding(8.dp),
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
                    decorationBox = { inner ->
                        Box(
                            Modifier.padding(8.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            Box {
                                inner()
                                if (inputField.text.isEmpty()) {
                                    Text(
                                        sendType.hint(),
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
                    keyboardOptions = KeyboardOptions(imeAction = if (currentTagInside != null) ImeAction.Next else ImeAction.Default),
                    keyboardActions =
                        KeyboardActions(onNext = {
                            if (currentTagInside != null) {
                                onUpdateInput(escapeCursorFromTagAndClean(inputField))
                            }
                        }),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 40.dp, max = ChatInputTextMaxHeight)
                            .verticalScroll(scrollState),
                )

                val activeSpeechMode =
                    currentTagInside?.let { SenderType.senderForTag(it) } ?: SenderType.CHARACTER
                val isLoading = isSendingPending || isGenerating
                val cleanLength =
                    remember(inputField.text) {
                        getCleanTextLength(inputField.text)
                    }
                val progress = cleanLength.toFloat() / maxContentLength

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AnimatedContent(
                        targetState =
                            actualCharacter?.let { it.id to it.image }
                                ?: (content.mainCharacter?.id to content.mainCharacter?.image),
                        modifier =
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable { characterMenu = true },
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

                    val speechModeChipShape = MaterialTheme.shapes.extraLarge
                    val isInsideTag = currentTagInside != null
                    Row(
                        modifier =
                            Modifier
                                .clip(speechModeChipShape)
                                .background(
                                    if (isInsideTag) {
                                        resolvedColor.copy(alpha = .2f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainer
                                    },
                                ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .clickable { speechModeSheet = true }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            activeSpeechMode.icon()?.let {
                                Icon(
                                    painterResource(it),
                                    contentDescription = null,
                                    tint = resolvedColor,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            Text(
                                activeSpeechMode.title(),
                                style =
                                    MaterialTheme.typography.labelSmall,
                            )
                        }
                        AnimatedVisibility(
                            visible = isInsideTag,
                            enter =
                                expandHorizontally(expandFrom = Alignment.Start) +
                                    fadeIn(
                                        tween(
                                            200,
                                        ),
                                    ),
                            exit =
                                shrinkHorizontally(shrinkTowards = Alignment.Start) +
                                    fadeOut(
                                        tween(150),
                                    ),
                        ) {
                            Row(
                                modifier =
                                    Modifier
                                        .background(
                                            MaterialTheme.colorScheme.background.copy(alpha = .2f),
                                            speechModeChipShape,
                                        ).clickable {
                                            onUpdateInput(
                                                escapeCursorFromTagAndClean(inputField),
                                            )
                                        }.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    stringResource(R.string.next),
                                    style =
                                        MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                )
                            }
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    val canSend = inputField.text.isNotBlank() || isGenerating

                    Box(contentAlignment = Alignment.Center) {
                        IconButton(
                            onClick = {
                                if (isLoading) {
                                    onStopGeneration()
                                } else {
                                    sendMessage()
                                }
                            },
                            enabled = canSend,
                            colors =
                                IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    disabledContainerColor =
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    disabledContentColor =
                                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                                ),
                            modifier =
                                Modifier
                                    .padding(4.dp)
                                    .size(32.dp),
                        ) {
                            AnimatedContent(isLoading) { loading ->
                                Icon(
                                    painterResource(
                                        if (loading) R.drawable.ic_stop else R.drawable.ic_send,
                                    ),
                                    contentDescription = stringResource(R.string.chat_input_send),
                                    modifier =
                                        Modifier
                                            .padding(8.dp)
                                            .fillMaxSize(),
                                )
                            }
                        }

                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                trackColor = Color.Transparent,
                                strokeWidth = 1.dp,
                            )
                        } else if (inputField.text.isNotEmpty()) {
                            CircularProgressIndicator(
                                progress = { progress.coerceIn(0f, 1f) },
                                modifier = Modifier.size(32.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                trackColor = Color.Transparent,
                                strokeWidth = 1.dp,
                            )
                        }
                    }
                }
            }
            AnimatedVisibility(queryItemsType != null) {
                queryItemsType?.let { itemsType ->
                    LazyRow(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                    ) {
                        when (itemsType) {
                            is ItemsType.Characters -> {
                                items(itemsType.filteredCharacters) { character ->
                                    val col = character.hexColor.hexToColor() ?: resolvedColor
                                    Row(
                                        Modifier
                                            .border(1.dp, col.copy(alpha = .3f), CircleShape)
                                            .background(col.copy(alpha = .1f), CircleShape)
                                            .clip(CircleShape)
                                            .clickable {
                                                handleCharacterSelection(
                                                    character,
                                                    inputField,
                                                    onUpdateInput,
                                                )
                                            }.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
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
                                items(itemsType.filteredWikis) { wiki ->
                                    Row(
                                        Modifier
                                            .border(
                                                1.dp,
                                                resolvedColor.copy(alpha = .3f),
                                                CircleShape,
                                            ).background(
                                                resolvedColor.copy(alpha = .1f),
                                                CircleShape,
                                            ).clip(CircleShape)
                                            .clickable {
                                                handleWikiSelection(
                                                    wiki,
                                                    inputField,
                                                    onUpdateInput,
                                                )
                                            }.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Text(
                                            wiki.emojiTag ?: "📖",
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                        Text(
                                            wiki.title,
                                            style =
                                                MaterialTheme.typography.labelSmall.copy(
                                                    color = resolvedColor,
                                                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                                ),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
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

        if (speechModeSheet) {
            SpeechModeSheet(
                activeTag = currentTagInside,
                accentColor = resolvedColor,
                canInsertTag = currentTagInside == null,
                onSelectSpeak = {
                    if (currentTagInside != null) {
                        onUpdateInput(escapeCursorFromTagAndClean(inputField))
                    }
                },
                onSelectTag = { tag ->
                    onUpdateInput(insertExpressiveTag(inputField, tag))
                },
                onDismiss = { speechModeSheet = false },
            )
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
