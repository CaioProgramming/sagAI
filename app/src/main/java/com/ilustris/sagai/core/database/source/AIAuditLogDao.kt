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

    @Query("SELECT * FROM ai_audit_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): Flow<List<AIAuditLog>>

    @Query("DELETE FROM ai_audit_logs")
    suspend fun clearLogs()
}
