package app.schedula.ui.appointment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.schedula.ui.home.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailScreen(
    appointmentId: String,
    viewModel: AppointmentDetailViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel(),
    onBack: () -> Unit,
    onCancelClick: (String) -> Unit
) {

    LaunchedEffect(appointmentId) {
        viewModel.loadAppointment(appointmentId)
    }

    val appointment by viewModel.appointment.collectAsState()
    val doctors by homeViewModel.doctors.collectAsState()
    val doctor = doctors.find { it.id == appointment?.doctorId }

    if (appointment == null) return

    val formattedDate = appointment?.createdAt?.let {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it)
    } ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointment Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF6F8FB))
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {

            appointment?.let { StatusBadge(it.status) }

            Spacer(modifier = Modifier.height(20.dp))

            appointment?.let { DetailCard("Doctor", it.doctorName) }
            doctor?.let { DetailCard("Specialty", it.specialty) }
            DetailCard("Date", formattedDate, Icons.Default.CalendarMonth)
            appointment?.let { DetailCard("Time", it.time, Icons.Default.Schedule) }
            DetailCard("Location", "Medical Center, Room 402", Icons.Default.LocationOn)

            Spacer(modifier = Modifier.height(24.dp))

            if (appointment?.status == "UPCOMING") {
                Button(
                    onClick = { onCancelClick(appointmentId) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel Appointment")
                }
            }
        }
    }
}

@Composable
fun DetailCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            icon?.let {
                Icon(it, null)
                Spacer(modifier = Modifier.width(10.dp))
            }
            Column {
                Text(label, color = Color.Gray, fontSize = 12.sp)
                Text(value, fontSize = 16.sp)
            }
        }
    }
}