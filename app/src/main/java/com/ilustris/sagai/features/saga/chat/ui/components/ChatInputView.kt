@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.saga.chat.ui.components

import android.content.res.Configuration
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.characters.ui.CharacterForm
import com.ilustris.sagai.features.characters.ui.CharacterHorizontalView
import com.ilustris.sagai.features.characters.ui.components.transformTextWithContent
import com.ilustris.sagai.features.home.data.model.IllustrationVisuals
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.SenderType
import com.ilustris.sagai.features.saga.chat.presentation.ChatState
import com.ilustris.sagai.ui.theme.GradientType
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFade
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputView(
    content: SagaContent,
    state: ChatState,
    isGenerating: Boolean,
    modifier: Modifier = Modifier,
    onSendMessage: (String, SenderType) -> Unit,
    onCreateNewCharacter: (Character) -> Unit, // Added callback
) {
    var action by remember { mutableStateOf(SenderType.USER) }
    var inputField by remember { mutableStateOf(TextFieldValue("")) }
    val inputBrush =
        if (isGenerating) {
            gradientAnimation(
                listOf(Color.White, Color.White.copy(alpha = .5f), Color.Transparent),
                duration = 2.seconds,
                gradientType = GradientType.SWEEP,
            )
        } else {
            Color.Transparent.gradientFade()
        }
    val glowRadius by animateFloatAsState(
        if (isGenerating) 50f else 0f,
    )
    var charactersExpanded by remember {
        mutableStateOf(false)
    }

    var actionsExpanded by remember {
        mutableStateOf(true)
    }
    val inputShape = RoundedCornerShape(50.dp)

    // State for the New Character Bottom Sheet
    var showNewCharacterSheet by remember { mutableStateOf(false) }
    val newCharacterSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = true) // Consider skipping partial expansion
    val scope = rememberCoroutineScope()
    var characterBeingCreated by remember {
        mutableStateOf(Character(details = Details()))
    }

    Column(
        modifier
            .fillMaxWidth()
            .animateContentSize(tween(300, easing = EaseIn))
            .pointerInput(Unit) {
                detectVerticalDragGestures(onDragEnd = {}) { change, dragAmount ->
                    Log.i("inputDrag", "dragAmount: $dragAmount")
                    if (dragAmount < (DRAG_THRESHOLD).unaryMinus()) {
                        actionsExpanded = true
                    } else if (dragAmount > 0.0f) {
                        actionsExpanded = false
                    }
                }
            }.padding(bottom = 24.dp),
    ) {
        AnimatedVisibility(isGenerating, enter = fadeIn(tween(500)), exit = fadeOut()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        brush =
                            content.data.genre.gradient(
                                gradientType = GradientType.LINEAR,
                                targetValue = 250f,
                                animated = isGenerating,
                            ),
                        RoundedCornerShape(0.dp),
                    ),
            )
        }

        if (content.characters.isNotEmpty()) {
            AnimatedVisibility(charactersExpanded) {
                LazyRow(Modifier.padding(12.dp).fillMaxWidth()) {
                    items(content.characters) {
                        CharacterHorizontalView(
                            Modifier.padding(4.dp).wrapContentSize().clickable {
                                val startIndex =
                                    inputField.text.indexOfLast { char -> char == '@' }
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
                            genre = content.data.genre,
                            borderSize = 1.dp,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
        ) {
            content.mainCharacter?.let { character ->
                MainCharacterInputButton(content.data, character, action) {
                    actionsExpanded = actionsExpanded.not()
                }
            }

            val textStyle =
                MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 12.sp,
                )
            val maxLength = 350
            val tagBackgroundColor = MaterialTheme.colorScheme.onBackground

            BasicTextField(
                inputField,
                enabled = isGenerating.not(),
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
                        content.mainCharacter,
                        content.characters,
                        content.wikis,
                        inputField.text,
                        tagBackgroundColor,
                    )
                },
                cursorBrush =
                    content.data.genre.gradient(),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        // Or your desired alignment
                        if (inputField.text.isEmpty()) {
                            Text(
                                action.hint(),
                                style = textStyle,
                                modifier = Modifier.fillMaxWidth().padding(16.dp).alpha(.4f),
                            )
                        } else {
                            Box(Modifier.padding(16.dp)) {
                                innerTextField()
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f),
            )

            val buttonSize by animateDpAsState(
                if (inputField.text.isNotEmpty() && state is ChatState.Success) 32.dp else 0.dp,
            )
            val buttonColor = content.data.genre.color

            AnimatedVisibility(inputField.text.isEmpty()) {
                IconButton(onClick = {
                    characterBeingCreated = Character(sagaId = content.data.id, details = Details())
                    showNewCharacterSheet = true
                }, modifier = Modifier.padding(4.dp).size(32.dp)) {
                    Icon(
                        painterResource(id = R.drawable.character_icon),
                        contentDescription = stringResource(R.string.sender_type_new_character_title),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f),
                        modifier = Modifier.padding(2.dp).fillMaxSize(),
                    )
                }
            }

            AnimatedVisibility(
                inputField.text.isNotEmpty(),
                enter = fadeIn(),
                exit = scaleOut(),
            ) {
                IconButton(
                    onClick = {
                        onSendMessage(inputField.text, action)
                        inputField = TextFieldValue("")
                        actionsExpanded = false
                        charactersExpanded = false
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
                        tint = content.data.genre.iconColor,
                    )
                }
            }
        }

        AnimatedVisibility(actionsExpanded) {
            val actions =
                SenderType.entries
                    .filter {
                        it != SenderType.CHARACTER &&
                            it != SenderType.NEW_CHAPTER &&
                            it != SenderType.NEW_CHARACTER
                    }
            LazyRow(
                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                items(actions) {
                    it.itemOption(
                        selectedItem = action,
                        showText = true,
                        genre = content.data.genre,
                        iconSize = 32.dp,
                    ) { selectedAction ->
                        actionsExpanded = actionsExpanded.not()
                        action = selectedAction
                    }
                }
            }
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
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                val sagaFormForNewCharacter =
                    remember(content.data.genre, characterBeingCreated) {
                        SagaForm(
                            genre = content.data.genre,
                            character = characterBeingCreated,
                        )
                    }

                CharacterForm(
                    sagaForm = sagaFormForNewCharacter,
                    onUpdateCharacter = { updatedCharacter ->
                        characterBeingCreated = updatedCharacter
                    },
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        // Optional: Add validation here if needed
                        // if (CharacterFormRules.validateCharacter(characterBeingCreated)) {
                        onCreateNewCharacter(characterBeingCreated)
                        scope.launch { newCharacterSheetState.hide() }.invokeOnCompletion {
                            if (!newCharacterSheetState.isVisible) {
                                showNewCharacterSheet = false
                            }
                        }
                        // } else { /* Show validation error message */ }
                    },
                    modifier = Modifier.fillMaxWidth(), // Make button full width
                ) {
                    Text(stringResource(R.string.save_saga)) // Using "Save" as a general create/save action
                }
                Spacer(Modifier.height(8.dp)) // Padding at the bottom
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
                caretSize = DpSize(12.dp, 12.dp),
                shape = RoundedCornerShape(saga.genre.cornerSize()),
            ) {
                Text(currentAction.description())
            }
        },
        state = tooltipState,
    ) {
        Box(
            Modifier.size(32.dp),
        ) {
            CharacterAvatar(
                character,
                borderSize = 2.dp,
                genre = saga.genre,
                softFocusRadius = 0f,
                grainRadius = 0.01f,
                modifier =
                    Modifier.clip(CircleShape).fillMaxSize().clickable {
                        onClickAction()
                    },
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
                                .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                                .background(saga.genre.color, CircleShape)
                                .size(16.dp)
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

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
)
@Composable
fun ChatInputViewPreview() {
    ChatInputView(
        SagaContent(
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
                            gender = "Gender",
                            occupation = "Occupation",
                            ethnicity = "Ethnicity",
                        ),
                    joinedAt = System.currentTimeMillis(),
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
                                gender = "Gender",
                                occupation = "Occupation",
                                ethnicity = "Ethnicity",
                            ),
                        joinedAt = System.currentTimeMillis(),
                    ),
                ),
            data =
                SagaData(
                    id = 0,
                    title = "Saga Title",
                    description = "Saga description",
                    icon = "icon_url",
                    createdAt = System.currentTimeMillis(),
                    genre = Genre.SCI_FI,
                    mainCharacterId = 0,
                    visuals = IllustrationVisuals(),
                    lastLoreReference = 0,
                ),
        ),
        state = ChatState.Success,
        isGenerating = true,
        onSendMessage = { _, _ -> },
        onCreateNewCharacter = {},
    )
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
                        gender = "Gender",
                        occupation = "Occupation",
                        ethnicity = "Ethnicity",
                    ),
                joinedAt = System.currentTimeMillis(),
            ),
    ) {}
}

private const val DRAG_THRESHOLD = -50f
