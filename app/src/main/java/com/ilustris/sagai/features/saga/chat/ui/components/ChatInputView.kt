@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.ilustris.sagai.features.saga.chat.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseIn
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
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.characters.ui.CharacterHorizontalView
import com.ilustris.sagai.features.characters.ui.SimpleCharacterForm
import com.ilustris.sagai.features.characters.ui.components.transformTextWithContent
import com.ilustris.sagai.features.home.data.model.IllustrationVisuals
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.CharacterInfo
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.SenderType
import com.ilustris.sagai.ui.theme.GradientType
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.BlurredGlowContainer
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.solidGradient
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputView(
    content: SagaContent,
    isGenerating: Boolean,
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
            Color.Transparent
                .solidGradient()
        }

    var charactersExpanded by remember {
        mutableStateOf(false)
    }

    var actionsExpanded by remember {
        mutableStateOf(false)
    }
    val inputShape = RoundedCornerShape(content.data.genre.cornerSize())

    var showNewCharacterSheet by remember { mutableStateOf(false) }
    val newCharacterSheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = false) // Consider skipping partial expansion
    val scope = rememberCoroutineScope()

    Column(
        modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .padding(bottom = 24.dp),
    ) {
        AnimatedVisibility(charactersExpanded && content.characters.isNotEmpty()) {
            LazyColumn(
                Modifier
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp))
                    .heightIn(max = 300.dp)
                    .fillMaxWidth()
                    .padding(12.dp),
            ) {
                items(content.characters) {
                    CharacterHorizontalView(
                        Modifier
                            .padding(4.dp)
                            .wrapContentSize()
                            .clickable {
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

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            BlurredGlowContainer(
                brush = inputBrush,
                shape = inputShape,
                modifier =
                    Modifier
                        .weight(1f),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier
                            .padding(2.dp)
                            .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = .3f), inputShape)
                            .background(MaterialTheme.colorScheme.background, inputShape)
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                ) {
                    content.mainCharacter?.let { character ->
                        val isImeVisible = WindowInsets.isImeVisible

                        AnimatedVisibility(
                            inputField.text.isEmpty() && isImeVisible.not() && isGenerating.not(),
                            enter = scaleIn(),
                            exit = fadeOut(),
                        ) {
                            MainCharacterInputButton(content.data, character, action) {
                                actionsExpanded = actionsExpanded.not()
                            }
                        }
                    }

                    val textStyle =
                        MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 12.sp,
                            fontFamily = content.data.genre.bodyFont(),
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
                            val boxPadding = 12.dp
                            Box(contentAlignment = Alignment.CenterStart) {
                                // Or your desired alignment
                                if (inputField.text.isEmpty()) {
                                    Text(
                                        action.hint(),
                                        style = textStyle,
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(boxPadding)
                                                .alpha(.4f),
                                    )
                                } else {
                                    Box(Modifier.padding(boxPadding)) {
                                        innerTextField()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )

                    IconButton(
                        onClick = {
                            actionsExpanded = true
                        },
                        enabled = isGenerating.not(),
                        modifier =
                            Modifier
                                .border(1.dp, MaterialTheme.colorScheme.onBackground, CircleShape)
                                .size(24.dp),
                    ) {
                        val iconRotation by animateFloatAsState(
                            if (actionsExpanded) 45f else 0f,
                        )
                        Icon(
                            Icons.Rounded.Add,
                            contentDescription = stringResource(R.string.sender_type_new_character_title),
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier =
                                Modifier
                                    .rotate(iconRotation)
                                    .padding(2.dp)
                                    .fillMaxSize(),
                        )

                        DropdownMenu(
                            actionsExpanded,
                            onDismissRequest = { actionsExpanded = false },
                            offset = DpOffset(0.dp, (-15).dp),
                        ) {
                            SenderType.entries.forEach {
                                it.itemOption(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    selectedItem = action,
                                    genre = content.data.genre,
                                    iconSize = 32.dp,
                                ) { selectedAction ->
                                    actionsExpanded = actionsExpanded.not()
                                    if (selectedAction != SenderType.NEW_CHARACTER) {
                                        action = selectedAction
                                    } else {
                                        showNewCharacterSheet = true
                                    }
                                }
                            }
                        }
                    }
                }
            }
            AnimatedVisibility(
                inputField.text.isNotEmpty(),
                enter = scaleIn() + fadeIn(tween(300)),
                exit = scaleOut(animationSpec = tween(500, delayMillis = 100, easing = EaseIn)),
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
                        actionsExpanded = false
                        charactersExpanded = false
                    },
                    modifier =
                        Modifier
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
                                .padding(4.dp)
                                .fillMaxSize(),
                        tint = content.data.genre.iconColor,
                    )
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
            Modifier.size(24.dp),
        ) {
            CharacterAvatar(
                character,
                borderSize = 1.dp,
                genre = saga.genre,
                softFocusRadius = 0f,
                grainRadius = 0.01f,
                innerPadding = 0.dp,
                modifier =
                    Modifier
                        .clip(CircleShape)
                        .fillMaxSize()
                        .clickable {
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

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
)
@Composable
fun ChatInputViewPreview() {
    SagAIScaffold {
        Box(Modifier.fillMaxSize()) {
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
                        ).plus(
                            List(4) {
                                Character(
                                    id = it + 1,
                                    name = "Character ${it + 1}",
                                    backstory = "Character backstory",
                                    image = "image_url",
                                    hexColor = "#567EFF",
                                    sagaId = 0,
                                    details = Details(),
                                )
                            },
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
                isGenerating = true,
                onSendMessage = { _, _ -> },
                onCreateNewCharacter = {},
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
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
