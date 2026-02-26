package app.schedula.ui.confirmation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.schedula.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationScreen(
    appointmentId: String,
    onBackClick: () -> Unit,
    onViewAppointmentClick: () -> Unit
) {
    val viewModel: ConfirmationViewModel = viewModel()
    val appointment by viewModel.appointment.collectAsState()
    val doctor by viewModel.doctor.collectAsState()

    LaunchedEffect(appointmentId) {
        viewModel.loadAppointmentDetails(appointmentId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirmation") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
                .background(Color(0xFFF5F7FA))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Appointment Confirmed!",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Your booking with Dr. ${doctor?.name ?: "..."} has been successfully scheduled and added to our system.",
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))
            QueuePositionCard(appointment?.time ?: "...")
            Spacer(modifier = Modifier.height(16.dp))
            InstructionsCard()
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { /* Add to calendar */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                Text("Add to Calendar")
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onViewAppointmentClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.Visibility, contentDescription = null)
                Text("View My Appointment")
            }
            Spacer(modifier = Modifier.height(24.dp))
            ClinicMapCard()
        }
    }
}

@Composable
fun QueuePositionCard(time: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("QUEUE POSITION", color = Color.Gray, fontSize = 12.sp)
                Text("Token Number: #14", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Time: $time", color = Color.Gray, fontSize = 12.sp)
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_doctor), // Replace with your token icon
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
fun InstructionsCard() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                Text("Important Instructions", fontWeight = FontWeight.Bold)
                Text(
                    "Please arrive at least 15 minutes before your scheduled appointment time for check-in and vitals recording.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text("View Clinic Map ->", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ClinicMapCard() {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier
            .height(200.dp)
            .background(Color.LightGray)) {
            Text("300x300", modifier = Modifier.align(Alignment.Center))
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                Text("St. Mary's Clinic", fontSize = 12.sp)
            }
        }
    }
}
