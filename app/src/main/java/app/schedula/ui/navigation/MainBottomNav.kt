package app.schedula.ui.navigation

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import app.schedula.ui.appointment.AppointmentDetailScreen
import app.schedula.ui.appointment.AppointmentScreen
import app.schedula.ui.appointment.CancelAppointmentScreen
import app.schedula.ui.booking.BookingScreen
import app.schedula.ui.booking.SlotUnavailableScreen
import app.schedula.ui.confirmation.ConfirmationScreen
import app.schedula.ui.doctor.DoctorDetailsScreen
import app.schedula.ui.home.HomeScreen
import app.schedula.ui.profile.FamilyMembersScreen
import app.schedula.ui.profile.PersonalInfoScreen
import app.schedula.ui.profile.ProfileScreen
import app.schedula.ui.record.RecordsScreen
import app.schedula.ui.support.SupportCenterScreen

@Composable
fun MainBottomNav(
    onLogout: () -> Unit, 
    navController: NavHostController,
    rootNavController: NavHostController,
    initialTab: String = Routes.DOCTORS
) {

    val haptics = LocalHapticFeedback.current
    val context = LocalContext.current

    val items = listOf(
        BottomNavItem(Routes.DOCTORS, "Doctors", Icons.Default.LocalHospital),
        BottomNavItem(Routes.RECORDS, "Records", Icons.Default.Description),
        BottomNavItem(Routes.APPOINTMENTS, "Appointments", Icons.Default.CalendarMonth),
        BottomNavItem(Routes.PROFILE, "Profile", Icons.Default.Person)
    )

    var bottomBarVisible by remember { mutableStateOf(true) }

    val scrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: androidx.compose.ui.geometry.Offset,
                source: NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {

                if (available.y < -5) bottomBarVisible = false
                if (available.y > 5) bottomBarVisible = true

                return androidx.compose.ui.geometry.Offset.Zero
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity
            ): Velocity {
                if (consumed.y < -2000f) bottomBarVisible = false
                return Velocity.Zero
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollConnection)
    ) {

        NavHost(
            navController = navController,
            startDestination = initialTab,
            modifier = Modifier.fillMaxSize()
        ) {

            composable(Routes.DOCTORS) {
                HomeScreen(
                    onDoctorClick = {
                        navController.navigate("${Routes.DOCTOR_DETAILS}/$it")
                    }
                )
            }

            composable(Routes.RECORDS) {
                RecordsScreen(onViewDetails = {
                    navController.navigate("${Routes.APPOINTMENT_DETAILS}/$it")
                })
            }

            composable(Routes.APPOINTMENTS) {
                AppointmentScreen(
                    onViewDetails = {
                        navController.navigate("${Routes.APPOINTMENT_DETAILS}/$it")
                    }
                )
            }

            composable(Routes.PROFILE) {
                ProfileScreen(
                    onLogout = onLogout,
                    onSupportClick = {
                        navController.navigate(Routes.SUPPORT)
                    },
                    onRateClick = {
                        val packageName = context.packageName
                        try {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                            )
                        } catch (e: android.content.ActivityNotFoundException) {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
                            )
                        }
                    },
                    onPersonalInfoClick = {
                        navController.navigate(Routes.PERSONAL_INFO)
                    },
                    onFamilyMembersClick = {
                        navController.navigate(Routes.FAMILY_MEMBERS)
                    }
                )
            }

            composable(Routes.SUPPORT) {
                SupportCenterScreen(onBackClick = { navController.popBackStack() })
            }

            composable(Routes.PERSONAL_INFO) {
                PersonalInfoScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.FAMILY_MEMBERS) {
                FamilyMembersScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                "${Routes.DOCTOR_DETAILS}/{doctorId}",
                arguments = listOf(navArgument("doctorId") {
                    type = NavType.StringType
                })
            ) {
                val doctorId = it.arguments?.getString("doctorId") ?: ""
                DoctorDetailsScreen(
                    doctorId = doctorId,
                    onBackClick = { navController.popBackStack() },
                    onBookAppointmentClick = {
                        navController.navigate("${Routes.BOOKING}/$doctorId")
                    }
                )
            }

            composable(
                "${Routes.BOOKING}/{doctorId}",
                arguments = listOf(navArgument("doctorId") {
                    type = NavType.StringType
                })
            ) {
                val doctorId = it.arguments?.getString("doctorId") ?: ""
                val viewModelStoreOwner = remember(it) {
                    rootNavController.getBackStackEntry("${Routes.MAIN}?initialTab={initialTab}")
                }
                BookingScreen(
                    doctorId = doctorId,
                    onBackClick = { navController.popBackStack() },
                    onBookingSuccess = { appointmentId ->
                        navController.navigate("${Routes.CONFIRMATION}/$appointmentId?isReschedule=false") {
                            popUpTo("${Routes.BOOKING}/$doctorId") { inclusive = true }
                        }
                    },
                    onSlotUnavailable = {
                        navController.navigate(Routes.SLOT_UNAVAILABLE)
                    },
                    onAuthError = {},
                    onPatientDetailsClick = { rootNavController.navigate(Routes.PATIENT_DETAILS) },
                    bookingViewModel = viewModel(viewModelStoreOwner)
                )
            }

            composable(Routes.SLOT_UNAVAILABLE) {
                SlotUnavailableScreen(
                    onSeeNextAvailable = { navController.popBackStack() },
                    onReturnToCalendar = { navController.popBackStack() },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                "${Routes.CONFIRMATION}/{appointmentId}?isReschedule={isReschedule}",
                arguments = listOf(
                    navArgument("appointmentId") { type = NavType.StringType },
                    navArgument("isReschedule") { type = NavType.BoolType; defaultValue = false }
                )
            ) {
                val appointmentId = it.arguments?.getString("appointmentId") ?: ""
                val isReschedule = it.arguments?.getBoolean("isReschedule") ?: false
                 ConfirmationScreen(
                    appointmentId = appointmentId,
                    isReschedule = isReschedule,
                    onBackClick = { navController.popBackStack() },
                    onViewAppointmentClick = {
                        navController.navigate(Routes.APPOINTMENTS) {
                            popUpTo(Routes.DOCTORS)
                        }
                    }
                )
            }

            composable(
                "${Routes.APPOINTMENT_DETAILS}/{appointmentId}",
                arguments = listOf(navArgument("appointmentId") {
                    type = NavType.StringType
                })
            ) {
                val appointmentId = it.arguments?.getString("appointmentId") ?: ""
                AppointmentDetailScreen(
                    appointmentId = appointmentId,
                    onBack = { navController.popBackStack() },
                    onCancelClick = {
                        navController.navigate("${Routes.CANCEL_APPOINTMENT}/$appointmentId")
                    },
                    onRescheduleSuccess = { newId ->
                        navController.navigate("${Routes.CONFIRMATION}/$newId?isReschedule=true") {
                            popUpTo("${Routes.APPOINTMENT_DETAILS}/$appointmentId") { inclusive = true }
                        }
                    }
                )
            }

            composable(
                "${Routes.CANCEL_APPOINTMENT}/{appointmentId}",
                arguments = listOf(navArgument("appointmentId") {
                    type = NavType.StringType
                })
            ) {
                val appointmentId = it.arguments?.getString("appointmentId") ?: ""
                CancelAppointmentScreen(
                    appointmentId = appointmentId,
                    onBack = { navController.popBackStack() },
                    onCancellationSuccess = {
                        navController.navigate(Routes.RECORDS) {
                            popUpTo(Routes.APPOINTMENTS) { inclusive = true }
                        }
                    }
                )
            }
        }

        FloatingGlassBottomBar(
            navController = navController,
            items = items,
            visible = bottomBarVisible,
            haptics = haptics,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/* ---------- FLOATING GLASS NAV BAR ---------- */

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
private fun FloatingGlassBottomBar(
    navController: NavHostController,
    items: List<BottomNavItem>,
    visible: Boolean,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback,
    modifier: Modifier = Modifier
) {

    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    val offsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 120.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = ""
    )

    Box(
        modifier = modifier
            .offset(y = offsetY)
            .padding(bottom = 18.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {

        Surface(
            shape = RoundedCornerShape(30.dp),
            tonalElevation = 8.dp,
            shadowElevation = 20.dp,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        ) {

            Row(
                modifier = Modifier
                    .height(72.dp)
                    .padding(horizontal = 26.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                items.forEach { item ->

                    val selected = currentRoute == item.route

                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1.15f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ), label = ""
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                haptics.performHapticFeedback(
                                    HapticFeedbackType.TextHandleMove
                                )

                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                },
                            tint = if (selected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )

                        AnimatedVisibility(visible = selected) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = item.label,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
