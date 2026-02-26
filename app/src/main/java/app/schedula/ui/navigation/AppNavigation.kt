package app.schedula.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.schedula.ui.auth.AuthViewModel
import app.schedula.ui.auth.LoginScreen
import app.schedula.ui.auth.OtpScreen
import app.schedula.ui.records.PatientDetailsScreen


@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    val startDestination = remember {
        if (authViewModel.checkLoginStatus()) Routes.MAIN else Routes.LOGIN
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onCodeSent = { navController.navigate(Routes.OTP) }
            )
        }

        composable(Routes.OTP) {
            OtpScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBackClick = {
                    authViewModel.resetState()
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.MAIN) {
            MainBottomNav(onLogout = {
                authViewModel.logout()
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.MAIN) { inclusive = true }
                }
            })
        }

        composable(Routes.PATIENT_DETAILS) {
            PatientDetailsScreen(
                onBackClick = { navController.popBackStack() },
                onSaveClick = { navController.popBackStack() }
            )
        }
    }
}
