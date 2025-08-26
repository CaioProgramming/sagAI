@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.ilustris.sagai.features.newsaga.ui.components

import SagaFormCards
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Send
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.ai.client.generativeai.type.content
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.ui.presentation.FormState
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import com.ilustris.sagai.ui.theme.TypewriterText
import com.ilustris.sagai.ui.theme.Typography
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import kotlin.time.Duration.Companion.seconds

@Composable
fun NewSagaAIForm(
    sagaForm: SagaForm,
    isLoading: Boolean = false,
    aiState: FormState,
    sendDescription: (String) -> Unit = {},
    onSave: () -> Unit = {},
) {
    val genre = sagaForm.saga.genre
    val brush = genre?.gradient() ?: Brush.linearGradient(holographicGradient)
    val textFont = genre?.bodyFont()
    var showInputDialog by remember { mutableStateOf(false) }
    var showReview by remember { mutableStateOf(false) }
    val blurRadius by animateDpAsState(
        targetValue = if (showInputDialog) 50.dp else 0.dp,
        label = "BlurRadius",
    )

    val iconScale by animateFloatAsState(
        targetValue = if (showReview || isLoading) .5f else 1f,
        animationSpec = tween(500),
        label = "iconScaleAnimation",
    )

    LaunchedEffect(aiState.readyToSave) {
        if (aiState.readyToSave) {
            showReview = true
        }
    }

    AnimatedVisibility(
        isLoading,
        enter = fadeIn(),
        exit = scaleOut(),
        modifier = Modifier.fillMaxSize(),
    ) {
        StarryTextPlaceholder(modifier = Modifier.fillMaxSize().gradientFill(brush))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier =
            Modifier
                .reactiveShimmer(isLoading)
                .blur(blurRadius)
                .fillMaxSize()
                .padding(16.dp)
                .animateContentSize()
                .verticalScroll(rememberScrollState()),
    ) {
        AnimatedVisibility(isLoading.not()) {
            Text(
                "Let's bring your history to life",
                style =
                    Typography.labelSmall.copy(
                        fontFamily = textFont,
                        brush = brush,
                    ),
                modifier = Modifier.alpha(.4f),
            )
        }

        AnimatedVisibility(isLoading.not()) {
            AnimatedContent(aiState.message) {
                it?.let { message ->
                    SimpleTypewriterText(
                        aiState.message ?: emptyString(),
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                brush = brush,
                                fontFamily = textFont,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                            ),
                    )
                }
            }
        }

        SharedTransitionLayout(modifier = Modifier.fillMaxWidth().weight(1f)) {
            AnimatedContent(showReview) {
                if (it && isLoading.not()) {
                    SagaFormCards(
                        sagaForm,
                        onDismiss = {
                            showReview = false
                        },
                        modifier = Modifier.fillMaxSize(),
                        this@SharedTransitionLayout,
                        this@AnimatedContent,
                    )
                } else {
                    Icon(
                        painterResource(R.drawable.ic_spark),
                        null,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier =
                            Modifier
                                .clickable(!isLoading) {
                                    showReview = true
                                }.sharedElement(
                                    rememberSharedContentState(key = "image"),
                                    animatedVisibilityScope = this@AnimatedContent,
                                ).scale(iconScale)
                                .fillMaxSize()
                                .gradientFill(brush),
                    )
                }
            }
        }

        AnimatedVisibility(isLoading.not()) {
            AnimatedContent(
                showReview,
                modifier =
                    Modifier
                        .padding(24.dp),
            ) {
                if (it) {
                    Button(
                        onClick = { onSave() },
                        shape = MaterialTheme.shapes.extraLarge,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Color.Black,
                                contentColor = Color.White,
                            ),
                        border = BorderStroke(1.dp, brush),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Salvar", modifier = Modifier.gradientFill(brush))
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .shadow(1.dp, MaterialTheme.shapes.extraLarge)
                                .clickable {
                                    showInputDialog = true
                                }.border(1.dp, brush, MaterialTheme.shapes.extraLarge)
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainer,
                                    MaterialTheme.shapes.extraLarge,
                                ).alpha(.4f)
                                .padding(16.dp),
                    ) {
                        Icon(
                            Icons.Default.Add,
                            null,
                            modifier = Modifier.size(24.dp),
                        )

                        Text(
                            aiState.hint ?: "Escrever história...",
                            style =
                                MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            modifier = Modifier.weight(1f),
                        )

                        Icon(
                            Icons.AutoMirrored.Rounded.Send,
                            null,
                            modifier = Modifier.alpha(.4f).size(24.dp),
                        )
                    }
                }
            }
        }
    }

    if (showInputDialog) {
        Dialog(
            onDismissRequest = {
                showInputDialog = false
            },
            properties =
                DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnBackPress = true,
                ),
        ) {
            Column(
                modifier = Modifier.padding(vertical = 50.dp, horizontal = 16.dp).fillMaxSize(),
            ) {
                var inputValue by remember {
                    mutableStateOf("")
                }
                IconButton(onClick = {
                    showInputDialog = false
                }, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Rounded.Close,
                        "Close",
                        tint = Color.White,
                    )
                }

                if (aiState.suggestions.isNotEmpty()) {
                    LazyRow {
                        items(aiState.suggestions) {
                            Text(
                                it,
                                style =
                                    MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = textFont,
                                        brush = brush,
                                    ),
                                modifier =
                                    Modifier
                                        .padding(8.dp)
                                        .fillParentMaxWidth(.7f)
                                        .clip(MaterialTheme.shapes.large)
                                        .border(
                                            1.dp,
                                            brush,
                                            shape = MaterialTheme.shapes.large,
                                        ).background(
                                            MaterialTheme.colorScheme.surfaceContainer.copy(alpha = .3f),
                                            MaterialTheme.shapes.large,
                                        ).clickable {
                                            inputValue = it
                                        }.padding(16.dp),
                            )
                        }
                    }
                }

                TextField(
                    inputValue,
                    onValueChange = {
                        inputValue = it
                    },
                    colors =
                        TextFieldDefaults.colors().copy(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                        ),
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    textStyle =
                        MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = textFont,
                            brush = brush,
                        ),
                    placeholder = {
                        Text(
                            aiState.hint ?: emptyString(),
                            style =
                                MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = textFont,
                                    brush = brush,
                                ),
                            modifier = Modifier.fillMaxSize().alpha(.4f),
                        )
                    },
                )

                Text(
                    "Enviar",
                    style =
                        MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = textFont,
                            brush = brush,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        ),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .border(1.dp, brush, shape = MaterialTheme.shapes.medium)
                            .background(Color.Black, shape = MaterialTheme.shapes.large)
                            .clickable {
                                sendDescription(inputValue)
                                showInputDialog = false
                            }.padding(16.dp),
                )
            }
        }
    }
}

@Preview
@Composable
fun NewSagaAIFormPreview() {
    SagAIScaffold {
        val sagaForm =
            SagaForm(
                saga =
                    SagaDraft(
                        genre = Genre.SCI_FI,
                    ),
            )
        NewSagaAIForm(
            sagaForm = sagaForm,
            isLoading = false,
            FormState(
                "AI Hint",
                suggestions =
                    listOf(
                        "try fantasy",
                    ),
            ),
            sendDescription = {},
        )
    }
}
