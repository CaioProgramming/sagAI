package com.ilustris.sagai.features.characters.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ilustris.sagai.core.data.State
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.presentation.CharacterViewModel
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.SagaTopBar
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.holographicGradient
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterGalleryView(
    navController: NavHostController,
    sagaId: String?,
    characterViewModel: CharacterViewModel = hiltViewModel(),
) {
    val saga by characterViewModel.saga.collectAsState()
    val state by characterViewModel.state.collectAsState()

    LaunchedEffect(saga) {
        if (saga == null) {
            characterViewModel.loadCharacters(sagaId?.toInt())
        }
    }

    val showCharacterDialog =
        remember {
            mutableStateOf<Character?>(null)
        }

    CharactersGalleryContent(
        saga,
        state,
        onSelectCharacter = {
            showCharacterDialog.value = it
        },
        onBackClick = {
            navController.popBackStack()
        },
    )
    showCharacterDialog.value?.let {
        CharacterDetailsDialog(character = it, saga?.saga?.genre ?: Genre.FANTASY) {
            showCharacterDialog.value = null
        }
    }
}

@Composable
fun CharactersGalleryContent(
    content: SagaContent?,
    state: State,
    onSelectCharacter: (Character) -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    AnimatedContent(state) {
        when (it) {
            is State.Success -> {
                content?.let { saga ->
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(8.dp),
                    ) {
                        stickyHeader {
                            SagaTopBar(
                                "Elenco de ${saga.saga.title}",
                                "${saga.characters.size} Personagens",
                                saga.saga.genre,
                                onBackClick = onBackClick,
                                modifier =
                                    Modifier
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(top = 25.dp),
                            )
                        }
                        items(saga.characters, key = { character -> character.id }) { character ->
                            CharacterYearbookItem(
                                character = character,
                                character.id == saga.mainCharacter?.id,
                                saga.saga.genre,
                                modifier =
                                    Modifier.clickable {
                                        onSelectCharacter(character)
                                    },
                            )
                        }
                    }
                }
            }

            else ->
                Box {
                    SparkIcon(
                        brush =
                            gradientAnimation(
                                content?.saga?.genre?.gradient() ?: holographicGradient,
                            ),
                        modifier =
                            Modifier
                                .size(50.dp)
                                .align(Alignment.Center),
                    )
                }
        }
    }
}

@Composable
fun CharacterYearbookItem(
    character: Character,
    isMainCharacter: Boolean,
    genre: Genre,
    modifier: Modifier = Modifier,
) {
    CharacterVerticalItem(modifier, character, genre, imageSize = 100.dp)
}

@Composable
private fun CharacterVerticalItem(
    modifier: Modifier,
    character: Character,
    genre: Genre,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    borderSize: Dp = 2.dp,
    borderColor: Color? = null,
    imageSize: Dp,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            modifier
                .padding(8.dp),
    ) {
        CharacterAvatar(
            character = character,
            borderColor = borderColor,
            borderSize = borderSize,
            textStyle =
                MaterialTheme.typography.titleLarge.copy(
                    fontFamily = genre.headerFont(),
                ),
            modifier =
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(imageSize)
                    .aspectRatio(1f), // Makes it a square
        )
        Text(
            text = character.name,
            style =
                style.copy(
                    fontFamily = genre.bodyFont(),
                ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}



@Preview
@Composable
fun CharactersGalleryContentPreview() {
    val sagaContent = SagaContent(
        saga = SagaData(id = 0, title = "Saga Title", description = "Saga Description", genre = Genre.FANTASY),
        mainCharacter = Character(id = 1, name = "Main Character", details = Details(), sagaId = 0),
        messages = emptyList(),
        chapters = emptyList(),
        characters = listOf(
            Character(id = 1, name = "Character 1", details = Details(), sagaId = 0),
            Character(id = 2, name = "Character 2", details = Details(), sagaId = 0)
        )
    )
    CharactersGalleryContent(content = sagaContent, state = State.Success(sagaContent))
}

@Preview
@Composable
fun CharacterYearbookItemPreview() {
    val character = Character(
        id = 1,
        name = "Character Name",
        backstory = "Character backstory",
        image = "",
        hexColor = "#FF0000",
        sagaId = 1,
        details = Details(
            appearance = "Appearance",
            personality = "Personality",
            race = "Race",
            height = 1.80,
            weight = 70.0,
            style = "Style",
            gender = "Gender",
            occupation = "Occupation",
            ethnicity = "Ethnicity"
        ),
        joinedAt = System.currentTimeMillis()
    )
    CharacterYearbookItem(character = character, isMainCharacter = true, genre = Genre.FANTASY)
}

@Preview
@Composable
fun CharacterVerticalItemPreview() {
    val character = Character(
        id = 1,
        name = "Character Name",
        backstory = "Character backstory",
        image = "",
        hexColor = "#FF0000",
        sagaId = 1,
        details = Details(
            appearance = "Appearance",
            personality = "Personality",
            race = "Race",
            height = 1.80,
            weight = 70.0,
            style = "Style",
            gender = "Gender",
            occupation = "Occupation",
            ethnicity = "Ethnicity"
        ),
        joinedAt = System.currentTimeMillis()
    )
    CharacterVerticalItem(modifier = Modifier, character = character, genre = Genre.FANTASY, imageSize = 100.dp)
}

@Preview
@Composable
fun CharacterHorizontalViewPreview() {
    val character = Character(
        id = 1,
        name = "Character Name",
        backstory = "Character Backstory",
        image = "",
        hexColor = "#3d98f7",
        sagaId = 1,
        details = Details(
            appearance = "Appearance",
            personality = "Personality",
            race = "Race"
        ),
        joinedAt = 0L
    )
    CharacterHorizontalView(character = character, genre = Genre.FANTASY)
}
@Composable
fun CharacterHorizontalView(
    modifier: Modifier = Modifier,
    character: Character,
    genre: Genre,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    borderSize: Dp = 2.dp,
    borderColor: Color? = null,
    imageSize: Dp = 50.dp,
) {
    Row(
        modifier = modifier
            .padding(8.dp),
       verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CharacterAvatar(
            character = character,
            borderColor = borderColor,
            borderSize = borderSize,
            textStyle = MaterialTheme.typography.titleLarge.copy(
                fontFamily = genre.headerFont(),
            ),
            modifier = Modifier
                .size(imageSize)
                .aspectRatio(1f)
        )

        Text(
            text = character.name,
            style = style.copy(
                fontFamily = genre.bodyFont(),
                fontWeight = FontWeight.W700,
                color = Color(character.hexColor.toColorInt())
            ),
            textAlign = TextAlign.Start,
            modifier = Modifier.align(Alignment.CenterVertically).weight(1f)
        )
    }
}


