package com.ilustris.sagai.features.brain.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.features.brain.domain.ConstellationLayoutEngine
import com.ilustris.sagai.features.brain.presentation.BrainMiniViewModel
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.detail.ui.components.RowHeader

@Composable
fun BrainMiniPreview(
    sagaId: Int,
    genre: Genre?,
    sectionTitle: String,
    onOpenBrain: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BrainMiniViewModel = hiltViewModel(),
) {
    LaunchedEffect(sagaId) {
        viewModel.load(sagaId)
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val graph = state.graph
    val accent = genreAccent(genre)

    Column(modifier = modifier.fillMaxWidth()) {
        RowHeader(
            title = sectionTitle,
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            onOpenBrain()
        }

        if (graph != null && state.layout != null) {
            val layoutEngine = ConstellationLayoutEngine()
            val miniLayout = layoutEngine.layoutMini(graph, width = 360f, height = 120f)
            BrainMiniCanvas(
                graph = graph,
                layout = miniLayout,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(horizontal = 16.dp)
                        .clickable { onOpenBrain() },
                genrePrimary = accent,
                genreSecondary = genreSecondaryAccent(genre),
            )
        }
    }
}

private fun genreAccent(genre: Genre?): Color =
    when (genre) {
        Genre.FANTASY -> Color(0xFFB1A7F0)
        Genre.CYBERPUNK -> Color(0xFF90E0EF)
        Genre.HORROR -> Color(0xFF9B59B6)
        Genre.HEROES -> Color(0xFF64B5F6)
        Genre.CRIME -> Color(0xFFE57373)
        Genre.SHINOBI -> Color(0xFF81C784)
        Genre.SPACE_OPERA -> Color(0xFF4FC3F7)
        Genre.COWBOY -> Color(0xFFE8A838)
        Genre.PUNK_ROCK -> Color(0xFFFF7043)
        null -> Color(0xFF90E0EF)
    }

private fun genreSecondaryAccent(genre: Genre?): Color =
    when (genre) {
        Genre.FANTASY -> Color(0xFF8B80D0)
        Genre.CYBERPUNK -> Color(0xFF4ECDC4)
        Genre.HORROR -> Color(0xFFBB86FC)
        Genre.HEROES -> Color(0xFF90CAF9)
        Genre.CRIME -> Color(0xFFFFAB91)
        Genre.SHINOBI -> Color(0xFFA5D6A7)
        Genre.SPACE_OPERA -> Color(0xFF81D4FA)
        Genre.COWBOY -> Color(0xFFFFCC80)
        Genre.PUNK_ROCK -> Color(0xFFFFAB91)
        null -> Color(0xFFB0BEC5)
    }
