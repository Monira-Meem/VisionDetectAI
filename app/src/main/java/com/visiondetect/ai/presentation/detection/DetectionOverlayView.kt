package com.visiondetect.ai.presentation.detection
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.visiondetect.ai.domain.model.DetectionResult
import kotlin.math.min

class DetectionOverlayView @JvmOverloads constructor(ctx: Context, attrs: AttributeSet? = null, def: Int = 0) : View(ctx, attrs, def) {
    private var detections: List<DetectionResult> = emptyList()
    private var imgW = 1; private var imgH = 1
    var showBoundingBoxes = true; var showLabels = true

    private val boxPaint   = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 4f; strokeCap = Paint.Cap.ROUND }
    private val fillPaint  = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val labelBg    = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val labelText  = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textSize = 36f; typeface = Typeface.DEFAULT_BOLD }

    private val colors = listOf(0xFFFF4081,0xFF00BCD4,0xFF76FF03,0xFFFF6D00,0xFF7C4DFF,
        0xFF00E5FF,0xFFFFEA00,0xFFF50057,0xFF1DE9B6,0xFFFF9100,
        0xFF40C4FF,0xFFEEFF41,0xFFE040FB,0xFF69F0AE,0xFFFF6E40).map { it.toInt() }

    fun updateDetections(results: List<DetectionResult>, w: Int, h: Int) {
        detections = results; imgW = w; imgH = h; postInvalidate()
    }
    fun clearDetections() { detections = emptyList(); postInvalidate() }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (detections.isEmpty() || width == 0) return
        val sx = width.toFloat() / imgW; val sy = height.toFloat() / imgH
        for (d in detections) {
            val col = colors[d.colorIndex % colors.size]
            val r = RectF(d.boundingBox.left * imgW * sx, d.boundingBox.top * imgH * sy,
                          d.boundingBox.right * imgW * sx, d.boundingBox.bottom * imgH * sy)
                .also { it.left = it.left.coerceIn(0f, width.toFloat()); it.top = it.top.coerceIn(0f, height.toFloat())
                        it.right = it.right.coerceIn(0f, width.toFloat()); it.bottom = it.bottom.coerceIn(0f, height.toFloat()) }
            if (showBoundingBoxes) drawBox(canvas, r, col)
            if (showLabels) drawLabel(canvas, r, d, col)
        }
    }

    private fun drawBox(canvas: Canvas, r: RectF, col: Int) {
        fillPaint.color = Color.argb(30, Color.red(col), Color.green(col), Color.blue(col))
        canvas.drawRoundRect(r, 10f, 10f, fillPaint)
        boxPaint.color = col; boxPaint.strokeWidth = 3.5f
        canvas.drawRoundRect(r, 10f, 10f, boxPaint)
        val m = min(r.width(), r.height()) * 0.15f
        boxPaint.strokeWidth = 6f
        canvas.drawLine(r.left, r.top+m, r.left, r.top, boxPaint); canvas.drawLine(r.left, r.top, r.left+m, r.top, boxPaint)
        canvas.drawLine(r.right-m, r.top, r.right, r.top, boxPaint); canvas.drawLine(r.right, r.top, r.right, r.top+m, boxPaint)
        canvas.drawLine(r.left, r.bottom-m, r.left, r.bottom, boxPaint); canvas.drawLine(r.left, r.bottom, r.left+m, r.bottom, boxPaint)
        canvas.drawLine(r.right-m, r.bottom, r.right, r.bottom, boxPaint); canvas.drawLine(r.right, r.bottom, r.right, r.bottom-m, boxPaint)
    }

    private fun drawLabel(canvas: Canvas, r: RectF, d: DetectionResult, col: Int) {
        val txt = "${d.label}  ${d.confidencePercent}%"
        val tw = labelText.measureText(txt); val th = labelText.textSize; val pad = 8f
        val lx = r.left; val ly = (r.top - th - pad * 2 - 2f).coerceAtLeast(0f)
        labelBg.color = col
        canvas.drawRoundRect(RectF(lx, ly, lx + tw + pad * 2, ly + th + pad * 2), 6f, 6f, labelBg)
        canvas.drawText(txt, lx + pad, ly + th + pad, labelText)
    }
}
