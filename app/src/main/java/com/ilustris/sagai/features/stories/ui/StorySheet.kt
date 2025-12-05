package com.ilustris.sagai.features.stories.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.stories.data.model.StoryDailyBriefing
import com.ilustris.sagai.ui.components.StarryLoader
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.headerFont

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StorySheet(
    sagaContent: SagaContent,
    storyDailyBriefing: StoryDailyBriefing?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onContinue: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var scale by remember { mutableFloatStateOf(1f) }

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dragAmount ->
                            dragOffsetY += dragAmount
                            scale = (1f - (dragOffsetY / 1000f)).coerceIn(0.8f, 1f)
                        },
                        onDragEnd = {
                            if (dragOffsetY < -200) {
                                onContinue()
                            } else {
                                dragOffsetY = 0f
                                scale = 1f
                            }
                        }
                    )
                }
        ) {
            val imageAlpha by animateFloatAsState(targetValue = 1f - (dragOffsetY / -500f).coerceIn(0f, 1f))

            AsyncImage(
                model = sagaContent.data.icon,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationY = dragOffsetY / 2
                    )
                    .alpha(imageAlpha)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.5f),
                                Color.Black
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )

            if (isLoading || storyDailyBriefing == null) {
                StarryLoader(modifier = Modifier.align(Alignment.Center))
            } else {
                val pagerState = rememberPagerState { 2 }
                Column(
                    modifier = Modifier.fillMaxSize().offset(y = dragOffsetY.dp),
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
                                title = "Previously on ${sagaContent.data.title}",
                                content = storyDailyBriefing.summary
                            )
                            1 -> StoryPage(
                                title = "The history continues",
                                content = storyDailyBriefing.hook
                            )
                        }
                    }
                    Button(
                        onClick = { onContinue() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(text = "Continue Saga")
                    }
                }
            }
        }
    }
}

@Composable
fun StoryPage(title: String, content: String) {
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
            fontFamily = headerFont(),
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = bodyFont(),
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}