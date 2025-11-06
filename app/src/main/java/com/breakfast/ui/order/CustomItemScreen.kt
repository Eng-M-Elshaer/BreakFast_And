package com.breakfast.ui.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.viewmodel.compose.viewModel
import com.breakfast.network.ApiClient
import com.breakfast.utils.Result
import com.breakfast.viewmodel.OrderViewModel
import com.breakfast.designsystem.BreakfastButtonRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.res.stringResource
import com.breakfast.R
import com.breakfast.models.CustomItemPayload
import com.breakfast.ui.components.ErrorDialog

/**
 * Screen for creating a custom item with a name, price, quantity and optional note.
 * The [orderId] should reference an existing order; when the operation succeeds,
 * [onCreate] is invoked so the caller can navigate back or refresh.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CustomItemScreen(
    orderId: Int = 1,
    onCreate: () -> Unit = {},
    navController: androidx.navigation.NavController? = null,
    incoming: CustomItemPayload? = null
) {
    val viewModel: OrderViewModel = viewModel(factory = OrderViewModel.Factory(ApiClient.apiService))
    val name = remember { mutableStateOf(incoming?.name ?: "") }
    val price = remember { mutableStateOf(if (incoming != null) incoming.price.toString() else "") }
    val quantity = remember { mutableStateOf(if (incoming != null && incoming.quantity != 0) incoming.quantity.toString() else "") }
    val note = remember { mutableStateOf(incoming?.note ?: "") }
    val customItemState by viewModel.customItemState.collectAsState()

    val showError = remember { mutableStateOf(false) }
    val errorMsg = remember { mutableStateOf("") }
    val defaultErrorText = stringResource(id = R.string.something_went_wrong)

    LaunchedEffect(customItemState) {
        when (customItemState) {
            is Result.Success -> {
                onCreate()
            }
            is Result.Error -> {
                errorMsg.value = (customItemState as Result.Error).message ?: defaultErrorText
                showError.value = true
            }
            else -> Unit
        }
    }

    val isButtonDisabled = { n: String, q: String, p: String ->
        n.isBlank() || q.isBlank() || p.isBlank()
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = {
                    Text(
                        text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.custom_item),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFF3F1F6))
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = stringResource(id = R.string.name), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = name.value,
                onValueChange = { name.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    containerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(id = R.string.quantity_label), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = quantity.value,
                onValueChange = { txt -> quantity.value = txt.filter { it.isDigit() } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    containerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(id = R.string.price_label), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = price.value,
                onValueChange = { txt -> price.value = txt.filter { ch -> ch.isDigit() || ch == '.' } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    containerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(id = R.string.add_note_optional), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = note.value,
                onValueChange = { note.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    containerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Button section fixed at bottom visually
            BreakfastButtonRes(
                iconRes = com.breakfast.R.drawable.plus,
                isHasObserver = isButtonDisabled(name.value, quantity.value, price.value),
                onClick = {
                    val priceDouble = price.value.toDoubleOrNull() ?: 0.0
                    val qtyInt = quantity.value.toIntOrNull() ?: 1
                    val targetOrderId = if (incoming != null && incoming.orderID != 0) incoming.orderID else orderId
                    viewModel.addCustomItem(
                        targetOrderId,
                        name.value,
                        priceDouble,
                        qtyInt,
                        note.value.takeIf { it.isNotBlank() }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Text(text = stringResource(R.string.add_custom_item))
            }

            if (showError.value) {
                ErrorDialog(
                    visible = true,
                    title = stringResource(id = R.string.something_went_wrong),
                    message = errorMsg.value,
                    onDismiss = {showError.value = false},
                    buttonText = stringResource(id = R.string.submit),
                )
            }
        }
    }
}
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun CustomItemScreenPreview() {
    CustomItemScreen(
        orderId = 1,
        onCreate = {},
        incoming = CustomItemPayload(
            name = "Custom Bread",
            orderID = 1,
            storeID = 10,
            userID = 64,
            price = 12.5,
            quantity = 2,
            note = "brown",
            orderItemID = 0
        )
    )
}