package com.ilustris.sagai.features.newsaga.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.shape

@Composable
fun SagaFormSummaryCards(form: SagaForm, genre: Genre) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (isExpanded) 0f else -5f, label = "rotation aniamtion")
    val characterRotation by animateFloatAsState(targetValue = if (isExpanded) 0f else 5f, label = "character rotation aniamtion")
    val offset by animateDpAsState(targetValue = if (!isExpanded) (-150).dp else 0.dp, label = "offset animation")

    Row(
        modifier = Modifier
            .padding(8.dp)
            .wrapContentSize()
            .clip(genre.shape())
            .clickable { isExpanded = !isExpanded }
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CharacterSummaryCard(
            form = form, genre = genre, isExpanded = isExpanded, modifier = Modifier
                .size(150.dp)
                .rotate(characterRotation).also {
                    if (isExpanded) {
                        Modifier.weight(1f)
                    }
                }
        )
        SagaSummaryCard(
            form = form,
            genre = genre, isExpanded = isExpanded , modifier = Modifier
                .size(150.dp)
                .offset(x = offset)
                .rotate(rotation)
                .also {
                    if (isExpanded) {
                        Modifier.weight(1f)
                    }
                }
        )

    }
}

@Composable
fun SagaSummaryCard(form: SagaForm, genre: Genre, isExpanded: Boolean, modifier: Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(0.5f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = genre.color
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = form.saga.title, modifier = Modifier.padding(8.dp) , style = MaterialTheme.typography.titleLarge.copy(fontFamily = genre.headerFont()), textAlign = TextAlign.Center)
            if (isExpanded) {
                Text(text = form.saga.description, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = genre.bodyFont()), textAlign = TextAlign.Center)
            }
        }
    }
}

@Preview
@Composable
fun SagaFormSummaryCardsPreview() {
    val form = SagaForm(
        saga = SagaDraft(title = "Saga Title", description = "Saga description"),
        character = CharacterInfo(
            name = "Character Name",
            description = "Character description"
        )
    )
    val genre = Genre.FANTASY
    SagaFormSummaryCards(form = form, genre = genre,
    )
}

@Composable
fun CharacterSummaryCard(form: SagaForm, genre: Genre, isExpanded: Boolean,  modifier: Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(0.5f),
        colors = CardDefaults.cardColors(
            containerColor = genre.color,
            contentColor = genre.iconColor
        )
    ) {
        Column(
            modifier = Modifier.animateContentSize().padding(8.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = form.character.name, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.titleLarge.copy(fontFamily = genre.headerFont()), textAlign = TextAlign.Center)
            if (isExpanded) {
                Text(text = form.character.description, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = genre.bodyFont()), textAlign = TextAlign.Center)
            }
        }
    }
}