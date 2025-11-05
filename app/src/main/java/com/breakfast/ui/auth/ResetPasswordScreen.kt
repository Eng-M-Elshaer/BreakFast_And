package com.breakfast.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.breakfast.BreakfastApplication
import com.breakfast.designsystem.BreakfastButtonRes
import com.breakfast.network.ApiClient
import com.breakfast.ui.components.ErrorDialog
import com.breakfast.utils.Result
import com.breakfast.utils.Validator
import com.breakfast.viewmodel.AuthViewModel
import com.breakfast.viewmodel.AuthViewModelFactory


@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(email: String, code: String, navController: NavController) {
    val newPassword = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }
    val preferenceManager = BreakfastApplication.get().preferenceManager
    val apiService = remember { ApiClient.apiService }
    val viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(preferenceManager, apiService))
    val resetState = viewModel.resetPasswordState.collectAsState(null)

    val defaultFailed = stringResource(id = com.breakfast.R.string.failed_reset_password)
    val showError = remember { mutableStateOf(false) }
    val errorMsg = remember { mutableStateOf("") }

    LaunchedEffect(resetState.value) {
        val s = resetState.value
        if (s is Result.Error) {
            errorMsg.value = s.message ?: defaultFailed
            showError.value = true
        }
    }

    when (resetState.value) {
        is Result.Success<*> -> {
            LaunchedEffect(Unit) {
                navController.navigate("login") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }
        else -> {}
    }

    ResetPasswordContent(
        newPassword = newPassword.value,
        confirmPassword = confirmPassword.value,
        onNewPasswordChange = { newPassword.value = it },
        onConfirmPasswordChange = { confirmPassword.value = it },
        resetState = resetState.value,
        onChangePassword = {
            if (newPassword.value.isNotBlank() && confirmPassword.value.isNotBlank()) {
                viewModel.resetPassword(email, code, newPassword.value, confirmPassword.value)
            }
        },
        illustrationResId = null
    )

    ErrorDialog(
        visible = showError.value,
        title = "Heading",
        message = errorMsg.value,
        buttonText = "Submit",
        onDismiss = {
            showError.value = false
            viewModel.clearResetPasswordState()
        }
    )
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun ResetPasswordContent(
    newPassword: String,
    confirmPassword: String,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    resetState: Result<*>?,
    onChangePassword: () -> Unit,
    illustrationResId: Int? = null
) {
    var passwordVisible = remember { mutableStateOf(false) }
    var confirmPasswordVisible = remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val confirmFocus = remember { FocusRequester() }

    val newPasswordError: String? = when {
        newPassword.isBlank() -> null
        !Validator.isValidPassword(newPassword) -> stringResource(id = com.breakfast.R.string.invalid_password)
        else -> null
    }
    val confirmPasswordError: String? = when {
        confirmPassword.isBlank() -> null
        confirmPassword != newPassword -> stringResource(id = com.breakfast.R.string.password_mismatch)
        else -> null
    }

    val canChange =
        newPassword.isNotBlank() &&
        confirmPassword.isNotBlank() &&
        newPasswordError == null &&
        confirmPasswordError == null

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

            // Title
            Text(
                text = stringResource(id = com.breakfast.R.string.reset_password_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = com.breakfast.R.color.woodsmoke),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // New Password label + field
            Text(
                text = stringResource(id = com.breakfast.R.string.new_password),
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = com.breakfast.R.color.woodsmoke),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = newPassword,
                onValueChange = onNewPasswordChange,
                placeholder = { Text(stringResource(id = com.breakfast.R.string.new_password)) },
                singleLine = true,
                isError = newPasswordError != null,
                visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                        Icon(
                            imageVector = if (passwordVisible.value) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
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
            )
            if (newPasswordError != null) {
                Text(
                    text = newPasswordError,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password label + field
            Text(
                text = stringResource(id = com.breakfast.R.string.confirm_password),
                fontWeight = FontWeight.SemiBold,
                color = colorResource(id = com.breakfast.R.color.woodsmoke),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                placeholder = { Text(stringResource(id = com.breakfast.R.string.confirm_password)) },
                singleLine = true,
                isError = confirmPasswordError != null,
                visualTransformation = if (confirmPasswordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible.value = !confirmPasswordVisible.value }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible.value) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
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
            if (confirmPasswordError != null) {
                Text(
                    text = confirmPasswordError,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }

            // Loading only; errors are shown via dialog in the screen layer
            when (resetState) {
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
                Text(stringResource(id = com.breakfast.R.string.change_password))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, name = "ResetPassword Preview")
@Composable
private fun ResetPasswordPreview() {
    val newPassword = remember { mutableStateOf("password") }
    val confirmPassword = remember { mutableStateOf("password") }
    val state: Result<*>? = null

    ResetPasswordContent(
        newPassword = newPassword.value,
        confirmPassword = confirmPassword.value,
        onNewPasswordChange = { newPassword.value = it },
        onConfirmPasswordChange = { confirmPassword.value = it },
        resetState = state,
        onChangePassword = {},
        illustrationResId = null
    )
}