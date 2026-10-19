package com.visiondetect.ai.presentation.home
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visiondetect.ai.domain.usecase.GetDetectionAnalyticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(val totalDetections: Int=0, val totalSessions: Int=0, val topObject: String?=null)

@HiltViewModel
class HomeViewModel @Inject constructor(private val getAnalytics: GetDetectionAnalyticsUseCase) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()
    init { viewModelScope.launch(Dispatchers.IO) {
        getAnalytics().onSuccess { a -> _state.value = HomeUiState(a.totalDetections, a.sessionCount, a.topObjects.firstOrNull()?.first) }
    }}
}
