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
import com.ilustris.sagai.ui.genre.book.CowboyBurnMarks
import com.ilustris.sagai.ui.genre.book.HorrorPoliceTapeOverlay
import com.ilustris.sagai.ui.genre.book.ShinobiInkBlooms
import com.ilustris.sagai.ui.genre.collage.PunkScribbleOverlay
import com.ilustris.sagai.ui.genre.crime.CrimeBackground
import com.ilustris.sagai.ui.genre.surface.book.BookStoryBeat
import com.ilustris.sagai.ui.genre.surface.collage.CollageStoryBeat
import com.ilustris.sagai.ui.genre.surface.comic.ComicStoryBeat
import com.ilustris.sagai.ui.genre.surface.crime.CrimeStoryBeat
import com.ilustris.sagai.ui.genre.surface.plain.PlainStoryBeat
import com.ilustris.sagai.ui.genre.surface.terminal.TerminalStoryBeat
import com.ilustris.sagai.ui.genre.surfaceStyle
import com.ilustris.sagai.ui.genre.terminal.TerminalBackground
import com.ilustris.sagai.ui.genre.terminal.TerminalGlitchOverlay
import com.ilustris.sagai.ui.theme.LocalSagaGenre
import com.ilustris.sagai.ui.theme.filters.crtScreen

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

/**
 * The genre-exclusive ambient decoration drawn over a beat, on its own so every caller — a beat's
 * own surface, the Introduction, a loading state — reaches the same three genres' worth of texture
 * through one door instead of each re-implementing its own `when (genre)`.
 *
 * Deliberately keyed on the exact [Genre], not [GenreSurfaceStyle]: these are the genres that stand
 * out *within* a shared style (Shinobi/Cowboy/Horror all wear Book, Punk Rock is the only Collage
 * genre today), so a style-level dispatch would erase exactly the distinction this exists to draw.
 */
@Composable
fun GenreStoryAmbientOverlay(
    modifier: Modifier = Modifier,
    genre: Genre? = LocalSagaGenre.current,
) {
    when (genre) {
        Genre.SHINOBI -> ShinobiInkBlooms(modifier)
        Genre.COWBOY -> CowboyBurnMarks(modifier)
        Genre.HORROR -> HorrorPoliceTapeOverlay(modifier)
        Genre.PUNK_ROCK -> PunkScribbleOverlay(modifier)
        else -> Unit
    }
}

/**
 * The screen-level filter Terminal wears: tube curvature and bloom that resample the whole picture
 * — background and copy together, which is why this wraps both rather than sitting behind them —
 * plus Cyberpunk's own signal glitch riding on top of everything as the one topmost layer. Space
 * Opera gets the tube but not the glitch: a working console's picture doesn't tear.
 *
 * This is chrome for the screen a beat lives on, not a concern of the beat itself, so a caller that
 * owns a full screen wraps everything in this once — see [com.ilustris.sagai.features.saga.milestone.ui.MilestoneScreen].
 * The story review's own `TerminalReviewContainer` applies the same two effects itself around its
 * whole pager, so nothing here needs to run again for an [GenreStorySurface] page embedded in it.
 */
@Composable
fun GenreScreenEffects(
    genre: Genre? = LocalSagaGenre.current,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val isTerminal = (genre?.surfaceStyle() ?: GenreSurfaceStyle.DEFAULT) == GenreSurfaceStyle.TERMINAL

    Box(
        if (isTerminal) modifier.fillMaxSize().crtScreen() else modifier.fillMaxSize(),
    ) {
        content()
        if (isTerminal && genre == Genre.CYBERPUNK) {
            TerminalGlitchOverlay(Modifier.fillMaxSize())
        }
    }
}
