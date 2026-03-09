package app.schedula.ui.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import app.schedula.ui.adminpanel.AddDoctorScreen
import app.schedula.ui.adminpanel.AdminDashboardScreen
import app.schedula.ui.adminpanel.AppointmentsAdminScreen
import app.schedula.ui.adminpanel.EditDoctorScreen
import app.schedula.ui.adminpanel.ManageDoctorsScreen
import app.schedula.ui.auth.*
import app.schedula.ui.booking.PatientDetailsScreen
import app.schedula.ui.booking.PaymentScreen
import app.schedula.ui.confirmation.ConfirmationScreen
import app.schedula.ui.doctorpanel.*

@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    val startDestination = Routes.LOGIN

    // Role based navigation
    LaunchedEffect(authState.first) {

        when (authState.first) {

            "patient_success" -> {
                navController.navigate(Routes.MAIN) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                    launchSingleTop = true
                }
            }

            "doctor_success" -> {
                navController.navigate(Routes.DOCTOR_DASHBOARD) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                    launchSingleTop = true
                }
            }

            "admin_success" -> {
                navController.navigate(Routes.ADMIN_DASHBOARD) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // LOGIN
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onCodeSent = { navController.navigate(Routes.OTP) }
            )
        }

        // OTP
        composable(Routes.OTP) {
            OtpScreen(
                viewModel = authViewModel,
                onLoginSuccess = {},
                onBackClick = {
                    authViewModel.resetState()
                    navController.popBackStack()
                }
            )
        }

        // PATIENT MAIN
        composable(
            route = "${Routes.MAIN}?initialTab={initialTab}",
            arguments = listOf(
                navArgument("initialTab") {
                    type = NavType.StringType
                    defaultValue = Routes.DOCTORS
                }
            )
        ) { backStackEntry ->

            val initialTab =
                backStackEntry.arguments?.getString("initialTab")
                    ?: Routes.DOCTORS

            val bottomNavController = rememberNavController()

            MainBottomNav(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                },
                navController = bottomNavController,
                rootNavController = navController,
                initialTab = initialTab
            )
        }

        // DOCTOR DASHBOARD
        composable(Routes.DOCTOR_DASHBOARD) {
            DoctorDashboardScreen(
                navController = navController, 
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.DOCTOR_DASHBOARD) { inclusive = true }
                    }
                }
            )
        }

        // DOCTOR HISTORY
        composable(Routes.DOCTOR_HISTORY) {
            DoctorHistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // DOCTOR SCHEDULE
        composable(Routes.DOCTOR_SCHEDULE) {
            DoctorScheduleScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // DOCTOR PROFILE
        composable(Routes.DOCTOR_PROFILE) {
            DoctorProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ADMIN DASHBOARD
        composable(Routes.ADMIN_DASHBOARD) {

            AdminDashboardScreen(

                onAddDoctor = {
                    navController.navigate(Routes.ADD_DOCTOR)
                },

                onManageDoctors = {
                    navController.navigate(Routes.MANAGE_DOCTORS)
                },

                onAppointments = {
                    navController.navigate(Routes.ADMIN_APPOINTMENTS)
                },

                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ADMIN_DASHBOARD) { inclusive = true }
                    }
                }
            )
        }

        // ADD DOCTOR
        composable(Routes.ADD_DOCTOR) {
            AddDoctorScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // EDIT DOCTOR
        composable(
            route = "${Routes.EDIT_DOCTOR}/{doctorId}",
            arguments = listOf(
                navArgument("doctorId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val doctorId =
                backStackEntry.arguments?.getString("doctorId") ?: ""

            EditDoctorScreen(
                doctorId = doctorId,
                onBack = { navController.popBackStack() }
            )
        }

        // MANAGE DOCTORS
        composable(Routes.MANAGE_DOCTORS) {
            ManageDoctorsScreen(
                onBack = { navController.popBackStack() },
                onEditDoctor = { doctor ->
                    navController.navigate("${Routes.EDIT_DOCTOR}/${doctor.id}")
                }
            )
        }

        // ADMIN APPOINTMENTS
        composable(Routes.ADMIN_APPOINTMENTS) {
            AppointmentsAdminScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // PATIENT DETAILS
        composable(Routes.PATIENT_DETAILS) { backStackEntry ->

            val viewModelStoreOwner = remember(backStackEntry) {
                navController.getBackStackEntry(
                    "${Routes.MAIN}?initialTab={initialTab}"
                )
            }

            PatientDetailsScreen(
                onBackClick = { navController.popBackStack() },
                onContinueToPayment = {
                    navController.navigate(Routes.PAYMENT)
                },
                bookingViewModel = viewModel(viewModelStoreOwner)
            )
        }

        // PAYMENT
        composable(Routes.PAYMENT) { backStackEntry ->

            val viewModelStoreOwner = remember(backStackEntry) {
                navController.getBackStackEntry(
                    "${Routes.MAIN}?initialTab={initialTab}"
                )
            }

            PaymentScreen(
                onBackClick = { navController.popBackStack() },
                onPaymentSuccess = { appointmentId ->

                    navController.navigate(
                        "${Routes.CONFIRMATION}/$appointmentId?isReschedule=false"
                    ) {

                        popUpTo("${Routes.MAIN}?initialTab={initialTab}") {
                            inclusive = false
                        }
                    }
                },
                bookingViewModel = viewModel(viewModelStoreOwner)
            )
        }

        // CONFIRMATION
        composable(
            "${Routes.CONFIRMATION}/{appointmentId}?isReschedule={isReschedule}",
            arguments = listOf(
                navArgument("appointmentId") {
                    type = NavType.StringType
                },
                navArgument("isReschedule") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) {

            val appointmentId =
                it.arguments?.getString("appointmentId") ?: ""

            val isReschedule =
                it.arguments?.getBoolean("isReschedule") ?: false

            ConfirmationScreen(
                appointmentId = appointmentId,
                isReschedule = isReschedule,
                onBackClick = {

                    navController.navigate(Routes.MAIN) {

                        popUpTo("${Routes.MAIN}?initialTab={initialTab}") {
                            inclusive = true
                        }
                    }
                },
                onViewAppointmentClick = {

                    navController.navigate(
                        "${Routes.MAIN}?initialTab=${Routes.APPOINTMENTS}"
                    ) {

                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}
