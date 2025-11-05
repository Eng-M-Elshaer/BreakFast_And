
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.breakfast.models.StoreItemModel
import com.breakfast.models.PersonModel
import com.breakfast.network.ApiClient
import com.breakfast.utils.Result
import com.breakfast.viewmodel.OrderViewModel
import com.breakfast.R
import com.breakfast.designsystem.BreakfastButtonRes
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
/**
 * Screen for displaying store items and adding them to an order. This version additionally
 * allows selecting a user to assign the item to at the time of adding. The user list is
 * fetched from the backend and displayed with radio buttons. When an item is added, the
 * selected user's ID (if any) is passed along to the server.
 *
 * @param orderId The ID of the current order if it already exists (nullable).
 * @param storeId The ID of the store from which to load items.
 * @param onItemAdded Callback invoked after a successful add operation to allow navigation.
 */
@Composable
fun AddToOrderScreen(
    orderId: Int? = null,
    storeId: Int = 1,
    navController: NavController? = null,
    onItemAdded: () -> Unit = {}
) {
    // Obtain a ViewModel scoped to this screen. A factory is used to inject ApiService.
    val viewModel: OrderViewModel = viewModel(factory = OrderViewModel.Factory(ApiClient.apiService))
    // Observe state flows from the ViewModel.
    val storeItemsState by viewModel.storeItemsState.collectAsState()
    val usersState by viewModel.usersState.collectAsState()
    val addItemState by viewModel.addItemState.collectAsState()
    val orderItemsState by viewModel.orderItemsState.collectAsState()
    val deleteItemState by viewModel.deleteItemState.collectAsState()
    var selectedItem by remember { mutableStateOf<StoreItemModel?>(null) }
    var isItemsExpanded by remember { mutableStateOf(false) }
    var quantity by remember { mutableStateOf("1") }
    var note by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf<PersonModel?>(null) }
    var isUsersExpanded by remember { mutableStateOf(false) }
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    // Trigger loading of store items when the composable first enters the composition.
    LaunchedEffect(storeId) { viewModel.fetchStoreItems(storeId) }
    // Trigger loading of users list when the screen appears.
    LaunchedEffect(Unit) { viewModel.fetchUsers() }
    // If an item was successfully added, reload order items and clear form fields for better UX
    LaunchedEffect(addItemState) {
        if (addItemState is Result.Success) {
            // reload order items so the table shows the new one
            orderId?.let { viewModel.fetchOrderItems(orderId) }

            // clear form for better UX
            selectedItem = null
            quantity = "1"
            note = ""
            selectedUser = null
        }
    }
    // If an item was successfully deleted, reload order items
    LaunchedEffect(deleteItemState) {
        if (deleteItemState is Result.Success) {
            orderId?.let { viewModel.fetchOrderItems(orderId) }
        }
    }

    // Fetch order items if we have an orderId
    LaunchedEffect(orderId) {
        orderId?.let { viewModel.fetchOrderItems(orderId) }
    }

    // --- State for delete dialog ---
    var itemToDeleteId by remember { mutableStateOf<Int?>(null) }
    var showDelete by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.add_to_order)) },
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
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 1. Items dropdown (Select Item)
            Text(text = stringResource(id = R.string.select_item), fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            val itemsList: List<StoreItemModel> = when (val state = storeItemsState) {
                is Result.Success -> state.data.data ?: emptyList()
                else -> emptyList()
            }
            ExposedDropdownMenuBox(
                expanded = isItemsExpanded,
                onExpandedChange = { isItemsExpanded = !isItemsExpanded },
            ) {
                OutlinedTextField(
                    value = "${selectedItem?.name} - ${selectedItem?.price} EGP",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(20.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isItemsExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = isItemsExpanded,
                    onDismissRequest = { isItemsExpanded = false }
                ) {
                    itemsList.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(text ="${item.name} - ${item.price} EGP") },
                            onClick = {
                                selectedItem = item
                                isItemsExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // 2. Quantity
            Text(text = stringResource(id = R.string.quantity), fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = quantity,
                onValueChange = { value -> quantity = value.filter { it.isDigit() } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 3. Note (optional)
            Text(text = stringResource(id = R.string.add_note_optional), fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                placeholder = { Text(text = stringResource(id = R.string.note_placeholder)) }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 4. Order for others (users dropdown)
            Text(text = stringResource(id = R.string.order_for_others), fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            val usersList: List<PersonModel> = when (val u = usersState) {
                is Result.Success -> u.data.data ?: emptyList()
                else -> emptyList()
            }
            ExposedDropdownMenuBox(
                expanded = isUsersExpanded,
                onExpandedChange = { isUsersExpanded = !isUsersExpanded },
            ) {
                OutlinedTextField(
                    value = selectedUser?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(20.dp),
                    placeholder = { Text(text = stringResource(id = R.string.name)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isUsersExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = isUsersExpanded,
                    onDismissRequest = { isUsersExpanded = false }
                ) {
                    usersList.forEach { user ->
                        DropdownMenuItem(
                            text = { Text(text = user.name ?: user.email ?: "") },
                            onClick = {
                                selectedUser = user
                                isUsersExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            // 5. Submit button (Breakfast)
            val isManualItem = (selectedItem?.id ?: -1) == 0
            val isFormValid = selectedItem != null && quantity.isNotBlank() && !(isManualItem && note.isBlank())

            BreakfastButtonRes(
                onClick = {
                    val item = selectedItem ?: return@BreakfastButtonRes
                    // if it's the manual/custom item (id == 0) and note is empty, do nothing
                    if ((item.id ?: -1) == 0 && note.isBlank()) {
                        return@BreakfastButtonRes
                    }
                    val itemId = item.id ?: 0
                    val qtyInt = quantity.toIntOrNull() ?: 1
                    viewModel.addItem(
                        orderId = orderId,
                        itemId = if (itemId == 0) null else itemId,
                        quantity = qtyInt,
                        price = item.price,
                        note = if (note.isBlank()) "" else note,
                        userId = selectedUser?.id
                    )
                },
                enabled = isFormValid,
                isHasObserver = !isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                iconRes = com.breakfast.R.drawable.plus_square_fill
            ) {
                Text(text = stringResource(id = R.string.submit))
            }
            Spacer(modifier = Modifier.height(20.dp))

            // 6. Table of added items
            Text(text = stringResource(id = R.string.added_items), fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(10.dp))
            val orderItems = when (val s = orderItemsState) {
                is Result.Success -> s.data.data ?: emptyList()
                else -> emptyList()
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0066FF))
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
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    )
                }
                orderItems.forEach { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF7F7F7))
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.itemName ?: item.note ?: "",
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = (item.quantity ?: 0).toString(),
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "${item.price ?: 0.0} EGP",
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "${item.total ?: 0.0} EGP",
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 4.dp),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "\uD83D\uDDD1",
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val id = item.id
                                        if (id != null) {
                                            itemToDeleteId = id
                                            showDelete = true
                                        }
                                    },
                                textAlign = TextAlign.Center,
                                color = Color.Red
                            )
                        }
                        if (!item.note.isNullOrBlank() && !item.itemName.isNullOrBlank()) {
                            Text(
                                text = stringResource(com.breakfast.R.string.note) + ": " + item.note,
                                fontSize = 12.sp,
                                color = colorResource(id = R.color.punch),
                                modifier = Modifier.padding(top = 6.dp, start = 4.dp)
                            )
                        }
                    }
                }
            }

            // --- Confirm Delete Dialog ---
            if (showDelete && itemToDeleteId != null) {
                AlertDialog(
                    onDismissRequest = { showDelete = false },
                    confirmButton = {
                        TextButton(onClick = {
                            val targetId = itemToDeleteId
                            showDelete = false
                            if (targetId != null) {
                                viewModel.removeOrderItem(targetId)
                                orderId?.let { viewModel.fetchOrderItems(it) }
                            }
                        }) {
                            Text(text = stringResource(id = R.string.yes))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDelete = false }) {
                            Text(text = stringResource(id = R.string.cancel))
                        }
                    },
                    title = { Text(text = stringResource(id = R.string.delete)) },
                    text = { Text(text = stringResource(id = R.string.are_you_sure_delete_item)) }
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "AddToOrderScreen Preview")
@Composable
private fun AddToOrderScreenPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // top part mock
            Text(text = "Select Item", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = "Foul Sandwich",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Quantity", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = "2",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Added Items", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0066FF))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Name", color = Color.White, fontSize = 12.sp, modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp))
                    Text(text = "Quantity", color = Color.White, fontSize = 12.sp, modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp), textAlign = TextAlign.Center)
                    Text(text = "Price", color = Color.White, fontSize = 12.sp, modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp), textAlign = TextAlign.Center)
                    Text(text = "Total", color = Color.White, fontSize = 12.sp, modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp), textAlign = TextAlign.Center)
                    Text(text = "Action", color = Color.White, fontSize = 12.sp, modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp), textAlign = TextAlign.End)
                }
                val mock = listOf(
                    Triple("Foul Sandwich", 2, 20.0),
                    Triple("Tea", 1, 5.0)
                )
                mock.forEach { (name, qty, price) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF7F7F7))
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = name, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Text(text = qty.toString(), fontSize = 12.sp, modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp), textAlign = TextAlign.Center)
                        Text(text = "$price EGP", fontSize = 12.sp, modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp), textAlign = TextAlign.Center)
                        Text(text = "${price * qty} EGP", fontSize = 12.sp, modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp), textAlign = TextAlign.Center)
                        Text(text = "\uD83D\uDDD1", modifier = Modifier.weight(1f), textAlign = TextAlign.End, color = Color.Red)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "AddToOrderScreen Preview – with note")
@Composable
private fun AddToOrderScreenPreviewWithNote() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(text = "Added Items", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0066FF))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Name", color = Color.White, fontSize = 12.sp, modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp))
                    Text("Quantity", color = Color.White, fontSize = 12.sp, modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp), textAlign = TextAlign.Center)
                    Text("Price", color = Color.White, fontSize = 12.sp, modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp), textAlign = TextAlign.Center)
                    Text("Total", color = Color.White, fontSize = 12.sp, modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp), textAlign = TextAlign.Center)
                    Text("Action", color = Color.White, fontSize = 12.sp, modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp), textAlign = TextAlign.End)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF7F7F7))
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Foul Sandwich", fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text(text = "1", fontSize = 12.sp, modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp), textAlign = TextAlign.Center)
                    Text(text = "20.0 EGP", fontSize = 12.sp, modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp), textAlign = TextAlign.Center)
                    Text(text = "20.0 EGP", fontSize = 12.sp, modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp), textAlign = TextAlign.Center)
                    Text(text = "\uD83D\uDDD1", modifier = Modifier.weight(1f), textAlign = TextAlign.End, color = Color.Red)
                }
                Text(
                    text = "Note: extra pickles, no onions",
                    fontSize = 11.sp,
                    color = colorResource(id = R.color.punch),
                    modifier = Modifier.padding(start = 24.dp, bottom = 12.dp)
                )
            }
        }
    }
}