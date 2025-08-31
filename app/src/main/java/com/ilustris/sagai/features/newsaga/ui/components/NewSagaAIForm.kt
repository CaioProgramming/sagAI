@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.ilustris.sagai.features.newsaga.ui.components

import ReviewCard
import SagaFormCards
import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseInElastic
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.RepeatMode.Reverse
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.data.model.isValid
import com.ilustris.sagai.features.newsaga.ui.presentation.FormState
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.theme.MorphPolygonShape
import com.ilustris.sagai.ui.theme.RoundedPolygonShape
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import com.ilustris.sagai.ui.theme.Typography
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.BlurredGlowContainer
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.solidGradient
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import com.skydoves.balloon.compose.Balloon
import com.skydoves.balloon.compose.BalloonWindow
import com.skydoves.balloon.compose.rememberBalloonBuilder
import com.skydoves.balloon.compose.setBackgroundColor
import com.skydoves.balloon.compose.setTextColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun NewSagaAIForm(
    sagaForm: SagaForm,
    isLoading: Boolean = false,
    aiState: FormState,
    savedSaga: Saga?,
    sendDescription: (String) -> Unit = {},
    selectGenre: (Genre) -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val genre = sagaForm.saga.genre
    val brush = genre?.gradient() ?: Brush.linearGradient(holographicGradient)
    val textFont = genre?.bodyFont()
    var showReview by remember { mutableStateOf(false) }
    var showText by remember { mutableStateOf(false) }
    val blurRadius by animateDpAsState(
        targetValue = if (showText) 50.dp else 0.dp,
        label = "BlurRadius",
    )

    LaunchedEffect(isLoading) {
        if (isLoading) {
            showText = false
        }
    }

    LaunchedEffect(aiState.readyToSave) {
        if (aiState.readyToSave) {
            showReview = true
        }
    }

    LaunchedEffect(aiState.message) {
        showText = aiState.message != null
    }

    val infiniteTransition = rememberInfiniteTransition()

    val shapeA =
        remember {
            RoundedPolygon.star(
                4,
                rounding = CornerRounding(5f),
            )
        }
    val shapeB =
        remember {
            RoundedPolygon.star(
                4,
                rounding = CornerRounding(0f),
            )
        }

    val morph =
        remember {
            Morph(shapeA, shapeB)
        }

    val morphProgress by
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    tween(3.seconds.toInt(DurationUnit.MILLISECONDS), easing = EaseIn),
                    repeatMode = Reverse,
                ),
            label = "morph",
        )

    val shape =
        if (showReview.not()) {
            MorphPolygonShape(
                morph,
                morphProgress,
            )
        } else {
            RoundedCornerShape(genre?.cornerSize() ?: 20.dp)
        }

    val inputRadius = genre?.cornerSize() ?: 25.dp
    val cornerAnimation by animateDpAsState(
        inputRadius,
    )
    val inputShape = RoundedCornerShape(cornerAnimation)
    Box {
        AnimatedVisibility(
            isLoading || savedSaga != null,
            enter = fadeIn(),
            exit = scaleOut(),
            modifier = Modifier.fillMaxSize(),
        ) {
            StarryTextPlaceholder(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .gradientFill(brush),
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .animateContentSize()
                    .verticalScroll(rememberScrollState()),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                AnimatedContent(
                    showReview,
                    transitionSpec = {
                        fadeIn(tween(400)) + scaleIn() togetherWith fadeOut()
                    },
                    modifier =
                        Modifier
                            .align(Alignment.Center)
                            .wrapContentSize()
                            .blur(blurRadius, edgeTreatment = BlurredEdgeTreatment.Unbounded),
                ) {
                    if (it) {
                        BlurredGlowContainer(
                            Modifier.padding(16.dp).fillMaxWidth().fillMaxHeight(.5f),
                            brush,
                            shape = shape,
                        ) {
                            savedSaga?.let {
                                SagaCard(
                                    it,
                                    modifier =
                                        Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth()
                                            .fillMaxHeight(.5f),
                                )
                            } ?: run {
                                SagaFormCards(
                                    sagaForm,
                                    onDismiss = {
                                        showReview = false
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    } else {
                        val fullStarCount = Genre.entries.size * 10
                        val starCount = if (showText) 0 else fullStarCount
                        val blurStar by animateDpAsState(
                            if (showText) 50.dp else 5.dp,
                        )
                        val background =
                            if (showText) brush else MaterialTheme.colorScheme.background.gradientFade()
                        Box(
                            modifier =
                                Modifier
                                    .align(Alignment.Center)
                                    .clip(shape)
                                    .size(100.dp),
                        ) {
                            StarryTextPlaceholder(
                                starCount = starCount,
                                modifier =
                                    Modifier
                                        .align(Alignment.Center)
                                        .fillMaxSize()
                                        .background(background)
                                        .gradientFill(brush),
                            )
                        }
                    }
                }

                this@Column.AnimatedVisibility(
                    showText,
                    modifier = Modifier.align(Alignment.Center),
                ) {
                    AnimatedContent(aiState.message) {
                        it?.let { _ ->
                            SimpleTypewriterText(
                                aiState.message ?: emptyString(),
                                style =
                                    MaterialTheme.typography.bodyLarge.copy(
                                        brush = brush,
                                        fontFamily = textFont,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                    ),
                                modifier =
                                    Modifier.clickable {
                                        showText = false
                                    },
                            )
                        }
                    }
                }
            }

            Text(
                "Temas",
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontFamily = textFont,
                    ),
                modifier = Modifier.padding(16.dp),
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val genres = Genre.entries
                items(genres) {
                    GenreAvatar(
                        it,
                        true,
                        sagaForm.saga.genre == it,
                        modifier = Modifier.wrapContentSize(),
                    ) {
                        selectGenre(it)
                    }
                }
            }
            var input by remember {
                mutableStateOf("")
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier =
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
            ) {
                items(aiState.suggestions) {
                    Text(
                        it,
                        style =
                            MaterialTheme.typography.labelMedium.copy(
                                fontFamily = textFont,
                                brush = brush,
                            ),
                        modifier =
                            Modifier
                                .clickable {
                                    input = it
                                }.weight(5f, false)
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainer,
                                    inputShape,
                                ).padding(8.dp),
                    )
                }
            }

            Row(
                modifier =
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    input,
                    onValueChange = {
                        showText = it.isEmpty()
                        if (it.length < 500) {
                            input = it
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions =
                        KeyboardActions(
                            onDone = {
                                if (input.isNotEmpty()) {
                                    sendDescription(input)
                                }
                            },
                        ),
                    colors =
                        TextFieldDefaults.colors().copy(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                    leadingIcon = {
                        Image(
                            painterResource(R.drawable.ic_spark),
                            null,
                            colorFilter =
                                ColorFilter.tint(
                                    genre?.color ?: MaterialTheme.colorScheme.onBackground,
                                ),
                            modifier =
                                Modifier
                                    .alpha(.5f)
                                    .size(32.dp)
                                    .reactiveShimmer(isLoading),
                        )
                    },
                    shape = inputShape,
                    textStyle =
                        MaterialTheme.typography.labelSmall.copy(
                            fontFamily = textFont,
                        ),
                    placeholder = {
                        Text(
                            aiState.hint ?: stringResource(R.string.saga_title_hint),
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = textFont,
                                ),
                            maxLines = 1,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .alpha(.4f),
                        )
                    },
                    modifier = Modifier.weight(1f),
                )

                AnimatedVisibility(input.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            sendDescription(input)
                        },
                        modifier =
                            Modifier
                                .size(50.dp)
                                .padding(8.dp),
                        colors =
                            IconButtonDefaults.iconButtonColors().copy(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentColor =
                                    genre?.iconColor
                                        ?: MaterialTheme.colorScheme.onPrimary,
                            ),
                    ) {
                        Image(
                            painterResource(R.drawable.ic_arrow_up),
                            "Send message",
                            modifier = Modifier.fillMaxSize().gradientFill(brush),
                        )
                    }

                    IconButton(
                        onClick = {
                            if (sagaForm.character.name.isNotEmpty()) {
                            }
                        },
                        modifier =
                            Modifier
                                .size(50.dp)
                                .padding(8.dp),
                        colors =
                            IconButtonDefaults.iconButtonColors().copy(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = MaterialTheme.colorScheme.onBackground,
                            ),
                    ) {
                        AnimatedContent(
                            sagaForm.character,
                            modifier = Modifier.gradientFill(brush),
                        ) {
                            if (it.name.isEmpty()) {
                                Icon(Icons.Rounded.Person, "Character")
                            } else {
                                Text(
                                    it.name.first().toString(),
                                    style =
                                        MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = genre?.headerFont(),
                                        ),
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
fun NewSagaAIFormPreview() {
    SagAIScaffold {
        NewSagaAIForm(
            sagaForm = SagaForm(),
            aiState = FormState(),
            savedSaga = null,
        )
    }
}
