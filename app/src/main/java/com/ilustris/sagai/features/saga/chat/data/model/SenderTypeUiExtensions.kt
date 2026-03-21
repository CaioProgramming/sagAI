package com.ilustris.sagai.features.saga.chat.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ilustris.sagai.R
import com.ilustris.sagai.features.saga.chat.ui.components.ExpressiveTag

/**
 * UI affordances for [SenderType], lives in the UI layer.
 *
 * These represent **inline tag insertion options** in the input bar's tag-picker chip row.
 * They are NOT persisted sender types — they're just a way to expose [ExpressiveTag] values
 * with a SenderType facade.
 *
 * NOTE: This tag-picker UI is effectively superseded by the inline tag system.
 * These extensions exist only for compile compatibility with ChatInputView.
 * The [SenderType] enum itself (the persisted value) is not affected.
 */

/** The [ExpressiveTag] this sender type maps to, or null if it has no inline tag. */
val SenderType.tag: ExpressiveTag?
    get() =
        when (this) {
            SenderType.NARRATOR -> ExpressiveTag.NARRATOR
            SenderType.ACTION -> ExpressiveTag.ACTION
            SenderType.THOUGHT -> ExpressiveTag.THINK
            else -> null
        }

/** Icon drawable resource for this sender type, used in the input bar. */
@DrawableRes
fun SenderType.icon(): Int =
    when (this) {
        SenderType.USER -> R.drawable.ic_spark
        SenderType.CHARACTER -> R.drawable.ic_spark
        SenderType.NARRATOR -> R.drawable.ic_feather
        SenderType.ACTION -> R.drawable.action_icon
        SenderType.THOUGHT -> R.drawable.think_icon
    }

/** Short display title resource for this sender type. */
@get:StringRes
val SenderType.titleRes: Int
    get() =
        when (this) {
            SenderType.USER -> R.string.sender_type_user_title
            SenderType.CHARACTER -> R.string.user_action_title
            SenderType.NARRATOR -> R.string.sender_type_narrator_title
            SenderType.ACTION -> R.string.sender_type_action_title
            SenderType.THOUGHT -> R.string.sender_type_thought_title
        }

/** Short display title for this sender type, localized. */
@Composable
fun SenderType.title(): String = stringResource(id = titleRes)

/** Hint resource shown in the empty input field. */
@get:StringRes
val SenderType.hintRes: Int
    get() =
        when (this) {
            SenderType.USER -> R.string.sender_type_user_hint
            SenderType.CHARACTER -> R.string.sender_type_character_hint
            SenderType.NARRATOR -> R.string.sender_type_narrator_hint
            SenderType.ACTION -> R.string.sender_type_action_hint
            SenderType.THOUGHT -> R.string.sender_type_thought_hint
        }

/** Hint text shown in the empty input field, localized. */
@Composable
fun SenderType.hint(): String = stringResource(id = hintRes)

/** Accessibility description resource for this sender type. */
@get:StringRes
val SenderType.descriptionRes: Int
    get() =
        when (this) {
            SenderType.USER -> R.string.sender_type_user_description
            SenderType.CHARACTER -> R.string.sender_type_character_description
            SenderType.NARRATOR -> R.string.sender_type_narrator_description
            SenderType.ACTION -> R.string.sender_type_action_description
            SenderType.THOUGHT -> R.string.sender_type_thought_description
        }

/** Accessibility description for this sender type, localized. */
@Composable
fun SenderType.description(): String = stringResource(id = descriptionRes)

/**
 * Returns the sender types that map to inline expressive tags in the tag-picker.
 * USER and CHARACTER are omitted — they are underlying sender types, not tag options.
 */
fun SenderType.Companion.filterUserInputTypes(): List<SenderType> = listOf(SenderType.NARRATOR, SenderType.ACTION, SenderType.THOUGHT)

/**
 * Returns the [SenderType] whose [tag] matches the given [ExpressiveTag].
 * Used by the input bar to highlight the chip matching the cursor's current tag.
 */
fun SenderType.Companion.senderForTag(expressiveTag: ExpressiveTag): SenderType? =
    SenderType.entries.firstOrNull { it.tag == expressiveTag }
