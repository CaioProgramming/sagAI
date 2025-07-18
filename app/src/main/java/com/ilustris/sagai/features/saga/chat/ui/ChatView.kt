@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class,
    ExperimentalHazeMaterialsApi::class,
)

package com.ilustris.sagai.features.saga.chat.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets // Added import
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible // Added import
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.formatDate
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.characters.ui.CharacterDetailsContent
import com.ilustris.sagai.features.home.data.model.IllustrationVisuals
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.features.saga.chat.domain.model.Suggestion
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.CharacterInfo
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.Message
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.SenderType
import com.ilustris.sagai.features.saga.chat.presentation.ChatAction
import com.ilustris.sagai.features.saga.chat.presentation.ChatState
import com.ilustris.sagai.features.saga.chat.presentation.ChatViewModel
import com.ilustris.sagai.features.saga.chat.ui.components.ChatBubble
import com.ilustris.sagai.features.saga.chat.ui.components.ChatInputView
import com.ilustris.sagai.features.saga.chat.ui.components.icon
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.navigation.navigateToRoute
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.BlurredGlowContainer
import com.ilustris.sagai.ui.theme.components.ConditionalImage
import com.ilustris.sagai.ui.theme.components.SagaTopBar
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.cornerSize
import com.ilustris.sagai.ui.theme.defaultHeaderImage
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.fadeGradientTop
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.genresGradient
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.holographicGradient
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import effectForGenre
import java.util.Calendar
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatView(
    navHostController: NavHostController,
    padding: PaddingValues = PaddingValues(0.dp),
    sagaId: String? = null,
    isDebug: Boolean = false, // Added isDebug parameter
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    val content by viewModel.content.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val characters by viewModel.characters.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val snackBarMessage by viewModel.snackBarMessage.collectAsStateWithLifecycle()
    val contentHaze = rememberHazeState()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()

    LaunchedEffect(sagaId, isDebug) {
        viewModel.initChat(sagaId, isDebug)
    }

    Box {
        AnimatedContent(
            state.value,
            transitionSpec = {
                fadeIn(tween(200)) with fadeOut(tween(700))
            },
            modifier =
                Modifier
                    .fillMaxSize()
                    .hazeSource(contentHaze),
        ) {
            when (it) {
                is ChatState.Error ->
                    EmptyMessagesView(
                        text = "Saga não encontrada.",
                        brush =
                            gradientAnimation(
                                holographicGradient,
                            ),
                        modifier = Modifier.align(Alignment.Center),
                    )

                is ChatState.Success -> {
                    content?.let { cont ->
                        ChatContent(
                            state = state.value,
                            content = cont,
                            characters = characters,
                            messagesList = messages,
                            suggestions = suggestions,
                            isGenerating = isGenerating,
                            padding = padding,
                            isDebug = isDebug, // Pass isDebug to ChatContent
                            onSendMessage = viewModel::sendInput,
                            onCreateCharacter = viewModel::createCharacter,
                            onBack = navHostController::popBackStack,
                            openSagaDetails = {
                                navHostController.navigateToRoute(
                                    Routes.SAGA_DETAIL,
                                    mapOf("sagaId" to cont.data.id.toString()),
                                )
                            },
                            // Pass the fake message injector function
                            onInjectFakeMessages = { count ->
                                viewModel.sendFakeUserMessages(count)
                            },
                        )
                    }
                }

                else ->
                    Box(Modifier.fillMaxSize()) {
                        SparkIcon(
                            brush = gradientAnimation(genresGradient()),
                            modifier =
                                Modifier
                                    .size(50.dp)
                                    .align(Alignment.Center),
                            duration = 2.seconds,
                            blurRadius = 3.dp,
                            tint = MaterialTheme.colorScheme.background,
                        )
                    }
            }
        }
        AnimatedVisibility(
            snackBarMessage != null,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = scaleIn() + fadeIn(),
            exit = fadeOut() + scaleOut(),
        ) {
            snackBarMessage?.let { snackBar ->
                var isExpanded by remember {
                    mutableStateOf(false)
                }

                val brush =
                    content?.data?.genre?.gradient() ?: Brush.verticalGradient(holographicGradient)
                val shape = RoundedCornerShape(content?.data?.genre?.cornerSize() ?: 25.dp)
                Column(
                    Modifier
                        .padding(vertical = 75.dp, horizontal = 16.dp)
                        .clip(
                            shape,
                        ).border(1.dp, brush, shape)
                        .hazeEffect(state = contentHaze, style = HazeMaterials.thin())
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable {
                            isExpanded = isExpanded.not()
                        }.animateContentSize(
                            animationSpec = tween(200, easing = LinearOutSlowInEasing),
                        ),
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_spark),
                            null,
                            modifier = Modifier.size(12.dp),
                            tint =
                                content?.data?.genre?.color
                                    ?: MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            snackBar.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontFamily = content?.data?.genre?.bodyFont(),
                            textAlign = TextAlign.Start,
                            modifier =
                                Modifier
                                    .padding(8.dp)
                                    .weight(1f),
                        )
                    }

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Text(
                            snackBar.text,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = content?.data?.genre?.bodyFont(),
                            textAlign = TextAlign.Start,
                            modifier =
                                Modifier
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                        )
                    }

                    AnimatedVisibility(isExpanded) {
                        if (snackBar.redirectAction != null) {
                            Button(
                                onClick = {
                                    when (snackBar.redirectAction.first) {
                                        ChatAction.RESEND -> viewModel.dismissSnackBar()
                                        else -> {
                                            content?.data?.let { saga ->
                                                navHostController.navigateToRoute(
                                                    Routes.SAGA_DETAIL,
                                                    mapOf("sagaId" to saga.id.toString()),
                                                )
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.textButtonColors(),
                                modifier =
                                    Modifier
                                        .padding(8.dp)
                                        .fillMaxWidth(),
                            ) {
                                Text(
                                    snackBar.redirectAction.second,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = content?.data?.genre?.bodyFont(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatContent(
    state: ChatState = ChatState.Loading,
    content: SagaContent,
    characters: List<Character> = emptyList(),
    messagesList: List<MessageContent> = emptyList(),
    suggestions: List<Suggestion> = emptyList(),
    isGenerating: Boolean = false,
    padding: PaddingValues = PaddingValues(),
    isDebug: Boolean = false,
    onSendMessage: (String, SenderType) -> Unit = { _, _ -> },
    onCreateCharacter: (CharacterInfo) -> Unit = {},
    onBack: () -> Unit = {},
    openSagaDetails: (SagaData) -> Unit = {},
    onInjectFakeMessages: (Int) -> Unit = {},
) {
    val saga = content.data
    val listState = rememberLazyListState()
    val hazeState = rememberHazeState()

    LaunchedEffect(messagesList.size) {
        if (messagesList.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    val bottomSheetState = rememberModalBottomSheetState()
    var showCharacter by remember {
        mutableStateOf<Character?>(null)
    }

    Box {
        ConditionalImage(
            saga.genre.background,
            saga.genre.gradient(),
            customBlendMode = null,
            Modifier
                .fillMaxSize()
                .alpha(.5f),
        )

        ConstraintLayout(
            Modifier
                .padding(top = padding.calculateTopPadding()) // Keep overall padding
                .fillMaxSize(),
        ) {
            // Order of refs matters for layout, debug UI will be on top
            val (debugControls, messages, chatInput, topBar, bottomFade) = createRefs()
            val hazeList = rememberHazeState()

            ChatList(
                saga = content,
                messagesList = messagesList,
                listState = listState,
                hazeState = hazeState,
                modifier =
                    Modifier
                        .hazeSource(hazeList)
                        .constrainAs(messages) {
                            top.linkTo(topBar.bottom)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            width = Dimension.fillToConstraints
                            height = Dimension.fillToConstraints
                        },
                openCharacter = {
                    showCharacter = it
                },
                openSaga = { openSagaDetails(saga) },
            )

            AnimatedVisibility(
                state !is ChatState.Loading && saga.isDebug.not(),
                modifier =
                    Modifier
                        .constrainAs(chatInput) {
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            width = Dimension.fillToConstraints
                        }.animateContentSize(),
                enter = slideInVertically(),
                exit = fadeOut(),
            ) {
                if (saga.isEnded) {
                    Text(
                        "Sua saga chegou ao fim em ${saga.endedAt.formatDate()}",
                        style =
                            MaterialTheme.typography.labelLarge.copy(
                                brush = saga.genre.gradient(true, targetValue = 200f),
                                fontStyle = FontStyle.Italic,
                                fontFamily = saga.genre.bodyFont(),
                                textAlign = TextAlign.Center,
                            ),
                        modifier = Modifier.padding(16.dp),
                    )
                } else {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        val isImeVisible = WindowInsets.isImeVisible
                        AnimatedVisibility(suggestions.isNotEmpty() && isImeVisible) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Bottom,
                            ) {
                                items(suggestions) {
                                    Row(
                                        modifier =
                                            Modifier
                                                .widthIn(max = 200.dp)
                                                .padding(8.dp)
                                                .clip(RoundedCornerShape(saga.genre.cornerSize()))
                                                .border(
                                                    1.dp,
                                                    saga.genre.color.copy(alpha = .3f),
                                                    RoundedCornerShape(saga.genre.cornerSize()),
                                                ).hazeEffect(hazeList, HazeMaterials.regular())
                                                .padding(4.dp)
                                                .animateContentSize()
                                                .clickable {
                                                    onSendMessage(it.text, it.type)
                                                },
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        it.type.icon()?.let { icon ->
                                            Icon(
                                                painterResource(icon),
                                                null,
                                                tint = saga.genre.color,
                                                modifier =
                                                    Modifier
                                                        .padding(end = 4.dp)
                                                        .size(12.dp),
                                            )
                                        }

                                        Text(
                                            it.text,
                                            style =
                                                MaterialTheme.typography.labelMedium.copy(
                                                    fontFamily = saga.genre.bodyFont(),
                                                    color = saga.genre.color,
                                                    textAlign = TextAlign.Center,
                                                ),
                                        )
                                    }
                                }
                            }
                        }

                        ChatInputView(
                            content = content,
                            isGenerating = isGenerating,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                            onSendMessage = onSendMessage,
                            onCreateNewCharacter = onCreateCharacter,
                        )
                    }
                }
            }

            val alpha by animateFloatAsState(
                if (listState.canScrollForward.not()) 0f else 1f,
                animationSpec = tween(450, easing = EaseIn),
            )
            SagaTopBar(
                saga.title,
                "${messagesList.size} mensagens",
                saga.genre,
                onBackClick = onBack,
                modifier =
                    Modifier
                        .graphicsLayer(alpha = alpha)
                        .constrainAs(topBar) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }.background(MaterialTheme.colorScheme.background)
                        .padding(top = 50.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                        .clickable {
                            openSagaDetails(saga)
                        },
                actionContent = {
                    AnimatedContent(characters, transitionSpec = {
                        slideInVertically() + fadeIn() with fadeOut()
                    }) { chars ->
                        CharactersTopIcons(chars, { openSagaDetails(saga) }, saga)
                    }
                },
            )

            if (isDebug && saga.isEnded.not()) {
                var fakeMessageCountText by rememberSaveable { mutableStateOf("3") }
                val blurRadius by animateFloatAsState(
                    if (isGenerating) 40f else 0f,
                )
                val shape = RoundedCornerShape(content.data.genre.cornerSize())
                BlurredGlowContainer(
                    Modifier
                        .padding(16.dp)
                        .constrainAs(debugControls) {
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            width = Dimension.fillToConstraints
                            height = Dimension.wrapContent
                        },
                    content.data.genre.gradient(isGenerating),
                    shape = shape,
                    blurSigma = blurRadius,
                ) {
                    val textStyle =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = content.data.genre.bodyFont(),
                        )
                    TextField(
                        value = fakeMessageCountText,
                        textStyle = textStyle,
                        onValueChange = { fakeMessageCountText = it },
                        label = {
                            Text(
                                "Debug controls",
                                style = textStyle,
                                modifier = Modifier.scale(.9f),
                            )
                        },
                        placeholder = {
                            Text(
                                "Number of Fake Messages",
                                style = textStyle,
                                modifier = Modifier.alpha(.7f),
                            )
                        },
                        shape = shape,
                        modifier =
                            Modifier
                                .padding(2.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainer,
                                    shape,
                                ).fillMaxWidth(),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    val count = fakeMessageCountText.toIntOrNull() ?: 0
                                    if (count > 0) {
                                        onInjectFakeMessages(count)
                                    }
                                },
                                modifier =
                                    Modifier
                                        .background(
                                            content.data.genre.color,
                                            CircleShape,
                                        ).size(32.dp)
                                        .padding(4.dp),
                            ) {
                                Icon(
                                    painterResource(R.drawable.ic_inject),
                                    null,
                                    tint = content.data.genre.iconColor,
                                )
                            }
                        },
                        colors =
                            TextFieldDefaults.colors().copy(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                            ),
                    )
                }
            }
        }
    }

    showCharacter?.let { character ->
        ModalBottomSheet(
            onDismissRequest = { showCharacter = null },
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.background,
            dragHandle = {
                Box {}
            },
        ) {
            CharacterDetailsContent(
                content,
                character,
                messagesList.count { it.character?.id == character.id || it.message.speakerName == character.name },
            )
        }
    }
}

@Composable
private fun EmptyMessagesView(
    text: String = "Comece a escrever sua saga!",
    brush: Brush,
    modifier: Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.Center) {
        SparkIcon(
            modifier =
                Modifier
                    .size(200.dp)
                    .align(Alignment.CenterHorizontally),
            description = "No messages",
            brush = brush,
        )

        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
        )
    }
}

@Composable
fun SagaHeader(
    saga: SagaData,
    isEmpty: Boolean,
) {
    var size by remember {
        mutableStateOf(if (isEmpty) 500.dp else 400.dp)
    }
    val imageSize by animateDpAsState(
        targetValue = size,
        animationSpec = tween(200, easing = EaseIn),
    )
    Box(
        modifier =
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
                .height(imageSize),
    ) {
        val iconUrl = saga.icon ?: saga.genre.defaultHeaderImage()
        AsyncImage(
            iconUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            onError = {
                size = 0.dp
            },
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .effectForGenre(saga.genre)
                    .selectiveColorHighlight(
                        saga.genre.selectiveHighlight(),
                    ).fillMaxSize(),
        )

        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxSize()
                .background(
                    fadeGradientBottom(),
                ),
        )
    }
}

@Composable
fun ChatList(
    saga: SagaContent,
    messagesList: List<MessageContent>,
    modifier: Modifier,
    listState: LazyListState,
    hazeState: HazeState,
    openCharacter: (Character?) -> Unit = {},
    openSaga: () -> Unit = {},
) {
    val animatedMessages = remember { mutableSetOf<Int>() }
    LazyColumn(modifier, state = listState, reverseLayout = messagesList.isNotEmpty()) {
        // Changed saga.messages to messagesList
        saga.let {
            item {
                Spacer(
                    Modifier
                        .fillMaxWidth()
                        .height(75.dp),
                )
            }

            items(messagesList, key = { it.message.id }) { message ->

                AnimatedVisibility(
                    visible = true,
                    enter =
                        fadeIn(tween(500, delayMillis = 100)) +
                            slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = tween(500, easing = LinearOutSlowInEasing),
                            ),
                ) {
                    ChatBubble(
                        message,
                        content = saga,
                        hazeState = hazeState,
                        animatedMessages,
                        canAnimate = message == messagesList.lastOrNull(), // Changed saga.messages to messagesList
                        openCharacters = { openCharacter(it) },
                    )
                }
            }

            item {
                var isDescriptionExpanded by remember { mutableStateOf(false) }
                val textColor by animateColorAsState(
                    targetValue =
                        if (isDescriptionExpanded) {
                            MaterialTheme.colorScheme.onBackground
                        } else {
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        },
                )
                Text(
                    if (isDescriptionExpanded) {
                        saga.data.description
                    } else {
                        saga.data.description
                            .take(200)
                            .plus("...")
                    },
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            color = textColor,
                        ),
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth()
                            .clickable {
                                isDescriptionExpanded = !isDescriptionExpanded
                            }.animateContentSize(),
                )
            }

            item {
                Text(
                    saga.data.title,
                    style =
                        MaterialTheme.typography.displayMedium.copy(
                            fontFamily = saga.data.genre.headerFont(),
                        ),
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .background(fadeGradientTop())
                            .fillMaxWidth()
                            .padding(16.dp)
                            .gradientFill(saga.data.genre.gradient())
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                openSaga()
                            },
                )
            }

            item {
                SagaHeader(
                    saga.data,
                    messagesList.isEmpty(),
                ) // Changed saga.messages to messagesList
            }
        }
    }
}

@Composable
private fun CharactersTopIcons(
    characters: List<Character>,
    onCharacterSelected: (Int) -> Unit,
    data: SagaData,
) {
    val overlapAmount = (-10).dp
    val density = LocalDensity.current
    val charactersToDisplay =
        characters.take(3)
    LazyRow(
        Modifier
            .clip(RoundedCornerShape(25.dp))
            .fillMaxWidth(.2f)
            .clickable {
                onCharacterSelected(data.id)
            },
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        itemsIndexed(charactersToDisplay) { index, character ->
            val overlapAmountPx = with(density) { overlapAmount.toPx() }
            CharacterAvatar(
                character,
                borderSize = 2.dp,
                borderColor = MaterialTheme.colorScheme.background,
                innerPadding = 0.dp,
                genre = data.genre,
                modifier =
                    Modifier
                        .zIndex(
                            if (index ==
                                0
                            ) {
                                charactersToDisplay.size.toFloat()
                            } else {
                                (charactersToDisplay.size - 1 - index).toFloat()
                            },
                        ).graphicsLayer(
                            translationX = if (index > 0) (index * overlapAmountPx) else 0f,
                        ).size(24.dp),
            )
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_TYPE_NORMAL,
)
@Composable
fun ChatViewPreview() {
    val saga =
        SagaData(
            id = 1,
            title = "Byte Legend",
            description = "This is a sample saga for preview purposes.",
            icon = "",
            genre = Genre.FANTASY,
            createdAt = Calendar.getInstance().timeInMillis,
            mainCharacterId = null,
            visuals = IllustrationVisuals(),
        )
    val messages =
        List(17) {
            Message(
                id = it,
                text = "This is a sample message number $it.",
                senderType = if (it % 2 == 0) SenderType.CHARACTER else SenderType.USER,
                timestamp = Calendar.getInstance().timeInMillis - it * 1000L,
                sagaId = saga.id,
            )
        }.plus(
            Message(
                id = 20,
                text = "This is a sample message from the narrator.",
                senderType = SenderType.NARRATOR,
                timestamp = Calendar.getInstance().timeInMillis - 11 * 1000L,
                sagaId = saga.id,
            ),
        ).reversed()
    val successState = ChatState.Empty
    SagAIScaffold {
        ChatContent(
            state = successState,
            content =
                SagaContent(
                    saga,
                    characters = emptyList(),
                    wikis = emptyList(),
                    mainCharacter = null,
                ),
            suggestions =
                List(3) {
                    Suggestion(
                        "This is a sample suggestion number $it.",
                        SenderType.NARRATOR,
                    )
                },
            isDebug = true, // Enable debug UI for preview
            onInjectFakeMessages = {},
        )
    }
}
