package app.schedula.ui.doctorpanel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.schedula.data.model.Appointment
import app.schedula.data.model.AppointmentStatus
import app.schedula.ui.navigation.Routes
import java.util.*

@Composable
fun DoctorDashboardScreen(
    onLogout: () -> Unit,
    viewModel: DoctorPanelViewModel = viewModel(),
    navController: NavController? = null
) {

    val doctor by viewModel.doctor.collectAsState()
    val appointments by viewModel.todayAppointments.collectAsState()

    var overrideTarget by remember { mutableStateOf<Appointment?>(null) }
    var prescriptionTarget by remember { mutableStateOf<Appointment?>(null) }

    val queue = appointments
        .filter { it.status == AppointmentStatus.CHECKED_IN.name }
        .sortedBy { it.checkedInAt }

    val ongoing = appointments.firstOrNull {
        it.status == AppointmentStatus.ONGOING.name
    }

    val completedCount = appointments.count {
        it.status == AppointmentStatus.COMPLETED.name
    }

    if (prescriptionTarget != null) {
        PrescriptionFormScreen(
            appointment = prescriptionTarget!!,
            onBack = { prescriptionTarget = null },
            onSubmit = { prescription ->
                viewModel.submitPrescription(prescription) {
                    viewModel.completeConsultation(prescriptionTarget!!.id)
                    prescriptionTarget = null
                }
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8FB))
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { navController?.navigate(Routes.DOCTOR_PROFILE) }
            ) {

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {

                    Text(
                        doctor?.name ?: "Doctor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        doctor?.specialty ?: "Loading...",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            Row {

                IconButton(
                    onClick = { navController?.navigate(Routes.DOCTOR_SCHEDULE) }
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = "Schedule")
                }

                TextButton(
                    onClick = { navController?.navigate(Routes.DOCTOR_HISTORY) }
                ) {
                    Text("History")
                }

                TextButton(onClick = onLogout) {
                    Text("Logout")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Waiting", color = Color.Gray, fontSize = 12.sp)
                    Text("${queue.size}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Ongoing", color = Color.Gray, fontSize = 12.sp)
                    Text("${if (ongoing != null) 1 else 0}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Completed", color = Color.Gray, fontSize = 12.sp)
                    Text("$completedCount", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.callNextPatient() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = queue.isNotEmpty() && ongoing == null,
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Call Next Patient", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Ongoing Consultation", fontWeight = FontWeight.Bold, fontSize = 18.sp)

        Spacer(modifier = Modifier.height(12.dp))

        if (ongoing != null) {

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {

                Column(Modifier.padding(16.dp)) {

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                ongoing.patientDetails?.fullName?.take(1) ?: "P",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                ongoing.patientDetails?.fullName ?: "Unknown",
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                ongoing.consultingType,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (ongoing.meetingLink != null) {

                        Text(
                            "Meeting: ${ongoing.meetingLink}",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Button(
                            onClick = { prescriptionTarget = ongoing },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Write Prescription")
                        }

                        OutlinedButton(
                            onClick = { viewModel.completeConsultation(ongoing.id) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Complete Only")
                        }
                    }
                }
            }

        } else {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color.White, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("No active session.", color = Color.LightGray)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Queue Today", fontWeight = FontWeight.Bold, fontSize = 18.sp)

        Spacer(modifier = Modifier.height(12.dp))

        if (queue.isEmpty()) {

            Text("No patients waiting.", color = Color.Gray)

        } else {

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                itemsIndexed(queue) { index, appointment ->

                    val waitingMinutes =
                        appointment.checkedInAt?.let {
                            ((Date().time - it.time) / 60000)
                        } ?: 0

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {

                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                "#${index + 1}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(Modifier.weight(1f)) {

                                Text(
                                    appointment.patientDetails?.fullName ?: "Patient",
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    "$waitingMinutes mins waiting",
                                    fontSize = 12.sp,
                                    color = Color.Red
                                )
                            }

                            IconButton(onClick = { overrideTarget = appointment }) {
                                Icon(
                                    Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = Color(0xFFFFA500)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    overrideTarget?.let { target ->

        AlertDialog(
            onDismissRequest = { overrideTarget = null },

            confirmButton = {
                TextButton(onClick = {
                    viewModel.forceStart(target)
                    overrideTarget = null
                }) {
                    Text("Confirm")
                }
            },

            dismissButton = {
                TextButton(onClick = { overrideTarget = null }) {
                    Text("Cancel")
                }
            },

            title = { Text("Emergency Override") },

            text = {
                Text(
                    "Force start session with ${target.patientDetails?.fullName}? Current session will be completed."
                )
            }
        )
    }
}