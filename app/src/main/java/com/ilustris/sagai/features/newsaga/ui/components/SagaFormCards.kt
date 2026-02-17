@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.ilustris.sagai.features.newsaga.ui.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.newsaga.data.model.CreationSuggestion
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.data.model.resolveBackground
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.newsaga.data.model.resolveIconColor
import com.ilustris.sagai.features.newsaga.data.model.shimmerColors
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.ui.theme.bodyFont
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
fun EditCard(
    textValue: String,
    onTextChange: (String) -> Unit,
    genre: Genre,
    hint: String?,
    suggestions: List<CreationSuggestion>,
    isLoading: Boolean,
    onSeedClick: (CreationSuggestion) -> Unit,
    onEnhanceClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = genre.bubble(tailWidth = 0.dp, tailHeight = 0.dp, isNarrator = true)
    val brush = genre.gradient(true)
    val bodyFont = genre.bodyFont()
    val headerFont = genre.headerFont()

    val resolvedColor = genre.resolveColor()
    val resolvedIconColor = genre.resolveIconColor()

    Box(
        modifier =
            modifier
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
            painter = rememberAsyncImagePainter(model = genre.resolveBackground()),
            null,
            Modifier
                .align(Center)
                .size(50.dp)
                .gradientFill(genre.gradient(true, targetValue = 100f)),
            colorFilter = ColorFilter.tint(resolvedIconColor.copy(alpha = .15f)),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .reactiveShimmer(isLoading, genre.shimmerColors()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header label and AI ASSISTED tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "DESCRIBE YOUR STORY",
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            fontFamily = headerFont,
                            letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing * 1.5,
                        ),
                    color = resolvedIconColor.copy(alpha = .6f),
                )

                Box(
                    modifier =
                        Modifier
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = "AI ASSISTED",
                        style =
                            MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                            ),
                        color = Color.White.copy(alpha = 0.5f),
                    )
                }
            }

            // Text input area
            BasicTextField(
                value = textValue,
                onValueChange = onTextChange,
                textStyle =
                    MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = bodyFont,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                cursorBrush = SolidColor(resolvedIconColor),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                decorationBox = { innerTextField ->
                    Box(
                        contentAlignment = Alignment.TopStart,
                    ) {
                        if (textValue.isEmpty()) {
                            Text(
                                text = hint ?: "Tell us your story idea...",
                                style =
                                    MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = bodyFont,
                                    ),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .2f),
                            )
                        }
                        innerTextField()
                    }
                },
            )

            // Bottom bar with Magic Wand and counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEnhanceClick,
                        modifier =
                            Modifier
                                .background(resolvedColor.copy(alpha = 0.1f), CircleShape)
                                .size(32.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_idea), // Using ic_idea as magic wand for now
                            contentDescription = "Enhance",
                            modifier = Modifier.size(16.dp),
                            tint = resolvedColor,
                        )
                    }
                }

                Text(
                    text = "${textValue.length} / 2000 characters",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                )
            }

            // Suggestions at the bottom
            if (suggestions.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "💡 STORY SEEDS",
                        style =
                            MaterialTheme.typography.labelSmall.copy(
                                fontFamily = headerFont,
                            ),
                        color = resolvedIconColor.copy(alpha = .4f),
                    )
                    StorySeedRow(
                        suggestions = suggestions,
                        genre = genre,
                        onSeedClick = onSeedClick,
                    )
                }
            }
        }
    }
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

    val resolvedColor = genre.resolveColor()
    val resolvedIconColor = genre.resolveIconColor()

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
            painter = rememberAsyncImagePainter(model = genre.resolveBackground()),
            null,
            Modifier
                .align(Center)
                .size(50.dp)
                .gradientFill(genre.gradient(true, targetValue = 100f)),
            colorFilter = ColorFilter.tint(resolvedIconColor),
        )

        Column(
            modifier
                .animateContentSize()
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
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
                                resolvedColor,
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
