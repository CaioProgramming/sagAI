package com.ilustris.sagai.features.share.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.saga.chat.data.model.MessageContent
import com.ilustris.sagai.features.saga.chat.ui.components.ChatBubble
import com.ilustris.sagai.features.share.presentation.SharePlayViewModel
import com.ilustris.sagai.ui.theme.SagaTitle
import com.ilustris.sagai.ui.theme.headerFont
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ConversationShareView(
    sagaContent: SagaContent,
    messages: List<MessageContent>,
    modifier: Modifier = Modifier,
    viewModel: SharePlayViewModel = hiltViewModel()
) {
    val graphicsLayer = rememberGraphicsLayer()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val filePath = viewModel.savedFilePath.collectAsStateWithLifecycle().value

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

    Column(
        modifier = modifier
            .background(sagaContent.data.genre.color)
            .padding(16.dp)
            .drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }
                drawLayer(graphicsLayer)
            }
    ) {
        Text(
            text = sagaContent.data.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = sagaContent.data.genre.headerFont(),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally),
            color = Color.White
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(messages) { message ->
                ChatBubble(
                    messageContent = message,
                    content = sagaContent,
                    canAnimate = false,
                    messageEffectsEnabled = false,
                    isSelectionMode = false
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        SagaTitle(
            textStyle = MaterialTheme.typography.labelMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

