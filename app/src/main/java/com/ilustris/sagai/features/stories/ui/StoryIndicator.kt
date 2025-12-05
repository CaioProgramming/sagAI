package com.ilustris.sagai.features.stories.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.headerFont
import kotlinx.coroutines.launch
import androidx.compose.foundation.pager.PagerState
import com.ilustris.sagai.ui.theme.cornerSize

@Composable
fun StoryIndicator(
    pagerState: PagerState,
    pageCount: Int,
    sagaTitle: String,
    genre: Genre,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            repeat(pageCount) { index ->
                val isSelected = index <= pagerState.currentPage
                val progress by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.5f,
                    animationSpec = tween(500),
                    label = "progressAnimation",
                )
                Box(
                    modifier = Modifier
                        .clickable {
                            if (pagerState.currentPage != index) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(
                                        index,
                                        animationSpec = tween(500),
                                    )
                                }
                            }
                        }
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                        .height(3.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onBackground.copy(
                                alpha = progress,
                            ),
                            shape = RoundedCornerShape(genre.cornerSize()),
                        ),
                )
            }
        }
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Image(
                painterResource(R.drawable.ic_spark),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
            )
            Text(
                sagaTitle,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = genre.headerFont(),
                    color = MaterialTheme.colorScheme.onBackground,
                ),
            )
        }
    }
}