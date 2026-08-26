package com.ilustris.sagai.features.saga.detail.review.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.saga.detail.review.presentation.ReviewShareViewModel
import com.ilustris.sagai.features.share.ui.launchShareActivity
import com.ilustris.sagai.ui.theme.SagaTitle
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.sagaShape
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

/** Story friendly proportion — the card is 9:16 no matter how tall the device is. */
private const val SHARE_CARD_ASPECT = 9f / 16f

/** How much of the dialog the preview is allowed to take. */
private const val PREVIEW_WIDTH_FRACTION = 0.88f
private const val PREVIEW_HEIGHT_FRACTION = 0.72f

/** Reserved band at the bottom of the card for the saga name and the app signature. */
private val FOOTER_HEIGHT = 96.dp

/** Lets the page settle (images decoded, text laid out) before the snapshot is taken. */
private val CAPTURE_DELAY = 1.5.seconds

/**
 * Shares the review page the reader is on by drawing it again inside a 9:16 card.
 * The page renders itself, so the card inherits the genre styling for free — no
 * parallel share layout to keep in sync with the experience.
 */
@Composable
fun ReviewShareCard(
    page: ReviewPage,
    onDismiss: () -> Unit,
    viewModel: ReviewShareViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val graphicsLayer = rememberGraphicsLayer()
    val coroutineScope = rememberCoroutineScope()
    val shareUri by viewModel.shareUri.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val saga = page.content.data

    suspend fun captureCard() {
        viewModel.shareCard(
            bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap(),
            fileName = "review_${page.pageType.name.lowercase()}",
        )
    }

    Dialog(
        onDismissRequest = {
            viewModel.clear()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            // The card is laid out at full width so the capture keeps the device's pixel
            // density, then scaled down to fit the dialog — the record is untouched by it.
            val cardWidth = maxWidth
            val cardHeight = cardWidth / SHARE_CARD_ASPECT
            val previewScale =
                min(
                    PREVIEW_WIDTH_FRACTION,
                    (maxHeight * PREVIEW_HEIGHT_FRACTION) / cardHeight,
                )

            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = .95f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        viewModel.clear()
                        onDismiss()
                    },
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(cardWidth * previewScale, cardHeight * previewScale)
                            .clip(sagaShape()),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .requiredSize(cardWidth, cardHeight)
                                .scale(previewScale)
                                .drawWithContent {
                                    graphicsLayer.record {
                                        this@drawWithContent.drawContent()
                                    }
                                    drawLayer(graphicsLayer)
                                }.background(MaterialTheme.colorScheme.background),
                    ) {
                        CompositionLocalProvider(LocalReviewCapture provides true) {
                            page.Background(Modifier.fillMaxSize())

                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .padding(bottom = FOOTER_HEIGHT),
                                contentAlignment = Alignment.Center,
                            ) {
                                page.Show(
                                    modifier = Modifier.fillMaxSize(),
                                    canAnimate = false,
                                )
                            }
                        }

                        ReviewShareCardFooter(
                            saga = saga,
                            modifier = Modifier.align(Alignment.BottomCenter),
                        )
                    }
                }

                Button(
                    onClick = {
                        coroutineScope.launch { captureCard() }
                    },
                    enabled = !isSaving,
                    border = BorderStroke(1.dp, saga.genre.gradient()),
                    colors =
                        ButtonDefaults.elevatedButtonColors().copy(
                            containerColor = MaterialTheme.colorScheme.onBackground,
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                    modifier = Modifier.padding(24.dp),
                ) {
                    Text(stringResource(R.string.share))
                }
            }
        }
    }

    LaunchedEffect(page.pageType) {
        delay(CAPTURE_DELAY)
        captureCard()
    }

    LaunchedEffect(shareUri) {
        shareUri?.let { launchShareActivity(it, context) }
    }
}

@Composable
private fun ReviewShareCardFooter(
    saga: Saga,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(FOOTER_HEIGHT)
                .background(fadeGradientBottom(MaterialTheme.colorScheme.background)),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = saga.title,
                style =
                    MaterialTheme.typography.titleSmall.copy(
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            SagaTitle(
                textStyle = MaterialTheme.typography.labelMedium,
                modifier = Modifier.alpha(.7f),
            )
        }
    }
}
