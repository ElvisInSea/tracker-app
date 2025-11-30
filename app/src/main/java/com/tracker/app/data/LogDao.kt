package com.tracker.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Query("SELECT * FROM logs WHERE taskId = :taskId ORDER BY timestamp DESC")
    fun getLogsByTaskId(taskId: String): Flow<List<Log>>
    
    @Query("SELECT * FROM logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<Log>>
    
    @Query("SELECT * FROM logs WHERE taskId = :taskId AND timestamp >= :startTime AND timestamp < :endTime ORDER BY timestamp DESC")
    fun getLogsByTaskIdAndDateRange(taskId: String, startTime: Long, endTime: Long): Flow<List<Log>>
    
    @Query("SELECT * FROM logs WHERE timestamp >= :startTime AND timestamp < :endTime ORDER BY timestamp ASC")
    fun getLogsByDateRange(startTime: Long, endTime: Long): Flow<List<Log>>
    
    @Insert
    suspend fun insertLog(log: Log)
    
    @Delete
    suspend fun deleteLog(log: Log)
    
    @Query("DELETE FROM logs WHERE taskId = :taskId")
    suspend fun deleteLogsByTaskId(taskId: String)
}

