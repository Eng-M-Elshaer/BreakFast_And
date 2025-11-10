package com.breakfast.repositories

import android.content.Context
import com.breakfast.R
import com.breakfast.managers.PreferenceManager
import com.breakfast.models.ApiResponse
import com.breakfast.models.UserModel
import com.breakfast.network.ApiService
import com.breakfast.network.LoginRequest
import com.breakfast.network.RegisterRequest
import com.breakfast.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Repository that handles authentication related operations. It encapsulates the network calls
 * and stores the received token and user data in the [PreferenceManager].
 */
class AuthRepository(
    private val apiService: ApiService,
    private val preferenceManager: PreferenceManager,
    private val context: Context
) {

    /**
     * Perform user login. This method makes a network request using the provided credentials.
     * On success it stores the token and user information.
     */
    suspend fun login(phone: String, password: String): Result<ApiResponse<UserModel>> = withContext(Dispatchers.IO) {
        try {
            val fcm = preferenceManager.getFcmToken()   // ← هنا
            val response = apiService.login(LoginRequest(phone = phone, password = password, socialId = null, fcmToken = fcm, socialType = null))
            // Persist token and user in encrypted shared preferences
            preferenceManager.saveToken(response.data?.token)
            preferenceManager.saveUser(response.data?.user)
            Result.Success(response)
        } catch (e: HttpException) {
            Result.Error(e.response()?.errorBody()?.string())
        } catch (e: IOException) {
            Result.Error(context.getString(R.string.no_internet))
        } catch (e: Exception) {
            Result.Error(e.localizedMessage)
        }
    }

    /**
     * Perform user registration. On success it stores the token and user info.
     */
    suspend fun register(name: String, phone: String, email: String, password: String?): Result<ApiResponse<UserModel>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.register(RegisterRequest(name = name, phone = phone, email = email, password = password))
            preferenceManager.saveToken(response.data?.token)
            preferenceManager.saveUser(response.data?.user)
            Result.Success(response)
        } catch (e: HttpException) {
            Result.Error(e.response()?.errorBody()?.string())
        } catch (e: IOException) {
            Result.Error(context.getString(R.string.no_internet))
        } catch (e: Exception) {
            Result.Error(e.localizedMessage)
        }
    }

    /**
     * Change the user's password while logged in. On success, updates the stored user info if returned.
     */
    suspend fun changePassword(currentPassword: String, newPassword: String, confirm: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            apiService.changePassword(
                com.breakfast.network.ChangePasswordRequest(
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    passwordConfirmation = confirm
                )
            )
            Result.Success(Unit)
        } catch (e: HttpException) {
            Result.Error(e.response()?.errorBody()?.string())
        } catch (e: IOException) {
            Result.Error(context.getString(R.string.no_internet))
        } catch (e: Exception) {
            Result.Error(e.localizedMessage)
        }
    }

    /**
     * Send a password reset code to the user's email.
     */
    suspend fun sendResetMail(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            apiService.forgetPassword(com.breakfast.network.ForgetPasswordRequest(email))
            Result.Success(Unit)
        } catch (e: HttpException) {
            Result.Error(e.response()?.errorBody()?.string())
        } catch (e: IOException) {
            Result.Error(context.getString(R.string.no_internet))
        } catch (e: Exception) {
            Result.Error(e.localizedMessage)
        }
    }

    /**
     * Verify the password reset code.
     */
    suspend fun verifyResetCode(email: String, code: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            apiService.verifyResetCode(
                com.breakfast.network.ForgetPasswordVerifyRequest(email = email, code = code)
            )
            Result.Success(Unit)
        } catch (e: HttpException) {
            Result.Error(e.response()?.errorBody()?.string())
        } catch (e: IOException) {
            Result.Error(context.getString(R.string.no_internet))
        } catch (e: Exception) {
            Result.Error(e.localizedMessage)
        }
    }

    /**
     * Reset the user's password using the provided verification code.
     */
    suspend fun resetPassword(email: String, code: String, password: String, confirm: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            apiService.resetPassword(
                com.breakfast.network.ForgetPasswordResetRequest(
                    email = email,
                    code = code,
                    password = password,
                    passwordConfirmation = confirm
                )
            )
            Result.Success(Unit)
        } catch (e: HttpException) {
            Result.Error(e.response()?.errorBody()?.string())
        } catch (e: IOException) {
            Result.Error(context.getString(R.string.no_internet))
        } catch (e: Exception) {
            Result.Error(e.localizedMessage)
        }
    }
}