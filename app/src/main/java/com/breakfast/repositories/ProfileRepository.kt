package com.breakfast.repositories

import android.content.Context
import com.breakfast.R
import com.breakfast.models.ApiResponse
import com.breakfast.models.UserModel
import com.breakfast.network.UpdateProfileRequest
import com.breakfast.network.DeleteAccountRequest
import com.breakfast.network.ApiService
import com.breakfast.utils.Result
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import com.breakfast.managers.PreferenceManager
import java.io.IOException

/**
 * Repository for managing the user's profile. Handles fetching profile data,
 * updating account information and deleting the account. Interacts with
 * [PreferenceManager] to persist user changes locally.
 */
class ProfileRepository(
    private val apiService: ApiService,
    private val preferenceManager: PreferenceManager,
    private val context: Context
) {

    /**
     * Retrieve the current user's profile from the backend. On success, updates the stored
     * user object in [PreferenceManager].
     */
    suspend fun getProfile(): Result<ApiResponse<UserModel>> {
        return try {
            val userModel = apiService.getProfile()
            // Extract the actual user data from the wrapper returned by the API
            val user = userModel.data?.user ?: throw Exception("User data is missing")
            // Persist the token and user locally
            preferenceManager.saveUser(user)
            Result.Success(userModel)
        } catch (e: IOException) {
            Result.Error(context.getString(R.string.no_internet))
        } catch (e: Exception) {
            Result.Error(e.message)
        }
    }

    /**
     * Update the user's name, phone and email. On success, persist the updated user locally.
     */
    suspend fun updateProfile(name: String, phone: String, email: String): Result<ApiResponse<UserModel>> {
        return try {
            val request = UpdateProfileRequest(name, phone, email)
            val userModel = apiService.updateProfile(request)
            val user = userModel.data?.user ?: throw Exception("User data is missing")
            preferenceManager.saveUser(user)
            userModel.data.token?.let { preferenceManager.saveToken(it) }
            Result.Success(userModel)
        } catch (e: IOException) {
            Result.Error(context.getString(R.string.no_internet))
        } catch (e: Exception) {
            Result.Error(e.message)
        }
    }

    /**
     * Delete the user's account. Requires the current password. Clears local preferences on success.
     */
    suspend fun deleteAccount(password: String): Result<ApiResponse<UserModel>> {
        return try {
            val request = DeleteAccountRequest(password)
            val userModel = apiService.deleteAccount(request)
            preferenceManager.clear()
            val user = userModel
            Result.Success(user)
        } catch (e: IOException) {
            Result.Error(context.getString(R.string.no_internet))
        } catch (e: Exception) {
            Result.Error(e.message)
        }
    }

    /**
     * Upload a new avatar image for the user.
     * Expects raw image bytes. On success, persist the updated user and token.
     */
    suspend fun uploadAvatar(bytes: ByteArray): Result<ApiResponse<UserModel>> {
        return try {
            val mediaType = "image/jpeg".toMediaTypeOrNull()
            val body = bytes.toRequestBody(mediaType)
            val part = MultipartBody.Part.createFormData(
                name = "image",
                filename = "avatar.jpg",
                body = body
            )

            val userModel = apiService.uploadAvatar(part)
            val user = userModel.data ?: throw Exception("User data is missing")
            Result.Success(userModel)
        } catch (e: IOException) {
            Result.Error(context.getString(R.string.no_internet))
        } catch (e: Exception) {
            Result.Error(e.message)
        }
    }

    /**
     * Log out the current user. Clears locally persisted credentials and user info.
     * If you later add a backend logout endpoint, call it here before clearing.
     */
    suspend fun logout(): Result<Unit> {
        return try {
            // Clear local session (token, user, any cached prefs)
            preferenceManager.clear()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message)
        }
    }
}