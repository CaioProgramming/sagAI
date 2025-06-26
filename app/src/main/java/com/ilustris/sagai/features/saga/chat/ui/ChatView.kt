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
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
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
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.home.data.model.IllustrationVisuals
import com.ilustris.sagai.features.home.data.model.SagaData
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.Message
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.usecase.model.SenderType
import com.ilustris.sagai.features.saga.chat.presentation.ChatState
import com.ilustris.sagai.features.saga.chat.presentation.ChatViewModel
import com.ilustris.sagai.features.saga.chat.ui.components.ChatBubble
import com.ilustris.sagai.features.saga.chat.ui.components.ChatInputView
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.navigation.navigateToRoute
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.backgroundTintFade
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.brightness
import com.ilustris.sagai.ui.theme.components.SagaTopBar
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.defaultHeaderImage
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.fadeGradientTop
import com.ilustris.sagai.ui.theme.genresGradient
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.holographicGradient
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import effectForGenre
import java.util.Calendar
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatView(
    navHostController: NavHostController,
    padding: PaddingValues = PaddingValues(0.dp),
    snackbarState: SnackbarHostState,
    sagaId: String? = null,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    val content by viewModel.content.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val characters by viewModel.characters.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val mainCharacter = content?.mainCharacter
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(content) {
        if (content == null) {
            viewModel.initChat(sagaId)
        }
    }

    AnimatedContent(state.value, transitionSpec = {
        fadeIn(tween(200)) with fadeOut(tween(700))
    }) {
        Box(Modifier.fillMaxSize()) {
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
                            state.value,
                            cont.data,
                            messages,
                            mainCharacter,
                            characters,
                            isGenerating,
                            padding,
                            viewModel::sendInput,
                            navHostController::popBackStack,
                            onCharacterSelected = {
                                navHostController.navigateToRoute(
                                    Routes.CHARACTER_GALLERY,
                                    mapOf("sagaId" to cont.data.id.toString()),
                                )
                            },
                            openSagaDetails = {
                                navHostController.navigateToRoute(
                                    Routes.SAGA_DETAIL,
                                    mapOf("sagaId" to cont.data.id.toString()),
                                )
                            },
                        )
                    }
                }

                else ->
                    SparkIcon(
                        brush = gradientAnimation(genresGradient()),
                        modifier =
                            Modifier
                                .size(64.dp)
                                .align(Alignment.Center),
                        duration = 2.seconds,
                        blurRadius = 3.dp,
                        tint = MaterialTheme.colorScheme.background,
                    )
            }

            AnimatedVisibility(
                snackbarMessage != null,
                modifier = Modifier.align(Alignment.TopCenter),
                enter = scaleIn() + fadeIn(),
                exit = slideOutVertically(),
            ) {
                snackbarMessage?.let { message ->
                    val backgroundColor =
                        content?.data?.genre?.color ?: MaterialTheme.colorScheme.primary
                    val contentColor =
                        content?.data?.genre?.iconColor ?: MaterialTheme.colorScheme.onPrimary
                    val brush =
                        gradientAnimation(
                            backgroundColor.darkerPalette(factor = .4f),
                            targetValue = 500f,
                            duration = 2.seconds,
                        )
                    Row(
                        Modifier
                            .padding(32.dp)
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                brush,
                                RoundedCornerShape(25.dp),
                            ).background(
                                MaterialTheme.colorScheme.background,
                                RoundedCornerShape(25.dp),
                            ).gradientFill(brush),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Image(
                            painterResource(R.drawable.ic_spark),
                            null,
                            modifier =
                                Modifier
                                    .padding(8.dp)
                                    .size(12.dp),
                        )
                        Text(
                            message,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = content?.data?.genre?.bodyFont(),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f),
                        )
                        Image(
                            painterResource(R.drawable.ic_spark),
                            null,
                            modifier =
                                Modifier
                                    .padding(8.dp)
                                    .size(12.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatContent(
    state: ChatState = ChatState.Loading,
    saga: SagaData,
    messagesList: List<MessageContent> = emptyList(),
    mainCharacter: Character?,
    characters: List<Character>,
    isGenerating: Boolean = false,
    padding: PaddingValues = PaddingValues(),
    onSendMessage: (String, SenderType) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
    onCharacterSelected: (Int) -> Unit = {},
    openSagaDetails: (SagaData) -> Unit = {},
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messagesList.size) {
        listState.scrollToItem(0)
    }

    Box {
        val fadeTints = saga.genre.backgroundTintFade()
        Image(
            painterResource(saga.genre.background),
            null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
                .brightness(.2f.unaryMinus()),
        )

        Box(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .fillMaxHeight(.15f)
                .background(
                    fadeGradientTop(
                        fadeTints.first,
                    ),
                ),
        )

        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(.3f)
                .background(
                    fadeGradientBottom(
                        fadeTints.second,
                    ),
                ),
        )

        ConstraintLayout(
            Modifier
                .padding(top = padding.calculateTopPadding())
                .fillMaxSize(),
        ) {
            val brush = gradientAnimation(saga.genre.gradient(), targetValue = 500f)
            val (messages, chatInput, topBar) = createRefs()

            ChatList(
                saga = saga,
                messages = messagesList,
                characters = characters,
                isGenerating = isGenerating,
                listState = listState,
                modifier =
                    Modifier.constrainAs(messages) {
                        top.linkTo(parent.top)
                        bottom.linkTo(chatInput.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                        height = Dimension.fillToConstraints
                    },
                openCharacter = { onCharacterSelected(saga.id) },
                openSaga = { openSagaDetails(saga) },
            )

            AnimatedVisibility(
                state !is ChatState.Loading,
                modifier =
                    Modifier
                        .constrainAs(chatInput) {
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            width = Dimension.fillToConstraints
                        }.padding(bottom = 16.dp, top = 2.dp),
                enter = slideInVertically(),
                exit = fadeOut(),
            ) {
                ChatInputView(
                    mainCharacter,
                    characters,
                    saga,
                    state,
                    isGenerating,
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    onSendMessage,
                )
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
                        CharactersTopIcons(chars, onCharacterSelected, saga)
                    }
                },
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
        mutableStateOf(if (isEmpty) 500.dp else 300.dp)
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
                    .fillMaxSize(),
        )

        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(.3f)
                .background(fadeGradientBottom()),
        )
    }
}

@Composable
fun ChatList(
    saga: SagaData?,
    messages: List<MessageContent>,
    characters: List<Character>,
    modifier: Modifier,
    listState: LazyListState,
    isGenerating: Boolean = false,
    openCharacter: () -> Unit = {},
    openSaga: () -> Unit = {},
) {
    val animatedMessages = remember { mutableSetOf<Int>() }

    LazyColumn(modifier.padding(bottom = 2.dp), state = listState, reverseLayout = true) {
        saga?.let {
            items(messages.reversed(), key = { it.message.id }) { message ->
                ChatBubble(
                    message,
                    saga.genre,
                    characters = characters,
                    animatedMessages,
                    canAnimate = message == messages.last(),
                    openCharacters = openCharacter,
                )
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
                        saga.description
                    } else {
                        saga.description
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
                    saga.title,
                    style =
                        MaterialTheme.typography.displayMedium.copy(
                            fontFamily = saga.genre.headerFont(),
                        ),
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .background(fadeGradientTop())
                            .fillMaxWidth()
                            .padding(16.dp)
                            .gradientFill(
                                gradientAnimation(
                                    saga.genre.gradient(),
                                    targetValue = 500f,
                                ),
                            ).clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                openSaga()
                            },
                )
            }

            item {
                SagaHeader(saga, messages.isEmpty())
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
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
)
@Composable
fun ChatViewPreview() {
    val saga =
        SagaData(
            id = 1,
            title = "Byte Legend",
            description = "This is a sample saga for preview purposes.",
            icon = "",
            genre = Genre.SCI_FI,
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
    val successState = ChatState.Success
    SagAIScaffold {
        ChatContent(
            successState,
            saga = saga,
            characters = emptyList(),
            messagesList =
                messages.map {
                    MessageContent(
                        message = it,
                    )
                },
            mainCharacter = null,
        )
    }
}
