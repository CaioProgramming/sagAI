package com.ilustris.sagai.features.wiki.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.model.WikiType
import com.ilustris.sagai.features.wiki.data.model.iconForType
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.Typography
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.cornerSize

@Composable
fun WikiCard(
    wiki: Wiki,
    genre: Genre,
    modifier: Modifier,
) {
    Box(
        modifier =
            modifier
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .border(
                    width = 1.dp,
                    color = genre.color.copy(alpha = .4f),
                    shape =
                        RoundedCornerShape(
                            genre.cornerSize(),
                        ),
                )
                .clip(RoundedCornerShape(genre.cornerSize())),
    ) {
        Image(
            painterResource(wiki.type.iconForType(genre)),
            null,
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground.copy(alpha = .1f)),
            modifier = Modifier.fillMaxSize().scale(1.4f).offset(x = (-50).dp, y = (50).dp),
        )
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            Text(
                text = wiki.title,
                style =
                    Typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = genre.bodyFont(),
                    ),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = wiki.content,
                style =
                    Typography.bodyMedium.copy(
                        fontFamily = genre.bodyFont(),
                    ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp),
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
                    Text(it.title, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
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
                        modifier = Modifier.fillMaxWidth().height(300.dp).padding(8.dp),
                    )
                }
            }
        }
    }
}
