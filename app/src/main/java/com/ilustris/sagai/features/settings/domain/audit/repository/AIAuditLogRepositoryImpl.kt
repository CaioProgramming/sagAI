package com.ilustris.sagai.features.settings.domain.audit.repository

import com.ilustris.sagai.core.database.model.AIAuditLog
import com.ilustris.sagai.core.database.source.AIAuditLogDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AIAuditLogRepositoryImpl
    @Inject
    constructor(
        private val aiAuditLogDao: AIAuditLogDao,
    ) : AIAuditLogRepository {
        override suspend fun insertLog(log: AIAuditLog) {
            aiAuditLogDao.insertLog(log)
        }

        override suspend fun updateLog(log: AIAuditLog) {
            aiAuditLogDao.updateLog(log)
        }

        override suspend fun clearLogs() {
            aiAuditLogDao.clearLogs()
        }

        override suspend fun getLogsPage(
            filters: AIAuditLogFilters,
            limit: Int,
            offset: Int,
        ): List<AIAuditLog> =
            aiAuditLogDao.getLogsPage(
                status = filters.status,
                dataType = filters.dataType,
                model = filters.model,
                limit = limit,
                offset = offset,
            )

        override fun observeLogCount(): Flow<Int> = aiAuditLogDao.observeLogCount()

        override suspend fun getDistinctDataTypes(): List<String> = aiAuditLogDao.getDistinctDataTypes()

        override suspend fun getDistinctModels(): List<String> = aiAuditLogDao.getDistinctModels()

        override suspend fun getRecentLogsForInsight(limit: Int): List<AIAuditLog> = aiAuditLogDao.getRecentLogsForInsight(limit)
    }
