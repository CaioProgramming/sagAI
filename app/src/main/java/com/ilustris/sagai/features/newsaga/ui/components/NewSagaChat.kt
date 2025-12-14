@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.newsaga.ui.components

import android.content.res.Configuration
import android.graphics.Matrix
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.RepeatMode.Reverse
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.features.characters.data.model.CharacterInfo
import com.ilustris.sagai.features.newsaga.data.model.CallBackAction
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.SagaDraft
import com.ilustris.sagai.features.newsaga.data.model.SagaForm
import com.ilustris.sagai.features.newsaga.data.model.Sender
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.saga.chat.ui.components.BubbleStyle
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.theme.MorphPolygonShape
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.SagaTitle
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shape
import com.ilustris.sagai.ui.theme.solidGradient
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NewSagaChat(
    currentForm: SagaForm? = null,
    messages: List<ChatMessage>,
    callback: CallBackAction? = null,
    onSendMessage: (String) -> Unit,
    onRetry: () -> Unit = {},
    saveSaga: () -> Unit = {},
    updateGenre: (Genre) -> Unit = {},
    resetSaga: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    userInputHint: String? = "Chat with SagAI...",
    isLoading: Boolean = false,
    isGenerating: Boolean = false,
    inputSuggestions: List<String> = emptyList(),
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }
    var inputField by remember { mutableStateOf(TextFieldValue("")) }
    var showThemes by remember { mutableStateOf(false) }
    val sheetState =
        rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun sendMessage() {
        onSendMessage(inputField.text)
        inputField = TextFieldValue("")
    }

    AnimatedContent(currentForm?.saga?.genre, transitionSpec = {
        fadeIn() togetherWith fadeOut()
    }) {
        val defaultGenre = remember { Genre.entries.random() }
        val genre = it ?: defaultGenre

        ConstraintLayout(
            Modifier
                .navigationBarsPadding()
                .fillMaxSize(),
        ) {
            val (chatList, inputView) = createRefs()

            LazyColumn(
                Modifier.constrainAs(chatList) {
                    top.linkTo(parent.top)
                    bottom.linkTo(inputView.top)
                    width = Dimension.matchParent
                    height = Dimension.fillToConstraints
                },
                state = listState,
            ) {
                stickyHeader {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .statusBarsPadding()
                                .padding(16.dp),
                    ) {
                        IconButton(onClick = {
                            onNavigateBack()
                        }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                painterResource(R.drawable.ic_back_left),
                                contentDescription = stringResource(R.string.back_button_description),
                                modifier = Modifier.padding(4.dp),
                            )
                        }

                        SagaTitle(
                            Modifier
                                .statusBarsPadding()
                                .weight(1f),
                        )

                        Box(Modifier.size(24.dp))
                    }
                }

                if (messages.size == 1) {
                    item {
                        val message = messages.first()

                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier =
                                Modifier
                                    .animateItem()
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                        ) {
                            StarryAnimation(
                                genre,
                                Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .size(150.dp),
                            )

                            Text(
                                message.text,
                                style =
                                    MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = genre.headerFont(),
                                        brush = genre.gradient(),
                                        textAlign = TextAlign.Center,
                                    ),
                                modifier = Modifier.reactiveShimmer(true),
                            )
                        }
                    }
                } else {
                    items(messages) { message ->
                        ChatMessageBubble(
                            message,
                            genre,
                            sagaForm = currentForm,
                            isLast = messages.last() == message,
                            modifier = Modifier.animateItem(),

                        )
                    }
                }

                if (isGenerating || isLoading) {
                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier =
                                Modifier
                                    .animateItem()
                                    .fillMaxWidth()
                                    .padding(8.dp),
                        ) { StarryAnimation(genre, Modifier.size(50.dp)) }
                    }
                }

                if (callback == CallBackAction.AWAITING_CONFIRMATION && isLoading.not()) {
                    item {
                        Button(
                            shape = it.shape(),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                ),
                            onClick = { saveSaga() },
                            modifier =
                                Modifier
                                    .padding(16.dp)
                                    .border(1.dp, genre.gradient(), it.shape())
                                    .background(genre.gradient(), it.shape())
                                    .fillMaxWidth()
                                    .reactiveShimmer(true),
                        ) {
                            Text(
                                stringResource(R.string.save_saga),
                                style =
                                    MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = genre.bodyFont(),
                                        color = genre.iconColor,
                                    ),
                            )
                        }
                    }
                }
            }

            val isBusy = isLoading || isGenerating
            remember {
                if (isBusy) {
                    Brush.verticalGradient(genre.colorPalette())
                } else {
                    Color.Transparent.solidGradient()
                }
            }
            val glowRadius by animateDpAsState(
                targetValue = if (isBusy) 10.dp else 5.dp,
                label = "glowRadius",
                animationSpec = tween(500),
            )
            remember { it.shape() }

            Column(
                Modifier.constrainAs(inputView) {
                    bottom.linkTo(parent.bottom)
                    width = Dimension.matchParent
                },
            ) {
                val shape = remember { genre.shape() }
                AnimatedVisibility(callback != CallBackAction.AWAITING_CONFIRMATION) {
                    LazyRow(
                        modifier =
                            Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        items(inputSuggestions) { suggestion ->
                            Button(
                                onClick = {
                                    inputField = TextFieldValue(suggestion)
                                },
                                shape = it.shape(),
                                modifier =
                                    Modifier
                                        .border(1.dp, genre.color.gradientFade(), it.shape())
                                        .fillParentMaxWidth(.6f),
                                colors =
                                    ButtonDefaults.outlinedButtonColors().copy(
                                        contentColor = genre.color,
                                        containerColor = Color.Transparent,
                                    ),
                            ) {
                                Text(
                                    suggestion,
                                    style =
                                        MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = genre.bodyFont(),
                                            brush = genre.gradient(),
                                        ),
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(isLoading.not() && messages.isNotEmpty()) {
                    // Add rotation animation for the border
                    val infiniteTransition = rememberInfiniteTransition(label = "border_animation")
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec =
                            infiniteRepeatable(
                                animation = tween(3000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart,
                            ),
                        label = "rotation",
                    )

                    Column(
                        Modifier
                            .imePadding()
                            .animateContentSize()
                            .padding(16.dp)
                            .dropShadow(
                                shape = shape,
                                shadow =
                                    Shadow(
                                        radius = glowRadius,
                                        spread = 2.dp,
                                        color = genre.color,
                                        offset = DpOffset.Zero,
                                    ),
                            )
                            .clip(genre.shape())
                            .drawWithContent {
                                drawContent()
                                val outline = shape.createOutline(size, layoutDirection, this)
                                if (isBusy) {
                                    val brush =
                                        object : ShaderBrush() {
                                            override fun createShader(size: Size): Shader {
                                                val shader =
                                                    (
                                                            sweepGradient(
                                                                genre.colorPalette(),
                                                            ) as ShaderBrush
                                                            ).createShader(size)
                                                val matrix = Matrix()
                                                matrix.setRotate(
                                                    rotation,
                                                    size.width / 2,
                                                    size.height / 2,
                                                )
                                                shader.setLocalMatrix(matrix)
                                                return shader
                                            }
                                        }
                                    drawOutline(
                                        outline = outline,
                                        brush = brush,
                                        style = Stroke(width = 1.dp.toPx()),
                                    )
                                } else {
                                    drawOutline(
                                        outline = outline,
                                        brush = genre.color.gradientFade(),
                                        style = Stroke(width = 1.dp.toPx()),
                                    )
                                }
                            }
                            .background(MaterialTheme.colorScheme.surfaceContainer, shape),
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            BasicTextField(
                                value = inputField,
                                onValueChange = { inputField = it },
                                enabled = !isBusy,
                                cursorBrush = genre.color.solidGradient(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = { sendMessage() }),
                                textStyle =
                                    MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = genre.bodyFont(),
                                        color = MaterialTheme.colorScheme.onBackground,
                                    ),
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .padding(8.dp),
                                decorationBox = { innerTextField ->
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (inputField.text.isEmpty()) {
                                            Text(
                                                text = userInputHint ?: emptyString(),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurface.copy(
                                                    alpha = 0.5f
                                                ),
                                                fontFamily = genre.bodyFont(),
                                            )
                                        }
                                        innerTextField()
                                    }
                                },
                            )

                            IconButton(
                                onClick = { sendMessage() },
                                enabled = inputField.text.isNotEmpty(),
                                colors =
                                    IconButtonDefaults.iconButtonColors(
                                        containerColor = genre.color,
                                        disabledContainerColor = MaterialTheme.colorScheme.background.copy(
                                            alpha = .3f
                                        ),
                                        contentColor = genre.iconColor,
                                        disabledContentColor = MaterialTheme.colorScheme.onBackground.copy(
                                            alpha = .3f
                                        ),
                                    ),
                                modifier =
                                    Modifier
                                        .padding(8.dp)
                                        .size(32.dp),
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_arrow_up),
                                    contentDescription = "Send message",
                                    modifier = Modifier.padding(4.dp),
                                )
                            }
                        }

                        LazyRow(
                            modifier =
                                Modifier
                                    .animateContentSize()
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            val iconSize = 32.dp
                            item {
                                val shape = remember { genre.shape() }
                                val tooltipState =
                                    rememberTooltipState(
                                        isPersistent = true,
                                    )
                                val tooltipPositionProvider =
                                    TooltipDefaults.rememberPlainTooltipPositionProvider(
                                        spacingBetweenTooltipAndAnchor = 8.dp,
                                    )
                                TooltipBox(
                                    positionProvider = tooltipPositionProvider,
                                    state = tooltipState,
                                    tooltip = {
                                        Column(
                                            modifier =
                                                Modifier
                                                    .padding(16.dp)
                                                    .border(
                                                        1.dp,
                                                        genre.color.gradientFade(),
                                                        shape,
                                                    )
                                                    .background(
                                                        MaterialTheme.colorScheme.surface,
                                                        shape,
                                                    )
                                                    .padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            val character = currentForm?.character
                                            Text(
                                                text = character?.name ?: emptyString(),
                                                style =
                                                    MaterialTheme.typography.titleMedium.copy(
                                                        fontFamily = genre.headerFont(),
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                    ),
                                            )
                                            Text(
                                                text = character?.description ?: emptyString(),
                                                style =
                                                    MaterialTheme.typography.bodySmall.copy(
                                                        fontFamily = genre.bodyFont(),
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                    ),
                                                modifier = Modifier.padding(top = 4.dp),
                                            )

                                            Text(
                                                "Escreva mais sobre seu personagem para enriquecer seus detalhes",
                                                style =
                                                    MaterialTheme.typography.labelMedium.copy(
                                                        fontFamily = genre.bodyFont(),
                                                    ),
                                                modifier =
                                                    Modifier
                                                        .align(Alignment.CenterHorizontally)
                                                        .alpha(.5f),
                                            )
                                        }
                                    },
                                ) {
                                    Icon(
                                        painterResource(R.drawable.ic_eye_mask),
                                        contentDescription = "Select character",
                                        modifier =
                                            Modifier
                                                .clip(CircleShape)
                                                .clickable(
                                                    enabled = currentForm?.character?.name?.isNotEmpty() == true,
                                                    onClick = {
                                                        coroutineScope.launch { tooltipState.show() }
                                                    },
                                                )
                                                .size(iconSize)
                                                .padding(8.dp)
                                                .gradientFill(genre.color.gradientFade()),
                                    )
                                }
                            }

                            item {
                                val tooltipState =
                                    rememberTooltipState(
                                        isPersistent = true,
                                    )
                                val tooltipPositionProvider =
                                    TooltipDefaults.rememberPlainTooltipPositionProvider(
                                        spacingBetweenTooltipAndAnchor = 8.dp,
                                    )

                                TooltipBox(
                                    positionProvider = tooltipPositionProvider,
                                    state = tooltipState,
                                    tooltip = {
                                        Column(
                                            Modifier
                                                .padding(18.dp)
                                                .fillMaxWidth()
                                                .border(
                                                    1.dp,
                                                    genre.color.gradientFade(),
                                                    it.shape()
                                                )
                                                .background(
                                                    MaterialTheme.colorScheme.background,
                                                    it.shape(),
                                                ),
                                        ) {
                                            Row {
                                                Text(
                                                    "Temas",
                                                    style =
                                                        MaterialTheme.typography.titleMedium.copy(
                                                            fontFamily = genre.bodyFont(),
                                                        ),
                                                    modifier =
                                                        Modifier
                                                            .weight(1f)
                                                            .animateContentSize()
                                                            .padding(16.dp),
                                                )

                                                Text(
                                                    stringResource(R.string.see_more),
                                                    style =
                                                        MaterialTheme.typography.titleMedium.copy(
                                                            fontFamily = genre.bodyFont(),
                                                            color = genre.color,
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
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .padding(8.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            ) {
                                                val genres = Genre.entries
                                                items(genres) { g ->
                                                    GenreAvatar(
                                                        g,
                                                        true,
                                                        48.dp,
                                                        genre == g,
                                                        modifier = Modifier.padding(8.dp),
                                                    ) {
                                                        updateGenre(g)
                                                    }
                                                }
                                            }
                                        }
                                    },
                                ) {
                                    Icon(
                                        painterResource(genre.background),
                                        contentDescription = "Select genre",
                                        modifier =
                                            Modifier
                                                .clip(CircleShape)
                                                .clickable(onClick = {
                                                    coroutineScope.launch { tooltipState.show() }
                                                })
                                                .size(iconSize)
                                                .padding(8.dp)
                                                .gradientFill(genre.color.gradientFade()),
                                    )
                                }
                            }

                            item {
                                Icon(
                                    painterResource(R.drawable.baseline_refresh_24),
                                    contentDescription = "Reset saga",
                                    modifier =
                                        Modifier
                                            .clip(CircleShape)
                                            .clickable(
                                                enabled = messages.size > 1,
                                                onClick = {
                                                    resetSaga()
                                                },
                                            )
                                            .size(iconSize)
                                            .padding(8.dp)
                                            .gradientFill(genre.color.gradientFade()),
                                )
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

                items(Genre.entries) { genr ->
                    GenreCard(
                        genre = genr,
                        isSelected = genr == currentForm?.saga?.genre,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                                .aspectRatio(.5f),
                    ) {
                        showThemes = false
                        updateGenre(genr)
                    }
                }
            }
        }
    }
}

@Composable
private fun StarryAnimation(
    genre: Genre,
    modifier: Modifier = Modifier,
) {
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
                    tween(
                        3.seconds.toInt(DurationUnit.MILLISECONDS),
                        easing = EaseIn,
                    ),
                    repeatMode = Reverse,
                ),
            label = "morph",
        )

    val shape =
        MorphPolygonShape(
            morph,
            morphProgress,
        )
    Box(
        modifier =
            modifier
                .clip(shape),
    ) {
        StarryTextPlaceholder(
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .fillMaxSize()
                    .gradientFill(genre.gradient(true)),
        )
    }
}

@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    genre: Genre,
    isLast: Boolean,
    sagaForm: SagaForm?,
    modifier: Modifier = Modifier,
) {
    val textColor = remember { genre.iconColor }
    val isUSer = remember { message.sender == Sender.USER }
    remember { if (isUSer) genre.iconColor else genre.color }

    val bubbleStyle =
        remember(message.sender == Sender.USER, genre) {
            if (isUSer) {
                BubbleStyle.userBubble(genre)
            } else {
                BubbleStyle.characterBubble(genre, false)
            }
        }

    val bubbleShape = remember { genre.bubble(bubbleStyle.tailAlignment) }

    Column(modifier.padding(16.dp)) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            horizontalArrangement = if (message.sender == Sender.USER) Arrangement.End else Arrangement.Start,
        ) {
            if (message.sender == Sender.AI) {
                Image(
                    painterResource(R.drawable.ic_spark),
                    null,
                    modifier =
                        Modifier
                            .gradientFill(bubbleStyle.backgroundColor.gradientFade())
                            .size(24.dp)
                            .align(Alignment.Bottom),
                )
            }

            SimpleTypewriterText(
                text = message.text,
                isAnimated = isLast,
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontFamily = genre.bodyFont(),
                        color = textColor,
                    ),
                modifier =
                    Modifier
                        .background(bubbleStyle.backgroundColor, bubbleShape)
                        .padding(16.dp),
            )
        }


        if (message.callback == CallBackAction.AWAITING_CONFIRMATION && message.sender == Sender.AI) {
            sagaForm?.let { SagaFormSummaryCards(it, genre) }

        }
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NewSagaChatPreview() {
    SagAIScaffold {
        NewSagaChat(
            callback = CallBackAction.AWAITING_CONFIRMATION,
            currentForm =
                SagaForm(
                    saga =
                        SagaDraft(
                            "The one",
                            "A deep journey to find the one of a kind",
                        ),
                    character =
                        CharacterInfo(
                            "Luke",
                            "A humble warrior trying to find its place",
                        ),
                ),
            messages =
                listOf(
                    ChatMessage(text = "Hello there!", sender = Sender.USER),
                    ChatMessage(
                        text = "Hi! How can I help you?",
                        sender = Sender.AI,
                        callback = CallBackAction.AWAITING_CONFIRMATION
                    ),
                ),
            onSendMessage = {},
        )
    }
}
