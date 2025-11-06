package com.breakfast.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data transfer objects used to send request bodies to the backend.
 * Each request class corresponds to an API endpoint defined in [ApiService].
 */

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val phone: String? = null,
    val password: String? = null,
    @Json(name = "social_id") val socialId: String? = null,
    @Json(name = "fcm_token") val fcmToken: String? = null,
    @Json(name = "social_type") val socialType: String? = null
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val name: String,
    val phone: String,
    val email: String,
    val password: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdateProfileRequest(
    val name: String,
    val phone: String,
    val email: String
)

@JsonClass(generateAdapter = true)
data class ChangePasswordRequest(
    @Json(name = "current_password") val currentPassword: String,
    @Json(name = "new_password") val newPassword: String,
    @Json(name = "password_confirmation") val passwordConfirmation: String
)

@JsonClass(generateAdapter = true)
data class DeleteAccountRequest(
    val password: String
)

// Forgot password flows
@JsonClass(generateAdapter = true)
data class ForgetPasswordRequest(
    val email: String
)

@JsonClass(generateAdapter = true)
data class ForgetPasswordVerifyRequest(
    val email: String,
    val code: String
)

@JsonClass(generateAdapter = true)
data class ForgetPasswordResetRequest(
    val email: String,
    val code: String,
    val password: String,
    @Json(name = "password_confirmation") val passwordConfirmation: String
)

// Order creation
@JsonClass(generateAdapter = true)
data class StartOrderRequest(
    @Json(name = "store_id") val storeId: Int
)

@JsonClass(generateAdapter = true)
data class CreateOrderRequest(
    @Json(name = "order_id") val orderId: Int? = null,
    @Json(name = "user_id") val userId: Int? = null,
    @Json(name = "item_id") val itemId: Int? = null,
    val quantity: Int? = null,
    val price: Double? = null,
    val note: String? = null
)

@JsonClass(generateAdapter = true)
data class CustomItemRequest(
    @Json(name = "order_id") val orderId: Int? = null,
    @Json(name = "name") val itemName: String,
    val price: Double,
    val quantity: Int,
    val note: String? = null
)

// Optionally used when closing an order to set tax and delivery fees
@JsonClass(generateAdapter = true)
data class TaxDeliveryRequest(
    @Json(name = "order_id") val orderId: Int,
    val tax: Double,
    val delivery: Double
)