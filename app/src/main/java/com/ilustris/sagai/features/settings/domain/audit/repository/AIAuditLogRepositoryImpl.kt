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

        override fun getRecentLogs(limit: Int): Flow<List<AIAuditLog>> = aiAuditLogDao.getRecentLogs(limit)
    }
