package com.ilustris.sagai.features.newsaga.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable // Added for SuggestionChip
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.ilustris.sagai.R
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.newsaga.data.model.ChatMessage // Correct import
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.MessageType // Correct import
import com.ilustris.sagai.features.newsaga.data.model.SagaForm // Added import for SagaForm
import com.ilustris.sagai.features.saga.chat.ui.components.BubbleStyle
// import com.ilustris.sagai.features.saga.chat.ui.components.hint // Removed if not used
import com.ilustris.sagai.ui.theme.CurvedChatBubbleShape
import com.ilustris.sagai.ui.theme.GradientType
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.BlurredGlowContainer
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.solidGradient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NewSagaChat(
    currentForm: SagaForm? = null,
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onRetry: () -> Unit = {},
    saveSaga: () -> Unit = {},
    modifier: Modifier = Modifier,
    userInputHint: String? = "Chat with SagAI...",
    isLoading: Boolean = false,
    isGenerating: Boolean = false,
    isError: Boolean = false,
    sagaToReveal: Saga? = null,
    inputSuggestions: List<String> = emptyList(),
) {
    val genre = currentForm?.saga?.genre ?: Genre.FANTASY
    var initialAnimationFinished by remember { mutableStateOf(true) }
    var orchestratingSparkVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (isLoading) return@LaunchedEffect
        delay(300L)
        orchestratingSparkVisible = true
        delay(2700L)
        initialAnimationFinished = true
    }

    val sparkIconSize = 64.dp
    val sparkOverlap = sparkIconSize / 2

    val sparkAlpha by animateFloatAsState(
        targetValue = if (orchestratingSparkVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "SparkAlpha",
    )
    val sparkScale by animateFloatAsState(
        targetValue = if (initialAnimationFinished) 1f else 1.5f,
        animationSpec =
            tween(
                durationMillis = 1000,
                delayMillis = if (initialAnimationFinished) 0 else 200,
                easing = FastOutSlowInEasing,
            ),
        label = "SparkScale",
    )
    val sparkVerticalBias by animateFloatAsState(
        targetValue = if (initialAnimationFinished || isLoading) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "SparkVerticalBias",
    )
    val sparkOffsetY by animateDpAsState(
        targetValue = if (initialAnimationFinished) -sparkOverlap else 0.dp,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "SparkOffsetY",
    )

    val cardScale by animateFloatAsState(
        targetValue = if (initialAnimationFinished) 1f else 0.0f,
        animationSpec =
            tween(
                durationMillis = 700,
                delayMillis = if (initialAnimationFinished) 300 else 0,
                easing = FastOutSlowInEasing,
            ),
        label = "CardScale",
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (initialAnimationFinished) 1f else 0f,
        animationSpec =
            tween(
                durationMillis = 700,
                delayMillis = if (initialAnimationFinished) 300 else 0,
                easing = LinearEasing,
            ),
        label = "CardAlpha",
    )

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val brush = genre.gradient(isLoading)

    LaunchedEffect(messages.size, initialAnimationFinished) {
        if (messages.isNotEmpty() && initialAnimationFinished) {
            coroutineScope.launch {
                var lastContentIndex = messages.size - 1
                if (messages.lastOrNull()?.type == MessageType.GENRE_SELECTION ||
                    messages.lastOrNull()?.type == MessageType.FORM_CONFIRMATION
                ) {
                    lastContentIndex = messages.size
                }
                listState.animateScrollToItem(lastContentIndex)
            }
        }
    }
    var inputField by remember { mutableStateOf(TextFieldValue("")) }
    var reviewSaga by remember { mutableStateOf(false) }
    var reviewEnabled by remember { mutableStateOf(false) }

    fun sendMessage() {
        onSendMessage(inputField.text)
        inputField = TextFieldValue("")
    }

    LaunchedEffect(currentForm) {
        val isValidSaga =
            currentForm != null
        currentForm?.saga?.title?.isNotEmpty() == true &&
            currentForm.saga.description.length > 20 &&
            currentForm.saga.genre != null

        val isValidCharacter =
            currentForm?.character?.name?.isNotEmpty() == true &&
                currentForm.character.briefDescription.length > 20

        reviewEnabled = isValidSaga && isValidCharacter
    }

    Box(modifier = modifier.fillMaxSize().reactiveShimmer(isLoading || isGenerating)) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .fillMaxHeight(0.88f)
                    .scale(cardScale)
                    .alpha(cardAlpha),
        ) {
            AnimatedContent(
                targetState = genre,
                transitionSpec = {
                    fadeIn(animationSpec = tween(600)) togetherWith
                        fadeOut(
                            animationSpec =
                                tween(
                                    600,
                                ),
                        )
                },
                modifier = Modifier.fillMaxSize(),
                label = "GenreAnimatedContent",
            ) { animatedGenre ->
                Column(
                    modifier =
                        Modifier
                            .padding(horizontal = 16.dp, vertical = 32.dp)
                            .clip(RoundedCornerShape(animatedGenre.cornerSize()))
                            .border(
                                2.dp,
                                genre.color,
                                RoundedCornerShape(animatedGenre.cornerSize()),
                            ).background(
                                MaterialTheme.colorScheme.surfaceContainer,
                                shape = RoundedCornerShape(animatedGenre.cornerSize()),
                            ).fillMaxSize(),
                ) {
                    AnimatedVisibility(
                        reviewSaga,
                        enter = fadeIn(tween(1000)) + slideInVertically { -it },
                        exit = fadeOut(),
                    ) {
                        AnimatedContent(currentForm) { form ->
                            Column(Modifier.fillMaxSize()) {
                                val pagerState = rememberPagerState { 2 }

                                Row(
                                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    IconButton(onClick = {
                                        reviewSaga = false
                                    }) {
                                        Icon(Icons.Rounded.Close, contentDescription = "close")
                                    }
                                    repeat(2) {
                                        val backgroundAlpha by animateFloatAsState(
                                            if (it == pagerState.currentPage) 1f else .4f,
                                        )
                                        Box(
                                            Modifier
                                                .weight(1f)
                                                .background(
                                                    animatedGenre.gradient(),
                                                    RoundedCornerShape(animatedGenre.cornerSize()),
                                                ).height(5.dp)
                                                .alpha(backgroundAlpha),
                                        )
                                    }
                                }

                                HorizontalPager(pagerState, modifier = Modifier.padding(top = 32.dp).weight(1f)) {
                                    when (it) {
                                        0 ->

                                            Column(
                                                modifier =
                                                    Modifier
                                                        .padding(12.dp)
                                                        .fillMaxSize(),
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                            ) {
                                                Text(
                                                    "Saga Details",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = animatedGenre.color.copy(alpha = 0.7f),
                                                )
                                                Text(
                                                    currentForm?.saga?.title ?: "No title yet",
                                                    style =
                                                        MaterialTheme.typography.titleMedium.copy(
                                                            fontFamily = animatedGenre.headerFont(),
                                                            brush = animatedGenre.gradient(),
                                                        ),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                                Text(
                                                    "Genre: ${currentForm?.saga?.genre?.title ?: "Not set"}",
                                                    style =
                                                        MaterialTheme.typography.bodySmall.copy(
                                                            fontFamily = animatedGenre.bodyFont(),
                                                        ),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                                Text(
                                                    currentForm?.saga?.description
                                                        ?: "No description yet",
                                                    style =
                                                        MaterialTheme.typography.bodySmall.copy(
                                                            fontFamily = animatedGenre.bodyFont(),
                                                        ),
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                )
                                            }

                                        else ->

                                            Column(
                                                modifier =
                                                    Modifier
                                                        .padding(12.dp)
                                                        .fillMaxSize(),
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                            ) {
                                                Text(
                                                    "Character Details",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = animatedGenre.color.copy(alpha = 0.7f),
                                                )
                                                Text(
                                                    currentForm?.character?.name ?: "Unknown",
                                                    style =
                                                        MaterialTheme.typography.titleLarge.copy(
                                                            fontFamily = animatedGenre.headerFont(),
                                                            brush = animatedGenre.gradient(),
                                                        ),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                                Text(
                                                    currentForm?.character?.briefDescription ?: "No description yet",
                                                    style =
                                                        MaterialTheme.typography.bodySmall.copy(
                                                            fontFamily = animatedGenre.bodyFont(),
                                                        ),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                    }
                                }

                                Button(
                                    onClick = {
                                        saveSaga()
                                    },
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    shape = RoundedCornerShape(animatedGenre.cornerSize()),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
                                ) {
                                    val brush = animatedGenre.gradient(true)
                                    Image(
                                        painter = painterResource(R.drawable.ic_spark),
                                        contentDescription = "Save",
                                        modifier = Modifier.size(24.dp).gradientFill(brush),
                                    )
                                    Text("Salvar", modifier = Modifier.gradientFill(brush))
                                    Image(
                                        painter = painterResource(R.drawable.ic_spark),
                                        contentDescription = "Save",
                                        modifier = Modifier.size(24.dp).gradientFill(brush),
                                    )
                                }
                            }
                        }
                    }
                    LazyColumn(
                        state = listState,
                        modifier =
                            Modifier
                                .weight(1f)
                                .padding(horizontal = 0.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp + sparkOverlap),
                    ) {
                        items(messages) { message ->
                            ChatMessageBubble(
                                message = message,
                                genre = animatedGenre,
                                isLast = messages.last() == message,
                            )
                            if (message.type == MessageType.GENRE_SELECTION) {
                                val genres = Genre.entries
                                LazyRow(
                                    contentPadding =
                                        PaddingValues(
                                            horizontal = 16.dp,
                                            vertical = 8.dp,
                                        ),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    items(
                                        genres,
                                    ) { genreEntry ->
                                        GenreCard(
                                            genre = genreEntry,
                                            isSelected = true,
                                            modifier =
                                                Modifier
                                                    .size(170.dp),
                                            onClick = { selectedGenre ->
                                                inputField = TextFieldValue(selectedGenre.title)
                                            },
                                        )
                                    }
                                }
                            }
                            if (message.type == MessageType.FORM_CONFIRMATION && currentForm != null) {
                                currentForm?.let { sagaForm ->
                                    LazyRow(
                                        contentPadding =
                                            PaddingValues(
                                                horizontal = 16.dp,
                                                vertical = 8.dp,
                                            ),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        // Saga Details Card
                                        item {
                                            Card(
                                                modifier =
                                                    Modifier
                                                        .width(200.dp)
                                                        .height(250.dp),
                                                shape = RoundedCornerShape(animatedGenre.cornerSize()),
                                                border =
                                                    BorderStroke(
                                                        1.dp,
                                                        animatedGenre.gradient(),
                                                    ),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                            ) {
                                                Column(
                                                    modifier =
                                                        Modifier
                                                            .padding(12.dp)
                                                            .fillMaxSize(),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                                ) {
                                                    Text(
                                                        "Saga Details",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = animatedGenre.color.copy(alpha = 0.7f),
                                                    )
                                                    Text(
                                                        sagaForm.saga.title,
                                                        style =
                                                            MaterialTheme.typography.titleMedium.copy(
                                                                fontFamily = animatedGenre.headerFont(),
                                                                brush = animatedGenre.gradient(),
                                                            ),
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                    Text(
                                                        "Genre: ${sagaForm.saga?.genre?.title ?: "Not set"}",
                                                        style =
                                                            MaterialTheme.typography.bodySmall.copy(
                                                                fontFamily = animatedGenre.bodyFont(),
                                                            ),
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                    Text(
                                                        "Description: ${sagaForm.saga?.description}",
                                                        style =
                                                            MaterialTheme.typography.bodySmall.copy(
                                                                fontFamily = animatedGenre.bodyFont(),
                                                            ),
                                                        maxLines = 3,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                }
                                            }
                                        }

                                        // Character Details Card
                                        item {
                                            Card(
                                                modifier =
                                                    Modifier
                                                        .width(200.dp)
                                                        .height(250.dp),
                                                shape = RoundedCornerShape(animatedGenre.cornerSize()),
                                                border =
                                                    BorderStroke(
                                                        1.dp,
                                                        animatedGenre.gradient(),
                                                    ),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                                            ) {
                                                Column(
                                                    modifier =
                                                        Modifier
                                                            .padding(12.dp)
                                                            .fillMaxSize(),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                                ) {
                                                    Text(
                                                        "Character Details",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = animatedGenre.color.copy(alpha = 0.7f),
                                                    )
                                                    Text(
                                                        sagaForm.character?.name ?: "Unknown",
                                                        style =
                                                            MaterialTheme.typography.titleLarge.copy(
                                                                fontFamily = animatedGenre.headerFont(),
                                                                brush = animatedGenre.gradient(),
                                                            ),
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                    Text(
                                                        "${sagaForm.character?.briefDescription}",
                                                        style =
                                                            MaterialTheme.typography.bodySmall.copy(
                                                                fontFamily = animatedGenre.bodyFont(),
                                                            ),
                                                        maxLines = 2,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (isError) {
                            item {
                                Row {
                                    IconButton(onClick = {
                                        onRetry.invoke()
                                    }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Rounded.Refresh, contentDescription = "retry")
                                    }

                                    Text(
                                        "Ocorreu um erro inesperado, tente novamente",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = inputSuggestions.isNotEmpty() && isLoading.not(),
                        enter =
                            fadeIn(animationSpec = tween(300)) +
                                slideInVertically(
                                    initialOffsetY = { it / 2 },
                                    animationSpec = tween(300),
                                ),
                        exit =
                            fadeOut(animationSpec = tween(300)) +
                                slideOutVertically(
                                    targetOffsetY = { it / 2 },
                                    animationSpec = tween(300),
                                ),
                    ) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            items(inputSuggestions) { suggestion ->
                                SuggestionChip(
                                    suggestion = suggestion,
                                    genre = animatedGenre,
                                    onClick = {
                                        inputField = TextFieldValue(suggestion)
                                    },
                                )
                            }
                        }
                    }

                    val inputBrush =
                        if (isLoading) {
                            animatedGenre.gradient(
                                true,
                                gradientType = GradientType.LINEAR,
                                duration = 2.seconds,
                            )
                        } else {
                            Color.Transparent.solidGradient()
                        }
                    val glowRadius by animateFloatAsState(
                        if (isLoading.not()) 0f else 30f,
                        label = "inputGlowRadius",
                    )
                    val inputShape = RoundedCornerShape(animatedGenre.cornerSize())

                    BlurredGlowContainer(
                        brush = inputBrush,
                        blurSigma = glowRadius,
                        shape = inputShape,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 2.dp, top = 2.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =
                                Modifier
                                    .padding(4.dp)
                                    .fillMaxWidth()
                                    .border(1.dp, inputBrush, inputShape)
                                    .background(MaterialTheme.colorScheme.background, inputShape),
                        ) {
                            val textStyle =
                                MaterialTheme.typography.labelLarge.copy(
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontFamily = animatedGenre.bodyFont(),
                                )
                            val maxLength = 300

                            BasicTextField(
                                inputField,
                                enabled = isGenerating.not(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions =
                                    KeyboardActions(onSend = {
                                        if (isLoading.not() && inputField.text.isNotEmpty()) {
                                            sendMessage()
                                        }
                                    }),
                                onValueChange = {
                                    if (it.text.length <= maxLength) {
                                        inputField = it
                                    }
                                },
                                textStyle = textStyle,
                                cursorBrush = animatedGenre.gradient(),
                                decorationBox = { innerTextField ->
                                    val boxPadding = 12.dp
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (inputField.text.isEmpty()) {
                                            Text(
                                                (
                                                    userInputHint

                                                        ?: "Type your message here..."
                                                ).ifEmpty { "Type your message here..." },
                                                style = textStyle,
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .padding(boxPadding)
                                                        .alpha(.4f),
                                            )
                                        } else {
                                            Box(Modifier.padding(boxPadding)) {
                                                innerTextField()
                                            }
                                        }
                                    }
                                },
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .animateContentSize(),
                            )

                            AnimatedVisibility(
                                inputField.text.isNotEmpty(),
                                enter = scaleIn(animationSpec = tween(easing = LinearOutSlowInEasing)),
                                exit = scaleOut(animationSpec = tween(easing = EaseIn)),
                            ) {
                                val buttonColor by animateColorAsState(
                                    if (isLoading.not()) {
                                        animatedGenre.color
                                    } else {
                                        Color.Transparent
                                    },
                                    label = "sendButtonColor",
                                )
                                IconButton(
                                    enabled = isGenerating.not(),
                                    onClick = {
                                        if (isLoading) return@IconButton
                                        sendMessage()
                                    },
                                    modifier =
                                        Modifier
                                            .padding(4.dp)
                                            .background(
                                                buttonColor,
                                                CircleShape,
                                            ).size(32.dp),
                                ) {
                                    Icon(
                                        painterResource(R.drawable.ic_arrow_up),
                                        contentDescription = "Send Message",
                                        modifier =
                                            Modifier
                                                .padding(2.dp)
                                                .fillMaxSize(),
                                        tint = animatedGenre.iconColor,
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(reviewEnabled) {
                        Button(
                            onClick = {
                                reviewSaga = true
                            },
                            shape = RoundedCornerShape(animatedGenre.cornerSize()),
                            colors =
                                ButtonDefaults.buttonColors().copy(
                                    containerColor = Color.Black,
                                    contentColor = Color.White,
                                ),
                            modifier =
                                Modifier
                                    .padding(horizontal = 16.dp)
                                    .fillMaxWidth(),
                        ) {
                            Image(
                                painterResource(R.drawable.ic_spark),
                                contentDescription = "Review Saga",
                                modifier =
                                    Modifier.size(24.dp).gradientFill(
                                        animatedGenre.gradient(true),
                                    ),
                            )
                            Text(
                                "Review Saga",
                                modifier =
                                    Modifier.padding(start = 8.dp).gradientFill(
                                        animatedGenre.gradient(true),
                                    ),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            AnimatedVisibility(sagaToReveal != null, enter = fadeIn(tween(1000))) {
                sagaToReveal?.let {
                    SagaCard(saga = it, modifier = Modifier.fillMaxSize())
                }
            }
        }

        Icon(
            painter = painterResource(id = R.drawable.ic_spark),
            contentDescription = "Orchestrating Spark",
            modifier =
                Modifier
                    .align(BiasAlignment(horizontalBias = 0f, verticalBias = sparkVerticalBias))
                    .offset(y = sparkOffsetY)
                    .scale(sparkScale)
                    .alpha(sparkAlpha)
                    .size(sparkIconSize)
                    .zIndex(1f),
            tint = genre.color,
        )
    }
}

@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    genre: Genre,
    isLast: Boolean,
) {
    val textColor = genre.iconColor
    val loadingIndicatorColor = if (message.isUser) genre.iconColor else genre.color

    val bubbleStyle =
        remember(message.isUser, genre) {
            if (message.isUser) {
                BubbleStyle.userBubble(genre)
            } else {
                BubbleStyle.characterBubble(genre, false)
            }
        }
    val cornerSize = genre.cornerSize()

    val bubbleShape =
        remember(bubbleStyle.tailAlignment, cornerSize) {
            CurvedChatBubbleShape(
                cornerRadius = cornerSize,
                tailWidth = 2.dp,
                tailHeight = 4.dp,
                tailAlignment = bubbleStyle.tailAlignment,
            )
        }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier =
                Modifier
                    .background(bubbleStyle.backgroundColor, bubbleShape)
                    .padding(12.dp),
        ) {
            if (message.text.isNotBlank()) {
                SimpleTypewriterText(
                    text = message.text,
                    isAnimated = isLast,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = genre.bodyFont(),
                            color = textColor,
                        ),
                )
            }
        }
    }
}

// Added a simple SuggestionChip composable for demonstration
@Composable
fun SuggestionChip(
    suggestion: String,
    genre: Genre,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = CircleShape,
        color = genre.color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, genre.color.copy(alpha = 0.5f)),
    ) {
        Text(
            text = suggestion,
            style = MaterialTheme.typography.labelMedium.copy(fontFamily = genre.bodyFont()),
            color = genre.color,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun NewSagaChatPreviewWithOrchestratingAnimation() {
    SagAITheme {
        var currentGenre by remember { mutableStateOf(Genre.FANTASY) }
        val sampleMessages =
            remember {
                mutableStateListOf(
                    ChatMessage(
                        text = "Welcome! This is a ${currentGenre.name} chat.",
                        isUser = false,
                    ),
                    ChatMessage(text = "I wish to embark on an epic quest.", isUser = true),
                    ChatMessage(
                        text = "Please choose a genre for our story:",
                        isUser = false,
                        type = MessageType.GENRE_SELECTION,
                    ),
                    // Example for FORM_CONFIRMATION preview
                    ChatMessage(
                        text = "Here's a summary of your saga. Ready to create it?",
                        isUser = false,
                        type = MessageType.FORM_CONFIRMATION,
                    ),
                )
            }
        // Preview-specific state for hints and suggestions
        var currentHint by remember { mutableStateOf("What happens next in this Fantasy tale?") }
        var currentSuggestions by remember {
            mutableStateOf(
                listOf(
                    "Explore the dark forest",
                    "Visit the king",
                    "Look for clues",
                ),
            )
        }

        LaunchedEffect(currentGenre) {
            sampleMessages[0] =
                sampleMessages[0].copy(text = "Welcome! This is a ${currentGenre.name.uppercase()} chat.")

            val genreSelectionMessageIndex =
                sampleMessages.indexOfFirst { it.type == MessageType.GENRE_SELECTION }
            if (genreSelectionMessageIndex != -1) {
                sampleMessages[genreSelectionMessageIndex] =
                    sampleMessages[genreSelectionMessageIndex].copy(
                        text = "Please select a ${currentGenre.name.lowercase()} sub-theme or confirm the main theme:",
                    )
            }
            // Update hint and suggestions based on genre for preview
            currentHint = "What happens next in this ${currentGenre.name.lowercase()} tale?"
            currentSuggestions =
                when (currentGenre) {
                    Genre.FANTASY ->
                        listOf(
                            "Explore the dark forest",
                            "Seek the ancient artifact",
                            "Consult the oracle",
                        )

                    Genre.SCI_FI ->
                        listOf(
                            "Hack the mainframe",
                            "Negotiate with the smugglers",
                            "Investigate the anomaly",
                        )

                    else -> listOf("Continue the story...", "Describe the scene")
                }
        }

        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Preview Genre:", style = MaterialTheme.typography.titleSmall)
                Genre.entries.forEach { genreEntry ->
                    Button(
                        onClick = { currentGenre = genreEntry },
                        modifier = Modifier.padding(horizontal = 2.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    if (currentGenre ==
                                        genreEntry
                                    ) {
                                        genreEntry.color
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                contentColor =
                                    if (currentGenre ==
                                        genreEntry
                                    ) {
                                        genreEntry.iconColor
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                            ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(genreEntry.name.take(3), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            NewSagaChat(
                messages = sampleMessages,
                userInputHint = currentHint, // Pass the preview's hint
                inputSuggestions = currentSuggestions, // Pass the preview's suggestions
                onSendMessage = { newMessage ->
                    sampleMessages.add(ChatMessage(text = newMessage, isUser = true))
                    sampleMessages.add(
                        ChatMessage(
                            text = "Thinking about your next adventure...",
                            isUser = false,
                        ),
                    )
                },
                isLoading = true,
                sagaToReveal =
                    Saga(
                        title = "The Dragon\'s Echo",
                        description = "A quest to find the last dragon.",
                    ),
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
            )
        }
    }
}
