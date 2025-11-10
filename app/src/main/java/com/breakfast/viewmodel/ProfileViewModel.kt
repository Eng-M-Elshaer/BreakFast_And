package com.breakfast.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.breakfast.managers.PreferenceManager
import com.breakfast.models.ApiResponse
import com.breakfast.models.UserModel
import com.breakfast.repositories.ProfileRepository
import com.breakfast.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the user's profile. Handles loading profile data, updating profile
 * information and deleting the account. Uses [ProfileRepository] to interact with
 * the backend and [PreferenceManager] for persistence.
 */
class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {
    private val _profileState = MutableStateFlow<Result<ApiResponse<UserModel>>?>(null)
    val profileState = _profileState.asStateFlow()

    private val _updateState = MutableStateFlow<Result<ApiResponse<UserModel>>?>(null)
    val updateState = _updateState.asStateFlow()

    private val _avatarState = MutableStateFlow<Result<ApiResponse<UserModel>>?>(null)
    val avatarState = _avatarState.asStateFlow()

    private val _logoutState = MutableStateFlow<Result<Unit>?>(null)
    val logoutState = _logoutState.asStateFlow()

    /**
     * Load the current user's profile information.
     */
    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = Result.Loading
            val result = repository.getProfile()
            _profileState.value = result
        }
    }

    /**
     * Update the user's name, phone and email.
     */
    fun updateProfile(name: String, phone: String, email: String) {
        viewModelScope.launch {
            _updateState.value = Result.Loading
            val result = repository.updateProfile(name, phone, email)
            _updateState.value = result
        }
    }

    /**
     * Upload a new profile picture. Expects raw image bytes (already compressed if needed).
     */
    fun uploadAvatar(bytes: ByteArray) {
        viewModelScope.launch {
            _avatarState.value = Result.Loading
            val result = repository.uploadAvatar(bytes)
            _avatarState.value = result
            // If repository updates the stored user on success, you may also refresh profile here if needed:
            // if (result is Result.Success) loadProfile()
        }
    }

    /**
     * Log out the current user by clearing local credentials and informing backend if needed.
     */
    fun logout() {
        viewModelScope.launch {
            _logoutState.value = Result.Loading
            val result = repository.logout()
            _logoutState.value = result
        }
    }

    fun clearLogoutState() { _logoutState.value = null }
    fun clearProfileState() { _profileState.value = null }
    fun clearUpdateState() { _updateState.value = null }
    fun clearAvatarState() { _avatarState.value = null }

    /**
     * Factory to create [ProfileViewModel] with required dependencies.
     */
    class Factory(
        private val preferenceManager: PreferenceManager,
        private val apiService: com.breakfast.network.ApiService,
        private val context: android.content.Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                val repository = ProfileRepository(apiService, preferenceManager, context)
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}