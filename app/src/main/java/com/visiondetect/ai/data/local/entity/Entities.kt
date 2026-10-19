package com.visiondetect.ai.data.local.entity
import androidx.room.*

@Entity(tableName = "detection_records")
@TypeConverters(DetectionConverters::class)
data class DetectionRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long, val detectedObjects: List<String>,
    val confidenceScores: List<Float>, val inferenceTimeMs: Long,
    val fps: Float, val imagePath: String?, val sessionId: String, val objectCount: Int
)

@Entity(tableName = "session_summaries")
@TypeConverters(DetectionConverters::class)
data class SessionSummaryEntity(
    @PrimaryKey val sessionId: String,
    val startTime: Long, val endTime: Long, val totalFrames: Int,
    val totalDetections: Int, val uniqueObjects: List<String>,
    val averageFps: Float, val averageInferenceTime: Float, val topDetectedObject: String?
)

class DetectionConverters {
    @TypeConverter fun fromStringList(v: List<String>): String = v.joinToString("|") { it.replace("|","\\|") }
    @TypeConverter fun toStringList(v: String): List<String> = if (v.isBlank()) emptyList() else v.split(Regex("(?<!\\\\)\\|")).map { it.replace("\\|","|") }
    @TypeConverter fun fromFloatList(v: List<Float>): String = v.joinToString(",")
    @TypeConverter fun toFloatList(v: String): List<Float> = if (v.isBlank()) emptyList() else v.split(",").mapNotNull { it.trim().toFloatOrNull() }
}
