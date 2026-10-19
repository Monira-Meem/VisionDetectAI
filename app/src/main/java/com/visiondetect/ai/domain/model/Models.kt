package com.visiondetect.ai.domain.model

import android.graphics.RectF

data class DetectionResult(
    val id: Int,
    val label: String,
    val confidence: Float,
    val boundingBox: RectF,
    val colorIndex: Int = 0
) {
    val confidencePercent: Int
        get() = (confidence * 100).toInt().coerceIn(0, 100)
}

data class DetectionFrame(
    val detections: List<DetectionResult>,
    val inferenceTimeMs: Long,
    val fps: Float,
    val imageWidth: Int,
    val imageHeight: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class DetectionRecord(
    val id: Long = 0,
    val timestamp: Long,
    val detectedObjects: List<String>,
    val confidenceScores: List<Float>,
    val inferenceTimeMs: Long,
    val fps: Float,
    val imagePath: String? = null,
    val sessionId: String,
    val objectCount: Int
)

data class DetectionAnalytics(
    val totalDetections: Int,
    val uniqueObjects: Map<String, Int>,
    val averageConfidence: Float,
    val averageInferenceTime: Float,
    val averageFps: Float,
    val sessionCount: Int,
    val topObjects: List<Pair<String, Int>>
)

data class DetectionConfig(
    val confidenceThreshold: Float = 0.5f,
    val iouThreshold: Float        = 0.5f,
    val maxDetections: Int         = 10,
    val showLabels: Boolean        = true,
    val showBoundingBoxes: Boolean = true,
    val useGpuDelegate: Boolean    = false,
    val numThreads: Int            = 4
)

data class SessionSummary(
    val sessionId: String,
    val startTime: Long,
    val endTime: Long,
    val totalFrames: Int,
    val totalDetections: Int,
    val uniqueObjects: List<String>,
    val averageFps: Float,
    val averageInferenceTime: Float,
    val topDetectedObject: String?
)
