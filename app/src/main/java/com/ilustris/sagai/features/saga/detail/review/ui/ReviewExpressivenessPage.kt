package com.ilustris.sagai.features.saga.detail.review.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.detail.data.model.ReviewStage
import com.ilustris.sagai.ui.theme.headerFont
import kotlin.random.Random

class ReviewExpressivenessPage(
    private val stage: ReviewStage,
    private val genre: Genre,
    private val totalActivity: Int,
) : ReviewPage {
    @Composable
    override fun Show(
        modifier: Modifier,
        onAction: (ReviewAction) -> Unit,
    ) {
        val counterProgress = remember { Animatable(0f) }

        LaunchedEffect(Unit) {
            counterProgress.animateTo(
                1f,
                animationSpec = tween(durationMillis = 1500, easing = LinearEasing),
            )
        }

        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            DynamicLinework(
                color = genre.color,
                lineCount = Random.nextInt(8, 15),
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize(),
            ) {
                stage.hook?.let {
                    ReviewTextDisplay(
                        title = it.title,
                        subtitle = it.subtitle,
                        genre = genre,
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // The Big Counter
                val animatedValue = (totalActivity * counterProgress.value).toInt()

                StrokedText(
                    text = animatedValue.toString(),
                    style =
                        MaterialTheme.typography.displayLarge.copy(
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = genre.headerFont(),
                            color = genre.color,
                        ),
                    strokeColor = Color.White,
                    strokeWidth = 12f,
                )

                Spacer(modifier = Modifier.height(32.dp))

                stage.content?.let {
                    ReviewTextDisplay(
                        title = it.title,
                        subtitle = it.subtitle,
                        genre = genre,
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
