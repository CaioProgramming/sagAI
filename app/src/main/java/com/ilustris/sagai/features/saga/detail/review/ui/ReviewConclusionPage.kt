package com.ilustris.sagai.features.saga.detail.review.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.share.domain.model.ShareType
import com.ilustris.sagai.ui.theme.filters.effectForGenre
import com.ilustris.sagai.ui.theme.reactiveShimmer
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

class ReviewConclusionPage(
    override val content: SagaContent,
) : ReviewPage {
    override val pageType: ReviewPageType = ReviewPageType.CONCLUSION

    @Composable
    override fun Show(
        modifier: Modifier,
        canAnimate: Boolean,
        onAction: (ReviewAction) -> Unit,
    ) {
        val genre = content.data.genre
        val mainCharacter = content.mainCharacter ?: return
        val supportingCharacters = content.getCharacters(filterMainCharacter = true)
        var showShareButton by remember {
            mutableStateOf(false)
        }
        var showText by remember { mutableStateOf(!canAnimate) }

        LaunchedEffect(Unit) {
            delay(1.seconds)
            showText = true
        }

        LaunchedEffect(Unit) {
            delay(3.seconds)
            showShareButton = true
        }
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            SagaLegendLayout(
                mainCharacter = mainCharacter,
                supportingCharacters = supportingCharacters,
                sagaIcon = content.data.icon,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .effectForGenre(genre),
            )

            AnimatedVisibility(
                visible = showText,
                enter =
                    androidx.compose.animation.fadeIn(tween(800)) +
                        slideInVertically(
                            tween(800, easing = androidx.compose.animation.core.EaseOutBack),
                        ) { it / 2 },
                modifier = Modifier.align(Alignment.Center),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    val conclusion =
                        content.data.review
                            ?.conclusion
                            ?.content
                    conclusion?.title?.let {
                        StrokedText(
                            text = it.uppercase(),
                            style =
                                MaterialTheme.typography.headlineLarge.copy(
                                    fontFamily = MaterialTheme.typography.headlineSmall.fontFamily,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onBackground,
                                ),
                            textAlign = TextAlign.Center,
                            strokeColor = MaterialTheme.colorScheme.background,
                            strokeWidth = 15f,
                            modifier =
                                Modifier
                                    .offset(y = 100.dp)
                                    .reactiveShimmer(true, repeatMode = RepeatMode.Restart),
                        )
                    }

                    conclusion?.subtitle?.let {
                        Text(
                            text = it,
                            style =
                                MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                                    shadow =
                                        Shadow(
                                            MaterialTheme.colorScheme.primary,
                                            Offset(5f, 0f),
                                            10f,
                                        ),
                                ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.offset(y = 100.dp),
                        )
                    }
                }
            }

            AnimatedVisibility(
                showShareButton,
                modifier =
                    Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
            ) {
                Button(
                    onClick = {
                        onAction(ReviewAction.Share(ShareType.RELATIONS))
                    },
                    colors =
                        ButtonDefaults.elevatedButtonColors().copy(
                            containerColor = MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                ) {
                    Text(stringResource(R.string.share))
                }
            }
        }
    }
}
