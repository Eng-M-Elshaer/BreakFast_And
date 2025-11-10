
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import com.breakfast.designsystem.BreakfastOutlinedTextField
import androidx.compose.ui.text.input.KeyboardType
import com.breakfast.designsystem.CollectorTableHeader
import com.breakfast.designsystem.CollectorOrderItemCard
import com.breakfast.designsystem.CollectorOrderDisplayItem
import com.breakfast.models.OrderModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddToOrderScreenContent(
    itemsList: List<StoreItemModel>,
    usersList: List<PersonModel>,
    orderItems: List<OrderModel>,
    onBack: () -> Unit = {},
    onSubmit: (selectedItem: StoreItemModel, quantity: Int, note: String, selectedUser: PersonModel?) -> Unit = { _,_,_,_ -> },
    onDeleteItem: (Int) -> Unit = {}
) {
    var selectedItem by remember { mutableStateOf<StoreItemModel?>(null) }
    var isItemsExpanded by remember { mutableStateOf(false) }
    var quantity by remember { mutableStateOf("1") }
    var note by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf<PersonModel?>(null) }
    var isUsersExpanded by remember { mutableStateOf(false) }
    // --- State for delete dialog ---
    var itemToDeleteId by remember { mutableStateOf<Int?>(null) }
    var showDelete by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))
        ExposedDropdownMenuBox(
            expanded = isItemsExpanded,
            onExpandedChange = { isItemsExpanded = !isItemsExpanded },
        ) {
            BreakfastOutlinedTextField(
                value = if (selectedItem != null) "${selectedItem?.name} - ${selectedItem?.price} EGP" else "",
                onValueChange = {},
                label = stringResource(id = R.string.select_item),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailing = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isItemsExpanded)
                }
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
        Spacer(modifier = Modifier.height(6.dp))
        BreakfastOutlinedTextField(
            value = quantity,
            onValueChange = { value -> quantity = value.filter { it.isDigit() } },
            label = stringResource(id = R.string.quantity),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Spacer(modifier = Modifier.height(6.dp))
        BreakfastOutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = stringResource(id = R.string.add_note_optional),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Spacer(modifier = Modifier.height(6.dp))
        ExposedDropdownMenuBox(
            expanded = isUsersExpanded,
            onExpandedChange = { isUsersExpanded = !isUsersExpanded },
        ) {
            BreakfastOutlinedTextField(
                value = selectedUser?.name ?: "",
                onValueChange = {},
                label = stringResource(id = R.string.order_for_others),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailing = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isUsersExpanded)
                }
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

        val isManualItem = (selectedItem?.id ?: -1) == 0
        val isFormValid = selectedItem != null && quantity.isNotBlank() && !(isManualItem && note.isBlank())

        BreakfastButtonRes(
            onClick = {
                val item = selectedItem ?: return@BreakfastButtonRes
                if ((item.id ?: -1) == 0 && note.isBlank()) {
                    return@BreakfastButtonRes
                }
                val qtyInt = quantity.toIntOrNull() ?: 1
                onSubmit(item, qtyInt, note, selectedUser)
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
        Text(text = stringResource(id = R.string.added_items), fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            CollectorTableHeader(showAction = true)

            orderItems.forEachIndexed { index, item ->
                val isLast = index == orderItems.lastIndex
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = colorResource(id = com.breakfast.R.color.background_grey),
                            shape = if (isLast) RoundedCornerShape(
                                bottomStart = 24.dp,
                                bottomEnd = 24.dp
                            ) else RoundedCornerShape(0.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    CollectorOrderItemCard(
                        item = AddOrderItemDisplayAdapter(item),
                        showDeleteAction = true,
                        onDeleteItem = {
                            val id = item.id
                            if (id != null) {
                                itemToDeleteId = id
                                showDelete = true
                            }
                        }
                    )
                }
            }
        }

        if (showDelete && itemToDeleteId != null) {
            AlertDialog(
                onDismissRequest = { showDelete = false },
                confirmButton = {
                    TextButton(onClick = {
                        val targetId = itemToDeleteId
                        showDelete = false
                        if (targetId != null) {
                            onDeleteItem(targetId)
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
    val context = LocalContext.current
    val viewModel: OrderViewModel = viewModel(factory = OrderViewModel.Factory(ApiClient.apiService, context))
    // Observe state flows from the ViewModel.
    val storeItemsState by viewModel.storeItemsState.collectAsState()
    val usersState by viewModel.usersState.collectAsState()
    val addItemState by viewModel.addItemState.collectAsState()
    val orderItemsState by viewModel.orderItemsState.collectAsState()
    val deleteItemState by viewModel.deleteItemState.collectAsState()
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

    val itemsList: List<StoreItemModel> = when (val state = storeItemsState) {
        is Result.Success -> state.data.data ?: emptyList()
        else -> emptyList()
    }
    val usersList: List<PersonModel> = when (val u = usersState) {
        is Result.Success -> u.data.data ?: emptyList()
        else -> emptyList()
    }
    val orderItems: List<OrderModel> = when (val s = orderItemsState) {
        is Result.Success -> s.data.data ?: emptyList()
        else -> emptyList()
    }
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
        Box(modifier = Modifier.padding(paddingValues)) {
            AddToOrderScreenContent(
                itemsList = itemsList,
                usersList = usersList,
                orderItems = orderItems,
                onBack = {
                    val popped = navController?.popBackStack() ?: false
                    if (!popped) {
                        backDispatcher?.onBackPressed()
                    }
                },
                onSubmit = { selectedItem, quantity, note, selectedUser ->
                    val itemId = selectedItem.id ?: 0
                    viewModel.addItem(
                        orderId = orderId,
                        itemId = if (itemId == 0) null else itemId,
                        quantity = quantity,
                        price = selectedItem.price,
                        note = if (note.isBlank()) "" else note,
                        userId = selectedUser?.id
                    )
                },
                onDeleteItem = { itemId ->
                    viewModel.removeOrderItem(itemId)
                    orderId?.let { viewModel.fetchOrderItems(it) }
                }
            )
        }
    }
}

@Preview(showBackground = true, name = "AddToOrderScreen Preview")
@Composable
private fun AddToOrderScreenPreview() {
    MaterialTheme {
        AddToOrderScreenContent(
            itemsList = listOf(StoreItemModel(id=1, name="Foul Sandwich", price=20.0)),
            usersList = emptyList(),
            orderItems = listOf(
                OrderModel(id=1, orderID=10, itemName="Foul Sandwich", quantity=2, price=20.0, total=40.0, note="no onions", other = null)
            )
        )
    }
}

private data class AddOrderItemDisplayAdapter(
    private val src: OrderModel
) : CollectorOrderDisplayItem {
    override val displayName: String? get() = src.itemName ?: src.note
    override val displayQuantity: Int? get() = src.quantity
    override val displayPrice: Double? get() = src.price
    override val displayTotal: Double? get() = src.total
    override val displayNote: String? get() = src.note
    override val displayUsers: List<Nothing>? get() = null
}