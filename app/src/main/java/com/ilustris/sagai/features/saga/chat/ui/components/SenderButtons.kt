package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

@Composable
fun SenderType.itemOption(
    modifier: Modifier = Modifier,
    iconSize: Dp = 40.dp,
    selectedItem: SenderType? = null,
    showText: Boolean = false,
    genre: Genre,
    alignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    onSelect: (SenderType) -> Unit = {},
) {
    val isSelected = this == selectedItem
    val color by animateColorAsState(
        targetValue =
            if (isSelected) {
                genre.iconColor
            } else {
                MaterialTheme.colorScheme.onBackground
            },
    )

    val borderColor by animateColorAsState(
        if (isSelected) genre.color else MaterialTheme.colorScheme.surfaceContainer.copy(alpha = .5f),
    )

    this@itemOption.icon()?.let {
        Column(
            modifier =
                modifier
                    .clip(RoundedCornerShape(genre.cornerSize()))
                    .clickable {
                        onSelect(this@itemOption)
                    }
                    .padding(4.dp),
            horizontalAlignment = alignment,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Image(
                painter = painterResource(id = it),
                contentDescription = this@itemOption.title(),
                colorFilter =
                    ColorFilter
                        .tint(color),
                modifier =
                    Modifier
                        .size(iconSize)
                        .clip(CircleShape)
                        .border(1.dp, borderColor, CircleShape)
                        .background(borderColor, CircleShape)
                        .padding(8.dp)
                        .align(alignment),
                contentScale = ContentScale.Fit,
            )
            if (showText) {
                Text(
                    this@itemOption.title(),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Light,
                   textAlign =  TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()

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
                modifier = Modifier.padding(4.dp),
                selectedItem = THOUGHT,
                genre = Genre.FANTASY,
                showText = true
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
        else -> null
    }

@Composable
fun SenderType.title() =
    when (this) {
        USER -> stringResource(R.string.user_action_title)
        NARRATOR -> stringResource(R.string.sender_type_narrator_title)
        THOUGHT -> stringResource(R.string.sender_type_thought_title)
        ACTION -> stringResource(R.string.sender_type_action_title)
        NEW_CHARACTER -> stringResource(R.string.sender_type_new_character_title)
        else -> emptyString()
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