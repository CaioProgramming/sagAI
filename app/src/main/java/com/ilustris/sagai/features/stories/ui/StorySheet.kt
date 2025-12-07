package com.ilustris.sagai.features.stories.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.ui.SagaBriefing
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.features.newsaga.data.model.shimmerColors
import com.ilustris.sagai.ui.components.stylisedText
import com.ilustris.sagai.ui.components.views.DepthLayout
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.fadedGradientTopAndBottom
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shape
import effectForGenre
import kotlinx.coroutines.launch


@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalSharedTransitionApi::class
)
@Composable
fun StorySheet(
    sagaBriefing: SagaBriefing?,
    onDismiss: () -> Unit,
    onContinue: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var scale by remember { mutableFloatStateOf(1f) }
    val coroutineScope = rememberCoroutineScope()


    sagaBriefing?.let {
        val sagaContent = sagaBriefing.saga
        val genre = sagaContent.data.genre

        ModalBottomSheet(
            onDismissRequest = { onDismiss() },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background,
            dragHandle = { Box {} },
            shape = genre.shape()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                val segmentationPair = sagaBriefing.segmentationPair
                val deepEffectAvailable = segmentationPair != null
                AnimatedContent(deepEffectAvailable) {
                    if (it) {
                        Box(Modifier.fillMaxSize()) {
                            segmentationPair?.let {
                                DepthLayout(
                                    it.first,
                                    it.second,
                                    modifier = Modifier.fillMaxSize(),
                                    imageModifier = Modifier
                                        .selectiveColorHighlight(genre.selectiveHighlight())
                                        .effectForGenre(genre)
                                ) {
                                    genre.stylisedText(
                                        sagaContent.data.title,
                                        fontSize = MaterialTheme.typography.displaySmall.fontSize,
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .offset(y = 100.dp)
                                            .reactiveShimmer(true)
                                            .padding(4.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        AsyncImage(
                            model = sagaBriefing.saga.data.icon,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale
                                )
                        )
                    }
                }


                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            fadedGradientTopAndBottom(genre.color)
                        )
                )

                val pagerState = rememberPagerState { 2 }
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    StoryIndicator(
                        pagerState = pagerState,
                        pageCount = 2,
                        sagaTitle = sagaContent.data.title,
                        genre = sagaContent.data.genre,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) { page ->
                        when (page) {
                            0 -> StoryPage(
                                title = stringResource(
                                    R.string.story_sheet_title_previously_on,
                                    sagaContent.data.title
                                ),
                                content = sagaBriefing.briefing.summary,
                                genre
                            )

                            1 -> StoryPage(
                                title = stringResource(R.string.story_sheet_title_history_continues),
                                content = sagaBriefing.briefing.hook,
                                genre

                            )
                        }
                    }
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                onContinue()
                            }
                        },
                        modifier = Modifier
                            .reactiveShimmer(true, genre.shimmerColors())
                            .fillMaxWidth()
                            .padding(32.dp),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = genre.iconColor
                        )
                    ) {
                        Text(text = stringResource(R.string.story_sheet_button_continue_saga))
                    }
                }
            }
        }
    }

}

@Composable
fun StoryPage(title: String, content: String, genre: Genre) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontFamily = genre.headerFont(),
            color = genre.iconColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = genre.bodyFont(),
            color = genre.iconColor,
            textAlign = TextAlign.Start
        )
    }
}