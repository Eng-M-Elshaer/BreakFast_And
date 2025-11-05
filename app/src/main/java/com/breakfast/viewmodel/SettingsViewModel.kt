package com.breakfast.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.breakfast.managers.PreferenceManager
import com.breakfast.models.ApiResponse
import com.breakfast.models.VersionModel
import com.breakfast.network.ApiService
import com.breakfast.repositories.SettingsRepository
import com.breakfast.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Settings screen. It retrieves version information, the "About Us" content
 * and manages the user's language preference. Uses [SettingsRepository] for network calls
 * and [PreferenceManager] for local persistence of the language.
 */
class SettingsViewModel(
    private val repository: SettingsRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val _versionState = MutableStateFlow<Result<ApiResponse<List<VersionModel>>>?>(null)
    val versionState = _versionState.asStateFlow()

    private val _aboutUsState = MutableStateFlow<Result<ApiResponse<Map<String, String>>>?>(null)
    val aboutUsState = _aboutUsState.asStateFlow()

    private val _languageState = MutableStateFlow<String?>(null)
    val languageState = _languageState.asStateFlow()

    /**
     * Fetch the version info from the repository. Emits loading, then success/error.
     */
    fun fetchVersion() {
        viewModelScope.launch {
            _versionState.value = Result.Loading
            val result = repository.getVersion()
            _versionState.value = result
        }
    }

    /**
     * Fetch the "About Us" content from the repository. Emits loading, then success/error.
     */
    fun fetchAboutUs() {
        viewModelScope.launch {
            _aboutUsState.value = Result.Loading
            val result = repository.getAboutUs()
            _aboutUsState.value = result
        }
    }

    /**
     * Retrieve the currently stored language and update the language state. Should be called
     * when the Settings screen is first composed.
     */
    fun loadLanguage() {
        _languageState.value = preferenceManager.getLanguage() ?: "en"
    }

    /**
     * Persist a new language preference. This will cause subsequent network requests
     * to include the "Accept-Language" header via [ApiClient].
     */
    fun setLanguage(language: String) {
        preferenceManager.saveLanguage(language)
        _languageState.value = language
    }

    /**
     * Factory for creating [SettingsViewModel] instances. Injects [SettingsRepository] and
     * [PreferenceManager] dependencies.
     */
    class Factory(
        private val preferenceManager: PreferenceManager,
        private val apiService: ApiService
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                val repository = SettingsRepository(apiService)
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(repository, preferenceManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}