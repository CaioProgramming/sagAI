package com.ilustris.sagai.features.characters.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.ilustris.sagai.core.data.State
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.presentation.CharacterViewModel
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.SagaTopBar
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.holographicGradient

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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            modifier
                .padding(8.dp),
    ) {
        val characterColor = Color(character.hexColor.toColorInt())

        CharacterAvatar(
            character = character,
            isAnimated = isMainCharacter,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f), // Makes it a square
        )
        Text(
            text = character.name,
            style =
                MaterialTheme.typography.titleMedium.copy(
                    fontFamily = genre.bodyFont(),
                ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
