package com.breakfast.ui.tabbar.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.breakfast.models.HistoryModel
import com.breakfast.network.ApiClient
import com.breakfast.utils.Result
import com.breakfast.viewmodel.HistoryViewModel
import androidx.navigation.NavController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.breakfast.designsystem.OrderCard
import com.breakfast.ui.components.BreakfastEmptyState
import com.breakfast.designsystem.BreakfastScreen
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController? = null) {

    val apiService = ApiClient.apiService
    val context = LocalContext.current
    val viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory(apiService, context))
    val historyState = viewModel.historyState.collectAsState(null)

    LaunchedEffect(Unit) {
        viewModel.fetchHistory()
    }

    val isRefreshing = historyState.value is Result.Loading
    val swipeState = rememberSwipeRefreshState(isRefreshing = isRefreshing)

    BreakfastScreen(
        title = stringResource(id = com.breakfast.R.string.history)
    ) { innerPadding ->
        SwipeRefresh(
            state = swipeState,
            onRefresh = { viewModel.fetchHistory() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                when (val state = historyState.value) {
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
                            iconRes = if (state.message == stringResource(com.breakfast.R.string.no_internet)) {
                                com.breakfast.R.drawable.no_internet
                            } else {
                                com.breakfast.R.drawable.ic_error_round
                            },
                            title = state.message ?: stringResource(id = com.breakfast.R.string.failed_load_history),
                            showButton = true,
                            buttonText = stringResource(id = com.breakfast.R.string.retry),
                            onButtonClick = { viewModel.fetchHistory() },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                    is Result.Success -> {
                        val itemsList = state.data.data ?: emptyList()
                        if (itemsList.isEmpty()) {
                            BreakfastEmptyState(
                                iconRes = com.breakfast.R.drawable.no_history,
                                title = stringResource(id = com.breakfast.R.string.no_history),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            )
                        } else {
                            LazyColumn(
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                    top = 0.dp,
                                    bottom = innerPadding.calculateBottomPadding()
                                )
                            ) {
                                items(itemsList) { item ->
                                    val title = item.store?.name ?: (stringResource(id = com.breakfast.R.string.order) + " #${item.id ?: ""}")
                                    val collector = item.collector?.name ?: ""
                                    val datePart = item.date?.takeIf { it.isNotBlank() } ?: ""
                                    val avatar = item.store?.image?.let { if (it.startsWith("http://")) it.replace("http://", "https://") else it }

                                    OrderCard(
                                        title = title,
                                        subtitle = collector,
                                        date = datePart,
                                        avatarUrl = avatar,
                                        onClick = {
                                            item.id?.let { id ->
                                                navController?.navigate("history_detail/${id}")
                                            }
                                        },
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "History – Empty")
@Composable
private fun HistoryEmptyPreview() {
    androidx.compose.material3.Scaffold(
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            BreakfastEmptyState(
                iconRes = com.breakfast.R.drawable.no_history,
                title = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.no_history),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "History – With Data")
@Composable
private fun HistoryWithDataPreview() {
    val sample = listOf(
        HistoryModel(
            id = 101,
            status = null,
            store = com.breakfast.models.Collector(name = "عم حسن", id = 1, image = null, phone = "01000000000", instaPay = null),
            collector = com.breakfast.models.Collector(name = "iOS Tester", id = 64, image = null, phone = "01000000000", instaPay = null),
            date = "2025-11-01 14:00",
            totalPrice = 75.0
        ),
        HistoryModel(
            id = 102,
            status = null,
            store = com.breakfast.models.Collector(name = "عم وحيد", id = 2, image = null, phone = "01000000000", instaPay = null),
            collector = com.breakfast.models.Collector(name = "iOS Tester", id = 64, image = null, phone = "01000000000", instaPay = null),
            date = "2025-11-01 16:30",
            totalPrice = 49.99
        )
    )
    androidx.compose.material3.Scaffold(
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            LazyColumn {
                items(sample) { item ->
                    val title = item.store?.name ?: (androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.order) + " #${item.id ?: ""}")
                    val datePart = item.date?.takeIf { it.isNotBlank() } ?: ""
                    val pricePart = item.totalPrice?.let { String.format("%.2f $", it) } ?: ""
                    val subtitle = listOf(datePart, pricePart).filter { it.isNotEmpty() }.joinToString(" • ")

                    OrderCard(
                        title = title,
                        subtitle = subtitle,
                        avatarUrl = null,
                        onClick = {},
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
        }
    }
}