
package com.breakfast.ui.auth
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.breakfast.BreakfastApplication
import com.breakfast.designsystem.BreakfastButtonRes
import com.breakfast.network.ApiClient
import com.breakfast.ui.components.ErrorDialog
import com.breakfast.utils.Result
import com.breakfast.utils.Validator
import com.breakfast.viewmodel.AuthViewModel
import com.breakfast.viewmodel.AuthViewModelFactory


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(navController: NavController? = null) {

    val currentPassword = remember { mutableStateOf("") }
    val newPassword = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }
    val preferenceManager = BreakfastApplication.get().preferenceManager
    val apiService = remember { ApiClient.apiService }
    val viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(preferenceManager, apiService))
    val changeState = viewModel.changePasswordState.collectAsState(null)
    val defaultFailed = stringResource(id = com.breakfast.R.string.failed_change_password)
    val showError = remember { mutableStateOf(false) }
    val errorMsg = remember { mutableStateOf("") }

    LaunchedEffect(changeState.value) {
        val s = changeState.value
        if (s is Result.Error) {
            errorMsg.value = s.message ?: defaultFailed
            showError.value = true
        }
    }

    ChangePasswordContent(
        currentPassword = currentPassword.value,
        newPassword = newPassword.value,
        confirmPassword = confirmPassword.value,
        onCurrentPasswordChange = { currentPassword.value = it },
        onNewPasswordChange = { newPassword.value = it },
        onConfirmPasswordChange = { confirmPassword.value = it },
        changeState = changeState.value,
        onChangePassword = {
            if (currentPassword.value.isNotBlank() && newPassword.value.isNotBlank() && confirmPassword.value.isNotBlank()) {
                viewModel.changePassword(currentPassword.value, newPassword.value, confirmPassword.value)
            }
        },
        illustrationResId = null
    )

    ErrorDialog(
        visible = showError.value,
        title = stringResource(id = com.breakfast.R.string.something_went_wrong),
        message = errorMsg.value,
        buttonText = stringResource(id = com.breakfast.R.string.submit),
        onDismiss = {
            showError.value = false
            viewModel.clearChangePasswordState()
        }
    )

    // Navigate back on success (if navController is provided)
    if (changeState.value is Result.Success<*>) {
        navController?.popBackStack()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangePasswordContent(
    currentPassword: String,
    newPassword: String,
    confirmPassword: String,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    changeState: Result<*>?,
    onChangePassword: () -> Unit,
    illustrationResId: Int? = null
) {

    var currentVisible = remember { mutableStateOf(false) }
    var newVisible = remember { mutableStateOf(false) }
    var confirmVisible = remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val newFocus = remember { FocusRequester() }
    val confirmFocus = remember { FocusRequester() }

    val currentError: String? = when {
        currentPassword.isBlank() -> null
        !Validator.isValidPassword(currentPassword) -> null // we don't enforce strength on current password
        else -> null
    }
    val newError: String? = when {
        newPassword.isBlank() -> null
        !Validator.isValidPassword(newPassword) -> stringResource(id = com.breakfast.R.string.invalid_password)
        else -> null
    }
    val confirmError: String? = when {
        confirmPassword.isBlank() -> null
        confirmPassword != newPassword -> stringResource(id = com.breakfast.R.string.password_mismatch)
        else -> null
    }
    val canChange =
        currentPassword.isNotBlank() &&
        newPassword.isNotBlank() &&
        confirmPassword.isNotBlank() &&
        newError == null &&
        confirmError == null

    Scaffold(
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Illustration (optional)
            illustrationResId?.let { resId ->
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = null,
                    modifier = Modifier
                        .height(250.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Title & subtitle
            Text(
                text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.change_password_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = com.breakfast.R.color.woodsmoke),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Current password
            Text(
                text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.current_password),
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = com.breakfast.R.color.woodsmoke),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = currentPassword,
                onValueChange = onCurrentPasswordChange,
                placeholder = { Text(stringResource(id = com.breakfast.R.string.current_password)) },
                singleLine = true,
                isError = currentError != null,
                visualTransformation = if (currentVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { currentVisible.value = !currentVisible.value }) {
                        Icon(
                            imageVector = if (currentVisible.value) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { newFocus.requestFocus() }),
                modifier = Modifier.fillMaxWidth()
            )
            if (currentError != null) {
                Text(
                    text = currentError,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // New password
            Text(
                text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.new_password),
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = com.breakfast.R.color.woodsmoke),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = newPassword,
                onValueChange = onNewPasswordChange,
                placeholder = { Text(stringResource(id = com.breakfast.R.string.new_password)) },
                singleLine = true,
                isError = newError != null,
                visualTransformation = if (newVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { newVisible.value = !newVisible.value }) {
                        Icon(
                            imageVector = if (newVisible.value) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { confirmFocus.requestFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(newFocus)
            )
            if (newError != null) {
                Text(
                    text = newError,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm password
            Text(
                text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.confirm_password),
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = com.breakfast.R.color.woodsmoke),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                placeholder = { Text(stringResource(id = com.breakfast.R.string.confirm_password)) },
                singleLine = true,
                isError = confirmError != null,
                visualTransformation = if (confirmVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmVisible.value = !confirmVisible.value }) {
                        Icon(
                            imageVector = if (confirmVisible.value) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = null
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onChangePassword()
                        focusManager.clearFocus()
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(confirmFocus)
            )
            if (confirmError != null) {
                Text(
                    text = confirmError,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }

            // Loading only; errors shown via dialog in screen layer
            when (changeState) {
                is Result.Loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                else -> {}
            }

            Spacer(modifier = Modifier.height(32.dp))

            BreakfastButtonRes(
                onClick = onChangePassword,
                enabled = canChange,
                isHasObserver = !canChange,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.update_password))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, name = "ChangePassword Preview")
@Composable
private fun ChangePasswordPreview() {

    val current = remember { mutableStateOf("oldpass") }
    val newPass = remember { mutableStateOf("newpass") }
    val confirm = remember { mutableStateOf("newpass") }
    val state: Result<*>? = null

    ChangePasswordContent(
        currentPassword = current.value,
        newPassword = newPass.value,
        confirmPassword = confirm.value,
        onCurrentPasswordChange = { current.value = it },
        onNewPasswordChange = { newPass.value = it },
        onConfirmPasswordChange = { confirm.value = it },
        changeState = state,
        onChangePassword = {},
        illustrationResId = null
    )
}