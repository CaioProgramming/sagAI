package com.ilustris.sagai.ui.genre.surface

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.genre.GenreSurfaceStyle
import com.ilustris.sagai.ui.genre.collage.PAPER_INK
import com.ilustris.sagai.ui.genre.collage.TornPaperStrip
import com.ilustris.sagai.ui.genre.collage.rememberTearReveal
import com.ilustris.sagai.ui.genre.comic.ComicCaptionBox
import com.ilustris.sagai.ui.genre.comic.ComicPanel
import com.ilustris.sagai.ui.genre.comic.COMIC_INK
import com.ilustris.sagai.ui.genre.comic.COMIC_PAPER
import com.ilustris.sagai.ui.genre.crime.CrimeBubbleFrame
import com.ilustris.sagai.ui.genre.surfaceStyle
import com.ilustris.sagai.ui.genre.terminal.TerminalLine
import com.ilustris.sagai.ui.genre.terminal.TerminalProgress
import com.ilustris.sagai.ui.genre.terminal.TerminalTypewriter
import com.ilustris.sagai.ui.genre.terminal.terminalHost
import com.ilustris.sagai.ui.genre.terminal.terminalPromptLine
import com.ilustris.sagai.ui.theme.LocalSagaGenre
import com.ilustris.sagai.ui.theme.SimpleTypewriterText
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
 * Separate entry points rather than [StoryBeatTone] variants because their genre idioms have
 * nothing to do with a title and a body. Waiting is a blinking prompt, a typing indicator, a strip
 * caught mid-tear — none of which [StoryBeat] describes, and forcing them through it would leave
 * every surface carrying a branch that ignores most of the model.
 */
@Composable
fun GenreStoryLoading(
    message: String,
    modifier: Modifier = Modifier,
    genre: Genre? = LocalSagaGenre.current,
    progress: StoryProgress? = null,
) {
    Box(modifier.fillMaxSize()) {
        GenreStoryBackground(Modifier.fillMaxSize(), genre)

        when (genre?.surfaceStyle() ?: GenreSurfaceStyle.DEFAULT) {
            GenreSurfaceStyle.TERMINAL -> TerminalLoading(message, progress)
            GenreSurfaceStyle.BOOK -> BookLoading(message)
            GenreSurfaceStyle.CRIME -> CrimeLoading(message, genre)
            GenreSurfaceStyle.COLLAGE -> CollageLoading(message)
            GenreSurfaceStyle.COMIC -> ComicLoading(message)
            GenreSurfaceStyle.DEFAULT -> PlainLoading(message)
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
                    // Terminal prints it as a failed command rather than a titled paragraph.
                    verb = "recover --retry",
                    tone = StoryBeatTone.SYSTEM,
                    actions = listOfNotNull(action?.copy(emphasis = StoryActionEmphasis.PRIMARY)),
                ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            genre = genre,
            canAnimate = false,
            embedded = true,
        )
    }
}

/** A live prompt with the caret still blinking, and a bar sweeping with no known total. */
@Composable
private fun TerminalLoading(
    message: String,
    progress: StoryProgress?,
) {
    val accent = MaterialTheme.colorScheme.primary
    val mono = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace)

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        TerminalTypewriter(
            lines =
                listOf(
                    terminalPromptLine(host = terminalHost(""), command = "story --advance", accent = accent),
                    TerminalLine(text = message, style = mono.copy(color = MaterialTheme.colorScheme.onBackground)),
                ),
            caretColor = accent,
        )
        // Sweeps when nothing knows how far along generation is — a bar claiming a real position
        // would be lying, in a language that reads as precise. With a [progress] to show, it does.
        TerminalProgress(
            current = progress?.index ?: rememberSweep(),
            total = progress?.total ?: 100,
            color = accent,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        )
    }
}

/** 0..100 and back, so an unknowable wait still shows the machine is alive. */
@Composable
private fun rememberSweep(): Int {
    val transition = rememberInfiniteTransition(label = "sweep")
    val value by transition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(tween(2200), RepeatMode.Reverse),
        label = "sweepValue",
    )
    return value.toInt()
}

/** The next line still being set — an italic hand over the parchment. */
@Composable
private fun BookLoading(message: String) {
    val ink = MaterialTheme.colorScheme.onBackground
    Box(Modifier.fillMaxSize().padding(48.dp), contentAlignment = Alignment.Center) {
        SimpleTypewriterText(
            text = message,
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontStyle = FontStyle.Italic,
                    color = ink.copy(alpha = .75f),
                ),
            textAlign = TextAlign.Center,
        )
    }
}

/** Someone on the other end is typing. */
@Composable
private fun CrimeLoading(
    message: String,
    genre: Genre?,
) {
    Column(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        CrimeBubbleFrame(
            isMe = false,
            genre = genre ?: Genre.CRIME,
            showAvatar = false,
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        ) { contentColor ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) { index -> TypingDot(contentColor, index) }
            }
        }
        Text(
            text = message,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .6f),
            modifier = Modifier.padding(start = 12.dp, top = 10.dp),
        )
    }
}

@Composable
private fun TypingDot(
    color: Color,
    index: Int,
) {
    val transition = rememberInfiniteTransition(label = "typingDot$index")
    val alpha by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = index * 180), RepeatMode.Reverse),
        label = "typingDotAlpha$index",
    )
    Box(
        Modifier
            .size(7.dp)
            .alpha(alpha)
            .clip(CircleShape)
            .background(color),
    )
}

/** A strip caught halfway through its tear — the page is still being ripped open. */
@Composable
private fun CollageLoading(message: String) {
    val reveal = rememberTearReveal(canAnimate = true, delayMs = 0L)
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        TornPaperStrip(
            seed = 13,
            modifier = Modifier.fillMaxWidth(1.14f),
            revealProgress = reveal,
            contentPadding = PaddingValues(horizontal = 30.dp, vertical = 40.dp),
        ) {
            Text(
                text = message.uppercase(),
                color = PAPER_INK,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** The empty frame a comic leaves before the next panel is drawn. */
@Composable
private fun ComicLoading(message: String) {
    Box(Modifier.fillMaxSize().padding(28.dp), contentAlignment = Alignment.Center) {
        ComicPanel(
            modifier = Modifier.fillMaxWidth(),
            borderColor = COMIC_INK,
            background = COMIC_PAPER,
        ) {
            Box(Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                ComicCaptionBox(text = message, align = TextAlign.Center)
            }
        }
    }
}

/** Reached only for a null genre — the app's own generic "thinking" beat. */
@Composable
private fun PlainLoading(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
