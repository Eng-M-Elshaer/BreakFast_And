package com.breakfast.repositories

import com.breakfast.models.*
import com.breakfast.network.ApiService
import com.breakfast.models.PersonModel
import com.breakfast.network.CreateOrderRequest
import com.breakfast.network.CustomItemRequest
import com.breakfast.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Repository encapsulating all order-related operations. This class provides
 * functions to fetch the user's current order items, retrieve available items
 * from a store, add items or custom items to an order, update an order's
 * status (such as closing or reopening) and fetch collector/driver data.
 *
 * Each method wraps network calls in a [Result] to propagate loading,
 * success or error states back to the ViewModel layer.
 */
class OrderRepository(private val apiService: ApiService) {

    /**
     * Fetch the list of items in the current user's active order.
     */
    suspend fun getOrderItems(id: Int): Result<ApiResponse<List<OrderModel>>> = withContext(Dispatchers.IO) {
        try {
            val items = apiService.myOrders(id)
            Result.Success(items)
        } catch (e: HttpException) {
            Result.Error(e.response()?.errorBody()?.string())
        } catch (e: IOException) {
            Result.Error(e.message)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage)
        }
    }

    /**
     * Retrieve available store items for a given store ID. These items can
     * subsequently be added to the user's order.
     */
    suspend fun getStoreItems(storeId: Int): Result<ApiResponse<List<StoreItemModel>>> =
        withContext(Dispatchers.IO) {
            try {
                val items = apiService.storeItems(storeId)
                val remoteList = items.data ?: emptyList()

                val custom = StoreItemModel(
                    id = 0,
                    name = "Custom item",
                    price = 0.0,
                )

                Result.Success(
                    items.copy(data = listOf(custom) + remoteList)
                )
            } catch (e: HttpException) {
                Result.Error(e.response()?.errorBody()?.string())
            } catch (e: IOException) {
                Result.Error(e.message)
            } catch (e: Exception) {
                Result.Error(e.localizedMessage)
            }
        }

    /**
     * Add a regular item to an order. If the order ID is null the backend
     * creates a new order. The quantity and price fields are optional; when
     * omitted the server uses defaults.
     */
    suspend fun addItem(
        orderId: Int?,
        itemId: Int?,
        quantity: Int? = null,
        price: Double? = null,
        note: String? = null,
        userId: Int? = null
    ): Result<ApiResponse<OrderModel>> = withContext(Dispatchers.IO) {
        try {
            val request = CreateOrderRequest(
                orderId = orderId,
                userId = userId,
                itemId = itemId,
                quantity = quantity,
                price = price,
                note = note
            )
            val response = apiService.createOrder(request)
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
     * Add a custom item to an existing order by specifying its name, price,
     * quantity and optional note.
     */
    suspend fun addCustomItem(
        orderId: Int,
        name: String,
        price: Double,
        quantity: Int,
        note: String?
    ): Result<ApiResponse<OrderModel>> = withContext(Dispatchers.IO) {
        try {
            val request = CustomItemRequest(orderId = orderId, itemName = name, price = price, quantity = quantity, note = note)
            val response = apiService.createCustomItem(request)
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
     * Update the status of a specific order. The action string must match
     * backend-supported operations: e.g. "stop", "close" or "reopen".
     */
    suspend fun updateStatus(orderId: Int, action: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            apiService.updateOrderStatus(orderId, action)
            Result.Success(Unit)
        } catch (e: HttpException) {
            Result.Error(e.response()?.errorBody()?.string())
        } catch (e: IOException) {
            Result.Error(e.message)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage)
        }
    }

    /**
     * Retrieve the items assigned to the collector/driver. This endpoint
     * aggregates order items for driver duties rather than the user’s own order.
     */
    suspend fun getCollectorItems(): Result<ApiResponse<CollectorModel>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.collectorItems()
            Result.Success(response)
        } catch (e: HttpException) {
            Result.Error(e.response()?.errorBody()?.string())
        } catch (e: IOException) {
            Result.Error(e.message)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage)
        }
    }
    suspend fun getCollectorHistoryItems(orderId: Int): Result<ApiResponse<CollectorHistoryModel>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.collectorHistory(orderId)
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
     * Stop Collecting Orders
     * */
    suspend fun stopCollecting(orderId: Int): Result<ApiResponse<HomeModel>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.stopCollecting(orderId)
            Result.Success(response)
        } catch (e: HttpException) {
            Result.Error(e.response()?.errorBody()?.string())
        } catch (e: IOException) {
            Result.Error(e.message)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage)
        }
    }
    suspend fun reOpenCollecting(orderId: Int): Result<ApiResponse<HomeModel>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.reOpenCollecting(orderId)
            Result.Success(response)
        } catch (e: HttpException) {
            Result.Error(e.response()?.errorBody()?.string())
        } catch (e: IOException) {
            Result.Error(e.message)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage)
        }
    }
    suspend fun receipt(orderId: Int): Result<ApiResponse<List<ReceiptModel>>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.receipt(orderId)
            Result.Success(response)
        } catch (e: HttpException) {
            Result.Error(e.response()?.errorBody()?.string())
        } catch (e: IOException) {
            Result.Error(e.message)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage)
        }
    }
    suspend fun closeCollecting(
        orderId: Int,
        tax: Double,
        delivery: Double,
        total: Double
    ): Result<ApiResponse<HomeModel>> = withContext(Dispatchers.IO) {
        try {
            val request = CloseOrderRequest(
                tax = tax,
                delivery = delivery,
                total = total
            )
            val response = apiService.closeCollecting(orderId, request)
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
     * Fetch a list of users for assigning order items. The backend returns a collection
     * of [PersonModel] objects representing potential assignees. On error, returns
     * [Result.Error].
     */
    suspend fun getUsers(): Result<ApiResponse<List<PersonModel>>> = withContext(Dispatchers.IO) {
        try {
            val users = apiService.getUsers()
            Result.Success(users)
        } catch (e: HttpException) {
            Result.Error(e.response()?.errorBody()?.string())
        } catch (e: IOException) {
            Result.Error(e.message)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage)
        }
    }

    /**
     * Assign an existing order item to another user. This is a placeholder stub since the
     * backend API does not currently define a specific endpoint for item assignment. It
     * simply returns [Result.Success] immediately. You can replace this implementation
     * with a real network call when the API supports assigning items.
     */
    suspend fun assignItem(orderItemId: Int, userId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        // TODO: Replace with real API call when available
        Result.Success(Unit)
    }

    /**
     * Remove/delete a single order item by its id.
     * This wraps the backend delete endpoint in Result.
     */
    suspend fun removeOrderItem(orderItemId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            apiService.deleteOrderItem(orderItemId)
            Result.Success(Unit)
        } catch (e: HttpException) {
            Result.Error(e.response()?.errorBody()?.string())
        } catch (e: IOException) {
            Result.Error(e.message)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage)
        }
    }
}