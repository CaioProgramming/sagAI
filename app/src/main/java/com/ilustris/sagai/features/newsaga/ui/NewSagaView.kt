package com.ilustris.sagai.features.newsaga.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.ui.pages.NewSagaPages
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient
import kotlin.time.Duration.Companion.seconds

@Composable
fun NewSagaView(
    navHostController: NavHostController,
    createSagaViewModel: CreateSagaViewModel = hiltViewModel(),
) {
    val page by createSagaViewModel.sagaPage.collectAsStateWithLifecycle()
    NewSagaForm(page) {
        createSagaViewModel.updateData(it)
    }
}

@Composable
fun NewSagaForm(
    formPage: NewSagaPages = NewSagaPages.TITLE,
    onUpdate: (String) -> Unit = {},
) {
    var input by remember {
        mutableStateOf("")
    }
    val rotationState = remember { Animatable(0f) }

    val gradient =
        gradientAnimation(
            holographicGradient,
            targetValue = 1000f,
            duration = 5.seconds,
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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier =
            Modifier
                .padding(16.dp)
                .fillMaxSize(),
    ) {
        val composition by rememberLottieComposition(
            LottieCompositionSpec.RawRes(R.raw.sphere),
        )

        val compositionProgress by animateLottieCompositionAsState(
            composition,
            iterations = LottieConstants.IterateForever,
        )

        LottieAnimation(
            composition,
            compositionProgress,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(.6f).gradientFill(gradient),
        )

        AnimatedContent(formPage) {
            Text(
                stringResource(it.title),
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        AnimatedContent(formPage) {
            Text(
                stringResource(it.subtitle),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        TextField(
            input,
            {
                input = it
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(30.dp),
                    ).border(2.dp, brush = gradient, RoundedCornerShape(30.dp)),
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
                    text = stringResource(formPage.inputHint),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = {},
                    modifier =
                        Modifier
                            .background(gradient, CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.background, CircleShape),
                ) {
                    androidx.compose.material3.Icon(Icons.Rounded.Done, "Ok", tint = Color.White)
                }
            },
            singleLine = true,
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

@Preview
@Composable
fun NewSagaViewPreview() {
    SagAIScaffold(title = "nova saga") {
        NewSagaForm()
    }
}
