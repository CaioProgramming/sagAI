package com.ilustris.sagai.features.saga.detail.review.ui.templates.book

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.newsaga.data.model.compiledColorPalette
import com.ilustris.sagai.features.saga.detail.data.model.Farewell
import com.ilustris.sagai.features.saga.detail.data.model.cleanMessage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewAction
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPage
import com.ilustris.sagai.features.saga.detail.review.ui.ReviewPageType

private val Ink = androidx.compose.ui.graphics.Color(0xFF3B2E1F)

/** The "Afterword" — a signed farewell letter from each of the saga's most important characters. */
class BookFarewellsPage(
    override val content: SagaContent,
    private val farewells: List<Farewell>,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.FAREWELLS

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val accent = content.data.genre.compiledColorPalette().firstOrNull() ?: MaterialTheme.colorScheme.primary
        val speakers =
            remember(farewells) {
                farewells.mapNotNull { farewell ->
                    content.characters
                        .find { it.data.id == farewell.characterId }
                        ?.let { it.data.name to farewell.cleanMessage(it.data.name) }
                }
            }

        Column(
            modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.review_farewells_title),
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                color = accent,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
            )

            LazyColumn(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                items(speakers) { (name, message) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "“$message”",
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            color = Ink,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            text = "— $name",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = accent,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }
        }
    }

    @Composable
    override fun Background(modifier: Modifier) {
        BookBackground(modifier)
    }
}
