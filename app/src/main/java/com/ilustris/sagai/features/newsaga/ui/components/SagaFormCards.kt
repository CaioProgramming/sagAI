@file:OptIn(ExperimentalSharedTransitionApi::class)

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

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
    sagaForm: SagaForm,
    onDismiss: () -> Unit,
    modifier: Modifier,
) {
    var cardFace by remember {
        mutableStateOf(CardFace.Front)
    }

    LaunchedEffect(Unit) {
        delay(5.seconds)
        cardFace = cardFace.next
        delay(5.seconds)
        cardFace = cardFace.next
    }

    FlipCard(
        cardFace,
        onClick = {
            cardFace = it.next
        },
        front = {
            ReviewCard(
                sagaForm.saga.title,
                sagaForm.saga.genre?.title ?: emptyString(),
                sagaForm.saga.description,
                sagaForm.saga.genre,
            )
        },
        back = {
            ReviewCard(
                sagaForm.character.name,
                sagaForm.character.gender,
                sagaForm.character.description,
                sagaForm.saga.genre,
            )
        },
        modifier =
            Modifier.fillMaxSize(),
    )
}

@Composable
fun ReviewCard(
    title: String,
    subtitle: String,
    content: String,
    genre: Genre?,
    modifier: Modifier = Modifier,
) {
    val brush = genre?.gradient(true) ?: Brush.verticalGradient(holographicGradient)

    val cornerSize by animateDpAsState(
        targetValue = genre.cornerSize(),
        label = "cornerSize",
    )

    val shape = RoundedCornerShape(cornerSize)
    val font = genre?.bodyFont()
    val headerFont = genre?.headerFont()

    Column(
        modifier
            .fillMaxSize()
            .clip(shape)
            .border(2.dp, brush, shape)
            .background(MaterialTheme.colorScheme.surfaceContainer, shape)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .reactiveShimmer(true),
    ) {
        Text(
            text = title,
            style =
                MaterialTheme.typography.titleLarge.copy(
                    fontFamily = headerFont,
                    brush = brush,
                ),
            modifier = Modifier.padding(vertical = 8.dp),
        )

        Text(
            text = subtitle,
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontFamily = font,
                    brush = brush,
                ),
            modifier = Modifier.alpha(.6f),
        )

        Text(
            text = content,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = font,
                    brush = brush,
                ),
            color = MaterialTheme.colorScheme.onBackground,
        )
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
            ) {
                back()
            }
        }
    }
}
