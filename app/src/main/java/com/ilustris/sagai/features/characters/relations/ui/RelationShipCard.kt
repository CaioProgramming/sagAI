package com.ilustris.sagai.features.characters.relations.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.model.LocalGenreVisualConfig
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterProfile
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.relations.data.model.CharacterRelation
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipContent
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipUpdateEvent
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.characters.ui.components.buildCharactersAnnotatedString
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.chat.BubbleTailAlignment
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.hexToColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelationShipCard(
    saga: Saga,
    content: RelationshipContent,
    avatarSize: Dp = 100.dp,
    modifier: Modifier = Modifier,
) {
    var showDetailSheet by remember { mutableStateOf(false) }
    val genre = remember { saga.genre }
    val shape = genre.bubble(BubbleTailAlignment.BottomRight, 0.dp, 0.dp, true)

    val visualConfig = LocalGenreVisualConfig.current
    val brush = remember { content.getBrush(genre, visualConfig) }
    Column(
        modifier =
            modifier
                .clip(shape)
                .clickable { showDetailSheet = true }
                .border(1.dp, brush, shape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val firstCharacter =
            remember {
                content.characterOne
            }
        val secondCharacter =
            remember {
                content.characterTwo
            }
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(16.dp),
        ) {
            CharacterAvatar(
                firstCharacter,
                firstCharacter.hexColor.hexToColor(),
                genre = genre,
                innerPadding = 0.dp,
                modifier =
                    Modifier
                        .size(avatarSize)
                        .offset(x = 15.dp)
                        .zIndex(1f),
            )

            CharacterAvatar(
                secondCharacter,
                secondCharacter.hexColor.hexToColor(),
                innerPadding = 0.dp,
                genre = genre,
                modifier =
                    Modifier
                        .size(avatarSize)
                        .offset(x = (-15).dp),
            )
        }
        val relation =
            remember {
                content.relationshipEvents.sortedByDescending { it.timestamp }.firstOrNull()
            }

        relation?.let {
            Text(
                relation.emoji,
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .background(
                            MaterialTheme.colorScheme.background.copy(alpha = .3f),
                            CircleShape,
                        ).padding(8.dp),
            )

            Text(
                relation.title,
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = genre.bodyFont()),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Text(
                relation.description,
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = genre.bodyFont()),
                textAlign = TextAlign.Start,
            )
        }
    }

    if (showDetailSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDetailSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            RelationShipSheet(saga = saga, content = content)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleRelationShipCard(
    saga: Saga,
    character: Character,
    content: RelationshipContent,
    showText: Boolean = true,
    showUpdates: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val genre = saga.genre
    val shape = genre.bubble(BubbleTailAlignment.BottomRight, 0.dp, 0.dp, true)

    var showDetailSheet by remember { mutableStateOf(false) }
    val visualConfig = LocalGenreVisualConfig.current
    val brush = content.getBrush(genre, visualConfig)
    val relationshipEvents =
        remember {
            content.relationshipEvents.sortedByDescending { it.timestamp }
        }

    Column(
        modifier =
            modifier
                .clip(shape)
                .clickable {
                    showDetailSheet = true
                }.border(1.dp, brush, shape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(horizontalArrangement = Arrangement.Center) {
            CharacterAvatar(
                character,
                character.hexColor.hexToColor(),
                genre = genre,
                modifier = Modifier.size(100.dp),
            )
        }
        relationshipEvents.firstOrNull()?.let { relation ->
            Text(
                relation.emoji,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
                        .padding(8.dp),
            )
            Text(
                relation.title,
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = genre.bodyFont()),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            if (showText) {
                Text(
                    relation.description,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = genre.bodyFont()),
                    textAlign = TextAlign.Center,
                )
            }

            if (showUpdates) {
                Text(
                    stringResource(id = R.string.updates_count, content.relationshipEvents.size),
                    style =
                        MaterialTheme.typography.labelMedium.copy(
                            fontFamily = genre.bodyFont(),
                        ),
                )
            }
        }
    }

    if (showDetailSheet && content.relationshipEvents.isNotEmpty()) {
        ModalBottomSheet(
            onDismissRequest = { showDetailSheet = false },
            sheetState = rememberModalBottomSheetState(),
            shape = shape,
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            RelationShipSheet(saga = saga, content = content)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RelationShipSheet(
    saga: Saga,
    content: RelationshipContent,
) {
    val genre = saga.genre
    val firstCharacter = content.characterOne
    val secondCharacter = content.characterTwo
    val sortRelationsByTimestamp =
        remember {
            content.relationshipEvents.sortedByDescending { it.timestamp }
        }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier =
                    Modifier
                        .padding(vertical = 8.dp),
            ) {
                CharacterAvatar(
                    firstCharacter,
                    firstCharacter.hexColor.hexToColor(),
                    genre = genre,
                    innerPadding = 0.dp,
                    modifier =
                        Modifier
                            .size(64.dp)
                            .offset(x = 15.dp)
                            .zIndex(1f),
                )

                CharacterAvatar(
                    secondCharacter,
                    secondCharacter.hexColor.hexToColor(),
                    innerPadding = 0.dp,
                    genre = genre,
                    modifier =
                        Modifier
                            .size(64.dp)
                            .offset(x = (-15).dp),
                )
            }
        }

        stickyHeader {
            Text(
                buildCharactersAnnotatedString(
                    "${firstCharacter.name} & ${secondCharacter.name}",
                    null,
                    listOf(firstCharacter, secondCharacter),
                    genre,
                    genre.resolveColor(),
                ),
                style =
                    MaterialTheme.typography.titleLarge
                        .copy(
                            fontFamily = genre.headerFont(),
                            textAlign = TextAlign.Center,
                        ),
                modifier =
                    Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .fillMaxWidth()
                        .padding(16.dp),
            )
        }

        if (content.relationshipEvents.isNotEmpty()) {
            items(sortRelationsByTimestamp) {
                RelationshipEventCard(
                    relationshipEvent = it,
                    content = content,
                    genre = genre,
                )
            }
        }

        item {
            Spacer(Modifier.size(32.dp))
        }
    }
}

@Composable
fun RelationshipEventCard(
    relationshipEvent: RelationshipUpdateEvent,
    content: RelationshipContent,
    genre: Genre,
) {
    val firstCharacter = content.characterOne
    val secondCharacter = content.characterTwo
    val charactersColors =
        listOf(
            firstCharacter.hexColor.hexToColor() ?: genre.resolveColor(),
            secondCharacter.hexColor.hexToColor() ?: genre.resolveColor(),
        )
    ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
        val (avatarsRow, relationshipCard, divider) = createRefs()
        val shape = genre.bubble(BubbleTailAlignment.BottomRight, 0.dp, 0.dp, true)

        val brush =
            Brush.linearGradient(
                listOf(
                    firstCharacter.hexColor.hexToColor() ?: genre.resolveColor(),
                    secondCharacter.hexColor.hexToColor() ?: genre.resolveColor(),
                ),
            )

        Column(
            modifier =
                Modifier
                    .constrainAs(relationshipCard) {
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                        start.linkTo(parent.start)
                        width = Dimension.fillToConstraints
                    }.padding(horizontal = 16.dp)
                    .clip(shape)
                    .border(2.dp, brush, shape)
                    .background(MaterialTheme.colorScheme.background, shape)
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                relationshipEvent.emoji,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )

            Text(
                relationshipEvent.title,
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontFamily = genre.bodyFont(),
                        textAlign = TextAlign.Center,
                    ),
            )

            Text(
                relationshipEvent.description,
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontFamily = genre.bodyFont(),
                        textAlign = TextAlign.Justify,
                    ),
                modifier = Modifier.alpha(.6f),
            )
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier =
                Modifier
                    .constrainAs(avatarsRow) {
                        top.linkTo(relationshipCard.top, margin = (-16).dp)
                        end.linkTo(parent.end)
                        start.linkTo(parent.start)
                        width = Dimension.matchParent
                    },
        ) {
            CharacterAvatar(
                firstCharacter,
                firstCharacter.hexColor.hexToColor(),
                genre = genre,
                innerPadding = 0.dp,
                modifier =
                    Modifier
                        .size(32.dp)
                        .offset(x = 5.dp)
                        .zIndex(1f),
            )

            CharacterAvatar(
                secondCharacter,
                secondCharacter.hexColor.hexToColor(),
                innerPadding = 0.dp,
                genre = genre,
                modifier =
                    Modifier
                        .size(32.dp)
                        .offset(x = (-5).dp),
            )
        }

        if (relationshipEvent != content.relationshipEvents.last()) {
            val verticalBrush =
                Brush.verticalGradient(
                    charactersColors.reversed(),
                )
            VerticalDivider(
                color = MaterialTheme.colorScheme.onBackground,
                thickness = 2.dp,
                modifier =
                    Modifier
                        .constrainAs(divider) {
                            top.linkTo(relationshipCard.bottom)
                            end.linkTo(avatarsRow.end)
                            start.linkTo(avatarsRow.start)
                        }.gradientFill(verticalBrush)
                        .height(50.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RelationShipCardPreview() {
    val saga = Saga(id = 1, title = "My Saga", genre = Genre.FANTASY)
    val content =
        RelationshipContent(
            data =
                CharacterRelation(
                    id = 1,
                    characterOneId = 1,
                    characterTwoId = 2,
                    sagaId = 1,
                    title = "Best Friends",
                    description = "They've been through a lot together.",
                    emoji = "🧑‍🤝‍🧑",
                ),
            characterOne =
                Character(
                    id = 1,
                    name = "Main Hero",
                    details = Details(),
                    hexColor = "#FF0000",
                    profile = CharacterProfile(),
                ),
            characterTwo =
                Character(
                    id = 2,
                    name = "Sidekick",
                    details = Details(),
                    hexColor = "#00FF00",
                    profile = CharacterProfile(),
                ),
            relationshipEvents =
                listOf(
                    RelationshipUpdateEvent(
                        id = 1,
                        relationId = 1,
                        title = "First Met",
                        description = "They met under mysterious circumstances.",
                        emoji = "❓",
                        timelineId = 0,
                        timestamp = System.currentTimeMillis() - 1000000,
                    ),
                    RelationshipUpdateEvent(
                        id = 2,
                        relationId = 1,
                        title = "Became Friends",
                        description = "After an adventure, they became close.",
                        emoji = "🤝",
                        timelineId = 0,
                        timestamp = System.currentTimeMillis(),
                    ),
                ),
        )
    SagAITheme {
        RelationShipCard(saga = saga, content = content)
    }
}
