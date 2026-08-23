package com.ilustris.sagai.ui.genre.surface

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.genre.GenreSurfaceStyle
import com.ilustris.sagai.ui.genre.book.BookBackground
import com.ilustris.sagai.ui.genre.crime.CrimeBackground
import com.ilustris.sagai.ui.genre.surface.book.BookStoryBeat
import com.ilustris.sagai.ui.genre.surface.collage.CollageStoryBeat
import com.ilustris.sagai.ui.genre.surface.comic.ComicStoryBeat
import com.ilustris.sagai.ui.genre.surface.crime.CrimeStoryBeat
import com.ilustris.sagai.ui.genre.surface.plain.PlainStoryBeat
import com.ilustris.sagai.ui.genre.surface.terminal.TerminalStoryBeat
import com.ilustris.sagai.ui.genre.terminal.TerminalBackground
import com.ilustris.sagai.ui.genre.surfaceStyle
import com.ilustris.sagai.ui.theme.LocalSagaGenre

/**
 * Renders one [StoryBeat] in whichever visual language [genre] speaks. This is the single entry
 * point — callers describe the beat and never branch on genre themselves.
 *
 * [genre] defaults to the ambient [LocalSagaGenre] but stays overridable, the same shape
 * [com.ilustris.sagai.ui.theme.components.MorphingThemeIcon] uses: a design-system preview wants to
 * force a genre, while a real screen should just inherit the one its theme already established.
 *
 * [drawBackground] is on by default because a full-screen beat owns its whole surface. The story
 * review turns it off: its own containers already paint the background behind a pager, and painting
 * it twice would cover the page-turn.
 */
@Composable
fun GenreStorySurface(
    beat: StoryBeat,
    modifier: Modifier = Modifier,
    genre: Genre? = LocalSagaGenre.current,
    canAnimate: Boolean = true,
    drawBackground: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    Box(modifier.fillMaxSize()) {
        if (drawBackground) {
            GenreStoryBackground(Modifier.fillMaxSize(), genre)
        }

        when (genre?.surfaceStyle() ?: GenreSurfaceStyle.DEFAULT) {
            GenreSurfaceStyle.BOOK ->
                BookStoryBeat(beat, Modifier.fillMaxSize(), canAnimate, contentPadding)

            GenreSurfaceStyle.TERMINAL ->
                TerminalStoryBeat(beat, Modifier.fillMaxSize(), canAnimate, contentPadding)

            GenreSurfaceStyle.CRIME ->
                CrimeStoryBeat(beat, Modifier.fillMaxSize(), canAnimate, contentPadding)

            GenreSurfaceStyle.COLLAGE ->
                CollageStoryBeat(beat, Modifier.fillMaxSize(), canAnimate, contentPadding)

            GenreSurfaceStyle.COMIC ->
                ComicStoryBeat(beat, Modifier.fillMaxSize(), canAnimate, contentPadding)

            // Only reachable for a null genre — every real genre has a style of its own.
            GenreSurfaceStyle.DEFAULT ->
                PlainStoryBeat(beat, Modifier.fillMaxSize(), canAnimate, contentPadding, genre)
        }
    }
}

/**
 * The ground a beat is told on, on its own so a screen that composes several beats — or scrolls
 * through them — paints it once instead of once per beat.
 */
@Composable
fun GenreStoryBackground(
    modifier: Modifier = Modifier,
    genre: Genre? = LocalSagaGenre.current,
) {
    when (genre?.surfaceStyle() ?: GenreSurfaceStyle.DEFAULT) {
        GenreSurfaceStyle.BOOK -> BookBackground(modifier)
        GenreSurfaceStyle.TERMINAL -> TerminalBackground(modifier)
        GenreSurfaceStyle.CRIME -> CrimeBackground(modifier)
        GenreSurfaceStyle.COLLAGE,
        GenreSurfaceStyle.COMIC,
        GenreSurfaceStyle.DEFAULT,
        -> Box(modifier.background(MaterialTheme.colorScheme.background))
    }
}
