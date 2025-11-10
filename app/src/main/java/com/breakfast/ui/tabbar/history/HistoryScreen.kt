package com.breakfast.ui.tabbar.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.breakfast.designsystem.OrderCard
import com.breakfast.ui.components.BreakfastEmptyState

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

    Scaffold() { paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp)
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.history),
                fontWeight = FontWeight.SemiBold,
                style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp)
            )
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
                        iconRes = com.breakfast.R.drawable.no_internet,
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
                            title = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.no_history),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                    } else {
                        LazyColumn {
                            items(itemsList) { item ->

                                val title = item.store?.name ?: (androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.order) + " #${item.id ?: ""}")
                                val collector = item.collector?.name ?: ""
                                val datePart = item.date?.takeIf { it.isNotBlank() } ?: ""
                                //val pricePart = item.totalPrice?.let { String.format("%.2f $", it) } ?: ""
                                //val subtitle = listOf(datePart, pricePart).filter { it.isNotEmpty() }.joinToString(" • ")
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
            store = com.breakfast.models.Collector(name = "عم حسن", id = 1, image = null),
            collector = com.breakfast.models.Collector(name = "iOS Tester", id = 64, image = null),
            date = "2025-11-01 14:00",
            totalPrice = 75.0
        ),
        HistoryModel(
            id = 102,
            status = null,
            store = com.breakfast.models.Collector(name = "عم وحيد", id = 2, image = null),
            collector = com.breakfast.models.Collector(name = "iOS Tester", id = 64, image = null),
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