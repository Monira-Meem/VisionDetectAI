package com.visiondetect.ai.presentation.home

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import com.visiondetect.ai.R
import com.visiondetect.ai.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!
    private val vm: HomeViewModel by viewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentHomeBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v, s)
        b.btnStartDetection.setOnClickListener { findNavController().navigate(R.id.action_home_to_detection) }
        b.cardHistory.setOnClickListener   { findNavController().navigate(R.id.action_home_to_history) }
        b.cardAnalytics.setOnClickListener { findNavController().navigate(R.id.action_home_to_analytics) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collect { st ->
                    b.tvTotalDetections.text = "${st.totalDetections}"
                    b.tvTotalSessions.text   = "${st.totalSessions}"
                    b.tvTopObject.text       = st.topObject ?: "—"
                    // Updated text — ML Kit instead of TFLite
                    b.tvModelStatus.text     = "ML Kit Object Detection — Ready"
                    b.tvDeviceInfo.text      = "Google ML Kit · Real-time · ${Runtime.getRuntime().availableProcessors()} Cores"
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
