@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.ilustris.sagai.features.characters.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.sortCharactersByMessageCount
import com.ilustris.sagai.core.utils.sortCharactersContentByMessageCount
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.presentation.CharacterViewModel
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.detail.ui.DetailAction
import com.ilustris.sagai.features.saga.detail.ui.sharedTransitionActionItemModifier
import com.ilustris.sagai.features.saga.detail.ui.titleAndSubtitle
import com.ilustris.sagai.features.timeline.data.model.Timeline
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.LargeHorizontalHeader
import com.ilustris.sagai.ui.theme.components.SagaTopBar
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.shape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterGalleryView(
    navController: NavHostController,
    sagaId: String?,
    characterViewModel: CharacterViewModel = hiltViewModel(),
) {
    val saga by characterViewModel.saga.collectAsStateWithLifecycle()
    val characters by characterViewModel.characters.collectAsStateWithLifecycle()
    val state by characterViewModel.state.collectAsState()

    LaunchedEffect(saga) {
        if (saga == null) {
            characterViewModel.loadCharacters(sagaId?.toInt())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharactersGalleryContent(
    saga: SagaContent,
    onOpenEvent: (Timeline) -> Unit = {},
    onBackClick: () -> Unit = {},
    titleModifier: Modifier = Modifier,
    animationScopes: Pair<SharedTransitionScope, AnimatedContentScope>,
) {
    var showCharacter by remember {
        mutableStateOf<CharacterContent?>(null)
    }

    val titleAndSubtitle =
        DetailAction.CHARACTERS.titleAndSubtitle(saga)
    val genre = saga.data.genre
    with(animationScopes.first) {
        Box {
            AnimatedContent(saga.characters, transitionSpec = {
                fadeIn() togetherWith fadeOut()
            }) {
                val characters =
                    remember {
                        sortCharactersContentByMessageCount(it, saga.flatMessages())
                    }
                val listState = rememberLazyGridState()
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = listState,
                    modifier =
                        Modifier
                            .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(8.dp),
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LargeHorizontalHeader(
                            titleAndSubtitle.first,
                            titleAndSubtitle.second,
                            titleStyle =
                                MaterialTheme.typography.displayMedium.copy(
                                    fontFamily = genre.headerFont(),
                                ),
                            subtitleStyle =
                                MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = genre.bodyFont(),
                                ),
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            titleModifier = titleModifier,
                        )
                    }

                    items(characters, key = { character -> character.data.id }) { character ->
                        val characterModifier =
                            this@with.sharedTransitionActionItemModifier(
                                DetailAction.CHARACTERS,
                                animationScopes.second,
                                character.data.id,
                                saga.data.id,
                            )
                        CharacterYearbookItem(
                            character = character.data,
                            saga.data.genre,
                            imageModifier = characterModifier,
                            modifier =
                                Modifier
                                    .clip(genre.shape())
                                    .clickable {
                                        showCharacter = character
                                    },
                        )
                    }
                }

                val newCharacterSheetState =
                    rememberModalBottomSheetState(skipPartiallyExpanded = true)

                showCharacter?.let { character ->
                    ModalBottomSheet(
                        onDismissRequest = { showCharacter = null },
                        sheetState = newCharacterSheetState,
                        containerColor = MaterialTheme.colorScheme.background,
                        dragHandle = { Box {} },
                    ) {
                        CharacterDetailsContent(
                            saga,
                            character,
                            openEvent = { event ->
                                event?.let {
                                    onOpenEvent(event)
                                }
                            },
                        )
                    }
                }

                AnimatedVisibility(
                    listState.canScrollBackward,
                    enter = fadeIn(tween(400, delayMillis = 200)),
                    exit = fadeOut(tween(200)),
                ) {
                    SagaTopBar(
                        titleAndSubtitle.first,
                        titleAndSubtitle.second,
                        saga.data.genre,
                        onBackClick = { onBackClick() },
                        actionContent = { Box(Modifier.size(24.dp)) },
                        modifier =
                            Modifier
                                .background(MaterialTheme.colorScheme.background)
                                .fillMaxWidth()
                                .padding(top = 50.dp, start = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun CharacterYearbookItem(
    character: Character,
    genre: Genre,
    modifier: Modifier = Modifier,
    imageModifier: Modifier = Modifier,
) {
    CharacterVerticalItem(modifier, imageModifier.size(100.dp), character, genre)
}

@Composable
private fun CharacterVerticalItem(
    modifier: Modifier,
    iconModifier: Modifier,
    character: Character,
    genre: Genre,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    borderSize: Dp = 2.dp,
    borderColor: Color? = null,
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
            genre = genre,
            modifier =
                iconModifier
                    .align(Alignment.CenterHorizontally)
                    .aspectRatio(1f),
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
fun CharacterYearbookItemPreview() {
    val character =
        Character(
            id = 1,
            name = "Character Name",
            backstory = "Character backstory",
            image = "",
            hexColor = "#FF0000",
            sagaId = 1,
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
        )
    CharacterYearbookItem(character = character, genre = Genre.FANTASY)
}

@Preview
@Composable
fun CharacterVerticalItemPreview() {
    val character =
        Character(
            id = 1,
            name = "Character Name",
            backstory = "Character backstory",
            image = "",
            hexColor = "#FF0000",
            sagaId = 1,
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
        )
    CharacterVerticalItem(
        modifier = Modifier,
        character = character,
        genre = Genre.FANTASY,
        iconModifier = Modifier.size(100.dp),
    )
}

@Preview
@Composable
fun CharacterHorizontalViewPreview() {
    val character =
        Character(
            id = 1,
            name = "Character Name",
            backstory = "Character Backstory",
            image = "",
            hexColor = "#3d98f7",
            sagaId = 1,
            details =
                Details(
                    appearance = "Appearance",
                    personality = "Personality",
                    race = "Race",
                ),
            joinedAt = 0L,
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
    isLast: Boolean = false,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            CharacterAvatar(
                character = character,
                borderColor = borderColor,
                borderSize = borderSize,
                textStyle =
                    MaterialTheme.typography.labelLarge.copy(
                        fontFamily = genre.headerFont(),
                    ),
                genre = genre,
                modifier =
                    Modifier
                        .size(imageSize),
            )

            Text(
                text = character.name,
                style =
                    style.copy(
                        fontFamily = genre.bodyFont(),
                        fontWeight = FontWeight.W700,
                        color = character.hexColor.hexToColor() ?: genre.color,
                    ),
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f),
            )
        }

        if (!isLast) {
            HorizontalDivider(
                modifier =
                    Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .2f),
            )
        }
    }
}
