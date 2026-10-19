package com.visiondetect.ai.ai.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.visiondetect.ai.domain.model.DetectionConfig
import com.visiondetect.ai.domain.model.DetectionResult
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.ObjectDetector as TFObjectDetector
import java.io.FileInputStream
import java.nio.channels.FileChannel
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Dual-engine detector:
 *
 * PRIMARY: TFLite Task Vision with EfficientDet-Lite0
 *   → Gives specific COCO labels: "person", "laptop", "cell phone", etc.
 *   → Uses tensorflow-lite-task-vision (no GPU delegate, no 16KB issue)
 *
 * FALLBACK: ML Kit Object Detection
 *   → Used if no .tflite model found in assets
 *   → Gives broad categories: "Person", "Food", "Plant", etc.
 */
class TFLiteObjectDetector(
    private val context: Context,
    private var config: DetectionConfig
) {
    companion object {
        private const val TAG = "VisionDetector"

        // Model file candidates in assets folder
        private val MODEL_CANDIDATES = listOf(
            "efficientdet_lite0.tflite",
            "ssd_mobilenet_v1.tflite",
            "detect.tflite",
            "model.tflite"
        )
    }

    // TFLite Task API detector (specific labels)
    private var tfDetector: TFObjectDetector? = null

    // ML Kit detector (broad categories — fallback)
    private var mlKitDetector: ObjectDetector? = null

    private var usingTFLite  = false
    private var usingMLKit   = false
    private var initialized  = false

    fun initialize() {
        val assets = context.assets.list("") ?: arrayOf()
        Log.d(TAG, "Assets: ${assets.toList()}")

        // Try TFLite Task API first (gives specific labels)
        val modelFile = MODEL_CANDIDATES.firstOrNull { assets.contains(it) }
        if (modelFile != null) {
            tryInitTFLite(modelFile)
        }

        // If TFLite failed or no model, use ML Kit
        if (!usingTFLite) {
            tryInitMLKit()
        }

        initialized = usingTFLite || usingMLKit
        Log.d(TAG, "Init done. TFLite=$usingTFLite MLKit=$usingMLKit")
    }

    private fun tryInitTFLite(modelFile: String) {
        try {
            Log.d(TAG, "Loading TFLite model: $modelFile")
            val options = TFObjectDetector.ObjectDetectorOptions.builder()
                .setMaxResults(10)
                .setScoreThreshold(0.3f)
                .setNumThreads(4)
                .build()
            tfDetector  = TFObjectDetector.createFromFileAndOptions(context, modelFile, options)
            usingTFLite = true
            Log.d(TAG, "✅ TFLite Task API ready — specific COCO labels active")
        } catch (e: Exception) {
            Log.w(TAG, "TFLite init failed: ${e.message} — will use ML Kit")
            usingTFLite = false
        }
    }

    private fun tryInitMLKit() {
        try {
            Log.d(TAG, "Initializing ML Kit fallback...")
            val options = ObjectDetectorOptions.Builder()
                .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
                .enableMultipleObjects()
                .enableClassification()
                .build()
            mlKitDetector = ObjectDetection.getClient(options)
            usingMLKit    = true
            Log.d(TAG, "✅ ML Kit ready — broad category labels active")
        } catch (e: Exception) {
            Log.e(TAG, "ML Kit init failed: ${e.message}")
            usingMLKit = false
        }
    }

    fun updateConfig(newConfig: DetectionConfig) {
        config = newConfig
        // Update TFLite threshold if using TFLite
        // (Task API requires re-init for threshold change — handled via filter)
    }

    fun detect(bitmap: Bitmap): List<DetectionResult> {
        if (!initialized) return emptyList()

        return if (usingTFLite && tfDetector != null) {
            detectWithTFLite(bitmap)
        } else {
            detectWithMLKit(bitmap)
        }
    }

    /**
     * TFLite Task API detection — gives specific COCO labels.
     * e.g. "person", "laptop", "cell phone", "chair", "bottle"
     */
    private fun detectWithTFLite(bitmap: Bitmap): List<DetectionResult> {
        return try {
            val tensorImage = TensorImage.fromBitmap(bitmap)
            val detections  = tfDetector!!.detect(tensorImage)

            val imgW = bitmap.width.toFloat()
            val imgH = bitmap.height.toFloat()
            val results = mutableListOf<DetectionResult>()

            for ((idx, det) in detections.withIndex()) {
                val score = det.categories.firstOrNull()?.score ?: 0f
                val label = det.categories.firstOrNull()?.label ?: "object"

                Log.d(TAG, "TFLite: $label @ ${(score*100).toInt()}%")

                if (score >= config.confidenceThreshold) {
                    // TFLite Task API bounding box is already in pixel coords
                    val box = det.boundingBox
                    results.add(DetectionResult(
                        id    = idx,
                        label = label,
                        confidence  = score,
                        boundingBox = RectF(
                            box.left   / imgW,
                            box.top    / imgH,
                            box.right  / imgW,
                            box.bottom / imgH
                        ),
                        colorIndex = idx % 20
                    ))
                }
            }

            results.sortedByDescending { it.confidence }.take(config.maxDetections)
        } catch (e: Exception) {
            Log.e(TAG, "TFLite detect error: ${e.message}")
            emptyList()
        }
    }

    /**
     * ML Kit detection fallback — gives broad categories.
     * e.g. "Person", "Home good", "Food", "Plant", "Vehicle"
     */
    private fun detectWithMLKit(bitmap: Bitmap): List<DetectionResult> {
        val results = mutableListOf<DetectionResult>()
        val latch   = CountDownLatch(1)

        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            mlKitDetector!!.process(image)
                .addOnSuccessListener { objects ->
                    val imgW = bitmap.width.toFloat()
                    val imgH = bitmap.height.toFloat()
                    var idx  = 0
                    for (obj in objects) {
                        val label      = obj.labels.maxByOrNull { it.confidence }?.text ?: "object"
                        val confidence = obj.labels.maxOfOrNull { it.confidence } ?: 0.6f
                        if (confidence >= config.confidenceThreshold) {
                            results.add(DetectionResult(
                                id    = idx++,
                                label = label,
                                confidence  = confidence,
                                boundingBox = RectF(
                                    obj.boundingBox.left   / imgW,
                                    obj.boundingBox.top    / imgH,
                                    obj.boundingBox.right  / imgW,
                                    obj.boundingBox.bottom / imgH
                                ),
                                colorIndex = (obj.trackingId ?: idx) % 20
                            ))
                        }
                    }
                    latch.countDown()
                }
                .addOnFailureListener { latch.countDown() }

            latch.await(500, TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            Log.e(TAG, "ML Kit detect error: ${e.message}")
        }

        return results.sortedByDescending { it.confidence }.take(config.maxDetections)
    }

    fun close() {
        tfDetector?.close()
        mlKitDetector?.close()
        tfDetector    = null
        mlKitDetector = null
        initialized   = false
        Log.d(TAG, "Detector closed")
    }

    fun isUsingTFLite()  = usingTFLite
    fun isUsingMLKit()   = usingMLKit
    fun isUsingDemoMode() = !initialized
}
