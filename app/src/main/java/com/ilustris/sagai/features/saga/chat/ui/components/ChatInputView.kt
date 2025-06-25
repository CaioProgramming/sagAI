@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
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
    saga: SagaData,
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
    var charactersExpanded by remember {
        mutableStateOf(false)
    }

    var actionsExpanded by remember {
        mutableStateOf(false)
    }
    val inputShape = RoundedCornerShape(50.dp)
    Column(
        modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth()
            .border(
                1.dp,
                inputBrush,
                inputShape,
            ).background(
                MaterialTheme.colorScheme.background,
                inputShape,
            ).padding(horizontal = 8.dp, vertical = 0.dp)
            .animateContentSize(tween(300, easing = EaseIn)),
    ) {
        if (characters.isNotEmpty()) {
            AnimatedVisibility(charactersExpanded) {
                LazyRow(Modifier.padding(12.dp).fillMaxWidth()) {
                    items(characters) {
                        CharacterHorizontalView(
                            Modifier.padding(4.dp).wrapContentSize().clickable {
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
                            genre = saga.genre,
                            borderSize = 1.dp,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            mainCharacter?.let { character ->
                MainCharacterInputButton(saga, character, action) {
                    actionsExpanded = actionsExpanded.not()
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
                        action.hint(),
                        style =
                            MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp,
                            ),
                        modifier = Modifier.padding(0.dp)
                    )
                },
                shape = RoundedCornerShape(40.dp),
                colors =
                    TextFieldDefaults.colors().copy(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor =
                            saga.genre.color,
                        disabledIndicatorColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    ),
                textStyle =
                    MaterialTheme.typography.labelSmall,
                modifier =
                    Modifier
                        .weight(1f),
            )

            val buttonSize by animateDpAsState(
                if (inputField.text.isNotEmpty() && state is ChatState.Success) 32.dp else 0.dp,
            )
            val buttonColor = saga.genre.color
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
                        tint = saga.genre.iconColor,
                    )
                }
            }
        }

        AnimatedVisibility(actionsExpanded) {
            val actions =
                SenderType.entries
                    .filter {
                        it != SenderType.CHARACTER &&
                            it != SenderType.NEW_CHAPTER
                    }
            LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.padding(vertical = 8.dp)) {
                items(actions) {
                    it.itemOption(
                        selectedItem = action,
                        showText = true,
                        genre = saga.genre,
                        iconSize = 32.dp
                    ) { selectedAction ->
                        actionsExpanded = actionsExpanded.not()
                        action = selectedAction
                    }
                }
            }
        }
    }
}

@Composable
private fun MainCharacterInputButton(
    saga: SagaData,
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
                caretSize = DpSize(12.dp, 8.dp),
                shape = RoundedCornerShape(saga.genre.cornerSize()),
            ) {
                Text(currentAction.description())
            }
        },
        state = tooltipState,
    ) {
        AnimatedContent(currentAction, modifier = Modifier.padding(start = 10.dp), transitionSpec = {
            scaleIn() + fadeIn(tween(300)) togetherWith scaleOut() + fadeOut()
        }) {
            when (it) {
                SenderType.USER ->
                    CharacterAvatar(
                        character,
                        borderSize = 2.dp,
                        genre = saga.genre,
                        modifier =
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable {
                                    onClickAction()
                                },
                    )

                else ->
                    it.itemOption(
                        iconSize = 32.dp,
                        selectedItem = currentAction,
                        showText = false,
                        genre = saga.genre,
                    ) {
                        onClickAction()
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
        characters =
            listOf(
                Character(
                    id = 1,
                    name = "Character 1",
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
                Character(
                    id = 2,
                    name = "Character 2",
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
                Character(
                    id = 3,
                    name = "Character 3",
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
                Character(
                    id = 4,
                    name = "Character 4",
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
                Character(
                    id = 5,
                    name = "Character 5",
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
            ),
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
        currentAction = SenderType.USER,
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
