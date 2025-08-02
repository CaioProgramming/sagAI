package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.SenderType.*
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.darker

@Composable
fun SenderType.itemOption(
    modifier: Modifier = Modifier,
    iconSize: Dp = 40.dp,
    selectedItem: SenderType? = null,
    genre: Genre,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    onSelect: (SenderType) -> Unit = {},
) {
    val isSelected = this == selectedItem
    val backgroundColor by animateColorAsState(
        if (isSelected.not()) MaterialTheme.colorScheme.surfaceContainer else
            MaterialTheme.colorScheme.surfaceContainer.darker(.2f)
    )

    this@itemOption.icon()?.let {

        Column(modifier =
            modifier
                .background(backgroundColor)
                .clickable {
                    onSelect(this@itemOption)
                }) {

            Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.onBackground.copy(alpha = .1f)))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    this@itemOption.title(),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.weight(1f),
                )

                Image(
                    painter = painterResource(id = it),
                    contentDescription = this@itemOption.title(),
                    colorFilter =
                        ColorFilter
                            .tint(MaterialTheme.colorScheme.onBackground),
                    modifier =
                        Modifier
                            .size(iconSize)
                            .padding(8.dp),
                    contentScale = ContentScale.Fit,
                )

            }

        }

    }
}

@Preview(showBackground = true)
@Composable
fun SenderTypePreview() {
    Column {
        SenderType.entries.forEach {
            it.itemOption(
                selectedItem = THOUGHT,
                genre = Genre.FANTASY,
            )
        }
    }
}

fun SenderType.icon() =
    when (this) {
        USER -> R.drawable.talk_bubble
        NARRATOR -> R.drawable.ic_feather
        THOUGHT -> R.drawable.think_icon
        ACTION -> R.drawable.action_icon
        NEW_CHARACTER -> R.drawable.character_icon
        else -> R.drawable.ic_spark
    }

@Composable
fun SenderType.title() =
    when (this) {
        USER -> stringResource(R.string.user_action_title)
        NARRATOR -> stringResource(R.string.sender_type_narrator_title)
        THOUGHT -> stringResource(R.string.sender_type_thought_title)
        ACTION -> stringResource(R.string.sender_type_action_title)
        NEW_CHARACTER -> stringResource(R.string.sender_type_new_character_title)
        CHARACTER -> "Personagem"
        NEW_CHAPTER -> "Novo Capítulo"
        NEW_ACT -> "Novo Ato"
    }

@Composable
fun SenderType.description() =
    when (this) {
        USER -> stringResource(R.string.sender_type_user_description)
        THOUGHT -> stringResource(R.string.sender_type_thought_description)
        ACTION -> stringResource(R.string.sender_type_action_description)
        NARRATOR -> stringResource(R.string.sender_type_narrator_description)
        NEW_CHAPTER -> stringResource(R.string.sender_type_new_chapter_description)
        NEW_CHARACTER -> stringResource(R.string.sender_type_new_character_description)
        CHARACTER -> stringResource(R.string.sender_type_character_description)
        else -> emptyString()
    }

@Composable
fun SenderType.hint() =
    when (this) {
        USER -> stringResource(R.string.sender_type_user_hint)
        NARRATOR -> stringResource(R.string.sender_type_narrator_hint)
        THOUGHT -> stringResource(R.string.sender_type_thought_hint)
        ACTION -> stringResource(R.string.sender_type_action_hint)
        NEW_CHARACTER -> stringResource(R.string.sender_type_new_character_hint)
        NEW_CHAPTER -> stringResource(R.string.sender_type_new_chapter_hint)
        CHARACTER -> stringResource(R.string.sender_type_character_hint)
        else -> stringResource(R.string.sender_type_default_hint) // Default hint
    }
