@file:OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)

package com.ilustris.sagai.features.newsaga.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.ui.components.SagaCard
import com.ilustris.sagai.features.newsaga.ui.pages.NewSagaPages
import com.ilustris.sagai.features.newsaga.ui.pages.NewSagaPages.*
import com.ilustris.sagai.features.newsaga.ui.pages.NewSagaPagesView
import com.ilustris.sagai.features.newsaga.ui.presentation.CreateSagaState
import com.ilustris.sagai.features.newsaga.ui.presentation.CreateSagaViewModel
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.navigation.navigateToRoute
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.components.SparkLoader
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun NewSagaView(
    navHostController: NavHostController,
    createSagaViewModel: CreateSagaViewModel = hiltViewModel(),
) {
    val form by createSagaViewModel.saga.collectAsStateWithLifecycle()
    val state by createSagaViewModel.state.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(0) { NewSagaPages.entries.size }
    val coroutineScope = rememberCoroutineScope()

    fun animateToPage(
        page: Int,
        delayTime: Duration = 0.seconds,
    ) {
        coroutineScope.launch {
            delay(delayTime)
            pagerState.animateScrollToPage(page)
        }
    }

    Box {
        val isLoading =
            state is CreateSagaState.Loading ||
                state is CreateSagaState.Success ||
                state is CreateSagaState.GeneratedSaga
        val blurRadius = animateDpAsState(if (isLoading) 20.dp else 0.dp)
        NewSagaFlow(
            pagerState,
            form,
            Modifier
                .align(Alignment.Center)
                .blur(blurRadius.value, edgeTreatment = BlurredEdgeTreatment.Unbounded),
        ) { page, data ->

            when (page) {
                TITLE -> {
                    createSagaViewModel.updateTitle(data as String)
                    animateToPage(pagerState.currentPage + 1)
                }

                GENRE -> {
                    createSagaViewModel.updateGenre(data as Genre)
                    animateToPage(pagerState.currentPage + 1)
                }

                DESCRIPTION -> {
                    createSagaViewModel.updateDescription(data as String)
                    animateToPage(pagerState.currentPage + 1)
                }

                CHARACTER -> {
                    createSagaViewModel.updateCharacterDescription(data as String)
                    animateToPage(pagerState.currentPage + 1)
                }
            }
        }

        AnimatedVisibility(
            isLoading,
            enter = fadeIn() + scaleIn(),
            exit = scaleOut(),
            modifier = Modifier.align(Alignment.Center),
        ) {
            SparkLoader(
                brush =
                    gradientAnimation(
                        holographicGradient,
                        targetValue = 500f,
                        duration = 5.seconds,
                    ),
                modifier = Modifier.size(100.dp),
            )
        }

        if (state is CreateSagaState.GeneratedSaga || state is CreateSagaState.Success) {
            Dialog(onDismissRequest = { createSagaViewModel.resetGeneratedSaga() }) {
                Column(verticalArrangement = Arrangement.Center) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    ) {
                        when (state) {
                            is CreateSagaState.GeneratedSaga -> {
                                SagaCard(
                                    sagaData = (state as CreateSagaState.GeneratedSaga).saga,
                                    modifier =
                                        Modifier
                                            .align(Alignment.Center)
                                            .fillMaxWidth()
                                            .fillMaxHeight(.5f),
                                )
                            }

                            is CreateSagaState.Success -> {
                                val saga = (state as CreateSagaState.Success).saga
                                SagaCard(
                                    sagaData = saga,
                                    modifier =
                                        Modifier
                                            .align(Alignment.Center)
                                            .fillMaxWidth()
                                            .fillMaxHeight(.5f),
                                )
                            }

                            else -> Box {}
                        }
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val saveText = if (state is CreateSagaState.Success) "Continuar" else "Salvar"
                        AnimatedVisibility(state is CreateSagaState.GeneratedSaga) {
                            Button(
                                modifier = Modifier.fillMaxWidth(.5f),
                                onClick = { createSagaViewModel.resetGeneratedSaga() },
                                colors = ButtonDefaults.textButtonColors(),
                            ) {
                                Text(text = "Fechar")
                            }
                        }

                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                when (state) {
                                    is CreateSagaState.GeneratedSaga -> {
                                        createSagaViewModel.saveSaga((state as CreateSagaState.GeneratedSaga).saga)
                                    }
                                    is CreateSagaState.Success -> {
                                        navHostController.navigateToRoute(
                                            Routes.CHAT,
                                            arguments =
                                                mapOf(
                                                    Routes.CHAT.arguments.first() to (state as CreateSagaState.Success).saga.id.toString(),
                                                ),
                                        )
                                    }
                                    else -> doNothing()
                                }
                            },
                        ) {
                            Text(text = saveText)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewSagaFlow(
    pagerState: PagerState,
    form: SagaForm,
    modifier: Modifier = Modifier,
    updateContent: (NewSagaPages, Any?) -> Unit = { _, _ -> },
) {
    val currentPage = NewSagaPages.entries[pagerState.currentPage]
    var data by remember {
        mutableStateOf<Any?>(null)
    }
    Column(
        modifier = modifier.animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        currentPage.title?.let {
            Text(
                stringResource(it),
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                modifier = Modifier.padding(16.dp),
            )
        }
        currentPage.subtitle?.let {
            Text(
                stringResource(it),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp),
            )
        }

        NewSagaPagesView(
            pagerState,
            form,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            { page, newValue ->
                data = newValue
            },
        )

        val brush = gradientAnimation(holographicGradient, targetValue = 700f)
        val pageEnabled =
            when (currentPage) {
                GENRE -> true
                TITLE, DESCRIPTION, CHARACTER -> (data as? String)?.isNotEmpty() == true
            }

        Row(
            modifier =
                Modifier
                    .padding(16.dp)
                    .gradientFill(brush),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NewSagaPages.entries.forEachIndexed { page, index ->
                val isEnabled = page <= pagerState.currentPage
                val size =
                    animateDpAsState(
                        targetValue = if (isEnabled) 24.dp else 12.dp,
                        label = "indicatorSize",
                    )
                val dividerAlpha by animateFloatAsState(
                    if (isEnabled) 1f else .1f,
                    label = "dividerAlpha",
                )
                Icon(
                    painterResource(R.drawable.ic_spark),
                    null,
                    tint =
                        MaterialTheme.colorScheme.onBackground.copy(
                            alpha = dividerAlpha,
                        ),
                    modifier = Modifier.size(size.value),
                )

                Box(
                    modifier =
                        Modifier
                            .background(
                                MaterialTheme.colorScheme.onBackground.copy(alpha = dividerAlpha),
                                RoundedCornerShape(15.dp),
                            ).height(1.dp)
                            .weight(1f),
                )
            }
        }

        AnimatedVisibility(
            pageEnabled,
            modifier =
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth(),
        ) {
            Button(
                onClick = {
                    updateContent(currentPage, data)
                },
                shape = RoundedCornerShape(15.dp),
                modifier =
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .border(2.dp, brush, RoundedCornerShape(15.dp)),
                colors =
                    ButtonDefaults.buttonColors().copy(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
            ) {
                Text(
                    text = stringResource(R.string.next),
                    style =
                        MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.background,
                        ),
                    modifier =
                        Modifier
                            .padding(16.dp)
                            .gradientFill(brush),
                )

                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = "Next",
                    modifier =
                        Modifier
                            .gradientFill(brush)
                            .size(24.dp),
                    tint = MaterialTheme.colorScheme.background,
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NewSagaFormPreview() {
    SagAIScaffold {
        var form by remember {
            mutableStateOf(
                SagaForm(
                    title = "ok",
                    genre = Genre.SCI_FI,
                    description = "that's a test description for the saga",
                ),
            )
        }
        val pagerState = rememberPagerState { NewSagaPages.entries.size }

        NewSagaFlow(
            pagerState,
            form,
            updateContent = { _, data ->
                form = form.copy(title = data.toString())
                pagerState.requestScrollToPage(pagerState.currentPage + 1)
            },
        )
    }
}
