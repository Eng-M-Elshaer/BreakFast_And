
package com.breakfast.ui.order

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.breakfast.models.OrderHistoryItemModel
import com.breakfast.network.ApiClient
import com.breakfast.utils.Result
import com.breakfast.viewmodel.OrderViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import com.breakfast.ui.components.BreakfastEmptyState
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.breakfast.R
import com.breakfast.designsystem.BreakfastButtonRes
import com.breakfast.designsystem.BreakfastOutlinedTextField
import com.breakfast.designsystem.CollectorOrderDisplayItem
import com.breakfast.models.CustomItemPayload
import com.breakfast.designsystem.CollectorOrderItemCard
import com.breakfast.designsystem.CollectorTableHeader

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
/**
 * Screen for collectors/drivers to view assigned items and user details. This screen
 * fetches the current collector items from the backend and displays both regular and
 * custom order items. The list is read-only; there are no actions in this demo.
 */
@Composable
fun CollectorDetailsScreen(
    navController: NavController? = null,
) {
    val context = LocalContext.current
    val viewModel: OrderViewModel = viewModel(factory = OrderViewModel.Factory(ApiClient.apiService, context))
    val collectorState by viewModel.collectorState.collectAsState()
    val stopState by viewModel.stopState.collectAsState()
    val closeState by viewModel.closeState.collectAsState()
    val showUsersDialog = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val selectedUsers = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<List<com.breakfast.models.Collector>>(emptyList()) }
    val taxText = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    val deliveryText = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    val totalText = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    val taxError = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    val deliveryError = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    // Add derived flag for stopped UI
    val isStoppedUi = (collectorState as? com.breakfast.utils.Result.Success)?.data?.data?.order?.status?.name == com.breakfast.models.StatusNames.Stopped || stopState is com.breakfast.utils.Result.Success

    // Load collector items on first composition
    LaunchedEffect(Unit) { viewModel.fetchCollectorItems() }

    LaunchedEffect(closeState) {
        if (closeState is com.breakfast.utils.Result.Success) {
            val orderId = (collectorState as? com.breakfast.utils.Result.Success)?.data?.data?.order?.id
            if (orderId != null) {
                navController?.navigate("order_details/$orderId")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.collector_details)) },
                navigationIcon = {
                    IconButton(onClick = {
                        val popped = navController?.popBackStack() ?: false
                        if (!popped) {
                            backDispatcher?.onBackPressed()
                        }
                    }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            if (isStoppedUi) {
                BreakfastButtonRes(
                    onClick = {
                        val orderId = (collectorState as? com.breakfast.utils.Result.Success)?.data?.data?.order?.id
                        if (orderId != null) {
                            val tax = taxText.value.toDoubleOrNull() ?: 0.0
                            val delivery = deliveryText.value.toDoubleOrNull() ?: 0.0
                            val total = totalText.value.toDoubleOrNull() ?: 0.0
                            viewModel.closeCollecting(orderId, tax, delivery, total)
                        }
                    },
                    enabled = true,
                    isHasObserver = false,
                    iconRes = com.breakfast.R.drawable.xmark_circle_fill,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 16.dp)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                    Text(text = stringResource(id = com.breakfast.R.string.close_order))
                }
            } else {
                BreakfastButtonRes(
                    onClick = {
                        val orderId = (collectorState as? com.breakfast.utils.Result.Success)?.data?.data?.order?.id
                        if (orderId != null) {
                            viewModel.stopCollecteing(orderId)
                        }
                    },
                    enabled = true,
                    isHasObserver = false,
                    iconRes = com.breakfast.R.drawable.xmark_circle_fill,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 16.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(text = stringResource(id = com.breakfast.R.string.stop_collecting))
                }
            }
        }

    ) { paddingValues ->
        CollectorDetailsContent(
            state = collectorState,
            onBack = {
                val popped = navController?.popBackStack() ?: false
                if (!popped) {
                    backDispatcher?.onBackPressed()
                }
            },
            onStopCollecting = {
                val orderId = (collectorState as? com.breakfast.utils.Result.Success)?.data?.data?.order?.id
                if (orderId != null) {
                    viewModel.stopCollecteing(orderId)
                }
            },
            onCloseCollecting = {
                val orderId = (collectorState as? com.breakfast.utils.Result.Success)?.data?.data?.order?.id
                if (orderId != null) {
                    val tax = taxText.value.toDoubleOrNull() ?: 0.0
                    val delivery = deliveryText.value.toDoubleOrNull() ?: 0.0
                    val total = totalText.value.toDoubleOrNull() ?: 0.0
                    viewModel.closeCollecting(orderId, tax, delivery, total)
                }
            },
            showStoppedUi = isStoppedUi,
            collectorTax = taxText.value,
            collectorDelivery = deliveryText.value,
            collectorTotal = totalText.value,
            onTaxChange = {
                taxText.value = it
                taxError.value = if (it.isNotEmpty() && it.toDoubleOrNull() == null) {
                    "Invalid number"
                } else null
            },
            onDeliveryChange = {
                deliveryText.value = it
                deliveryError.value = if (it.isNotEmpty() && it.toDoubleOrNull() == null) {
                    "Invalid number"
                } else null
            },
            onTotalChange = { totalText.value = it },
            onShowUsers = { users ->
                selectedUsers.value = users
                showUsersDialog.value = true
            },
            onCustomItemClick = { payload ->
                // navigate with payload fields as query params
                navController?.navigate(
                    "custom_item?orderId=${payload.orderID}&name=${payload.name}&price=${payload.price}&quantity=${payload.quantity}&note=${payload.note}&storeId=${payload.storeID}&userId=${payload.userID}&orderItemId=${payload.orderItemID}"
                )
            },
            onDismissUsers = { showUsersDialog.value = false },
            showUsersDialog = showUsersDialog.value,
            selectedUsers = selectedUsers.value,
            collectorTaxError = taxError.value,
            collectorDeliveryError = deliveryError.value,
            onRetry = { viewModel.fetchCollectorItems() },
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        )
    }
}

@Composable
private fun CollectorDetailsContent(
    state: com.breakfast.utils.Result<com.breakfast.models.ApiResponse<com.breakfast.models.CollectorModel>>?,
    onBack: () -> Unit,
    onStopCollecting: () -> Unit,
    onCloseCollecting: () -> Unit,
    showStoppedUi: Boolean,
    collectorTax: String,
    collectorDelivery: String,
    collectorTotal: String,
    collectorTaxError: String?,
    collectorDeliveryError: String?,
    onTaxChange: (String) -> Unit,
    onDeliveryChange: (String) -> Unit,
    onTotalChange: (String) -> Unit,
    onShowUsers: (List<com.breakfast.models.Collector>) -> Unit,
    onCustomItemClick: (CustomItemPayload) -> Unit,
    onDismissUsers: () -> Unit,
    showUsersDialog: Boolean,
    selectedUsers: List<com.breakfast.models.Collector>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        when (val s = state) {
            is Result.Loading -> {
                CircularProgressIndicator()
            }
            is Result.Error -> {
                BreakfastEmptyState(
                    iconRes = R.drawable.no_internet,
                    title = s.message ?: stringResource(id = R.string.failed_load_collector),
                    showButton = true,
                    buttonText = stringResource(id = R.string.retry),
                    onButtonClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                )
            }
            is Result.Success -> {
                val data = s.data.data
                if (data == null || (data.orderItems.isNullOrEmpty() && data.customOrderItems.isNullOrEmpty())) {
                    BreakfastEmptyState(
                        iconRes = R.drawable.no_orders,
                        title = stringResource(id = R.string.no_assigned_items),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        // Order items section
                        if (!data.orderItems.isNullOrEmpty()) {
                            Text(
                                text = stringResource(id = R.string.order_items),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            // header row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = colorResource(R.color.blue_ribbon),
                                        shape = RoundedCornerShape(
                                            topStart = 24.dp,
                                            topEnd = 24.dp
                                        )
                                    )
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CollectorTableHeader(showAction = true)
                            }
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                data.orderItems.forEachIndexed { index, item ->
                                    val isLast = index == data.orderItems.lastIndex
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                color = Color(0xFFF0F0F0),
                                                shape = if (isLast) RoundedCornerShape(
                                                    bottomStart = 24.dp,
                                                    bottomEnd = 24.dp
                                                ) else RoundedCornerShape(0.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        CollectorOrderItemCard(
                                            item = OrderHistoryItemDisplayAdapter(item),
                                            onShowUsers = onShowUsers
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.padding(vertical = 16.dp))

                        // Custom items section
                        if (!data.customOrderItems.isNullOrEmpty()) {
                            Text(
                                text = stringResource(id = R.string.custom_items),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = Color(0xFF0D5BFF),
                                        shape = RoundedCornerShape(
                                            topStart = 24.dp,
                                            topEnd = 24.dp
                                        )
                                    )
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = stringResource(R.string.descriptions), color = colorResource(id = R.color.card_bg), modifier = Modifier.weight(1f))
                            }
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                data.customOrderItems.forEachIndexed { index, item ->
                                    val isLast = index == data.customOrderItems.lastIndex
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                color = Color(0xFFF0F0F0),
                                                shape = if (isLast) RoundedCornerShape(
                                                    bottomStart = 24.dp,
                                                    bottomEnd = 24.dp
                                                ) else RoundedCornerShape(0.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(
                                                    colorResource(id = R.color.card_bg),
                                                    shape = RoundedCornerShape(20.dp)
                                                )
                                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = item.itemName ?: "--",
                                                    modifier = Modifier.weight(0.9f)
                                                )
                                                IconButton(onClick = {
                                                    val payload = CustomItemPayload(
                                                        name = item.itemName ?: "",
                                                        orderID = data.order?.id ?: 0,
                                                        storeID = data.order?.store?.id ?: 0,
                                                        userID = item.users?.first()?.id ?: 0,
                                                        price = item.price ?: 0.0,
                                                        quantity = item.quantity ?: 0,
                                                        note = item.note ?: "",
                                                        orderItemID = item.orderItemID ?: 0
                                                    )
                                                    onCustomItemClick(payload)
                                                }) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.plus),
                                                        contentDescription = null,
                                                        tint = Color(0xFF0D5BFF),
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .padding(end = 8.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        // Order info section
                        if (showStoppedUi) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(colorResource(id = R.color.card_bg), RoundedCornerShape(24.dp))
                                    .border(width = 1.dp, color = Color(0xFFE5E5E5), shape = RoundedCornerShape(24.dp))
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.order_info),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                Text(
                                    text = "${stringResource(com.breakfast.R.string.quantity)}: ${data.count}",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                BreakfastOutlinedTextField(
                                    value = collectorTax,
                                    onValueChange = onTaxChange,
                                    label = stringResource(id = R.string.tax),
                                    isError = collectorTaxError != null,
                                    errorText = collectorTaxError,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                )

                                BreakfastOutlinedTextField(
                                    value = collectorDelivery,
                                    onValueChange = onDeliveryChange,
                                    label = stringResource(id = R.string.delivery),
                                    isError = collectorDeliveryError != null,
                                    errorText = collectorDeliveryError,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                )

                                Divider(modifier = Modifier.padding(vertical = 12.dp))

                                BreakfastOutlinedTextField(
                                    value = data.order?.totalPrice?.toString() ?: "",
                                    onValueChange = { onTotalChange },
                                    label = stringResource(id = R.string.total),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
            null -> {}
        }
    }
    if (showUsersDialog) {
        AlertDialog(
            onDismissRequest = onDismissUsers,
            confirmButton = {
                TextButton(onClick = onDismissUsers) {
                    Text(text = stringResource(com.breakfast.R.string.submit))
                }
            },
            title = { Text(text = stringResource(com.breakfast.R.string.users)) },
            text = {
                Column {
                    selectedUsers.forEach {
                        Text(text = it.name ?: "-")
                    }
                }
            }
        )
    }
}



@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun CollectorDetailsScreenPreview() {
    val fakeOrderItems = listOf(
        OrderHistoryItemModel(
            itemName = "فول بلدي",
            quantity = 1,
            price = 7.0,
            totalPrice = 7.0,
            note = null,
            users = emptyList(),
            orderItemID = 1
        ),
        OrderHistoryItemModel(
            itemName = "فول اسكندراني بلدي",
            quantity = 2,
            price = 8.0,
            totalPrice = 16.0,
            note = "ssssss",
            users = listOf(com.breakfast.models.Collector(id = 64, name = "iOS Tester", image = null)),
            orderItemID = 2
        )
    )
    val fakeCustom = listOf(
        OrderHistoryItemModel(
            itemName = "new test",
            quantity = null,
            price = null,
            totalPrice = null,
            note = null,
            users = emptyList(),
            orderItemID = 3
        )
    )
    val fakeResponse = com.breakfast.utils.Result.Success(
        com.breakfast.models.ApiResponse(
            data = com.breakfast.models.CollectorModel(
                orderItems = fakeOrderItems,
                customOrderItems = fakeCustom,
                order = null,
                count = null
            ),
            message = null,
            statusCode = 200
        )
    )
    CollectorDetailsContent(
        state = fakeResponse,
        onBack = {},
        onStopCollecting = {},
        onCloseCollecting = {},
        showStoppedUi = true,
        collectorTax = "0.0",
        collectorDelivery = "0.0",
        collectorTotal = "0.0",
        collectorTaxError = null,
        collectorDeliveryError = null,
        onTaxChange = {},
        onDeliveryChange = {},
        onTotalChange = {},
        onShowUsers = {},
        onCustomItemClick = {},
        onDismissUsers = {},
        showUsersDialog = false,
        selectedUsers = emptyList(),
        onRetry = {},
        modifier = Modifier.padding(16.dp)
    )
}

private data class OrderHistoryItemDisplayAdapter(
    private val src: OrderHistoryItemModel
) : CollectorOrderDisplayItem {

    override val displayName: String? get() = src.itemName ?: src.note
    override val displayQuantity: Int? get() = src.quantity
    override val displayPrice: Double? get() = src.price
    override val displayTotal: Double? get() = src.totalPrice
    override val displayNote: String? get() = src.note
    override val displayUsers: List<com.breakfast.models.Collector>? get() =
        src.users?.mapNotNull { apiUser ->
            val id = apiUser.id ?: return@mapNotNull null
            com.breakfast.models.Collector(
                id = id,
                name = apiUser.name ?: "",
                image = apiUser.image
            )
        }
}