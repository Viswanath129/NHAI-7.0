package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AuditLogDao {
    @Insert
    suspend fun insertLog(log: AuditLog)

    @Query("SELECT * FROM audit_logs WHERE isSynced = 0")
    suspend fun getUnsyncedLogs(): List<AuditLog>

    @Query("UPDATE audit_logs SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)
}
