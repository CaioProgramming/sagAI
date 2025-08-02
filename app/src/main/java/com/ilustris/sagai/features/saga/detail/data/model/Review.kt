package com.ilustris.sagai.features.saga.detail.data.model

import androidx.room.ColumnInfo

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
