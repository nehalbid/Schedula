package app.schedula.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.schedula.ui.profile.ProfileScreen
import app.schedula.ui.support.SupportCenterScreen

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "profile") {
        composable("profile") {
            ProfileScreen(
                onLogout = { /* Not implemented */ },
                onSupportClick = { navController.navigate("support") },
                onRateClick = { /* Not implemented */ }
            )
        }
        composable("support") {
            SupportCenterScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}