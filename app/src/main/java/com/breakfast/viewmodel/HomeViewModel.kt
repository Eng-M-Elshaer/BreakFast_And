package com.breakfast.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.breakfast.models.ApiResponse
import com.breakfast.models.HomeModel
import com.breakfast.models.StoreModel
import com.breakfast.models.VersionModel
import com.breakfast.repositories.HomeRepository
import com.breakfast.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Home screen. It fetches the home data via [HomeRepository]
 * and exposes it as a StateFlow so the UI can react to loading, success and error states.
 */
class HomeViewModel(private val repository: HomeRepository) : ViewModel() {

    private val _homeState = MutableStateFlow<Result< ApiResponse<List<HomeModel>>>>(Result.Loading)
    val homeState = _homeState.asStateFlow()
    private val _storesState = MutableStateFlow<Result< ApiResponse<List<StoreModel>>>>(Result.Loading)
    val storeState = _storesState.asStateFlow()

    private val _infoState = MutableStateFlow<Result< ApiResponse<List<VersionModel>>>>(Result.Loading)
    val infoState = _infoState.asStateFlow()

    init {
        fetchHome()
    }

    fun fetchHome() {
        viewModelScope.launch {
            _homeState.value = Result.Loading
            val result = repository.getHome()
            _homeState.value = result
        }
    }

    fun fetchStores() {
        viewModelScope.launch {
            _storesState.value = Result.Loading
            val result = repository.getStores()
            _storesState.value = result
        }
    }

    fun fetchInfo() {
        viewModelScope.launch {
            _infoState.value = Result.Loading
            val result = repository.getInfo()
            _infoState.value = result
        }
    }

    /**
     * Factory to create [HomeViewModel] with the provided [ApiService].
     */
    class Factory(private val apiService: com.breakfast.network.ApiService) : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                val repository = HomeRepository(apiService)
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}