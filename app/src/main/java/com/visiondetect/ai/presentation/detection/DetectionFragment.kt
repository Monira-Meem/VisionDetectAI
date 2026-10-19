package com.visiondetect.ai.presentation.detection
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import com.google.android.material.snackbar.Snackbar
import com.visiondetect.ai.databinding.FragmentDetectionBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class DetectionFragment : Fragment() {
    private var _b: FragmentDetectionBinding? = null
    private val b get() = _b!!
    private val vm: DetectionViewModel by viewModels()
    private lateinit var camExec: ExecutorService
    private var statsCtr = 0

    private val permLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) startCamera() else Snackbar.make(b.root,"Camera permission denied",Snackbar.LENGTH_LONG).show()
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View { _b = FragmentDetectionBinding.inflate(i,c,false); return b.root }

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)
        camExec = Executors.newSingleThreadExecutor()
        setupUI(); observe(); checkPerm(); vm.startSession()
    }

    private fun setupUI() {
        b.btnPauseResume.setOnClickListener { vm.togglePause() }
        b.btnCapture.setOnClickListener { vm.captureCurrentDetection(); Snackbar.make(b.root,"✓ Saved to history",Snackbar.LENGTH_SHORT).show() }
        b.sliderConfidence.addOnChangeListener { _, v, u -> if (u) { vm.updateConfidenceThreshold(v/100f); b.tvConfidenceValue.text="${v.toInt()}%" } }
        b.switchOverlay.setOnCheckedChangeListener { _, c -> b.overlayView.visibility = if (c) View.VISIBLE else View.INVISIBLE }
        b.btnBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
    }

    private fun observe() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { vm.uiState.collect { handleState(it) } }
                launch { vm.detectionFrame.collect { f ->
                    f ?: return@collect
                    b.overlayView.updateDetections(f.detections, f.imageWidth, f.imageHeight)
                    if (++statsCtr % 5 == 0) {
                        b.tvFps.text = "FPS: ${"%.1f".format(f.fps)}"
                        b.tvInferenceTime.text = "Inference: ${f.inferenceTimeMs}ms"
                        b.tvDetectionCount.text = "Objects: ${f.detections.size}"
                    }
                }}
                launch { vm.isPaused.collect { p ->
                    b.btnPauseResume.text = if (p) "▶ Resume" else "⏸ Pause"
                    b.overlayView.visibility = if (p) View.INVISIBLE else View.VISIBLE
                    b.tvPausedIndicator.visibility = if (p) View.VISIBLE else View.GONE
                }}
            }
        }
    }

    private fun handleState(s: DetectionUiState) = when (s) {
        is DetectionUiState.Initializing -> { b.loadingIndicator.visibility=View.VISIBLE; b.tvStatus.text="Initializing AI..." }
        is DetectionUiState.Ready -> { b.loadingIndicator.visibility=View.GONE; b.tvStatus.text="AI Ready" }
        is DetectionUiState.Detecting -> { b.loadingIndicator.visibility=View.GONE; b.tvStatus.text="Detecting..." }
        is DetectionUiState.Error -> { b.loadingIndicator.visibility=View.GONE; b.tvStatus.text="Error"; Snackbar.make(b.root,s.message,Snackbar.LENGTH_LONG).show() }
        is DetectionUiState.SessionComplete -> {}
    }

    private fun checkPerm() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> startCamera()
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ->
                Snackbar.make(b.root,"Camera needed for detection",Snackbar.LENGTH_INDEFINITE).setAction("Grant"){ permLauncher.launch(Manifest.permission.CAMERA) }.show()
            else -> permLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        ProcessCameraProvider.getInstance(requireContext()).addListener({
            val cp = ProcessCameraProvider.getInstance(requireContext()).get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(b.cameraPreview.surfaceProvider) }
            val analyzer = ImageAnalysis.Builder()
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().also { it.setAnalyzer(camExec) { proxy -> processProxy(proxy) } }
            try { cp.unbindAll(); cp.bindToLifecycle(viewLifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analyzer) }
            catch (e: Exception) { Timber.e(e, "Camera bind failed") }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun processProxy(proxy: ImageProxy) {
        try {
            val bmp = Bitmap.createBitmap(proxy.width, proxy.height, Bitmap.Config.ARGB_8888)
            bmp.copyPixelsFromBuffer(proxy.planes[0].buffer)
            vm.processFrame(bmp, proxy.width, proxy.height)
        } catch (e: Exception) { Timber.e(e, "Proxy error") }
        finally { proxy.close() }
    }

    override fun onDestroyView() { super.onDestroyView(); camExec.shutdown(); _b = null }
}
