package com.visiondetect.ai.presentation.detection

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visiondetect.ai.ai.engine.TFLiteObjectDetector
import com.visiondetect.ai.domain.model.*
import com.visiondetect.ai.domain.usecase.SaveDetectionRecordUseCase
import com.visiondetect.ai.domain.usecase.SaveSessionSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DetectionViewModel @Inject constructor(
    private val detector: TFLiteObjectDetector,
    private val saveRecord: SaveDetectionRecordUseCase,
    private val saveSession: SaveSessionSummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetectionUiState>(DetectionUiState.Initializing)
    val uiState: StateFlow<DetectionUiState> = _uiState.asStateFlow()

    private val _frame = MutableStateFlow<DetectionFrame?>(null)
    val detectionFrame: StateFlow<DetectionFrame?> = _frame.asStateFlow()

    private val _paused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _paused.asStateFlow()

    private val _config = MutableStateFlow(DetectionConfig())
    val config: StateFlow<DetectionConfig> = _config.asStateFlow()

    // Session tracking
    private var sessionId        = UUID.randomUUID().toString()
    private var sessionStart     = 0L
    private var totalFrames      = 0
    private var totalDetections  = 0
    private val sessionObjs      = mutableSetOf<String>()
    private val fpsSamples       = mutableListOf<Float>()
    private val inferSamples     = mutableListOf<Float>()

    // FPS
    private var frameCount  = 0
    private var windowStart = 0L
    private var currentFps  = 0f

    private var inferJob: Job? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                detector.initialize()
                _uiState.value = DetectionUiState.Ready
                Timber.d("ML Kit detector ready")
            } catch (e: Exception) {
                _uiState.value = DetectionUiState.Error(e.message ?: "Init failed")
                Timber.e(e, "Detector init failed")
            }
        }
    }

    fun processFrame(bitmap: Bitmap, w: Int, h: Int) {
        if (_paused.value) return
        // Skip if previous frame still processing — prevents crash
        if (inferJob?.isActive == true) return

        inferJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val t       = System.currentTimeMillis()
                val results = detector.detect(bitmap)
                val ms      = System.currentTimeMillis() - t
                val fps     = calcFps()

                _frame.value = DetectionFrame(results, ms, fps, w, h)

                totalFrames++
                totalDetections += results.size
                results.forEach { sessionObjs.add(it.label) }
                fpsSamples.add(fps)
                inferSamples.add(ms.toFloat())

                if (_uiState.value !is DetectionUiState.Detecting) {
                    _uiState.value = DetectionUiState.Detecting
                }
            } catch (e: Exception) {
                Timber.e(e, "Frame processing error")
            }
        }
    }

    fun togglePause() { _paused.value = !_paused.value }

    fun startSession() {
        sessionId    = UUID.randomUUID().toString()
        sessionStart = System.currentTimeMillis()
        totalFrames     = 0
        totalDetections = 0
        sessionObjs.clear()
        fpsSamples.clear()
        inferSamples.clear()
    }

    fun stopSession() {
        val summary = SessionSummary(
            sessionId    = sessionId,
            startTime    = sessionStart,
            endTime      = System.currentTimeMillis(),
            totalFrames      = totalFrames,
            totalDetections  = totalDetections,
            uniqueObjects    = sessionObjs.toList(),
            averageFps       = fpsSamples.average().toFloat().takeIf { fpsSamples.isNotEmpty() } ?: 0f,
            averageInferenceTime = inferSamples.average().toFloat().takeIf { inferSamples.isNotEmpty() } ?: 0f,
            topDetectedObject    = sessionObjs.firstOrNull()
        )
        viewModelScope.launch(Dispatchers.IO) { saveSession(summary) }
        _uiState.value = DetectionUiState.SessionComplete(summary)
    }

    fun captureCurrentDetection() {
        val f = _frame.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            saveRecord(DetectionRecord(
                timestamp        = System.currentTimeMillis(),
                detectedObjects  = f.detections.map { it.label },
                confidenceScores = f.detections.map { it.confidence },
                inferenceTimeMs  = f.inferenceTimeMs,
                fps              = f.fps,
                sessionId        = sessionId,
                objectCount      = f.detections.size
            ))
        }
    }

    fun updateConfidenceThreshold(v: Float) {
        _config.value = _config.value.copy(confidenceThreshold = v)
        detector.updateConfig(_config.value)
    }

    private fun calcFps(): Float {
        val now = System.currentTimeMillis()
        frameCount++
        if (windowStart == 0L) windowStart = now
        if (now - windowStart >= 1000L) {
            currentFps  = frameCount * 1000f / (now - windowStart)
            frameCount  = 0
            windowStart = now
        }
        return currentFps
    }

    override fun onCleared() {
        super.onCleared()
        detector.close()
        inferJob?.cancel()
    }
}

sealed class DetectionUiState {
    object Initializing : DetectionUiState()
    object Ready        : DetectionUiState()
    object Detecting    : DetectionUiState()
    data class Error(val message: String) : DetectionUiState()
    data class SessionComplete(val summary: SessionSummary) : DetectionUiState()
}
