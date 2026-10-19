package com.visiondetect.ai.data.local.database
import androidx.room.*
import com.visiondetect.ai.data.local.dao.*
import com.visiondetect.ai.data.local.entity.*

@Database(entities = [DetectionRecordEntity::class, SessionSummaryEntity::class], version = 1, exportSchema = false)
@TypeConverters(DetectionConverters::class)
abstract class VisionDetectDatabase : RoomDatabase() {
    abstract fun detectionRecordDao(): DetectionRecordDao
    abstract fun sessionSummaryDao(): SessionSummaryDao
    companion object { const val DATABASE_NAME = "visiondetect_db" }
}
