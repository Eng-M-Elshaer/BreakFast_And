package com.breakfast.ui.order

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.FileOutputStream
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import coil.compose.AsyncImage
import com.breakfast.R
import com.breakfast.designsystem.BreakfastButtonRes
import com.breakfast.designsystem.CollectorOrderDisplayItem
import com.breakfast.designsystem.CollectorOrderItemCard
import com.breakfast.designsystem.CollectorTableHeader
import com.breakfast.models.ApiResponse
import com.breakfast.models.CollectorHistoryModel
import com.breakfast.models.OrderInfoModel
import com.breakfast.models.OrderItem
import com.breakfast.models.UsersItem
import com.breakfast.network.ApiClient
import com.breakfast.utils.Result
import com.breakfast.viewmodel.OrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    orderId: Int = 1,
    navController: NavController? = null,
    previewData: CollectorHistoryModel? = null,
    onReopen: () -> Unit = {}
) {
    val viewModel: OrderViewModel? = if (previewData == null) {
        viewModel(factory = OrderViewModel.Factory(ApiClient.apiService))
    } else {
        null
    }

    val collectorState: State<Result<*>?> = if (previewData == null) {
        viewModel!!.collectorHistoryState.collectAsState()
    } else {
        remember { mutableStateOf<Result<*>?>(Result.Success(previewData)) }
    }

    val reOpenState: State<Result<*>?> = if (previewData == null) {
        viewModel!!.reOpenState.collectAsState()
    } else {
        remember { mutableStateOf<Result<*>?>(null) }
    }

    // receipt state from VM
    val receiptState: State<Result<*>?> = if (previewData == null) {
        viewModel!!.receiptState.collectAsState()
    } else {
        remember { mutableStateOf<Result<*>?>(null) }
    }

    var selectedTab by remember { mutableStateOf(0) }
    var showReceipt by remember { mutableStateOf(false) }

    LaunchedEffect(orderId, previewData) {
        if (previewData == null) {
            viewModel?.fetchCollectorHistoryItems(orderId)
        }
    }

    LaunchedEffect(reOpenState.value) {
        if (reOpenState.value is Result.Success<*>) {
            onReopen()
            navController?.navigate(BottomNavItem.Home.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.order_details)) },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                // replaced actions block
                // see instructions for details
                actions = {
                    val context = LocalContext.current
                    IconButton(
                        onClick = {
                            // we only have real data when previewData == null and state is success
                            val currentState = collectorState.value
                            if (currentState is Result.Success<*>) {
                                val model: CollectorHistoryModel = if (previewData != null) {
                                    currentState.data as CollectorHistoryModel
                                } else {
                                    val apiResp = currentState.data as com.breakfast.models.ApiResponse<com.breakfast.models.CollectorHistoryModel>
                                    apiResp.data ?: return@IconButton
                                }
                                createAndShareOrderPdf(context, model)
                            }
                        }
                    ) {
                        Icon(
                            Icons.Filled.IosShare,
                            contentDescription = stringResource(id = R.string.share)
                        )
                    }
                }
            )
        },
        bottomBar = {
            BreakfastButtonRes(
                iconRes = R.drawable.lock_open_fill,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                onClick = {
                    if (previewData == null) {
                        viewModel?.reOpenCollecting(orderId)
                    } else {
                        onReopen()
                    }
                }
            ) {
                Text(text = stringResource(id = R.string.re_open_order))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            // segmented
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(
                        color = Color(0xFFE5F0FF),
                        shape = RoundedCornerShape(999.dp)
                    )
                    .padding(4.dp)
            ) {
                TabChip(
                    text = stringResource(id = R.string.table),
                    selected = selectedTab == 0,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = 0 }
                )
                TabChip(
                    text = stringResource(id = R.string.list),
                    selected = selectedTab == 1,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = 1 }
                )
            }

            Spacer(Modifier.height(16.dp))

            when (val state = collectorState.value) {
                is Result.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is Result.Error -> {
                    Text(
                        text = state.message ?: stringResource(id = R.string.failed_load_order_items),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                is Result.Success<*> -> {
                    val data: CollectorHistoryModel = if (previewData != null) {
                        state.data as CollectorHistoryModel
                    } else {
                        val apiResponse = state.data as ApiResponse<CollectorHistoryModel>
                        apiResponse.data ?: return@Column
                    }
                    val usersItems = data.usersItems.orEmpty()

                    if (selectedTab == 0) {
                        CollectorTableSection(items = usersItems)
                    } else {
                        ListViewSection(items = usersItems)
                    }

                    Spacer(Modifier.height(16.dp))

                    OrderInfoCard(
                        quantity = data.order?.totalPrice?.toInt()?.toString() ?: "0",
                        tax = data.order?.tax ?: 0.0,
                        delivery = data.order?.delivery ?: 0.0,
                        total = data.order?.totalPrice ?: 0.0,
                        onInfoClick = {
                            if (previewData == null) {
                                viewModel?.receipt(orderId)
                            }
                            showReceipt = true
                        }
                    )

                    Spacer(Modifier.height(90.dp))
                }

                null -> Unit
            }
        }
    }

    // dialog for receipt (now as a bottom sheet)
    if (showReceipt) {
        val rs = receiptState.value
        ModalBottomSheet(
            onDismissRequest = { showReceipt = false },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            when (rs) {
                is Result.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is Result.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.order_info),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(text = rs.message ?: stringResource(id = R.string.failed_load_order_items))
                        Spacer(Modifier.height(16.dp))
                        BreakfastButtonRes(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { showReceipt = false }
                        ) {
                            Text(text = stringResource(id = R.string.ok))
                        }
                    }
                }
                is Result.Success<*> -> {
                    // assume API returns CollectorHistoryModel-like receipt
                    val receipt = rs.data
                    val items = (receipt as? CollectorHistoryModel)?.usersItems.orEmpty()
                    val orderInfo = (receipt as? CollectorHistoryModel)?.order
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .align(Alignment.CenterHorizontally)
                                .background(Color.LightGray, RoundedCornerShape(999.dp))
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = stringResource(id = R.string.receipt),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(12.dp))
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 420.dp)
                        ) {
                            items(items) { user ->
                                Text(
                                    text = "#" + (user.userID ?: 0) + " — " + (user.userName ?: "-"),
                                    fontWeight = FontWeight.SemiBold
                                )
                                user.orderItems.orEmpty().forEach { item ->
                                    Text(
                                        text = "- ${item.itemName.orEmpty()} x${item.quantity ?: 0} = ${item.total ?: 0.0}",
                                        modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        // summary like iOS: quantity, tax, delivery, total
                        Divider()
                        Spacer(Modifier.height(8.dp))
                        ReceiptSummaryRow(
                            label = stringResource(id = R.string.quantity),
                            value = (orderInfo?.totalPrice ?: 0.0).toInt().toString()
                        )
                        ReceiptSummaryRow(
                            label = stringResource(id = R.string.tax),
                            value = stringResource(id = R.string.price_with_currency, orderInfo?.tax ?: 0.0)
                        )
                        ReceiptSummaryRow(
                            label = stringResource(id = R.string.delivery),
                            value = stringResource(id = R.string.price_with_currency, orderInfo?.delivery ?: 0.0)
                        )
                        ReceiptSummaryRow(
                            label = stringResource(id = R.string.total_price),
                            value = stringResource(id = R.string.price_with_currency, orderInfo?.totalPrice ?: 0.0),
                            isBold = true
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }
                null -> {
                    showReceipt = false
                }
            }
        }
    }
}

@Composable
private fun TabChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) Color(0xFF0F6BFF) else Color.Transparent)
            .fillMaxHeight()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color.Black,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private data class OrderItemDisplayAdapter(private val src: OrderItem) : CollectorOrderDisplayItem {
    override val displayName: String? get() = src.itemName
    override val displayQuantity: Int? get() = src.quantity
    override val displayPrice: Double? get() = src.price
    override val displayTotal: Double? get() = src.total
    override val displayNote: String? get() = src.note
    override val displayUsers: List<com.breakfast.models.Collector>? get() = src.users
}

@Composable
private fun CollectorTableSection(items: List<UsersItem>) {

    //Header
    CollectorTableHeader(showAction = false)

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(id = com.breakfast.R.color.background_grey), RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .padding(bottom = 16.dp)
    ) {
        items(items) { user ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val avatarUrl = user.userImage
                if (!avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape),
                        placeholder = painterResource(id = com.breakfast.R.drawable.image_placeholder),
                        error = painterResource(id = com.breakfast.R.drawable.image_placeholder)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = com.breakfast.R.drawable.image_placeholder),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = user.userName ?: "",
                    fontWeight = FontWeight.Bold
                )
            }
            user.orderItems?.forEachIndexed { index, order ->
                val isLast = index == user.orderItems.lastIndex
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
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    CollectorOrderItemCard(
                        item = OrderItemDisplayAdapter(order),
                        onShowUsers = {},
                        showInfoAction = !order.users.isNullOrEmpty()
                    )
                }
            }
        }
    }
}

@Composable
private fun ListViewSection(items: List<UsersItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        items.forEach { user ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                val avatarUrl = user.userImage
                if (!avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape),
                        placeholder = painterResource(id = com.breakfast.R.drawable.image_placeholder),
                        error = painterResource(id = com.breakfast.R.drawable.image_placeholder)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = com.breakfast.R.drawable.image_placeholder),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                    )
                }
                Spacer(Modifier.width(8.dp))

                Text(
                    text = user.userName ?: "",
                    fontWeight = FontWeight.Bold
                )
            }
            user.orderItems?.forEach { item ->
                if (!item.note.isNullOrBlank()) {
                    Text(
                        text = "${item.quantity ?: 0} - ${item.itemName ?: ""} - ${item.note ?: ""} - ${item.total ?: 0.0} EGP",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Text(
                        text = "${item.quantity ?: 0} - ${item.itemName ?: ""} - ${item.total ?: 0.0} EGP",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun OrderInfoCard(
    title: String = stringResource(id = R.string.order_info),
    quantity: String,
    tax: Double,
    delivery: Double,
    total: Double,
    onInfoClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE5F0FF), RoundedCornerShape(24.dp))
            .background(Color.White, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            if (onInfoClick != null) {
                IconButton(onClick = onInfoClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_info),
                        contentDescription = stringResource(id = R.string.info),
                        tint = Color(0xFF0F6BFF),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(text = stringResource(id = R.string.quantity_with_value, quantity))

        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = stringResource(id = R.string.tax))
            Text(text = stringResource(id = R.string.price_with_currency, tax))
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = stringResource(id = R.string.delivery))
            Text(text = stringResource(id = R.string.price_with_currency, delivery))
        }
        Spacer(Modifier.height(8.dp))
        Divider()
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = stringResource(id = R.string.total))
            Text(
                text = stringResource(id = R.string.price_with_currency, total),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OrderDetailsScreenPreview() {
    val fake = CollectorHistoryModel(
        usersItems = listOf(
            UsersItem(
                userID = 1,
                userName = "User One",
                orderItems = listOf(
                    OrderItem(
                        id = 1,
                        orderID = 10,
                        itemName = "فول بلدي",
                        quantity = 1,
                        price = 7.0,
                        total = 7.0,
                        note = null,
                        tax = null,
                        users = null
                    ),
                    OrderItem(
                        id = 2,
                        orderID = 10,
                        itemName = "فول اسكندراني بلدي",
                        quantity = 2,
                        price = 8.0,
                        total = 16.0,
                        note = "sssss",
                        tax = null,
                        users = null
                    )
                ),
                totalQuantity = 3,
                taxDelivery = 0.0,
                totalPrice = 23.0,
                userImage = null
            )
        ),
        order = OrderInfoModel(
            id = 10,
            status = null,
            createdAt = null,
            totalPrice = 49.0,
            store = null,
            tax = 0.0,
            delivery = 10.0,
            collector = null
        ),
        count = 1
    )
    OrderDetailsScreen(
        previewData = fake
    )
}

@Preview(showBackground = true)
@Composable
private fun OrderDetailsScreenListPreview() {
    val fake = listOf(
        UsersItem(
            userID = 1,
            userName = "User One",
            orderItems = listOf(
                OrderItem(
                    id = 1,
                    orderID = 10,
                    itemName = "فول بلدي",
                    quantity = 1,
                    price = 7.0,
                    total = 7.0,
                    note = null,
                    tax = null,
                    users = null
                ),
                OrderItem(
                    id = 2,
                    orderID = 10,
                    itemName = "فول اسكندراني بلدي",
                    quantity = 2,
                    price = 8.0,
                    total = 16.0,
                    note = "extra",
                    tax = null,
                    users = null
                )
            ),
            totalQuantity = 3,
            taxDelivery = 0.0,
            totalPrice = 23.0,
            userImage = null
        )
    )
    ListViewSection(items = fake)
}
// Helper function to create and share order PDF
private fun createAndShareOrderPdf(context: Context, data: CollectorHistoryModel) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint().apply { textSize = 12f }
    var y = 40f

    canvas.drawText("Order report", 40f, y, paint)
    y += 20f

    data.usersItems.orEmpty().forEachIndexed { index, user ->
        canvas.drawText("#${index + 1} ${user.userName.orEmpty()}", 40f, y, paint)
        y += 18f
        user.orderItems.orEmpty().forEach { item ->
            val line = "- ${item.itemName.orEmpty()} x${item.quantity ?: 0} = ${item.total ?: 0.0}"
            canvas.drawText(line, 60f, y, paint)
            y += 16f
        }
        y += 8f
    }

    pdfDocument.finishPage(page)

    val file = File(context.cacheDir, "order_report.pdf")
    pdfDocument.writeTo(FileOutputStream(file))
    pdfDocument.close()

    val uri = FileProvider.getUriForFile(
        context,
        context.packageName + ".fileprovider",
        file
    )
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share order PDF"))
}
@Composable
private fun ReceiptSummaryRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray)
        Text(
            text = value,
            fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal
        )
    }
    Spacer(Modifier.height(6.dp))
}