package com.ilustris.sagai.features.wiki.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.model.WikiType
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.Typography
import com.ilustris.sagai.ui.theme.gradientFade

@Composable
fun WikiCard(
    wiki: Wiki,
    genre: Genre,
    modifier: Modifier,
    expanded: Boolean = false,
) {
    var expanded by remember {
        mutableStateOf(expanded)
    }
    val shape = genre.bubble(isNarrator = true, tailWidth = 0.dp, tailHeight = 0.dp)
    Column(
        modifier =
            modifier
                .border(
                    width = 1.dp,
                    brush = MaterialTheme.colorScheme.primary.gradientFade(),
                    shape = shape,
                ).clip(shape)
                .clickable {
                    expanded = !expanded
                }.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        val tag = wiki.emojiTag ?: emptyString()

        Text(
            text = "${tag.plus(" ")}${wiki.title}",
            style =
                Typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                ),
            color = MaterialTheme.colorScheme.onBackground,
        )
        AnimatedVisibility(expanded) {
            Text(
                text = wiki.content,
                style =
                    Typography.labelMedium.copy(
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    ),
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WikiCardPreview() {
    SagAIScaffold {
        LazyVerticalGrid(columns = GridCells.Fixed(2)) {
            Genre.entries.forEach {
                item(span = { GridItemSpan(2) }) {
                    Text(stringResource(it.title), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }

                items(WikiType.entries) { type ->
                    WikiCard(
                        Wiki(
                            title = type.name,
                            content = "Wiki card content for ${it.name}",
                            sagaId = 0,
                            type = type,
                        ),
                        genre = it,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                    )
                }
            }
        }
    }
}
