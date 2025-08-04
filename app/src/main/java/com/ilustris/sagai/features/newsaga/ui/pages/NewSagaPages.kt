package com.ilustris.sagai.features.newsaga.ui.pages

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.Clothing
import com.ilustris.sagai.features.characters.data.model.Details // Ensure Details is imported
import com.ilustris.sagai.features.characters.data.model.FacialFeatures
import com.ilustris.sagai.features.characters.ui.CharacterForm
import com.ilustris.sagai.features.characters.ui.CharacterHudForm
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.ui.components.GenreCard
import com.ilustris.sagai.features.newsaga.ui.components.SagaCard
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.holographicGradient
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

enum class NewSagaPages(
    @StringRes val title: Int? = null,
    @StringRes val subtitle: Int? = null,
    val content: @Composable (((Any?) -> Unit), Any?) -> Unit = { _, _ -> },
) {
    TITLE(
        R.string.start_saga,
        R.string.start_saga_subtitle,
        content = { onSendData, data ->
            TitlePageView(data as String) {
                onSendData(it)
            }
        },
    ),
    GENRE(
        R.string.saga_genre,
        R.string.saga_genre_subtitle,
        content = { onSendData, data ->
            GenresPageView(data as Genre) {
                onSendData(it)
            }
        },
    ),
    DESCRIPTION(
        R.string.saga_description,
        R.string.saga_description_subtitle,
        content = { onSendData, data ->
            DescriptionPageView(data as String, stringResource(R.string.saga_description_hint)) {
                onSendData(it)
            }
        },
    ),

    CHARACTER(
        R.string.saga_character_description,
        R.string.saga_character_description_subtitle,
        content = { onSendData, data ->
            val form = data as SagaForm
            CharacterHudForm(character = form.character, form.genre , onCharacterChange = { character ->
                onSendData(character)
            })
        },
    ),
}

@Composable
fun TitlePageView(
    title: String,
    onSendTitle: (String) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
    ) {
        var input by remember { mutableStateOf(title) }

        val isValidTitle = input.isNotEmpty() && input.length <= 30
        val brush = Brush.linearGradient(holographicGradient)

        TextField(
            value = input,
            onValueChange = {
                input = it
                onSendTitle(input)
            },
            colors =
                TextFieldDefaults.colors(
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                ),
            maxLines = 3,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Sentences,
                    autoCorrect = true,
                ),
            keyboardActions =
                KeyboardActions(
                    onSend = {
                        if (isValidTitle) onSendTitle(input)
                    },
                ),
            placeholder = {
                Text(
                    style =
                        MaterialTheme.typography.displayMedium.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                        ),
                    text = stringResource(R.string.saga_title_hint),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .alpha(.5f),
                )
            },
            textStyle =
                MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    brush = brush,
                ),
            modifier = Modifier.wrapContentSize(),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TitlePageViewPreview() {
    SagAIScaffold {
        TitlePageView(title = "My Awesome Saga") {
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GenresPageViewPreview() {
    SagAIScaffold {
        GenresPageView(initialGenre = Genre.FANTASY) {
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DescriptionPageViewPreview() {
    SagAIScaffold {
        DescriptionPageView(
            description = "This is a great saga about a hero.",
            placeHolder = "Describe your saga...",
        ) {
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SagaCardPreview() {
    SagAIScaffold {
        Column {
            SagaCard(
                modifier =
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .fillMaxHeight(.8f),
                saga =
                    Saga(
                        0,
                        "My Awesome Saga",
                        "This is a great saga about a hero.",
                        "",
                        0L,
                        Genre.FANTASY,
                        mainCharacterId = 0,
                    ),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GenresPageView(
    initialGenre: Genre,
    onSelectGenre: (Genre) -> Unit,
) {
    val genres = Genre.entries
    val pagerState =
        rememberPagerState(
            initialPage = genres.indexOf(initialGenre).coerceAtLeast(0),
            pageCount = { genres.size },
        )
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        val selectedGenre = genres[pagerState.currentPage]
        onSelectGenre(selectedGenre)
    }

    HorizontalPager(
        state = pagerState,
        modifier =
            Modifier
                .fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 64.dp),
        pageSpacing = 8.dp,
    ) { pageIndex ->
        val genre = genres[pageIndex]
        val pageOffset = pagerState.currentPageOffsetFraction

        GenreCard(
            genre = genre,
            isSelected = pagerState.currentPage == pageIndex,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .graphicsLayer {
                        val scale = lerp(0.80f, 1f, 1f - pageOffset.absoluteValue.coerceIn(0f, 1f))
                        scaleX = scale
                        scaleY = scale
                        alpha = lerp(0.6f, 1f, 1f - pageOffset.absoluteValue.coerceIn(0f, 1f))
                    },
            onClick = {
                if (pagerState.currentPage != pageIndex) {
                    scope.launch {
                        pagerState.animateScrollToPage(pageIndex)
                    }
                }
            },
        )
    }
}

@Composable
fun DescriptionPageView(
    description: String,
    placeHolder: String,
    onSendDescription: (String) -> Unit,
) {
    var input by remember { mutableStateOf(description) }
    val isValidDescription = input.isNotEmpty() && input.length <= 300
    val brush = Brush.linearGradient(holographicGradient)

    LaunchedEffect(description) {
        if (input != description) {
            onSendDescription(input)
        }
    }

    TextField(
        value = input,
        onValueChange = {
            input = it
            if (it.length <= 300) {
                onSendDescription(it)
            }
        },
        colors =
            TextFieldDefaults.colors(
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
            ),
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
                capitalization = KeyboardCapitalization.Sentences,
                autoCorrect = true,
            ),
        keyboardActions =
            KeyboardActions(
                onDone = {
                    if (isValidDescription) onSendDescription(input)
                },
            ),
        placeholder = {
            Text(
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Medium,
                    ),
                text = placeHolder,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .alpha(.5f),
            )
        },
        textStyle =
            MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Start,
                brush = brush,
            ),
        modifier =
            Modifier
                .padding(16.dp)
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    shape = RoundedCornerShape(12.dp),
                    brush = Brush.verticalGradient(holographicGradient),
                ).background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    RoundedCornerShape(12.dp),
                ).padding(8.dp),
    )
}

@Preview(showBackground = true)
@Composable
fun CharacterFormPreview() {
    val sampleCharacterDetails =
        Details(
            appearance = "Slender and agile, with long silver hair often tied back. Her movements are fluid and quiet.",
            facialDetails = FacialFeatures(),
            clothing = Clothing(body = "Wears practical, dark leather armor, a hooded cloak for stealth, and soft boots."),
            occupation = "Forest Warden / Scout",
            race = "Wood Elf", // Added race to preview
            weapons = "A finely crafted longbow and a set of daggers. Carries a small satchel with herbs and survival gear.",
            personality = "Reserved but kind",
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
        CharacterHudForm(
            sampleSagaForm.character,
            sampleSagaForm.genre,
        ) { updatedCharacter ->
            println(
                "Character updated in preview: ${updatedCharacter.name}, Gender: ${updatedCharacter.details.gender}, Race: ${updatedCharacter.details.race}",
            )
        }
    }
}
