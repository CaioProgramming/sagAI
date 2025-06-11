package com.ilustris.sagai.features.characters.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.headerFont

@Composable
fun CharacterSection(title: String, content: String, genre: Genre) {
    Text(
        title,
        style =
        MaterialTheme.typography.titleLarge.copy(
            fontFamily = genre.headerFont(),
            fontWeight = FontWeight.Normal,
        ),
        modifier =
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
    )

    Text(
        content,
        style =
        MaterialTheme.typography.bodyMedium.copy(
            fontFamily = genre.bodyFont(),
        ),
        modifier =
        Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
    )
}
