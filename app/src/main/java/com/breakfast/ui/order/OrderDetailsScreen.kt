package com.breakfast.ui.order

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.breakfast.models.OrderModel
import com.breakfast.network.ApiClient
import com.breakfast.utils.Result
import com.breakfast.viewmodel.OrderViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
/**
 * Screen showing details of the current order including its items. It also provides
 * functionality to add new items and close the order. The order ID is assumed to be
 * the user's current active order. When the order is successfully closed, [onClose]
 * is invoked so the caller can navigate away.
 */
@Composable
fun OrderDetailsScreen(
    orderId: Int = 1,
    onAddItem: () -> Unit = {},
    onClose: () -> Unit = {},
    navController: androidx.navigation.NavController? = null
) {
    val viewModel: OrderViewModel = viewModel(factory = OrderViewModel.Factory(ApiClient.apiService))
    val orderItemsState by viewModel.orderItemsState.collectAsState()
    val statusState by viewModel.statusState.collectAsState()

    // Load order items when the composable enters the composition
    LaunchedEffect(orderId) { viewModel.fetchOrderItems(orderId) }

    // Invoke callback when the order status update operation completes successfully
    LaunchedEffect(statusState) {
        if (statusState is Result.Success) {
            onClose()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.order_details)) }) },
        floatingActionButton = {
            Button(onClick = onAddItem) { Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.add_item)) }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            when (val state = orderItemsState) {
                is Result.Loading -> {
                    CircularProgressIndicator()
                }
                is Result.Error -> {
                    Text(text = state.message ?: androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.failed_load_order_items))
                }
                is Result.Success -> {
                    val items = state.data.data ?: emptyList()
                    if (items.isEmpty()) {
                        Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.no_items_in_your_order))
                    } else {
                        LazyColumn {
                            items(items) { item ->
                                OrderItemRow(
                                    item = item,
                                    onAssign = { orderItemId ->
                                        navController?.navigate("assign_item/${orderId}/${orderItemId}")
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.padding(8.dp))
                    Row {
                        Button(onClick = { viewModel.updateStatus(orderId, "stop") }) {
                            Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.stop_order))
                        }
                        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                        Button(onClick = { viewModel.updateStatus(orderId, "close") }) {
                            Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.close_order))
                        }
                    }
                }
                null -> {
                    // Do nothing for initial state
                }
            }
        }
    }
}

/**
 * Helper composable to display an individual order item row.
 */
@Composable
private fun OrderItemRow(
    item: OrderModel,
    onAssign: (orderItemId: Int) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.itemName ?: androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.item_name))
            Text(text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.qty_label) + ": ${item.quantity ?: 0}")
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.price_label) + ": ${item.price ?: 0.0}")
            Text(text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.total) + ": ${item.total ?: 0.0}")
        }
        val itemId = item.id
        if (itemId != null) {
            Button(onClick = { onAssign(itemId) }, modifier = Modifier.padding(start = 8.dp)) {
                Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.assign))
            }
        }
    }
}