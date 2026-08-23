package com.ilustris.sagai.ui.genre.surface

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
 * [embedded] says whether the beat owns the screen or is one item inside somebody else's scrolling
 * container. The story review embeds; the Milestone screen does not. It changes real structure, not
 * just decoration — an embedded beat is measured with unbounded height, so it must not fill, weight
 * or scroll — which is why it is a parameter rather than something each surface infers.
 */
@Composable
fun GenreStorySurface(
    beat: StoryBeat,
    modifier: Modifier = Modifier,
    genre: Genre? = LocalSagaGenre.current,
    canAnimate: Boolean = true,
    embedded: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    Box(if (embedded) modifier.fillMaxWidth() else modifier.fillMaxSize()) {
        // An embedded beat draws no ground of its own: the review's containers already painted it
        // behind the whole pager, and a second coat would cover the page-turn between beats.
        if (!embedded) {
            GenreStoryBackground(Modifier.fillMaxSize(), genre)
        }

        when (genre?.surfaceStyle() ?: GenreSurfaceStyle.DEFAULT) {
            GenreSurfaceStyle.BOOK ->
                BookStoryBeat(beat, Modifier, canAnimate, embedded, contentPadding)

            GenreSurfaceStyle.TERMINAL ->
                TerminalStoryBeat(beat, Modifier, canAnimate, embedded, contentPadding)

            GenreSurfaceStyle.CRIME ->
                CrimeStoryBeat(beat, Modifier, canAnimate, embedded, contentPadding)

            GenreSurfaceStyle.COLLAGE ->
                CollageStoryBeat(beat, Modifier, canAnimate, embedded, contentPadding)

            GenreSurfaceStyle.COMIC ->
                ComicStoryBeat(beat, Modifier, canAnimate, embedded, contentPadding)

            // Only reachable for a null genre — every real genre has a style of its own.
            GenreSurfaceStyle.DEFAULT ->
                PlainStoryBeat(beat, Modifier, canAnimate, embedded, contentPadding, genre)
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
