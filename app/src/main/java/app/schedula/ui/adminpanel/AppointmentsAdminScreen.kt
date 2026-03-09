package app.schedula.ui.adminpanel

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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.schedula.data.repository.AdminAppointment
import app.schedula.ui.components.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsAdminScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = viewModel()
) {
    val appointments by viewModel.appointments.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointments", fontWeight = FontWeight.Bold) },
                actions = {
                    Button(
                        onClick = onBack,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD1B2E0))
                    ) {
                        Text("Back", color = Color.Black)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(appointments) { appt ->
                AppointmentCard(appt)
            }
        }
    }
}

@Composable
fun AppointmentCard(appt: AdminAppointment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3E3A42))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Doctor: ${appt.doctorName}",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Patient: ${appt.patientName}",
                        color = Color.LightGray,
                        fontSize = 14.sp
                    )
                }
                StatusBadge(status = appt.status)
            }

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f), thickness = 0.5.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Date: ${appt.date}",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Time: ${appt.time}",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = "Status: ${appt.status}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
