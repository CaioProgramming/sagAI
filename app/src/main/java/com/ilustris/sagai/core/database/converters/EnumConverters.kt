package com.ilustris.sagai.core.database.converters

import MessageStatus
import androidx.room.TypeConverter
import com.ilustris.sagai.features.saga.chat.data.model.EmotionalTone
import com.ilustris.sagai.features.saga.chat.data.model.SenderType

/**
 * Safe Room TypeConverters for enum types that the AI or DB inspector might corrupt.
 *
 * The root cause of data loss: Room stores enums as their .name() string by default,
 * and throws IllegalArgumentException if the stored value doesn't match any enum constant.
 * If anyone renames an enum value or corrupts it via DB Inspector, the whole Flow crashes.
 *
 * This converter explicitly maps strings → enums with a safe fallback, so a bad value
 * results in a default state rather than an app crash.
 */
object EnumConverters {
    @TypeConverter
    @JvmStatic
    fun senderTypeToString(value: SenderType?): String? = value?.name

    @TypeConverter
    @JvmStatic
    fun stringToSenderType(value: String?): SenderType? = if (value == null) null else SenderType.fromString(value)

    @TypeConverter
    @JvmStatic
    fun messageStatusToString(value: MessageStatus?): String? = value?.name

    @TypeConverter
    @JvmStatic
    fun stringToMessageStatus(value: String?): MessageStatus? =
        if (value == null) {
            null
        } else {
            runCatching { MessageStatus.valueOf(value) }.getOrDefault(MessageStatus.OK)
        }

    @TypeConverter
    @JvmStatic
    fun emotionalToneToString(value: EmotionalTone?): String? = value?.name

    @TypeConverter
    @JvmStatic
    fun stringToEmotionalTone(value: String?): EmotionalTone? =
        if (value == null) {
            null
        } else {
            EmotionalTone.getTone(value)
        }
}
