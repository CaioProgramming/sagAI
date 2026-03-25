package com.ilustris.sagai.features.settings.domain.audit.repository

import com.ilustris.sagai.core.database.model.AIAuditLog
import kotlinx.coroutines.flow.Flow

interface AIAuditLogRepository {
    suspend fun insertLog(log: AIAuditLog)

    suspend fun updateLog(log: AIAuditLog)

    suspend fun clearLogs()

    fun getRecentLogs(limit: Int = 100): Flow<List<AIAuditLog>>
}
