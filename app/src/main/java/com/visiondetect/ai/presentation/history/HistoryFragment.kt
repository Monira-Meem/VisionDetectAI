package com.visiondetect.ai.presentation.history
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.recyclerview.widget.*
import com.visiondetect.ai.databinding.FragmentHistoryBinding
import com.visiondetect.ai.databinding.ItemDetectionHistoryBinding
import com.visiondetect.ai.domain.model.DetectionRecord
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class HistoryFragment : Fragment() {
    private var _b: FragmentHistoryBinding? = null
    private val b get() = _b!!
    private val vm: HistoryViewModel by viewModels()
    private lateinit var adapter: HistoryAdapter
    override fun onCreateView(i: LayoutInflater,c: ViewGroup?,s: Bundle?): View { _b=FragmentHistoryBinding.inflate(i,c,false); return b.root }
    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v,s)
        adapter = HistoryAdapter { r -> vm.deleteRecord(r.id) }
        b.recyclerHistory.layoutManager = LinearLayoutManager(requireContext())
        b.recyclerHistory.adapter = adapter
        b.searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?) = false
            override fun onQueryTextChange(t: String?): Boolean { vm.search(t.orEmpty()); return true }
        })
        b.btnClearAll.setOnClickListener { vm.clearAllHistory() }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.history.collect { list ->
                    adapter.submitList(list)
                    b.emptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    b.recyclerHistory.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}

class HistoryAdapter(private val onDelete: (DetectionRecord)->Unit) : ListAdapter<DetectionRecord, HistoryAdapter.VH>(DIFF) {
    private val fmt = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
    override fun onCreateViewHolder(p: ViewGroup,t: Int) = VH(ItemDetectionHistoryBinding.inflate(LayoutInflater.from(p.context),p,false))
    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))
    inner class VH(private val b: ItemDetectionHistoryBinding): RecyclerView.ViewHolder(b.root) {
        fun bind(r: DetectionRecord) {
            b.tvTimestamp.text = fmt.format(Date(r.timestamp))
            b.tvObjectCount.text = "${r.objectCount} objects detected"
            b.tvObjects.text = r.detectedObjects.take(3).joinToString(", ")
            b.tvInferenceTime.text = "${r.inferenceTimeMs}ms"
            b.tvFps.text = "${"%.1f".format(r.fps)} FPS"
            val avg = r.confidenceScores.average().toFloat().takeIf { r.confidenceScores.isNotEmpty() } ?: 0f
            b.tvConfidence.text = "${(avg*100).toInt()}% avg"
            b.btnDelete.setOnClickListener { onDelete(r) }
        }
    }
    companion object { val DIFF = object: DiffUtil.ItemCallback<DetectionRecord>() {
        override fun areItemsTheSame(o: DetectionRecord, n: DetectionRecord) = o.id == n.id
        override fun areContentsTheSame(o: DetectionRecord, n: DetectionRecord) = o == n
    }}
}
