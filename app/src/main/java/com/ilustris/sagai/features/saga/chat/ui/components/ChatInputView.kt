@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.ilustris.sagai.features.saga.chat.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.characters.ui.CharacterHorizontalView
import com.ilustris.sagai.features.characters.ui.SimpleCharacterForm
import com.ilustris.sagai.features.characters.ui.components.transformTextWithContent
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.shimmerColors
import com.ilustris.sagai.features.saga.chat.domain.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.model.Suggestion
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.theme.GradientType
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.BlurredGlowContainer
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shape
import com.ilustris.sagai.ui.theme.solidGradient
import effectForGenre
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputView(
    content: SagaContent,
    isGenerating: Boolean,
    suggestions: List<Suggestion>,
    modifier: Modifier = Modifier,
    onSendMessage: (String, SenderType) -> Unit,
    onCreateNewCharacter: (CharacterInfo) -> Unit, // Added callback
) {
    var action by remember { mutableStateOf(SenderType.USER) }
    var inputField by remember { mutableStateOf(TextFieldValue("")) }
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

    var actionsExpanded by remember {
        mutableStateOf(false)
    }
    val glowRadius by animateFloatAsState(
        if (isGenerating.not()) 0f else 30f,
    )
    val inputShape = RoundedCornerShape(content.data.genre.cornerSize())

    var showNewCharacterSheet by remember { mutableStateOf(false) }
    val newCharacterSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    fun sendMessage(
        text: String,
        action: SenderType,
    ) {
        onSendMessage(text, action)
        inputField = TextFieldValue("")
        charactersExpanded = false
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    Column(
        modifier
            .fillMaxWidth(),
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

                                inputField =
                                    TextFieldValue(
                                        textReplacement,
                                        TextRange(textReplacement.length),
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
                            inputField = TextFieldValue(it.text, selection = TextRange(it.text.length))
                            action = it.type
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
                                Modifier.padding(4.dp).size(12.dp).reactiveShimmer(
                                    true,
                                    content.data.genre.color
                                        .darkerPalette()
                                        .plus(Color.Transparent),
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
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .padding(8.dp),
            inputBrush,
            glowRadius,
            shape = inputShape,
        ) {
            Column(
                modifier =
                    Modifier
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
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions =
                            KeyboardActions(onSend = {
                                if (isGenerating.not() && inputField.text.isNotEmpty()) {
                                    sendMessage(
                                        inputField.text,
                                        action,
                                    )
                                }
                            }),
                        onValueChange = {
                            if (it.text.length <= maxLength) {
                                inputField = it

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
                            content.data.genre.gradient(),
                        decorationBox = { innerTextField ->
                            val boxPadding = 12.dp
                            Box(
                                contentAlignment = Alignment.CenterStart,
                                modifier =
                                    Modifier.reactiveShimmer(
                                        isGenerating,
                                        content.data.genre.shimmerColors(),
                                    ),
                            ) {
                                if (inputField.text.isEmpty()) {
                                    AnimatedContent(action) {
                                        Text(
                                            it.hint(),
                                            style = textStyle,
                                            maxLines = 1,
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(boxPadding)
                                                    .alpha(.4f),
                                        )
                                    }
                                } else {
                                    Box(Modifier.padding(boxPadding)) {
                                        innerTextField()
                                    }
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
                                onSendMessage(inputField.text, action)
                                inputField = TextFieldValue("")
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
                                            action = it
                                        }.padding(16.dp),
                            ) {
                                val weight = if (it == action) FontWeight.Bold else FontWeight.Normal
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

                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier =
                                    Modifier
                                        .clip(content.data.genre.shape())
                                        .gradientFill(gradientAnimation(holographicGradient, gradientType = GradientType.VERTICAL))
                                        .clickable {
                                            showNewCharacterSheet = true
                                        }.padding(16.dp),
                            ) {
                                Image(
                                    painterResource(R.drawable.character_icon),
                                    null,
                                    modifier = Modifier.size(12.dp),
                                )

                                Text(
                                    SenderType.NEW_CHARACTER.title(),
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isImeVisible) {
            Spacer(Modifier.height(50.dp))
        }
    }

    if (showNewCharacterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNewCharacterSheet = false },
            sheetState = newCharacterSheetState,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    stringResource(R.string.sender_type_new_character_title),
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontFamily = content.data.genre.bodyFont(),
                            fontWeight = FontWeight.Bold,
                        ),
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                SimpleCharacterForm(
                    content.data.genre,
                    Modifier
                        .fillMaxSize(),
                ) {
                    onCreateNewCharacter(it)

                    scope.launch { newCharacterSheetState.hide() }.invokeOnCompletion {
                        if (!newCharacterSheetState.isVisible) {
                            showNewCharacterSheet = false
                        }
                    }
                }

                Spacer(Modifier.height(8.dp)) // Padding at the bottom
            }
        }
    }
}

@Composable
private fun MainCharacterInputButton(
    saga: Saga,
    character: Character,
    currentAction: SenderType,
    onClickAction: () -> Unit = {},
) {
    val tooltipState = rememberTooltipState()

    TooltipBox(
        positionProvider =
            TooltipDefaults.rememberRichTooltipPositionProvider(
                spacingBetweenTooltipAndAnchor = 8.dp,
            ),
        tooltip = {
            RichTooltip(
                title = { Text(currentAction.title()) },
                caretSize = DpSize(12.dp, 12.dp),
                shape = RoundedCornerShape(saga.genre.cornerSize()),
            ) {
                Text(currentAction.description())
            }
        },
        state = tooltipState,
    ) {
        Box(
            Modifier.size(24.dp),
        ) {
            CharacterAvatar(
                character,
                borderSize = 1.dp,
                genre = saga.genre,
                innerPadding = 0.dp,
                modifier =
                    Modifier
                        .clip(CircleShape)
                        .fillMaxSize()
                        .clickable {
                            onClickAction()
                        }.effectForGenre(
                            saga.genre,
                            focusRadius = .3f,
                            customGrain = .2f,
                        ),
            )

            AnimatedContent(
                currentAction,
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp),
                transitionSpec = {
                    scaleIn() + fadeIn(tween(300)) togetherWith scaleOut() + fadeOut()
                },
            ) {
                it.icon()?.let { icon ->
                    Box(
                        modifier =
                            Modifier
                                .border(1.dp, MaterialTheme.colorScheme.background, CircleShape)
                                .background(saga.genre.color, CircleShape)
                                .size(12.dp)
                                .padding(3.dp),
                    ) {
                        Icon(
                            painterResource(icon),
                            null,
                            tint = saga.genre.iconColor,
                            modifier =
                                Modifier
                                    .align(Alignment.Center)
                                    .fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}
