package com.ilustris.sagai.features.newsaga.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.CreationSuggestion
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.newsaga.data.model.resolveIconColor
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.headerFont

@Composable
fun StorySeedCard(
    suggestion: CreationSuggestion,
    genre: Genre,
    modifier: Modifier = Modifier,
    onClick: (CreationSuggestion) -> Unit,
) {
    val shape = genre.bubble(tailWidth = 0.dp, tailHeight = 0.dp, isNarrator = true)
    val borderBrush = genre.gradient()
    val headerFont = genre.headerFont()
    val bodyFont = genre.bodyFont()

    Column(
        modifier =
            modifier
                .width(220.dp)
                .clip(shape)
                .border(1.5.dp, borderBrush, shape)
                .background(MaterialTheme.colorScheme.surfaceContainer, shape)
                .clickable { onClick(suggestion) }
                .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = suggestion.title.ifBlank { suggestion.text },
            style =
                MaterialTheme.typography.titleSmall.copy(
                    fontFamily = headerFont,
                    shadow =
                        Shadow(
                            genre.resolveColor().copy(alpha = .4f),
                            blurRadius = 6f,
                        ),
                ),
            color = genre.resolveIconColor(),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        if (suggestion.description.isNotBlank()) {
            Text(
                text = suggestion.description,
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontFamily = bodyFont,
                    ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .75f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun StorySeedRow(
    suggestions: List<CreationSuggestion>,
    genre: Genre,
    modifier: Modifier = Modifier,
    onSeedClick: (CreationSuggestion) -> Unit,
) {
    if (suggestions.isEmpty()) return

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
    ) {
        items(suggestions) { suggestion ->
            StorySeedCard(
                suggestion = suggestion,
                genre = genre,
                onClick = onSeedClick,
            )
        }
    }
}
