package com.ilustris.sagai.features.saga.detail.review.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.headerFont

class ReviewSummaryPage(
    private val content: SagaContent,
    private val onNavigate: (Int) -> Unit,
) : ReviewPage {
    @Composable
    override fun Show(
        modifier: Modifier,
        onAction: (ReviewAction) -> Unit,
    ) {
        val genre = content.data.genre
        val review = content.data.review ?: return

        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "YOUR SAGA WRAPPED",
                style =
                    MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = genre.headerFont(),
                        fontWeight = FontWeight.Black,
                    ),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            HeroSummaryCard(
                content = content,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "REVISIT THE JOURNEY",
                style =
                    MaterialTheme.typography.labelLarge.copy(
                        fontFamily = genre.bodyFont(),
                        fontWeight = FontWeight.Bold,
                        color = genre.color,
                    ),
                modifier =
                    Modifier
                        .padding(horizontal = 24.dp)
                        .align(Alignment.Start),
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                review.introduction?.let {
                    item {
                        ArchetypeMiniCard(
                            title = "The Ritual",
                            subtitle = it.content?.title ?: "Legend Builder",
                            genre = genre,
                        ) { onNavigate(0) }
                    }
                }
                review.expressiveness?.let {
                    item {
                        ArchetypeMiniCard(
                            title = "The Voice",
                            subtitle = it.content?.title ?: "Expressive",
                            genre = genre,
                        ) { onNavigate(2) } // Approximate index
                    }
                }
                review.playstyle?.let {
                    item {
                        ArchetypeMiniCard(
                            title = "The Vibe",
                            subtitle = it.content?.title ?: "Archetype",
                            genre = genre,
                        ) { onNavigate(4) }
                    }
                }
                review.topCharacters?.let {
                    item {
                        ArchetypeMiniCard(
                            title = "The Squad",
                            subtitle = it.content?.title ?: "Bonds",
                            genre = genre,
                        ) { onNavigate(6) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onNavigate(0) },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = genre.color,
                        contentColor = Color.White,
                    ),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
            ) {
                Text(
                    "WATCH AGAIN",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }
    }
}
