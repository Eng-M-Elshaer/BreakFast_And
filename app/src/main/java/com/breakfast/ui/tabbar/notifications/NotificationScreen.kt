package com.breakfast.ui.tabbar.notifications

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.breakfast.network.ApiClient
import com.breakfast.models.NotificationModel
import com.breakfast.utils.Result
import com.breakfast.viewmodel.NotificationViewModel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.breakfast.ui.components.ErrorDialog
import com.breakfast.designsystem.OrderCard
import androidx.navigation.NavController
import com.breakfast.R
import com.breakfast.models.NotificationSubjectType

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController) {

    val apiService = ApiClient.apiService
    val viewModel: NotificationViewModel = viewModel(factory = NotificationViewModel.Factory(apiService))
    val notificationsState = viewModel.notificationsState.collectAsState(null)
    val markState = viewModel.markState.collectAsState(null)
    val defaultLoadFailed = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.failed_load_notifications)
    val defaultMarkFailed = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.failed_mark_read)
    val showError = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val errorMsg = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

    LaunchedEffect(notificationsState.value) {
        val s = notificationsState.value
        if (s is Result.Error) {
            errorMsg.value = s.message ?: defaultLoadFailed
            showError.value = true
        }
    }
    LaunchedEffect(markState.value) {
        val s = markState.value
        when (s) {
            is Result.Error -> {
                errorMsg.value = s.message ?: defaultMarkFailed
                showError.value = true
            }
            is Result.Success -> {
                // re-fetch to update UI after mark-all
                viewModel.fetchNotifications()
            }
            else -> Unit
        }
    }

    // Load notifications on first composition
    LaunchedEffect(Unit) {
        viewModel.fetchNotifications()
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.notifications),
                    fontWeight = FontWeight.SemiBold,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.weight(1f)
                )
                androidx.compose.material3.TextButton(onClick = { viewModel.markAllNotificationsAsRead() }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Filled.Checklist,
                        tint = colorResource(com.breakfast.R.color.blue_ribbon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(start = 2.dp)
                    )
                    Text(
                        text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.mark_all_read),
                        color = colorResource(com.breakfast.R.color.blue_ribbon),
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
            when (val state = notificationsState.value) {
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
                is Result.Error -> { /* Error shown via dialog */ }
                is Result.Success -> {
                    val itemsList = state.data.data ?: emptyList()

                    if (itemsList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    painter = painterResource(id = com.breakfast.R.drawable.no_notification),
                                    contentDescription = null,
                                    modifier = Modifier.size(220.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.no_notifications),
                                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    } else {
                        LazyColumn {
                            items(itemsList) { notification ->
                                val isUnread = notification.isRead == true
                                val title = notification.title ?: androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.notification)
                                val subtitle = notification.message ?: ""
                                val date = notification.createdAt ?: ""
                                OrderCard(
                                    title = title,
                                    subtitle = subtitle,
                                    date = date,
                                    avatarUrl = null,
                                    icon = if (isUnread) null else androidx.compose.material.icons.Icons.Filled.Circle,
                                    avatarRes = com.breakfast.R.drawable.circle_logo,
                                    onClick = {
                                        val id = notification.id
                                        val subjectId = notification.subjectID
                                        val storeId = notification.storeID
                                        val type = notification.subjectType
                                        if (isUnread) {
                                            id?.let { viewModel.markNotificationAsRead(it) }
                                        }
                                        when (type) {
                                            NotificationSubjectType.OPEN -> {
                                                if (subjectId != null && storeId != null) {
                                                    navController.navigate("add_to_order/$subjectId/$storeId")
                                                }
                                            }
                                            NotificationSubjectType.CLOSE -> {
                                                if (subjectId != null) {
                                                    navController.navigate("history_detail/$subjectId")
                                                }
                                            }
                                            NotificationSubjectType.STOP -> {
                                                if (subjectId != null) {
                                                    navController.navigate("history_detail/$subjectId")
                                                }
                                            }
                                            else -> {}
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
        ErrorDialog(
            visible = showError.value,
            title = stringResource(id = R.string.something_went_wrong),
            message = errorMsg.value,
            buttonText = stringResource(id = R.string.submit),
            onDismiss = {
                showError.value = false
                viewModel.clearNotificationsState()
                viewModel.clearMarkState()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true, name = "Notifications – Empty")
@Composable
private fun NotificationsEmptyPreview() {
    Scaffold(
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.notifications),
                    fontWeight = FontWeight.SemiBold,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.weight(1f)
                )
                androidx.compose.material3.TextButton(onClick = { /* preview only */ }) {
                    Icon(imageVector = androidx.compose.material.icons.Icons.Filled.Check,
                        tint = colorResource(com.breakfast.R.color.blue_ribbon),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp))
                    Text(text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.mark_all_read),
                        color = colorResource(com.breakfast.R.color.blue_ribbon),
                        modifier = Modifier.padding(start = 6.dp))
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = com.breakfast.R.drawable.no_notification),
                        contentDescription = null,
                        modifier = Modifier.size(220.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.no_notifications),
                        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
    // NotificationScreen(navController)  // previews don't navigate
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true, name = "Notifications – With Data")
@Composable
private fun NotificationsWithDataPreview() {
    val sample = listOf(
        NotificationModel(
            id = 1,
            title = "New order assigned",
            message = "Order #345 to عم حسن",
            isRead = false,
            subjectID = null,
            createdAt = "2025-11-02 10:15",
            subjectType = null,
            storeID = null
        ),
        NotificationModel(
            id = 2,
            title = "Order delivered",
            message = "Order #122 delivered",
            isRead = true,
            subjectID = null,
            createdAt = "2025-11-02 10:25",
            subjectType = null,
            storeID = null
        )
    )

    Scaffold(
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.notifications),
                    fontWeight = FontWeight.SemiBold,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.weight(1f)
                )
                androidx.compose.material3.TextButton(onClick = { /* preview only */ }) {
                    Icon(imageVector = androidx.compose.material.icons.Icons.Filled.Checklist,
                        contentDescription = null,
                        tint = colorResource(com.breakfast.R.color.blue_ribbon),
                        modifier = Modifier
                            .size(18.dp)
                            .padding(start = 2.dp))
                    Text(text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.mark_all_read),
                        color = colorResource(com.breakfast.R.color.blue_ribbon),
                        modifier = Modifier.padding(start = 6.dp))
                }
            }
            LazyColumn {
                items(sample) { model ->
                    val isUnread = model.isRead != true
                    val title = model.title ?: androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.notification)
                    val subtitle = model.message ?: ""
                    val date = model.createdAt ?: ""
                    OrderCard(
                        title = title,
                        subtitle = subtitle,
                        date = date,
                        avatarUrl = null,
                        icon = if (isUnread) null else androidx.compose.material.icons.Icons.Filled.Circle,
                        onClick = { /* no-op in preview */ },
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
        }
    }
    // NotificationScreen(navController)  // previews don't navigate
}