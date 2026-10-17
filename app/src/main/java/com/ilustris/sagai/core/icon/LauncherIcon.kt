package com.ilustris.sagai.core.icon

import androidx.annotation.DrawableRes
import com.ilustris.sagai.R
import com.ilustris.sagai.features.newsaga.data.model.Genre

/**
 * The launcher icons the app can wear: the default dragon, plus one per [Genre].
 *
 * Each one is an `<activity-alias>` in the manifest named `.launcher.<aliasName>`; exactly one of them
 * is enabled at any time. The foregrounds are also what the picker previews, so the two stay in sync.
 */
enum class LauncherIcon(
    val aliasName: String,
    val genre: Genre?,
    @DrawableRes val backgroundLight: Int,
    @DrawableRes val backgroundDark: Int,
    @DrawableRes val foregroundLight: Int,
    @DrawableRes val foregroundDark: Int,
) {
    DEFAULT(
        "DefaultIcon",
        null,
        R.drawable.ic_dragon_bg_light,
        R.drawable.ic_dragon_bg_dark,
        R.drawable.ic_dragon_fg_light,
        R.drawable.ic_dragon_fg_dark,
    ),
    FANTASY(
        "FantasyIcon",
        Genre.FANTASY,
        R.drawable.ic_dragon_fantasy_bg_light,
        R.drawable.ic_dragon_fantasy_bg_dark,
        R.drawable.ic_dragon_fantasy_fg_light,
        R.drawable.ic_dragon_fantasy_fg_dark,
    ),
    CYBERPUNK(
        "CyberpunkIcon",
        Genre.CYBERPUNK,
        R.drawable.ic_dragon_cyberpunk_bg_light,
        R.drawable.ic_dragon_cyberpunk_bg_dark,
        R.drawable.ic_dragon_cyberpunk_fg_light,
        R.drawable.ic_dragon_cyberpunk_fg_dark,
    ),
    HORROR(
        "HorrorIcon",
        Genre.HORROR,
        R.drawable.ic_dragon_horror_bg_light,
        R.drawable.ic_dragon_horror_bg_dark,
        R.drawable.ic_dragon_horror_fg_light,
        R.drawable.ic_dragon_horror_fg_dark,
    ),
    HEROES(
        "HeroesIcon",
        Genre.HEROES,
        R.drawable.ic_dragon_heroes_bg_light,
        R.drawable.ic_dragon_heroes_bg_dark,
        R.drawable.ic_dragon_heroes_fg_light,
        R.drawable.ic_dragon_heroes_fg_dark,
    ),
    CRIME(
        "CrimeIcon",
        Genre.CRIME,
        R.drawable.ic_dragon_crime_bg_light,
        R.drawable.ic_dragon_crime_bg_dark,
        R.drawable.ic_dragon_crime_fg_light,
        R.drawable.ic_dragon_crime_fg_dark,
    ),
    SHINOBI(
        "ShinobiIcon",
        Genre.SHINOBI,
        R.drawable.ic_dragon_shinobi_bg_light,
        R.drawable.ic_dragon_shinobi_bg_dark,
        R.drawable.ic_dragon_shinobi_fg_light,
        R.drawable.ic_dragon_shinobi_fg_dark,
    ),
    SPACE_OPERA(
        "SpaceOperaIcon",
        Genre.SPACE_OPERA,
        R.drawable.ic_dragon_space_opera_bg_light,
        R.drawable.ic_dragon_space_opera_bg_dark,
        R.drawable.ic_dragon_space_opera_fg_light,
        R.drawable.ic_dragon_space_opera_fg_dark,
    ),
    COWBOY(
        "CowboyIcon",
        Genre.COWBOY,
        R.drawable.ic_dragon_cowboy_bg_light,
        R.drawable.ic_dragon_cowboy_bg_dark,
        R.drawable.ic_dragon_cowboy_fg_light,
        R.drawable.ic_dragon_cowboy_fg_dark,
    ),
    PUNK_ROCK(
        "PunkRockIcon",
        Genre.PUNK_ROCK,
        R.drawable.ic_dragon_punk_rock_bg_light,
        R.drawable.ic_dragon_punk_rock_bg_dark,
        R.drawable.ic_dragon_punk_rock_fg_light,
        R.drawable.ic_dragon_punk_rock_fg_dark,
    ),
    ;

    companion object {
        /** The icon that goes with [genre]; the default dragon when there is none. */
        fun forGenre(genre: Genre?): LauncherIcon = entries.firstOrNull { it.genre == genre } ?: DEFAULT
    }
}
