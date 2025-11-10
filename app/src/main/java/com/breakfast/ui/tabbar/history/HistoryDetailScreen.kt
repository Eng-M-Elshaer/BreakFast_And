import com.breakfast.ui.order.OrderInfoCard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.breakfast.models.OrderHistoryItemModel
import com.breakfast.network.ApiClient
import com.breakfast.utils.Result
import com.breakfast.viewmodel.HistoryViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.breakfast.R
import com.breakfast.designsystem.BreakfastButtonRes
import com.breakfast.designsystem.CollectorOrderDisplayItem
import com.breakfast.designsystem.CollectorOrderItemCard
import com.breakfast.designsystem.CollectorTableHeader
import com.breakfast.designsystem.TabChip
import com.breakfast.managers.PreferenceManager

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    orderId: Int,
    navController: NavController? = null
) {
    val apiService = ApiClient.apiService
    val viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory(apiService))
    val detailState by viewModel.detailState.collectAsState(null)
    val context = LocalContext.current
    val myId = remember { PreferenceManager(context).getUser()?.id }

    // Fetch order details when this screen is first displayed
    LaunchedEffect(orderId) {
        viewModel.fetchHistoryDetail(orderId)
    }

    var showTable by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.order_details)) },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
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
                .background(colorResource(id = com.breakfast.R.color.background_grey), RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            when (val state = detailState) {
                is Result.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is Result.Error -> {
                    Text(text = state.message ?: androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.failed_load_order_detail))
                }
                is Result.Success -> {
                    val detail = state.data.data
                    val items = detail?.orderItems ?: emptyList()

                    if (items.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    painter = painterResource(id = com.breakfast.R.drawable.no_orders),
                                    contentDescription = null,
                                    modifier = Modifier.size(220.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = stringResource(id = com.breakfast.R.string.no_items_in_order),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    } else {
                        // toggle row (Table / List)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 16.dp)
                                .clip(RoundedCornerShape(30.dp))
                                .background(Color(0xFF0066FF)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TabChip(
                                text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.table),
                                selected = showTable,
                                onClick = { showTable = true },
                                modifier = Modifier.weight(1f)
                            )
                            TabChip(
                                text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.list),
                                selected = !showTable,
                                onClick = { showTable = false },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (showTable) {
                            // header
                            CollectorTableHeader()
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(
                                        RoundedCornerShape(
                                            bottomStart = 20.dp,
                                            bottomEnd = 20.dp
                                        )
                                    )
                                    .background(colorResource(id = com.breakfast.R.color.background_grey), RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                            ) {
                                items.forEach { item ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 10.dp)
                                    ) {
                                        CollectorOrderItemCard(
                                            item = HistoryOrderItemDisplayAdapter(item),
                                            onShowUsers = { _: List<com.breakfast.models.Collector> -> },
                                            showInfoAction = false
                                        )
                                    }
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White)
                                    .padding(16.dp)
                            ) {
                                items.forEach { item ->
                                    Text(
                                        text = "${item.totalPrice ?: 0.0} EGP - ${item.itemName ?: ""} - ${item.quantity ?: 0}",
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Order info card
                        OrderInfoCard(
                            title = stringResource(id = com.breakfast.R.string.order_info),
                            quantity = (detail?.count ?: 0).toString(),
                            tax = 0.0,
                            delivery = 0.0,
                            total = detail?.totalPrice ?: 0.0,
                            onInfoClick = null
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (detail?.order?.collector?.id == myId) {
                        BreakfastButtonRes(
                            onClick = {
                                navController?.navigate("order_details/$orderId")
                            },
                            enabled = true,
                            isHasObserver = false,
                            iconRes = com.breakfast.R.drawable.list_bullet_clipboard_fill,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(text = stringResource(R.string.collector_view))
                        }
                    }
                }
                else -> {}
            }
        }
    }
}


@Preview(showBackground = true, name = "History Detail – Table")
@Composable
private fun HistoryDetailScreenPreviewTable() {
    val mockItems = listOf(
        OrderHistoryItemModel(
            itemName = "فول بلدي",
            quantity = 20,
            price = 7.0,
            totalPrice = 140.0,
            note = null,
            users = emptyList(),
            orderItemID = 1
        ),
        OrderHistoryItemModel(
            itemName = "شاي",
            quantity = 1,
            price = 5.0,
            totalPrice = 5.0,
            note = "null",
            users = emptyList(),
            orderItemID = 2
        )
    )
    HistoryDetailPreviewContent(items = mockItems)
}

@Preview(showBackground = true, name = "History Detail – List")
@Composable
private fun HistoryDetailScreenPreviewList() {
    val mockItems = listOf(
        OrderHistoryItemModel(
            itemName = "فول بلدي",
            quantity = 20,
            price = 7.0,
            totalPrice = 140.0,
            note = "null",
            users = emptyList(),
            orderItemID = 1
        )
    )
    HistoryDetailPreviewContent(items = mockItems, startWithTable = false)
}

@Composable
private fun HistoryDetailPreviewContent(
    items: List<OrderHistoryItemModel>,
    startWithTable: Boolean = true
) {
    var showTable by remember { mutableStateOf(startWithTable) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // main scrollable content at top
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color(0xFF0066FF)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabChip(
                    text = "Table",
                    selected = showTable,
                    onClick = { showTable = true },
                    modifier = Modifier.weight(1f)
                )
                TabChip(
                    text = "List",
                    selected = !showTable,
                    onClick = { showTable = false },
                    modifier = Modifier.weight(1f)
                )
            }

            if (showTable) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(Color(0xFF0066FF))
                ) {
                    CollectorTableHeader()
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                        .background(colorResource(id = com.breakfast.R.color.background_grey), RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                ) {
                    items.forEach { item ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 10.dp)
                        ) {
                            CollectorOrderItemCard(
                                item = HistoryOrderItemDisplayAdapter(item),
                                onShowUsers = { _: List<com.breakfast.models.Collector> -> },
                                showInfoAction = false
                            )
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    items.forEach { item ->
                        if (!item.note.isNullOrBlank()) {
                            Text(
                                text = "${item.quantity ?: 0} - ${item.itemName ?: ""} - ${item.note ?: ""} - ${item.totalPrice ?: 0.0} EGP",
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            Text(
                                text = "${item.quantity ?: 0} - ${item.itemName ?: ""} - ${item.totalPrice ?: 0.0} EGP",
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OrderInfoCard(
                title = stringResource(id = com.breakfast.R.string.order_info),
                quantity = items.sumOf { it.quantity ?: 0 }.toString(),
                tax = 0.0,
                delivery = 0.0,
                total = items.sumOf { it.totalPrice ?: 0.0 },
                onInfoClick = null
            )
            Spacer(modifier = Modifier.height(72.dp)) // give space for bottom button
        }

        // fixed bottom button
        BreakfastButtonRes(
            onClick = { /* TODO: Go To Collector View */ },
            enabled = true,
            isHasObserver = false,
            iconRes = com.breakfast.R.drawable.list_bullet_clipboard_fill,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(vertical = 8.dp)
        ) {
            Text(text = stringResource(R.string.collector_view))
        }
    }
}

private data class HistoryOrderItemDisplayAdapter(
    val src: OrderHistoryItemModel
) : CollectorOrderDisplayItem {
    override val displayName: String? get() = src.itemName
    override val displayQuantity: Int? get() = src.quantity
    override val displayPrice: Double? get() = src.price
    override val displayTotal: Double? get() = src.totalPrice
    override val displayNote: String? get() = src.note
    override val displayUsers: List<com.breakfast.models.Collector>? get() = src.users
}