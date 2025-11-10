package com.breakfast.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.breakfast.managers.PreferenceManager
import com.breakfast.repositories.AuthRepository
import com.breakfast.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.breakfast.utils.Validator
import kotlinx.coroutines.Job

/**
 * ViewModel for managing authentication state. It exposes login and registration flows as
 * StateFlow that the UI can observe to react to loading, success or error states.
 */
class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    // Keep only the latest job for each action
    private var loginJob: Job? = null
    private var registerJob: Job? = null
    private var changePwdJob: Job? = null
    private var sendMailJob: Job? = null
    private var verifyCodeJob: Job? = null
    private var resetPwdJob: Job? = null

    private inline fun launchLatest(previous: Job?, crossinline block: suspend () -> Unit): Job {
        previous?.cancel()
        return viewModelScope.launch { block() }
    }

    private val _loginState = MutableStateFlow<Result<Any>?>(null)
    val loginState = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<Result<Any>?>(null)
    val registerState = _registerState.asStateFlow()

    // Change password state
    private val _changePasswordState = MutableStateFlow<Result<Unit>?>(null)
    val changePasswordState = _changePasswordState.asStateFlow()

    // Forgot password: send reset mail state
    private val _sendResetMailState = MutableStateFlow<Result<Unit>?>(null)
    val sendResetMailState = _sendResetMailState.asStateFlow()

    // Forgot password: verify code state
    private val _verifyCodeState = MutableStateFlow<Result<Unit>?>(null)
    val verifyCodeState = _verifyCodeState.asStateFlow()

    // Forgot password: reset password state
    private val _resetPasswordState = MutableStateFlow<Result<Unit>?>(null)
    val resetPasswordState = _resetPasswordState.asStateFlow()

    /**
     * Trigger login using the repository. Updates the [loginState] accordingly.
     */
    fun login(phone: String, password: String) {
        // Validate each field independently; both are required
        if (Validator.isEmpty(phone)) {
            _loginState.value = Result.Error("Phone is required")
            return
        }
        if (Validator.isEmpty(password)) {
            _loginState.value = Result.Error("Password is required")
            return
        }
        // Optional: if you expect Egyptian numbers
        if (!Validator.isValidEgyptianPhoneNumber(phone)) {
            _loginState.value = Result.Error("Invalid phone number format")
            return
        }
        _loginState.value = Result.Loading
        loginJob = launchLatest(loginJob) {
            val result = authRepository.login(phone, password)
            _loginState.value = result
        }
    }

    /**
     * Trigger registration. Updates the [registerState] accordingly.
     */
    fun register(name: String, phone: String, email: String, password: String?) {
        if (Validator.isEmpty(name) || Validator.isEmpty(phone) || Validator.isEmpty(email) || (password ?: "").isEmpty()) {
            _registerState.value = Result.Error("All fields are required")
            return
        }
        if (!Validator.isValidFullName(name)) {
            _registerState.value = Result.Error("Enter a valid full name")
            return
        }
        if (!Validator.isValidEgyptianPhoneNumber(phone)) {
            _registerState.value = Result.Error("Invalid phone number format")
            return
        }
        if (!Validator.isValidEmail(email)) {
            _registerState.value = Result.Error("Invalid email address")
            return
        }
        if (!Validator.isValidPassword(password!!)) {
            _registerState.value = Result.Error("Password must be 8+ chars with upper, lower and a digit")
            return
        }
        _registerState.value = Result.Loading
        registerJob = launchLatest(registerJob) {
            val result = authRepository.register(name, phone, email, password)
            _registerState.value = result
        }
    }

    /**
     * Change the current user's password. Updates [changePasswordState].
     */
    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        if (Validator.isEmpty(currentPassword) || Validator.isEmpty(newPassword) || Validator.isEmpty(confirmPassword)) {
            _changePasswordState.value = Result.Error("All fields are required")
            return
        }
        if (!Validator.isValidPassword(newPassword)) {
            _changePasswordState.value = Result.Error("New password too weak")
            return
        }
        if (newPassword != confirmPassword) {
            _changePasswordState.value = Result.Error("Passwords do not match")
            return
        }
        _changePasswordState.value = Result.Loading
        changePwdJob = launchLatest(changePwdJob) {
            val result = authRepository.changePassword(currentPassword, newPassword, confirmPassword)
            _changePasswordState.value = result
        }
    }

    /**
     * Send a password reset code to the specified email. Updates [sendResetMailState].
     */
    fun sendResetMail(email: String) {
        if (!Validator.isValidEmail(email)) {
            _sendResetMailState.value = Result.Error("Invalid email address")
            return
        }
        _sendResetMailState.value = Result.Loading
        sendMailJob = launchLatest(sendMailJob) {
            val result = authRepository.sendResetMail(email)
            _sendResetMailState.value = result
        }
    }

    /**
     * Verify the reset code sent to the user's email. Updates [verifyCodeState].
     */
    fun verifyResetCode(email: String, code: String) {
        if (!Validator.isValidEmail(email)) {
            _verifyCodeState.value = Result.Error("Invalid email address")
            return
        }
        if (code.length != 4 || !code.all { it.isDigit() }) {
            _verifyCodeState.value = Result.Error("Enter the 4-digit code")
            return
        }
        _verifyCodeState.value = Result.Loading
        verifyCodeJob = launchLatest(verifyCodeJob) {
            val result = authRepository.verifyResetCode(email, code)
            _verifyCodeState.value = result
        }
    }

    /**
     * Reset the user's password using the verification code. Updates [resetPasswordState].
     */
    fun resetPassword(email: String, code: String, password: String, confirm: String) {
        if (!Validator.isValidEmail(email)) {
            _resetPasswordState.value = Result.Error("Invalid email address")
            return
        }
        if (code.length != 4 || !code.all { it.isDigit() }) {
            _resetPasswordState.value = Result.Error("Enter the 4-digit code")
            return
        }
        if (!Validator.isValidPassword(password)) {
            _resetPasswordState.value = Result.Error("Password must be 8+ chars with upper, lower and a digit")
            return
        }
        if (password != confirm) {
            _resetPasswordState.value = Result.Error("Passwords do not match")
            return
        }
        _resetPasswordState.value = Result.Loading
        resetPwdJob = launchLatest(resetPwdJob) {
            val result = authRepository.resetPassword(email, code, password, confirm)
            _resetPasswordState.value = result
        }
    }

    fun clearLoginState() { _loginState.value = null }
    fun clearRegisterState() { _registerState.value = null }
    fun clearChangePasswordState() { _changePasswordState.value = null }
    fun clearSendResetMailState() { _sendResetMailState.value = null }
    fun clearVerifyCodeState() { _verifyCodeState.value = null }
    fun clearResetPasswordState() { _resetPasswordState.value = null }
}

/**
 * Factory to create [AuthViewModel] with required dependencies. This allows providing
 * [PreferenceManager] and [AuthRepository] without relying on a dependency injection framework.
 */
class AuthViewModelFactory(
    private val preferenceManager: PreferenceManager,
    private val apiService: com.breakfast.network.ApiService,
    private val context: android.content.Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            val repository = AuthRepository(apiService, preferenceManager, context)
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}