package com.ilustris.sagai.ui.genre.recap

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.genre.GenreSurfaceStyle
import com.ilustris.sagai.ui.genre.recap.book.BookRecapCard
import com.ilustris.sagai.ui.genre.recap.collage.CollageRecapCard
import com.ilustris.sagai.ui.genre.recap.comic.ComicRecapCard
import com.ilustris.sagai.ui.genre.recap.crime.CrimeRecapCard
import com.ilustris.sagai.ui.genre.recap.plain.PlainRecapCard
import com.ilustris.sagai.ui.genre.recap.terminal.TerminalRecapCard
import com.ilustris.sagai.ui.genre.surfaceStyle
import com.ilustris.sagai.ui.theme.LocalSagaGenre

/**
 * The "your saga is over, come read the recap" card, in whichever visual language [genre] speaks.
 *
 * Same shape as [com.ilustris.sagai.ui.genre.surface.GenreStorySurface]: callers describe the card
 * and never branch on genre. [PlainRecapCard] — today's gradient card, unchanged — is now only
 * reachable for a null genre, the same place every other style dispatch in this package lands it.
 *
 * The click lands on the whole card here rather than inside each style, so no treatment can forget
 * to be tappable.
 *
 * None of the styles draw the full-screen ambient overlays their beat surfaces do. Those are built
 * at screen scale, and a 150dp card would get a single strip of police tape crossing it corner to
 * corner. The genres that want weather wear [com.ilustris.sagai.ui.theme.themeVfx] instead, decided
 * per card.
 */
@Composable
fun GenreRecapCard(
    card: RecapCard,
    modifier: Modifier = Modifier,
    genre: Genre? = LocalSagaGenre.current,
) {
    val tappable = modifier.clickable(onClick = card.onClick)

    when (genre?.surfaceStyle() ?: GenreSurfaceStyle.DEFAULT) {
        GenreSurfaceStyle.TERMINAL -> TerminalRecapCard(card, tappable)
        GenreSurfaceStyle.COLLAGE -> CollageRecapCard(card, tappable)
        GenreSurfaceStyle.BOOK -> BookRecapCard(card, tappable)
        GenreSurfaceStyle.CRIME -> CrimeRecapCard(card, tappable)
        GenreSurfaceStyle.COMIC -> ComicRecapCard(card, tappable)
        GenreSurfaceStyle.DEFAULT -> PlainRecapCard(card, tappable, genre)
    }
}
