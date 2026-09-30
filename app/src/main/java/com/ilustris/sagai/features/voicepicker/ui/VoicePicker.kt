package com.ilustris.sagai.features.voicepicker.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.core.ai.model.Voice
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.voicepicker.presentation.VoicePickerViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Picks the saga's narrator. One orb per voice in a centered pager; the orb is the selector, and
 * the one in the middle plays its greeting as soon as it settles. Tapping it pauses or replays.
 * Opens on the voice suggested for [genre], and leaves free scrolling to the rest.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VoicePicker(
    genre: Genre,
    onConfirm: (Voice) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VoicePickerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val genrePalette = genre.colorPalette()

    LaunchedEffect(genre) { viewModel.load(genre) }
    DisposableEffect(Unit) { onDispose { viewModel.stop() } }

    if (state.voices.isEmpty()) return

    val voices = state.voices
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = state.initialPage) { voices.size }
    LaunchedEffect(voices) {
        snapshotFlow { pagerState.settledPage }.collect { page -> voices.getOrNull(page)?.let(viewModel::play) }
    }
    val current = voices[pagerState.currentPage.coerceIn(voices.indices)]

    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(ORB_SIZE),
            contentPadding = PaddingValues(horizontal = (LocalConfiguration.current.screenWidthDp.dp - ORB_SIZE) / 2),
            flingBehavior = PagerDefaults.flingBehavior(pagerState, pagerSnapDistance = PagerSnapDistance.atMost(2)),
            verticalAlignment = Alignment.CenterVertically,
        ) { page ->
            val voice = voices[page]
            val selected = pagerState.currentPage == page
            VoiceOrb(
                palette = voice.orbColors(genrePalette),
                selected = selected,
                level = { viewModel.level.value },
                size = ORB_SIZE,
                modifier =
                    Modifier
                        .graphicsLayer {
                            val distance = abs((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).coerceAtMost(1f)
                            val scale = 1f - 0.28f * distance
                            scaleX = scale
                            scaleY = scale
                        }.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            if (selected) viewModel.toggle(voice) else scope.launch { pagerState.animateScrollToPage(page) }
                        },
            )
        }

        Spacer(Modifier.height(8.dp))
        Text(
            current.name.orEmpty(),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            current.tagline.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = { onConfirm(current) }, modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth()) {
            Text(stringResource(R.string.voice_picker_confirm, current.name.orEmpty()))
        }
    }
}

private fun Voice.orbColors(fallback: List<Color>): List<Color> =
    palette
        ?.mapNotNull { hex -> runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrNull() }
        ?.takeIf { it.isNotEmpty() }
        ?: fallback

private val ORB_SIZE = 240.dp
