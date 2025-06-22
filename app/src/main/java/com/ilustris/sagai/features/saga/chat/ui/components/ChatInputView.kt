@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.characters.ui.CharacterHorizontalView
import com.ilustris.sagai.features.characters.ui.components.transformTextWithCharacters
import com.ilustris.sagai.features.home.data.model.IllustrationVisuals
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.SenderType
import com.ilustris.sagai.features.saga.chat.presentation.ChatState
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.holographicGradient
import kotlin.time.Duration.Companion.seconds

@Composable
fun ChatInputView(
    mainCharacter: Character?,
    characters: List<Character>,
    saga: SagaData?,
    state: ChatState,
    isGenerating: Boolean,
    modifier: Modifier = Modifier,
    onSendMessage: (String, SenderType) -> Unit,
) {
    var action by remember { mutableStateOf(SenderType.USER) }
    var inputField by remember { mutableStateOf(TextFieldValue("")) }
    val inputBrush =
        if (isGenerating) {
            gradientAnimation(holographicGradient, targetValue = 500f, duration = 2.seconds)
        } else {
            MaterialTheme.colorScheme.onSurface
                .copy(alpha = .4f)
                .gradientFade()
        }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier =
            modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .fillMaxWidth()
                .border(
                    1.dp,
                    inputBrush,
                    RoundedCornerShape(40.dp),
                ).background(
                    MaterialTheme.colorScheme.background,
                    RoundedCornerShape(40.dp),
                ).padding(horizontal = 16.dp, vertical = 2.dp),
    ) {
        mainCharacter?.let { character ->
            MainCharacterInputButton(action, saga, character) {
                action = it
            }
        }
        var charactersExpanded by remember { mutableStateOf(false) }
        if (characters.isNotEmpty()) {
            DropdownMenu(
                charactersExpanded,
                shape = RoundedCornerShape(20.dp),
                onDismissRequest = {
                    charactersExpanded = false
                },
            ) {
                characters.forEach {
                    CharacterHorizontalView(
                        Modifier.clip(RoundedCornerShape(25.dp)).clickable {
                            val startIndex = inputField.text.indexOfLast { char -> char == '@' }
                            val endIndex = inputField.text.length

                            val newText = it.name
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
                        character = it,
                        imageSize = 24.dp,
                        genre = saga?.genre ?: Genre.FANTASY,
                    )
                }
            }
        }

        val maxLength = 300
        TextField(
            enabled = isGenerating.not(),
            visualTransformation = {
                return@TextField transformTextWithCharacters(
                    characters,
                    inputField.text,
                )
            },
            value = inputField,
            onValueChange = {
                if (it.text.length <= maxLength) {
                    inputField = it

                    charactersExpanded = it.text.isNotEmpty() &&
                        it.text.last().toString() == "@" &&
                        it.text.length <= (maxLength - 20)
                }
            },
            placeholder = {
                Text(
                    "Continua sua saga...",
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                        ),
                )
            },
            shape = RoundedCornerShape(40.dp),
            colors =
                TextFieldDefaults.colors().copy(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor =
                        saga?.genre?.color
                            ?: MaterialTheme.colorScheme.primary,
                    disabledIndicatorColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
            textStyle =
                MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                ),
            modifier =
                Modifier
                    .wrapContentHeight()
                    .weight(1f),
        )

        val buttonSize by animateDpAsState(
            if (inputField.text.isNotEmpty() && state is ChatState.Success) 32.dp else 0.dp,
        )
        val buttonColor =
            saga?.genre?.color ?: MaterialTheme.colorScheme.primary
        AnimatedVisibility(inputField.text.isNotEmpty(), enter = fadeIn(), exit = scaleOut()) {
            IconButton(
                onClick = {
                    onSendMessage(inputField.text, action)
                    inputField = TextFieldValue("")
                },
                modifier =
                    Modifier
                        .background(
                            buttonColor,
                            CircleShape,
                        ).size(buttonSize),
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = "Send Message",
                    modifier =
                        Modifier
                            .padding(4.dp)
                            .fillMaxSize(),
                    tint = saga?.genre?.iconColor ?: MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

@Composable
private fun MainCharacterInputButton(
    sendAction: SenderType,
    saga: SagaData?,
    character: Character,
    onChangeAction: (SenderType) -> Unit = {},
) {
    var action by remember {
        mutableStateOf(sendAction)
    }
    var actionsExpanded by remember {
        mutableStateOf(true)
    }

    val tooltipState = rememberTooltipState()

    DropdownMenu(
        actionsExpanded,
        properties =
            PopupProperties(
                usePlatformDefaultWidth = false,
            ),
        shape = RoundedCornerShape(saga?.genre?.cornerSize() ?: 15.dp),
        containerColor = Color.Transparent,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        onDismissRequest = {
            actionsExpanded = false
        },
    ) {
        Column {
            SenderType.entries
                .filter {
                    it != SenderType.CHARACTER &&
                        it != SenderType.NEW_CHAPTER
                }.forEach { type ->
                    type.itemOption(
                        iconSize = 42.dp,
                        action,
                        selectedColor =
                            saga?.genre?.color
                                ?: MaterialTheme.colorScheme.primary,
                    ) { selectedAction ->
                        action = selectedAction
                        onChangeAction(selectedAction)
                        actionsExpanded = false
                    }
                }
        }
    }
    TooltipBox(
        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
        tooltip = {
            RichTooltip(
                title = { Text(action.title()) },
                caretSize = DpSize(12.dp, 16.dp),
                shape = RoundedCornerShape(saga?.genre?.cornerSize() ?: 0.dp)
            ) {
                Text(action.description())
            }
        },
        state = tooltipState,
    ) {
        AnimatedContent(action, transitionSpec = {
            scaleIn() + fadeIn(tween(300)) togetherWith scaleOut() + fadeOut()
        }) {
            when (it) {
                SenderType.USER ->
                    CharacterAvatar(
                        character,
                        borderSize = 2.dp,
                        modifier =
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable {
                                    actionsExpanded = actionsExpanded.not()
                                },
                    )

                else ->
                    it.itemOption(
                        iconSize = 32.dp,
                        action,
                        selectedColor =
                            saga?.genre?.color
                                ?: MaterialTheme.colorScheme.primary,
                    ) {
                        actionsExpanded = actionsExpanded.not()
                    }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatInputViewPreview() {
    ChatInputView(
        mainCharacter =
            Character(
                id = 0,
                name = "Character Name",
                backstory = "Character backstory",
                image = "image_url",
                hexColor = "#FF0000",
                sagaId = 0,
                details =
                    Details(
                        appearance = "Appearance",
                        personality = "Personality",
                        race = "Race",
                        height = 1.80,
                        weight = 70.0,
                        style = "Style",
                        gender = "Gender",
                        occupation = "Occupation",
                        ethnicity = "Ethnicity",
                    ),
                joinedAt = System.currentTimeMillis(),
                status = "Character status",
            ),
        characters = listOf(),
        saga =
            SagaData(
                id = 0,
                title = "Saga Title",
                description = "Saga description",
                icon = "icon_url",
                createdAt = System.currentTimeMillis(),
                genre = Genre.FANTASY,
                mainCharacterId = 0,
                visuals = IllustrationVisuals(),
                lastLoreReference = 0,
            ),
        state = ChatState.Success,
        isGenerating = false,
    ) { _, _ -> }
}

@Preview
@Composable
fun MainCharacterInputButtonPreview() {
    MainCharacterInputButton(
        sendAction = SenderType.USER,
        saga =
            SagaData(
                id = 0,
                title = "Saga Title",
                description = "Saga description",
                icon = "icon_url",
                createdAt = System.currentTimeMillis(),
                genre = Genre.FANTASY,
                mainCharacterId = 0,
                visuals = IllustrationVisuals(),
                lastLoreReference = 0,
            ),
        character =
            Character(
                id = 0,
                name = "Character Name",
                backstory = "Character backstory",
                image = "image_url",
                hexColor = "#FF0000",
                sagaId = 0,
                details =
                    Details(
                        appearance = "Appearance",
                        personality = "Personality",
                        race = "Race",
                        height = 1.80,
                        weight = 70.0,
                        style = "Style",
                        gender = "Gender",
                        occupation = "Occupation",
                        ethnicity = "Ethnicity",
                    ),
                joinedAt = System.currentTimeMillis(),
                status = "Character status",
            ),
    ) {}
}
