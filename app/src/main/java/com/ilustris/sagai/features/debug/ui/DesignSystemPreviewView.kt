package com.ilustris.sagai.features.debug.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.ui.components.stylisedText
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.rememberRotatingBorderAngle
import com.ilustris.sagai.ui.theme.rotatingGradientBorder
import com.ilustris.sagai.ui.theme.sagaShape

/**
 * Dev-only: cycles all genres and renders the design-system primitives — bubble shape, glow
 * border, rotating "AI is generating" stroke, and the genre shader filter — so each can be
 * eyeballed before being wired into real screens. Reachable from Settings → Debug in DEBUG
 * builds only. Mirrors the iOS `DesignSystemPreviewView`.
 */
@Composable
fun DesignSystemPreviewView(onBack: () -> Unit = {}) {
    var genre by remember { mutableStateOf(Genre.FANTASY) }

    SagAITheme(genre = genre) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            painterResource(R.drawable.ic_back_left),
                            contentDescription = stringResource(R.string.back_button_description),
                        )
                    }
                    Text(
                        stringResource(R.string.design_system_preview_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    )
                }

                GenrePicker(selected = genre, onSelect = { genre = it })
                HeaderFontSample(genre)
                BubbleShapeSample(genre)
                GlowBorderSample(genre)
                RotatingStrokeSample(genre)
                ShaderFilterSample(genre)

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SampleLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
    )
}

@Composable
private fun GenrePicker(
    selected: Genre,
    onSelect: (Genre) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(Genre.entries) { g ->
            val isSelected = g == selected
            Text(
                stringResource(g.title),
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) g.iconColor else MaterialTheme.colorScheme.onBackground,
                    ),
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) g.color else MaterialTheme.colorScheme.surfaceContainer)
                        .clickable { onSelect(g) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun HeaderFontSample(genre: Genre) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SampleLabel(stringResource(R.string.design_system_preview_header_font_label))
        genre.stylisedText(
            stringResource(genre.title).uppercase(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun BubbleShapeSample(genre: Genre) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SampleLabel(stringResource(R.string.design_system_preview_bubble_label))
        Text(
            stringResource(
                R.string.design_system_preview_bubble_sample_text,
                stringResource(genre.title),
            ),
            color = Color.White,
            modifier =
                Modifier
                    .clip(genre.bubble())
                    .background(genre.color)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun GlowBorderSample(genre: Genre) {
    val shape = RoundedCornerShape(50)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SampleLabel(stringResource(R.string.design_system_preview_glow_label))
        Text(
            stringResource(R.string.home_create_new_saga_title).uppercase(),
            style =
                MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                ),
            modifier =
                Modifier
                    .dropShadow(shape, Shadow(10.dp, Brush.verticalGradient(genre.colorPalette())))
                    .border(1.dp, Brush.verticalGradient(genre.colorPalette()), shape)
                    .background(Color.Black, shape)
                    .padding(horizontal = 32.dp, vertical = 16.dp),
        )
    }
}

/**
 * Same [rememberRotatingBorderAngle]/[Modifier.rotatingGradientBorder] primitive used on a
 * generating [ChatBubble]'s border — not a simplified stand-in, so this preview validates the
 * exact thing that ships, just applied to a different [shape].
 */
@Composable
private fun RotatingStrokeSample(genre: Genre) {
    val rotationValue = rememberRotatingBorderAngle()
    val shape = RoundedCornerShape(16.dp)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SampleLabel(stringResource(R.string.design_system_preview_rotating_stroke_label))
        Box(
            Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer, shape)
                .rotatingGradientBorder(
                    shape = shape,
                    colors = genre.colorPalette(),
                    rotationDegrees = rotationValue,
                ),
        )
    }
}

@Composable
private fun ShaderFilterSample(genre: Genre) {
    var boosted by remember { mutableStateOf(true) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Switch(checked = boosted, onCheckedChange = { boosted = it })
            SampleLabel(stringResource(R.string.design_system_preview_shader_label))
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(sagaShape())
                .background(Brush.linearGradient(genre.colorPalette()))
                .let { if (boosted) it.effectForGenre(genre) else it },
        )
    }
}
