package com.breakfast.network

import com.breakfast.models.*
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Defines API endpoints. Add functions for each endpoint in the backend.
 */
interface ApiService {
    // ---------------- Authentication ----------------
    /**
     * Authenticate a user via phone/password or social credentials.
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<UserModel>

    /**
     * Register a new user account.
     */
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<UserModel>

    /**
     * Logout the current user by invalidating the token on the backend.
     */
    @POST("auth/logout")
    suspend fun logout(): retrofit2.Response<Unit>

    // ---------------- User Profile ----------------
    /**
     * Retrieve the current user's profile information.
     */
    @GET("users/profile")
    suspend fun getProfile(): ApiResponse<UserModel>

    /**
     * Update a user's name, phone and email.
     */
    @PUT("users/profile-update")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ApiResponse<UserModel>

    /**
     * Change the user's password while authenticated.
     */
    @PUT("users/update-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): ApiResponse<UserModel>

    /**
     * Upload / replace the user's profile picture.
     */
    @Multipart
    @POST("users/profile-pic")
    suspend fun uploadAvatar(
        @Part image: okhttp3.MultipartBody.Part
    ): ApiResponse<UserModel>

    /**
     * Delete the user's account (requires providing the current password).
     */
    @DELETE("users/delete-account")
    suspend fun deleteAccount(@Body request: DeleteAccountRequest): ApiResponse<UserModel>

    // ---------------- Forgot Password ----------------
    /**
     * Send a password reset link or code to the user's email.
     */
    @POST("password/reset-mail")
    suspend fun forgetPassword(@Body request: ForgetPasswordRequest): retrofit2.Response<Unit>

    /**
     * Verify the reset code sent to the user's email.
     */
    @POST("password/check-code")
    suspend fun verifyResetCode(@Body request: ForgetPasswordVerifyRequest): retrofit2.Response<Unit>

    /**
     * Reset the user's password using the verification code.
     */
    @POST("password/reset")
    suspend fun resetPassword(@Body request: ForgetPasswordResetRequest): retrofit2.Response<Unit>

    // ---------------- Home & Stores ----------------
    /**
     * Retrieve dashboard data such as current orders or available stores.
     */
    @GET("orders/home")
    suspend fun home(): ApiResponse<List<HomeModel>>

    /**
     * Get a list of stores available for creating orders.
     */
    @GET("stores/dropdown")
    suspend fun getStores(): ApiResponse<List<StoreModel>>

    /**
     * Start a new order in a selected store.
     */
    @POST("orders")
    suspend fun startOrder(@Body request: StartOrderRequest): HomeModel

    /**
     * Retrieve the current user's items in the ongoing order.
     */
    @GET("order-items/my")
    suspend fun myOrders(@Query("order_id") orderId: Int): ApiResponse<List<OrderModel>>

    /**
     * Retrieve available items from a specific store.
     * @param storeId ID of the store to fetch items for.
     */
    @GET("items/dropdown")
    suspend fun storeItems(@Query("store_id") storeId: Int): ApiResponse<List<StoreItemModel>>

    // ---------------- Order Items ----------------
    /**
     * Add an item to an order.
     */
    @POST("order-items")
    suspend fun createOrder(@Body request: CreateOrderRequest): ApiResponse<OrderModel>

    /**
     * Remove an item from an order by its ID.
     */
    @DELETE("order-items/{item_id}")
    suspend fun deleteItem(@Path("item_id") itemId: Int): retrofit2.Response<Unit>

    /**
     * Alias for deleting a single order item (used by repository.removeOrderItem).
     */
    @DELETE("order-items/{item_id}")
    suspend fun deleteOrderItem(@Path("item_id") itemId: Int): retrofit2.Response<Unit>

    /**
     * Add a custom item to an order (name, price, quantity, note).
     */
    @POST("items/create-custom")
    suspend fun createCustomItem(@Body request: CustomItemRequest): ApiResponse<OrderModel>

    /**
     * Update the order status (stop, close, reopen).
     */
    @PUT("orders/{order_id}/{action}")
    suspend fun updateOrderStatus(@Path("order_id") orderId: Int, @Path("action") action: String): retrofit2.Response<Unit>

    /**
     * Retrieve a receipt for a completed order.
     */
    @GET("orders/receipt")
    suspend fun receipt(@Query("order_id") orderId: Int): ApiResponse<ReceiptModel>

    // ---------------- History ----------------
    /**
     * Paginated history of past orders for the current user.
     * @param page Optional page number (default is 1).
     */
    @GET("history/user")
    suspend fun getHistory(@Query("page") page: Int = 1): ApiResponse<List<HistoryModel>>

    /**
     * Detailed view of a specific historical order for the user.
     */
    @GET("history/{order_id}/user-show")
    suspend fun showHistory(@Path("order_id") orderId: Int): ApiResponse<OrderHistoryModel>

    // ---------------- Collector (Driver) ----------------
    /**
     * Current items for the collector/driver.
     */
    @GET("order-items")
    suspend fun collectorItems(): ApiResponse<CollectorModel>

    /**
     * Detailed view of a historical order from the collector's perspective.
     */
    @GET("history/{order_id}/admin-show")
    suspend fun collectorHistory(@Path("order_id") orderId: Int): ApiResponse<CollectorHistoryModel>

    // ---------------- Users List ----------------
    /**
     * List of users when assigning an item.
     */
    @GET("users/dropdown")
    suspend fun getUsers():  ApiResponse<List<PersonModel>>

    // ---------------- Notifications ----------------
    /**
     * Retrieve notifications for the user.
     */
    @GET("notifications")
    suspend fun notifications(): ApiResponse<List<NotificationModel>>

    /**
     * Mark a single notification as read.
     */
    @PUT("notifications/{id}/read")
    suspend fun readNotification(@Path("id") id: Int): retrofit2.Response<Unit>

    /**
     * Mark all notifications as read.
     */
    @PUT("notifications/mark-all-read")
    suspend fun readAllNotifications(): retrofit2.Response<Unit>

    // ---------------- Settings ----------------
    /**
     * Fetch app version information. The API path retains the `ios` suffix from the backend.
     */
    @GET("settings/android")
    suspend fun version(): ApiResponse<List<VersionModel>>

    /**
     * Fetch the "About Us" content.
     */
    @GET("settings/about-us")
    suspend fun aboutUs(): ApiResponse<Map<String, String>>
}

// Request DTO classes are defined in Requests.kt and imported into this interface as needed.