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
    val usedTools: List<String>? = null,
    val safetyStatus: String? = null,
    /** The final merged system instruction block sent to the Gemini API `system_instruction` field. */
    val systemInstruction: String? = null,
    /** JSON representation of the variables map sent to [com.ilustris.sagai.core.ai.services.PromptService.buildSplitBlueprint]. */
    val sentVariables: String? = null,
)

data class AIStats(
    val blueprintKey: String?,
    val sentVariables: Map<String, String>?,
    val missingVariables: List<String>?,
    val systemInstructions: String,
)
