package com.breakfast.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.breakfast.BreakfastApplication
import com.breakfast.network.ApiClient
import com.breakfast.utils.Result
import com.breakfast.viewmodel.AuthViewModel
import com.breakfast.viewmodel.AuthViewModelFactory
import androidx.compose.ui.tooling.preview.Preview
import com.breakfast.designsystem.BreakfastButtonRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.MaterialTheme
import com.breakfast.utils.Validator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import com.breakfast.ui.components.ErrorDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController, onSignUpSuccess: () -> Unit = {}) {

    val name = remember { mutableStateOf("") }
    val phone = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }
    val preferenceManager = BreakfastApplication.get().preferenceManager
    val apiService = remember { ApiClient.apiService }
    val viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(preferenceManager, apiService))
    val registerState = viewModel.registerState.collectAsState(null)
    val defaultRegisterFailed = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.registration_failed)
    val showError = remember { mutableStateOf(false) }
    val errorMsg = remember { mutableStateOf("") }

    LaunchedEffect(registerState.value) {
        val s = registerState.value
        if (s is Result.Error) {
            errorMsg.value = s.message ?: defaultRegisterFailed
            showError.value = true
        }
    }

    // Navigate when success
    if (registerState.value is Result.Success<*>) {
        navController.navigate("main") { popUpTo("signup") { inclusive = true } }
    }

    SignUpScreenContent(
        name = name.value,
        phone = phone.value,
        email = email.value,
        password = password.value,
        confirmPassword = confirmPassword.value,
        onNameChange = { name.value = it },
        onPhoneChange = { phone.value = it },
        onEmailChange = { email.value = it },
        onPasswordChange = { password.value = it },
        onConfirmPasswordChange = { confirmPassword.value = it },
        registerState = registerState.value,
        onRegister = {
            if (
                name.value.isNotBlank() &&
                phone.value.isNotBlank() &&
                email.value.isNotBlank() &&
                password.value.isNotBlank() &&
                confirmPassword.value == password.value
            ) {
                viewModel.register(name.value, phone.value, email.value, password.value)
            }
        },
        onSignIn = { navController.navigate("login") },
        illustrationResId = com.breakfast.R.drawable.sign_up_placeholder
    )

    ErrorDialog(
        visible = showError.value,
        title = stringResource(id = com.breakfast.R.string.something_went_wrong),
        message = errorMsg.value,
        buttonText = stringResource(id = com.breakfast.R.string.submit),
        onDismiss = {
            showError.value = false
            viewModel.clearRegisterState()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignUpScreenContent(
    name: String,
    phone: String,
    email: String,
    password: String,
    confirmPassword: String,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    registerState: Result<*>?,
    onRegister: () -> Unit,
    onSignIn: () -> Unit,
    illustrationResId: Int? = null
) {

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val phoneFocus = remember { FocusRequester() }
    val emailFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    val confirmFocus = remember { FocusRequester() }

    // Inline validation while typing (show only when user typed something)
    val nameError: String? = when {
        name.isBlank() -> null
        !Validator.isValidFullName(name) -> stringResource(id = com.breakfast.R.string.invalid_name)
        else -> null
    }
    val phoneError: String? = when {
        phone.isBlank() -> null
        !Validator.isValidEgyptianPhoneNumber(phone) -> stringResource(id = com.breakfast.R.string.invalid_phone_number)
        else -> null
    }
    val emailError: String? = when {
        email.isBlank() -> null
        !Validator.isValidEmail(email) -> stringResource(id = com.breakfast.R.string.invalid_email)
        else -> null
    }
    val passwordError: String? = when {
        password.isBlank() -> null
        password.length < 8 -> stringResource(id = com.breakfast.R.string.invalid_password)
        else -> null
    }
    val confirmError: String? = when {
        confirmPassword.isBlank() -> null
        confirmPassword != password -> stringResource(id = com.breakfast.R.string.password_mismatch)
        else -> null
    }

    val canRegister =
        name.isNotBlank() &&
        phone.isNotBlank() &&
        email.isNotBlank() &&
        password.isNotBlank() &&
        confirmPassword.isNotBlank() &&
        nameError == null &&
        phoneError == null &&
        emailError == null &&
        passwordError == null &&
        confirmError == null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(50.dp))

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

        // Welcome title
        Text(
            text = stringResource(id = com.breakfast.R.string.welcome),
            fontWeight = FontWeight.Bold,
            color = colorResource(id = com.breakfast.R.color.woodsmoke),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Full name
        Text(
            text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.full_name),
            fontWeight = FontWeight.SemiBold,
            color = colorResource(id = com.breakfast.R.color.woodsmoke),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = { Text(stringResource(id = com.breakfast.R.string.full_name)) },
            singleLine = true,
            isError = nameError != null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { phoneFocus.requestFocus() }),
            modifier = Modifier
                .fillMaxWidth()
        )
        if (nameError != null) {
            Text(
                text = nameError,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Phone
        Text(
            text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.phone_number),
            fontWeight = FontWeight.SemiBold,
            color = colorResource(id = com.breakfast.R.color.woodsmoke),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            placeholder = { Text(stringResource(id = com.breakfast.R.string.phone_number)) },
            singleLine = true,
            isError = phoneError != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { emailFocus.requestFocus() }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(phoneFocus)
        )
        if (phoneError != null) {
            Text(
                text = phoneError,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Email
        Text(
            text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.email),
            fontWeight = FontWeight.SemiBold,
            color = colorResource(id = com.breakfast.R.color.woodsmoke),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = { Text(stringResource(id = com.breakfast.R.string.email)) },
            singleLine = true,
            isError = emailError != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(emailFocus)
        )
        if (emailError != null) {
            Text(
                text = emailError,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Password
        Text(
            text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.password),
            fontWeight = FontWeight.SemiBold,
            color = colorResource(id = com.breakfast.R.color.woodsmoke),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            placeholder = { Text(stringResource(id = com.breakfast.R.string.password)) },
            singleLine = true,
            isError = passwordError != null,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
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
                .focusRequester(passwordFocus)
        )
        if (passwordError != null) {
            Text(
                text = passwordError,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password
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
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = null
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
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

        // State handling (show loader only; errors via dialog)
        when (registerState) {
            is Result.Loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
            else -> {}
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign Up button
        BreakfastButtonRes(
            onClick = onRegister,
            enabled = canRegister,
            isHasObserver = !canRegister,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.sign_up))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign In link row
        Row {
            Text(
                text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.have_account),
                color = colorResource(id = com.breakfast.R.color.dove_gray)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.sign_in),
                color = colorResource(id = com.breakfast.R.color.blue_ribbon),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onSignIn)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true, name = "SignUpScreen Preview")
@Composable
private fun SignUpScreenPreview() {
    val name = remember { mutableStateOf("Jane Doe") }
    val phone = remember { mutableStateOf("01000000000") }
    val email = remember { mutableStateOf("jane@example.com") }
    val password = remember { mutableStateOf("password") }
    val state: Result<*>? = null

    SignUpScreenContent(
        name = name.value,
        phone = phone.value,
        email = email.value,
        password = password.value,
        confirmPassword = password.value,
        onNameChange = { name.value = it },
        onPhoneChange = { phone.value = it },
        onEmailChange = { email.value = it },
        onPasswordChange = { password.value = it },
        onConfirmPasswordChange = { },
        registerState = state,
        onRegister = {},
        onSignIn = {},
        illustrationResId = com.breakfast.R.drawable.sign_up_placeholder
    )
}