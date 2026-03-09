package app.schedula.ui.record

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.schedula.ui.appointment.AppointmentDetailViewModel
import app.schedula.ui.components.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDetailScreen(
    appointmentId: String,
    viewModel: AppointmentDetailViewModel = viewModel(),
    onBack: () -> Unit
) {

    LaunchedEffect(appointmentId) {
        viewModel.loadAppointment(appointmentId)
    }

    val appointment by viewModel.appointment.collectAsState()

    if (appointment == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Record Details", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF6F8FB)
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF6F8FB))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            // Top Status
            appointment?.let { 
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    StatusBadge(it.status)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // PART 1: BOOKING DETAILS
            RecordSectionHeader("BOOKING DETAILS")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    RecordDetailRow("Booking ID", appointment?.id ?: "", Icons.Default.Tag)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color.LightGray)
                    RecordDetailRow("Date", appointment?.date ?: "", Icons.Default.CalendarMonth)
                    RecordDetailRow("Time", appointment?.time ?: "", Icons.Default.Schedule)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Payment Info", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    appointment?.paymentDetails?.let { payment ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Amount", color = Color.Gray, fontSize = 14.sp)
                            Text("₹${payment.totalAmount}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Text("Status: ${payment.status}", color = if (payment.status == "PAID") Color(0xFF4CAF50) else Color.Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // PART 2: DOCTOR DETAILS
            RecordSectionHeader("DOCTOR DETAILS")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    RecordDetailRow("Doctor Name", appointment?.doctorName ?: "", Icons.Default.Person)
                    RecordDetailRow("Specialization", appointment?.doctorSpecialty ?: "", Icons.Default.MedicalServices)
                    RecordDetailRow("Location", "Medical Center, Room 402", Icons.Default.LocationOn)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // PART 3: PATIENT DETAILS
            RecordSectionHeader("PATIENT DETAILS")
            appointment?.patientDetails?.let { patient ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        RecordDetailRow("Patient Name", patient.fullName, Icons.Default.AccountCircle)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.weight(1f)) { RecordDetailRow("Gender", patient.gender, Icons.Default.Wc) }
                            Box(modifier = Modifier.weight(1f)) { RecordDetailRow("Blood", patient.bloodType, Icons.Default.Bloodtype) }
                        }
                        RecordDetailRow("Contact", patient.contactNumber, Icons.Default.Phone)
                        RecordDetailRow("Complaint", patient.complaint, Icons.AutoMirrored.Filled.Notes)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RecordSectionHeader(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 13.sp,
        color = Color.Gray,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun RecordDetailRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, color = Color.Gray, fontSize = 11.sp)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
        }
    }
}
