package com.ilustris.sagai.features.act.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ilustris.sagai.core.utils.emptyString

@Entity
data class Act(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = emptyString(),
    val content: String = emptyString(),
    @ColumnInfo(index = true)
    val sagaId: Int? = null,
)
