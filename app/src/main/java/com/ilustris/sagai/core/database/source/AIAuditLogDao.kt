package com.ilustris.sagai.core.database.source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ilustris.sagai.core.database.model.AIAuditLog
import kotlinx.coroutines.flow.Flow

@Dao
interface AIAuditLogDao {
    @Insert
    suspend fun insertLog(log: AIAuditLog)

    @Update
    suspend fun updateLog(log: AIAuditLog)

    @Query("DELETE FROM ai_audit_logs")
    suspend fun clearLogs()

    @Query(
        """
        SELECT * FROM ai_audit_logs
        WHERE (:status IS NULL OR status = :status)
          AND (:dataType IS NULL OR dataType = :dataType)
          AND (:model IS NULL OR model = :model)
        ORDER BY timestamp DESC
        LIMIT :limit OFFSET :offset
        """,
    )
    suspend fun getLogsPage(
        status: String?,
        dataType: String?,
        model: String?,
        limit: Int,
        offset: Int,
    ): List<AIAuditLog>

    @Query("SELECT COUNT(*) FROM ai_audit_logs")
    fun observeLogCount(): Flow<Int>

    @Query("SELECT DISTINCT dataType FROM ai_audit_logs ORDER BY dataType ASC")
    suspend fun getDistinctDataTypes(): List<String>

    @Query("SELECT DISTINCT model FROM ai_audit_logs ORDER BY model ASC")
    suspend fun getDistinctModels(): List<String>

    @Query("SELECT * FROM ai_audit_logs ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentLogsForInsight(limit: Int): List<AIAuditLog>
}
