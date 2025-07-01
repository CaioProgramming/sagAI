package com.ilustris.sagai.features.chapter.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.chapter.data.model.Chapter
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.ui.theme.TypewriterText
import com.ilustris.sagai.ui.theme.bodyFont
import com.ilustris.sagai.ui.theme.fadeGradientBottom
import com.ilustris.sagai.ui.theme.fadeGradientTop
import com.ilustris.sagai.ui.theme.fadedGradientTopAndBottom
import com.ilustris.sagai.ui.theme.gradient
import com.ilustris.sagai.ui.theme.gradientAnimation
import com.ilustris.sagai.ui.theme.headerFont
import effectForGenre
import kotlin.time.Duration.Companion.seconds

@Composable
fun ChapterContentView(
    genre: Genre,
    content: Chapter,
    mainCharacter: Character? = null,
    characters: List<Character>,
    wiki: List<Wiki>,
    textColor: Color,
    fontStyle: FontStyle,
    isAnimated: Boolean,
    openCharacters: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            Modifier
                .background(fadeGradientBottom())
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .fillMaxWidth(.20f)
                    .height(1.dp)
                    .background(genre.color),
            )

            Text(
                text = content.title,
                modifier =
                    Modifier
                        .padding(16.dp)
                        .weight(1f),
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Normal,
                        fontFamily = genre.bodyFont(),
                        fontStyle = FontStyle.Italic,
                        color = textColor,
                        textAlign = TextAlign.Center,
                    ),
            )

            Box(
                Modifier
                    .fillMaxWidth(.20f)
                    .height(1.dp)
                    .background(genre.color),
            )
        }

        var imageSize by remember {
            mutableStateOf(
                300.dp,
            )
        }

        val sizeAnimation by animateDpAsState(
            targetValue = imageSize,
            label = "Image Size Animation",
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(sizeAnimation),
        ) {
            AsyncImage(
                model = content.coverImage,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onError = {
                    imageSize = 0.dp
                },
                modifier =
                    Modifier.fillMaxSize().effectForGenre(genre),
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .background(fadedGradientTopAndBottom()),
            )
        }

        Text(
            text = content.title,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(fadeGradientTop())
                    .padding(16.dp),
            style =
                MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Light,
                    letterSpacing = 5.sp,
                    fontFamily = genre.headerFont(),
                    fontStyle = fontStyle,
                    brush = genre.gradient(true),
                    textAlign = TextAlign.Center,
                ),
        )

        TypewriterText(
            text = content.overview,
            modifier = Modifier.padding(16.dp),
            duration = 3.seconds,
            easing = LinearEasing,
            isAnimated = isAnimated,
            genre = genre,
            mainCharacter = mainCharacter,
            characters = characters,
            wiki = wiki,
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Normal,
                    fontFamily = genre.bodyFont(),
                    fontStyle = fontStyle,
                    color = textColor,
                    textAlign = TextAlign.Center,
                ),
            onTextUpdate = {
            },
            onTextClick = openCharacters,
        )
    }
}
