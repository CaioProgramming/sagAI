package com.ilustris.sagai.features.share.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.R
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.features.share.presentation.SharePlayViewModel
import com.ilustris.sagai.ui.theme.SagaTitle
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.components.chat.BubbleTailAlignment
import com.ilustris.sagai.ui.theme.darkerPalette
import com.ilustris.sagai.ui.theme.gradientFade
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.headerFont
import com.ilustris.sagai.ui.theme.hexToColor
import com.ilustris.sagai.ui.theme.shape
import com.ilustris.sagai.ui.theme.solidGradient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ConversationShareView(
    sagaContent: SagaContent,
    messages: List<MessageContent>,
    viewModel: SharePlayViewModel = hiltViewModel()
) {
    val graphicsLayer = rememberGraphicsLayer()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val filePath = viewModel.savedFilePath.collectAsStateWithLifecycle().value
    val genre = sagaContent.data.genre

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            delay(1000)
            val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
            viewModel.saveBitmap(bitmap, "conversation_share")
        }
    }

    LaunchedEffect(filePath) {
        viewModel.savedFilePath.value?.let {
            launchShareActivity(it, context)
        }
    }



    Box(
        Modifier
            .fillMaxSize()
            .padding(16.dp), contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier
                .clip(genre.shape())
                .drawWithContent {
                    graphicsLayer.record {
                        this@drawWithContent.drawContent()
                    }
                    drawLayer(graphicsLayer)
                }
                .border(1.dp, genre.color.gradientFade(), genre.shape())
                .background(
                    Brush.verticalGradient(
                        genre.color.darkerPalette(factor = .35f)
                    ), shape = genre.shape()
                )
                .padding(8.dp)

        ) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Image(
                        painterResource(R.drawable.ic_spark), null,
                        Modifier.size(32.dp), colorFilter = ColorFilter.tint(
                            genre.iconColor
                        )
                    )

                    Text(
                        text = sagaContent.data.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = sagaContent.data.genre.headerFont(),
                            fontWeight = FontWeight.Bold,
                        ),
                        color = genre.iconColor
                    )

                }
            }

            items(messages) { message ->
                val isFromUser = message.character?.id == sagaContent.mainCharacter?.data?.id

                Row(
                    horizontalArrangement = if (isFromUser) Arrangement.End else Arrangement.Start,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val shape = genre.bubble(
                        tailAlignment = if (isFromUser) BubbleTailAlignment.BottomRight else BubbleTailAlignment.BottomLeft
                    )
                    val backgroundColor = MaterialTheme.colorScheme.surfaceContainer
                    if (isFromUser) {
                        Text(
                            message.message.text,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = genre.bodyFont(),
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 50.dp, end = 8.dp)
                                .background(
                                    backgroundColor,
                                    shape
                                )
                                .padding(16.dp)
                        )

                        message.character?.let {
                            CharacterAvatar(
                                it,
                                genre = genre,
                                modifier = Modifier
                                    .size(24.dp)
                                    .offset(y = (16).dp),
                                borderColor = it.hexColor.hexToColor()
                            )
                        }
                    } else {
                        message.character?.let {
                            CharacterAvatar(
                                it,
                                genre = genre,
                                borderSize = 1.dp,
                                modifier = Modifier
                                    .size(24.dp)
                                    .offset(y = (16).dp),
                                borderColor = it.hexColor.hexToColor()
                            )
                        }

                        Text(
                            message.message.text,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = genre.bodyFont(),
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 50.dp, start = 8.dp)
                                .background(
                                    backgroundColor.copy(alpha = .3f),
                                    shape
                                )
                                .padding(16.dp)
                        )

                    }
                }
            }




            item {
                Box(Modifier.fillMaxWidth()) {
                    SagaTitle(
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                            .gradientFill(genre.iconColor.solidGradient())
                    )
                }
            }
        }

    }
}

