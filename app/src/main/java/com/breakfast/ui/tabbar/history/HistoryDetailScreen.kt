
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import coil.compose.AsyncImage
import com.breakfast.utils.Validator
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.breakfast.R
import com.breakfast.designsystem.BreakfastButtonRes
import com.breakfast.designsystem.CollectorOrderDisplayItem
import com.breakfast.designsystem.CollectorOrderItemCard
import com.breakfast.designsystem.CollectorTableHeader
import com.breakfast.designsystem.OrderInfoCard
import com.breakfast.designsystem.TabChip
import com.breakfast.managers.PreferenceManager
import com.breakfast.ui.components.BreakfastEmptyState
import com.breakfast.designsystem.BreakfastScreen

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    orderId: Int,
    navController: NavController? = null
) {
    val apiService = ApiClient.apiService
    val context = LocalContext.current
    val viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory(apiService, context))
    val detailState by viewModel.detailState.collectAsState(null)
    val myId = remember { PreferenceManager(context).getUser()?.id }

    // Fetch order details when this screen is first displayed
    LaunchedEffect(orderId) {
        viewModel.fetchHistoryDetail(orderId)
    }

    var showTable by remember { mutableStateOf(true) }
    BreakfastScreen(
        title = stringResource(id = R.string.order_details),
        onLeftAction = { navController?.popBackStack() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(vertical = 8.dp)
        ) {
            when (val state = detailState) {
                is Result.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is Result.Error -> {
                    BreakfastEmptyState(
                        iconRes = if (state.message == stringResource(com.breakfast.R.string.no_internet)) {
                            com.breakfast.R.drawable.no_internet
                        } else {
                            com.breakfast.R.drawable.ic_error_round
                        },
                        title = state.message ?: stringResource(id = R.string.failed_load_order_detail),
                        showButton = true,
                        buttonText = stringResource(id = R.string.retry),
                        onButtonClick = { viewModel.fetchHistoryDetail(orderId) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                }
                is Result.Success -> {
                    val detail = state.data.data
                    val items = detail?.orderItems ?: emptyList()

                    if (items.isEmpty()) {
                        BreakfastEmptyState(
                            iconRes = R.drawable.no_orders,
                            title = stringResource(id = R.string.no_items_in_order),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .background(
                                    color = colorResource(R.color.blue_ribbon),
                                    shape = RoundedCornerShape(999.dp)
                                )
                                .padding(4.dp)
                        ) {
                            TabChip(
                                text = stringResource(id = R.string.table),
                                selected = showTable,
                                modifier = Modifier.weight(1f),
                                onClick = { showTable = true }
                            )
                            TabChip(
                                text = stringResource(id = R.string.list),
                                selected = !showTable,
                                modifier = Modifier.weight(1f),
                                onClick =  { showTable = false }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (showTable) {
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
                                    .background(
                                        colorResource(id = R.color.background_grey),
                                        RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                                    )
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
                                    .background(colorResource(id = R.color.card_bg))
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

                        if (detail?.order?.collector?.id != myId) {
                            val collectorName = detail?.order?.collector?.name
                            val collectorImage = detail?.order?.collector?.image
                            val collectorPhone = detail?.order?.collector?.phone
                            val collectorInstaPay = detail?.order?.collector?.instaPay

                            CollectorInfoCard(
                                name = collectorName,
                                phone = collectorPhone,
                                instaPay = collectorInstaPay,
                                imageUrl = collectorImage,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            )
                        }
                        OrderInfoCard(
                            title = stringResource(id = R.string.order_info),
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
                            iconRes = R.drawable.list_bullet_clipboard_fill,
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

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun CollectorInfoCard(
    name: String?,
    phone: String?,
    instaPay: String?,
    imageUrl: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.card_bg))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                placeholder = painterResource(id = R.drawable.image_placeholder),
                error = painterResource(id = R.drawable.image_placeholder),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (!name.isNullOrBlank()) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        color = colorResource(id = R.color.woodsmoke)
                    )
                }
                if (!phone.isNullOrBlank()) {
                    Text(
                        text = phone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorResource(id = R.color.blue_ribbon),
                        modifier = Modifier.combinedClickable(
                            onClick = {
                                val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                dial.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                runCatching { context.startActivity(dial) }
                            },
                            onLongClick = {
                                clipboard.setText(AnnotatedString(phone))
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.copied),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    )
                }
                if (!instaPay.isNullOrBlank()) {
                    val valid = runCatching { Validator.isValidInstaPayLink(instaPay) }.getOrDefault(false)
                    if (valid) {
                        Text(
                            text = stringResource(R.string.pay_via_instapay),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorResource(id = R.color.blue_ribbon),
                            modifier = Modifier.clickable {
                                val view = Intent(Intent.ACTION_VIEW, Uri.parse(instaPay))
                                view.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                runCatching { context.startActivity(view) }
                            }
                        )
                    }
                }
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
            // Segmented
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(
                        color = colorResource(R.color.blue_ribbon),
                        shape = RoundedCornerShape(999.dp)
                    )
                    .padding(4.dp)
            ) {
                TabChip(
                    text = stringResource(id = R.string.table),
                    selected = showTable,
                    modifier = Modifier.weight(1f),
                    onClick = { showTable = true }
                )
                TabChip(
                    text = stringResource(id = R.string.list),
                    selected = !showTable,
                    modifier = Modifier.weight(1f),
                    onClick =  { showTable = false }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
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
                        .background(
                            colorResource(id = com.breakfast.R.color.background_grey),
                            RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        )
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
                        .background(colorResource(id = R.color.card_bg))
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