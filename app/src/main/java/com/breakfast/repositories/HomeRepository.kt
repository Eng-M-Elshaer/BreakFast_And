package com.breakfast.repositories

import android.content.Context
import com.breakfast.R
import com.breakfast.models.ApiResponse
import com.breakfast.models.HomeModel
import com.breakfast.models.StoreModel
import com.breakfast.models.VersionModel
import com.breakfast.network.ApiService
import com.breakfast.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import com.breakfast.network.StartOrderRequest
/**
 * Repository to fetch data for the Home screen, such as current orders or stores list.
 */
class HomeRepository(
    private val apiService: ApiService,
    private val context: Context
) {
    suspend fun getHome(): Result<ApiResponse<List<HomeModel>>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.home()
            Result.Success(response)
        } catch (e: HttpException) {
            Result.Error(e.response()?.errorBody()?.string())
        } catch (e: IOException) {
            Result.Error(context.getString(R.string.no_internet))
        } catch (e: Exception) {
            Result.Error(e.localizedMessage)
        }
    }
    suspend fun startOrder(storeId: Int): Result<ApiResponse<HomeModel>> = withContext(Dispatchers.IO) {
        try {
            val request = StartOrderRequest(storeId = storeId)
            val response = apiService.startOrder(request)
            Result.Success(response)
        } catch (e: HttpException) {
            Result.Error(e.response()?.errorBody()?.string())
        } catch (e: IOException) {
            Result.Error(context.getString(R.string.no_internet))
        } catch (e: Exception) {
            Result.Error(e.localizedMessage)
        }
    }

    suspend fun getStores(): Result<ApiResponse<List<StoreModel>>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getStores()
            Result.Success(response)
        } catch (e: HttpException) {
            Result.Error(e.response()?.errorBody()?.string())
        } catch (e: IOException) {
            Result.Error(context.getString(R.string.no_internet))
        } catch (e: Exception) {
            Result.Error(e.localizedMessage)
        }
    }

    suspend fun getInfo(): Result<ApiResponse<List<VersionModel>>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.version()
            Result.Success(response)
        } catch (e: HttpException) {
            Result.Error(e.response()?.errorBody()?.string())
        } catch (e: IOException) {
            Result.Error(context.getString(R.string.no_internet))
        } catch (e: Exception) {
            Result.Error(e.localizedMessage)
        }
    }
}