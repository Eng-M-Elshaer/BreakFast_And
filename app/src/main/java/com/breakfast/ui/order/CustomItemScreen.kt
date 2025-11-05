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

/**
 * Screen for creating a custom item with a name, price, quantity and optional note.
 * The [orderId] should reference an existing order; when the operation succeeds,
 * [onCreate] is invoked so the caller can navigate back or refresh.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CustomItemScreen(
    orderId: Int = 1,
    onCreate: () -> Unit = {}
) {
    val viewModel: OrderViewModel = viewModel(factory = OrderViewModel.Factory(ApiClient.apiService))
    val name = remember { mutableStateOf("") }
    val price = remember { mutableStateOf("") }
    val quantity = remember { mutableStateOf("") }
    val note = remember { mutableStateOf("") }
    val customItemState by viewModel.customItemState.collectAsState()

    // Invoke callback when custom item is successfully created
    LaunchedEffect(customItemState) {
        if (customItemState is Result.Success) {
            onCreate()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.custom_item)) }) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            OutlinedTextField(value = name.value, onValueChange = { name.value = it }, label = { Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.item_name)) })
            OutlinedTextField(value = price.value, onValueChange = { price.value = it.filter { ch -> ch.isDigit() || ch == '.' } }, label = { Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.price)) })
            OutlinedTextField(value = quantity.value, onValueChange = { quantity.value = it.filter { ch -> ch.isDigit() } }, label = { Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.quantity_label)) })
            OutlinedTextField(value = note.value, onValueChange = { note.value = it }, label = { Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.note_label)) })
            when (customItemState) {
                is Result.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                }
                is Result.Error -> {
                    Text(text = (customItemState as Result.Error).message ?: androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.error), modifier = Modifier.padding(top = 16.dp))
                    Button(onClick = {
                        val priceDouble = price.value.toDoubleOrNull() ?: 0.0
                        val qtyInt = quantity.value.toIntOrNull() ?: 1
                        viewModel.addCustomItem(orderId, name.value, priceDouble, qtyInt, note.value.takeIf { it.isNotBlank() })
                    }, modifier = Modifier.padding(top = 8.dp)) {
                        Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.retry_action))
                    }
                }
                is Result.Success, null -> {
                    Button(onClick = {
                        val priceDouble = price.value.toDoubleOrNull() ?: 0.0
                        val qtyInt = quantity.value.toIntOrNull() ?: 1
                        viewModel.addCustomItem(orderId, name.value, priceDouble, qtyInt, note.value.takeIf { it.isNotBlank() })
                    }, modifier = Modifier.padding(top = 16.dp)) {
                        Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.add_custom_item))
                    }
                }
            }
        }
    }
}