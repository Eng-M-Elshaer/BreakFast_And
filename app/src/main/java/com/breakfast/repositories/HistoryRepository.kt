package com.breakfast.repositories

import com.breakfast.models.ApiResponse
import com.breakfast.models.HistoryModel
import com.breakfast.models.OrderHistoryModel
import com.breakfast.network.ApiService
import com.breakfast.utils.Result

/**
 * Repository responsible for loading user history of past orders and detailed history information.
 */
class HistoryRepository(private val apiService: ApiService) {
    /**
     * Fetch a paginated list of the user's order history.
     */
    suspend fun getHistory(page: Int = 1): Result<ApiResponse<List<HistoryModel>>> {
        return try {
            val list = apiService.getHistory(page)
            Result.Success(list)
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
        } catch (e: Exception) {
            Result.Error(e.message)
        }
    }
}