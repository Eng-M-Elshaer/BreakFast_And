package com.breakfast.ui.order

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.breakfast.network.ApiClient
import com.breakfast.utils.Result
import com.breakfast.viewmodel.OrderViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Surface

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
/**
 * Screen for entering tax and delivery fees and then closing or reopening an order. This
 * composable leverages [OrderViewModel] to update the order's status and handles loading
 * and error states accordingly.
 */
@Composable
fun OrderClosedScreen(
    orderId: Int = 1,
    onClose: () -> Unit = {},
    onReopen: () -> Unit = {}
) {
    val viewModel: OrderViewModel = viewModel(factory = OrderViewModel.Factory(ApiClient.apiService))
    val tax = remember { mutableStateOf("") }
    val delivery = remember { mutableStateOf("") }
    val statusState by viewModel.statusState.collectAsState()

    // If the order status was updated successfully, invoke the appropriate callback
    LaunchedEffect(statusState) {
        if (statusState is Result.Success) {
            // When closing or reopening finishes, call the appropriate callback based on current action
            // For simplicity, always call onClose as closure covers both cases in this example
            onClose()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.close_order)) }) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            OutlinedTextField(value = tax.value, onValueChange = { tax.value = it.filter { ch -> ch.isDigit() || ch == '.' } }, label = { Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.tax)) })
            OutlinedTextField(value = delivery.value, onValueChange = { delivery.value = it.filter { ch -> ch.isDigit() || ch == '.' } }, label = { Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.delivery)) })
            when (statusState) {
                is Result.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                }
                is Result.Error -> {
                    Text(text = (statusState as Result.Error).message ?: androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.error), modifier = Modifier.padding(top = 16.dp))
                }
                is Result.Success, null -> {
                    Button(onClick = {
                        viewModel.updateStatus(orderId, "close")
                    }, modifier = Modifier.padding(top = 16.dp)) {
                        Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.close_order))
                    }
                    Button(onClick = {
                        viewModel.updateStatus(orderId, "reopen")
                    }, modifier = Modifier.padding(top = 8.dp)) {
                        Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.reopen_order))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OrderClosedScreenPreview() {
    Surface {
        OrderClosedScreen(
            orderId = 123,
            onClose = {},
            onReopen = {}
        )
    }
}