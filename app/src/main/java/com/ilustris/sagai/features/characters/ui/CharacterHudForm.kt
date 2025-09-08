package com.ilustris.sagai.features.characters.ui

// ... (Existing imports - ensure these are present)
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions // Added
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip // Added
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction // Added
import androidx.compose.ui.text.input.KeyboardCapitalization // Added
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.narrative.CharacterFormRules
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.getNamePlaceholderResId
import com.ilustris.sagai.ui.theme.* // Make sure this imports your existing styles

// Enum for managing HUD sections (can be used later if we add more complex navigation)
enum class HUDSection {
    PROFILE,
    BACKSTORY,
    DETAILS,
}

// Helper extension for capitalizing words (from original file)
fun String.capitalizeWords(): String =
    split("_").joinToString(" ") {
        it.lowercase().replaceFirstChar(Char::titlecase)
    }

// HudTextInputRow (from original file - ensure this matches your current version)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HudTextInputRow(
    label: String = "",
    value: String,
    placeHolder: String,
    onValueChange: (String) -> Unit,
    genre: Genre,
    modifier: Modifier = Modifier.fillMaxWidth(),
    maxLines: Int = 1,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier =
        modifier,
        textStyle =
            MaterialTheme.typography.bodySmall.copy(
                color = genre.color,
                fontFamily = genre.bodyFont(),
            ),
        colors = hudTextFieldColors(genre = genre), // Assuming hudTextFieldColors exists
        shape = RoundedCornerShape(genre.cornerSize() / 2),
        maxLines = maxLines,
        singleLine = singleLine,
        label = {
            if (label.isNotEmpty()) {
                Text(
                    label,
                    modifier =
                        Modifier
                            .background(
                                genre.color,
                                RoundedCornerShape(genre.cornerSize() / 2),
                            ).padding(4.dp),
                    style =
                        MaterialTheme.typography.labelMedium.copy(
                            fontFamily = genre.bodyFont(),
                            color = genre.iconColor,
                        ),
                )
            }
        },
        placeholder = {
            Text(
                placeHolder,
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = genre.bodyFont()),
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun hudTextFieldColors(genre: Genre) =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = genre.color,
        unfocusedTextColor = genre.color.copy(alpha = 0.8f),
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        disabledContainerColor = Color.Transparent,
        cursorColor = genre.color,
        focusedBorderColor = genre.color.copy(alpha = 0.5f),
        unfocusedBorderColor = genre.color.copy(alpha = 0.3f),
        focusedLabelColor = genre.color.copy(alpha = 0.7f),
        unfocusedLabelColor = genre.color.copy(alpha = 0.5f),
    )

// Definition for HudCard
@Composable
fun HudCard(
    title: String = "",
    genre: Genre,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(genre.cornerSize()))
                .background(backgroundColor)
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (title.isNotEmpty()) {
            Text(
                text = title.uppercase(),
                style =
                    MaterialTheme.typography.titleSmall.copy(
                        fontFamily = genre.headerFont(),
                        color = genre.color.copy(alpha = 0.8f),
                    ),
            )
        }

        this.content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterHudForm(
    character: Character,
    genre: Genre,
    modifier: Modifier = Modifier,
    onCharacterChange: (Character) -> Unit,
) {
    var currentCharacter by remember(character) {
        mutableStateOf(character)
    }

    fun syncCharacterState(updatedChar: Character) {
        currentCharacter = updatedChar
        onCharacterChange(updatedChar)
    }

    val hudBorderColor = genre.color.copy(alpha = 0.7f)
    val hudScreenBackgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    val cardBackgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val numberOfColumns = 2
    val brush = genre.gradient(true)
    val shape = RoundedCornerShape(genre.cornerSize())
    Column(
        modifier =
            modifier
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row {
            Image(
                painterResource(R.drawable.ic_spark),
                contentDescription = null,
                modifier =
                    Modifier
                        .size(100.dp)
                        .gradientFill(brush),
            )

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
                                textAlign = TextAlign.Start,
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
                        brush = brush,
                        fontFamily = genre.headerFont(),
                        textAlign = TextAlign.Start,
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
                        .weight(1f)
                        .padding(bottom = 16.dp),
            )
        }

        HudCard(
            emptyString(),
            genre,
            backgroundColor = cardBackgroundColor,
        ) {
            val genderOptions =
                LocalContext.current.resources
                    .getStringArray(R.array.character_form_gender_options)
                    .toList()
            LazyRow(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                item {
                    Text(
                        stringResource(R.string.character_form_title_gender),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = genre.bodyFont(),
                    )
                }

                item {
                    VerticalDivider(
                        color = MaterialTheme.colorScheme.onBackground,
                        thickness = 1.dp,
                        modifier =
                            Modifier
                                .padding(horizontal = 12.dp)
                                .height(32.dp),
                    )
                }

                items(genderOptions) {
                    val backgroundColor by animateColorAsState(
                        if (currentCharacter.details.gender ==
                            it
                        ) {
                            genre.color.darker()
                        } else {
                            MaterialTheme.colorScheme.surfaceContainer.darker()
                        },
                    )
                    val textColor by animateColorAsState(
                        if (currentCharacter.details.gender == it) {
                            genre.iconColor
                        } else {
                            MaterialTheme.colorScheme.onBackground
                        },
                    )
                    Text(
                        it,
                        modifier =
                            Modifier
                                .clip(shape)
                                .clickable {
                                    syncCharacterState(
                                        currentCharacter.copy(
                                            details =
                                                currentCharacter.details.copy(
                                                    gender = it,
                                                ),
                                        ),
                                    )
                                }.padding(end = 8.dp)
                                .background(
                                    backgroundColor,
                                    shape = shape,
                                ).padding(8.dp),
                        color = textColor,
                        style =
                            MaterialTheme.typography.labelMedium.copy(
                                fontFamily = genre.bodyFont(),
                            ),
                    )
                }
            }
        }

        HudCard(
            title = "CORE DETAILS",
            genre = genre,
            backgroundColor = cardBackgroundColor,
            modifier = Modifier.animateContentSize(),
        ) {
            HudTextInputRow(
                label = emptyString(),
                placeHolder = stringResource(R.string.character_form_placeholder_backstory),
                value = currentCharacter.backstory,
                onValueChange = { syncCharacterState(currentCharacter.copy(backstory = it)) },
                genre = genre,
                maxLines = 6,
                singleLine = false,
                modifier = Modifier.fillMaxWidth(),
            )

            HudTextInputRow(
                label = stringResource(R.string.character_form_label_occupation),
                placeHolder = stringResource(R.string.character_form_placeholder_occupation),
                value = currentCharacter.details.occupation,
                onValueChange = {
                    syncCharacterState(
                        currentCharacter.copy(
                            details =
                                currentCharacter.details.copy(
                                    occupation = it,
                                ),
                        ),
                    )
                },
                genre = genre,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HudTextInputRow(
                    label = stringResource(R.string.character_form_label_race),
                    placeHolder = stringResource(R.string.character_form_placeholder_race),
                    value = currentCharacter.details.race,
                    onValueChange = {
                        syncCharacterState(
                            currentCharacter.copy(
                                details =
                                    currentCharacter.details.copy(
                                        race = it,
                                    ),
                            ),
                        )
                    },
                    genre = genre,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(.5f),
                )

                HudTextInputRow(
                    label = stringResource(R.string.character_form_label_ethnicity),
                    placeHolder = stringResource(R.string.character_form_placeholder_ethnicity),
                    value = currentCharacter.details.ethnicity,
                    onValueChange = {
                        syncCharacterState(
                            currentCharacter.copy(
                                details =
                                    currentCharacter.details.copy(
                                        ethnicity = it,
                                    ),
                            ),
                        )
                    },
                    genre = genre,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                    )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HudTextInputRow(
                    label = "Height",
                    placeHolder = "1.67",
                    value = currentCharacter.details.height.toString(),
                    onValueChange = {
                        syncCharacterState(
                            currentCharacter.copy(
                                details =
                                    currentCharacter.details.copy(
                                        height = it.toDouble(),
                                    ),
                            ),
                        )
                    },
                    genre = genre,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(.5f),
                )

                HudTextInputRow(
                    label = "Weight",
                    placeHolder = "70.5",
                    value = currentCharacter.details.weight.toString(),
                    onValueChange = {
                        syncCharacterState(
                            currentCharacter.copy(
                                details =
                                    currentCharacter.details.copy(
                                        weight = it.toDouble(),
                                    ),
                            ),
                        )
                    },
                    genre = genre,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

            }

            HudTextInputRow(
                value = currentCharacter.details.personality,
                label = stringResource(R.string.personality_title),
                placeHolder = stringResource(R.string.character_form_placeholder_personality),
                onValueChange = {
                    syncCharacterState(
                        currentCharacter.copy(
                            details =
                                currentCharacter.details.copy(
                                    personality = it,
                                ),
                        ),
                    )
                },
                genre = genre,
                maxLines = 1,
                singleLine = false,
            )

        }

        HudCard(
            title = stringResource(R.string.character_form_title_facial_features),
            genre = genre,
            backgroundColor = cardBackgroundColor,
            modifier =
                Modifier.animateContentSize(),
        ) {

            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                HudTextInputRow(
                    label = stringResource(R.string.character_form_label_eyes),
                    placeHolder = stringResource(R.string.character_form_placeholder_eyes),
                    value = currentCharacter.details.facialDetails.eyes,
                    onValueChange = {
                        syncCharacterState(
                            currentCharacter.copy(
                                details =
                                    currentCharacter.details.copy(
                                        facialDetails =
                                            currentCharacter.details.facialDetails.copy(
                                                eyes = it,
                                            ),
                                    ),
                            ),
                        )
                    },
                    genre = genre,
                    modifier = Modifier.fillMaxWidth(.5f),
                    maxLines = 4,
                    singleLine = false,
                )

                HudTextInputRow(
                    stringResource(R.string.character_form_label_hair),
                    currentCharacter.details.facialDetails.hair,
                    placeHolder = stringResource(R.string.character_form_placeholder_hair),
                    genre = genre,
                    singleLine = false,
                    onValueChange = {
                        syncCharacterState(
                            currentCharacter.copy(
                                details =
                                    currentCharacter.details.copy(
                                        facialDetails =
                                            currentCharacter.details.facialDetails.copy(
                                                hair = it,
                                            ),
                                    ),
                            ),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(.5f)
                )

            }


            Row(modifier = Modifier.fillMaxWidth()) {

                HudTextInputRow(
                    stringResource(R.string.character_form_label_mouth),
                    currentCharacter.details.facialDetails.mouth,
                    placeHolder = stringResource(R.string.character_form_placeholder_mouth),
                    genre = genre,
                    modifier = Modifier.fillMaxWidth(.5f),
                    onValueChange = {
                        syncCharacterState(
                            currentCharacter.copy(
                                details =
                                    currentCharacter.details.copy(
                                        facialDetails =
                                            currentCharacter.details.facialDetails.copy(
                                                mouth = it,
                                            ),
                                    ),
                            ),
                        )
                    },
                )

                HudTextInputRow(
                    stringResource(R.string.character_form_label_scars),
                    currentCharacter.details.facialDetails.scars,
                    placeHolder = stringResource(R.string.character_form_placeholder_scars),
                    genre = genre,
                    modifier = Modifier.weight(1f),
                    singleLine = false,
                    onValueChange = {
                        syncCharacterState(
                            currentCharacter.copy(
                                details =
                                    currentCharacter.details.copy(
                                        facialDetails =
                                            currentCharacter.details.facialDetails.copy(
                                                scars = it,
                                            ),
                                    ),
                            ),
                        )
                    },
                )


            }
        }

        HudCard(
            genre = genre,
            backgroundColor = cardBackgroundColor,
            title = stringResource(R.string.character_form_title_appearance_details),
        ) {

            HudTextInputRow(
                label = stringResource(R.string.character_form_title_appearance_details),
                placeHolder = stringResource(R.string.character_form_placeholder_appearance_details),
                value = currentCharacter.details.appearance,
                onValueChange = {
                    syncCharacterState(
                        currentCharacter.copy(
                            details =
                                currentCharacter.details.copy(
                                    appearance = it,
                                ),
                        ),
                    )
                },
                genre = genre,
                maxLines = 4,
                singleLine = false,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HudTextInputRow(
                    value = currentCharacter.details.clothing.body,
                    label = stringResource(R.string.character_form_label_clothing_body),
                    placeHolder = stringResource(R.string.character_form_placeholder_clothing_body),
                    genre = genre,
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth(.5f),
                    onValueChange = {
                        syncCharacterState(
                            currentCharacter.copy(
                                details =
                                    currentCharacter.details.copy(
                                        clothing =
                                            currentCharacter.details.clothing.copy(
                                                body = it,
                                            ),
                                    ),
                            ),
                        )
                    },
                )

                HudTextInputRow(
                    stringResource(R.string.character_form_label_clothing_footwear),
                    currentCharacter.details.clothing.footwear,
                    placeHolder = stringResource(R.string.character_form_placeholder_clothing_footwear),
                    genre = genre,
                    singleLine = false,
                    onValueChange = {
                        syncCharacterState(
                            currentCharacter.copy(
                                details =
                                    currentCharacter.details.copy(
                                        clothing =
                                            currentCharacter.details.clothing.copy(
                                                footwear = it,
                                            ),
                                    ),
                            ),
                        )
                    },
                )

            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,) {
                HudTextInputRow(
                    stringResource(R.string.character_form_label_clothing_accessories),
                    currentCharacter.details.clothing.accessories,
                    placeHolder = stringResource(R.string.character_form_placeholder_clothing_accessories),
                    genre = genre,
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth(.5f),
                    onValueChange = {
                        syncCharacterState(
                            currentCharacter.copy(
                                details =
                                    currentCharacter.details.copy(
                                        clothing =
                                            currentCharacter.details.clothing.copy(
                                                accessories = it,
                                            ),
                                    ),
                            ),
                        )
                    },
                )

                HudTextInputRow(
                    stringResource(R.string.character_form_label_weapons_gear),
                    currentCharacter.details.weapons,
                    placeHolder = stringResource(R.string.character_form_placeholder_weapons_gear),
                    genre = genre,
                    modifier = Modifier.weight(1f),
                    onValueChange = {
                        syncCharacterState(
                            currentCharacter.copy(
                                details =
                                    currentCharacter.details.copy(
                                        weapons = it,
                                    ),
                            ),
                        )
                    },
                )

            }

        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1E1E, widthDp = 400, heightDp = 700)
@Composable
fun CharacterHudFormPreview_GridSciFi_Corrected() {
    SagAITheme(darkTheme = true) {
        var character by remember {
            mutableStateOf(
                Character(
                    name = "Nyx",
                    details =
                        Details(
                            gender = "Female",
                            race = "Synth",
                            occupation = "Infiltrator",
                            appearance = "Sleek chrome and optical camo.",
                            personality = "Quiet, observant, surprisingly loyal.",
                        ),
                    backstory = "A ghost in the machine, Nyx''s origins are a mystery even to herself. She moves through the neon-drenched cityscapes like a whisper.",
                ),
            )
        }
        CharacterHudForm(
            character = character,
            genre = Genre.SCI_FI,
            onCharacterChange = { character = it },
        )
    }
}
