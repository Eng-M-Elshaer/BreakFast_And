package com.breakfast.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.breakfast.models.*
import com.breakfast.network.ApiService
import com.breakfast.repositories.OrderRepository
import com.breakfast.models.PersonModel
import com.breakfast.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing order-related UI state. It interacts with
 * [OrderRepository] to perform network operations and exposes state flows
 * representing the current list of order items, available store items,
 * the result of adding items or custom items, order status updates and
 * collector item listings.
 */
class OrderViewModel(private val repository: OrderRepository) : ViewModel() {

    private val _orderItemsState = MutableStateFlow<Result<ApiResponse<List<OrderModel>>>?>(null)
    val orderItemsState = _orderItemsState.asStateFlow()

    private val _storeItemsState = MutableStateFlow<Result<ApiResponse<List<StoreItemModel>>>?>(null)
    val storeItemsState = _storeItemsState.asStateFlow()

    private val _addItemState = MutableStateFlow<Result<ApiResponse<OrderModel>>?>(null)
    val addItemState = _addItemState.asStateFlow()

    private val _customItemState = MutableStateFlow<Result<ApiResponse<OrderModel>>?>(null)
    val customItemState = _customItemState.asStateFlow()

    private val _statusState = MutableStateFlow<Result<Unit>?>(null)
    val statusState = _statusState.asStateFlow()

    private val _collectorState = MutableStateFlow<Result<ApiResponse<CollectorModel>>?>(null)
    val collectorState = _collectorState.asStateFlow()

    // Users state for assigning items
    private val _usersState = MutableStateFlow<Result<ApiResponse<List<PersonModel>>>?>(null)
    val usersState = _usersState.asStateFlow()

    // Result state for assign item operation
    private val _assignState = MutableStateFlow<Result<Unit>?>(null)
    val assignState = _assignState.asStateFlow()

    // Result state for delete item operation
    private val _deleteItemState = MutableStateFlow<Result<Unit>?>(null)
    val deleteItemState = _deleteItemState.asStateFlow()

    // Result state for stop collecting operation
    private val _stopState = MutableStateFlow<Result<ApiResponse<HomeModel>>?>(null)
    val stopState = _stopState.asStateFlow()

    private val _closeState = MutableStateFlow<Result<ApiResponse<HomeModel>>?>(null)
    val closeState = _closeState.asStateFlow()

    /**
     * Fetch the current user's order items. Typically called when the order details
     * screen is first shown. Results are exposed via [orderItemsState].
     */
    fun fetchOrderItems(id: Int) {
        viewModelScope.launch {
            _orderItemsState.value = Result.Loading
            val result = repository.getOrderItems(id)
            _orderItemsState.value = result
        }
    }

    /**
     * Retrieve available items for a store by its identifier. The store ID
     * should be provided by the caller (often derived from the current order or
     * home screen). Results are exposed via [storeItemsState].
     */
    fun fetchStoreItems(storeId: Int) {
        viewModelScope.launch {
            _storeItemsState.value = Result.Loading
            val result = repository.getStoreItems(storeId)
            _storeItemsState.value = result
        }
    }

    /**
     * Add an existing store item to the order. Provide the order ID (nullable
     * for new orders), item ID, optional quantity, price and note. The
     * response (the newly created/updated item) is emitted via [addItemState].
     */
    fun addItem(
        orderId: Int?,
        itemId: Int?,
        quantity: Int? = null,
        price: Double? = null,
        note: String? = "",
        userId: Int? = null
    ) {
        viewModelScope.launch {
            _addItemState.value = Result.Loading
            val result = repository.addItem(orderId, itemId, quantity, price, note, userId)
            _addItemState.value = result
        }
    }

    /**
     * Create a custom item within an existing order. Provide all required
     * properties. The result is emitted via [customItemState].
     */
    fun addCustomItem(orderId: Int, name: String, price: Double, quantity: Int, note: String?) {
        viewModelScope.launch {
            _customItemState.value = Result.Loading
            val result = repository.addCustomItem(orderId, name, price, quantity, note)
            _customItemState.value = result
        }
    }

    /**
     * Update the status of a specific order. Supported actions include
     * "stop", "close" and "reopen". The result is exposed via [statusState].
     */
    fun updateStatus(orderId: Int, action: String) {
        viewModelScope.launch {
            _statusState.value = Result.Loading
            val result = repository.updateStatus(orderId, action)
            _statusState.value = result
        }
    }

    /**
     * Fetch items assigned to the collector/driver. Results are emitted via
     * [collectorState].
     */
    fun fetchCollectorItems() {
        viewModelScope.launch {
            _collectorState.value = Result.Loading
            val result = repository.getCollectorItems()
            _collectorState.value = result
        }
    }

    fun stopCollecteing(oderID: Int) {
        viewModelScope.launch {
            _stopState.value = Result.Loading
            val result = repository.stopCollecting(oderID)
            _stopState.value = result
        }
    }

    fun closeCollecting(
        orderId: Int,
        tax: Double,
        delivery: Double,
        total: Double
    ) {
        viewModelScope.launch {
            _closeState.value = Result.Loading
            val result = repository.closeCollecting(orderId, tax, delivery, total)
            _closeState.value = result
        }
    }

    /**
     * Retrieve the list of users that an order item can be assigned to. Results are exposed
     * via [usersState].
     */
    fun fetchUsers() {
        viewModelScope.launch {
            _usersState.value = Result.Loading
            val result = repository.getUsers()
            _usersState.value = result
        }
    }

    /**
     * Assign a specific order item to a user. The call is currently a stub and will
     * immediately return success. The result is exposed via [assignState].
     */
    fun assignItem(orderItemId: Int, userId: Int) {
        viewModelScope.launch {
            _assignState.value = Result.Loading
            val result = repository.assignItem(orderItemId, userId)
            _assignState.value = result
        }
    }

    /**
     * Remove a single order item by its id, then expose result via deleteItemState
     */
    fun removeOrderItem(orderItemId: Int) {
        viewModelScope.launch {
            _deleteItemState.value = Result.Loading
            val result = repository.removeOrderItem(orderItemId)
            _deleteItemState.value = result
        }
    }

    /**
     * Factory to create [OrderViewModel] instances with the provided
     * [ApiService]. This avoids boilerplate when instantiating the ViewModel
     * from composable functions.
     */
    class Factory(private val apiService: ApiService) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OrderViewModel::class.java)) {
                val repository = OrderRepository(apiService)
                @Suppress("UNCHECKED_CAST")
                return OrderViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}