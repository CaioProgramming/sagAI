package com.ilustris.sagai.features.home.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.core.ai.model.LocalGenreVisualConfig

@Composable
fun PersonalizedHomeHeader(
    userName: String,
    currentGenre: Genre?,
    scrollState: LazyListState,
) {
    val headerHeight = 120.dp
    val headerHeightPx = headerHeight.value

    // Calculate collapse progress
    val scrollOffset by remember(scrollState) {
        derivedStateOf { scrollState.firstVisibleItemScrollOffset.toFloat() }
    }

    val collapseProgress = (scrollOffset / headerHeightPx).coerceIn(0f, 1f)

    // Colors
    val genreColor =
        currentGenre?.resolveColor(LocalGenreVisualConfig.current) ?: MaterialTheme.colorScheme.primary
    val animatedGenreColor by animateColorAsState(genreColor)
    val animatedTitleAlpha by animateFloatAsState((1f - collapseProgress).coerceIn(0f, 1f))
    val animatedTitleSize by animateFloatAsState(1f - (collapseProgress * 0.3f).coerceIn(0f, 0.3f))
    val animatedSubtitleAlpha by animateFloatAsState((1f - collapseProgress * 1.5f).coerceIn(0f, 1f))

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Spark icon with animated position
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_spark),
                    contentDescription = null,
                    modifier =
                        Modifier
                            .size(24.dp)
                            .offset(
                                x = (collapseProgress * -8).dp,
                                y = (collapseProgress * -4).dp,
                            ),
                    colorFilter =
                        androidx.compose.ui.graphics.ColorFilter.tint(
                            animatedGenreColor
                        ),
                )
            }

            // Main title: "Eai, [Name]"
            Text(
                buildString {
                    append(stringResource(R.string.home_greeting_prefix))
                    append(userName.ifBlank { stringResource(R.string.home_greeting_default) })
                },
                style =
                    MaterialTheme.typography.headlineMedium.copy(
                        fontSize = (28 * animatedTitleSize).sp,
                        fontWeight = FontWeight.Bold,
                    ),
                color = MaterialTheme.colorScheme.onPrimary,
                modifier =
                    Modifier
                        .alpha(animatedTitleAlpha)
                        .fillMaxWidth(),
            )

            // Subtitle with genre color
            Text(
                stringResource(R.string.home_prompt_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = animatedGenreColor,
                modifier =
                    Modifier
                        .alpha(animatedSubtitleAlpha)
                        .fillMaxWidth(),
            )
        }
    }

    // Collapsed toolbar (shown when collapseProgress > 0.8)
    if (collapseProgress > 0.7f) {
        val toolbarAlpha = (collapseProgress - 0.7f) / 0.3f

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .alpha(toolbarAlpha.coerceIn(0f, 1f))
                    .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                stringResource(R.string.sagas_title),
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}



