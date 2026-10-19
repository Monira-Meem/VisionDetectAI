package com.visiondetect.ai.presentation.about

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.visiondetect.ai.databinding.FragmentAboutAiBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutAiFragment : Fragment() {
    private var _b: FragmentAboutAiBinding? = null
    private val b get() = _b!!
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentAboutAiBinding.inflate(i, c, false); return b.root
    }
    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)
        b.tvModelName.text    = "EfficientDet-Lite0 + ML Kit"
        b.tvFramework.text    = "TFLite Task Vision + Google ML Kit"
        b.tvInputSize.text    = "320 × 320 px (auto-scaled)"
        b.tvQuantization.text = "INT8 Quantized"
        b.tvClasses.text      = "90 COCO Classes"
        b.tvModelSize.text    = "4.4 MB"
        b.tvWorkflowDescription.text = """
Dual-engine detection system:

PRIMARY — EfficientDet-Lite0 (TFLite Task Vision):
• Specific COCO labels: person, laptop, cell phone, chair, etc.
• 90 object classes from COCO dataset
• INT8 quantized — fast and lightweight
• TFLite Task API — no GPU delegate needed

FALLBACK — Google ML Kit:
• Used when no model file found
• Broad categories: Person, Vehicle, Plant, etc.
• Fully built-in — no download needed

Pipeline:
1. CameraX frame capture (KEEP_ONLY_LATEST)
2. InputImage/TensorImage conversion
3. Model inference (4 CPU threads)
4. Confidence threshold filter (default 50%)
5. Bounding box overlay rendering
        """.trimIndent()
        b.tvFutureImprovements.text = """
• YOLOv8 Nano — even higher accuracy
• Object tracking with cross-frame IDs
• Custom model training for specific use-cases
• Distance estimation via bounding box size
• Voice announcement of detected objects
• Firebase cloud sync for detection history
• Export as CSV / PDF report
        """.trimIndent()
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
