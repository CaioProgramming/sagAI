package com.ilustris.sagai.core.globalshell

import android.graphics.Bitmap
import com.ilustris.sagai.features.newsaga.data.model.Genre
import com.ilustris.sagai.ui.components.NotificationStyle

sealed class GlobalShellEffect(
    open val sagaId: Int,
    open val sagaTitle: String,
    open val genre: Genre,
    open val message: String,
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

    abstract fun shouldSuppress(ctx: GlobalShellVisibilityContext): Boolean
}

data class NewMessageEffect(
    val messageId: Int,
    override val sagaId: Int,
    override val sagaTitle: String,
    override val genre: Genre,
    val speakerName: String,
    val rawText: String,
    override val icon: Bitmap? = null,
    override val largeIcon: Bitmap? = null,
    override val deepLink: String,
) : GlobalShellEffect(
    sagaId = sagaId,
    sagaTitle = sagaTitle,
    genre = genre,
    message = "$speakerName: $rawText",
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
    override val icon: Bitmap? = null,
    override val largeIcon: Bitmap? = null,
    override val deepLink: String,
) : GlobalShellEffect(
    sagaId = sagaId,
    sagaTitle = sagaTitle,
    genre = genre,
    message = characterName,
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
    override val icon: Bitmap? = null,
    override val largeIcon: Bitmap? = null,
    override val deepLink: String,
) : GlobalShellEffect(
    sagaId = sagaId,
    sagaTitle = sagaTitle,
    genre = genre,
    message = "Review ready",
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

