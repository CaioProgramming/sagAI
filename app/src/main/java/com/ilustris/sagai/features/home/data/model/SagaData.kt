package com.ilustris.sagai.features.home.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SagaData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val color: String,
    val icon: String?,
    val createdAt: Long,
)
