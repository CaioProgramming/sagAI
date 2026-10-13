package com.ilustris.sagai.core.globalshell

import android.graphics.Bitmap
import com.ilustris.sagai.features.characters.data.model.Character
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.components.NotificationStyle

sealed class GlobalShellEffect(
    open val sagaId: Int,
    open val sagaTitle: String,
    open val genre: Genre,
    open val message: String,
    open val character: Character? = null,
    open val icon: Bitmap? = null,
    open val largeIcon: Bitmap? = null,
    open val deepLink: String,
    open val notificationStyle: NotificationStyle = NotificationStyle.DEFAULT,
    open val priority: GlobalShellPriority,
    open val defaultExpansion: GlobalShellExpansion = GlobalShellExpansion.Collapsed,
) {
    /**
     * Uniquely identifies this effect instance.
     *
     * Used for cancelling/auto-dismiss safety and for "latest only" behavior.
     */
    abstract val id: String

    /** False for effects that are only worth showing while the app is open (no system notification in the background). */
    open val deliversSystemNotification: Boolean = true

    abstract fun shouldSuppress(ctx: GlobalShellVisibilityContext): Boolean
}

enum class ProgressionEffectKind { LEVEL_UP, MEDAL, LEGACY }

/**
 * One character of a progression announcement. A level-up carries the new [level] and the feat that paid it; a medal
 * carries the character's latest medal ([title], [detail] = why, [emoji]) and how many more came with it ([extra]);
 * a legacy carries its [title] and [detail] (the legend), [memorial] when the character is gone.
 */
data class ProgressionRow(
    val character: Character,
    val title: String = "",
    val detail: String = "",
    val level: Int? = null,
    val xp: Int = 0,
    val emoji: String? = null,
    val extra: Int = 0,
    val memorial: Boolean = false,
)

/**
 * Tells the player that characters leveled up or earned a medal or a legacy. Silent in the background (never a system
 * notification) and deterministic: names, levels and titles, no AI call. [ProgressionIslandContent] draws it.
 */
data class ProgressionEffect(
    val kind: ProgressionEffectKind,
    val announcementKey: String,
    val rows: List<ProgressionRow>,
    override val sagaId: Int,
    override val sagaTitle: String,
    override val genre: Genre,
    override val deepLink: String,
    /** Where "Ver mais" lands: the event, the chapter or the act that earned it. */
    val moreDeepLink: String = deepLink,
    /** True only for the very first level-up the player ever sees: it carries a short explanation of the system. */
    val showExplainer: Boolean = false,
) : GlobalShellEffect(
        sagaId = sagaId,
        sagaTitle = sagaTitle,
        genre = genre,
        message = rows.joinToString { "${it.character.name} · ${it.title.ifBlank { it.level?.toString().orEmpty() }}" },
        deepLink = deepLink,
        notificationStyle = NotificationStyle.DEFAULT,
        priority = GlobalShellPriority.Transient,
        defaultExpansion = GlobalShellExpansion.Collapsed,
    ) {
    override val id: String = "progression_${kind.name}_$announcementKey"
    override val deliversSystemNotification: Boolean = false

    override fun shouldSuppress(ctx: GlobalShellVisibilityContext): Boolean =
        kind == ProgressionEffectKind.LEVEL_UP && rows.size == 1 && ctx.isOnCharacterDetail(rows.first().character.id)
}

data class NewMessageEffect(
    val messageId: Int,
    override val sagaId: Int,
    override val sagaTitle: String,
    override val genre: Genre,
    val speakerName: String,
    val rawText: String,
    override val character: Character? = null,
    override val icon: Bitmap? = null,
    override val largeIcon: Bitmap? = null,
    override val deepLink: String,
) : GlobalShellEffect(
    sagaId = sagaId,
    sagaTitle = sagaTitle,
    genre = genre,
    message = "$speakerName: $rawText",
        character = character,
        deepLink = deepLink,
    notificationStyle = NotificationStyle.CHAT,
    priority = GlobalShellPriority.Transient,
    defaultExpansion = GlobalShellExpansion.Collapsed,
) {
    override val id: String = "msg_$messageId"

    override fun shouldSuppress(ctx: GlobalShellVisibilityContext): Boolean =
        ctx.isOnChatForSaga(sagaId)
}

data class NewChapterEffect(
    val chapterId: Int,
    override val sagaId: Int,
    override val sagaTitle: String,
    override val genre: Genre,
    val chapterTitle: String,
    override val icon: Bitmap? = null,
    override val largeIcon: Bitmap? = null,
    override val deepLink: String,
) : GlobalShellEffect(
    sagaId = sagaId,
    sagaTitle = sagaTitle,
    genre = genre,
    message = chapterTitle,
    deepLink = deepLink,
    notificationStyle = NotificationStyle.DEFAULT,
    priority = GlobalShellPriority.Transient,
    defaultExpansion = GlobalShellExpansion.Collapsed,
) {
    override val id: String = "chapter_$chapterId"

    override fun shouldSuppress(ctx: GlobalShellVisibilityContext): Boolean =
        // If the user is already in the chat, they will see the chapter content naturally.
        ctx.isOnChatForSaga(sagaId)
}

data class NewCharacterEffect(
    val characterId: Int,
    override val sagaId: Int,
    override val sagaTitle: String,
    override val genre: Genre,
    val characterName: String,
    override val character: Character? = null,
    override val icon: Bitmap? = null,
    override val largeIcon: Bitmap? = null,
    override val deepLink: String,
) : GlobalShellEffect(
    sagaId = sagaId,
    sagaTitle = sagaTitle,
    genre = genre,
    message = characterName,
        character = character,
    deepLink = deepLink,
    notificationStyle = NotificationStyle.DEFAULT,
    priority = GlobalShellPriority.Transient,
    defaultExpansion = GlobalShellExpansion.Collapsed,
) {
    override val id: String = "character_$characterId"

    override fun shouldSuppress(ctx: GlobalShellVisibilityContext): Boolean =
        ctx.isOnCharacterDetail(characterId)
}

data class ReviewReadyEffect(
    override val sagaId: Int,
    override val sagaTitle: String,
    override val genre: Genre,
    override val message: String,
    override val icon: Bitmap? = null,
    override val largeIcon: Bitmap? = null,
    override val deepLink: String,
) : GlobalShellEffect(
    sagaId = sagaId,
    sagaTitle = sagaTitle,
    genre = genre,
    message = message,
    deepLink = deepLink,
    notificationStyle = NotificationStyle.DEFAULT,
    priority = GlobalShellPriority.Transient,
    defaultExpansion = GlobalShellExpansion.Collapsed,
) {
    override val id: String = "review_$sagaId"

    override fun shouldSuppress(ctx: GlobalShellVisibilityContext): Boolean =
        ctx.isReviewVisibleForSaga(sagaId)
}

data class BookReadyEffect(
    val actId: Int,
    override val sagaId: Int,
    override val sagaTitle: String,
    override val genre: Genre,
    val actTitle: String,
    override val icon: Bitmap? = null,
    override val largeIcon: Bitmap? = null,
    override val deepLink: String,
) : GlobalShellEffect(
    sagaId = sagaId,
    sagaTitle = sagaTitle,
    genre = genre,
    message = actTitle,
    deepLink = deepLink,
    notificationStyle = NotificationStyle.DEFAULT,
    priority = GlobalShellPriority.Transient,
    defaultExpansion = GlobalShellExpansion.Collapsed,
) {
    override val id: String = "book_${sagaId}_$actId"

    override fun shouldSuppress(ctx: GlobalShellVisibilityContext): Boolean =
        ctx.isOnBookReader(sagaId, actId) || ctx.isOnChronicle(sagaId)
}

/**
 * Marks book (Act prose) generation as sticky "persistent work" so transient effects
 * queue behind it instead of stealing the shell. Rendering itself is driven directly
 * from [com.ilustris.sagai.features.act.BookGenerationService]'s own state, not from
 * this effect's content.
 */
data class BookGenerationWorkEffect(
    val actId: Int,
    override val sagaId: Int,
    override val sagaTitle: String,
    override val genre: Genre,
    override val message: String,
    override val deepLink: String,
) : GlobalShellEffect(
        sagaId = sagaId,
        sagaTitle = sagaTitle,
        genre = genre,
        message = message,
        deepLink = deepLink,
        notificationStyle = NotificationStyle.DEFAULT,
        priority = GlobalShellPriority.PersistentWork,
        defaultExpansion = GlobalShellExpansion.Collapsed,
    ) {
    override val id: String = "bookgen_${sagaId}_$actId"

    override fun shouldSuppress(ctx: GlobalShellVisibilityContext): Boolean = ctx.isOnChronicle(sagaId)
}

/**
 * Marks a chat reply generation as sticky "persistent work" so transient effects queue
 * behind it instead of stealing the shell. Rendering itself is driven directly from
 * [com.ilustris.sagai.features.saga.chat.data.usecase.ChatGenerationService]'s own
 * per-saga state, not from this effect's content.
 */
data class ChatGenerationWorkEffect(
    override val sagaId: Int,
    override val sagaTitle: String,
    override val genre: Genre,
    override val message: String,
    override val deepLink: String,
) : GlobalShellEffect(
        sagaId = sagaId,
        sagaTitle = sagaTitle,
        genre = genre,
        message = message,
        deepLink = deepLink,
        notificationStyle = NotificationStyle.DEFAULT,
        priority = GlobalShellPriority.PersistentWork,
        defaultExpansion = GlobalShellExpansion.Collapsed,
    ) {
    override val id: String = "chatgen_$sagaId"

    override fun shouldSuppress(ctx: GlobalShellVisibilityContext): Boolean = ctx.isOnChatForSaga(sagaId)
}

/**
 * Placeholder effect for persistent image generation work.
 *
 * Will be wired to [com.ilustris.sagai.features.imagegeneration.ImageGenerationService]
 * during the GlobalHost refactor.
 */
data class ImageGenerationWorkEffect(
    override val sagaId: Int,
    override val sagaTitle: String,
    override val genre: Genre,
    override val message: String,
    override val deepLink: String,
) : GlobalShellEffect(
    sagaId = sagaId,
    sagaTitle = sagaTitle,
    genre = genre,
    message = message,
    icon = null,
    largeIcon = null,
    deepLink = deepLink,
    notificationStyle = NotificationStyle.DEFAULT,
    priority = GlobalShellPriority.PersistentWork,
    defaultExpansion = GlobalShellExpansion.Collapsed,
) {
    override val id: String = "imagegen_${sagaId}"

    override fun shouldSuppress(ctx: GlobalShellVisibilityContext): Boolean = false
}

