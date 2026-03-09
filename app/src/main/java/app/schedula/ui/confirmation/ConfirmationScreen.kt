package app.schedula.ui.confirmation

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.schedula.R
import app.schedula.data.model.Appointment
import app.schedula.data.model.Doctor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationScreen(
    appointmentId: String,
    isReschedule: Boolean = false,
    onBackClick: () -> Unit,
    onViewAppointmentClick: () -> Unit
) {
    val viewModel: ConfirmationViewModel = viewModel()
    val appointment by viewModel.appointment.collectAsState()
    val doctor by viewModel.doctor.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(appointmentId) {
        viewModel.loadAppointmentDetails(appointmentId)
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            TopAppBar(
                title = { Text("Confirmation", color = Color.Black, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                if (isReschedule) "Appointment Rescheduled!" else "Appointment Confirmed!",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (isReschedule) {
                    "Your appointment with Dr. ${doctor?.name ?: "..."} has been successfully rescheduled."
                } else {
                    "Your booking with Dr. ${doctor?.name ?: "..."} has been successfully scheduled and added to our system."
                },
                textAlign = TextAlign.Center,
                color = Color.DarkGray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
            BookingDetailsCard(
                bookingId = appointment?.id ?: "...",
                date = appointment?.date ?: "...",
                time = appointment?.time ?: "..."
            )
            Spacer(modifier = Modifier.height(16.dp))
            InstructionsCard()
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { 
                    appointment?.let { appt ->
                        addToCalendar(context, appt, doctor)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                Spacer(Modifier.width(8.dp))
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
                Icon(
                    imageVector = Icons.Default.Visibility, 
                    contentDescription = null,
                    tint = Color(0xFF311B92) // Darker shade of primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "View My Appointment",
                    color = Color(0xFF311B92) // Darker shade of primary
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            ClinicMapCard()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun addToCalendar(context: Context, appointment: Appointment, doctor: Doctor?) {
    val startTime = appointment.appointmentDate?.time ?: return
    val endTime = startTime + 30 * 60 * 1000 // Assume 30 min duration

    val intent = Intent(Intent.ACTION_INSERT)
        .setData(CalendarContract.Events.CONTENT_URI)
        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
        .putExtra(CalendarContract.Events.TITLE, "Appointment with Dr. ${doctor?.name ?: appointment.doctorName}")
        .putExtra(CalendarContract.Events.DESCRIPTION, "Medical Consultation - ${appointment.consultingType}")
        .putExtra(CalendarContract.Events.EVENT_LOCATION, "St. Mary's Clinic")
        .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)

    context.startActivity(intent)
}

@Composable
fun BookingDetailsCard(bookingId: String, date: String, time: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("BOOKING DETAILS", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Booking ID: $bookingId", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text("Date: $date", color = Color.DarkGray, fontSize = 14.sp)
                Text("Time: $time", color = Color.DarkGray, fontSize = 14.sp)
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_doctor),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
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
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Important Instructions", fontWeight = FontWeight.Bold, color = Color.Black)
                Text(
                    "Please arrive at least 15 minutes before your scheduled appointment time for check-in and vitals recording.",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(4.dp))
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
            .height(150.dp)
            .background(Color.LightGray)) {
            Text("Map View Placeholder", modifier = Modifier.align(Alignment.Center), color = Color.DarkGray)
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
                Text("St. Mary's Clinic", fontSize = 12.sp, color = Color.Black)
            }
        }
    }
}
