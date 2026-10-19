package com.visiondetect.ai.data.local.dao
import androidx.room.*
import com.visiondetect.ai.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DetectionRecordDao {
    @Query("SELECT * FROM detection_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<DetectionRecordEntity>>
    @Query("SELECT * FROM detection_records WHERE id = :id")
    suspend fun getRecordById(id: Long): DetectionRecordEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: DetectionRecordEntity): Long
    @Query("DELETE FROM detection_records WHERE id = :id")
    suspend fun deleteRecord(id: Long)
    @Query("DELETE FROM detection_records")
    suspend fun clearAll()
    @Query("SELECT * FROM detection_records WHERE detectedObjects LIKE '%' || :q || '%' ORDER BY timestamp DESC")
    fun searchRecords(q: String): Flow<List<DetectionRecordEntity>>
    @Query("SELECT * FROM detection_records WHERE timestamp BETWEEN :s AND :e ORDER BY timestamp DESC")
    fun getRecordsByDateRange(s: Long, e: Long): Flow<List<DetectionRecordEntity>>
    @Query("SELECT * FROM detection_records ORDER BY timestamp DESC LIMIT 1000")
    suspend fun getRecordsForAnalytics(): List<DetectionRecordEntity>
    @Query("SELECT DISTINCT sessionId FROM detection_records ORDER BY timestamp DESC")
    suspend fun getDistinctSessions(): List<String>
}

@Dao
interface SessionSummaryDao {
    @Query("SELECT * FROM session_summaries ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<SessionSummaryEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(s: SessionSummaryEntity)
    @Query("SELECT COUNT(*) FROM session_summaries")
    suspend fun getSessionCount(): Int
}
