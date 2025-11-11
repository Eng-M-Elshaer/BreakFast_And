
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import com.breakfast.designsystem.BreakfastScreen
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.breakfast.network.ApiClient
import com.breakfast.utils.Result
import com.breakfast.viewmodel.HomeViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.breakfast.R
import com.breakfast.designsystem.BreakfastButtonRes
import com.breakfast.designsystem.OrderCard
import androidx.compose.ui.platform.LocalContext
import com.breakfast.models.HomeModel
import androidx.compose.runtime.remember
import androidx.compose.ui.res.colorResource
import com.breakfast.managers.PreferenceManager
import com.breakfast.ui.components.SelectStoreDialog
import com.breakfast.models.StoreModel
import com.breakfast.ui.components.BreakfastEmptyState
import com.google.android.datatransport.BuildConfig

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController? = null) {

    // Obtain HomeViewModel via factory to inject ApiService
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(ApiClient.apiService, context))
    val homeState = viewModel.homeState.collectAsState()
    val storesState = viewModel.storeState.collectAsState()
    val infoState = viewModel.infoState.collectAsState()
    val startOrderState = viewModel.startOrderState.collectAsState()
    val showSelectStore = remember { mutableStateOf(false) }
    val pendingStoreDialog = remember { mutableStateOf(false) }
    val myId = remember { PreferenceManager(context).getUser()?.id }
    val showForceUpdate = remember { mutableStateOf(false) }
    val requiredVersion = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchHome()
    }

    LaunchedEffect(Unit) {
        viewModel.fetchInfo()
    }

    LaunchedEffect(storesState.value) {
        when (storesState.value) {
            is Result.Success -> {
                if (pendingStoreDialog.value) {
                    showSelectStore.value = true
                }
                pendingStoreDialog.value = false
            }
            is Result.Error -> {
                pendingStoreDialog.value = false
            }
            else -> {}
        }
    }

    LaunchedEffect(startOrderState.value) {
        when (startOrderState.value) {
            is Result.Success -> {
               viewModel.fetchHome()
                pendingStoreDialog.value = false
            }
            is Result.Error -> {
                pendingStoreDialog.value = false
            }
            else -> {}
        }
    }

    LaunchedEffect(infoState.value) {
        val state = infoState.value
        if (state is com.breakfast.utils.Result.Success<*>) {
            val data = state.data
            // assume API response wrapper: ApiResponse<VersionCheckResponse>
            val versionInfo = when (data) {
                is com.breakfast.models.ApiResponse<*> -> data.data as? com.breakfast.models.VersionCheckResponse
                else -> null
            }
            if (versionInfo != null) {
                val currentVersion = BuildConfig.VERSION_NAME
                // if server says forceUpdate or current < minimumRequiredVersion -> show dialog
                if (versionInfo.forceUpdate || currentVersion < versionInfo.minimumRequiredVersion) {
                    requiredVersion.value = versionInfo.latestVersion
                    showForceUpdate.value = true
                }
            }
        }
    }

    BreakfastScreen(
        title = stringResource(id = R.string.home_screen_title)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when (val state = homeState.value) {
                is Result.Loading -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is Result.Error -> {
                    BreakfastEmptyState(
                        iconRes = R.drawable.no_internet,
                        title = state.message ?: stringResource(id = R.string.failed_load_home),
                        showButton = true,
                        buttonText = stringResource(id = R.string.retry),
                        onButtonClick = { viewModel.fetchHome() },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                }
                is Result.Success -> {
                    val itemsList: List<HomeModel> = state.data.data ?: emptyList()
                    val hasMyCollect = itemsList.any { item ->
                        val collectorId = item.collector?.id
                        collectorId != null && myId != null && collectorId == myId
                    }
                    val myCollectOrder = itemsList.firstOrNull { item ->
                        val collectorId = item.collector?.id
                        collectorId != null && myId != null && collectorId == myId
                    }

                    if (itemsList.isEmpty()) {
                        BreakfastEmptyState(
                            iconRes = R.drawable.no_orders,
                            title = stringResource(id = R.string.no_orders_available),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(itemsList) { homeItem ->
                                val title = homeItem.collector?.name
                                    ?: (stringResource(id = R.string.order) + " #${homeItem.id ?: ""}")
                                val subtitle = if (homeItem.store?.name != null) homeItem.store.name
                                else (stringResource(id = R.string.status) + ": ${homeItem.status?.name ?: ""}")

                                OrderCard(
                                    title = title,
                                    subtitle = subtitle,
                                    onClick = {
                                        navController?.let { controller ->
                                            val orderId = homeItem.id ?: 0
                                            val storeId = homeItem.store?.id ?: 0
                                            controller.navigate("add_to_order/${orderId}/${storeId}")
                                        }
                                    },
                                    avatarUrl = homeItem.store?.image,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }
                    if (hasMyCollect) {
                        BreakfastButtonRes(
                            onClick = {
                                myCollectOrder?.let { order ->
                                    navController?.navigate("collector_details")
                                }
                            },
                            enabled = true,
                            isHasObserver = false,
                            iconRes = R.drawable.list_bullet_clipboard_fill,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(text = stringResource(R.string.start_collecting))
                        }
                    } else {
                        BreakfastButtonRes(
                            onClick = {
                                viewModel.fetchStores()
                                pendingStoreDialog.value = true
                            },
                            enabled = true,
                            isHasObserver = false,
                            iconRes = R.drawable.list_bullet_clipboard_fill,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(text = stringResource(R.string.be_a_collector))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
        // keep dialogs below
        if (pendingStoreDialog.value && storesState.value is Result.Loading) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { /* block dismiss while loading */ }) {
                androidx.compose.material3.Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = colorResource(id = R.color.card_bg))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator()
                    }
                }
            }
        }
        val stores: List<StoreModel> = when (val s = storesState.value) {
            is Result.Success<*> -> {
                val data = s.data
                when {
                    data is com.breakfast.models.ApiResponse<*> -> {
                        (data.data as? List<*>)?.filterIsInstance<StoreModel>() ?: emptyList()
                    }
                    else -> emptyList()
                }
            }
            else -> emptyList()
        }
        SelectStoreDialog(
            visible = showSelectStore.value && stores.isNotEmpty(),
            stores = stores,
            onDismiss = { showSelectStore.value = false },
            onConfirm = { selectedStore ->
                showSelectStore.value = false
                viewModel.startOrder(selectedStore.id ?: 0)
            }
        )
        if (showForceUpdate.value) {
            AlertDialog(
                onDismissRequest = { /* blocked */ },
                confirmButton = {
                    Button(onClick = {
                        val appPackageName = context.packageName
                        val marketIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=$appPackageName"))
                        marketIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        try {
                            context.startActivity(marketIntent)
                        } catch (e: android.content.ActivityNotFoundException) {
                            val webIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName"))
                            webIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(webIntent)
                        }
                    }) {
                        Text(text = stringResource(id = R.string.update_now))
                    }
                },
                title = {
                    Text(text = stringResource(id = R.string.update_required))
                },
                text = {
                    Text(text = stringResource(id = R.string.update_required_message))
                }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "HomeScreen – Empty State")
@Composable
private fun HomeScreenEmptyPreview() {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .fillMaxSize()
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.home_screen_title),
                fontWeight = FontWeight.SemiBold,
                style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp)
            )

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
                        text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.no_orders_available),
                        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            BreakfastButtonRes(
                onClick = {},
                enabled = true,
                isHasObserver = false,
                iconRes = com.breakfast.R.drawable.list_bullet_clipboard_fill,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) { Text(text = stringResource(R.string.be_a_collector)) }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "HomeScreen – With Data")
@Composable
private fun HomeScreenWithDataPreview() {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .fillMaxSize()
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.home_screen_title),
                fontWeight = FontWeight.SemiBold,
                style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp)
            )

            // Sample item list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(listOf(1, 2)) { _ ->
                    OrderCard(
                        title = "iOS Tester",
                        subtitle = "عم حسن",
                        onClick = {},
                        avatarUrl = null,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }

            BreakfastButtonRes(
                onClick = {},
                enabled = true,
                isHasObserver = false,
                iconRes = com.breakfast.R.drawable.list_bullet_clipboard_fill,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) { Text(text = stringResource(R.string.start_collecting)) }
            Spacer(Modifier.height(8.dp))
        }
    }
}