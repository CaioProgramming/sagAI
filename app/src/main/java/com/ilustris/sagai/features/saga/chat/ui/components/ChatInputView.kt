package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.characters.ui.CharacterHorizontalView
import com.ilustris.sagai.features.characters.ui.components.transformTextWithCharacters
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.SenderType
import com.ilustris.sagai.features.saga.chat.presentation.ChatState
import com.ilustris.sagai.ui.theme.gradientFade
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import com.skydoves.balloon.compose.Balloon
import com.skydoves.balloon.compose.BalloonWindow
import com.skydoves.balloon.compose.rememberBalloonBuilder
import com.skydoves.balloon.compose.setBackgroundColor

@Composable
fun ChatInputView(
    mainCharacter: Character?,
    characters: List<Character>,
    sendAction: SenderType,
    saga: SagaData?,
    input: String,
    state: ChatState,
    onSendMessage: (String, SenderType) -> Unit,
) {
    var sendAction = sendAction
    var inputField by remember { mutableStateOf(input) }
    var textSelection by remember { mutableStateOf(TextRange(inputField.length)) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier =
            Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.onSurface
                        .copy(alpha = .4f)
                        .gradientFade(),
                    RoundedCornerShape(40.dp),
                ).background(
                    MaterialTheme.colorScheme.surfaceContainer,
                    RoundedCornerShape(40.dp),
                ).padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        mainCharacter?.let { character ->
            MainCharacterInputButton(sendAction, saga, character) {
                sendAction = it
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
                            val startIndex = inputField.indexOfLast { char -> char == '@' }
                            val endIndex = inputField.length

                            val newText = it.name
                            inputField =
                                inputField.replaceRange(
                                    startIndex,
                                    endIndex,
                                    newText,
                                )
                            textSelection = TextRange(startIndex + newText.length)
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
            visualTransformation = {
                return@TextField transformTextWithCharacters(
                    characters,
                    inputField,
                )
            },
            value = inputField,
            onValueChange = {
                if (it.length <= maxLength) {
                    inputField = it

                    charactersExpanded = it.isNotEmpty() &&
                        it.last().toString() == "@" &&
                        it.length <= (maxLength - 20)
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
            if (inputField.isNotEmpty() && state is ChatState.Success) 32.dp else 0.dp,
        )
        val buttonColor =
            saga?.genre?.color ?: MaterialTheme.colorScheme.primary
        IconButton(
            onClick = {
                onSendMessage(inputField, sendAction)
                inputField = ""
            },
            modifier =
                Modifier
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.onBackground.gradientFade(),
                        CircleShape,
                    ).background(
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
                tint = Color.White,
            )
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
    val balloonBackground = MaterialTheme.colorScheme.surfaceContainer
    var balloonWindow: BalloonWindow? by remember { mutableStateOf(null) }
    val builder =
        rememberBalloonBuilder {
            setArrowSize(10)
            setArrowPosition(0f)
            setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            setWidth(BalloonSizeSpec.WRAP)
            setHeight(BalloonSizeSpec.WRAP)
            setPadding(12)
            setMarginHorizontal(4)
            setCornerRadius(15f)
            setBackgroundColor(balloonBackground)
            setBalloonAnimation(BalloonAnimation.OVERSHOOT)
        }
    Balloon(
        builder = builder,
        onBalloonWindowInitialized = { balloonWindow = it },
        balloonContent = {
            Column {
                SenderType.entries
                    .filter {
                        it != SenderType.CHARACTER &&
                            it != SenderType.NEW_CHAPTER
                    }.forEach { type ->
                        type.itemOption(
                            sendAction,
                            selectedColor =
                                saga?.genre?.color
                                    ?: MaterialTheme.colorScheme.primary,
                        ) { action ->
                            onChangeAction(action)
                            balloonWindow?.dismissWithDelay(1000)
                        }
                    }
            }
        },
    ) {
        CharacterAvatar(
            character,
            borderSize = 2.dp,
            modifier =
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable {
                        balloonWindow?.showAsDropDown()
                    },
        )
    }
}
