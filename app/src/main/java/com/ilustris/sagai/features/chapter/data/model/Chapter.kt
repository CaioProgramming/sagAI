package com.ilustris.sagai.features.chapter.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Chapter(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val sagaId: Int,
    val title: String,
    val overview: String,
    val messageReference: Int,
    val coverImage: String,
)
