package com.visiondetect.ai.presentation.settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visiondetect.ai.domain.model.DetectionConfig
import com.visiondetect.ai.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repo: SettingsRepository) : ViewModel() {
    val config: StateFlow<DetectionConfig> = repo.getDetectionConfig().stateIn(viewModelScope, SharingStarted.Eagerly, DetectionConfig())
    val isDarkMode: StateFlow<Boolean> = repo.isDarkModeEnabled().stateIn(viewModelScope, SharingStarted.Eagerly, false)
    fun updateConfig(fn: (DetectionConfig)->DetectionConfig) { viewModelScope.launch(Dispatchers.IO) { repo.updateDetectionConfig(fn(config.value)) } }
    fun setDarkMode(v: Boolean) { viewModelScope.launch(Dispatchers.IO) { repo.setDarkMode(v) } }
    fun setPerformanceMode(v: Boolean) { viewModelScope.launch(Dispatchers.IO) { repo.setPerformanceMode(v) } }
}
