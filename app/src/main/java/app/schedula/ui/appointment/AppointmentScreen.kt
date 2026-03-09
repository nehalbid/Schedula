package app.schedula.ui.appointment

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.schedula.R
import app.schedula.data.model.Appointment
import app.schedula.data.model.AppointmentStatus
import app.schedula.ui.components.StatusBadge
import kotlinx.coroutines.delay

@Composable
fun AppointmentScreen(
    viewModel: AppointmentViewModel = viewModel(),
    onViewDetails: (String) -> Unit
) {

    // 🔥 Correct property from ViewModel
    val appointments =
        viewModel.appointments
            .collectAsState(initial = emptyList())
            .value

    val context = LocalContext.current

    // 🔔 Trigger notification when ONGOING
    LaunchedEffect(appointments) {
        viewModel.checkForOngoingAndNotify(context)
    }

    if (appointments.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No active appointments.")
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        items(
            items = appointments,
            key = { appointment: Appointment -> appointment.id }
        ) { appointment ->

            AppointmentCard(
                appointment = appointment,
                viewModel = viewModel,
                onViewDetails = onViewDetails
            )
        }
    }
}

@Composable
fun AppointmentCard(
    appointment: Appointment,
    viewModel: AppointmentViewModel,
    onViewDetails: (String) -> Unit
) {

    val queuePosition = viewModel.getQueuePosition(appointment)
    val doctorBusy = viewModel.isDoctorBusy(appointment)

    var remainingSeconds by remember(appointment.id, appointment.status, queuePosition) {
        mutableStateOf(viewModel.getRemainingSeconds(appointment))
    }

    // 🔥 Stable live countdown
    LaunchedEffect(
        appointment.status,
        appointment.checkedInAt,
        queuePosition
    ) {

        if (appointment.status == AppointmentStatus.CHECKED_IN.name) {

            while (true) {

                remainingSeconds =
                    viewModel.getRemainingSeconds(appointment)

                if (remainingSeconds <= 0) break

                delay(1000)
            }
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {

        Column(Modifier.padding(18.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Image(
                    painter = painterResource(
                        if (appointment.doctorGender
                                .lowercase() == "male"
                        ) R.drawable.doctor_male
                        else R.drawable.doctor_female
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                )

                Spacer(Modifier.width(14.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        appointment.doctorName,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        appointment.doctorSpecialty,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                StatusBadge(appointment.status)
            }

            Spacer(Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(6.dp))
                Text("${appointment.date} | ${appointment.time}", fontWeight = FontWeight.Medium)
            }

            // 🔥 CHECKED IN STATE
            if (appointment.status ==
                AppointmentStatus.CHECKED_IN.name
            ) {

                Spacer(Modifier.height(12.dp))

                if (queuePosition > 1) {

                    Text(
                        "You are #$queuePosition in queue",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    val minutes = remainingSeconds / 60
                    val seconds = remainingSeconds % 60

                    Text(
                        "Estimated Wait: ${minutes}m ${seconds}s",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                } else {
                    Text(
                        "You are next in queue",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }

                if (doctorBusy) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Doctor is attending another patient",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // 🔥 ONGOING STATE
            if (appointment.status ==
                AppointmentStatus.ONGOING.name
            ) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Your consultation is in progress",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }

            // 🔥 CHECK-IN BUTTON
            if (viewModel.canCheckIn(appointment)) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        viewModel.checkIn(appointment.id)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("I Have Arrived", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    onViewDetails(appointment.id)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("View Details", fontWeight = FontWeight.Bold)
            }
        }
    }
}
