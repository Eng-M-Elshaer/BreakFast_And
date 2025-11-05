package com.breakfast.ui.order

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
/**
 * Screen for collectors/drivers to view assigned items and user details. This screen
 * fetches the current collector items from the backend and displays both regular and
 * custom order items. The list is read-only; there are no actions in this demo.
 */
@Composable
fun CollectorDetailsScreen() {
    val viewModel: OrderViewModel = viewModel(factory = OrderViewModel.Factory(ApiClient.apiService))
    val collectorState by viewModel.collectorState.collectAsState()

    // Load collector items on first composition
    LaunchedEffect(Unit) { viewModel.fetchCollectorItems() }

    Scaffold(
        topBar = { TopAppBar(title = { Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.collector_details)) }) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            when (val state = collectorState) {
                is Result.Loading -> {
                    CircularProgressIndicator()
                }
                is Result.Error -> {
                    Text(text = state.message ?: androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.failed_load_collector))
                }
                is Result.Success -> {
                    val data = state.data.data
                    if (data == null || (data.orderItems.isNullOrEmpty() && data.customOrderItems.isNullOrEmpty())) {
                        Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.no_assigned_items))
                    } else {
                        if (!data.orderItems.isNullOrEmpty()) {
                            Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.order_items))
                            LazyColumn {
                                items(data.orderItems!!) { item ->
                                    CollectorItemRow(item = item)
                                }
                            }
                        }
                        if (!data.customOrderItems.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.padding(8.dp))
                            Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.custom_items))
                            LazyColumn {
                                items(data.customOrderItems!!) { item ->
                                    CollectorItemRow(item = item)
                                }
                            }
                        }
                    }
                }
                null -> {
                    // initial state: do nothing
                }
            }
        }
    }
}

/**
 * Helper composable to display a collector's order item row.
 */
@Composable
private fun CollectorItemRow(item: OrderHistoryItemModel) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.itemName ?: androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.item_name))
            Text(text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.qty_label) + ": ${item.quantity ?: 0}")
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.price_label) + ": ${item.price ?: 0.0}")
            Text(text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.total) + ": ${item.totalPrice ?: 0.0}")
        }
    }
}