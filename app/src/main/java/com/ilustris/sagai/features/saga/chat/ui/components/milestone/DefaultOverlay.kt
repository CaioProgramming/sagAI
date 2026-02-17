package com.ilustris.sagai.features.saga.chat.ui.components.milestone

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.newsaga.data.model.resolveColor
import com.ilustris.sagai.features.newsaga.data.model.resolveIconColor
import com.ilustris.sagai.features.saga.chat.ui.components.bubble
import com.ilustris.sagai.ui.theme.bodyFont
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

@Composable
fun DefaultOverlay(
    title: String,
    subtitle: String,
    message: String?,
    genre: Genre,
    sparkModifier: Modifier,
    extraContent: @Composable () -> Unit,
    onDismiss: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var showIcon by remember { mutableStateOf(false) }
    var showTitle by remember { mutableStateOf(false) }
    var showSubtitle by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }

    fun animationSequence() =
        coroutineScope.launch {
            showIcon = true
            delay(2.seconds)
            showTitle = true
            delay(2.seconds)
            showSubtitle = true
            delay(2.seconds)
            showButton = true
            delay(2.seconds)
        }

    LaunchedEffect(Unit) {
        animationSequence()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier =
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize(),
    ) {
        AnimatedVisibility(
            showIcon,
            enter = slideInVertically { -it } + scaleIn(),
            exit = fadeOut(),
        ) {
            Image(
                painterResource(R.drawable.ic_spark),
                null,
                sparkModifier.size(50.dp),
                colorFilter = ColorFilter.tint(genre.resolveColor()),
            )
        }

        AnimatedVisibility(
            showTitle,
            enter = slideInVertically { -it } + scaleIn(),
            exit = fadeOut(),
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                fontFamily = genre.bodyFont(),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp),
            )
        }

        AnimatedVisibility(
            showSubtitle,
            enter = slideInVertically { -it } + scaleIn(),
            exit = fadeOut(),
        ) {
            Text(
                subtitle,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                fontFamily = genre.bodyFont(),
                fontWeight = FontWeight.Bold,
            )
        }

        AnimatedVisibility(
            message != null,
            enter = slideInVertically { -it } + fadeIn(),
            exit = fadeOut(),
        ) {
            message?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = genre.bodyFont(),
                    textAlign = TextAlign.Center,
                )
            }
        }

        AnimatedVisibility(showSubtitle) {
            Box(Modifier.padding(16.dp)) {
                extraContent()
            }
        }

        AnimatedVisibility(showButton, enter = slideInVertically { -it }, exit = fadeOut()) {
            Button(
                onClick = {
                    onDismiss()
                },
                shape = genre.bubble(isNarrator = true),
                colors =
                    ButtonDefaults.elevatedButtonColors().copy(
                        containerColor = genre.resolveColor(),
                        contentColor = genre.resolveIconColor(),
                    ),
            ) {
                Text(stringResource(R.string.continue_button))
            }
        }
    }
}
