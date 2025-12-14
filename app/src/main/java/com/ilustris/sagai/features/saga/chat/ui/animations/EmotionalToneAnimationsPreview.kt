package com.ilustris.sagai.features.saga.chat.ui.animations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.chat.BubbleTailAlignment

@Preview(showBackground = true)
@Composable
fun EmotionalToneAnimationsPreview() {
    var globalAnimationTrigger by remember { mutableStateOf(false) }
    var selectedTone by remember { mutableStateOf<EmotionalTone?>(null) }
    val bubbleShape =
        remember { Genre.entries.random().bubble(BubbleTailAlignment.entries.random()) }

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (grid, button) = createRefs()

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .constrainAs(grid) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(button.top, margin = 8.dp)
                    height = Dimension.fillToConstraints
                }
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(EmotionalTone.entries) { tone ->
                AnimationPreviewItem(
                    tone = tone,
                    isGloballyAnimated = globalAnimationTrigger,
                    isSelected = selectedTone == tone,
                    shape = bubbleShape,
                    onClick = {
                        selectedTone = if (selectedTone == tone) null else tone
                    }
                )
            }
        }

        Button(
            onClick = {
                globalAnimationTrigger = !globalAnimationTrigger
                selectedTone = null // Reset individual selection
            },
            modifier = Modifier
                .constrainAs(button) {
                    start.linkTo(parent.start, margin = 16.dp)
                    end.linkTo(parent.end, margin = 16.dp)
                    bottom.linkTo(parent.bottom, margin = 16.dp)
                    width = Dimension.fillToConstraints
                }
        ) {
            Text(if (globalAnimationTrigger) "Reset All" else "Animate All")
        }
    }
}

@Composable
fun AnimationPreviewItem(
    tone: EmotionalTone,
    isGloballyAnimated: Boolean,
    isSelected: Boolean,
    shape: Shape,
    onClick: () -> Unit
) {
    // Randomly select a genre for visual variety
    val genre = remember { Genre.entries.random() }
    val shouldAnimate = isGloballyAnimated || isSelected

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Text(
            text = tone.name,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(bottom = 8.dp),
        )


        // Get genre-specific bubble shape

        Box(
            modifier = Modifier
                .size(140.dp, 80.dp)
                .emotionalEntrance(tone, shouldAnimate)
                .clip(shape)
                .background(
                    color = tone.color.copy(alpha = 0.8f)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Use regular Text instead of TypewriterText to avoid animation conflicts
            Text(
                text = "Sample\nMessage",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = genre.bodyFont()
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp)
            )
        }

        if (isSelected) {
            Text(
                text = "Tap again to reset",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
