package com.ilustris.sagai.core.database.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "ai_audit_logs")
data class AIAuditLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val model: String,
    val blueprintKey: String? = null,
    val dataType: String,
    val status: String,
    val reasoning: String? = null,
    val rawResponse: String? = null,
    val errorMessage: String? = null,
    val suggestion: String? = null,
    val responseTime: Long = 0,
)
