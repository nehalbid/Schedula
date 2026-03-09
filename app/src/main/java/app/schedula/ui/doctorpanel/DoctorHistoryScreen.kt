package app.schedula.ui.doctorpanel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.schedula.data.model.AppointmentStatus
import app.schedula.ui.doctorpanel.DoctorPanelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorHistoryScreen(
    onBack: () -> Unit,
    viewModel: DoctorPanelViewModel = viewModel()
) {

    val historyAppointments by viewModel.historyAppointments.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointment History") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->

        if (historyAppointments.isEmpty()) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No history yet.")
            }

        } else {

            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .background(Color(0xFFF6F8FB)),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(historyAppointments.sortedByDescending { it.completedAt ?: it.createdAt }) { appointment ->

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Text(
                                appointment.patientDetails?.fullName ?: "",
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text("Date: ${appointment.date}")
                            Text("Time: ${appointment.time}")

                            Spacer(modifier = Modifier.height(6.dp))

                            val statusColor = when (appointment.status) {
                                AppointmentStatus.COMPLETED.name -> Color(0xFF2E7D32)
                                AppointmentStatus.CANCELLED.name -> Color.Red
                                AppointmentStatus.NO_SHOW.name -> Color(0xFFFF9800)
                                else -> Color.Gray
                            }

                            Text(
                                "Status: ${appointment.status}",
                                color = statusColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}