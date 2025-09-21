package com.ilustris.sagai.features.characters.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.narrative.CharacterFormRules
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.getNamePlaceholderResId
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
                value = currentCharacter.description,
                onValueChange = { newBackstory ->
                    if (newBackstory.length <= CharacterFormRules.MAX_BACKSTORY_LENGTH) {
                        syncCharacterState(currentCharacter.copy(description = newBackstory))
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
                currentCharacter.description.isNotEmpty(),
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
