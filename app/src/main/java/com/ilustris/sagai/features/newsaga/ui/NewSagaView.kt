@file:OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)

package com.ilustris.sagai.features.newsaga.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.doNothing
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.ui.pages.NewSagaPages
import com.ilustris.sagai.features.newsaga.ui.pages.NewSagaPages.*
import com.ilustris.sagai.features.newsaga.ui.pages.NewSagaPagesView
import com.ilustris.sagai.features.newsaga.ui.presentation.CreateSagaState
import com.ilustris.sagai.features.newsaga.ui.presentation.CreateSagaViewModel
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.navigation.navigateToRoute
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.components.SparkIcon
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
    val saga by createSagaViewModel.sagaData.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(1) { NewSagaPages.entries.size }
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
        val isLoading = state is CreateSagaState.Loading
        val blurRadius = animateDpAsState(if (isLoading) 20.dp else 0.dp)
        NewSagaFlow(
            pagerState,
            form,
            saga,
            Modifier
                .align(Alignment.Center)
                .blur(blurRadius.value, edgeTreatment = BlurredEdgeTreatment.Unbounded),
        ) { page, data ->

            when (page) {
                INTRO -> animateToPage(pagerState.currentPage + 1)
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

                GENERATING -> doNothing()

                RESULT -> {
                    data?.let {
                        createSagaViewModel.saveSaga((it as SagaData))
                    }
                }
            }
        }

        AnimatedVisibility(
            isLoading,
            enter = fadeIn() + scaleIn(),
            exit = scaleOut(),
            modifier = Modifier.align(Alignment.Center),
        ) {
            SparkIcon(
                brush = gradientAnimation(holographicGradient),
                modifier = Modifier.size(100.dp),
            )
        }
    }

    LaunchedEffect(state) {
        if (state is CreateSagaState.Success) {
            val saga = (state as CreateSagaState.Success).saga
            coroutineScope.launch {
                delay(2.seconds)
                navHostController.navigateToRoute(
                    Routes.CHAT,
                    Routes.CHAT.arguments.associateWith { saga.id.toString() },
                )
            }
        } else if (state is CreateSagaState.GeneratedSaga) {
            animateToPage(pagerState.currentPage + 1, 5.seconds)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val page = NewSagaPages.entries[pagerState.currentPage]
        when (page) {
            INTRO -> {
                animateToPage(pagerState.currentPage + 1)
            }

            else -> doNothing()
        }
    }
}

@Composable
fun NewSagaFlow(
    pagerState: PagerState,
    form: SagaForm,
    saga: SagaData?,
    modifier: Modifier = Modifier,
    updateContent: (NewSagaPages, Any?) -> Unit = { _, _ -> },
) {
    val currentPage = NewSagaPages.entries[pagerState.currentPage]
    var data = remember<Any?> {
        mutableStateOf(null)
    }
    Column(modifier = modifier.animateContentSize(), horizontalAlignment = Alignment.CenterHorizontally) {
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
            saga,
            modifier = Modifier.fillMaxWidth().weight(1f),
            { _, newValue ->
                data = newValue
            },
        )

        val brush = Brush.linearGradient(holographicGradient)
        val pageEnabled = when(currentPage) {
            GENRE -> true
            TITLE -> form.title.isNotEmpty()
            DESCRIPTION -> form.description.isNotEmpty()
            CHARACTER -> form.characterDescription.isNotEmpty()
            GENERATING -> saga != null
            INTRO -> false
            RESULT -> false
        }
        AnimatedVisibility(pageEnabled) {
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
                    modifier = Modifier.gradientFill(brush).size(24.dp),
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
            saga = SagaData(),
            updateContent = { _, data ->
                form = form.copy(title = data.toString())
                pagerState.requestScrollToPage(pagerState.currentPage + 1)
            },
        )
    }
}
