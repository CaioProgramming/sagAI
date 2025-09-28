@file:OptIn(
    ExperimentalSharedTransitionApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package com.ilustris.sagai.features.newsaga.ui.components

import SagaFormCards
import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode.Reverse
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.data.model.defaultHeaderImage
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.features.newsaga.ui.presentation.FormState
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.theme.MorphPolygonShape
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.BlurredGlowContainer
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import effectForGenre
import kotlinx.coroutines.launch
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
    val genre = sagaForm.saga.genre
    var showThemes by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val sheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val color by animateColorAsState(
        genre?.color ?: MaterialTheme.colorScheme.primary,
    )
    val brush =
        if (genre != null) {
            gradientAnimation(color.darkerPalette())
        } else {
            gradientAnimation(
                holographicGradient,
            )
        }
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
        showText = aiState.message != null && aiState.readyToSave.not()
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
                        .gradientFill(gradientAnimation(holographicGradient)),
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
                // Orbit-like bubbles around the center with AnimatedVisibility and floating animation
                val character = sagaForm.character
                val density = androidx.compose.ui.platform.LocalDensity.current

                fun polarOffsetDp(
                    radius: Dp,
                    angleDeg: Float,
                ): Pair<Dp, Dp> {
                    val rPx = with(density) { radius.roundToPx() }
                    val rad = Math.toRadians(angleDeg.toDouble())
                    val xPx = (rPx * kotlin.math.cos(rad)).toInt()
                    val yPx = (rPx * kotlin.math.sin(rad)).toInt()
                    val xDp = with(density) { xPx.toDp() }
                    val yDp = with(density) { yPx.toDp() }
                    return xDp to yDp
                }

                val infinite = rememberInfiniteTransition(label = "orbit")
                val themeScale by infinite.animateFloat(
                    initialValue = .9f,
                    targetValue = 1.2f,
                    animationSpec =
                        infiniteRepeatable(
                            tween(3.seconds.toInt(DurationUnit.MILLISECONDS), easing = EaseInOutSine),
                            repeatMode = Reverse,
                        ),
                    label = "themeScale",
                )

                val backBlur by animateDpAsState(
                    if (showText) 25.dp else 0.dp,
                )

                this@Column.AnimatedVisibility(
                    genre != null,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut(),
                    modifier =
                        Modifier
                            .blur(backBlur, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                            .align(Alignment.Center),
                ) {
                    val (dx, dy) = polarOffsetDp(80.dp, 45f)

                    genre?.let { g ->
                        Image(
                            painterResource(g.defaultHeaderImage()),
                            g.name,
                            contentScale = ContentScale.Crop,
                            modifier =
                                Modifier
                                    .scale(themeScale)
                                    .offset(dx, dy)
                                    .size(64.dp)
                                    .border(1.dp, g.color, CircleShape)
                                    .background(
                                        Brush.verticalGradient(g.color.darkerPalette()),
                                        CircleShape,
                                    ).clip(CircleShape)
                                    .effectForGenre(g, customGrain = 0f)
                                    .selectiveColorHighlight(g.selectiveHighlight()),
                        )
                    }
                }

                this@Column.AnimatedVisibility(
                    visible = character.name.isNotEmpty(),
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                    modifier =
                        Modifier
                            .blur(backBlur, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                            .align(Alignment.Center),
                ) {
                    val (dx, dy) = polarOffsetDp(96.dp, 220f)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier =
                            Modifier
                                .scale(themeScale)
                                .offset(dx, dy)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .border(width = 1.dp, brush = brush, shape = CircleShape),
                    ) {
                        Text(
                            character.name.first().toString(),
                            style =
                                MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = genre?.headerFont(),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                ),
                            textAlign = TextAlign.Center,
                        )
                    }
                }

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
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                                .height(350.dp),
                            brush,
                            shape = shape,
                        ) {
                            savedSaga?.let {
                                SagaCard(
                                    it,
                                    modifier =
                                        Modifier
                                            .fillMaxSize(),
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
                        val fullStarCount = Genre.entries.size * 100
                        val starCount = if (showText) 0 else fullStarCount
                        val blurStar by animateDpAsState(
                            if (showText) 25.dp else 0.dp,
                        )
                        val background by animateColorAsState(
                            if (showText) color else Color.Transparent,
                        )
                        Box(
                            modifier =
                                Modifier
                                    .blur(blurStar)
                                    .align(Alignment.Center)
                                    .clip(shape)
                                    .background(background)
                                    .size(100.dp),
                        ) {
                            StarryTextPlaceholder(
                                starCount = starCount,
                                modifier =
                                    Modifier
                                        .align(Alignment.Center)
                                        .fillMaxSize()
                                        .gradientFill(brush),
                            )
                        }
                    }
                }

                this@Column.AnimatedVisibility(
                    showText,
                    modifier = Modifier.align(Alignment.Center),
                ) {
                    AnimatedContent(aiState.message, transitionSpec = {
                        slideInVertically() + fadeIn(tween(400)) togetherWith scaleOut() +
                            fadeOut(
                                tween(200),
                            )
                    }) {
                        it?.let { _ ->
                            Text(
                                aiState.message ?: emptyString(),
                                style =
                                    MaterialTheme.typography.bodyLarge.copy(
                                        brush = brush,
                                        fontFamily = textFont,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                    ),
                                modifier =
                                    Modifier
                                        .animateContentSize(tween(500, easing = EaseIn))
                                        .clickable {
                                            showText = false
                                        },
                            )
                        }
                    }
                }
            }

            Row {
                Text(
                    "Temas",
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontFamily = textFont,
                        ),
                    modifier =
                        Modifier
                            .weight(1f)
                            .animateContentSize()
                            .padding(16.dp),
                )

                Text(
                    "Ver mais",
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontFamily = textFont,
                            color = color,
                        ),
                    modifier =
                        Modifier
                            .padding(16.dp)
                            .clickable {
                                showThemes = true
                            },
                )
            }

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
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),
            ) {
                items(aiState.suggestions) {
                    Text(
                        it,
                        maxLines = 2,
                        style =
                            MaterialTheme.typography.labelMedium.copy(
                                fontFamily = textFont,
                                brush = brush,
                            ),
                        modifier =
                            Modifier
                                .clickable {
                                    input = it
                                }.fillMaxWidth(.5f)
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
                        .padding(12.dp)
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
                                    input = ""
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
                            input = ""
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
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .gradientFill(brush),
                        )
                    }
                }

                val character = sagaForm.character
                val characterTooltipState = androidx.compose.material3.rememberTooltipState()
                val tooltipPositionProvider =
                    androidx.compose.material3.TooltipDefaults.rememberPlainTooltipPositionProvider(
                        spacingBetweenTooltipAndAnchor = 8.dp,
                    )
                val scope = rememberCoroutineScope()
                TooltipBox(
                    positionProvider = tooltipPositionProvider,
                    state = characterTooltipState,
                    tooltip = {
                        if (character.name.isNotEmpty()) {
                            Column(
                                modifier =
                                    Modifier
                                        .background(
                                            MaterialTheme.colorScheme.surface,
                                            RoundedCornerShape(12.dp),
                                        ).padding(12.dp),
                            ) {
                                Text(
                                    text = character.name,
                                    style =
                                        MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = genre?.bodyFont(),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                )
                                if (character.description.isNotEmpty()) {
                                    Text(
                                        text = character.description,
                                        style =
                                            MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = genre?.bodyFont(),
                                                color = MaterialTheme.colorScheme.onSurface,
                                            ),
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                }
                            }
                        }
                    },
                ) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                if (character.name.isNotEmpty()) {
                                    characterTooltipState.show()
                                }
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
                            character,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Box(Modifier.fillMaxSize().gradientFill(brush), contentAlignment = Alignment.Center) {
                                if (it.name.isEmpty()) {
                                    Icon(Icons.Rounded.Person, "Character")
                                } else {
                                    Text(
                                        it.name.first().toString(),
                                        style =
                                            MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = genre?.headerFont(),
                                                textAlign = TextAlign.Center,
                                            ),
                                        modifier = Modifier.align(Alignment.Center),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showThemes) {
        ModalBottomSheet(
            { coroutineScope.launch { showThemes = false } },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        "Temas",
                        style =
                            MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        modifier =
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                    )
                }

                items(Genre.entries) { genre ->
                    GenreCard(
                        genre = genre,
                        isSelected = sagaForm.saga.genre == genre,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                                .aspectRatio(.5f),
                    ) {
                        showThemes = false
                        selectGenre(genre)
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
