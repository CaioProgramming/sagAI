@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.characters.relations.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.ilustris.sagai.core.utils.DateFormatOption
import com.ilustris.sagai.core.utils.formatDate
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.data.model.CharacterProfile
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.relations.data.model.CharacterRelation
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipContent
import com.ilustris.sagai.features.characters.relations.data.model.RelationshipUpdateEvent
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.characters.ui.components.buildCharactersAnnotatedString
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.shape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelationShipCard(
    saga: SagaContent,
    content: RelationshipContent,
    avatarSize: Dp = 100.dp,
    modifier: Modifier = Modifier,
) {
    var showDetailSheet by remember { mutableStateOf(false) }
    val genre = saga.data.genre
    val brush = content.getBrush(genre)
    Column(
        modifier =
            modifier
                .clip(genre.shape())
                .clickable { showDetailSheet = true }
                .border(1.dp, brush, genre.shape())
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
            modifier = Modifier.fillMaxWidth().height(150.dp).padding(16.dp),
        ) {
            CharacterAvatar(
                firstCharacter,
                firstCharacter.hexColor.hexToColor(),
                genre = genre,
                innerPadding = 0.dp,
                modifier = Modifier.size(avatarSize).offset(x = 15.dp).zIndex(1f),
            )

            CharacterAvatar(
                secondCharacter,
                secondCharacter.hexColor.hexToColor(),
                innerPadding = 0.dp,
                genre = genre,
                modifier = Modifier.size(avatarSize).offset(x = (-15).dp),
            )
        }
        val relation = content.relationshipEvents.lastOrNull()
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

@Composable
fun SingleRelationShipCard(
    saga: SagaContent,
    character: Character,
    content: RelationshipContent,
    showText: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val genre = saga.data.genre

    var showDetailSheet by remember { mutableStateOf(false) }
    val brush = content.getBrush(genre)
    Column(
        modifier =
            modifier
                .clip(genre.shape())
                .clickable {
                    showDetailSheet = true
                }.border(1.dp, brush, genre.shape())
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
        content.relationshipEvents.lastOrNull()?.let { relation ->
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
        }
    }

    if (showDetailSheet && content.relationshipEvents.isNotEmpty()) {
        ModalBottomSheet(
            onDismissRequest = { showDetailSheet = false },
            sheetState = rememberModalBottomSheetState(),
            shape = genre.shape(),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            RelationShipSheet(saga = saga, content = content)
        }
    }
}

@Composable
fun RelationShipSheet(
    saga: SagaContent,
    content: RelationshipContent,
) {
    val genre = saga.data.genre
    val firstCharacter = content.characterOne
    val secondCharacter = content.characterTwo

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
                val firstCharacter =
                    remember {
                        content.characterOne
                    }
                val secondCharacter =
                    remember {
                        content.characterTwo
                    }
                CharacterAvatar(
                    firstCharacter,
                    firstCharacter.hexColor.hexToColor(),
                    genre = genre,
                    innerPadding = 0.dp,
                    modifier = Modifier.size(64.dp).offset(x = 15.dp).zIndex(1f),
                )

                CharacterAvatar(
                    secondCharacter,
                    secondCharacter.hexColor.hexToColor(),
                    innerPadding = 0.dp,
                    genre = genre,
                    modifier = Modifier.size(64.dp).offset(x = (-15).dp),
                )
            }
        }

        stickyHeader {
            Text(
                buildCharactersAnnotatedString(
                    "${firstCharacter.name} & ${secondCharacter.name}",
                    saga.mainCharacter?.data,
                    listOf(firstCharacter, secondCharacter),
                    genre,
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

        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp),
            ) {
                Text(
                    "Última atualização",
                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = genre.bodyFont()),
                    modifier = Modifier.alpha(.4f),
                )

                Text(
                    content.relationshipEvents
                        .last()
                        .timestamp
                        .formatDate(DateFormatOption.HOUR_MINUTE_DAY_OF_MONTH_YEAR),
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = genre.bodyFont()),
                    modifier = Modifier.alpha(.8f).padding(8.dp),
                )
            }
        }

        if (content.relationshipEvents.isNotEmpty()) {
            items(content.relationshipEvents) {
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

@Preview(showBackground = true)
@Composable
fun RelationShipCardPreview() {
    val saga =
        SagaContent(
            data = Saga(id = 1, title = "My Saga", genre = Genre.FANTASY),
            mainCharacter =
                CharacterContent(
                    data = Character(id = 1, name = "Main Hero", details = Details(), profile = CharacterProfile()),
                ),
            characters =
                listOf(
                    CharacterContent(
                        data = Character(id = 1, name = "Main Hero", details = Details(), profile = CharacterProfile()),
                    ),
                    CharacterContent(
                        data = Character(id = 2, name = "Sidekick", details = Details(), profile = CharacterProfile()),
                    ),
                ),
        )
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

@Preview(showBackground = true)
@Composable
fun SingleRelationShipCardPreview() {
    val saga =
        SagaContent(
            data = Saga(id = 1, title = "My Saga", genre = Genre.SCI_FI),
            mainCharacter =
                CharacterContent(
                    data = Character(id = 1, name = "Space Captain", details = Details(), profile = CharacterProfile()),
                ),
        )
    val character =
        Character(id = 2, name = "Alien Ally", details = Details(), hexColor = "#0000FF", profile = CharacterProfile())
    val content =
        RelationshipContent(
            data =
                CharacterRelation(
                    id = 1,
                    characterOneId = 1,
                    characterTwoId = 2,
                    sagaId = 1,
                    title = "Allies",
                    description = "They fight for the galaxy.",
                    emoji = "🚀",
                ),
            characterOne = Character(id = 1, name = "Space Captain", details = Details(), profile = CharacterProfile()),
            characterTwo = character,
            relationshipEvents =
                listOf(
                    RelationshipUpdateEvent(
                        id = 1,
                        relationId = 1,
                        title = "Formed Alliance",
                        description = "A common enemy brought them together.",
                        emoji = "🤝",
                        timestamp = System.currentTimeMillis(),
                        timelineId = 0,
                    ),
                ),
        )
    SagAITheme {
        SingleRelationShipCard(saga = saga, character = character, content = content)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RelationShipSheetPreview() {
    val saga =
        SagaContent(
            data = Saga(id = 1, title = "My Saga Preview", genre = Genre.HORROR),
            mainCharacter =
                CharacterContent(
                    data =
                        Character(
                            id = 1,
                            name = "Survivor",
                            details = Details(),
                            profile = CharacterProfile(),
                        ),
                ),
        )
    val content =
        RelationshipContent(
            data =
                CharacterRelation(
                    id = 1,
                    characterOneId = 1,
                    characterTwoId = 2,
                    sagaId = 1,
                    title = "Haunted",
                    description = "Bound by a curse",
                    emoji = "👻",
                ),
            characterOne =
                Character(
                    id = 1,
                    name = "Survivor",
                    details = Details(),
                    hexColor = "#AABBCC",
                    profile = CharacterProfile(),
                ),
            characterTwo =
                Character(
                    id = 2,
                    name = "Ghost",
                    details = Details(),
                    hexColor = "#CCBBAA",
                    profile = CharacterProfile(),
                ),
            relationshipEvents =
                listOf(
                    RelationshipUpdateEvent(
                        id = 1,
                        relationId = 1,
                        title = "The Haunting Begins",
                        description = "They encountered the entity.",
                        emoji = "😱",
                        timelineId = 0,
                        timestamp =
                            System.currentTimeMillis() - 2000000,
                    ),
                    RelationshipUpdateEvent(
                        id = 2,
                        relationId = 1,
                        title = "Seeking Help",
                        description = "They look for a way to break the curse.",
                        emoji = "🙏",
                        timelineId = 0,
                        timestamp = System.currentTimeMillis(),
                    ),
                ),
        )
    SagAITheme {
        RelationShipSheet(saga = saga, content = content)
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
            firstCharacter.hexColor.hexToColor() ?: genre.color,
            secondCharacter.hexColor.hexToColor() ?: genre.colorPalette().last(),
        )
    ConstraintLayout {
        val (avatarsRow, relationshipCard, relationshipEmoji, divider) = createRefs()

        val brush =
            Brush.linearGradient(
                listOf(
                    firstCharacter.hexColor.hexToColor() ?: genre.color,
                    secondCharacter.hexColor.hexToColor() ?: genre.color,
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
                    .clip(genre.shape())
                    .border(2.dp, brush, genre.shape())
                    .background(MaterialTheme.colorScheme.background, genre.shape())
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
                modifier = Modifier.size(32.dp).offset(x = 5.dp).zIndex(1f),
            )

            CharacterAvatar(
                secondCharacter,
                secondCharacter.hexColor.hexToColor(),
                innerPadding = 0.dp,
                genre = genre,
                modifier = Modifier.size(32.dp).offset(x = (-5).dp),
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
