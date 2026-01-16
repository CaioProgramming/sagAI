package com.ilustris.sagai.features.saga.detail.data.model

import androidx.room.ColumnInfo

/**
 * Data model for the Saga Review.
 * Now evolved to support "Saga Wrapped" Story Cards.
 * Mapping:
 * - [introduction]: "The Hook" / Title Card text.
 * - [playstyle]: "The Vibe" / Emotional & Playstyle summary.
 * - [topCharacters]: "The Cast" / Key relationships caption.
 * - [actsInsight]: "The Journey" / Key moments caption.
 * - [conclusion]: "The Legacy" / Final send-off.
 */
data class Review(
    @ColumnInfo(defaultValue = "")
    val introduction: String = "",
    @ColumnInfo(defaultValue = "")
    val playstyle: String = "",
    @ColumnInfo(defaultValue = "")
    val topCharacters: String = "",
    @ColumnInfo(defaultValue = "")
    val actsInsight: String = "",
    @ColumnInfo(defaultValue = "")
    val conclusion: String = "",
)
