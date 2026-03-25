package com.ilustris.sagai.features.settings.domain.audit.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.database.model.AIAuditLog
import kotlinx.coroutines.flow.Flow

interface AIAuditLogUseCase {
    suspend fun clearLogs(): RequestResult<Unit>

    suspend fun getRecentLogs(limit: Int = 100): Flow<List<AIAuditLog>>

    suspend fun generateSuggestion(log: AIAuditLog): RequestResult<Unit>

    suspend fun generateGlobalInsight(logs: List<AIAuditLog>): Flow<RequestResult<String>>
}
