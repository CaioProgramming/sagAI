package com.ilustris.sagai.features.saga.chat.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.characters.data.model.CharacterContent
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.chat.BubbleTailAlignment
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.hexToColor

@Composable
fun QueryItemsTooltip(
    saga: SagaContent,
    currentType: ItemsType,
    modifier: Modifier = Modifier,
    onClick: (ItemsType, Any) -> Unit,
) {
    val genre = saga.data.genre
    val shape = genre.bubble(BubbleTailAlignment.BottomLeft, tailWidth = 0.dp, tailHeight = 0.dp)

    Column(
        modifier =
            modifier
                .dropShadow(
                    shape,
                    Shadow(
                        radius = 5.dp,
                        genre.color,
                    ),
                ).border(1.dp, genre.color.gradientFade(), shape)
                .background(
                    MaterialTheme.colorScheme.background,
                    shape,
                ).clip(shape)
                .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AnimatedContent(currentType.title) {
            Text(
                it,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = genre.bodyFont(),
                    ),
                modifier =
                    Modifier
                        .padding(horizontal = 8.dp),
            )
        }

        AnimatedContent(currentType, transitionSpec = {
            slideInVertically { -it } + fadeIn(tween(300)) togetherWith fadeOut(tween(300))
        }) { currentType ->
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when (currentType) {
                    is ItemsType.Characters -> {
                        items(currentType.filteredCharacters) { character ->

                            Row(
                                Modifier
                                    .border(1.dp, genre.color.copy(alpha = .2f), shape)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceContainer,
                                        shape,
                                    ).clip(shape)
                                    .clickable {
                                        onClick(currentType, character)
                                    }.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                CharacterAvatar(
                                    character.data,
                                    genre = genre,
                                    innerPadding = 0.dp,
                                    borderSize = 1.dp,
                                    softFocusRadius = 0f,
                                    grainRadius = 0f,
                                    modifier = Modifier.size(24.dp),
                                )

                                Text(
                                    character.data.name,
                                    maxLines = 1,
                                    style =
                                        MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = genre.bodyFont(),
                                            color =
                                                character.data.hexColor.hexToColor()
                                                    ?: genre.color,
                                        ),
                                )
                            }
                        }
                    }

                    is ItemsType.Wikis -> {
                        items(currentType.filteredWikis) { wiki ->
                            Row(
                                Modifier
                                    .border(1.dp, genre.color.copy(alpha = .2f), shape)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceContainer,
                                        shape,
                                    ).clip(shape)
                                    .clickable {
                                        onClick(currentType, wiki)
                                    }.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    wiki.emojiTag ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                )

                                Text(
                                    wiki.title,
                                    maxLines = 1,
                                    style =
                                        MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = genre.bodyFont(),
                                        ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

sealed class ItemsType(
    val title: String,
) {
    data class Characters(
        val filteredCharacters: List<CharacterContent>,
        val charactersTitle: String,
    ) : ItemsType(
            charactersTitle,
        )

    data class Wikis(
        val filteredWikis: List<Wiki>,
        val wikiTitle: String,
    ) : ItemsType(wikiTitle)
}
