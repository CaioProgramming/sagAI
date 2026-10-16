package com.ilustris.sagai.features.widgets.domain

import com.ilustris.sagai.features.newsaga.data.model.Genre

/** A saga as the widgets need it. Image fields are absolute file paths, or null when the saga has none. */
data class WidgetSaga(
    val id: Int,
    val title: String,
    val genre: Genre,
    val iconPath: String?,
    val artPath: String?,
    val portraitPath: String?,
) {
    /** Best picture to put behind text: the saga artwork, then its icon, then the main character. */
    val backdropPath: String? get() = artPath ?: iconPath ?: portraitPath
}

/** Shown when no saga is running: the player is invited to write the next story. */
data class WidgetInvite(
    val genre: Genre,
    val hasFinishedSagas: Boolean,
)

sealed interface ContinueData {
    data class Active(
        val saga: WidgetSaga,
        val objective: String?,
    ) : ContinueData

    data class Invite(
        val invite: WidgetInvite,
    ) : ContinueData
}

sealed interface LastMessageData {
    data class Message(
        val saga: WidgetSaga,
        val text: String,
        val speaker: String?,
        /** Path to the speaker's portrait, or null when it should fall back to the saga icon. */
        val portraitPath: String?,
    ) : LastMessageData

    data class Invite(
        val invite: WidgetInvite,
    ) : LastMessageData
}

data class StoryBook(
    val id: Int,
    val title: String,
    val genre: Genre,
    val iconPath: String?,
)

data class StoriesData(
    val books: List<StoryBook>,
    val totalCount: Int,
    val playTimeHours: Int,
    /** Genre used when the widget has nothing to show; the newest finished saga's, if any. */
    val fallbackGenre: Genre,
)

/** Colors and corner radius for one widget, resolved from the genre's Remote Config. */
data class WidgetTheme(
    val palette: WidgetPalette,
    val cornerDp: Float,
)

/** The character a generated widget is about, as it is right now in the database. */
data class WidgetCharacter(
    val id: Int,
    val sagaId: Int,
    val name: String,
    val portraitPath: String?,
    val genre: Genre,
    val sagaTitle: String,
)
