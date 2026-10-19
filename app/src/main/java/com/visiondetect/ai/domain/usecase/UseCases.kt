package com.visiondetect.ai.domain.usecase
import com.visiondetect.ai.domain.model.*
import com.visiondetect.ai.domain.repository.DetectionRepository
import com.visiondetect.ai.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetDetectionHistoryUseCase @Inject constructor(private val repo: DetectionRepository) {
    operator fun invoke(): Flow<List<DetectionRecord>> = repo.getDetectionHistory()
}
class SaveDetectionRecordUseCase @Inject constructor(private val repo: DetectionRepository) {
    suspend operator fun invoke(record: DetectionRecord): Result<Long> = runCatching { repo.saveDetectionRecord(record) }
}
class DeleteDetectionRecordUseCase @Inject constructor(private val repo: DetectionRepository) {
    suspend operator fun invoke(id: Long): Result<Unit> = runCatching { repo.deleteDetectionRecord(id) }
}
class ClearDetectionHistoryUseCase @Inject constructor(private val repo: DetectionRepository) {
    suspend operator fun invoke(): Result<Unit> = runCatching { repo.clearAllHistory() }
}
class SearchDetectionsUseCase @Inject constructor(private val repo: DetectionRepository) {
    operator fun invoke(query: String): Flow<List<DetectionRecord>> = repo.searchDetections(query)
}
class GetDetectionAnalyticsUseCase @Inject constructor(private val repo: DetectionRepository) {
    suspend operator fun invoke(): Result<DetectionAnalytics> = runCatching { repo.getAnalytics() }
}
class SaveSessionSummaryUseCase @Inject constructor(private val repo: DetectionRepository) {
    suspend operator fun invoke(summary: SessionSummary): Result<Unit> = runCatching { repo.saveSessionSummary(summary) }
}
class GetSessionSummariesUseCase @Inject constructor(private val repo: DetectionRepository) {
    operator fun invoke(): Flow<List<SessionSummary>> = repo.getSessionSummaries()
}
class UpdateDetectionConfigUseCase @Inject constructor(private val repo: SettingsRepository) {
    suspend operator fun invoke(
        confidenceThreshold: Float? = null, showLabels: Boolean? = null,
        showBoundingBoxes: Boolean? = null, useGpuDelegate: Boolean? = null
    ): Result<Unit> = runCatching {
        val current = repo.getDetectionConfig().first()
        repo.updateDetectionConfig(current.copy(
            confidenceThreshold = confidenceThreshold ?: current.confidenceThreshold,
            showLabels = showLabels ?: current.showLabels,
            showBoundingBoxes = showBoundingBoxes ?: current.showBoundingBoxes,
            useGpuDelegate = useGpuDelegate ?: current.useGpuDelegate
        ))
    }
}
