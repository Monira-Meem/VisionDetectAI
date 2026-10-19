package com.visiondetect.ai.presentation.settings
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import com.visiondetect.ai.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private var _b: FragmentSettingsBinding? = null
    private val b get() = _b!!
    private val vm: SettingsViewModel by viewModels()
    override fun onCreateView(i: LayoutInflater,c: ViewGroup?,s: Bundle?): View { _b=FragmentSettingsBinding.inflate(i,c,false); return b.root }
    override fun onViewCreated(v: View, s: Bundle?) {
        super.onViewCreated(v,s)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { vm.config.collect { cfg ->
                    b.switchShowLabels.isChecked=cfg.showLabels; b.switchShowBoxes.isChecked=cfg.showBoundingBoxes
                    b.switchGpuDelegate.isChecked=cfg.useGpuDelegate
                    b.sliderConfidenceThreshold.value=(cfg.confidenceThreshold*100).coerceIn(10f,95f)
                    b.tvConfidenceValue.text="${(cfg.confidenceThreshold*100).toInt()}%"
                }}
                launch { vm.isDarkMode.collect { b.switchDarkMode.isChecked=it } }
            }
        }
        b.switchDarkMode.setOnCheckedChangeListener { _,c -> vm.setDarkMode(c)
            AppCompatDelegate.setDefaultNightMode(if(c) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO) }
        b.switchShowLabels.setOnCheckedChangeListener { _,c -> vm.updateConfig { it.copy(showLabels=c) } }
        b.switchShowBoxes.setOnCheckedChangeListener { _,c -> vm.updateConfig { it.copy(showBoundingBoxes=c) } }
        b.switchGpuDelegate.setOnCheckedChangeListener { _,c -> vm.updateConfig { it.copy(useGpuDelegate=c) } }
        b.switchPerformanceMode.setOnCheckedChangeListener { _,c -> vm.setPerformanceMode(c) }
        b.sliderConfidenceThreshold.addOnChangeListener { _,v,u -> if(u) { b.tvConfidenceValue.text="${v.toInt()}%"; vm.updateConfig { it.copy(confidenceThreshold=v/100f) } } }
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
