package com.breakfast.repositories

import android.content.Context
import com.breakfast.R
import com.breakfast.models.ApiResponse
import com.breakfast.models.HistoryModel
import com.breakfast.models.OrderHistoryModel
import com.breakfast.network.ApiService
import com.breakfast.utils.Result
import java.io.IOException

/**
 * Repository responsible for loading user history of past orders and detailed history information.
 */
class HistoryRepository(
    private val apiService: ApiService,
    private val context: Context
) {
    /**
     * Fetch a paginated list of the user's order history.
     */
    suspend fun getHistory(page: Int = 1): Result<ApiResponse<List<HistoryModel>>> {
        return try {
            val list = apiService.getHistory(page)
            Result.Success(list)
        } catch (e: IOException) {
            Result.Error(context.getString(R.string.no_internet))
        } catch (e: Exception) {
            Result.Error(e.message)
        }
    }

    /**
     * Fetch detailed information for a specific past order.
     */
    suspend fun showHistory(orderId: Int): Result<ApiResponse<OrderHistoryModel>> {
        return try {
            val detail = apiService.showHistory(orderId)
            Result.Success(detail)
        } catch (e: IOException) {
            Result.Error(context.getString(R.string.no_internet))
        } catch (e: Exception) {
            Result.Error(e.message)
        }
    }
}