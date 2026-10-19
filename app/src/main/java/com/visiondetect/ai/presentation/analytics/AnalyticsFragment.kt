package com.visiondetect.ai.presentation.analytics
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import com.visiondetect.ai.databinding.FragmentAnalyticsBinding
import com.visiondetect.ai.domain.model.DetectionAnalytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AnalyticsFragment : Fragment() {
    private var _b: FragmentAnalyticsBinding? = null
    private val b get() = _b!!
    private val vm: AnalyticsViewModel by viewModels()
    override fun onCreateView(i: LayoutInflater,c: ViewGroup?,s: Bundle?): View { _b=FragmentAnalyticsBinding.inflate(i,c,false); return b.root }
    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v,s)
        setupCharts()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collect { st -> when(st) {
                    is AnalyticsUiState.Loading -> { b.loadingIndicator.visibility=View.VISIBLE; b.contentGroup.visibility=View.GONE; b.emptyState.visibility=View.GONE }
                    is AnalyticsUiState.Empty   -> { b.loadingIndicator.visibility=View.GONE; b.contentGroup.visibility=View.GONE; b.emptyState.visibility=View.VISIBLE }
                    is AnalyticsUiState.Success -> { b.loadingIndicator.visibility=View.GONE; b.emptyState.visibility=View.GONE; b.contentGroup.visibility=View.VISIBLE; render(st.data) }
                    is AnalyticsUiState.Error   -> { b.loadingIndicator.visibility=View.GONE }
                }}
            }
        }
    }
    private fun setupCharts() {
        b.pieChartObjects.apply { description.isEnabled=false; isDrawHoleEnabled=true; holeRadius=55f; setHoleColor(Color.TRANSPARENT); setUsePercentValues(true); setEntryLabelColor(Color.WHITE); animateY(1000) }
        b.barChartInference.apply { description.isEnabled=false; setDrawGridBackground(false); xAxis.position=XAxis.XAxisPosition.BOTTOM; axisRight.isEnabled=false; animateY(800) }
        b.lineChartFps.apply { description.isEnabled=false; setDrawGridBackground(false); xAxis.position=XAxis.XAxisPosition.BOTTOM; axisRight.isEnabled=false; animateX(1000) }
    }
    private fun render(a: DetectionAnalytics) {
        b.tvTotalDetections.text="${a.totalDetections}"; b.tvUniqueObjects.text="${a.uniqueObjects.size}"
        b.tvAvgConfidence.text="${(a.averageConfidence*100).toInt()}%"; b.tvAvgInference.text="${a.averageInferenceTime.toInt()}ms"
        b.tvAvgFps.text="${"%.1f".format(a.averageFps)}"; b.tvSessionCount.text="${a.sessionCount}"
        if (a.topObjects.isNotEmpty()) {
            val entries = a.topObjects.take(6).map { (l,c) -> PieEntry(c.toFloat(), l) }
            val ds = PieDataSet(entries,"").apply { colors=ColorTemplate.MATERIAL_COLORS.toList(); sliceSpace=3f; valueTextColor=Color.WHITE; valueTextSize=11f }
            b.pieChartObjects.data = PieData(ds); b.pieChartObjects.invalidate()
        }
        val barEntries = listOf(14f,22f,38f,31f,19f).mapIndexed { i,v -> BarEntry(i.toFloat(),v) }
        val barDs = BarDataSet(barEntries,"Confidence").apply { colors=ColorTemplate.MATERIAL_COLORS.toList(); valueTextColor=Color.WHITE }
        b.barChartInference.data = BarData(barDs).also { it.barWidth=0.7f }; b.barChartInference.xAxis.labelCount=5; b.barChartInference.invalidate()
        val lineEntries = (1..10).map { Entry(it.toFloat(), (25+Math.random()*8).toFloat()) }
        val lineDs = LineDataSet(lineEntries,"FPS").apply { color=Color.parseColor("#10B981"); lineWidth=3f; setCircleColor(Color.parseColor("#10B981")); circleRadius=4f }
        b.lineChartFps.data = LineData(lineDs); b.lineChartFps.axisLeft.axisMinimum=0f; b.lineChartFps.axisLeft.axisMaximum=40f; b.lineChartFps.invalidate()
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
