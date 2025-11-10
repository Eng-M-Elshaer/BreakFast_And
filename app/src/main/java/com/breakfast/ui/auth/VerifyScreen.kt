package com.breakfast.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.breakfast.BreakfastApplication
import com.breakfast.designsystem.BreakfastButtonRes
import com.breakfast.network.ApiClient
import com.breakfast.ui.components.ErrorDialog
import com.breakfast.utils.Result
import com.breakfast.viewmodel.AuthViewModel
import com.breakfast.viewmodel.AuthViewModelFactory
import androidx.compose.ui.tooling.preview.Preview
import com.breakfast.R
import kotlinx.coroutines.delay

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun VerifyScreen(email: String, navController: NavController) {

    val codeState = remember { mutableStateOf("") }
    val preferenceManager = BreakfastApplication.get().preferenceManager
    val apiService = remember { ApiClient.apiService }
    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(preferenceManager, apiService, context))
    val verifyState = viewModel.verifyCodeState.collectAsState(null)
    val defaultFailed = stringResource(id = com.breakfast.R.string.failed_verify_code)
    val showError = remember { mutableStateOf(false) }
    val errorMsg = remember { mutableStateOf("") }

    LaunchedEffect(verifyState.value) {
        val s = verifyState.value
        if (s is Result.Error) {
            errorMsg.value = s.message ?: defaultFailed
            showError.value = true
        }
    }

    VerifyContent(
        code = codeState.value,
        onCodeChange = { codeState.value = it },
        verifyState = verifyState.value,
        onVerify = {
            val c = codeState.value
            if (c.length == 4 && c.all { it.isDigit() }) {
                viewModel.verifyResetCode(email, c)
            }
        }
    )

    ErrorDialog(
        visible = showError.value,
        title = stringResource(id = com.breakfast.R.string.something_went_wrong),
        message = errorMsg.value,
        buttonText = stringResource(id = com.breakfast.R.string.submit),
        onDismiss = {
            showError.value = false
            viewModel.clearVerifyCodeState()
        }
    )

    when (val state = verifyState.value) {
        is Result.Success<*> -> {
            LaunchedEffect(Unit) {
                navController.navigate("reset_password/${email}/${codeState.value}") {
                    popUpTo("verify_code/{email}") { inclusive = true }
                }
            }
        }
        else -> {}
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun VerifyContent(
    code: String,
    onCodeChange: (String) -> Unit,
    verifyState: Result<*>?,
    onVerify: () -> Unit
) {
    val codeError: String? = when {
        code.isBlank() -> null
        code.length < 4 || !code.all { it.isDigit() } -> stringResource(R.string.enter_the_4_digit_code)
        else -> null
    }
    val canVerify = code.isNotBlank() && codeError == null
    Scaffold(
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .offset(y = (-100).dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.authentication_code),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = com.breakfast.R.color.woodsmoke)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.enter_the_4_digit_code_we_just_texted_to_your_phone_number),
                color = colorResource(id = com.breakfast.R.color.dove_gray)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Split code into 4 digits
            val chars = (code + "    ").take(4)
            var d1 = remember(code) { chars.getOrNull(0)?.takeIf { it.isDigit() }?.toString() ?: "" }
            var d2 = remember(code) { chars.getOrNull(1)?.takeIf { it.isDigit() }?.toString() ?: "" }
            var d3 = remember(code) { chars.getOrNull(2)?.takeIf { it.isDigit() }?.toString() ?: "" }
            var d4 = remember(code) { chars.getOrNull(3)?.takeIf { it.isDigit() }?.toString() ?: "" }

            val r1 = remember { FocusRequester() }
            val r2 = remember { FocusRequester() }
            val r3 = remember { FocusRequester() }
            val r4 = remember { FocusRequester() }
            val focusManager = LocalFocusManager.current

            fun rebuild() = onCodeChange((d1 + d2 + d3 + d4).take(4))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                @Composable
                fun cell(value: String, onChange: (String) -> Unit, requester: FocusRequester, next: FocusRequester?) {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { t ->
                            val c = t.filter { it.isDigit() }.take(1)
                            onChange(c)
                            rebuild()
                            if (c.isNotEmpty()) {
                                next?.requestFocus() ?: focusManager.clearFocus()
                            }
                        },
                        textStyle = TextStyle(
                            fontSize = 24.sp,
                            textAlign = TextAlign.Center
                        ),
                        singleLine = true,
                        isError = codeError != null,
                        modifier = Modifier
                            .width(64.dp)
                            .height(64.dp)
                            .focusRequester(requester),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                cell(d1, { d1 = it }, r1, r2)
                cell(d2, { d2 = it }, r2, r3)
                cell(d3, { d3 = it }, r3, r4)
                cell(d4, { d4 = it }, r4, null)
            }

            if (codeError != null) {
                Text(
                    text = codeError,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Loading only; errors are handled by dialog at the screen level
            when (verifyState) {
                is Result.Loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                else -> {}
            }

            // Resend timer
            var seconds by remember { mutableStateOf(60) }
            LaunchedEffect(seconds) {
                if (seconds > 0) {
                    delay(1000)
                    seconds -= 1
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Resend In:")
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = seconds.toString(),
                    color = colorResource(id = com.breakfast.R.color.blue_ribbon),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            BreakfastButtonRes(
                onClick = onVerify,
                enabled = canVerify,
                isHasObserver = !canVerify,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(androidx.compose.ui.res.stringResource(id = com.breakfast.R.string.continue_text))
            }
        }
    }
}

@Preview(showBackground = true, name = "Verify Screen Preview")
@Composable
private fun VerifyPreview() {
    val code = remember { mutableStateOf("") }
    val state: Result<*>? = null

    VerifyContent(
        code = code.value,
        onCodeChange = { code.value = it },
        verifyState = state,
        onVerify = {}
    )
}