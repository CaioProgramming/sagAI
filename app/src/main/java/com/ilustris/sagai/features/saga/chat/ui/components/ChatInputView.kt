@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.ilustris.sagai.features.saga.chat.ui.components

import android.graphics.Matrix
import android.graphics.Shader
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults.rememberTooltipPositionProvider
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.characters.ui.CharacterYearbookItem
import com.ilustris.sagai.features.characters.ui.components.transformTextWithContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.data.model.TypoFix
import com.ilustris.sagai.features.saga.chat.data.model.TypoStatus
import com.ilustris.sagai.features.saga.chat.domain.model.Suggestion
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.ui.theme.GradientType
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.chat.BubbleTailAlignment
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shape
import com.ilustris.sagai.ui.theme.solidGradient
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

/**
 * Detects if user is querying for characters (@) or wikis (/) and returns the appropriate ItemsType.
 * @param text The input text to analyze
 * @param characters The list of available characters
 * @param wikis The list of available wikis
 * @param context The context for accessing string resources (for future i18n support)
 * @return ItemsType.Characters if @ query detected, ItemsType.Wikis if / query detected, null otherwise
 */
private fun detectQueryType(
    text: String,
    characters: List<CharacterContent>,
    wikis: List<Wiki>,
    context: android.content.Context? = null,
): ItemsType? {
    val lastAtIndex = text.lastIndexOf('@')
    val lastSlashIndex = text.lastIndexOf('/')

    // Determine which symbol was typed last
    val isCharacterQuery = lastAtIndex != -1 && lastAtIndex > lastSlashIndex
    val isWikiQuery = lastSlashIndex != -1 && lastSlashIndex > lastAtIndex

    return when {
        isCharacterQuery && lastAtIndex < text.length -> {
            val query = text.substring(lastAtIndex + 1)
            // Only show if user is still typing (no space after @)
            if (!query.contains(' ')) {
                val filtered =
                    characters.filter { character ->
                        character.data.name.contains(query, ignoreCase = true)
                    }
                if (filtered.isNotEmpty()) {
                    ItemsType.Characters(
                        filteredCharacters = filtered,
                        charactersTitle =
                            if (query.isEmpty()) {
                                "Mencionar personagem"
                            } else {
                                "Buscando \"$query\""
                            },
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
            // Only show if user is still typing (no space after /)
            if (!query.contains(' ')) {
                val filtered =
                    wikis.filter { wiki ->
                        wiki.title.contains(query, ignoreCase = true)
                    }
                if (filtered.isNotEmpty()) {
                    ItemsType.Wikis(
                        filteredWikis = filtered,
                        wikiTitle =
                            if (query.isEmpty()) {
                                "${wikis.size} Wiki items"
                            } else {
                                "Buscando \"$query\""
                            },
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

/**
 * Replaces a query symbol (@ or /) and the text after it with the replacement text.
 * @param text The original text
 * @param symbol The symbol to find (@ or /)
 * @param replacement The text to replace the query with (without the symbol)
 * @return The new text with the query replaced
 */
private fun replaceQueryInText(
    text: String,
    symbol: Char,
    replacement: String,
): String {
    val startIndex = text.lastIndexOf(symbol)
    val endIndex = text.length
    return text.replaceRange(startIndex, endIndex, "$replacement ")
}

/**
 * Handles character mention selection from the tooltip.
 * Replaces @query with the character name.
 */
private fun handleCharacterSelection(
    character: CharacterContent,
    currentInput: TextFieldValue,
    onUpdateInput: (TextFieldValue) -> Unit,
) {
    val newText = replaceQueryInText(currentInput.text, '@', character.data.name)
    onUpdateInput(
        TextFieldValue(
            newText,
            TextRange(newText.length),
        ),
    )
}

/**
 * Handles wiki selection from the tooltip.
 * Replaces /query with the wiki title.
 */
private fun handleWikiSelection(
    wiki: Wiki,
    currentInput: TextFieldValue,
    onUpdateInput: (TextFieldValue) -> Unit,
) {
    val newText = replaceQueryInText(currentInput.text, '/', wiki.title)
    onUpdateInput(
        TextFieldValue(
            newText,
            TextRange(newText.length),
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ChatInputView(
    content: SagaContent,
    isGenerating: Boolean,
    suggestions: List<Suggestion>,
    modifier: Modifier = Modifier,
    inputField: TextFieldValue,
    sendType: SenderType,
    typoFix: TypoFix?,
    selectedCharacter: CharacterContent? = null,
    onUpdateInput: (TextFieldValue) -> Unit,
    onUpdateSender: (SenderType) -> Unit,
    onSendMessage: (Boolean) -> Unit,
    onSelectCharacter: (CharacterContent) -> Unit = {},
    onRequestAudio: () -> Unit = {},
) {
    val context = LocalContext.current
    val action = sendType
    val inputBrush =
        content.data.genre.gradient(
            isGenerating,
            duration = 2.seconds,
        )

    var queryItemsType by remember { mutableStateOf<ItemsType?>(null) }

    val characterToolTipState =
        androidx.compose.material3.rememberTooltipState(
            isPersistent = true,
        )
    val tooltipPositionProvider =
        rememberTooltipPositionProvider(
            TooltipAnchorPosition.Above,
            spacingBetweenTooltipAndAnchor = 4.dp,
        )

    // Tooltip state for query items feature (both @ and /)
    val queryItemsTooltipState =
        androidx.compose.material3.rememberTooltipState(
            isPersistent = true,
        )
    val queryTooltipPositionProvider =
        rememberTooltipPositionProvider(
            TooltipAnchorPosition.Above,
            spacingBetweenTooltipAndAnchor = 4.dp,
        )

    LaunchedEffect(queryItemsType) {
        if (queryItemsType != null) {
            queryItemsTooltipState.show()
        } else {
            queryItemsTooltipState.dismiss()
        }
    }

    LaunchedEffect(inputField.text, content.characters, content.wikis) {
        queryItemsType =
            detectQueryType(
                text = inputField.text,
                characters = content.characters,
                wikis = content.wikis,
                context = context,
            )
    }

    val glowRadius by animateFloatAsState(
        if (isGenerating.not()) 10f else 25f,
    )
    val backgroundColor by animateColorAsState(
        if (isGenerating) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surfaceContainer,
    )
    val inputShape =
        remember {
            content.data.genre.bubble(
                BubbleTailAlignment.BottomLeft,
                tailWidth = 0.dp,
                tailHeight = 0.dp,
            )
        }

    val infiniteTransition = rememberInfiniteTransition(label = "border_animation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "rotation",
    )

    rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun sendMessage(confirmed: Boolean = false) {
        onSendMessage(confirmed)
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    Column(
        modifier
            .fillMaxWidth()
            .imePadding(),
    ) {
        val isImeVisible = WindowInsets.isImeVisible
        val suggestionsEnabled = suggestions.isNotEmpty() && isImeVisible
        val coroutineScope = rememberCoroutineScope()

        AnimatedVisibility(suggestionsEnabled) {
            LazyRow(
                modifier =
                    Modifier
                        .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
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
                        shape = inputShape,
                        modifier =
                            Modifier
                                .padding(horizontal = 16.dp)
                                .fillParentMaxWidth(.7f),
                        colors =
                            ButtonDefaults.outlinedButtonColors().copy(
                                contentColor = content.data.genre.color,
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            ),
                    ) {
                        Icon(
                            painterResource(it.type.icon()),
                            contentDescription = it.type.description(),
                            modifier =
                                Modifier
                                    .padding(4.dp)
                                    .size(12.dp)
                                    .reactiveShimmer(
                                        true,
                                        content.data.genre.color
                                            .darkerPalette()
                                            .plus(Color.Transparent),
                                        duration = 5.seconds,
                                    ),
                        )

                        Text(
                            it.text,
                            style = MaterialTheme.typography.labelSmall,
                            modifier =
                                Modifier.reactiveShimmer(
                                    true,
                                    duration = 5.seconds,
                                ),
                        )
                    }
                }
            }
        }

        Column(
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .heightIn(max = 250.dp)
                    .dropShadow(inputShape, {
                        brush = inputBrush
                        radius = glowRadius
                    })
                    .drawWithContent {
                        drawContent()
                        val outline = inputShape.createOutline(size, layoutDirection, this)
                        if (isGenerating) {
                            val brush =
                                object : ShaderBrush() {
                                    override fun createShader(size: Size): Shader {
                                        val shader =
                                            (
                                                sweepGradient(
                                                    content.data.genre.colorPalette(),
                                                ) as ShaderBrush
                                            ).createShader(size)
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
                            drawOutline(
                                outline = outline,
                                brush = brush,
                                style = Stroke(width = 1.dp.toPx()),
                            )
                        } else {
                            drawOutline(
                                outline = outline,
                                brush = inputBrush,
                                style = Stroke(width = 1.dp.toPx()),
                            )
                        }
                    }.border(1.dp, inputBrush, inputShape)
                    .background(backgroundColor, inputShape)
                    .verticalScroll(rememberScrollState()),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                val textStyle =
                    MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = content.data.genre.bodyFont(),
                    )
                val maxLength = 500
                val tagBackgroundColor = MaterialTheme.colorScheme.background

                TooltipBox(
                    positionProvider = tooltipPositionProvider,
                    state = characterToolTipState,
                    modifier =
                        Modifier
                            .size(36.dp),
                    onDismissRequest = {
                        coroutineScope.launch {
                            characterToolTipState.dismiss()
                        }
                    },
                    tooltip = {
                        val genre = content.data.genre
                        val shape =
                            genre.bubble(
                                BubbleTailAlignment.BottomRight,
                                0.dp,
                                0.dp,
                            )

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier =
                                Modifier
                                    .padding(16.dp)
                                    .dropShadow(
                                        shape,
                                        Shadow(
                                            radius = 5.dp,
                                            genre.color,
                                        ),
                                    ).border(1.dp, genre.color.gradientFade(), shape)
                                    .background(
                                        MaterialTheme.colorScheme.background,
                                        shape,
                                    ).clip(shape)
                                    .padding(8.dp),
                        ) {
                            item(span = { GridItemSpan(4) }) {
                                Text(
                                    "Selecionar personagem",
                                    style =
                                        MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = content.data.genre.bodyFont(),
                                            textAlign = TextAlign.Start,
                                        ),
                                    modifier = Modifier.padding(8.dp),
                                )
                            }

                            items(content.characters) {
                                CharacterYearbookItem(
                                    it.data,
                                    content.data.genre,
                                    useFallback = true,
                                    imageModifier =
                                        Modifier
                                            .clickable {
                                                onSelectCharacter(it)
                                                coroutineScope.launch {
                                                    characterToolTipState.dismiss()
                                                }
                                            }.size(36.dp),
                                    textStyle =
                                        MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = content.data.genre.bodyFont(),
                                        ),
                                )
                            }
                        }
                    },
                ) {
                    AnimatedContent(
                        selectedCharacter,
                        transitionSpec = {
                            scaleIn() togetherWith scaleOut()
                        },
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .clickable {
                                    coroutineScope.launch {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        characterToolTipState.show()
                                    }
                                },
                    ) {
                        it?.let { character ->
                            CharacterAvatar(
                                character.data,
                                useFallback = true,
                                genre = content.data.genre,
                                grainRadius = 0f,
                                pixelation = 0f,
                                modifier =
                                    Modifier
                                        .fillMaxSize(),
                            )
                        }
                    }
                }

                Box(Modifier.weight(1f)) {
                    TooltipBox(
                        positionProvider = queryTooltipPositionProvider,
                        state = queryItemsTooltipState,
                        onDismissRequest = {
                            // Tooltip will dismiss when query is null
                        },
                        tooltip = {
                            AnimatedContent(queryItemsType, transitionSpec = {
                                slideInVertically { -it } + fadeIn(tween(300)) togetherWith
                                    fadeOut(
                                        tween(300),
                                    )
                            }) {
                                it?.let { itemsType ->
                                    QueryItemsTooltip(
                                        saga = content,
                                        currentType = itemsType,
                                        modifier =
                                            Modifier
                                                .padding(16.dp)
                                                .fillMaxWidth(),
                                        onClick = { type, item ->
                                            when (type) {
                                                is ItemsType.Characters -> {
                                                    handleCharacterSelection(
                                                        item as CharacterContent,
                                                        inputField,
                                                        onUpdateInput,
                                                    )
                                                }

                                                is ItemsType.Wikis -> {
                                                    handleWikiSelection(
                                                        item as Wiki,
                                                        inputField,
                                                        onUpdateInput,
                                                    )
                                                }
                                            }
                                        },
                                    )
                                }
                            }
                        },
                    ) {
                        BasicTextField(
                            inputField,
                            enabled = isGenerating.not(),
                            maxLines = if (isGenerating) 1 else Int.MAX_VALUE,
                            onValueChange = {
                                if (it.text.length <= maxLength) {
                                    onUpdateInput(it)
                                }
                            },
                            textStyle = textStyle,
                            visualTransformation = {
                                transformTextWithContent(
                                    content.data.genre,
                                    content.mainCharacter?.data,
                                    content.getCharacters(),
                                    content.wikis,
                                    inputField.text,
                                    tagBackgroundColor,
                                )
                            },
                            cursorBrush =
                                Brush.verticalGradient(
                                    content.data.genre.color
                                        .darkerPalette(),
                                ),
                            decorationBox = { innerTextField ->
                                val boxPadding = 12.dp
                                Box(
                                    contentAlignment = Alignment.CenterStart,
                                    modifier =
                                        Modifier
                                            .padding(boxPadding)
                                            .reactiveShimmer(
                                                isGenerating,
                                            ),
                                ) {
                                    val textAlpha by animateFloatAsState(
                                        if (inputField.text.isEmpty()) 0f else 1f,
                                    )
                                    val hintAlpha by animateFloatAsState(
                                        if (inputField.text.isEmpty()) 1f else 0f,
                                    )
                                    AnimatedContent(
                                        action,
                                        modifier = Modifier.alpha(hintAlpha),
                                    ) {
                                        Text(
                                            it.hint(),
                                            style = textStyle,
                                            maxLines = 1,
                                            modifier =
                                                Modifier
                                                    .alpha(.4f),
                                        )
                                    }

                                    Box(
                                        Modifier
                                            .alpha(textAlpha),
                                    ) {
                                        innerTextField()
                                    }
                                }
                            },
                        )
                    }
                }

                val iconBackground by animateColorAsState(
                    if (isGenerating) Color.Transparent else content.data.genre.color,
                )

                val iconTint by animateColorAsState(
                    if (isGenerating) content.data.genre.color else content.data.genre.iconColor,
                )

                IconButton(
                    onClick = {
                        if (isGenerating) return@IconButton
                        if (inputField.text.isEmpty()) {
                            onRequestAudio()
                            return@IconButton
                        }
                        sendMessage()
                    },
                    colors =
                        IconButtonDefaults.filledIconButtonColors(
                            containerColor = iconBackground,
                            contentColor = iconTint,
                        ),
                    modifier = Modifier.size(36.dp),
                ) {
                    AnimatedContent(
                        isGenerating,
                        transitionSpec = {
                            slideInVertically { -it } togetherWith slideOutVertically { it }
                        },
                        modifier =
                            Modifier
                                .padding(8.dp)
                                .reactiveShimmer(
                                    isGenerating,
                                ).fillMaxSize(),
                    ) { loading ->
                        val icon =
                            if (loading) {
                                R.drawable.ic_spark
                            } else if (inputField.text.isEmpty()) {
                                R.drawable.ic_mic
                            } else {
                                R.drawable.ic_send
                            }
                        Icon(
                            painterResource(icon),
                            contentDescription = "Send Message",
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            AnimatedVisibility(isImeVisible) {
                val suggestionsState = rememberLazyListState()

                LaunchedEffect(action) {
                    suggestionsState.animateScrollToItem(0)
                }
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    state = suggestionsState,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val inputs =
                        SenderType.filterUserInputTypes().sortedByDescending {
                            it == action
                        }
                    items(inputs) {
                        val alpha by animateFloatAsState(
                            if (it == action) 1f else .5f,
                            tween(300),
                        )
                        val brush =
                            if (it ==
                                action
                            ) {
                                content.data.genre.gradient()
                            } else {
                                MaterialTheme.colorScheme.onBackground.solidGradient()
                            }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier =
                                Modifier
                                    .animateItem()
                                    .reactiveShimmer(it == action)
                                    .alpha(alpha)
                                    .wrapContentSize()
                                    .clip(content.data.genre.shape())
                                    .gradientFill(brush)
                                    .clickable {
                                        onUpdateSender(it)
                                    }.padding(16.dp),
                        ) {
                            val weight =
                                if (it == action) FontWeight.Bold else FontWeight.Normal
                            it.icon().let { icon ->
                                Image(
                                    painterResource(icon),
                                    null,
                                    modifier = Modifier.size(12.dp),
                                )

                                Text(
                                    it.title(),
                                    style =
                                        MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = weight,
                                            fontFamily = content.data.genre.bodyFont(),
                                        ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(typoFix) {
        if (typoFix != null && typoFix.status != TypoStatus.OK) {
            bottomSheetState.show()
        } else {
            if (bottomSheetState.isVisible) {
                bottomSheetState.hide()
            }
        }
    }

    typoFix?.let {
        if (it.status != TypoStatus.OK) {
            ModalBottomSheet(
                onDismissRequest = {
                    sendMessage(true)
                },
                sheetState = bottomSheetState,
                dragHandle = { },
                shape = content.data.genre.shape(),
                containerColor = Color.Transparent,
            ) {
                val genre = content.data.genre
                var isEnabled by remember { mutableStateOf(true) }
                typoFix.let {
                    Column(
                        modifier =
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                                .border(1.dp, genre.color.gradientFade(), genre.shape())
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainer,
                                    genre.shape(),
                                ).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            it.friendlyMessage ?: emptyString(),
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Light,
                                ),
                            modifier = Modifier.alpha(.4f),
                        )

                        Text(
                            it.suggestedText ?: emptyString(),
                            style =
                                MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = genre.bodyFont(),
                                    brush = genre.gradient(gradientType = GradientType.LINEAR),
                                ),
                            modifier =
                                Modifier.reactiveShimmer(
                                    true,
                                ),
                        )

                        AnimatedVisibility(it.status != TypoStatus.FIX) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    enabled = isEnabled,
                                    shape = genre.shape(),
                                    onClick = {
                                        it.suggestedText?.let { text ->
                                            onUpdateInput(
                                                TextFieldValue(
                                                    it.suggestedText,
                                                    TextRange(it.suggestedText.length),
                                                ),
                                            )
                                        }
                                        sendMessage(true)
                                        isEnabled = false
                                    },
                                    colors =
                                        ButtonDefaults.buttonColors().copy(
                                            containerColor = genre.color,
                                            contentColor = genre.iconColor,
                                        ),
                                ) {
                                    Text(
                                        "Corrigir",
                                        style =
                                            MaterialTheme.typography.labelMedium.copy(
                                                fontFamily = genre.bodyFont(),
                                                color = genre.iconColor,
                                            ),
                                    )
                                }

                                Button(
                                    enabled = isEnabled,
                                    onClick = {
                                        sendMessage(true)
                                        isEnabled = false
                                    },
                                    shape = genre.shape(),
                                    colors =
                                        ButtonDefaults.textButtonColors().copy(
                                            contentColor =
                                                MaterialTheme.colorScheme.onBackground.copy(
                                                    alpha = .5f,
                                                ),
                                        ),
                                ) {
                                    Text(
                                        "Continuar",
                                        style =
                                            MaterialTheme.typography.labelMedium.copy(
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
}
