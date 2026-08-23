package com.ilustris.sagai.ui.genre

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre

/**
 * The visual language a genre speaks. Decoupled from [Genre] on purpose, so several genres can
 * share one style — Fantasy, Shinobi, Cowboy and Horror are all printed books, they just have
 * different things bleeding through the paper.
 *
 * This is a property of the genre itself, not of any one screen, which is why it lives here rather
 * than in the review feature it was born in: the story review and the Milestone screen both ask the
 * same question and must get the same answer, or a saga would change its handwriting between two
 * screens the player sees minutes apart.
 *
 * [DEFAULT] is unreachable for a real saga — every one of the nine [Genre] entries maps to a real
 * style below. It exists for a null genre: a Compose preview, or a screen composed before the saga
 * has loaded.
 */
enum class GenreSurfaceStyle {
    DEFAULT,
    TERMINAL,
    BOOK,
    CRIME,
    COLLAGE,
    COMIC,
}

/**
 * Which [GenreSurfaceStyle] a genre wears. Adding a style to a new genre is a one-line change here
 * and nothing else needs to know about it.
 */
fun Genre.surfaceStyle(): GenreSurfaceStyle =
    when (this) {
        Genre.CYBERPUNK, Genre.SPACE_OPERA -> GenreSurfaceStyle.TERMINAL
        Genre.FANTASY, Genre.SHINOBI, Genre.COWBOY, Genre.HORROR -> GenreSurfaceStyle.BOOK
        Genre.CRIME -> GenreSurfaceStyle.CRIME
        Genre.PUNK_ROCK -> GenreSurfaceStyle.COLLAGE
        Genre.HEROES -> GenreSurfaceStyle.COMIC
    }

/**
 * What the Characters/Cast beat calls itself for genres with a stronger in-world label than the
 * generic [R.string.review_stage_characters_title] — properly localized (EN/pt-BR) rather than
 * hardcoded, unlike [com.ilustris.sagai.features.newsaga.data.model.subtitle]'s pattern elsewhere.
 */
@Composable
fun Genre.castTitle(): String =
    when (this) {
        Genre.FANTASY -> stringResource(R.string.review_cast_title_fantasy)
        Genre.SHINOBI -> stringResource(R.string.review_cast_title_shinobi)
        Genre.COWBOY -> stringResource(R.string.review_cast_title_cowboy)
        Genre.HORROR -> stringResource(R.string.review_cast_title_horror)
        else -> stringResource(R.string.review_stage_characters_title)
    }
