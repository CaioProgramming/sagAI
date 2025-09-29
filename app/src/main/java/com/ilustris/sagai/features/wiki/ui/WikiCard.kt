package com.ilustris.sagai.features.wiki.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.data.model.WikiType
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.Typography
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.gradientFade

@Composable
fun WikiCard(
    wiki: Wiki,
    genre: Genre,
    modifier: Modifier,
    expanded: Boolean = false,
) {
    var isExpanded by remember { mutableStateOf(expanded) }
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(genre.cornerSize()))
                .border(
                    width = 1.dp,
                    brush = genre.color.gradientFade(),
                    shape =
                        RoundedCornerShape(
                            genre.cornerSize(),
                        ),
                ).clickable { isExpanded = !isExpanded }
                .padding(16.dp)
                .animateContentSize(
                    tween(easing = FastOutSlowInEasing),
                ),
    ) {
        val tag = wiki.emojiTag ?: emptyString()

        Text(
            text = "${tag.plus(" ")}${wiki.title}",
            style =
                Typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = genre.bodyFont(),
                ),
            color = MaterialTheme.colorScheme.onBackground,
        )
        AnimatedVisibility(isExpanded) {
            Text(
                text = wiki.content,
                style =
                    Typography.bodySmall.copy(
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
