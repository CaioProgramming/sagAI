@file:OptIn(ExperimentalMaterial3Api::class)

package com.ilustris.sagai.features.home.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.ilustris.sagai.R
import com.ilustris.sagai.core.utils.emptyString
import com.ilustris.sagai.core.utils.formatToString
import com.ilustris.sagai.features.home.data.model.DynamicSagaPrompt
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.flatMessages
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.colorPalette
import com.ilustris.sagai.features.newsaga.data.model.defaultHeaderImage
import com.ilustris.sagai.features.newsaga.data.model.selectiveHighlight
import com.ilustris.sagai.features.saga.chat.domain.model.Message
import com.ilustris.sagai.features.saga.chat.domain.model.MessageContent
import com.ilustris.sagai.features.saga.chat.domain.model.SenderType
import com.ilustris.sagai.features.saga.chat.domain.model.joinMessage
import com.ilustris.sagai.ui.animations.StarryTextPlaceholder
import com.ilustris.sagai.ui.navigation.Routes
import com.ilustris.sagai.ui.navigation.navigateToRoute
import com.ilustris.sagai.ui.theme.GradientType
import com.ilustris.sagai.ui.theme.SagAITheme
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.SparkIcon
import com.ilustris.sagai.ui.theme.components.SparkLoader
import com.ilustris.sagai.ui.theme.fadeColors
import com.ilustris.sagai.ui.theme.filters.selectiveColorHighlight
import com.ilustris.sagai.ui.theme.genresGradient
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.holographicGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import effectForGenre
import java.util.Calendar
import kotlin.time.Duration.Companion.seconds

@Suppress("ktlint:standard:function-naming")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeView(
    navController: NavHostController,
    padding: PaddingValues,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val sagas by viewModel.sagas.collectAsStateWithLifecycle(emptyList())
    val showDebugButton by viewModel.showDebugButton.collectAsStateWithLifecycle()
    val startFakeSaga by viewModel.startDebugSaga.collectAsStateWithLifecycle()
    val dynamicNewSagaTexts by viewModel.dynamicNewSagaTexts.collectAsStateWithLifecycle()
    val isLoadingDynamicPrompts by viewModel.isLoadingDynamicPrompts.collectAsStateWithLifecycle()
    ChatList(
        sagas = if (showDebugButton.not()) sagas.filter { !it.data.isDebug } else sagas,
        padding = padding,
        showDebugButton = showDebugButton,
        dynamicNewSagaTexts = dynamicNewSagaTexts,
        isLoadingDynamicPrompts = isLoadingDynamicPrompts,
        onCreateNewChat = {
            navController.navigateToRoute(Routes.NEW_SAGA)
        },
        onSelectSaga = { sagaData ->
            navController.navigateToRoute(
                Routes.CHAT,
                mapOf(
                    "sagaId" to sagaData.id.toString(),
                    "isDebug" to sagaData.isDebug.toString(),
                ),
            )
        },
        createFakeSaga = {
            viewModel.createFakeSaga()
        },
    )
    LaunchedEffect(startFakeSaga) {
        startFakeSaga?.let {
            navController.navigateToRoute(
                Routes.CHAT,
                mapOf(
                    "sagaId" to it.id.toString(),
                    "isDebug" to "true",
                ),
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ChatList(
    sagas: List<SagaContent>,
    padding: PaddingValues = PaddingValues(0.dp),
    showDebugButton: Boolean,
    dynamicNewSagaTexts: DynamicSagaPrompt?,
    isLoadingDynamicPrompts: Boolean,
    onCreateNewChat: () -> Unit = {},
    onSelectSaga: (Saga) -> Unit = {},
    createFakeSaga: () -> Unit = {},
) {
    LazyColumn(
        modifier =
            Modifier.padding(padding),
    ) {
        if (showDebugButton) { // Condition updated
            item {
                val debugBrush = Brush.verticalGradient(listOf(Color.DarkGray, Color.Gray))
                Row(
                    modifier =
                        Modifier
                            .clickable {
                                createFakeSaga()
                            }.padding(16.dp)
                            .gradientFill(debugBrush)
                            .clip(RoundedCornerShape(15.dp))
                            .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painterResource(R.drawable.ic_bug),
                        contentDescription = stringResource(R.string.home_debug_session_icon_desc),
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier =
                            Modifier
                                .padding(8.dp)
                                .size(32.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            stringResource(R.string.home_start_debug_session_title),
                            style =
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                ),
                        )

                        Text(
                            stringResource(R.string.home_test_with_fake_messages_subtitle),
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Light,
                                    color = Color.White.copy(alpha = 0.8f),
                                ),
                        )
                    }
                }
            }
        }
        item {

            Row(
                modifier =
                    Modifier
                        .padding(16.dp)
                        .reactiveShimmer(true)
                        .clip(RoundedCornerShape(15.dp))
                        .clickable {
                            onCreateNewChat()
                        }.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SparkLoader(
                    brush = Brush.verticalGradient(genresGradient(), tileMode = TileMode.Decal ),
                    strokeSize = 2.dp,
                    modifier =
                        Modifier
                            .clip(CircleShape)
                            .padding(4.dp)
                            .size(32.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                AnimatedContent(
                    targetState = isLoadingDynamicPrompts,
                    transitionSpec = {
                        (slideInVertically { height -> height } + fadeIn()).togetherWith(
                            slideOutVertically { height -> -height } + fadeOut(),
                        ) using
                            SizeTransform(
                                clip = false,
                            )
                    },
                    label = "AnimatedDynamicTexts",
                    modifier = Modifier.weight(1f),
                ) { isLoading ->
                    Column(
                        modifier =
                            Modifier
                                .weight(1f)
                                .gradientFill(Brush.verticalGradient(holographicGradient)),
                    ) {
                        if (isLoading) {
                            StarryTextPlaceholder(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(MaterialTheme.typography.bodyMedium.lineHeight.value.dp),
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            StarryTextPlaceholder(
                                modifier =
                                    Modifier
                                        .fillMaxWidth(0.7f)
                                        .height(MaterialTheme.typography.labelSmall.lineHeight.value.dp),
                            )
                        } else {
                            Text(
                                text =
                                    dynamicNewSagaTexts?.title
                                        ?: stringResource(R.string.home_create_new_saga_title),
                                style =
                                    MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                            )

                            Text(
                                text =
                                    dynamicNewSagaTexts?.subtitle
                                        ?: stringResource(R.string.home_create_new_saga_subtitle),
                                style =
                                    MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Light,
                                    ),
                            )
                        }
                    }
                }
            }
        }

        items(sagas) {
            ChatCard(it, isEnabled = showDebugButton) {
                onSelectSaga(it.data)
            }
        }
    }
}

@Composable
fun ChatCard(
    saga: SagaContent,
    isEnabled: Boolean = false,
    onClick: () -> Unit = {},
) {
    val sagaData = saga.data
    Column {
        Row(
            modifier =
                Modifier
                    .clickable {
                        if (!saga.data.isDebug || isEnabled) {
                            onClick()
                        }
                    }.fillMaxWidth()
                    .clip(RoundedCornerShape(15.dp))
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val imageLoaded =
                remember {
                    mutableStateOf(false)
                }
            Box(
                modifier =
                    Modifier
                        .size(64.dp)
                        .clip(CircleShape),
            ) {
                if (saga.data.isDebug.not()) {
                    AsyncImage(
                        sagaData.icon ?: sagaData.genre.defaultHeaderImage(),
                        contentDescription = sagaData.title,
                        contentScale = ContentScale.Crop,
                        modifier =
                            Modifier
                                .padding(8.dp)
                                .border(
                                    2.dp,
                                    sagaData.genre.color,
                                    CircleShape,
                                ).padding(4.dp)
                                .background(
                                    sagaData.genre.color,
                                    CircleShape,
                                ).clip(CircleShape)
                                .fillMaxSize()
                                .effectForGenre(
                                    sagaData.genre,
                                    pixelSize = 1.3f,
                                ).selectiveColorHighlight(
                                    sagaData.genre.selectiveHighlight(),
                                ),
                        onSuccess = {
                            imageLoaded.value = true
                        },
                    )
                } else {
                    Image(
                        painterResource(R.drawable.ic_bug),
                        contentDescription = null,
                        colorFilter =
                            ColorFilter.tint(
                                sagaData.genre.iconColor,
                            ),
                        contentScale = ContentScale.Fit,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .border(2.dp, sagaData.genre.gradient(), CircleShape)
                                .padding(4.dp),
                    )

                    LaunchedEffect(Unit) {
                        imageLoaded.value = true
                    }
                }

                this@Row.AnimatedVisibility(
                    imageLoaded.value.not(),
                    modifier = Modifier.align(Alignment.Center),
                ) {
                    Text(
                        sagaData.title
                            .first()
                            .uppercaseChar()
                            .toString(),
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = sagaData.genre.headerFont(),
                                color = sagaData.genre.iconColor,
                                textAlign = TextAlign.Center,
                            ),
                    )
                }

                if (saga.data.isEnded) {
                    Image(
                        painterResource(R.drawable.ic_spark),
                        contentDescription = null,
                        colorFilter =
                            ColorFilter.tint(
                                sagaData.genre.color,
                            ),
                        modifier =
                            Modifier.offset(y = 6.dp).size(24.dp).align(
                                Alignment.BottomCenter,
                            ),
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            val lastMessage = saga.flatMessages().lastOrNull()
            Column(
                modifier =
                    Modifier.weight(1f).reactiveShimmer(
                        sagaData.isEnded,
                        shimmerColors = sagaData.genre.color.fadeColors(),
                        targetValue = 1000f,
                        duration = 5.seconds,
                    ),
            ) {
                Row {
                    Text(
                        text = sagaData.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = saga.data.genre.headerFont(),
                        modifier = Modifier.weight(1f),
                    )

                    lastMessage?.let {
                        val time =
                            Calendar.getInstance().apply { timeInMillis = it.message.timestamp }
                        val timeText =
                            String.format(
                                "%02d:%02d",
                                time.get(Calendar.HOUR_OF_DAY),
                                time.get(Calendar.MINUTE),
                            )

                        Text(
                            text = timeText,
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = saga.data.genre.bodyFont(),
                                ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                val message =
                    if (sagaData.isEnded) {
                        stringResource(R.string.chat_card_saga_ended)
                    } else {
                        if (saga.messagesSize() == 0) {
                            stringResource(R.string.chat_card_saga_begins)
                        } else {
                            lastMessage?.joinMessage()?.formatToString(lastMessage.message.senderType != SenderType.NARRATOR)
                        }
                    }
                Text(
                    text = message ?: emptyString(),
                    style =
                        MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Normal,
                            fontFamily = saga.data.genre.bodyFont(),
                            textAlign = TextAlign.Start,
                        ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth().alpha(.8f),
                )
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = .1f)),
        )
    }
}

@Composable
private fun NewChatCard(
    modifier: Modifier = Modifier,
    animatedBrush: Brush,
    onButtonClick: () -> Unit = {},
) {
    Box(modifier.padding(16.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SparkIcon(
                brush = animatedBrush,
                duration = 3.seconds,
                rotationTarget = 180f,
                modifier =
                    Modifier
                        .clip(CircleShape)
                        .padding(8.dp)
                        .size(200.dp)
                        .clickable {
                            onButtonClick()
                        },
            )

            Text(
                stringResource(R.string.home_new_chat_journey_begins_title),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                style =
                    MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        brush = animatedBrush,
                    ),
            )

            Text(
                stringResource(R.string.home_create_new_saga_subtitle),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
            )

            Button(
                onClick = {
                    onButtonClick()
                },
                modifier =
                    Modifier.fillMaxWidth(),
                colors =
                    ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground,
                        contentColor = Color.White,
                    ),
                shape = RoundedCornerShape(15.dp),
            ) {
                Text(
                    stringResource(R.string.home_new_chat_start_button),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier =
                        Modifier
                            .padding(8.dp)
                            .fillMaxWidth(0.85f)
                            .gradientFill(
                                animatedBrush,
                            ),
                )

                Icon(
                    Icons.AutoMirrored.Default.ArrowForward,
                    contentDescription = stringResource(R.string.new_saga_title),
                    modifier =
                        Modifier
                            .padding(8.dp)
                            .size(24.dp)
                            .gradientFill(
                                animatedBrush,
                            ),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeViewPreview() {
    SagAITheme {
        val route = Routes.HOME
        Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
            TopAppBar(
                title = {
                    route.title?.let {
                        Text(
                            text = stringResource(it),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier =
                                Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                        )
                    } ?: run {
                        Box(Modifier.fillMaxWidth()) {
                            Image(
                                painterResource(R.drawable.ic_spark),
                                contentDescription = stringResource(R.string.app_name),
                                modifier =
                                    Modifier
                                        .align(Alignment.Center)
                                        .size(50.dp),
                            )
                        }
                    }
                },
                actions = {},
                navigationIcon = {
                    Box(modifier = Modifier.size(24.dp))
                },
            )
        }) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                val previewChats =
                    List(10) {
                        SagaContent(
                            Saga(
                                title = "Chat ${it + 1}",
                                description = "The journey of our lifes",
                                genre = Genre.FANTASY,
                                icon = "",
                                isEnded = true,
                                createdAt = Calendar.getInstance().timeInMillis,
                                mainCharacterId = null,
                            ),
                            mainCharacter = null,
                            acts = emptyList(),
                        )
                    }
                ChatList(
                    sagas = previewChats,
                    showDebugButton = true, // Example for preview
                    dynamicNewSagaTexts =
                        DynamicSagaPrompt(
                            "Dynamic Title Preview",
                            "Dynamic Subtitle Preview",
                        ),
                    // Example for preview
                    isLoadingDynamicPrompts = false, // Example for preview
                )
            }
        }
    }
}
