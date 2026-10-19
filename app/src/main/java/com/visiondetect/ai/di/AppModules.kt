package com.visiondetect.ai.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.visiondetect.ai.ai.engine.TFLiteObjectDetector
import com.visiondetect.ai.data.local.dao.DetectionRecordDao
import com.visiondetect.ai.data.local.dao.SessionSummaryDao
import com.visiondetect.ai.data.local.database.VisionDetectDatabase
import com.visiondetect.ai.data.repository.DetectionRepositoryImpl
import com.visiondetect.ai.data.repository.SettingsRepositoryImpl
import com.visiondetect.ai.domain.model.DetectionConfig
import com.visiondetect.ai.domain.repository.DetectionRepository
import com.visiondetect.ai.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences>
        by preferencesDataStore(name = "visiondetect_prefs")

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): VisionDetectDatabase =
        Room.databaseBuilder(ctx, VisionDetectDatabase::class.java, VisionDetectDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration().build()

    @Provides fun provideRecordDao(db: VisionDetectDatabase): DetectionRecordDao  = db.detectionRecordDao()
    @Provides fun provideSessionDao(db: VisionDetectDatabase): SessionSummaryDao = db.sessionSummaryDao()

    @Provides @Singleton
    fun provideDataStore(@ApplicationContext ctx: Context): DataStore<Preferences> = ctx.dataStore
}

@Module
@InstallIn(SingletonComponent::class)
object AiModule {
    @Provides @Singleton
    fun provideDetector(@ApplicationContext ctx: Context): TFLiteObjectDetector =
        TFLiteObjectDetector(ctx, DetectionConfig())
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindDetectionRepo(impl: DetectionRepositoryImpl): DetectionRepository
    @Binds @Singleton abstract fun bindSettingsRepo(impl: SettingsRepositoryImpl):   SettingsRepository
}
