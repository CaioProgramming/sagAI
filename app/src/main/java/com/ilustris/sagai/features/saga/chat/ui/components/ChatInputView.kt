@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.ilustris.sagai.features.saga.chat.ui.components

import android.graphics.Matrix
import android.graphics.Shader
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.characters.ui.CharacterHorizontalView
import com.ilustris.sagai.features.characters.ui.CharacterYearbookItem
import com.ilustris.sagai.features.characters.ui.components.transformTextWithContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.shimmerColors
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.data.model.TypoFix
import com.ilustris.sagai.features.saga.chat.data.model.TypoStatus
import com.ilustris.sagai.features.saga.chat.domain.model.Suggestion
import com.ilustris.sagai.ui.theme.GradientType
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.BlurredGlowContainer
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shape
import com.ilustris.sagai.ui.theme.solidGradient
import kotlin.time.Duration.Companion.seconds

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
    sharedTransitionScope: SharedTransitionScope,
    onUpdateInput: (TextFieldValue) -> Unit,
    onUpdateSender: (SenderType) -> Unit,
    onSendMessage: (Boolean) -> Unit,
    onSelectCharacter: (CharacterContent) -> Unit = {},
) {
    val action = sendType
    val inputBrush =
        content.data.genre.gradient(
            isGenerating,
            duration = 2.seconds,
        )

    var charactersExpanded by remember {
        mutableStateOf(false)
    }

    val glowRadius by animateFloatAsState(
        if (isGenerating.not()) 10f else 30f,
    )
    val backgroundColor by animateColorAsState(
        if (isGenerating) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surfaceContainer,
    )
    val inputShape = remember { content.data.genre.shape() }

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
        charactersExpanded = false
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    Column(
        modifier
            .fillMaxWidth()
            .imePadding(),
    ) {
        AnimatedVisibility(charactersExpanded && content.characters.isNotEmpty()) {
            LazyColumn(
                Modifier
                    .padding(16.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceContainer,
                        RoundedCornerShape(10.dp),
                    ).heightIn(max = 300.dp)
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                items(content.characters) {
                    CharacterHorizontalView(
                        Modifier
                            .wrapContentSize()
                            .padding(8.dp)
                            .clickable {
                                val startIndex =
                                    inputField.text.indexOfLast { char -> char == '@' }
                                val endIndex = inputField.text.length

                                val newText = it.data.name
                                val textReplacement =
                                    inputField.text.replaceRange(
                                        startIndex,
                                        endIndex,
                                        newText,
                                    )

                                onUpdateInput(
                                    TextFieldValue(
                                        textReplacement,
                                        TextRange(textReplacement.length),
                                    ),
                                )

                                charactersExpanded = false
                            },
                        character = it.data,
                        isLast = it == content.characters.last(),
                        imageSize = 32.dp,
                        genre = content.data.genre,
                        borderSize = 1.dp,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }

        val isImeVisible = WindowInsets.isImeVisible
        val suggestionsEnabled = suggestions.isNotEmpty() && isImeVisible
        var characterSelectionExpanded by remember { mutableStateOf(false) }

        LaunchedEffect(isImeVisible) {
            characterSelectionExpanded = false
        }

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
                            characterSelectionExpanded = false
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

        BlurredGlowContainer(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            inputBrush,
            glowRadius,
            shape = inputShape,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
                modifier =
                    Modifier
                        .padding(1.dp)
                        .fillMaxWidth()
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
                        }.background(backgroundColor, inputShape),
            ) {
                val characterToolTipState =
                    androidx.compose.material3.rememberTooltipState(
                        isPersistent = true,
                    )
                val tooltipPositionProvider =
                    androidx.compose.material3.TooltipDefaults.rememberPlainTooltipPositionProvider(
                        spacingBetweenTooltipAndAnchor = 4.dp,
                    )

                LaunchedEffect(characterSelectionExpanded) {
                    if (characterSelectionExpanded) {
                        characterToolTipState.show()
                    } else {
                        characterToolTipState.dismiss()
                    }
                }

                Column(
                    modifier =
                        Modifier
                            .verticalScroll(rememberScrollState())
                            .align(Alignment.Bottom)
                            .heightIn(max = 300.dp)
                            .weight(1f)
                            .padding(8.dp),
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        val textStyle =
                            MaterialTheme.typography.labelMedium.copy(
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
                                    .padding(8.dp)
                                    .size(36.dp),
                            onDismissRequest = {
                                characterSelectionExpanded = false
                            },
                            tooltip = {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    modifier =
                                        Modifier
                                            .padding(16.dp)
                                            .heightIn(max = 300.dp)
                                            .fillMaxWidth(.5f)
                                            .border(
                                                1.dp,
                                                content.data.genre.color
                                                    .gradientFade(),
                                                content.data.genre.shape(),
                                            ).background(
                                                MaterialTheme.colorScheme.background,
                                                content.data.genre.shape(),
                                            ),
                                ) {
                                    item(span = { GridItemSpan(maxLineSpan) }) {
                                        Text(
                                            "Selecionar personagem",
                                            style =
                                                MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = content.data.genre.bodyFont(),
                                                    textAlign = TextAlign.Center,
                                                ),
                                            modifier = Modifier.padding(8.dp),
                                        )
                                    }
                                    items(content.characters) {
                                        CharacterYearbookItem(
                                            it.data,
                                            content.data.genre,
                                            imageModifier =
                                                Modifier
                                                    .clickable {
                                                        onSelectCharacter(it)
                                                        characterSelectionExpanded = false
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
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                it?.let { character ->
                                    CharacterAvatar(
                                        character.data,
                                        genre = content.data.genre,
                                        modifier =
                                            Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .clickable {
                                                    characterSelectionExpanded = true
                                                },
                                    )
                                }
                            }
                        }

                        BasicTextField(
                            inputField,
                            enabled = isGenerating.not(),
                            maxLines = if (isGenerating) 1 else Int.MAX_VALUE,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions =
                                KeyboardActions(onSend = {
                                    if (isGenerating.not() && inputField.text.isNotEmpty()) {
                                        sendMessage()
                                    }
                                }),
                            onValueChange = {
                                if (it.text.length <= maxLength) {
                                    onUpdateInput(it)

                                    charactersExpanded = it.text.isNotEmpty() &&
                                        it.text.last().toString() == "@" &&
                                        it.text.length <= (maxLength - 20)
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
                                                content.data.genre.shimmerColors(),
                                            ),
                                ) {
                                    val textAlpha by animateFloatAsState(
                                        if (inputField.text.isEmpty()) 0f else 1f,
                                    )
                                    val hintAlpha by animateFloatAsState(
                                        if (inputField.text.isEmpty()) 1f else 0f,
                                    )
                                    AnimatedContent(action, modifier = Modifier.alpha(hintAlpha)) {
                                        Text(
                                            it.hint(),
                                            style = textStyle,
                                            maxLines = 1,
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .alpha(.4f),
                                        )
                                    }

                                    Box(
                                        Modifier
                                            .alpha(textAlpha)
                                            .fillMaxWidth(),
                                    ) {
                                        innerTextField()
                                    }
                                }
                            },
                            modifier =
                                Modifier
                                    .align(Alignment.CenterVertically)
                                    .weight(1f)
                                    .animateContentSize(),
                        )

                        AnimatedVisibility(
                            inputField.text.isNotEmpty(),
                            enter = scaleIn(animationSpec = tween(easing = LinearOutSlowInEasing)),
                            exit = scaleOut(animationSpec = tween(easing = EaseIn)),
                            modifier = Modifier.align(Alignment.Bottom),
                        ) {
                            val buttonColor by animateColorAsState(
                                if (isGenerating.not()) {
                                    content.data.genre.color
                                } else {
                                    Color.Transparent
                                },
                            )
                            IconButton(
                                onClick = {
                                    if (isGenerating) return@IconButton
                                    sendMessage()
                                    charactersExpanded = false
                                },
                                modifier =
                                    Modifier
                                        .padding(4.dp)
                                        .background(
                                            buttonColor,
                                            CircleShape,
                                        ).size(32.dp),
                            ) {
                                AnimatedVisibility(
                                    isGenerating.not(),
                                    modifier =
                                        Modifier
                                            .padding(2.dp)
                                            .fillMaxSize(),
                                ) {
                                    val icon = R.drawable.ic_arrow_up
                                    Icon(
                                        painterResource(icon),
                                        contentDescription = "Send Message",
                                        modifier =
                                            Modifier
                                                .padding(2.dp)
                                                .fillMaxSize(),
                                        tint = content.data.genre.iconColor,
                                    )
                                }
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
