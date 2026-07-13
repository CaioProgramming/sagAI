package com.ilustris.sagai.features.settings.domain.audit.repository

import com.ilustris.sagai.core.database.model.AIAuditLog
import kotlinx.coroutines.flow.Flow

data class AIAuditLogFilters(
    val status: String? = null,
    val dataType: String? = null,
    val model: String? = null,
)

interface AIAuditLogRepository {
    suspend fun insertLog(log: AIAuditLog)

    suspend fun updateLog(log: AIAuditLog)

    suspend fun clearLogs()

    suspend fun getLogsPage(
        filters: AIAuditLogFilters,
        limit: Int,
        offset: Int,
    ): List<AIAuditLog>

    fun observeLogCount(): Flow<Int>

    suspend fun getDistinctDataTypes(): List<String>

    suspend fun getDistinctModels(): List<String>

    suspend fun getRecentLogsForInsight(limit: Int): List<AIAuditLog>
}
