package com.breakfast.repositories

import com.breakfast.models.ApiResponse
import com.breakfast.models.VersionModel
import com.breakfast.network.ApiService
import com.breakfast.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Repository responsible for retrieving application settings from the backend. This includes
 * version information and the "About Us" content. Each method wraps network calls in a
 * [Result] so that loading, success and error states can be observed from the ViewModel.
 */
class SettingsRepository(private val apiService: ApiService) {

    /**
     * Fetch the current version information. The backend may return multiple key/value
     * entries (e.g. minimum version, latest version). Results are emitted as a list of
     * [VersionModel] objects.
     */
    suspend fun getVersion(): Result<ApiResponse<List<VersionModel>>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.version()
            Result.Success(response)
        } catch (e: HttpException) {
            Result.Error(e.response()?.errorBody()?.string())
        } catch (e: IOException) {
            Result.Error(e.message)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage)
        }
    }

    /**
     * Fetch the "About Us" content. The backend returns a map of strings (e.g. keyed by
     * language). Results are emitted as the full map.
     */
    suspend fun getAboutUs(): Result<ApiResponse<Map<String, String>>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.aboutUs()
            Result.Success(response)
        } catch (e: HttpException) {
            Result.Error(e.response()?.errorBody()?.string())
        } catch (e: IOException) {
            Result.Error(e.message)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage)
        }
    }
}