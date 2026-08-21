package com.ilustris.sagai.features.saga.detail.review.ui.templates.collage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.ui.animations.imageStroke
import com.ilustris.sagai.ui.components.views.DepthLayout
import com.ilustris.sagai.ui.components.views.DepthLayoutViewModel

private val STICKER_SIZE = 96.dp

/** The most-present character (index 0, already ranked by message count) gets a bigger sticker — the visual "destaque" the default review's own top-character moment gives it. */
private val FEATURED_STICKER_SIZE = 156.dp

/**
 * A background-free character cutout with a per-character-color double outline, glued to a torn
 * paper caption below it — the collage template's basic unit for a single character.
 */
@Composable
fun CharacterSticker(
    character: Character,
    accentColor: Color,
    caption: String,
    featured: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val strokeWidthPx = with(LocalDensity.current) { 4.dp.toPx() }
    val stickerSize = if (featured) FEATURED_STICKER_SIZE else STICKER_SIZE

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        DepthLayout(
            imagePath = character.image,
            modifier = Modifier.size(stickerSize),
            backgroundImageModifier = Modifier.alpha(0f),
            foregroundImageModifier =
                Modifier
                    .imageStroke(MaterialTheme.colorScheme.background, strokeWidthPx)
                    .imageStroke(accentColor, strokeWidthPx * 1.3f),
            viewModel = hiltViewModel<DepthLayoutViewModel>(key = "collage_character_${character.id}"),
        ) {}

        // A torn note rather than a rounded chip — same scrap language as the rest of the
        // template, so the cutout and its caption read as one pasted-up piece.
        TornPaperScrap(
            seed = character.id + 200,
            paperColor = accentColor,
            modifier = Modifier.padding(top = 2.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = character.name,
                    color = accentColor.readableTextColor(),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = caption,
                    color = accentColor.readableTextColor().copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}
