@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class,
    ExperimentalHazeMaterialsApi::class,
)

package com.ilustris.sagai.features.saga.chat.ui

import ai.atick.material.MaterialColor
import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
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
import com.ilustris.sagai.features.saga.chat.ui.components.itemOption
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.navigation.navigateToRoute
import com.ilustris.sagai.ui.theme.GradientType
import com.ilustris.sagai.ui.theme.SagAIScaffold
import com.ilustris.sagai.ui.theme.components.SagaTopBar
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.defaultHeaderImage
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.fadedGradientTopAndBottom
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.holographicGradient
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import com.skydoves.balloon.compose.Balloon
import com.skydoves.balloon.compose.BalloonWindow
import com.skydoves.balloon.compose.rememberBalloonBuilder
import com.skydoves.balloon.compose.setBackgroundColor
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatView(
    navHostController: NavHostController,
    padding: PaddingValues = PaddingValues(0.dp),
    sagaId: String? = null,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    val saga by viewModel.saga.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val mainCharacter by viewModel.mainCharacter.collectAsStateWithLifecycle()
    val characters by viewModel.characters.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    LaunchedEffect(saga) {
        if (saga == null) {
            viewModel.initChat(sagaId)
        }
    }
    ChatContent(
        state.value,
        saga,
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
                mapOf("sagaId" to it.toString()),
            )
        },
        openSagaDetails = {
            navHostController.navigateToRoute(
                Routes.SAGA_DETAIL,
                mapOf("sagaId" to it.id.toString()),
            )
        },
    )
}

@Composable
fun ChatContent(
    state: ChatState = ChatState.Loading,
    saga: SagaData? = null,
    messagesList: List<MessageContent> = emptyList(),
    mainCharacter: Character?,
    characters: List<Character> = emptyList(),
    isGenerating: Boolean = false,
    padding: PaddingValues = PaddingValues(),
    onSendMessage: (String, SenderType) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
    onCharacterSelected: (Int) -> Unit = {},
    openSagaDetails: (SagaData) -> Unit = {},
) {
    var input by remember {
        mutableStateOf("")
    }
    var sendAction by remember {
        mutableStateOf(SenderType.USER)
    }
    val hazeState = rememberHazeState(blurEnabled = true)
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .hazeSource(state = hazeState),
    ) {
        saga?.let {
            SagaTopBar(
                it.title,
                "${messagesList.size} mensagens",
                it.genre,
                onBackClick = onBack,
                modifier =
                    Modifier
                        .padding(top = 50.dp, start = 16.dp)
                        .fillMaxWidth()
                        .clickable {
                            openSagaDetails(it)
                        },
                actionContent = {
                    val overlapAmount = (-10).dp
                    val density = LocalDensity.current
                    val charactersToDisplay =
                        characters.take(3)
                    LazyRow(
                        Modifier
                            .fillMaxWidth(.15f)
                            .clickable {
                                onCharacterSelected(it.id)
                            },
                        userScrollEnabled = false,
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Get the list of characters to display
                        itemsIndexed(charactersToDisplay) { index, character ->
                            val overlapAmountPx = with(density) { overlapAmount.toPx() }
                            CharacterAvatar(
                                character,
                                borderSize = 2.dp,
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
                },
            )
        }

        ConstraintLayout(
            Modifier
                .fillMaxSize(),
        ) {
            val brush =
                saga?.genre?.gradient()?.let { gradientAnimation(it, targetValue = 500f) }
                    ?: gradientAnimation()
            val (messages, chatInput, inputBottomFade) = createRefs()

            AnimatedContent(
                state,
                Modifier.constrainAs(messages) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                },
            ) {
                when (it) {
                    is ChatState.Success -> {
                        ChatList(
                            saga = saga,
                            messages = messagesList,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    is ChatState.Error ->
                        EmptyMessagesView(
                            text = it.message,
                            brush = brush,
                            modifier =
                                Modifier
                                    .fillMaxSize(),
                        )

                    else ->
                        Box(Modifier.fillMaxSize()) {
                            SparkIcon(
                                brush = brush,
                                tint = saga?.genre?.color ?: MaterialTheme.colorScheme.background,
                                modifier =
                                    Modifier
                                        .align(Alignment.Center)
                                        .size(150.dp),
                            )
                        }
                }
            }

            Box(
                Modifier
                    .constrainAs(inputBottomFade) {
                        bottom.linkTo(chatInput.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.matchParent
                    }.fillMaxHeight(.2f)
                    .background(
                        fadeGradientBottom(
                            tintColor = saga?.genre?.color ?: MaterialTheme.colorScheme.background,
                        ),
                    ),
            )
            AnimatedContent(
                isGenerating,
                transitionSpec = {
                    fadeIn() with fadeOut()
                },
                modifier =
                    Modifier.constrainAs(chatInput) {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    },
            ) {
                if (it) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        SparkIcon(
                            brush =
                                gradientAnimation(
                                    holographicGradient,
                                    gradientType = GradientType.VERTICAL,
                                ),
                            tint = MaterialColor.Gray50.copy(alpha = .5f),
                            modifier =
                                Modifier
                                    .align(Alignment.Center)
                                    .size(50.dp),
                        )
                    }
                } else {
                    val balloonBackground = Color.Transparent
                    var balloonWindow: BalloonWindow? by remember { mutableStateOf(null) }
                    val builder =
                        rememberBalloonBuilder {
                            setArrowSize(10)
                            setArrowPosition(0.5f)
                            setArrowPositionRules(ArrowPositionRules.ALIGN_BALLOON)
                            setWidth(BalloonSizeSpec.WRAP)
                            setHeight(BalloonSizeSpec.WRAP)
                            setPadding(12)
                            setMarginHorizontal(12)
                            setCornerRadius(8f)
                            setBackgroundColor(balloonBackground)
                            setBalloonAnimation(BalloonAnimation.OVERSHOOT)
                        }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier =
                            Modifier
                                .padding(16.dp)
                                .hazeEffect(state = hazeState, style = HazeMaterials.ultraThin())
                                .fillMaxWidth(),
                    ) {
                        this@Column.AnimatedVisibility(
                            mainCharacter != null,
                            enter = scaleIn(),
                            exit = scaleOut(),
                        ) {
                            mainCharacter?.let { character ->
                                Balloon(
                                    builder = builder,
                                    onBalloonWindowInitialized = { balloonWindow = it },
                                    balloonContent = {
                                        Column {
                                            SenderType.entries
                                                .filter {
                                                    it != SenderType.CHARACTER &&
                                                        it != SenderType.NEW_CHAPTER &&
                                                        it != SenderType.NEW_CHARACTER
                                                }.forEach { type ->
                                                    type.itemOption(
                                                        sendAction,
                                                        selectedColor =
                                                            saga?.genre?.color
                                                                ?: MaterialTheme.colorScheme.primary,
                                                    ) { action ->
                                                        sendAction = action
                                                        balloonWindow?.dismissWithDelay(1000)
                                                    }
                                                }
                                        }
                                    },
                                ) {
                                    CharacterAvatar(
                                        character,
                                        borderSize = 2.dp,
                                        modifier =
                                            Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    balloonWindow?.showAsDropDown()
                                                },
                                    )
                                }
                            }
                        }
                        val inputStyle =
                            HazeStyle(
                                backgroundColor = MaterialTheme.colorScheme.background.copy(alpha = .5f),
                                blurRadius = 25.dp,
                                noiseFactor = HazeDefaults.noiseFactor,
                                tint = HazeTint.Unspecified,
                            )
                        TextField(
                            value = input,
                            onValueChange = {
                                if (it.length <= 200) {
                                    input = it
                                }
                            },
                            placeholder = {
                                Text(
                                    "Continua sua saga...",
                                    style =
                                        MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 12.sp,
                                        ),
                                )
                            },
                            shape = RoundedCornerShape(40.dp),
                            colors =
                                TextFieldDefaults.colors().copy(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor =
                                        saga?.genre?.color
                                            ?: MaterialTheme.colorScheme.primary,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                ),
                            textStyle =
                                MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                ),
                            maxLines = 3,
                            modifier =
                                Modifier
                                    .padding(horizontal = 16.dp)
                                    .height(48.dp)
                                    .weight(1f)
                                    .clip(RoundedCornerShape(40.dp))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.onSurface
                                            .copy(alpha = .4f)
                                            .gradientFade(),
                                        RoundedCornerShape(40.dp),
                                    ).background(MaterialTheme.colorScheme.background.copy(alpha = .8f))
                                    .hazeEffect(hazeState, inputStyle)
                                    .animateContentSize(),
                        )

                        this@Row.AnimatedVisibility(
                            input.isNotEmpty() && state is ChatState.Success,
                            enter = scaleIn(),
                            exit = scaleOut(),
                        ) {
                            val buttonColor =
                                saga?.genre?.color ?: MaterialTheme.colorScheme.primary
                            IconButton(
                                onClick = {
                                    onSendMessage(input, sendAction)
                                    input = ""
                                },
                                modifier =
                                    Modifier
                                        .size(32.dp)
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.onBackground.gradientFade(),
                                            CircleShape,
                                        ).background(
                                            buttonColor.copy(alpha = .4f),
                                            CircleShape,
                                        ),
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = "Send Message",
                                    modifier =
                                        Modifier.fillMaxSize(),
                                    tint = Color.White,
                                )
                            }
                        }
                    }
                }
            }
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
fun SagaHeader(saga: SagaData) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(300.dp),
    ) {
        val iconUrl = saga.icon ?: saga.genre.defaultHeaderImage()
        AsyncImage(
            iconUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(fadedGradientTopAndBottom()),
        )
    }
}

@Composable
fun ChatList(
    saga: SagaData?,
    messages: List<MessageContent>?,
    modifier: Modifier,
) {
    val animatedMessages = remember { mutableSetOf<Int>() }
    val listState = rememberLazyListState()

    LaunchedEffect(messages) {
        if (messages?.isNotEmpty() == true) {
            listState.scrollToItem(0)
        }
    }
    LazyColumn(modifier, state = listState, reverseLayout = true) {
        item {
            Spacer(Modifier.height(100.dp))
        }
        saga?.let {
            messages?.let {
                items(messages, key = { it.message.id }) { message ->
                    ChatBubble(
                        message,
                        saga.genre,
                        animatedMessages,
                        canAnimate = message != messages.first(),
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
            stickyHeader {
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
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(16.dp)
                            .gradientFill(
                                gradientAnimation(
                                    saga.genre.gradient(),
                                    targetValue = 500f,
                                ),
                            ),
                )
            }

            item {
                SagaHeader(saga)
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
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
            messagesList =
                messages.map {
                    MessageContent(
                        message = it,
                    )
                },
            null,
        )
    }
}
