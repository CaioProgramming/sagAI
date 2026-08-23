package com.ilustris.sagai.ui.genre.surface

import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.theme.LocalSagaGenre
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shimmerize
import com.ilustris.sagai.ui.theme.themeBrushColors
import com.ilustris.sagai.ui.theme.themePainter
import com.ilustris.sagai.ui.theme.themeVfx
import kotlin.time.Duration.Companion.seconds

/**
 * The two states a beat can be in besides being told: still arriving, and having failed to.
 *
 * They are separate entry points rather than [StoryBeatTone] variants because their genre idioms
 * have nothing to do with a title and a body — a blinking prompt, a typing indicator, a half-torn
 * strip — and forcing them through [StoryBeat] would mean every surface carrying a branch that
 * ignores most of the model.
 */
@Composable
fun GenreStoryLoading(
    message: String,
    modifier: Modifier = Modifier,
    genre: Genre? = LocalSagaGenre.current,
    progress: StoryProgress? = null,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        GenreStoryBackground(Modifier.fillMaxSize(), genre)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = themePainter(),
                contentDescription = null,
                modifier =
                    Modifier
                        .size(50.dp)
                        .gradientFill(Brush.verticalGradient(morphingGradient()))
                        .themeVfx(true)
                        .reactiveShimmer(
                            true,
                            shimmerColors = Color.White.shimmerize(),
                            repeatMode = RepeatMode.Restart,
                            duration = 10.seconds,
                        ),
            )
            Text(
                text = message,
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        brush = Brush.horizontalGradient(themeBrushColors()),
                        shadow = Shadow(Color.White, blurRadius = 15f),
                    ),
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .padding(top = 16.dp, start = 32.dp, end = 32.dp)
                        .reactiveShimmer(
                            true,
                            shimmerColors = Color.White.shimmerize(),
                            repeatMode = RepeatMode.Restart,
                            duration = 10.seconds,
                        ),
            )
        }
    }
}

/** Something went wrong and the player has to decide what happens next. */
@Composable
fun GenreStoryNotice(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    genre: Genre? = LocalSagaGenre.current,
    action: StoryBeatAction? = null,
) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        GenreStoryBackground(Modifier.fillMaxSize(), genre)

        GenreStorySurface(
            beat =
                StoryBeat(
                    key = title to message,
                    title = title,
                    body = message,
                    tone = StoryBeatTone.SYSTEM,
                    actions = listOfNotNull(action?.copy(emphasis = StoryActionEmphasis.PRIMARY)),
                ),
            modifier = Modifier.fillMaxWidth(),
            genre = genre,
            canAnimate = false,
            embedded = true,
        )
    }
}
