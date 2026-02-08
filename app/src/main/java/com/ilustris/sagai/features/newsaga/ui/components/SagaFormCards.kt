@file:OptIn(ExperimentalSharedTransitionApi::class)

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.reactiveShimmer

enum class CardFace(
    val angle: Float,
) {
    Front(0f) {
        override val next: CardFace
            get() = Back
    },
    Back(180f) {
        override val next: CardFace
            get() = Front
    }, ;

    abstract val next: CardFace
}

@Composable
fun SagaFormCards(
    cardFace: CardFace,
    sagaForm: SagaForm,
    modifier: Modifier,
    toggleCard: () -> Unit = {},
) {
    FlipCard(
        cardFace,
        onClick = {
            toggleCard()
        },
        front = {
            ReviewCard(
                sagaForm.saga.title,
                emptyString(),
                sagaForm.saga.description,
                sagaForm.saga.genre,
            )
        },
        back = {
            ReviewCard(
                sagaForm.character?.name ?: emptyString(),
                sagaForm.character?.gender ?: emptyString(),
                sagaForm.character?.description ?: emptyString(),
                sagaForm.saga.genre,
            )
        },
        modifier =
        modifier,
    )
}

@Composable
fun ReviewCard(
    title: String,
    subtitle: String,
    content: String,
    genre: Genre,
    modifier: Modifier = Modifier,
) {
    val brush = genre.gradient(true)

    val shape = genre.bubble(tailWidth = 0.dp, tailHeight = 0.dp, isNarrator = true)
    val font = genre.bodyFont()
    val headerFont = genre.headerFont()

    Box(
        contentAlignment = Center,
        modifier =
            Modifier
                .dropShadow(
                    shape,
                    {
                        this.brush = brush
                        this.radius = 10f
                        spread = 5f
                    },
                ).clip(shape)
                .border(2.dp, brush, shape)
                .background(MaterialTheme.colorScheme.background, shape),
    ) {
        Image(
            painterResource(genre.background),
            null,
            Modifier
                .align(Center)
                .size(50.dp)
                .gradientFill(genre.gradient(true, targetValue = 100f)),
            colorFilter = ColorFilter.tint(genre.iconColor),
        )

        Column(
            modifier
                .animateContentSize()
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .reactiveShimmer(true),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                style =
                    MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = headerFont,
                        textAlign = TextAlign.Center,
                        shadow =
                            Shadow(
                                genre.color,
                                blurRadius = 10f,
                            ),
                    ),
                modifier =
                    Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
            )

            Text(
                text = subtitle,
                style =
                    MaterialTheme.typography.labelMedium.copy(
                        fontFamily = font,
                    ),
                modifier =
                    Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
            )

            Text(
                text = content,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = font,
                    ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier =
                    Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
            )
        }
    }
}

@Composable
fun FlipCard(
    cardFace: CardFace,
    onClick: (CardFace) -> Unit,
    modifier: Modifier = Modifier,
    back: @Composable () -> Unit = {},
    front: @Composable () -> Unit = {},
) {
    val rotation =
        animateFloatAsState(
            targetValue = cardFace.angle,
            animationSpec =
                tween(
                    durationMillis = 400,
                    easing = FastOutSlowInEasing,
                ),
        )
    Box(
        modifier =
            modifier
                .clickable {
                    onClick(cardFace)
                }.graphicsLayer {
                    rotationY = rotation.value
                    cameraDistance = 12f * density
                },
    ) {
        if (rotation.value <= 90f) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Center,
            ) {
                front()
            }
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY = 180f
                    },
                contentAlignment = Center,
            ) {
                back()
            }
        }
    }
}
