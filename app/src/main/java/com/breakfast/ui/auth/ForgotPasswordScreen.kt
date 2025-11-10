
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.MaterialTheme
import com.breakfast.utils.Validator
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import com.breakfast.designsystem.BreakfastButtonRes
import com.breakfast.designsystem.BreakfastOutlinedTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.breakfast.BreakfastApplication
import com.breakfast.network.ApiClient
import com.breakfast.utils.Result
import com.breakfast.viewmodel.AuthViewModel
import com.breakfast.viewmodel.AuthViewModelFactory
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.platform.LocalContext
import com.breakfast.ui.components.ErrorDialog
import androidx.compose.ui.res.stringResource

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun ForgotPasswordContent(
    email: String,
    onEmailChange: (String) -> Unit,
    sendState: Result<*>?,
    onSend: () -> Unit,
    illustrationResId: Int? = null
) {
    val focusManager = LocalFocusManager.current
    val emailError: String? = when {
        email.isBlank() -> null
        !Validator.isValidEmail(email) -> stringResource(id = com.breakfast.R.string.invalid_email)
        else -> null
    }
    val canSend = email.isNotBlank() && emailError == null
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

            // Illustration
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
                text = stringResource(id = com.breakfast.R.string.forgot_password),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = com.breakfast.R.color.woodsmoke),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = stringResource(id = com.breakfast.R.string.pleaseEnterYourRegisteredEmailAndWeWillSendYouTheCode),
                color = colorResource(id = com.breakfast.R.color.dove_gray),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Email label + field
            BreakfastOutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = stringResource(id = com.breakfast.R.string.email),
                isError = emailError != null,
                errorText = emailError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Loading only; errors are shown via dialog at the screen layer
            when (sendState) {
                is Result.Loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                else -> {}
            }

            Spacer(modifier = Modifier.height(32.dp))

            BreakfastButtonRes(
                onClick = onSend,
                enabled = canSend,
                isHasObserver = !canSend,
                modifier = Modifier
                    .fillMaxWidth()
            ) { Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.send_reset_code)) }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ForgotPasswordScreen(navController: NavController) {

    val email = remember { mutableStateOf("") }
    val preferenceManager = BreakfastApplication.get().preferenceManager
    val apiService = remember { ApiClient.apiService }
    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(preferenceManager, apiService, context))
    val sendState = viewModel.sendResetMailState.collectAsState(null)
    val defaultFailed = stringResource(id = com.breakfast.R.string.failed_send_reset)
    val showError = remember { mutableStateOf(false) }
    val errorMsg = remember { mutableStateOf("") }

    LaunchedEffect(sendState.value) {
        val s = sendState.value
        if (s is Result.Error) {
            errorMsg.value = s.message ?: defaultFailed
            showError.value = true
        }
    }

    // Navigate on success
    when (sendState.value) {
        is Result.Success<*> -> {
            LaunchedEffect(Unit) {
                navController.navigate("verify_code/${email.value}") {
                    popUpTo("forgot_password") { inclusive = true }
                }
            }
        }
        else -> {}
    }

    ForgotPasswordContent(
        email = email.value,
        onEmailChange = { email.value = it },
        sendState = sendState.value,
        onSend = {
            if (!Validator.isValidEmail(email.value)) return@ForgotPasswordContent
            viewModel.sendResetMail(email.value)
        },
        illustrationResId = com.breakfast.R.drawable.forget_password_placeholder
    )

    ErrorDialog(
        visible = showError.value,
        title = "Heading",
        message = errorMsg.value,
        buttonText = "Submit",
        onDismiss = {
            showError.value = false
            viewModel.clearSendResetMailState()
        }
    )
}

@Preview(showBackground = true, name = "ForgotPassword Preview")
@Composable
private fun ForgotPasswordPreview() {

    val email = remember { mutableStateOf("jane@example.com") }
    val state: Result<*>? = null

    ForgotPasswordContent(
        email = email.value,
        onEmailChange = { email.value = it },
        sendState = state,
        onSend = {},
        illustrationResId = com.breakfast.R.drawable.forget_password_placeholder
    )
}