package com.ilustris.sagai.features.widgets.domain

import com.ilustris.sagai.features.newsaga.data.model.Genre

/** A saga as the widgets need it. Image fields are absolute file paths, or null when the saga has none. */
data class WidgetSaga(
    val id: Int,
    val title: String,
    val genre: Genre,
    /** The saga's generated cover image (`Saga.icon`). */
    val iconPath: String?,
    val portraitPath: String?,
) {
    /** Best picture to put behind text: the saga cover, then the main character. */
    val backdropPath: String? get() = iconPath ?: portraitPath
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

/** One saga on the strip under the main one: a cover that opens its chat, or its details when finished. */
data class StoryBook(
    val id: Int,
    val title: String,
    val genre: Genre,
    val iconPath: String?,
    val isEnded: Boolean,
)

/**
 * The stories widget: the saga the player touched last on top, the other recent ones under it.
 * [hero] is the unfinished saga played most recently, or the newest finished one when none is running.
 */
data class StoriesData(
    val hero: WidgetSaga?,
    val heroIsEnded: Boolean,
    /** The hero's current objective, only for a running saga. */
    val objective: String?,
    val others: List<StoryBook>,
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
