
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.breakfast.BreakfastApplication
import com.breakfast.R
import com.breakfast.designsystem.BreakfastButtonRes
import com.breakfast.designsystem.BreakfastOutlinedTextField
import com.breakfast.network.ApiClient
import com.breakfast.ui.components.ErrorDialog
import com.breakfast.utils.Result
import com.breakfast.utils.Validator
import com.breakfast.viewmodel.AuthViewModel
import com.breakfast.viewmodel.AuthViewModelFactory

@Composable
fun LoginScreen(navController: NavController, onSignUp: () -> Unit = {}) {

    // State variables
    val phone = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val preferenceManager = BreakfastApplication.get().preferenceManager
    val apiService = remember { ApiClient.apiService }
    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(preferenceManager, apiService, context))
    val loginState = viewModel.loginState.collectAsState(null)
    val state = loginState.value
    val showError = remember { mutableStateOf(false) }
    val errorMsg  = remember { mutableStateOf("") }
    val defaultLoginFailed = stringResource(id = com.breakfast.R.string.login_failed)

    LaunchedEffect(state) {
        if (state is Result.Error) {
            errorMsg.value = state.message ?: defaultLoginFailed
            showError.value = true
        }
    }

    if (state is Result.Success<*>) {
        navController.navigate("main") {
            popUpTo("login") { inclusive = true }
        }
    }

    LoginScreenContent(
        phone = phone.value,
        password = password.value,
        onPhoneChange = { phone.value = it },
        onPasswordChange = { password.value = it },
        loginState = loginState.value,
        onLogin = {
            if (phone.value.isNotBlank() && password.value.isNotBlank()) {
                viewModel.login(phone.value, password.value)
            }
        },
        onForgotPassword = { navController.navigate("forgot_password") },
        onSignUp = onSignUp,
        illustrationResId = R.drawable.sign_in_placeholder
    )

    ErrorDialog(
        visible = showError.value,
        title = stringResource(id = com.breakfast.R.string.something_went_wrong),
        message = errorMsg.value,
        buttonText = stringResource(id = com.breakfast.R.string.submit),
        onDismiss = {
            showError.value = false
            viewModel.clearLoginState()
        }
    )
}

@Composable
private fun LoginScreenContent(
    phone: String,
    password: String,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    loginState: Result<*>?,
    onLogin: () -> Unit,
    onForgotPassword: () -> Unit,
    onSignUp: () -> Unit,
    illustrationResId: Int? = null
) {

    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val passwordFocus = remember { FocusRequester() }

    // Inline validation while typing
    val phoneError: String? = when {
        phone.isBlank() -> null // don't show error on empty, wait until user starts typing
        !Validator.isValidEgyptianPhoneNumber(phone) -> stringResource(id = com.breakfast.R.string.invalid_phone_number)
        else -> null
    }
    val passwordError: String? = when {
        password.isBlank() -> null
        password.length < 8 -> stringResource(id = com.breakfast.R.string.invalid_password)
        else -> null // for login we just require non-empty; keep strong checks for register
    }

    val canLogin = phone.isNotBlank() && password.isNotBlank() && phoneError == null && passwordError == null

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
            text = stringResource(id = com.breakfast.R.string.welcome_back),
            fontWeight = FontWeight.Bold,
            color = colorResource(id = com.breakfast.R.color.woodsmoke),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Phone label + field
        BreakfastOutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = stringResource(id = com.breakfast.R.string.phone_number),
            isError = phoneError != null,
            errorText = phoneError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password label + field
        BreakfastOutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = stringResource(id = com.breakfast.R.string.password),
            isError = passwordError != null,
            errorText = passwordError,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(passwordFocus),
            trailing = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = null
                    )
                }
            }
        )
        LaunchedEffect(password) {
            // no-op, keep structure
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Forgot password link
        Text(
            text = stringResource(id = com.breakfast.R.string.forgot_password),
            color = colorResource(id = com.breakfast.R.color.blue_ribbon),
            modifier = Modifier
                .align(Alignment.End)
                .clickable(onClick = onForgotPassword)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // State handling (show loader only; errors are shown via dialog in the screen layer)
        when (loginState) {
            is Result.Loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
            else -> {}
        }

        // Sign in button
        BreakfastButtonRes(
            onClick = onLogin,
            enabled = canLogin,
            isHasObserver = !canLogin,
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
        ) {
            Text(stringResource(id = com.breakfast.R.string.sign_in))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign up row
        Row {
            Text(
                text = stringResource(id = com.breakfast.R.string.dont_have_account),
                color = colorResource(id = com.breakfast.R.color.dove_gray)
            )
            Text(
                text = stringResource(id = com.breakfast.R.string.create_account_short),
                color = colorResource(id = com.breakfast.R.color.blue_ribbon),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onSignUp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true, name = "LoginScreen Preview")
@Composable
private fun LoginScreenPreview() {

    val phone = remember { mutableStateOf("01000000000") }
    val password = remember { mutableStateOf("password") }
    val state: Result<*>? = null

    LoginScreenContent(
        phone = phone.value,
        password = password.value,
        onPhoneChange = { phone.value = it },
        onPasswordChange = { password.value = it },
        loginState = state,
        onLogin = {},
        onForgotPassword = {},
        onSignUp = {},
        illustrationResId = R.drawable.sign_in_placeholder
    )
}