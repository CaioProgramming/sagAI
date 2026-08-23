package com.ilustris.sagai.ui.genre.surface.plain

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.characters.ui.CharacterAvatar
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.features.wiki.data.model.Wiki
import com.ilustris.sagai.features.wiki.ui.WikiCard
import com.ilustris.sagai.ui.genre.surface.StoryActionEmphasis
import com.ilustris.sagai.ui.genre.surface.StoryBeat
import com.ilustris.sagai.ui.genre.surface.StoryBeatAction
import com.ilustris.sagai.ui.genre.surface.StoryProgress
import com.ilustris.sagai.ui.theme.gradientFill
import com.ilustris.sagai.ui.theme.morphingGradient
import com.ilustris.sagai.ui.theme.reactiveShimmer
import com.ilustris.sagai.ui.theme.shimmerize
import com.ilustris.sagai.ui.theme.themePainter
import com.ilustris.sagai.ui.theme.themeVfx
import kotlin.time.Duration.Companion.seconds

/**
 * The unstyled beat: theme icon, centred title, body, and whatever extras came along in plain rows.
 *
 * Reached only when the genre is null — a Compose preview, or a screen composed before the saga has
 * loaded — since every real genre maps to a style of its own. It is the Milestone screen's old
 * layout, kept verbatim so that fallback stays a familiar screen rather than an empty one.
 */
@Composable
fun PlainStoryBeat(
    beat: StoryBeat,
    modifier: Modifier = Modifier,
    canAnimate: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    genre: Genre? = null,
) {
    Column(
        modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(contentPadding)
            .padding(horizontal = 32.dp, vertical = 24.dp),
    ) {
        beat.progress?.takeIf { it.total > 1 }?.let { DotProgress(it) }

        // Re-fires per beat, so a chain of them each get their own entrance instead of the second
        // one simply materialising where the first was.
        var visible by remember(beat.key) { mutableStateOf(!canAnimate) }
        LaunchedEffect(beat.key) { visible = true }

        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400)) + scaleIn(initialScale = 0.92f, animationSpec = tween(400)),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = themePainter(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
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

                    beat.eyebrow?.let {
                        Text(
                            text = it.lowercase(),
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 24.dp).alpha(.5f),
                        )
                    }

                    beat.title?.let {
                        Text(
                            text = it,
                            style =
                                MaterialTheme.typography.headlineLarge.copy(
                                    letterSpacing = 0.5.sp,
                                    shadow = Shadow(MaterialTheme.colorScheme.primary, blurRadius = 15f),
                                    fontWeight = FontWeight.SemiBold,
                                ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }

                    beat.body?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 16.dp),
                        )
                    }

                    if (beat.figures.size == 1) {
                        FigureCard(beat.figures.first(), Modifier.padding(top = 16.dp))
                    } else if (beat.figures.size > 1) {
                        FigureRow(beat.figures, Modifier.padding(top = 16.dp))
                    }

                    if (beat.entries.isNotEmpty() && genre != null) {
                        EntriesRow(beat.entries, beat.entriesLabel, genre, Modifier.padding(top = 16.dp))
                    }

                    if (beat.cast.isNotEmpty() && genre != null) {
                        CastRow(beat.cast, beat.castLabel, genre, Modifier.padding(top = 16.dp))
                    }

                    beat.aside?.let { AsideNote(it.label, it.text, Modifier.padding(top = 16.dp)) }
                }
            }
        }

        val actionsVisible = !beat.gateActionsOnReveal || visible
        AnimatedVisibility(actionsVisible) {
            Column {
                beat.actions.forEach { action ->
                    when (action.emphasis) {
                        StoryActionEmphasis.PRIMARY -> PrimaryAction(action)
                        StoryActionEmphasis.SECONDARY -> SecondaryAction(action)
                    }
                }
            }
        }
    }
}

@Composable
private fun PrimaryAction(action: StoryBeatAction) {
    Button(
        onClick = { if (!action.busy) action.onClick() },
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (action.busy) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(8.dp))
        }
        Text(action.label)
    }
}

@Composable
private fun SecondaryAction(action: StoryBeatAction) {
    TextButton(
        onClick = { if (!action.busy) action.onClick() },
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (action.busy) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(8.dp))
        }
        Text(action.label)
    }
}

@Composable
internal fun DotProgress(progress: StoryProgress) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        repeat(progress.total) { i ->
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (i < progress.index) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    ),
            )
        }
    }
}

/** Fades in on its own — a cover is generated in the background and often lands after the beat. */
@Composable
private fun FigureCard(
    url: String,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = url.isNotBlank(),
        enter = fadeIn(tween(500)) + scaleIn(initialScale = 0.94f, animationSpec = tween(500)),
        modifier = modifier,
    ) {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(MaterialTheme.shapes.large),
        )
    }
}

@Composable
private fun FigureRow(
    urls: List<String>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        urls.forEach { FigureCard(it, Modifier.width(200.dp)) }
    }
}

@Composable
private fun EntriesRow(
    entries: List<Wiki>,
    label: String?,
    genre: Genre,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        label?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            entries.forEach { WikiCard(wiki = it, genre = genre, expanded = false, modifier = Modifier.width(220.dp)) }
        }
    }
}

@Composable
private fun CastRow(
    cast: List<Character>,
    label: String?,
    genre: Genre,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        label?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            cast.forEach { character ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(72.dp),
                ) {
                    CharacterAvatar(character = character, genre = genre, modifier = Modifier.size(56.dp))
                    Text(
                        text = "${character.name} ${character.lastName.orEmpty()}".trim(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

/** A quiet, neutrally-bordered aside — deliberately not competing with the story above it. */
@Composable
private fun AsideNote(
    label: String,
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
            .padding(12.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
