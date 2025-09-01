package com.ilustris.sagai.features.act.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.act.data.model.Act
import com.ilustris.sagai.features.home.data.model.Saga
import com.ilustris.sagai.features.home.data.model.SagaContent
import com.ilustris.sagai.features.home.data.model.getCharacters
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.components.EmotionalCard
import com.ilustris.sagai.ui.theme.TypewriterText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.headerFont
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

private val DEFAULT_DELAY = 1.seconds

@Composable
fun ActComponent(
    act: Act,
    actCount: Int,
    content: SagaContent,
    modifier: Modifier = Modifier,
) {
    var titleVisible by remember { mutableStateOf(false) }
    var countVisible by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        titleVisible = true
    }

    LaunchedEffect(titleVisible) {
        if (titleVisible) {
            kotlinx.coroutines.delay(DEFAULT_DELAY)
            countVisible = true
        }
    }

    LaunchedEffect(countVisible) {
        if (countVisible) {
            kotlinx.coroutines.delay(DEFAULT_DELAY)
            contentVisible = true
        }
    }

    Column(
        modifier =
            modifier
                .padding(16.dp)
                .animateContentSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val countAnimation by animateFloatAsState(
            targetValue = if (countVisible) 1f else 0f,
            animationSpec = tween(1.seconds.toInt(DurationUnit.MILLISECONDS)),
            label = "Count Animation",
        )
        Text(
            actCount.toRoman(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = content.data.genre.headerFont(),
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.alpha(countAnimation),
        )

        AnimatedVisibility(
            visible = titleVisible,
            enter = fadeIn(animationSpec = tween(durationMillis = 500)),
            exit = fadeOut(animationSpec = tween(durationMillis = 500)),
        ) {
            Text(
                act.title.ifEmpty { "Ato em curso..." },
                style =
                    MaterialTheme.typography.displaySmall.copy(
                        fontFamily = content.data.genre.headerFont(),
                        brush = content.data.genre.gradient(true),
                    ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }

        if (contentVisible) {
            TypewriterText(
                act.content,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = content.data.genre.bodyFont(),
                        textAlign = TextAlign.Justify,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                duration = 10.seconds,
                isAnimated = true,
                genre = content.data.genre,
                mainCharacter = content.mainCharacter?.data,
                characters = content.getCharacters(),
                wiki = content.wikis,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            if (act.emotionalReview?.isNotEmpty() == true) {
                EmotionalCard(act.emotionalReview, content.data.genre, false)
            }
        }
    }
}

@Preview
@Composable
fun ActComponentPreview() {
    val act =
        Act(title = "The First Act", content = "This is the content of the first act.", sagaId = 1)
    val actCount = 1
    val saga =
        Saga(title = "My Saga", description = "This is a great saga.", genre = Genre.FANTASY)
    val content = SagaContent(data = saga)
    ActComponent(act = act, actCount = actCount, content = content)
}

fun Int.toRoman(): String {
    val values = listOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
    val symbols = listOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")
    var num = this
    val result = StringBuilder()

    for (i in values.indices) {
        while (num >= values[i]) {
            num -= values[i]
            result.append(symbols[i])
        }
    }
    return result.toString()
}
