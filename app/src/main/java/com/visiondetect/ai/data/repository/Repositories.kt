package com.visiondetect.ai.data.repository
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.visiondetect.ai.data.local.dao.*
import com.visiondetect.ai.data.local.entity.*
import com.visiondetect.ai.domain.model.*
import com.visiondetect.ai.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DetectionRepositoryImpl @Inject constructor(
    private val recordDao: DetectionRecordDao,
    private val sessionDao: SessionSummaryDao
) : DetectionRepository {
    override fun getDetectionHistory() = recordDao.getAllRecords().map { it.map { e -> e.toDomain() } }
    override suspend fun getDetectionById(id: Long) = recordDao.getRecordById(id)?.toDomain()
    override suspend fun saveDetectionRecord(record: DetectionRecord) = recordDao.insertRecord(record.toEntity())
    override suspend fun deleteDetectionRecord(id: Long) = recordDao.deleteRecord(id)
    override suspend fun clearAllHistory() = recordDao.clearAll()
    override fun searchDetections(query: String) = recordDao.searchRecords(query).map { it.map { e -> e.toDomain() } }
    override fun getDetectionsByDateRange(s: Long, e: Long) = recordDao.getRecordsByDateRange(s, e).map { it.map { x -> x.toDomain() } }
    override suspend fun getAnalytics(): DetectionAnalytics {
        val records = recordDao.getRecordsForAnalytics()
        if (records.isEmpty()) return DetectionAnalytics(0, emptyMap(), 0f, 0f, 0f, 0, emptyList())
        val allObjs = records.flatMap { it.detectedObjects }
        val objCounts = allObjs.groupingBy { it }.eachCount()
        val allConf = records.flatMap { it.confidenceScores }
        return DetectionAnalytics(
            totalDetections = records.sumOf { it.objectCount },
            uniqueObjects = objCounts,
            averageConfidence = if (allConf.isEmpty()) 0f else allConf.average().toFloat(),
            averageInferenceTime = records.map { it.inferenceTimeMs }.average().toFloat(),
            averageFps = records.map { it.fps }.average().toFloat(),
            sessionCount = recordDao.getDistinctSessions().size,
            topObjects = objCounts.entries.sortedByDescending { it.value }.take(10).map { it.key to it.value }
        )
    }
    override suspend fun saveSessionSummary(summary: SessionSummary) = sessionDao.insertSession(summary.toEntity())
    override fun getSessionSummaries() = sessionDao.getAllSessions().map { it.map { e -> e.toDomain() } }

    private fun DetectionRecordEntity.toDomain() = DetectionRecord(id, timestamp, detectedObjects, confidenceScores, inferenceTimeMs, fps, imagePath, sessionId, objectCount)
    private fun DetectionRecord.toEntity() = DetectionRecordEntity(id, timestamp, detectedObjects, confidenceScores, inferenceTimeMs, fps, imagePath, sessionId, objectCount)
    private fun SessionSummaryEntity.toDomain() = SessionSummary(sessionId, startTime, endTime, totalFrames, totalDetections, uniqueObjects, averageFps, averageInferenceTime, topDetectedObject)
    private fun SessionSummary.toEntity() = SessionSummaryEntity(sessionId, startTime, endTime, totalFrames, totalDetections, uniqueObjects, averageFps, averageInferenceTime, topDetectedObject)
}

class SettingsRepositoryImpl @Inject constructor(private val dataStore: DataStore<Preferences>) : SettingsRepository {
    private object Keys {
        val CONFIDENCE = floatPreferencesKey("confidence_threshold")
        val SHOW_LABELS = booleanPreferencesKey("show_labels")
        val SHOW_BOXES = booleanPreferencesKey("show_boxes")
        val USE_GPU = booleanPreferencesKey("use_gpu")
        val NUM_THREADS = intPreferencesKey("num_threads")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val PERF_MODE = booleanPreferencesKey("perf_mode")
    }
    override fun getDetectionConfig(): Flow<DetectionConfig> = dataStore.data.map { p ->
        DetectionConfig(p[Keys.CONFIDENCE] ?: 0.5f, showLabels = p[Keys.SHOW_LABELS] ?: true, showBoundingBoxes = p[Keys.SHOW_BOXES] ?: true, useGpuDelegate = p[Keys.USE_GPU] ?: false, numThreads = p[Keys.NUM_THREADS] ?: 4)
    }
    override suspend fun updateDetectionConfig(config: DetectionConfig) {
        dataStore.edit { p -> p[Keys.CONFIDENCE] = config.confidenceThreshold; p[Keys.SHOW_LABELS] = config.showLabels; p[Keys.SHOW_BOXES] = config.showBoundingBoxes; p[Keys.USE_GPU] = config.useGpuDelegate; p[Keys.NUM_THREADS] = config.numThreads }
    }
    override fun isDarkModeEnabled(): Flow<Boolean> = dataStore.data.map { it[Keys.DARK_MODE] ?: false }
    override suspend fun setDarkMode(enabled: Boolean) { dataStore.edit { it[Keys.DARK_MODE] = enabled } }
    override fun isPerformanceModeEnabled(): Flow<Boolean> = dataStore.data.map { it[Keys.PERF_MODE] ?: false }
    override suspend fun setPerformanceMode(enabled: Boolean) { dataStore.edit { it[Keys.PERF_MODE] = enabled } }
}
