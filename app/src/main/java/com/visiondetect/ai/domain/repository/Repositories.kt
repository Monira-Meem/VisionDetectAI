package com.visiondetect.ai.domain.repository
import com.visiondetect.ai.domain.model.*
import kotlinx.coroutines.flow.Flow

interface DetectionRepository {
    fun getDetectionHistory(): Flow<List<DetectionRecord>>
    suspend fun getDetectionById(id: Long): DetectionRecord?
    suspend fun saveDetectionRecord(record: DetectionRecord): Long
    suspend fun deleteDetectionRecord(id: Long)
    suspend fun clearAllHistory()
    fun searchDetections(query: String): Flow<List<DetectionRecord>>
    fun getDetectionsByDateRange(startTime: Long, endTime: Long): Flow<List<DetectionRecord>>
    suspend fun getAnalytics(): DetectionAnalytics
    suspend fun saveSessionSummary(summary: SessionSummary)
    fun getSessionSummaries(): Flow<List<SessionSummary>>
}

interface SettingsRepository {
    fun getDetectionConfig(): Flow<DetectionConfig>
    suspend fun updateDetectionConfig(config: DetectionConfig)
    fun isDarkModeEnabled(): Flow<Boolean>
    suspend fun setDarkMode(enabled: Boolean)
    fun isPerformanceModeEnabled(): Flow<Boolean>
    suspend fun setPerformanceMode(enabled: Boolean)
}
