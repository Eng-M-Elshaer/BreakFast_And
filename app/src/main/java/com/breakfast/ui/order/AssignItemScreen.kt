
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.breakfast.models.PersonModel
import com.breakfast.network.ApiClient
import com.breakfast.utils.Result
import com.breakfast.viewmodel.OrderViewModel

/**
 * Screen that allows the user to assign an order item to another user. It fetches the list
 * of available users and displays them with radio buttons. Upon selection and confirmation,
 * it calls into the [OrderViewModel] to perform the assignment.
 *
 * @param orderId the ID of the order containing the item
 * @param orderItemId the ID of the order item to assign
 * @param navController optional NavController for navigating back after assignment
 */

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun AssignItemScreen(
    orderId: Int,
    orderItemId: Int,
    navController: NavController? = null
) {

    val apiService = ApiClient.apiService
    val context = LocalContext.current
    val viewModel: OrderViewModel = viewModel(factory = OrderViewModel.Factory(apiService, context))
    val usersState by viewModel.usersState.collectAsState()
    val assignState by viewModel.assignState.collectAsState()
    val selectedUserId = remember { mutableStateOf<Int?>(null) }

    // Fetch users when the screen first appears
    LaunchedEffect(Unit) {
        viewModel.fetchUsers()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.assign)) }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            when (val state = usersState) {
                is Result.Loading -> {
                    CircularProgressIndicator()
                }
                is Result.Error -> {
                    Text(text = state.message ?: androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.failed_load_content))
                }
                is Result.Success<*> -> {
                    val users = state.data as? List<*>
                    val casted = users?.filterIsInstance<PersonModel>() ?: emptyList()
                    if (casted.isEmpty()) {
                        Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.no_users_available))
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                            items(casted) { user ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    RadioButton(
                                        selected = selectedUserId.value == user.id,
                                        onClick = { selectedUserId.value = user.id }
                                    )
                                    Text(text = user.name ?: androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.name), modifier = Modifier.padding(start = 8.dp))
                                }
                            }
                        }
                        when (val assign = assignState) {
                            is Result.Loading -> {
                                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                            }
                            is Result.Error -> {
                                Text(text = assign.message ?: androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.assignment_failed), modifier = Modifier.padding(top = 16.dp))
                            }
                            is Result.Success<*> -> {
                                LaunchedEffect(assign) {
                                    navController?.popBackStack()
                                }
                                Text(text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.assign_success), modifier = Modifier.padding(top = 16.dp))
                            }
                            else -> {
                                Button(
                                    onClick = {
                                        selectedUserId.value?.let { uid ->
                                            viewModel.assignItem(orderItemId, uid)
                                        }
                                    },
                                    enabled = selectedUserId.value != null,
                                    modifier = Modifier.padding(top = 16.dp)
                                ) {
                                    Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.assign))
                                }
                            }
                        }
                        Button(
                            onClick = { navController?.popBackStack() },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.cancel))
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AssignItemScreenPreview() {
    Surface {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Assign")
            LazyColumn {
                items(
                    listOf(
                        PersonModel(id = 1, name = "User One", email = "one@mail.com", status = null, phone = "01000000001"),
                        PersonModel(id = 2, name = "User Two", email = "two@mail.com", status = null, phone = "01000000002"),
                        PersonModel(id = 3, name = "User Three", email = "three@mail.com", status = null, phone = "01000000003")
                    )
                ) { user ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(selected = false, onClick = { })
                        Text(text = user.name ?: "-", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
            Button(
                onClick = {},
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Assign")
            }
            Button(
                onClick = {},
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Cancel")
            }
        }
    }
}