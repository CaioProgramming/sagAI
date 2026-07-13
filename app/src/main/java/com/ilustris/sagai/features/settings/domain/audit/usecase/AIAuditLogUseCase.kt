package com.ilustris.sagai.features.settings.domain.audit.usecase

import com.ilustris.sagai.core.data.RequestResult
import com.ilustris.sagai.core.database.model.AIAuditLog
import com.ilustris.sagai.features.settings.domain.audit.repository.AIAuditLogFilters
import kotlinx.coroutines.flow.Flow

interface AIAuditLogUseCase {
    suspend fun clearLogs(): RequestResult<Unit>

    suspend fun getLogsPage(
        filters: AIAuditLogFilters,
        limit: Int,
        offset: Int,
    ): RequestResult<List<AIAuditLog>>

    fun observeLogCount(): Flow<Int>

    suspend fun getDistinctDataTypes(): RequestResult<List<String>>

    suspend fun getDistinctModels(): RequestResult<List<String>>

    suspend fun getRecentLogsForInsight(limit: Int): RequestResult<List<AIAuditLog>>

    suspend fun generateSuggestion(log: AIAuditLog): RequestResult<Unit>

    suspend fun generateGlobalInsight(logs: List<AIAuditLog>): RequestResult<String>
}
