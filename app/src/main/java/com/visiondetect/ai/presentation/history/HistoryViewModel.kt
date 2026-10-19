package com.visiondetect.ai.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.visiondetect.ai.domain.model.DetectionRecord
import com.visiondetect.ai.domain.usecase.ClearDetectionHistoryUseCase
import com.visiondetect.ai.domain.usecase.DeleteDetectionRecordUseCase
import com.visiondetect.ai.domain.usecase.GetDetectionHistoryUseCase
import com.visiondetect.ai.domain.usecase.SearchDetectionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHistory: GetDetectionHistoryUseCase,
    private val deleteRecord: DeleteDetectionRecordUseCase,
    private val clearHistory: ClearDetectionHistoryUseCase,
    private val searchUseCase: SearchDetectionsUseCase
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    // Explicit return type on the lambda fixes Kotlin 2.0 type inference issue
    @OptIn(ExperimentalCoroutinesApi::class)
    val history: StateFlow<List<DetectionRecord>> = searchQuery
        .debounce(300L)
        .flatMapLatest { query: String ->
            val result: Flow<List<DetectionRecord>> =
                if (query.isBlank()) getHistory()
                else searchUseCase(query)
            result
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun search(query: String) {
        searchQuery.value = query
    }

    fun deleteRecord(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteRecord.invoke(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            clearHistory()
        }
    }
}
