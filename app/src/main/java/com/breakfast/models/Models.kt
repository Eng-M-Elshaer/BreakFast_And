package com.breakfast.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


// Wrapper
@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "status_code") val statusCode: Int?,
    val message: String?,
    val data: T?,
)

// User and status models
@JsonClass(generateAdapter = true)
data class UserModel(
    val user: User?,
    val token: String?
)

@JsonClass(generateAdapter = true)
data class User(
    val id: Int?,
    val name: String?,
    val email: String?,
    val status: StatusModel?,
    val phone: String?,
    @Json(name = "profile_pic") val profilePic: String?,
    @Json(name = "instapay_link") val instaPay: String?
)

@JsonClass(generateAdapter = true)
data class StatusModel(
    val value: Int?,
    val name: StatusNames?
)

enum class StatusNames {
    Active, Stopped, Closed, Inactive
}

data class CloseOrderRequest(
    val tax: Double,
    val delivery: Double,
    val total: Double
)

// Home and store models
@JsonClass(generateAdapter = true)
data class HomeModel(
    val id: Int?,
    val status: StatusModel?,
    val store: Collector?,
    val collector: Collector?
)

@JsonClass(generateAdapter = true)
data class Collector(
    val id: Int?,
    val name: String?,
    val image: String?,
    val phone: String?,
    @Json(name = "instapay_link") val instaPay: String?
)

@JsonClass(generateAdapter = true)
data class StoreModel(
    val id: Int?,
    val name: String?,
    val phone: String?,
    val image: String?
)

// Order and item models
@JsonClass(generateAdapter = true)
data class OrderModel(
    val id: Int?,
    @Json(name = "order_id") val orderID: Int?,
    @Json(name = "item_name") val itemName: String?,
    val quantity: Int?,
    val price: Double?,
    val total: Double?,
    val note: String?,
    val other: String?
)

@JsonClass(generateAdapter = true)
data class StoreItemModel(
    val id: Int?,
    val name: String?,
    val price: Double?
)

@JsonClass(generateAdapter = true)
data class PersonModel(
    val id: Int?,
    val name: String?,
    val email: String?,
    val status: StatusModel?,
    val phone: String?
)

// History and collector models
@JsonClass(generateAdapter = true)
data class OrderHistoryItemModel(
    @Json(name = "item_name") val itemName: String?,
    val quantity: Int?,
    val price: Double?,
    @Json(name = "total_price") val totalPrice: Double?,
    val note: String?,
    val users: List<Collector>?,
    @Json(name = "order_item_id") val orderItemID: Int?
)

@JsonClass(generateAdapter = true)
data class OrderHistoryModel(
    @Json(name = "order_items") val orderItems: List<OrderHistoryItemModel>?,
    val count: Int?,
    @Json(name = "total_price") val totalPrice: Double?,
    val tax: Double?,
    val delivery: Double?,
    val order: OrderInfoModel?
)

@JsonClass(generateAdapter = true)
data class OrderInfoModel(
    val id: Int?,
    val status: StatusModel?,
    @Json(name = "created_at") val createdAt: String?,
    @Json(name = "total_price") val totalPrice: Double?,
    val store: Collector?,
    val tax: Double?,
    val delivery: Double?,
    val collector: Collector?
)

@JsonClass(generateAdapter = true)
data class CollectorModel(
    @Json(name = "order_items") val orderItems: List<OrderHistoryItemModel>?,
    @Json(name = "custom_order_items") val customOrderItems: List<OrderHistoryItemModel>?,
    val order: OrderInfoModel?,
    val count: Int?
)

data class CustomItemPayload(
    val name: String = "",
    val orderID: Int = 0,
    val storeID: Int = 0,
    val userID: Int = 0,
    val price: Double = 0.0,
    val quantity: Int = 0,
    val note: String = "",
    val orderItemID: Int = 0
)

@JsonClass(generateAdapter = true)
data class CollectorHistoryModel(
    @Json(name = "users_items") val usersItems: List<UsersItem>?,
    val order: OrderInfoModel?,
    val count: Int?
)

@JsonClass(generateAdapter = true)
data class UsersItem(
    @Json(name = "user_id") val userID: Int?,
    @Json(name = "user_name") val userName: String?,
    @Json(name = "user_image") val userImage: String?,
    @Json(name = "order_items") val orderItems: List<OrderItem>?,
    @Json(name = "total_quantity") val totalQuantity: Int?,
    @Json(name = "tax_delivery") val taxDelivery: Double?,
    @Json(name = "total_price") val totalPrice: Double?
)

@JsonClass(generateAdapter = true)
data class OrderItem(
    val id: Int?,
    @Json(name = "order_id") val orderID: Int?,
    @Json(name = "item_name") val itemName: String?,
    val quantity: Int?,
    val price: Double?,
    val total: Double?,
    val note: String?,
    val tax: Double?,
    val users: List<Collector>?
)

@JsonClass(generateAdapter = true)
data class ReceiptModel(
    val id: Int?,
    @Json(name = "user_name") val userName: String?,
    @Json(name = "total_price") val totalPrice: Double?
)

// History model for order summaries in history tab
@JsonClass(generateAdapter = true)
data class HistoryModel(
    val id: Int?,
    val status: StatusModel?,
    val store: Collector?,
    val collector: Collector?,
    @Json(name = "created_at") val date: String?,
    @Json(name = "total_price") val totalPrice: Double?
)

// Notification model and related enums
enum class NotificationSubjectType(val value: String) {
    @Json(name = "Order Active")
    OPEN("Order Active"),

    @Json(name = "Order Closed")
    CLOSE("Order Closed"),

    @Json(name = "Order Stopped")
    STOP("Order Stopped"),

    @Json(name = "Order")
    UNKNWON("Order");
}

@JsonClass(generateAdapter = true)
data class NotificationModel(
    val id: Int?,
    val title: String?,
    val message: String?,
    @Json(name = "is_read") val isRead: Boolean?,
    @Json(name = "subject_id") val subjectID: Int?,
    @Json(name = "created_at") val createdAt: String?,
    @Json(name = "subject_type") val subjectType: NotificationSubjectType?,
    @Json(name = "store_id") val storeID: Int?
)

// Versioning
@JsonClass(generateAdapter = true)
data class VersionModel(
    val key: String?,
    val value: String?
)

@JsonClass(generateAdapter = true)
data class VersionCheckResponse(
    @Json(name = "minimumRequiredVersion") val minimumRequiredVersion: String,
    @Json(name = "latestVersion") val latestVersion: String,
    @Json(name = "forceUpdate") val forceUpdate: Boolean
)