package app.schedula.ui.record

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.schedula.R
import app.schedula.data.model.Appointment

@Composable
fun RecordsScreen(
    viewModel: RecordViewModel = viewModel(),
    onViewDetails: (String) -> Unit
) {

    val appointments by viewModel.appointments.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8FB))
    ) {

        Text(
            "Medical Records",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(20.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (appointments.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("You have no medical records.")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                items(appointments) { appointment ->
                    RecordCard(appointment, onViewDetails)
                }
            }
        }
    }
}

@Composable
fun RecordCard(
    appointment: Appointment,
    onViewDetails: (String) -> Unit
) {

    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        modifier = Modifier.clickable { onViewDetails(appointment.id) }
    ) {

        Column(modifier = Modifier.padding(18.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Image(
                    painter = painterResource(
                        if (appointment.doctorGender.equals("male", ignoreCase = true)) R.drawable.doctor_male
                        else R.drawable.doctor_female
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        appointment.doctorName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    // Specialty is not in the Appointment model.
                    // You would need to fetch doctor details separately to display this.
                }

                StatusBadge(appointment.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Icon(Icons.Default.CalendarMonth, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("${appointment.day} | ${appointment.time}")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onViewDetails(appointment.id) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("View Details")
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {

    val color = when (status) {
        "UPCOMING" -> Color(0xFF1976D2)
        "Completed" -> Color.Gray
        "Cancelled" -> Color.Red
        else -> Color.DarkGray
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(status.uppercase(), color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
