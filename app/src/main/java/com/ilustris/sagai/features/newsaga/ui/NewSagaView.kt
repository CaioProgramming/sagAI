@file:OptIn(ExperimentalAnimationApi::class)

package com.ilustris.sagai.features.newsaga.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseInElastic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.components.SagaLoader
import com.ilustris.sagai.ui.theme.darker
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.grayScale
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.lighter
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun NewSagaView(
    navHostController: NavHostController,
    createSagaViewModel: CreateSagaViewModel = hiltViewModel(),
) {
    val form by createSagaViewModel.saga.collectAsStateWithLifecycle()
    val chatData by createSagaViewModel.generatedChat.collectAsStateWithLifecycle()
    NewSagaForm(form, chatData, updateTitle = {
        createSagaViewModel.updateTitle(it)
    }, updateDescription = {
        createSagaViewModel.updateDescription(it)
    }, updateGenre = {
        createSagaViewModel.updateGenre(it)
    }, generateSaga = {
        createSagaViewModel.generateSaga()
    })
}

@Composable
fun NewSagaForm(
    formData: SagaForm,
    sagaData: SagaData?,
    updateTitle: (String) -> Unit = {},
    updateDescription: (String) -> Unit = {},
    updateGenre: (Genre) -> Unit = {},
    generateSaga: () -> Unit = {},
) {
    val rotationState = remember { Animatable(0f) }
    val genreSelectorVisible =
        remember {
            mutableStateOf(true)
        }

    val gradient =
        gradientAnimation(
            holographicGradient,
            duration = 7.seconds,
            targetValue = 500f,
        )

    LaunchedEffect(Unit) {
        rotationState.animateTo(
            targetValue = 360f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = 1000, easing = EaseInCubic),
                    repeatMode = RepeatMode.Reverse,
                ),
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier =
            Modifier
                .padding(16.dp)
                .fillMaxSize()
                .animateContentSize()
                .verticalScroll(rememberScrollState()),
    ) {
        SagaGenerator(
            gradient,
            formData,
            sagaData,
            Modifier
                .fillMaxWidth()
                .height(300.dp),
        )

        val scaleAnimation by animateFloatAsState(
            if (formData.title.isEmpty()) 0.8f else 1f,
            animationSpec =
                tween(
                    easing = EaseInElastic,
                    durationMillis = 6.seconds.toInt(DurationUnit.MILLISECONDS),
                ),
        )

        Text(
            stringResource(R.string.start_saga),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            modifier = Modifier.fillMaxWidth(),
        )

        TextField(
            formData.title,
            { value ->
                updateTitle(value)
            },
            colors =
                TextFieldDefaults.colors(
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                ),
            singleLine = true,
            maxLines = 1,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done,
                    capitalization = KeyboardCapitalization.Sentences,
                    autoCorrect = true,
                ),
            leadingIcon = {
                Image(
                    painterResource(R.drawable.ic_spark),
                    null,
                    modifier =
                        Modifier
                            .size(32.dp)
                            .scale(scaleAnimation),
                )
            },
            trailingIcon = {
                Image(
                    painterResource(R.drawable.ic_spark),
                    null,
                    modifier =
                        Modifier
                            .size(32.dp)
                            .scale(scaleAnimation),
                )
            },
            placeholder = {
                Text(
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                        ),
                    text = "De um nome para sua história",
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .alpha(.5f),
                )
            },
            textStyle =
                MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                ),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .gradientFill(gradient),
        )

        AnimatedVisibility(
            genreSelectorVisible.value,
            enter = scaleIn(),
            exit =
                scaleOut() + fadeOut(),
        ) {
            GenreSelectionCard(formData.genre) {
                updateGenre(it)
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            AnimatedContent(
                formData.genre,
                transitionSpec = {
                    scaleIn() + fadeIn() with scaleOut() + fadeOut()
                },
            ) {
                GenreAvatar(
                    it,
                    isSelected = true,
                    showText = false,
                    modifier = Modifier.padding(8.dp),
                ) {
                    genreSelectorVisible.value = genreSelectorVisible.value.not()
                }
            }

            TextField(
                formData.description,
                {
                    updateDescription(it)
                },
                modifier =
                    Modifier
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onBackground,
                            shape = RoundedCornerShape(40.dp),
                        ),
                colors =
                    TextFieldDefaults.colors(
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                    ),
                textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.W500),
                placeholder = {
                    Text(
                        style = MaterialTheme.typography.bodySmall,
                        text = stringResource(R.string.saga_description_hint),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.alpha(.5f),
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {},
                        modifier =
                            Modifier
                                .size(24.dp)
                                .background(gradient, CircleShape)
                                .padding(1.dp),
                    ) {
                        Icon(
                            Icons.Rounded.Done,
                            "Ok",
                            tint = Color.White,
                        )
                    }
                },
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done,
                        capitalization = KeyboardCapitalization.Sentences,
                        autoCorrect = true,
                    ),
            )
        }
    }
}

@Composable
private fun GenreSelectionCard(
    selectedGenre: Genre? = null,
    selectItem: (Genre) -> Unit = {},
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LazyRow(
            modifier =
                Modifier
                    .wrapContentSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val genres = Genre.entries
            items(genres) {
                GenreAvatar(it, isSelected = it == selectedGenre) {
                    selectItem(it)
                }
            }
        }
    }
}

@Composable
fun GenreAvatar(
    genre: Genre,
    showText: Boolean = true,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (Genre) -> Unit,
) {
    val saturation by animateFloatAsState(
        if (isSelected) 1f else 0f,
        tween(durationMillis = 2.seconds.toInt(DurationUnit.MILLISECONDS)),
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .grayScale(saturation)
                .background(genre.color.lighter(0.3f), RoundedCornerShape(50.dp)),
    ) {
        val infiniteTransition = rememberInfiniteTransition()

        val colorAnimation by infiniteTransition.animateColor(
            initialValue = genre.color,
            targetValue = genre.color.darker(.5f),
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = 2000, easing = EaseIn),
                    repeatMode = RepeatMode.Reverse,
                ),
        )

        Image(
            painterResource(genre.icon),
            genre.name,
            modifier =
                Modifier
                    .size(50.dp)
                    .background(
                        if (isSelected) colorAnimation else genre.color,
                        CircleShape,
                    ).border(1.dp, genre.color.gradientFade(), CircleShape)
                    .padding(1.dp)
                    .clip(CircleShape)
                    .clickable {
                        onClick(genre)
                    },
        )

        if (showText) {
            Text(
                genre.title,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.W500,
                color = genre.iconColor,
                modifier = Modifier.padding(end = 12.dp),
            )
        }
    }
}

@Composable
private fun SagaGenerator(
    gradient: Brush,
    form: SagaForm,
    sagaData: SagaData?,
    modifier: Modifier = Modifier,
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.sphere),
    )

    val compositionProgress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
    )
    ConstraintLayout(modifier = modifier) {
        val (animation, overview) = createRefs()
        val blur by animateDpAsState(
            if (sagaData != null) 50.dp else 0.dp,
        )
        /*LottieAnimation(
            composition,
            compositionProgress,
            contentScale = ContentScale.Fit,
            modifier =
                Modifier
                    .constrainAs(animation) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    }.fillMaxWidth()
                    .height(400.dp)
                    .gradientFill(gradient)
                    .blur(blur),
        )*/


        SagaLoader(
            modifier =
                Modifier
                    .constrainAs(animation) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    }.size(250.dp)
                    .padding(16.dp)
                    .blur(blur, edgeTreatment = BlurredEdgeTreatment.Unbounded),
        )

        AnimatedVisibility(
            sagaData != null,
            Modifier.constrainAs(overview) {
                top.linkTo(animation.top)
                start.linkTo(animation.start)
                end.linkTo(animation.end)
                bottom.linkTo(animation.bottom)
            },
            enter = scaleIn(),
            exit = scaleOut() + fadeOut(),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier =
                    Modifier
                        .border(1.dp, gradient, RoundedCornerShape(15.dp))
                        .padding(16.dp),
            ) {
                Image(
                    painterResource(form.genre.icon),
                    null,
                    modifier = Modifier.size(50.dp).clip(CircleShape),
                )

                Text(
                    sagaData?.title ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    form.genre.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = form.genre.color,
                )

                Text(
                    sagaData?.description ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Justify,
                )
            }
        }
    }
}

@Preview
@Composable
fun NewSagaViewPreview() {
    SagAIScaffold(title = "nova saga") {
        var form by remember {
            mutableStateOf(
                SagaForm(
                    title = "Test story",
                    genre = Genre.SCI_FI,
                    description = "that's a test description for the saga",
                ),
            )
        }

        var saga by remember {
            mutableStateOf<SagaData?>(
                null,
            )
        }

        NewSagaForm(
            formData = form,
            sagaData = saga,
            updateTitle = {
                form = form.copy(title = it)
            },
            updateDescription = {
                form = form.copy(description = it)
                saga =
                    SagaData(
                        title = form.title,
                        description = it,
                        color =
                            form.genre.color
                                .toArgb()
                                .toString(),
                        icon = form.genre.icon.toString(),
                        createdAt = System.currentTimeMillis(),
                    )
            },
            updateGenre = {
                form = form.copy(genre = Genre.valueOf(it.name))
            },
        )
    }
}
