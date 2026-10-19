package com.visiondetect.ai.presentation.analytics
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visiondetect.ai.domain.model.DetectionAnalytics
import com.visiondetect.ai.domain.usecase.GetDetectionAnalyticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AnalyticsUiState { object Loading:AnalyticsUiState(); object Empty:AnalyticsUiState()
    data class Success(val data: DetectionAnalytics):AnalyticsUiState(); data class Error(val msg: String):AnalyticsUiState() }

@HiltViewModel
class AnalyticsViewModel @Inject constructor(private val getAnalytics: GetDetectionAnalyticsUseCase) : ViewModel() {
    private val _state = MutableStateFlow<AnalyticsUiState>(AnalyticsUiState.Loading)
    val state: StateFlow<AnalyticsUiState> = _state.asStateFlow()
    init { load() }
    fun load() { viewModelScope.launch(Dispatchers.IO) {
        _state.value = AnalyticsUiState.Loading
        getAnalytics().onSuccess { a -> _state.value = if (a.totalDetections==0) AnalyticsUiState.Empty else AnalyticsUiState.Success(a) }
                      .onFailure { _state.value = AnalyticsUiState.Error(it.message?:"Error") }
    }}
}
