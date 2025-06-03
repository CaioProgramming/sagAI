@file:OptIn(ExperimentalAnimationApi::class)

package com.ilustris.sagai.features.newsaga.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
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
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.layout.ContentScale
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
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.addQueryParameter
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.ui.presentation.CreateSagaState
import com.ilustris.sagai.features.newsaga.ui.presentation.CreateSagaViewModel
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.components.SagaLoader
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.grayScale
import com.ilustris.sagai.ui.theme.holographicGradient
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Composable
fun NewSagaView(
    navHostController: NavHostController,
    createSagaViewModel: CreateSagaViewModel = hiltViewModel(),
) {
    val form by createSagaViewModel.saga.collectAsStateWithLifecycle()
    val state by createSagaViewModel.state.collectAsStateWithLifecycle()
    NewSagaForm(form, state, updateTitle = {
        createSagaViewModel.updateTitle(it)
    }, updateDescription = {
        createSagaViewModel.updateDescription(it)
    }, updateGenre = {
        createSagaViewModel.updateGenre(it)
    }, generateSaga = {
        createSagaViewModel.generateSaga()
    }, resetSaga = {
        createSagaViewModel.resetGeneratedSaga()
    }, saveSaga = {
        createSagaViewModel.saveSaga(it)
    })

    LaunchedEffect(state) {
        if (state is CreateSagaState.Success) {
            navHostController.navigate(
                Routes.CHAT.name.addQueryParameter(
                    "sagaId",
                    (state as CreateSagaState.Success).saga.id.toString(),
                ),
            ) {
                popUpTo("home") {
                    inclusive = false
                }
            }
        }
    }
}

@Composable
fun NewSagaForm(
    formData: SagaForm,
    state: CreateSagaState = CreateSagaState.Idle,
    updateTitle: (String) -> Unit = {},
    updateDescription: (String) -> Unit = {},
    updateGenre: (Genre) -> Unit = {},
    generateSaga: () -> Unit = {},
    resetSaga: () -> Unit = {},
    saveSaga: (SagaData) -> Unit = {},
) {
    val genreSelectorVisible =
        remember {
            mutableStateOf(true)
        }

    val gradient =
        gradientAnimation(
            holographicGradient,
            duration = 7.seconds,
            targetValue = 1000f,
        )

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (sagaContent, descriptionInput) = createRefs()

        val bottomAlpha by animateFloatAsState(if (state != CreateSagaState.Loading) 1f else 0f)
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .constrainAs(descriptionInput) {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }.alpha(bottomAlpha),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AnimatedVisibility(genreSelectorVisible.value) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Tema", style = MaterialTheme.typography.titleMedium)

                    GenreSelectionCard(formData.genre) {
                        updateGenre(it)
                    }
                }
            }

            SagaInput(
                formData,
                gradient,
                if (state is CreateSagaState.GeneratedSaga) 1 else 10,
                genreSelectorVisible,
                updateDescription,
                generateSaga,
            )
        }

        val alphaAnimation by animateDpAsState(
            if (state != CreateSagaState.Loading) 0.dp else 50.dp,
            animationSpec =
                tween(
                    durationMillis = 1500,
                    easing = EaseIn,
                ),
            label = "SagaContentAlpha",
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier =
                Modifier
                    .constrainAs(sagaContent) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(descriptionInput.top)
                        height = Dimension.fillToConstraints
                        width = Dimension.fillToConstraints
                    }.padding(16.dp),
        ) {
            Text(
                stringResource(R.string.start_saga),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .blur(alphaAnimation, edgeTreatment = BlurredEdgeTreatment.Unbounded),
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
                maxLines = 3,
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done,
                        capitalization = KeyboardCapitalization.Sentences,
                        autoCorrect = true,
                    ),
                placeholder = {
                    Text(
                        style =
                            MaterialTheme.typography.displaySmall.copy(
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium,
                                brush = MaterialTheme.colorScheme.onBackground.gradientFade(),
                            ),
                        text = stringResource(R.string.saga_title_hint),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .alpha(.5f),
                    )
                },
                textStyle =
                    MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        brush = gradient,
                    ),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .blur(alphaAnimation, edgeTreatment = BlurredEdgeTreatment.Unbounded),
            )
            val animationDuration =
                if (state == CreateSagaState.Loading) {
                    1.seconds
                } else {
                    10.seconds
                }

            SagaGenerator(
                gradient,
                formData,
                (state as? CreateSagaState.GeneratedSaga)?.saga,
                state,
                duration = animationDuration,
                onSaveSaga = { saveSaga(it) },
                onResetSaga = { resetSaga() },
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun SagaInput(
    formData: SagaForm,
    gradient: Brush,
    maxLines: Int? = null,
    genreSelectorVisible: MutableState<Boolean>,
    updateDescription: (String) -> Unit,
    generateSaga: () -> Unit,
) {
    ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
        val (selectedGenre, okButton, inputField) = createRefs()

        AnimatedContent(
            formData.genre,
            modifier =
                Modifier
                    .constrainAs(selectedGenre) {
                        top.linkTo(parent.top)
                        end.linkTo(okButton.start)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.wrapContent
                        height = Dimension.wrapContent
                    }.animateContentSize(),
            transitionSpec = {
                scaleIn() + fadeIn() with scaleOut() + fadeOut()
            },
        ) {
            GenreAvatar(
                it,
                isSelected = true,
                showText = false,
                modifier =
                    Modifier
                        .clip(CircleShape)
                        .border(2.dp, gradient, CircleShape)
                        .padding(4.dp),
            ) {
                genreSelectorVisible.value = !genreSelectorVisible.value
            }
        }

        IconButton(
            enabled = formData.description.isNotEmpty() && formData.title.isNotEmpty(),
            onClick = {
                generateSaga()
            },
            modifier =
                Modifier
                    .padding(horizontal = 4.dp)
                    .constrainAs(okButton) {
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }.size(50.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
                    .padding(8.dp),
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                "Ok",
                tint = Color.White,
                modifier = Modifier.gradientFill(gradient),
            )
        }

        TextField(
            formData.description,
            {
                updateDescription(it)
            },
            maxLines = maxLines ?: 10,
            modifier =
                Modifier
                    .padding(end = 10.dp)
                    .constrainAs(inputField) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(selectedGenre.start)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                    }.background(
                        MaterialTheme.colorScheme.surfaceContainer,
                        RoundedCornerShape(25.dp),
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

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.grayScale(saturation),
    ) {
        Image(
            painterResource(genre.icon),
            genre.name,
            modifier =
                Modifier
                    .size(50.dp)
                    .background(
                        genre.color,
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
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun SagaGenerator(
    gradient: Brush,
    form: SagaForm,
    sagaData: SagaData?,
    state: CreateSagaState,
    duration: Duration = 5.seconds,
    onSaveSaga: (SagaData) -> Unit = {},
    onResetSaga: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    ConstraintLayout(modifier = modifier) {
        val (animation, overview, sagaIcon) = createRefs()
        val blur by animateDpAsState(
            if (sagaData != null) 50.dp else 0.dp,
        )

        val infiniteAnimation = rememberInfiniteTransition()
        val scaleAnimation by
            infiniteAnimation.animateFloat(
                initialValue = .8f,
                targetValue = 1.1f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(duration.inWholeMilliseconds.toInt(), easing = EaseIn),
                        repeatMode = androidx.compose.animation.core.RepeatMode.Reverse,
                    ),
            )
        AnimatedVisibility(
            (state as? CreateSagaState.Success)?.saga?.icon != null,
            modifier =
                Modifier
                    .constrainAs(sagaIcon) {
                        top.linkTo(animation.top)
                        start.linkTo(animation.start)
                        end.linkTo(animation.end)
                        bottom.linkTo(animation.bottom)
                        height = Dimension.fillToConstraints
                        width = Dimension.fillToConstraints
                    },
        ) {
            AsyncImage(
                (state as? CreateSagaState.Success)?.saga?.icon,
                contentDescription = sagaData?.title,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .padding(16.dp)
                        .scale(scaleAnimation)
                        .border(2.dp, gradient, CircleShape)
                        .background(gradient, CircleShape)
                        .clip(CircleShape),
            )
        }

        SagaLoader(
            animationDuration = duration,
            modifier =
                Modifier
                    .constrainAs(animation) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    }.size(200.dp)
                    .blur(blur, edgeTreatment = BlurredEdgeTreatment.Unbounded),
        )

        AnimatedVisibility(
            sagaData != null,
            Modifier.constrainAs(overview) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
                height = Dimension.fillToConstraints
                width = Dimension.fillToConstraints
            },
            enter = scaleIn(),
            exit = scaleOut() + fadeOut(),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .border(2.dp, gradient, RoundedCornerShape(15.dp))
                        .background(
                            MaterialTheme.colorScheme.background.copy(alpha = .1f),
                            RoundedCornerShape(15.dp),
                        ).padding(16.dp)
                        .verticalScroll(rememberScrollState()),
            ) {
                IconButton(onClick = {
                    onResetSaga()
                }, modifier = Modifier.align(Alignment.End)) {
                    Icon(
                        Icons.Rounded.Refresh,
                        "Gerar novamente",
                        modifier =
                            Modifier
                                .size(24.dp)
                                .gradientFill(gradient),
                    )
                }

                Image(
                    painterResource(form.genre.icon),
                    null,
                    modifier =
                        Modifier
                            .size(50.dp)
                            .clip(CircleShape),
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

                Button(
                    onClick = {
                        sagaData?.let {
                            onSaveSaga(it)
                        }
                    },
                    modifier =
                        Modifier.fillMaxWidth().background(gradient, RoundedCornerShape(15.dp)),
                    colors =
                        ButtonDefaults.elevatedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    shape = RoundedCornerShape(15.dp),
                ) {
                    Text(
                        "Começar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier =
                            Modifier
                                .padding(8.dp)
                                .fillMaxWidth(0.85f),
                    )

                    Icon(
                        painterResource(R.drawable.ic_spark),
                        contentDescription = stringResource(R.string.new_saga_title),
                        modifier =
                            Modifier
                                .size(50.dp),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun NewSagaViewPreview() {
    SagAIScaffold(title = null) {
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
            mutableStateOf<CreateSagaState>(
                CreateSagaState.Idle,
            )
        }

        NewSagaForm(
            formData = form,
            state = saga,
            updateTitle = {
                form = form.copy(title = it)
            },
            updateDescription = {
                form = form.copy(description = it)
                saga =
                    CreateSagaState.GeneratedSaga(
                        SagaData(
                            title = form.title,
                            description = it,
                            genre = Genre.SCI_FI,
                            icon = form.genre.icon.toString(),
                            createdAt = System.currentTimeMillis(),
                        ),
                    )
            },
            updateGenre = {
                form = form.copy(genre = Genre.valueOf(it.name))
            },
        )
    }
}
