package com.ilustris.sagai.features.characters.relations.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.data.model.Details
import com.ilustris.sagai.features.characters.relations.data.model.CharacterRelation
import com.ilustris.sagai.features.characters.relations.domain.data.RelationshipContent
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.shape

@Composable
fun RelationShipCard(
    content: RelationshipContent,
    genre: Genre,
    avatarSize: Dp = 100.dp,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .clip(genre.shape())
                .border(1.dp, genre.color.copy(alpha = .3f), genre.shape())
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val relation = content.data
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().height(150.dp).padding(16.dp),
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
                modifier = Modifier.size(avatarSize).offset(x = 10.dp),
            )

            CharacterAvatar(
                secondCharacter,
                secondCharacter.hexColor.hexToColor(),
                genre = genre,
                modifier = Modifier.size(avatarSize).offset(x = (-10).dp),
            )
        }
        Text(
            relation.emoji,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .background(MaterialTheme.colorScheme.background.copy(alpha = .3f), CircleShape)
                    .padding(8.dp),
        )

        Text(
            relation.title,
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = genre.bodyFont()),
            textAlign = TextAlign.Center,
        )

        Text(
            relation.description,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = genre.bodyFont()),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun SingleRelationShipCard(
    character: Character,
    relation: CharacterRelation,
    genre: Genre,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .clip(genre.shape())
                .border(1.dp, genre.color.copy(alpha = .3f), genre.shape())
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
        )
        Text(
            relation.description,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = genre.bodyFont()),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
)
@Composable
fun RelationShipCardPreview() {
    val genre = Genre.FANTASY
    val char1 =
        Character(
            id = 1,
            name = "Aria",
            backstory = "A brave warrior from the north.",
            image = "", // Empty so CharacterAvatar shows initial
            hexColor = "#FF6F61",
            sagaId = 1,
            details = Details(),
        )
    val char2 =
        Character(
            id = 2,
            name = "Bryn",
            backstory = "A cunning rogue with a mysterious past.",
            image = "",
            hexColor = "#3D98F7",
            sagaId = 1,
            details = Details(),
        )
    val relation =
        CharacterRelation.create(
            char1Id = char1.id,
            char2Id = char2.id,
            sagaId = 1,
            title = "Allies",
            description = "They fight side by side against darkness.",
            emoji = "🤝",
        )
    val content = RelationshipContent(relation, char1, char2)

    SagAITheme {
        RelationShipCard(
            content = content,
            genre = genre,
            modifier = Modifier.padding(16.dp).fillMaxHeight(.6f),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SingleRelationShipCardPreview() {
    val genre = Genre.HORROR
    val char =
        Character(
            id = 3,
            name = "Cora",
            backstory = "A scholar who studies ancient texts.",
            image = "",
            hexColor = "#8E44AD",
            sagaId = 1,
            details = Details(),
        )
    val other =
        Character(
            id = 4,
            name = "Dax",
            backstory = "A stoic guardian.",
            image = "",
            hexColor = "#27AE60",
            sagaId = 1,
            details = Details(),
        )
    val relation =
        CharacterRelation.create(
            char1Id = char.id,
            char2Id = other.id,
            sagaId = 1,
            title = "Mentor",
            description = "Cora is mentored by Dax in the arcane arts.",
            emoji = "🧙",
        )
    SagAITheme {
        SingleRelationShipCard(
            character = char,
            relation = relation,
            genre = genre,
            modifier = Modifier.padding(16.dp),
        )
    }
}
