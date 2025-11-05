package com.breakfast.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.breakfast.models.ApiResponse
import com.breakfast.models.HistoryModel
import com.breakfast.models.OrderHistoryModel
import com.breakfast.repositories.HistoryRepository
import com.breakfast.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing the user's order history and history details. It exposes
 * [historyState] for the list of history items and [detailState] for a selected order's details.
 */
class HistoryViewModel(private val repository: HistoryRepository) : ViewModel() {

    private val _historyState = MutableStateFlow<Result< ApiResponse<List<HistoryModel>>>?>(null)
    val historyState = _historyState.asStateFlow()
    private val _detailState = MutableStateFlow<Result< ApiResponse<OrderHistoryModel>>?>(null)
    val detailState = _detailState.asStateFlow()

    /**
     * Fetch a paginated list of order history.
     */
    fun fetchHistory(page: Int = 1) {
        viewModelScope.launch {
            _historyState.value = Result.Loading
            val result = repository.getHistory(page)
            _historyState.value = result
        }
    }

    /**
     * Fetch detailed information for a specific order.
     */
    fun fetchHistoryDetail(orderId: Int) {
        viewModelScope.launch {
            _detailState.value = Result.Loading
            val result = repository.showHistory(orderId)
            _detailState.value = result
        }
    }

    /**
     * Factory to create [HistoryViewModel] with required dependencies.
     */
    class Factory(private val apiService: com.breakfast.network.ApiService) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
                val repository = HistoryRepository(apiService)
                @Suppress("UNCHECKED_CAST")
                return HistoryViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}