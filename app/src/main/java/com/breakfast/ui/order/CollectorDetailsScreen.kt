package com.breakfast.ui.order

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.breakfast.R
import com.breakfast.designsystem.BreakfastButtonRes
import com.breakfast.models.CustomItemPayload

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
    val viewModel: OrderViewModel = viewModel(factory = OrderViewModel.Factory(ApiClient.apiService))
    val collectorState by viewModel.collectorState.collectAsState()
    val showUsersDialog = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val selectedUsers = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<List<com.breakfast.models.Collector>>(emptyList()) }
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    // Load collector items on first composition
    LaunchedEffect(Unit) { viewModel.fetchCollectorItems() }

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
    onShowUsers: (List<com.breakfast.models.Collector>) -> Unit,
    onCustomItemClick: (CustomItemPayload) -> Unit,
    onDismissUsers: () -> Unit,
    showUsersDialog: Boolean,
    selectedUsers: List<com.breakfast.models.Collector>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        when (val s = state) {
            is Result.Loading -> {
                CircularProgressIndicator()
            }
            is Result.Error -> {
                Text(text = s.message ?: androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.failed_load_collector))
            }
            is Result.Success -> {
                val data = s.data.data
                if (data == null || (data.orderItems.isNullOrEmpty() && data.customOrderItems.isNullOrEmpty())) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.no_assigned_items))
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        // Order items section
                        if (!data.orderItems.isNullOrEmpty()) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.order_items),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            // header row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = colorResource(com.breakfast.R.color.blue_ribbon),
                                        shape = RoundedCornerShape(
                                            topStart = 24.dp,
                                            topEnd = 24.dp
                                        )
                                    )
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(id = R.string.name),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 4.dp)
                                )
                                Text(
                                    text = stringResource(id = R.string.quantity),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                )
                                Text(
                                    text = stringResource(id = R.string.price),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                )
                                Text(
                                    text = stringResource(id = R.string.total),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                )
                                Text(
                                    text = stringResource(id = R.string.action),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                )
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
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.White, RoundedCornerShape(20.dp))
                                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = item.itemName ?: "--",
                                                    fontSize = 12.sp,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = (item.quantity ?: 0).toString(),
                                                    fontSize = 12.sp,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = "${item.price ?: 0.0} EGP",
                                                    fontSize = 12.sp,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = "${item.totalPrice ?: 0.0} EGP",
                                                    fontSize = 12.sp,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                IconButton(onClick = { onShowUsers(item.users ?: emptyList()) }) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.ic_info),
                                                        contentDescription = null,
                                                        tint = Color(0xFF0D5BFF),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                            if (!item.note.isNullOrEmpty()) {
                                                Spacer(modifier = Modifier.size(6.dp))
                                                Text(
                                                    text = "${stringResource(com.breakfast.R.string.note)}: ${item.note}",
                                                    color = colorResource(com.breakfast.R.color.punch),
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.padding(vertical = 16.dp))

                        // Custom items section
                        if (!data.customOrderItems.isNullOrEmpty()) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.custom_items),
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
                                Text(text = "Descriptions", color = Color.White, modifier = Modifier.weight(1f))
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
                                                    Color.White,
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
                                                            .weight(0.1f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    BreakfastButtonRes(
                        onClick = onStopCollecting,
                        enabled = true,
                        isHasObserver = false,
                        iconRes = com.breakfast.R.drawable.xmark_circle_fill,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        androidx.compose.material3.Text(text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.stop_collecting))
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
        onShowUsers = {},
        onCustomItemClick = {},
        onDismissUsers = {},
        showUsersDialog = false,
        selectedUsers = emptyList(),
        modifier = Modifier.padding(16.dp)
    )
}