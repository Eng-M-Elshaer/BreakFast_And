
package com.breakfast.ui.navigation

import ForgotPasswordScreen
import LoginScreen
import MainScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.breakfast.ui.auth.*
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.breakfast.managers.PreferenceManager
 

// Central object to hold all route names
private object Routes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val FORGOT = "forgot_password"
    const val VERIFY = "verify_code/{email}"
    const val RESET = "reset_password/{email}/{code}"
    const val CHANGE = "change_password"
    const val HOME = "home"    // root of bottom nav
    const val MAIN = "main"    // legacy alias if used elsewhere
}

/**
 * Defines the navigation graph for the Breakfast app. This is a simple example that
 * maps route names to composable screens. In a real app you would pass callbacks to
 * navigate to other screens when actions occur.
 */
@Composable
fun BreakfastNavGraph(startDestination: String = "login", navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val computedStart = remember {
        val token = try {
            PreferenceManager(context).getToken()
        } catch (_: Exception) { null }
        if (!token.isNullOrBlank()) Routes.HOME else Routes.LOGIN
    }
    NavHost(navController = navController, startDestination = computedStart) {
        // Authentication flow
        composable(Routes.LOGIN) { LoginScreen(navController = navController, onSignUp = { navController.navigate(Routes.SIGNUP) }) }
        composable(Routes.SIGNUP) { SignUpScreen(navController = navController, onSignUpSuccess = { navController.navigate(Routes.HOME) }) }
        // Forgot password flow routes with arguments
        composable(Routes.FORGOT) {
            // Pass navController explicitly so the screen can navigate on success
            ForgotPasswordScreen(navController = navController)
        }
        composable(Routes.VERIFY) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            VerifyScreen(email = email, navController = navController)
        }
        composable(Routes.RESET) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val code = backStackEntry.arguments?.getString("code") ?: ""
            ResetPasswordScreen(email = email, code = code, navController = navController)
        }
        composable(Routes.CHANGE) { ChangePasswordScreen(navController = navController) }

        // Main app with bottom navigation
        composable(Routes.HOME) { MainScreen(navController) }
        // Backward-compat alias if some screens still navigate to "main"
        composable(Routes.MAIN) { MainScreen(navController) }
    }
}