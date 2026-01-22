package com.ilustris.sagai.features.saga.detail.review.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.detail.data.model.ReviewText
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.headerFont
import kotlin.random.Random

class ReviewHookPage(
    private val hook: ReviewText,
    private val genre: Genre,
) : ReviewPage {
    @Composable
    override fun Show(
        modifier: Modifier,
        onAction: (ReviewAction) -> Unit,
    ) {
        var showSubtitle by remember { mutableStateOf(false) }

        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            DynamicLinework(
                genre.color,
                Random.nextInt(5, 12),
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                hook.title?.let {
                    SimpleTypewriterText(
                        text = it,
                        style =
                            MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = genre.headerFont(),
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground,
                            ),
                        textAlign = TextAlign.Center,
                        strokeColor = MaterialTheme.colorScheme.background,
                        onAnimationFinished = {
                            showSubtitle = true
                        },
                    )
                }

                if (showSubtitle) {
                    Spacer(modifier = Modifier.height(16.dp))
                    hook.subtitle?.let {
                        SimpleTypewriterText(
                            text = it,
                            style =
                                MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = genre.bodyFont(),
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                ),
                            textAlign = TextAlign.Center,
                            strokeColor = MaterialTheme.colorScheme.background,
                        )
                    }
                }
            }
        }
    }
}
