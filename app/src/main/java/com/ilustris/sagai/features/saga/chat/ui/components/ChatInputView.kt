@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.ilustris.sagai.features.saga.chat.ui.components

import android.graphics.Matrix
import android.graphics.Shader
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.newsaga.data.model.resolveIconColor
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.data.model.TypoFix
import com.ilustris.sagai.features.saga.chat.data.model.TypoStatus
import com.ilustris.sagai.features.saga.chat.data.model.filterUserInputTypes
import com.ilustris.sagai.features.saga.chat.data.model.icon
import com.ilustris.sagai.features.saga.chat.data.model.senderForTag
import com.ilustris.sagai.features.saga.chat.data.model.tag
import com.ilustris.sagai.features.saga.chat.data.model.title
import com.ilustris.sagai.features.saga.chat.domain.model.Suggestion
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shape
import com.ilustris.sagai.ui.theme.solidGradient
import kotlin.time.Duration.Companion.seconds

private fun detectQueryType(
    text: String,
    characters: List<Character>,
    wikis: List<Wiki>,
): ItemsType? {
    val lastAtIndex = text.lastIndexOf('@')
    val lastSlashIndex = text.lastIndexOf('/')
    val isCharacterQuery = lastAtIndex != -1 && lastAtIndex > lastSlashIndex
    val isWikiQuery = lastSlashIndex != -1 && lastSlashIndex > lastAtIndex
    return when {
        isCharacterQuery && lastAtIndex < text.length -> {
            val query = text.substring(lastAtIndex + 1)
            if (!query.contains(' ')) {
                val filtered = characters.filter { it.name.contains(query, ignoreCase = true) }
                if (filtered.isNotEmpty()) {
                    ItemsType.Characters(
                        filtered,
                        query,
                    )
                } else {
                    null
                }
            } else {
                null
            }
        }

        isWikiQuery && lastSlashIndex < text.length -> {
            val query = text.substring(lastSlashIndex + 1)
            if (!query.contains(' ')) {
                val filtered = wikis.filter { it.title.contains(query, ignoreCase = true) }
                if (filtered.isNotEmpty()) {
                    ItemsType.Wikis(
                        filtered,
                        query,
                    )
                } else {
                    null
                }
            } else {
                null
            }
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
): String {
    val startIndex = text.lastIndexOf(symbol)
    return text.replaceRange(startIndex, text.length, "$replacement ")
}

private fun handleCharacterSelection(
    character: Character,
    currentInput: TextFieldValue,
    onUpdateInput: (TextFieldValue) -> Unit,
) {
    val newText = replaceQueryInText(currentInput.text, '@', character.name)
    onUpdateInput(TextFieldValue(newText, TextRange(newText.length)))
}

private fun handleWikiSelection(
    wiki: Wiki,
    currentInput: TextFieldValue,
    onUpdateInput: (TextFieldValue) -> Unit,
) {
    val newText = replaceQueryInText(currentInput.text, '/', wiki.title)
    onUpdateInput(TextFieldValue(newText, TextRange(newText.length)))
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
    var focusModeEnabled by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(inputField.text) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    val actualCharacter =
        characters.find { it.id == selectedCharacter?.id } ?: content.mainCharacter
    val genre = content.data.genre
    val resolvedColor = genre.resolveColor()
    val resolvedIconColor = genre.resolveIconColor()
    val inputBrush = genre.gradient(isGenerating, duration = 2.seconds)
    var queryItemsType by remember { mutableStateOf<ItemsType?>(null) }
    val textStyle =
        MaterialTheme.typography.labelMedium.copy(
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = genre.bodyFont(),
        )
    val tagBg = MaterialTheme.colorScheme.background
    val textColor = textStyle.color
    LaunchedEffect(inputField.text, characters, content.wikis) {
        queryItemsType = detectQueryType(inputField.text, characters, content.wikis)
    }
    val glowRadiusState =
        animateFloatAsState(if (isGenerating.not()) 10f else 25f, label = "glowRadius")
    val inputShape = genre.shape()
    val palette = genre.colorPalette()
    val infiniteTransition = rememberInfiniteTransition(label = "border")
    val rotationState =
        infiniteTransition.animateFloat(
            0f,
            360f,
            infiniteRepeatable(tween(3000, easing = LinearEasing)),
            label = "rotation",
        )

    val visualTransformation =
        remember(
            genre,
            content.mainCharacter,
            characters,
            content.wikis,
            resolvedColor,
            tagBg,
            textColor,
        ) {
            VisualTransformation { text ->
                transformTextWithContent(
                    genre = genre,
                    mainCharacter = content.mainCharacter,
                    characters = characters,
                    wiki = content.wikis,
                    text = text.text,
                    genreColor = resolvedColor,
                    tagBackgroundColor = tagBg,
                    textColor = textColor,
                )
            }
        }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun sendMessage(confirmed: Boolean = false) {
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
                    fontFamily = genre.bodyFont(),
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
                    .animateContentSize()
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
                                            rotationState.value,
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
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
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
                                            fontFamily = genre.bodyFont(),
                                        ),
                                )
                            }
                            Text(
                                stringResource(R.string.next),
                                style =
                                    MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = genre.bodyFont(),
                                        fontWeight = FontWeight.Bold,
                                        color = resolvedIconColor,
                                    ),
                                modifier =
                                    Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.background)
                                        .clickable {
                                            onUpdateInput(
                                                escapeCursorFromTagAndClean(
                                                    inputField,
                                                ),
                                            )
                                        }.padding(8.dp),
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
                    .heightIn(max = 400.dp)
                    .padding(8.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                ) {
                    var characterMenu by remember { mutableStateOf(false) }

                    AnimatedContent(
                        actualCharacter,
                        modifier =
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable { characterMenu = true },
                    ) {
                        Box {
                            val character = it ?: content.mainCharacter
                            character?.let {
                                CharacterAvatar(
                                    it,
                                    genre = genre,
                                    grainRadius = 0f,
                                    pixelation = 0f,
                                    useFallback = true,
                                    modifier = Modifier.fillMaxSize(),
                                    borderSize = 1.dp,
                                )
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
                                            fontFamily = genre.bodyFont(),
                                            textAlign = TextAlign.Center,
                                            modifier =
                                                Modifier
                                                    .padding(16.dp)
                                                    .fillMaxWidth(),
                                        )
                                        LazyVerticalGrid(
                                            columns =
                                                GridCells.Adaptive(
                                                    100.dp,
                                                ),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(16.dp),
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(bottom = 32.dp),
                                        ) {
                                            items(characters.size) { index ->
                                                val character = characters[index]
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier =
                                                        Modifier
                                                            .clip(MaterialTheme.shapes.medium)
                                                            .clickable {
                                                                onSelectCharacter(character)
                                                                characterMenu = false
                                                            }.padding(8.dp),
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
                                                        fontFamily = genre.bodyFont(),
                                                        modifier = Modifier.padding(top = 8.dp),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    BasicTextField(
                        inputField,
                        enabled = !isGenerating,
                        maxLines = if (!isImeVisible) 1 else Int.MAX_VALUE,
                        onValueChange = { newValue ->
                            if (newValue.text.length == inputField.text.length - 1 && handleSmartBackspace(
                                    inputField,
                                ) != null
                            ) {
                                handleSmartBackspace(inputField)?.let {
                                    onUpdateInput(it)
                                    return@BasicTextField
                                }
                            }
                            if (getCleanTextLength(newValue.text) <= maxContentLength) {
                                onUpdateInput(
                                    newValue,
                                )
                            }
                        },
                        textStyle = textStyle,
                        visualTransformation = visualTransformation,
                        cursorBrush = resolvedColor.solidGradient(),
                        decorationBox = { inner ->
                            Box(
                                Modifier
                                    .padding(8.dp)
                                    .reactiveShimmer(isGenerating),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                Box {
                                    inner()
                                    if (inputField.text.isEmpty()) {
                                        Text(
                                            sendType.hint(),
                                            style = textStyle,
                                            modifier = Modifier.alpha(.4f),
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
                                .weight(1f)
                                .heightIn(min = 40.dp)
                                .verticalScroll(scrollState),
                    )

                    AnimatedContent(inputField.text.isEmpty()) {
                        if (it && !isGenerating) {
                            IconButton(onClick = {
                                focusModeEnabled = true
                            }, modifier = Modifier.size(24.dp)) {
                                Icon(
                                    painterResource(R.drawable.ic_expand),
                                    stringResource(R.string.chat_input_expand),
                                    modifier =
                                        Modifier
                                            .padding(4.dp)
                                            .fillMaxSize(),
                                    tint = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                val iconBg by animateColorAsState(
                                    if (isGenerating ||
                                        inputField.text.isEmpty()
                                    ) {
                                        Color.Transparent
                                    } else {
                                        resolvedColor
                                    },
                                )
                                val tint by animateColorAsState(if (isGenerating) resolvedColor else resolvedIconColor)

                                val isLoading = isSendingPending || isGenerating
                                val cleanLength = getCleanTextLength(inputField.text)
                                val progress = cleanLength.toFloat() / maxContentLength

                                IconButton(
                                    onClick = {
                                        if (isLoading) {
                                            onStopGeneration()
                                        } else {
                                            sendMessage()
                                            onUpdateInput(
                                                escapeCursorFromTagAndClean(
                                                    inputField,
                                                ),
                                            )
                                        }
                                    },
                                    enabled = (inputField.text.isNotBlank() || isGenerating),
                                    colors =
                                        IconButtonDefaults.filledIconButtonColors(
                                            containerColor = iconBg,
                                            contentColor = tint,
                                        ),
                                    modifier =
                                        Modifier
                                            .padding(4.dp)
                                            .size(32.dp),
                                ) {
                                    AnimatedContent(isLoading) { loading ->
                                        val icon =
                                            if (loading) {
                                                R.drawable.ic_stop
                                            } else {
                                                if (inputField.text.isNotEmpty()) R.drawable.ic_send else null
                                            }
                                        icon?.let {
                                            Icon(
                                                painterResource(it),
                                                null,
                                                modifier =
                                                    Modifier
                                                        .padding(8.dp)
                                                        .fillMaxSize(),
                                            )
                                        }
                                    }
                                }

                                if (isLoading || inputField.text.isNotEmpty()) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(32.dp),
                                            color = resolvedColor,
                                            trackColor = Color.Transparent,
                                            strokeWidth = 1.dp,
                                        )
                                    } else {
                                        CircularProgressIndicator(
                                            progress = { progress.coerceIn(0f, 1f) },
                                            modifier = Modifier.size(32.dp),
                                            color = MaterialTheme.colorScheme.onBackground,
                                            trackColor = Color.Transparent,
                                            strokeWidth = 1.dp,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(isImeVisible) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SenderType
                            .filterUserInputTypes()
                            .filter { it.icon() != null }
                            .forEach { type ->
                                val sel = currentTagInside == type.tag
                                val col by animateColorAsState(
                                    if (sel) {
                                        resolvedIconColor
                                    } else {
                                        MaterialTheme.colorScheme.onBackground.copy(
                                            alpha = .5f,
                                        )
                                    },
                                )

                                type.icon()?.let {
                                    Icon(
                                        painterResource(it),
                                        null,
                                        tint = col,
                                        modifier =
                                            Modifier
                                                .border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.onBackground.copy(
                                                        alpha = .1f,
                                                    ),
                                                    CircleShape,
                                                ).clip(CircleShape)
                                                .clickable(currentTagInside == null) {
                                                    type.tag?.let {
                                                        onUpdateInput(
                                                            insertExpressiveTag(
                                                                inputField,
                                                                it,
                                                            ),
                                                        )
                                                    }
                                                }.size(24.dp)
                                                .padding(4.dp),
                                    )
                                }
                            }
                        Box {
                            var menu by remember { mutableStateOf(false) }
                            Icon(
                                painterResource(R.drawable.ic_menu),
                                null,
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier =
                                    Modifier
                                        .padding(8.dp)
                                        .clip(CircleShape)
                                        .size(24.dp)
                                        .clickable { menu = true },
                            )
                            DropdownMenu(menu, { menu = false }) {
                                DropdownMenuItem(
                                    { Text(stringResource(R.string.chat_input_mention_character)) },
                                    {
                                        menu = false
                                        onUpdateInput(
                                            TextFieldValue(
                                                inputField.text + "@",
                                                TextRange(inputField.text.length + 1),
                                            ),
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painterResource(R.drawable.ic_mail),
                                            null,
                                            tint = resolvedColor,
                                            modifier = Modifier.size(24.dp),
                                        )
                                    },
                                )
                                DropdownMenuItem(
                                    { Text(stringResource(R.string.chat_input_mention_wiki)) },
                                    {
                                        menu = false
                                        onUpdateInput(
                                            TextFieldValue(
                                                inputField.text + "/",
                                                TextRange(inputField.text.length + 1),
                                            ),
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painterResource(R.drawable.ic_slash),
                                            null,
                                            tint = resolvedColor,
                                            modifier = Modifier.size(24.dp),
                                        )
                                    },
                                )
                            }
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
                                        )
                                        Text(
                                            character.name,
                                            style =
                                                MaterialTheme.typography.labelSmall.copy(
                                                    color = col,
                                                    fontFamily = genre.bodyFont(),
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
                                                    fontFamily = genre.bodyFont(),
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
        if (focusModeEnabled) {
            ModalBottomSheet({ focusModeEnabled = false }) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .animateContentSize(),
                ) {
                    val isLoading = isSendingPending || isGenerating

                    Row(
                        Modifier.fillMaxWidth(),
                        Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton({
                            focusModeEnabled = false
                        }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                painterResource(R.drawable.ic_back_left),
                                null,
                                modifier =
                                    Modifier
                                        .padding(4.dp)
                                        .fillMaxSize(),
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }

                        Text(
                            stringResource(R.string.chat_input_focus_title),
                            style =
                                MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = genre.bodyFont(),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                ),
                            modifier = Modifier.weight(1f),
                        )

                        Box(Modifier.size(24.dp))
                    }

                    HorizontalDivider(
                        modifier =
                            Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth(),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = .1f),
                    )

                    AnimatedVisibility(
                        currentTagInside != null,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    ) {
                        currentTagInside?.let { tag ->
                            SenderType.senderForTag(tag)?.let { senderType ->

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier =
                                        Modifier
                                            .clip(
                                                MaterialTheme.shapes.extraLarge,
                                            ).background(
                                                resolvedColor,
                                                MaterialTheme.shapes.extraLarge,
                                            ).clickable {
                                                onUpdateInput(
                                                    escapeCursorFromTagAndClean(
                                                        inputField,
                                                    ),
                                                )
                                            }.padding(8.dp),
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
                                        stringResource(
                                            R.string.tag_inside_hint,
                                            senderType.title(),
                                        ),
                                        style =
                                            MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = resolvedIconColor,
                                                fontFamily = genre.bodyFont(),
                                            ),
                                    )
                                }
                            }
                        }
                    }

                    Box(Modifier.weight(1f), contentAlignment = Alignment.TopStart) {
                        BasicTextField(
                            inputField,
                            enabled = !isGenerating,
                            maxLines = if (!isImeVisible) 1 else Int.MAX_VALUE,
                            onValueChange = { newValue ->
                                if (newValue.text.length == inputField.text.length - 1 && handleSmartBackspace(
                                        inputField,
                                    ) != null
                                ) {
                                    handleSmartBackspace(inputField)?.let {
                                        onUpdateInput(it)
                                        return@BasicTextField
                                    }
                                }
                                if (getCleanTextLength(newValue.text) <= maxContentLength) {
                                    onUpdateInput(
                                        newValue,
                                    )
                                }
                            },
                            textStyle =
                                textStyle.copy(
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                ),
                            visualTransformation = {
                                transformTextWithContent(
                                    genre,
                                    content.mainCharacter,
                                    content.characters,
                                    content.wikis,
                                    inputField.text,
                                    resolvedColor,
                                    tagBg,
                                    textColor,
                                )
                            },
                            cursorBrush = resolvedColor.solidGradient(),
                            decorationBox = { inner ->
                                val alpha by animateFloatAsState(if (inputField.text.isEmpty()) .5f else 1f)
                                Column(
                                    Modifier
                                        .fillMaxSize()
                                        .alpha(alpha)
                                        .padding(8.dp)
                                        .reactiveShimmer(isGenerating),
                                ) {
                                    inner()
                                    if (inputField.text.isEmpty()) {
                                        Text(
                                            sendType.hint(),
                                            style = textStyle,
                                            modifier = Modifier.alpha(.4f),
                                        )
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
                                                            val col =
                                                                character.hexColor.hexToColor()
                                                                    ?: resolvedColor
                                                            Row(
                                                                Modifier
                                                                    .border(
                                                                        1.dp,
                                                                        col.copy(alpha = .3f),
                                                                        CircleShape,
                                                                    ).background(
                                                                        col.copy(alpha = .1f),
                                                                        CircleShape,
                                                                    ).clip(CircleShape)
                                                                    .clickable {
                                                                        handleCharacterSelection(
                                                                            character,
                                                                            inputField,
                                                                            onUpdateInput,
                                                                        )
                                                                    }.padding(
                                                                        horizontal = 12.dp,
                                                                        vertical = 6.dp,
                                                                    ),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement =
                                                                    Arrangement.spacedBy(
                                                                        8.dp,
                                                                    ),
                                                            ) {
                                                                CharacterAvatar(
                                                                    character,
                                                                    genre = genre,
                                                                    modifier = Modifier.size(20.dp),
                                                                    grainRadius = 0f,
                                                                    pixelation = 0f,
                                                                )
                                                                Text(
                                                                    character.name,
                                                                    style =
                                                                        MaterialTheme.typography.labelSmall.copy(
                                                                            color = col,
                                                                            fontFamily = genre.bodyFont(),
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
                                                                    }.padding(
                                                                        horizontal = 12.dp,
                                                                        vertical = 6.dp,
                                                                    ),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement =
                                                                    Arrangement.spacedBy(
                                                                        8.dp,
                                                                    ),
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
                                                                            fontFamily = genre.bodyFont(),
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
                            },
                            keyboardOptions =
                                KeyboardOptions(
                                    imeAction =
                                        if (currentTagInside !=
                                            null
                                        ) {
                                            ImeAction.Next
                                        } else {
                                            ImeAction.Default
                                        },
                                ),
                            keyboardActions =
                                KeyboardActions(onNext = {
                                    if (currentTagInside != null) {
                                        onUpdateInput(escapeCursorFromTagAndClean(inputField))
                                    }
                                }),
                            modifier =
                                Modifier.fillMaxWidth(),
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            SenderType
                                .filterUserInputTypes()
                                .filter { it.icon() != null }
                                .forEach { type ->
                                    val sel = currentTagInside == type.tag
                                    val col by animateColorAsState(
                                        if (sel) {
                                            resolvedIconColor
                                        } else {
                                            MaterialTheme.colorScheme.background.copy(
                                                alpha = .5f,
                                            )
                                        },
                                    )

                                    type.icon()?.let {
                                        Icon(
                                            painterResource(it),
                                            null,
                                            tint = col,
                                            modifier =
                                                Modifier
                                                    .clip(CircleShape)
                                                    .size(24.dp)
                                                    .background(
                                                        resolvedColor.copy(alpha = .3f),
                                                        shape = MaterialTheme.shapes.extraLarge,
                                                    ).padding(4.dp)
                                                    .clickable(currentTagInside == null) {
                                                        type.tag?.let {
                                                            onUpdateInput(
                                                                insertExpressiveTag(
                                                                    inputField,
                                                                    it,
                                                                ),
                                                            )
                                                        }
                                                    },
                                        )
                                    }
                                }

                            Icon(
                                painterResource(R.drawable.ic_mail),
                                null,
                                tint = resolvedIconColor,
                                modifier =
                                    Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            resolvedColor.copy(alpha = .3f),
                                            shape = MaterialTheme.shapes.extraLarge,
                                        ).padding(4.dp)
                                        .clickable {
                                            onUpdateInput(
                                                TextFieldValue(
                                                    inputField.text + " @",
                                                    TextRange(inputField.text.length + 1),
                                                ),
                                            )
                                        },
                            )

                            Icon(
                                painterResource(R.drawable.ic_slash),
                                null,
                                tint = resolvedIconColor,
                                modifier =
                                    Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            resolvedColor.copy(alpha = .3f),
                                            shape = CircleShape,
                                        ).padding(4.dp)
                                        .clickable {
                                            onUpdateInput(
                                                TextFieldValue(
                                                    inputField.text + "/",
                                                    TextRange(inputField.text.length + 1),
                                                ),
                                            )
                                        },
                            )
                        }

                        Spacer(Modifier.weight(1f))

                        Button(
                            {
                                onSendMessage(false)
                                focusModeEnabled = false
                            },
                            colors =
                                ButtonDefaults.buttonColors().copy(
                                    resolvedColor,
                                    resolvedIconColor,
                                ),
                            enabled = inputField.text.isNotEmpty() && isLoading.not(),
                            modifier = Modifier.padding(16.dp),
                        ) {
                            Text(
                                stringResource(R.string.chat_input_send),
                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = genre.bodyFont()),
                            )
                        }
                    }
                }
            }
        }
    }
}
