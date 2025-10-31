@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.characters.ui.CharacterHorizontalView
import com.ilustris.sagai.features.characters.ui.components.transformTextWithContent
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.newsaga.data.model.shimmerColors
import com.ilustris.sagai.features.saga.chat.data.model.SenderType
import com.ilustris.sagai.features.saga.chat.data.model.TypoFix
import com.ilustris.sagai.features.saga.chat.data.model.TypoStatus
import com.ilustris.sagai.features.saga.chat.domain.model.Suggestion
import com.ilustris.sagai.ui.theme.GradientType
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.BlurredGlowContainer
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shape
import com.ilustris.sagai.ui.theme.solidGradient
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputView(
    content: SagaContent,
    isGenerating: Boolean,
    suggestions: List<Suggestion>,
    modifier: Modifier = Modifier,
    inputField: TextFieldValue,
    sendType: SenderType,
    typoFix: TypoFix?,
    onUpdateInput: (TextFieldValue) -> Unit,
    onUpdateSender: (SenderType) -> Unit,
    onSendMessage: (Boolean) -> Unit,
) {
    val action = sendType
    val inputBrush =
        if (isGenerating) {
            content.data.genre.gradient(
                true,
                gradientType = GradientType.LINEAR,
                duration = 2.seconds,
            )
        } else {
            Color.Transparent.solidGradient()
        }

    var charactersExpanded by remember {
        mutableStateOf(false)
    }

    val glowRadius by animateFloatAsState(
        if (isGenerating.not()) 0f else 30f,
    )
    val inputShape = RoundedCornerShape(content.data.genre.cornerSize())

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

                                onUpdateInput(TextFieldValue(textReplacement, TextRange(textReplacement.length)))

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

        BlurredGlowContainer(
            modifier =
                Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .padding(8.dp),
            inputBrush,
            glowRadius,
            shape = inputShape,
        ) {
            Column(
                modifier =
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                        .border(1.dp, inputBrush, inputShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer, inputShape)
                        .padding(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .fillMaxWidth(),
                ) {
                    val textStyle =
                        MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontFamily = content.data.genre.bodyFont(),
                        )
                    val maxLength = 500
                    val tagBackgroundColor = MaterialTheme.colorScheme.background

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
                                    Modifier.padding(boxPadding).reactiveShimmer(
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

                                Box(Modifier.alpha(textAlpha)) {
                                    innerTextField()
                                }
                            }
                        },
                        modifier =
                            Modifier
                                .weight(1f)
                                .animateContentSize(),
                    )

                    AnimatedVisibility(
                        inputField.text.isNotEmpty(),
                        enter = scaleIn(animationSpec = tween(easing = LinearOutSlowInEasing)),
                        exit = scaleOut(animationSpec = tween(easing = EaseIn)),
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
                                        .gradientFill(content.data.genre.gradient())
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
                                .background(MaterialTheme.colorScheme.surfaceContainer, genre.shape())
                                .padding(16.dp),
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
