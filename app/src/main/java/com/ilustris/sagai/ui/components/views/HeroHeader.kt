package com.ilustris.sagai.ui.components.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.components.AutoResizeText
import com.ilustris.sagai.ui.components.stylisedText
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.fadeGradientTop
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.sagaBrush

/** Top scrim used inside a [DepthLayout]'s hero image so the pinned title stays legible while scrolling. */
@Composable
fun heroScrimOverlay(adaptiveColor: Color) {
    Column {
        Box(
            Modifier
                .background(adaptiveColor)
                .fillMaxWidth()
                .height(50.dp),
        )
        Box(
            Modifier
                .background(fadeGradientTop(adaptiveColor))
                .fillMaxWidth()
                .height(50.dp),
        )
    }
}

/** Big genre-stylised title overlaid on a hero portrait, gradient-filled and shimmering. */
@Composable
fun heroTitleOverlay(
    text: String,
    genre: Genre,
    adaptiveColor: Color,
    titleGradient: Brush,
    shimmerColors: List<Color>,
    modifier: Modifier = Modifier,
) {
    genre.stylisedText(
        text = text,
        modifier =
            modifier
                .background(fadeGradientTop(adaptiveColor))
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .gradientFill(titleGradient)
                .reactiveShimmer(true, shimmerColors),
    )
}

data class HeroAction(
    val icon: Painter,
    val contentDescription: String,
    val onClick: () -> Unit,
    val tint: Color = Color.Unspecified,
    val primary: Boolean = false,
)

/** Row of small pill/icon actions pinned to the bottom of a hero portrait, over a generous fade. */
@Composable
fun BoxScope.HeroActionRow(
    adaptiveColor: Color,
    actions: List<HeroAction>,
    modifier: Modifier = Modifier,
) {
    if (actions.isEmpty()) return

    Box(
        modifier =
            modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(140.dp)
                .background(fadeGradientBottom(adaptiveColor)),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 16.dp),
        ) {
            actions.forEach { action ->
                Image(
                    action.icon,
                    action.contentDescription,
                    modifier =
                        Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .gradientFill(sagaBrush())
                            .clickable(onClick = action.onClick),
                    colorFilter = ColorFilter.tint(action.tint),
                )
            }
        }
    }
}

/**
 * Status tag + big genre-stylised title + optional action-icon row, bottom-pinned inside a hero
 * portrait (Apple Music artist-page style). Always drawn in front of the portrait — pass this as a
 * sibling *after* [DepthLayout] rather than inside its `content` slot, so the subject cutout never
 * occludes the text.
 */
@Composable
fun heroBottomCluster(
    title: String,
    genre: Genre,
    adaptiveColor: Color,
    adaptiveTextColor: Color,
    titleGradient: Brush,
    shimmerColors: List<Color>,
    accentColor: Color,
    onAccentColor: Color,
    statusTag: String? = null,
    actions: List<HeroAction> = emptyList(),
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            modifier
                .fillMaxWidth(),
    ) {
        if (statusTag != null) {
            Text(
                statusTag,
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        color = adaptiveTextColor,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                    ),
                modifier =
                    Modifier
                        .alpha(.5f)
                        .padding(top = 24.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(adaptiveColor.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }

        genre.stylisedText(
            text = title,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .reactiveShimmer(true, shimmerColors),
        )

        if (actions.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp),
            ) {
                actions.forEach { action ->
                    val buttonSize = if (action.primary) 82.dp else 44.dp
                    IconButton(
                        onClick = {
                            action.onClick()
                        },
                        colors =
                            IconButtonDefaults.iconButtonColors().copy(
                                containerColor = if (action.primary) accentColor else accentColor.copy(alpha = 0.2f),
                                contentColor = if (action.primary) onAccentColor else accentColor,
                            ),
                        modifier = Modifier.size(buttonSize).padding(if (action.primary) 18.dp else 10.dp),
                    ) {
                        Icon(
                            action.icon,
                            action.contentDescription,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        } else {
            Spacer(Modifier.height(8.dp))
        }
    }
}

data class HeroMenuAction(
    val label: String,
    val destructive: Boolean = false,
    val onClick: () -> Unit,
)

/** "..." overflow-menu trigger. Caller controls placement (works inside a Box, a Row, anywhere). */
@Composable
fun HeroOverflowMenu(
    tint: Color,
    actions: List<HeroMenuAction>,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
) {
    if (actions.isEmpty()) return
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(36.dp),
            colors = IconButtonDefaults.iconButtonColors(containerColor = containerColor),
        ) {
            Icon(
                painterResource(R.drawable.ic_menu),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.padding(6.dp).fillMaxSize(),
            )
        }

        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            actions.forEach { action ->
                DropdownMenuItem(
                    text = {
                        Text(
                            action.label,
                            color = if (action.destructive) MaterialTheme.colorScheme.error else Color.Unspecified,
                        )
                    },
                    onClick = {
                        expanded = false
                        action.onClick()
                    },
                )
            }
        }
    }
}
