package com.ilustris.sagai.features.characters.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.core.narrative.CharacterFormRules
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.Clothing
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.data.model.FacialFeatures
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.data.model.getNamePlaceholderResId
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.CharacterInfo
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.headerFont

// Generic FormSection composable
@Composable
fun FormSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        content()
    }
}

@Composable
fun CharacterForm(
    sagaForm: SagaForm,
    modifier: Modifier = Modifier,
    onUpdateCharacter: (Character) -> Unit,
) {
    var currentCharacter by remember(sagaForm.character) {
        mutableStateOf(sagaForm.character)
    }

    fun syncCharacterState(updatedChar: Character) {
        currentCharacter = updatedChar
        onUpdateCharacter(updatedChar)
    }

    val scrollState = rememberScrollState()
    val brush = sagaForm.genre.gradient()
    val textFieldColors =
        TextFieldDefaults.colors(
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
            disabledContainerColor = Color.Transparent,
        )
    val commonTextStyle = MaterialTheme.typography.bodyLarge.copy(brush = brush)
    val placeholderAlpha = .3f
    val commonModifier =
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(bottom = 12.dp)
    val commonKeyboardOptions =
        KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Next,
        )

    val selectedChipLabelTextStyle = commonTextStyle
    val unselectedChipLabelTextStyle =
        MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )

    LaunchedEffect(sagaForm.character.image) {
        if (sagaForm.character.image.isNotEmpty()) {
            scrollState.scrollTo(0)
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .verticalScroll(scrollState),
    ) {
        AnimatedContent(sagaForm.character.image) {
            if (it.isNotEmpty()) {
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Box(Modifier.size(0.dp))
            }
        }

        OutlinedTextField(
            value = currentCharacter.name,
            onValueChange = { newName ->
                if (newName.length <= CharacterFormRules.MAX_NAME_LENGTH) {
                    syncCharacterState(currentCharacter.copy(name = newName))
                }
            },
            label = {
                Text(
                    stringResource(R.string.character_form_label_name),
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            textAlign = TextAlign.Center,
                            fontFamily = sagaForm.genre.bodyFont(),
                            color = sagaForm.genre.color,
                        ),
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            placeholder = { Text(stringResource(id = sagaForm.genre.getNamePlaceholderResId())) },
            singleLine = true,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    capitalization = KeyboardCapitalization.Words,
                ),
            textStyle =
                MaterialTheme.typography.headlineLarge.copy(
                    brush = brush,
                    fontFamily = sagaForm.genre.headerFont(),
                    textAlign = TextAlign.Center,
                ),
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = sagaForm.genre.color,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
        )

        FormSection(title = stringResource(R.string.character_form_title_gender)) {
            val genderOptions =
                LocalContext.current.resources
                    .getStringArray(R.array.character_form_gender_options)
                    .toList()
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items(genderOptions.size) { index ->
                    val gender = genderOptions[index]
                    FilterChip(
                        selected = currentCharacter.details.gender == gender,
                        onClick = {
                            syncCharacterState(
                                currentCharacter.copy(
                                    details =
                                        currentCharacter.details.copy(
                                            gender = gender,
                                        ),
                                ),
                            )
                        },
                        label = {
                            Text(
                                text = gender,
                                style =
                                    if (currentCharacter.details.gender ==
                                        gender
                                    ) {
                                        selectedChipLabelTextStyle
                                    } else {
                                        unselectedChipLabelTextStyle
                                    },
                            )
                        },
                        colors =
                            FilterChipDefaults.filterChipColors(
                                containerColor = Color.Transparent,
                                selectedContainerColor = sagaForm.genre.color.copy(alpha = 0.15f),
                            ),
                        shape = RoundedCornerShape(sagaForm.genre.cornerSize()),
                        border =
                            FilterChipDefaults.filterChipBorder(
                                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                selectedBorderColor = sagaForm.genre.color.copy(alpha = 0.7f),
                                borderWidth = 1.dp,
                                selectedBorderWidth = 1.dp,
                                enabled = true,
                                selected = currentCharacter.details.gender == gender,
                            ),
                    )
                }
            }
        }

        FormSection(title = stringResource(R.string.character_form_title_backstory)) {
            TextField(
                value = currentCharacter.backstory,
                onValueChange = { newBackstory ->
                    if (newBackstory.length <= CharacterFormRules.MAX_BACKSTORY_LENGTH) {
                        syncCharacterState(currentCharacter.copy(backstory = newBackstory))
                    }
                },
                placeholder = {
                    Text(
                        stringResource(R.string.character_form_placeholder_backstory),
                        modifier = Modifier.alpha(placeholderAlpha),
                    )
                },
                maxLines = 5,
                colors = textFieldColors,
                textStyle = commonTextStyle,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(bottom = 12.dp),
                keyboardOptions = commonKeyboardOptions.copy(imeAction = ImeAction.Next),
            )
        }

        FormSection(title = stringResource(R.string.personality_title)) {
            TextField(
                value = currentCharacter.details.personality,
                onValueChange = { newPersonality ->
                    if (newPersonality.length <= CharacterFormRules.MAX_PERSONALITY_LENGTH) {
                        syncCharacterState(
                            currentCharacter.copy(
                                details =
                                    currentCharacter.details.copy(
                                        personality = newPersonality,
                                    ),
                            ),
                        )
                    }
                },
                placeholder = {
                    Text(
                        stringResource(R.string.character_form_placeholder_personality),
                        modifier = Modifier.alpha(placeholderAlpha),
                    )
                },
                maxLines = 5,
                colors = textFieldColors,
                textStyle = commonTextStyle,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(bottom = 12.dp),
                keyboardOptions = commonKeyboardOptions.copy(imeAction = ImeAction.Next),
            )
        }

        FormSection(title = stringResource(R.string.character_form_title_quick_details)) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextField(
                    value = currentCharacter.details.occupation,
                    onValueChange = { newOccupation ->
                        if (newOccupation.length <= CharacterFormRules.MAX_OCCUPATION_LENGTH) {
                            syncCharacterState(
                                currentCharacter.copy(
                                    details =
                                        currentCharacter.details.copy(
                                            occupation = newOccupation,
                                        ),
                                ),
                            )
                        }
                    },
                    label = {
                        Text(
                            stringResource(R.string.character_form_label_occupation),
                            modifier = Modifier.padding(vertical = 8.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = sagaForm.genre.color,
                        )
                    },
                    placeholder = {
                        Text(
                            stringResource(R.string.character_form_placeholder_occupation),
                            modifier = Modifier.alpha(placeholderAlpha),
                        )
                    },
                    colors = textFieldColors,
                    textStyle = commonTextStyle,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = commonKeyboardOptions.copy(capitalization = KeyboardCapitalization.Sentences),
                    maxLines = 2,
                )
                TextField(
                    value = currentCharacter.details.ethnicity,
                    onValueChange = { newEthnicity ->
                        if (newEthnicity.length <= CharacterFormRules.MAX_ETHNICITY_LENGTH) {
                            syncCharacterState(
                                currentCharacter.copy(
                                    details =
                                        currentCharacter.details.copy(
                                            ethnicity = newEthnicity,
                                        ),
                                ),
                            )
                        }
                    },
                    label = {
                        Text(
                            stringResource(R.string.character_form_label_ethnicity),
                            modifier = Modifier.padding(vertical = 8.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = sagaForm.genre.color,
                        )
                    },
                    placeholder = {
                        Text(
                            stringResource(R.string.character_form_placeholder_ethnicity),
                            modifier = Modifier.alpha(placeholderAlpha),
                        )
                    },
                    colors = textFieldColors,
                    textStyle = commonTextStyle,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = commonKeyboardOptions.copy(capitalization = KeyboardCapitalization.Words),
                    maxLines = 2,
                )
            }

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextField(
                    value = currentCharacter.details.race,
                    onValueChange = { newRace ->
                        if (newRace.length <= CharacterFormRules.MAX_RACE_LENGTH) {
                            syncCharacterState(
                                currentCharacter.copy(
                                    details =
                                        currentCharacter.details.copy(
                                            race = newRace,
                                        ),
                                ),
                            )
                        }
                    },
                    label = {
                        Text(
                            stringResource(R.string.character_form_label_race),
                            modifier = Modifier.padding(vertical = 8.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = sagaForm.genre.color,
                        )
                    },
                    placeholder = {
                        Text(
                            stringResource(R.string.character_form_placeholder_race),
                            modifier = Modifier.alpha(placeholderAlpha),
                        )
                    },
                    colors = textFieldColors,
                    textStyle = commonTextStyle,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = commonKeyboardOptions.copy(capitalization = KeyboardCapitalization.Words),
                    maxLines = 2,
                )
            }

            TextField(
                value = currentCharacter.details.weapons,
                onValueChange = { newWeapons ->
                    if (newWeapons.length <= CharacterFormRules.MAX_WEAPONS_LENGTH) {
                        syncCharacterState(
                            currentCharacter.copy(
                                details =
                                    currentCharacter.details.copy(
                                        weapons = newWeapons,
                                    ),
                            ),
                        )
                    }
                },
                label = {
                    Text(
                        stringResource(R.string.character_form_label_weapons_gear),
                        modifier = Modifier.padding(vertical = 8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = sagaForm.genre.color,
                    )
                },
                placeholder = {
                    Text(
                        stringResource(R.string.character_form_placeholder_weapons_gear),
                        modifier = Modifier.alpha(placeholderAlpha),
                    )
                },
                colors = textFieldColors,
                textStyle = commonTextStyle,
                maxLines = 5,
                modifier = commonModifier,
                keyboardOptions = commonKeyboardOptions,
            )
        }

        FormSection(title = stringResource(R.string.character_form_title_appearance_details)) {
            TextField(
                value = currentCharacter.details.appearance,
                onValueChange = { newAppearance ->
                    if (newAppearance.length <= CharacterFormRules.MAX_APPEARANCE_LENGTH) {
                        syncCharacterState(
                            currentCharacter.copy(
                                details =
                                    currentCharacter.details.copy(
                                        appearance = newAppearance,
                                    ),
                            ),
                        )
                    }
                },
                placeholder = {
                    Text(
                        stringResource(R.string.character_form_placeholder_appearance_details),
                        modifier = Modifier.alpha(placeholderAlpha),
                    )
                },
                colors = textFieldColors,
                textStyle = commonTextStyle,
                maxLines = 5,
                modifier = commonModifier,
                keyboardOptions = commonKeyboardOptions,
            )
        }

        FormSection(title = stringResource(R.string.character_form_title_facial_features)) {
            TextField(
                value = currentCharacter.details.facialDetails.hair,
                onValueChange = { newHair ->
                    if (newHair.length <= CharacterFormRules.MAX_HAIR_LENGTH) {
                        syncCharacterState(
                            currentCharacter.copy(
                                details =
                                    currentCharacter.details.copy(
                                        facialDetails = currentCharacter.details.facialDetails.copy(hair = newHair),
                                    ),
                            ),
                        )
                    }
                },
                label = { Text(stringResource(R.string.character_form_label_hair), color = sagaForm.genre.color) },
                placeholder = { Text(stringResource(R.string.character_form_placeholder_hair)) },
                colors = textFieldColors,
                textStyle = commonTextStyle,
                maxLines = 3,
                modifier = commonModifier,
                keyboardOptions = commonKeyboardOptions,
            )
            TextField(
                value = currentCharacter.details.facialDetails.eyes,
                onValueChange = { newEyes ->
                    if (newEyes.length <= CharacterFormRules.MAX_EYES_LENGTH) {
                        syncCharacterState(
                            currentCharacter.copy(
                                details =
                                    currentCharacter.details.copy(
                                        facialDetails = currentCharacter.details.facialDetails.copy(eyes = newEyes),
                                    ),
                            ),
                        )
                    }
                },
                label = { Text(stringResource(R.string.character_form_label_eyes), color = sagaForm.genre.color) },
                placeholder = { Text(stringResource(R.string.character_form_placeholder_eyes)) },
                colors = textFieldColors,
                textStyle = commonTextStyle,
                maxLines = 3,
                modifier = commonModifier,
                keyboardOptions = commonKeyboardOptions,
            )
            TextField(
                value = currentCharacter.details.facialDetails.mouth,
                onValueChange = { newMouth ->
                    if (newMouth.length <= CharacterFormRules.MAX_MOUTH_LENGTH) {
                        syncCharacterState(
                            currentCharacter.copy(
                                details =
                                    currentCharacter.details.copy(
                                        facialDetails = currentCharacter.details.facialDetails.copy(mouth = newMouth),
                                    ),
                            ),
                        )
                    }
                },
                label = { Text(stringResource(R.string.character_form_label_mouth), color = sagaForm.genre.color) },
                placeholder = { Text(stringResource(R.string.character_form_placeholder_mouth)) },
                colors = textFieldColors,
                textStyle = commonTextStyle,
                maxLines = 3,
                modifier = commonModifier,
                keyboardOptions = commonKeyboardOptions,
            )
            TextField(
                value = currentCharacter.details.facialDetails.scars,
                onValueChange = { newScars ->
                    if (newScars.length <= CharacterFormRules.MAX_SCARS_LENGTH) {
                        syncCharacterState(
                            currentCharacter.copy(
                                details =
                                    currentCharacter.details.copy(
                                        facialDetails = currentCharacter.details.facialDetails.copy(scars = newScars),
                                    ),
                            ),
                        )
                    }
                },
                label = { Text(stringResource(R.string.character_form_label_scars), color = sagaForm.genre.color) },
                placeholder = { Text(stringResource(R.string.character_form_placeholder_scars)) },
                colors = textFieldColors,
                textStyle = commonTextStyle,
                maxLines = 3,
                modifier = commonModifier,
                keyboardOptions = commonKeyboardOptions,
            )
        }

        FormSection(title = stringResource(R.string.character_form_title_clothing_attire)) {
            TextField(
                value = currentCharacter.details.clothing.body,
                onValueChange = { newBody ->
                    if (newBody.length <= CharacterFormRules.MAX_CLOTHING_BODY_LENGTH) {
                        syncCharacterState(
                            currentCharacter.copy(
                                details =
                                    currentCharacter.details.copy(
                                        clothing = currentCharacter.details.clothing.copy(body = newBody),
                                    ),
                            ),
                        )
                    }
                },
                label = { Text(stringResource(R.string.character_form_label_clothing_body), color = sagaForm.genre.color) },
                placeholder = { Text(stringResource(R.string.character_form_placeholder_clothing_body)) },
                colors = textFieldColors,
                textStyle = commonTextStyle,
                maxLines = 3,
                modifier = commonModifier,
                keyboardOptions = commonKeyboardOptions,
            )
            TextField(
                value = currentCharacter.details.clothing.accessories,
                onValueChange = { newAccessories ->
                    if (newAccessories.length <= CharacterFormRules.MAX_CLOTHING_ACCESSORIES_LENGTH) {
                        syncCharacterState(
                            currentCharacter.copy(
                                details =
                                    currentCharacter.details.copy(
                                        clothing = currentCharacter.details.clothing.copy(accessories = newAccessories),
                                    ),
                            ),
                        )
                    }
                },
                label = { Text(stringResource(R.string.character_form_label_clothing_accessories), color = sagaForm.genre.color) },
                placeholder = { Text(stringResource(R.string.character_form_placeholder_clothing_accessories)) },
                colors = textFieldColors,
                textStyle = commonTextStyle,
                maxLines = 3,
                modifier = commonModifier,
                keyboardOptions = commonKeyboardOptions,
            )
            TextField(
                value = currentCharacter.details.clothing.footwear,
                onValueChange = { newFootwear ->
                    if (newFootwear.length <= CharacterFormRules.MAX_CLOTHING_FOOTWEAR_LENGTH) {
                        syncCharacterState(
                            currentCharacter.copy(
                                details =
                                    currentCharacter.details.copy(
                                        clothing = currentCharacter.details.clothing.copy(footwear = newFootwear),
                                    ),
                            ),
                        )
                    }
                },
                label = { Text(stringResource(R.string.character_form_label_clothing_footwear), color = sagaForm.genre.color) },
                placeholder = { Text(stringResource(R.string.character_form_placeholder_clothing_footwear)) },
                colors = textFieldColors,
                textStyle = commonTextStyle,
                maxLines = 3,
                modifier = commonModifier,
                keyboardOptions = commonKeyboardOptions.copy(imeAction = ImeAction.Done),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun SimpleCharacterForm(
    genre: Genre,
    modifier: Modifier = Modifier,
    onCreateCharacter: (CharacterInfo) -> Unit,
) {
    var currentCharacter by remember { mutableStateOf(CharacterInfo()) }

    fun syncCharacterState(updatedChar: CharacterInfo) {
        currentCharacter = updatedChar
    }

    val brush = genre.gradient()

    val textFieldColors =
        TextFieldDefaults.colors(
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
            disabledContainerColor = Color.Transparent,
        )
    val commonTextStyle = MaterialTheme.typography.bodyLarge.copy(brush = brush)

    Column(modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = currentCharacter.name,
            onValueChange = { newName ->
                if (newName.length <= CharacterFormRules.MAX_NAME_LENGTH) {
                    syncCharacterState(currentCharacter.copy(name = newName))
                }
            },
            label = {
                Text(
                    stringResource(R.string.character_form_label_name),
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            textAlign = TextAlign.Center,
                            fontFamily = genre.bodyFont(),
                            color = genre.color,
                        ),
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            placeholder = { Text(stringResource(id = genre.getNamePlaceholderResId())) },
            singleLine = true,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    capitalization = KeyboardCapitalization.Words,
                ),
            textStyle =
                MaterialTheme.typography.headlineLarge.copy(
                    brush = genre.gradient(true),
                    fontFamily = genre.headerFont(),
                    textAlign = TextAlign.Center,
                ),
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = genre.color,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
        )

        FormSection(title = stringResource(R.string.character_form_title_gender)) {
            val genderOptions =
                LocalContext.current.resources
                    .getStringArray(R.array.character_form_gender_options)
                    .toList()
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items(genderOptions.size) { index ->
                    val gender = genderOptions[index]
                    val isSelected = currentCharacter.gender == gender
                    val textColor by animateColorAsState(
                        if (isSelected) genre.iconColor else MaterialTheme.colorScheme.onBackground,
                    )
                    val backgroundColor by animateColorAsState(
                        if (isSelected) genre.color.copy(alpha = 0.15f) else Color.Transparent,
                    )
                    FilterChip(
                        selected = currentCharacter.gender == gender,
                        onClick = {
                            syncCharacterState(
                                currentCharacter.copy(
                                    gender = gender,
                                ),
                            )
                        },
                        label = {
                            Text(
                                text = gender,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                        colors =
                            FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.background.copy(alpha = .5f),
                                selectedContainerColor = genre.color,
                                selectedLabelColor = genre.iconColor,
                                labelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f),
                            ),
                        shape = RoundedCornerShape(genre.cornerSize()),
                        border =
                            FilterChipDefaults.filterChipBorder(
                                borderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = .3f),
                                selectedBorderColor = genre.iconColor,
                                borderWidth = 1.dp,
                                selectedBorderWidth = 1.dp,
                                enabled = true,
                                selected = isSelected,
                            ),
                    )
                }
            }
        }

        FormSection(title = stringResource(R.string.character_form_title_backstory)) {
            TextField(
                value = currentCharacter.briefDescription,
                onValueChange = { newBackstory ->
                    if (newBackstory.length <= CharacterFormRules.MAX_BACKSTORY_LENGTH) {
                        syncCharacterState(currentCharacter.copy(briefDescription = newBackstory))
                    }
                },
                placeholder = {
                    Text(
                        stringResource(R.string.character_form_placeholder_backstory),
                        modifier = Modifier.alpha(.5f),
                    )
                },
                maxLines = 5,
                colors = textFieldColors,
                textStyle = commonTextStyle,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(bottom = 12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            )
        }

        AnimatedVisibility(
            currentCharacter.name.isNotEmpty() &&
                currentCharacter.gender.isNotEmpty() &&
                currentCharacter.briefDescription.isNotEmpty(),
        ) {
            Button(
                onClick = {
                    onCreateCharacter(currentCharacter)
                },
                shape = RoundedCornerShape(genre.cornerSize()),
                colors =
                    ButtonDefaults.buttonColors().copy(
                        containerColor = Color.Transparent,
                        contentColor = genre.iconColor,
                    ),
                modifier =
                    Modifier
                        .border(
                            1.dp,
                            genre.gradient(),
                            RoundedCornerShape(genre.cornerSize()),
                        ).background(
                            genre.color,
                            RoundedCornerShape(genre.cornerSize()),
                        ).fillMaxWidth(),
            ) {
                Text(stringResource(R.string.save_saga)) // Using "Save" as a general create/save action
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CharacterFormPreview() {
    val sampleCharacterDetails =
        Details(
            appearance = "Slender and agile, with long silver hair often tied back. Her movements are fluid and quiet.",
            facialDetails =
                FacialFeatures(
                    hair = "Long silver hair, often tied back in a practical braid.",
                    eyes = "Sharp, intelligent green eyes that miss little. Almost feline in their intensity.",
                    mouth = "Usually a calm, focused expression. Thin lips.",
                    scars = "A faint, old scar above her left eyebrow, barely visible.",
                ),
            clothing =
                Clothing(
                    body = "Wears practical, dark leather armor, well-maintained but showing signs of use. A dark green tunic underneath.",
                    accessories = "A hooded cloak for stealth, and a simple leather belt with a few pouches for herbs and tools. No flashy jewelry.",
                    footwear = "Soft, sturdy leather boots that allow for silent movement.",
                ),
            occupation = "Forest Warden / Scout",
            race = "Wood Elf",
            weapons = "A finely crafted longbow and a set of daggers. Carries a small satchel with herbs and survival gear.",
            personality = "Reserved and cautious with strangers, but kind-hearted and fiercely protective of nature and those she considers friends. Values actions over words.",
            height = 1.7,
            weight = 60.0,
            gender = "Feminine",
            ethnicity = "Elven",
        )

    val sampleCharacter =
        Character(
            id = 0,
            name = "Elara Moonwhisper",
            backstory = "Orphaned at a young age, Elara was raised by the reclusive guardians of the Silverwood. She learned the ways of the forest and dedicated her life to protecting its secrets from those who would exploit them. A recent encroaching darkness has forced her to seek allies beyond her homeland.",
            details = sampleCharacterDetails,
            sagaId = 0,
            image = "",
            hexColor = "#3d98f7",
            joinedAt = 0L,
        )
    val sampleSagaForm =
        SagaForm(
            title = "The Silverwood Guardians",
            genre = Genre.FANTASY,
            description = "An epic tale of courage and magic.",
            character = sampleCharacter,
        )

    SagAIScaffold {
        CharacterForm(sagaForm = sampleSagaForm) { updatedCharacter ->
            println(
                "Character updated in preview: ${updatedCharacter.name}, Gender: ${updatedCharacter.details.gender}, Race: ${updatedCharacter.details.race}",
            )
        }
    }
}
